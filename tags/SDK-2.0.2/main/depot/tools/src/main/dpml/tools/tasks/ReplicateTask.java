/*
 * Copyright 2005 Stephen J. McConnell
 *
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

package dpml.tools.tasks;

import dpml.build.BuildError;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dpml.library.Resource;
import dpml.library.ResourceNotFoundException;
import dpml.library.Type;
import dpml.library.Scope;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.Layout;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;

/**
 * Consolidates a set of resources based on project dependencies.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ReplicateTask extends GenericTask
{
    private String m_key;
    private String m_ref;
    private File m_todir;
    private String m_layout;

    private boolean m_verbose = false;
    private boolean m_self = false;
    private boolean m_failOnError = true;
    
    private ArrayList<Include> m_includes = new ArrayList<Include>();

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
    * Set the id of the target layout strategy.
    *
    * @param id the layout identifier
    */
    public void setLayout( final String id )
    {
        m_layout = id;
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
    * Set the fail on error policy.
    * @param flag the fail on error policy
    */
    public void setFailonerror( boolean flag )
    {
        m_failOnError = flag;
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
    
   /**
    * Create and add a new include.
    * @return the include
    */
    public Include createInclude()
    {
        Include include = new Include();
        m_includes.add( include );
        return include;
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
        if( null == m_todir )
        {
            File target =  getContext().getTargetDirectory();
            m_todir = new File( target, "replicate" );
        }
        
        File destination = m_todir;
        Layout layout = resolveLayout();
        ArrayList<Resource> list = new ArrayList<Resource>();
        String ref = getRef();
        if( null != ref )
        {
            Resource resource = getResource( ref );
            aggregate( list, resource, m_self );
        }
        
        //
        // add nested includes
        //
        
        Include[] includes = m_includes.toArray( new Include[0] );
        for( int i=0; i<includes.length; i++ )
        {
            Include include = includes[i];
            String includeRef = include.getRef();
            Resource resource = getResource( includeRef );
            aggregate( list, resource, true );
        }
        
        //
        // get sorted list of resources
        //
        
        Resource[] resources = list.toArray( new Resource[0] );
        Resource[] selection = getContext().getLibrary().sort( resources );
        File cache = (File) getProject().getReference( "dpml.cache" );
        for( int i=0; i<selection.length; i++ )
        {
            Resource resource = selection[i];
            copy( cache, destination, resource, layout );
        }
    }
    
   /**
    * Copy the artifacts produced by the Resource from the cache to 
    * the destination using a suppplied target layout.
    */
    private void copy( File cache, File destination, Resource resource, Layout layout )
    {
        Type[] types = resource.getTypes();
        for( int j=0; j<types.length; j++ )
        {
            Type type = types[j];
            if( type.getTest() || !type.getExport() )
            {
                return;
            }
            try
            {
                Artifact artifact = type.getResolvedArtifact();
                copyArtifact( artifact, cache, destination, layout );
                if( type.getAliasProduction() )
                {
                    Artifact link = type.getLinkArtifact();
                    copyArtifact( link, cache, destination, layout );
                }
            }
            catch( IllegalStateException e )
            {
                if( m_failOnError )
                {
                    throw e;
                }
                else
                {
                    log( e.getMessage() );
                }
            }
            catch( IOException e )
            {
                if( m_failOnError )
                {
                    final String error = 
                      "Replication failure for the ["
                      + type
                      + "] type in the resource ["
                      + resource
                      + "].";
                    throw new BuildError( error, e );
                }
                else
                {
                    log( e.getMessage() );
                }
            }
        }
    }
    
    private void copyArtifact( Artifact artifact, File cache, File destination, Layout layout )
    {
        String sourcePath = Transit.getInstance().getCacheLayout().resolvePath( artifact );
        File source = new File( cache, sourcePath );
        if( !source.exists() )
        {
            final String error = 
              "Cached resource [" 
              + source 
              + "] does not exist.";
            log( error );
        }
        else
        {
            String destPath = layout.resolvePath( artifact );
            File dest = new File( destination, destPath );
            copyFile( source, dest );
            File md5 = new File( cache, sourcePath + ".md5" );
            if( md5.exists() )
            {
                copyFile( md5, new File( destination, destPath + ".md5" ) );
            }
            File asc = new File( cache, sourcePath + ".asc" );
            if( asc.exists() )
            {
                copyFile( asc, new File( destination, destPath + ".asc" ) );
            }
        }
    }
    
    private void copyFile( File source, File dest )
    {
        dest.getParentFile().mkdir();
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setFile( source );
        copy.setTofile( dest );
        copy.setPreserveLastModified( true );
        copy.setVerbose( m_verbose );
        copy.init();
        copy.execute();
    }
    
    private Layout resolveLayout() 
    {
        if( null == m_layout )
        {
            return Transit.getInstance().getCacheLayout();
        }
        else
        {
            Layout layout = Layout.getLayout( m_layout );
            if( null == layout )
            {
                final String error = 
                  "Target layout id [" 
                  + m_layout
                  + "] is unknown.";
                throw new BuildException( error, getLocation() );
            }
            else
            {
                return layout;
            }
        }
    }
    
    private void aggregate( List<Resource> list, Resource resource, boolean self )
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
    
   /**
    * Declaration of an include.
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
