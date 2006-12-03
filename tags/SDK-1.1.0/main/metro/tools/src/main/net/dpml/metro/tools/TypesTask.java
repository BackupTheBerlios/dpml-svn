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
import java.io.OutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.beans.IntrospectionException;

import net.dpml.library.info.Scope;

import net.dpml.metro.info.Type;
import net.dpml.metro.builder.ComponentTypeEncoder;

import net.dpml.tools.tasks.GenericTask;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicElementNS;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Task that handles the construction of catalog of type entries.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TypesTask extends GenericTask implements DynamicElementNS
{
    private static final ComponentTypeEncoder COMPONENT_TYPE_ENCODER = 
      new ComponentTypeEncoder();


    private List m_builders = new LinkedList();

   /**
    * Create and return a new type builder.
    * @return the type builder
    */
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
    * @return a dynamic type builder
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

   /**
    * Task executaion.
    */
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
                OutputStream output = getOutputStream( type );
                try
                {
                    COMPONENT_TYPE_ENCODER.export( type, output );
                }
                finally
                {
                    try
                    {
                        output.close();
                    }
                    catch( IOException ioe )
                    {
                        ioe.printStackTrace();
                    }
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
    
    private OutputStream getOutputStream( Type type ) throws IOException
    {
        final String classname = type.getInfo().getClassname();
        final String resource = getEmbeddedResourcePath( classname );
        final File file = getEmbeddedOutputFile( resource );
        file.getParentFile().mkdirs();
        return new FileOutputStream( file );
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
            return typeBuilder;
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
