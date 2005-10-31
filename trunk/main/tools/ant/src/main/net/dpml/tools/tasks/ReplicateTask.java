/*
 * Copyright 2004 Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.tools.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.ResourceNotFoundException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;


/**
 * Consolidates a set of resources based on project dependencies.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ReplicateTask extends GenericTask
{
    private String m_key;
    private String m_ref;
    private File m_todir;
    private Path m_path;
    private boolean m_flatten = false;
    private boolean m_verbose = false;
    private boolean m_self = false;
    private ArrayList m_includes = new ArrayList();

   /**
    * Set the key of the target project or resource.
    *
    * @param key the resource key
    */
    public void setKey( final String key )
    {
        m_key = key;
    }

   /**
    * Set the ref of the target project or resource.
    *
    * @param ref the resource reference
    */
    public void setRef( final String ref )
    {
        m_ref = ref;
    }

   /**
    * Set the flattern policy.
    * @param flag the flattern policy
    */
    public void setFlatten( boolean flag )
    {
        m_flatten = flag;
    }

   /**
    * Set the verbose policy.
    * @param flag the verbose flag
    */
    public void setVerbose( boolean flag )
    {
        m_verbose = flag;
    }

   /**
    * Settting self to TRUE will result in expansion of the path to include
    * the target resource.
    * @param flag the self inclusion flag
    */
    public void setSelf( boolean flag )
    {
        m_self = flag;
    }
    
    public Include createInclude()
    {
        Include include = new Include();
        m_includes.add( include );
        return include;
    }

   /**
    * The id of a repository based path.
    * @param id the path identifier
    * @exception BuildException if the id does not reference a path, or the path is
    *  already set, or the id references an object that is not a path
    */
    public void setRefid( String id )
        throws BuildException
    {
        if( null != m_path )
        {
            final String error =
              "Path already set.";
            throw new BuildException( error );
        }

        Object ref = getProject().getReference( id );
        if( null == ref )
        {
            final String error =
              "Replication path id [" + id + "] is unknown.";
            throw new BuildException( error );
        }

        if( !( ref instanceof Path ) )
        {
            final String error =
              "Replication path id [" + id + "] does not reference a path "
              + "(class " + ref.getClass().getName() + " is not a Path instance).";
            throw new BuildException( error );
        }

        m_path = (Path) ref;
    }

   /**
    * The target directory to copy cached based path elements to.
    * @param todir the destination directory
    */
    public void setTodir( File todir )
    {
        m_todir = todir;
    }

   /**
    * Execute the task.
    */
    public void execute()
    {
        if( null == m_path )
        {
            ArrayList list = new ArrayList();
            String ref = getRef();
            if( null != ref )
            {
                Resource resource = getResource( ref );
                aggregate( list, resource, m_self );
            }
            Include[] includes = (Include[])  m_includes.toArray( new Include[0] );
            for( int i=0; i<includes.length; i++ )
            {
                Include include = includes[i];
                String includeRef = include.getRef();
                Resource resource = getResource( includeRef );
                aggregate( list, resource, true );
            }
            
            Project project = getProject();
            Resource[] resources = (Resource[]) list.toArray( new Resource[0] );
            Resource[] selection = getContext().getLibrary().sort( resources );
            m_path = getContext().createPath( selection );
        }

        if( null == m_todir )
        {
            File target =  getContext().getTargetDirectory();
            m_todir = new File( target, "replicate" );
        }
        
        //
        // replicate the resources that the project references
        //
        
        final File cache = (File) getProject().getReference( "dpml.cache" );
        FileSet cacheSet = createCacheFileSet( cache, m_path );
        copy( m_todir, cacheSet );
    }
    
    private void aggregate( List list, Resource resource, boolean self )
    {
        Resource[] resources = resource.getAggregatedProviders( Scope.RUNTIME, true, false );
        for( int i=0; i<resources.length; i++ )
        {
            Resource r = resources[i];
            if( !list.contains( r ) )
            {
                list.add( r );
            }
        }
        if( self )
        {
            if( !list.contains( resource ) )
            {
                list.add( resource );
            }
        }
    }
    
    private Resource[] getPathResources( Resource resource )
    {
        Resource[] resources = resource.getAggregatedProviders( Scope.RUNTIME, true, false );
        if( m_self )
        {
            Resource[] result = new Resource[ resources.length + 1 ];
            System.arraycopy( resources, 0, result, 0, resources.length );
            result[ resources.length ] = resource;
            return result;
        }
        else
        {
            return resources;
        }
    }

    private FileSet createCacheFileSet( final File cache, final Path path )
    {
        getProject().log( "using replication path: " + m_path, Project.MSG_VERBOSE );

        final FileSet fileset = new FileSet();
        fileset.setDir( cache );

        String root = cache.toString();

        int count = 0;
        log( "Constructing repository based fileset", Project.MSG_VERBOSE );
        String[] translation = path.list();
        for( int i=0; i < translation.length; i++ )
        {
            String trans = translation[i];
            if( trans.startsWith( root ) )
            {
                boolean exists = new File( trans ).exists();
                if( !exists )
                {
                    final String error = 
                      "Cached replication path entry ["
                      + trans 
                      + "] does not exist.";
                    throw new BuildException( error );
                }
                String relativeFilename = trans.substring( root.length() + 1 );
                if( m_verbose )
                {
                    log( "${dpml.cache}" + File.separator + relativeFilename );
                }
                else
                {
                    log( "${dpml.cache}" + File.separator + relativeFilename, Project.MSG_VERBOSE );
                }
                fileset.createInclude().setName( relativeFilename );
                fileset.createInclude().setName( relativeFilename + ".*" );
                fileset.createInclude().setName( relativeFilename + ".*.*" );
                count++;
            }
        }
        log( "cached entries: " + count );
        return fileset;
    }

    private String getRef()
    {
        if( null != m_ref )
        {
            return m_ref;
        }
        else if( null != m_key )
        {
            return getResource().getParent().getResourcePath() + "/" + m_key;
        }
        else
        {
            return getResource().getResourcePath();
        }
    }

    private Resource getResource( String ref )
    {
        try
        {
            return getContext().getLibrary().getResource( ref );
        }
        catch( ResourceNotFoundException e )
        {
            final String error = 
              "Feature reference ["
              + ref
              + "] in the project [" 
              + getResource()
              + "] is unknown.";
            throw new BuildException( error, e );
        }
    }

    private void copy( final File destination, final FileSet fileset )
    {
        mkDir( destination );
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTaskName( getTaskName() );
        copy.setPreserveLastModified( true );
        copy.setFlatten( m_flatten );
        copy.setTodir( destination );
        copy.addFileset( fileset );
        copy.init();
        copy.execute();
    }

   /*
    private FileSet[] createFileSets( final File cache, final Path path )
    {
        getProject().log( "using replication path: " + m_path, Project.MSG_VERBOSE );

        ArrayList list = new ArrayList();

        final FileSet cacheset = new FileSet();
        cacheset.setDir( cache );
        list.add( cacheset );

        final File deliverables = getContext().getDeliverablesDirectory();
        final FileSet localset = new FileSet();
        localset.setDir( deliverables );
        list.add( localset );

        String root = cache.toString();
        String local = deliverables.toString();

        int count = 0;
        log( "Constructing repository based fileset", Project.MSG_VERBOSE );
        String[] translation = path.list();
        for( int i=0; i < translation.length; i++ )
        {
            String trans = translation[i];
            if( trans.startsWith( root ) )
            {
                String relativeFilename = trans.substring( root.length() + 1 );
                if( m_verbose )
                {
                    log( "${dpml.cache}" + File.separator + relativeFilename );
                }
                else
                {
                    log( "${dpml.cache}" + File.separator + relativeFilename, Project.MSG_VERBOSE );
                }
                cacheset.createInclude().setName( relativeFilename );
                cacheset.createInclude().setName( relativeFilename + ".*" );
                cacheset.createInclude().setName( relativeFilename + ".*.*" );
                count++;
            }
            else if( trans.startsWith( local ) )
            {
                String relativeFilename = trans.substring( local.length() + 1 );
                if( m_verbose )
                {
                    log( "${project.deliverables}" + File.separator + relativeFilename );
                }
                else
                {
                    log( "${project.deliverables}" + File.separator + relativeFilename, Project.MSG_VERBOSE );
                }
                localset.createInclude().setName( relativeFilename );
                localset.createInclude().setName( relativeFilename + ".*" );
                localset.createInclude().setName( relativeFilename + ".*.*" );
                count++;
            }
            else
            {
                if( m_verbose )
                {
                    log( "including: " + trans );
                }
                else
                {
                    log( "including: " + trans, Project.MSG_VERBOSE );
                }

                FileSet fileset = new FileSet();
                File file = new File( trans );
                fileset.setFile( file );
                list.add( fileset );
                String filename = file.getName();
                fileset.createInclude().setName( filename + ".*" );
                fileset.createInclude().setName( filename + ".*.*" );
                count++;
            }
        }
        log( "entries: " + count );
        return (FileSet[]) list.toArray( new FileSet[0] );
    }
    */

    public class Include
    {
        private String m_includekey;
        private String m_includeRef;
        
        /**
        * Set the key of the target project or resource.
        *
        * @param key the resource key
        */
        public void setKey( final String key )
        {
            m_includekey = key;
        }

        /**
        * Set the ref of the target project or resource.
        *
        * @param ref the resource reference
        */
        public void setRef( final String ref )
        {
            m_includeRef = ref;
        }
        
        private String getRef()
        {
            if( null != m_includeRef )
            {
                return m_includeRef;
            }
            else if( null != m_includekey )
            {
                return getResource().getParent().getResourcePath() + "/" + m_includekey;
            }
            else
            {
                final String error = 
                  "Missing 'ref' or 'key' attribute.";
                throw new BuildException( error, getLocation() );
            }
        }
    }
}
