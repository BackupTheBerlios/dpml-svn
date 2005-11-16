/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.beans.IntrospectionException;

import net.dpml.tools.tasks.GenericTask;
import net.dpml.tools.info.Scope;

import net.dpml.metro.info.Type;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicElementNS;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
 * Task that handles the construction of catalog of type entries.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class TypesTask extends GenericTask implements DynamicElementNS
{
    private List m_builders = new LinkedList();

    public TypeBuilder createType()
    {
        Project proj = getProject();
        TypeBuilderTask builder = new TypeBuilderTask();
        builder.setProject( proj );
        m_builders.add( builder );
        return builder;
    }

   /**
    * Operation used to construct a custom part type directive.
    * @param uri the part handler uri
    * @param name the element name
    * @param qualified the qualified name
    */
    public Object createDynamicElement( String uri, String name, String qualified )
    {
        String path = getProject().replaceProperties( uri );
        TypeBuilder builder = loadTypeBuilder( path, name );
        if( null != builder )
        {
            m_builders.add( builder );
        }
        return builder;
    }

    public void execute()
    {
        Project proj = getProject();
        Path path = getContext().getPath( Scope.RUNTIME );
        File classes = getContext().getTargetClassesMainDirectory();
        path.createPathElement().setLocation( classes );
        ClassLoader classloader = new AntClassLoader( proj, path );
        buildTypes( classloader );
    }

    private void buildTypes( ClassLoader classloader )
    {
        final TypeBuilder[] builders = (TypeBuilder[]) m_builders.toArray( new TypeBuilder[0] );
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        for( int i=0; i<builders.length; i++ )
        {
            final TypeBuilder builder = builders[i];
            try
            {
                final Type type = builder.buildType( classloader );
                final String classname = type.getInfo().getClassname();
                final String resource = getEmbeddedResourcePath( classname );
                final File file = getEmbeddedOutputFile( resource );
                file.getParentFile().mkdirs();
                final FileOutputStream output = new FileOutputStream( file );
                final BufferedOutputStream buffer = new BufferedOutputStream( output );
                try
                {
                    Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
                    Type.encode( type, output );
                }
                finally
                {
                    Thread.currentThread().setContextClassLoader( current );
                    output.close();
                }
            }
            catch( IntrospectionException e )
            {
                final String error = e.getMessage();
                throw new BuildException( error, e, getLocation() );
            }
            catch( BuildException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while attempting to build types."
                  + "\nCause: " + e.getClass().getName()
                  + "\nMessage: " + e.getMessage();
                throw new BuildException( error, e, getLocation() );
            }
        }
    }

    private String getEmbeddedResourcePath( String classname )
    {
        String path = classname.replace( '.', '/' );
        String filename = path + ".type";
        return filename;
    }

    private File getEmbeddedOutputFile( String filename )
    {
        File classes = getContext().getTargetClassesMainDirectory();
        File destination = new File( classes, filename );
        return destination;
    }

    private TypeBuilder loadTypeBuilder( String uri, String name ) throws BuildException
    {
        String urn = uri + ":" + name;
        Object builder = null;
        TypeBuilder typeBuilder = null;
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            Project proj = getProject();
            builder = proj.createDataType( urn );
            typeBuilder  = (TypeBuilder) builder;
            return typeBuilder ;
        }
        catch( ClassCastException e )
        {
            final String error = 
              "The custom type builder ["
              + builder.getClass().getName()
              + "] established by the uri [" 
              + urn
              + "] declared by the element <"
              + name
              + "' does not implement the net.dpml.metro.builder.TypeBuilder interface.";
            throw new BuildException( error, e, getLocation() );
        }
        catch( BuildException e )
        {
            final String error = 
              "Unable to load the plugin from the uri [" 
              + urn 
              + "] to handle the custom type declared by the element <"
              + name
              + ">.";
            throw new BuildException( error, e, getLocation() );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected exception while attempting to load the custom type handler with the uri [" 
              + urn 
              + "] declared by the element <"
              + name
              + ">.";
            throw new BuildException( error, e, getLocation() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( context );
        }
    }
}
