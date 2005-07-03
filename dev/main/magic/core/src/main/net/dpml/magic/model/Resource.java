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

package net.dpml.magic.model;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.UnknownResourceException;

import net.dpml.transit.NullArgumentException;
import net.dpml.transit.artifact.Handler;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Defintion of a resource.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Resource
{
    private final String m_key;
    private Info m_info;
    private ResourceRef[] m_resources;
    private AntFileIndex m_index;
    private String m_uri;

    public Resource(
      final AntFileIndex index, final String key, final Info info, final ResourceRef[] resources, String uri )
      throws IllegalArgumentException, NullArgumentException
    {
        assertNotNull( index, "index" );
        assertNotNull( key, "key" );
        assertNotNull( info, "info" );
        assertNotNull( resources, "resources" );
        assertNotNull( uri, "uri" );

        assertNotZeroLength( key, "key" );
        assertNotInvalidKeyLength( key, uri, "uri" );

        m_key = key;
        m_info = info;
        m_resources = resources;
        m_index = index;
        m_uri = uri;
    }

    private void assertNotNull( Object object, String key ) throws NullArgumentException
    {
        if( null == object )
        {
            throw new NullArgumentException( key );
        }
    }

    private void assertNotZeroLength( String value, String key ) throws IllegalArgumentException
    {
        if( value.length() == 0 )
        {
            final String error =
              "Supplied argument value for the parameter ["
              + key
              + "] contains an illegal zero length string.";
            throw new IllegalArgumentException( error );
        }
    }

    private void assertNotInvalidKeyLength( String id, String value, String key ) throws IllegalArgumentException
    {
        if( value.startsWith( "key:" ) )
        {
            final String ref = value.substring( 4 );
            try
            {
                assertNotZeroLength( ref , key );
            }
            catch( IllegalArgumentException iae )
            {
                final String error =
                  "Module key binding for the resource ["
                  + id
                  + "] under the parameter ["
                  + key
                  + "] is undefined.";
                throw new IllegalArgumentException ( error );
            }
        }
        else
        {
            assertNotZeroLength( value, key );
        }
    }

    public String getKey()
    {
        return m_key;
    }

    public Info getInfo()
    {
        return m_info;
    }

    public ResourceRef[] getResourceRefs()
    {
        if( "module".equals( getInfo().getType() ) )
        {
            ResourceRef[] implicit = getIndex().getSubsidiaryRefs( this );
            ArrayList list = new ArrayList();
            for( int i=0; i<implicit.length; i++ )
            {
                list.add( implicit[i] );
            }
            for( int i=0; i<m_resources.length; i++ )
            {
                list.add( m_resources[i] );
            }
            return (ResourceRef[]) list.toArray( new ResourceRef[0] );
        }
        else
        {
            return m_resources;
        }
    }

    protected AntFileIndex getIndex()
    {
        return m_index;
    }

   /**
    * Return the uri of the containing module.
    * @return the containg module uri
    */
    public String getModule()
    {
        if( m_uri.startsWith( "key:" ) )
        {
            final String key = m_uri.substring( 4 );
            try
            {
                return getIndex().getResource( key ).getInfo().getURI();
            }
            catch( BuildException e )
            {
                final String error =
                  "The resource ["
                  + getKey()
                  + "] contains an enclosing module reference ["
                  + key
                  + "] that is unresolvable.";
                throw new BuildException( error, e );
            }
        }
        else
        {
            return m_uri;
        }
    }

   /**
    * Return the set of resource references implied by this resource.
    * @param project the current project
    * @param mode a value of Policy.BUILD, Policy.TEST, Policy.RUNTIME or Policy.ANY
    * @param tag a value of ResourceRef.API, SPI, IMPL or ANY
    * @param flag if true then recursively resolve references
    * @return the set of references
    */
    public ResourceRef[] getResourceRefs(
      final Project project, final int mode, final int tag, final boolean flag )
    {
        final ArrayList list = new ArrayList();
        getResourceRefs( project, list, mode, tag, flag );
        return (ResourceRef[]) list.toArray( new ResourceRef[0] );
    }

    protected void getResourceRefs(
      final Project project, final List list, final int mode, final int tag, final boolean flag )
    {
        final ResourceRef[] refs = getResourceRefs();
        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            if( !list.contains( ref ) )
            {
                final Policy policy = ref.getPolicy();
                if( policy.matches( mode ) && ref.matches( tag ) )
                {
                    list.add( ref );
                    if( flag )
                    {
                        final Resource def = getResource( project, ref );
                        def.getResourceRefs( project, list, mode, ResourceRef.ANY, flag );
                    }
                }
            }
        }
    }

   /**
    * Returns a path of artifact filenames relative to the supplied scope.
    * The mode may be one of Policy.ANY, Policy.BUILD, Policy.TEST or Policy.RUNTIME.
    */
    public Path getPath( final Project project, final int mode )
    {
        return getPath( project, mode, "jar", false );
    }

   /**
    * Returns a path of artifact filenames relative to the supplied scope.
    * The mode may be one of ANY, BUILD, TEST or RUNTIME.
    * @param project the current project
    * @param mode one of Policy.ANY, Policy.BUILD, Policy.TEST or Policy.RUNTIME
    * @param filter a value of "*" for any or a type identifier such as "jar"
    * @exception NullArgumentException if the supplied project argument is null.
    */
    public Path getPath( final Project project, final int mode, String filter, boolean moduleFilter )
        throws NullArgumentException
    {
        if( null == project )
        {
            throw new NullArgumentException( "project" );
        }

        final Path path = new Path( project );
        final ArrayList visited = new ArrayList();

        final ResourceRef[] refs = getResourceRefs( project, mode, ResourceRef.ANY, true );
        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            if( ! visited.contains( ref ) )
            {
                final Resource resource = getResource( project, ref );
                if( filterModule( resource, moduleFilter ) )
                {
                    Info info = resource.getInfo();
                    if( "*".equals( filter ) )
                    {
                        String[] types = info.getTypes();
                        for( int j=0; j<types.length; j++ )
                        {
                            String type = types[j];
                            if( !"theme".equals( type ) )
                            {
                                final File file = resource.getArtifact( project, type );
                                path.createPathElement().setLocation( file );

                                //final URI uri = resource.getArtifactURI( type );
                                //path.createPathElement().setPath( uri.toString() );
                            }
                        }
                    }
                    else if( !"theme".equals( filter ) )
                    {
                        if( info.isa( filter ) )
                        {
                            final File file = resource.getArtifact( project, filter );
                            path.createPathElement().setLocation( file );
                        }
                    }
                }
                visited.add( ref );
            }
        }
        return path;
    }

   /**
    * Used to filter the resources assigned to a path based on a module
    * restriction policy.  If the supplied module argument is false then
    * a resource is by default a path candidate.  If the module argument
    * is true then the function evaluates if the module of this resource
    * and the module of the supplied resource are equal.
    *
    * @param resource the resource to evaluate for path inclusion
    * @param module a boolean value declaring if module filtering shall be
    *    performed
    */
    private boolean filterModule( Resource resource, boolean module )
    {
        if( !module )
        {
            return true;
        }
        else
        {
            try
            {
                return getModule().equals( resource.getModule() );
            }
            catch( BuildException be )
            {
                return false;
            }
        }
    }

    private Resource getResource( Project project, ResourceRef ref )
    {
        try
        {
            return getIndex().getResource( ref );
        }
        catch( UnknownResourceException ure )
        {
            final String error =
              "Resource defintion " + this + " contains a unknown resource reference ["
                 + ure.getKey() + "] (referenced by project '" + project.getName() + "'.";
            throw new BuildException( error );
        }
    }

   /**
    * Return the set of resource refs for a given category not including resources
    * already visited in the supplied list.
    *
    * @param project the current project
    * @param visited a list of Resource instances already handled
    * @param category a value of ResourceRef.API, SPI, IMPL or ANY
    * @return the set of resource refs
    */
    public ResourceRef[] getQualifiedRefs( Project project, final List visited, final int category )
    {
        final ArrayList list = new ArrayList();
        final ResourceRef[] refs =
          getResourceRefs( project, Policy.RUNTIME, category, true );
        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            final Resource resource = getIndex().getResource( ref );
            if( !visited.contains( resource ) && !list.contains( ref ) )
            {
                list.add( ref );
                visited.add( resource );
            }
        }
        return (ResourceRef[]) list.toArray( new ResourceRef[0] );
    }

    public File getArtifact( final Project project )
    {
        return get( getInfo().getType() );
    }

    public File getArtifact( final Project project, String type )
    {
        return get( type );
    }

    private File get( String type ) throws BuildException
    {
        final String path = getInfo().getURI( type );
        try
        {
            URL url = new URL( null, path, new Handler() );
            Object object = url.openConnection().getContent( new Class[]{ File.class } );
            if( null == object )
            {
                final String error =
                  "Artifact handler returned a null file for the artifact: ["
                  + path
                  + "].";
                throw new BuildException( error );
            }
            else
            {
                return (File) object;
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Unable to resolve local file for the resource: "
              + this;
            throw new BuildException( error, e );
        }
    }

    public URI getArtifactURI( String type ) throws BuildException
    {
        final String path = getInfo().getURI( type );
        try
        {
            return new URI( path );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Unable to resolve artifact uri for the resource: "
              + this;
            throw new BuildException( error, e );
        }
    }


    public String getFilename()
    {
        return getFilename( getInfo().getType() );
    }

    public String getFilename( final String type )
    {
        final Info info = getInfo();
        final String name = info.getName();
        if( null != info.getVersion() )
        {
            return name + "-" + info.getVersion() + "." + type;
        }
        else
        {
            return name + "." + type;
        }
    }

    public String getFilename( final String prefix, final String type )
    {
        if( null == prefix )
        {
            throw new NullArgumentException( "prefix" );
        }
        final String name = getInfo().getName() + "-" + prefix;
        if( null != getInfo().getVersion() )
        {
            return name + "-" + getInfo().getVersion() + "." + type;
        }
        else
        {
            return name + "." + type;
        }
    }

    public String toString()
    {
        return "[" + getInfo().toString() + "]";
    }

    public boolean equals( final Object other )
    {
        if( ! ( other instanceof Resource ) )
            return false;

        final Resource def = (Resource) other;

        if( null == getKey() )
        {
            if( def.getKey() != null )
            {
                return false;
            }
        }
        else
        {
            if( !getKey().equals( def.getKey() ) )
            {
                return false;
            }
        }

        if( !getInfo().equals( def.getInfo() ) )
        {
            return false;
        }

        final ResourceRef[] refs = getResourceRefs();
        final ResourceRef[] references = def.getResourceRefs();

        if( refs.length != references.length )
        {
            return false;
        }

        for( int i=0; i<refs.length; i++ )
        {
            if( !refs[i].equals( references[i] ) )
                return false;
        }

        return true;
    }

    public int hashCode()
    {
        int hash = getInfo().hashCode();
        final ResourceRef[] refs = getResourceRefs();
        for( int i = 0 ; i < refs.length ; i++ )
            hash = hash ^ refs[i].hashCode();
        return hash;
    }
}
