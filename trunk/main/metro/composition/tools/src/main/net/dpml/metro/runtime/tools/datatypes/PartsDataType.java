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

package net.dpml.metro.runtime.tools.datatypes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.beans.IntrospectionException;

import net.dpml.metro.info.Type;
import net.dpml.metro.info.PartReference;

import net.dpml.metro.runtime.tools.ComponentBuilderTask;
import net.dpml.metro.runtime.tools.PartReferenceBuilder;
import net.dpml.metro.runtime.tools.ClassLoaderBuilderTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicElementNS;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;

/**
 * A datatype that enables custom part builders.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PartsDataType extends ClassLoaderBuilderTask implements DynamicElementNS
{
    private List m_builders = new LinkedList();
    private Task m_owner;

    public PartsDataType( Task owner )
    {
        Project proj = owner.getProject();
        setProject( proj );
        m_owner = owner;
    }

    public ComponentBuilderTask createComponent()
    {
        ComponentBuilderTask builder = new ComponentBuilderTask();
        m_builders.add( builder );
        return builder;
    }

    public PartDataType createPart()
    {
        PartDataType builder = new PartDataType();
        m_builders.add( builder );
        return builder;
    }

   /**
    * Create a new constructed value builder.
    * @return a part builder
    */
    public EntryDataType createValue()
    {
        final EntryDataType builder = new EntryDataType();
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
        PartReferenceBuilder builder = loadPartHandler( path, name );
        if( null != builder )
        {
            m_builders.add( builder );
        }
        return builder;
    }

    private PartReferenceBuilder loadPartHandler( String uri, String name ) throws BuildException
    {
        String urn = uri + ":" + name;
        Object builder = null;
        PartReferenceBuilder partBuilder = null;
        String target = m_owner.getOwningTarget().getName();
        Location location = m_owner.getLocation();
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            Project proj = getProject();
            builder = proj.createDataType( urn );
            partBuilder  = (PartReferenceBuilder) builder;
            return partBuilder ;
        }
        catch( ClassCastException e )
        {
            final String error = 
              "The custom part builder ["
              + builder.getClass().getName()
              + "] established by the uri [" 
              + urn
              + "] declared by the element <"
              + name
              + "> under the task <"
              + m_owner.getTaskName() 
              + "><parts> within the target <"
              + target  
              + "> does not implement the net.dpml.metro.tools.PartReferenceBuilder interface.";
            throw new BuildException( error, e );
        }
        catch( BuildException e )
        {
            final String error = 
              "Unable to load the plugin from the uri [" 
              + urn 
              + "] to handle the custom part type declared by the element <"
              + name
              + "> within <"
              + m_owner.getTaskName() 
              + "><parts> under the target <"
              + target  
              + ">.";
            throw new BuildException( error, e );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected exception while attempting to load the custom part handler with the uri [" 
              + urn 
              + "] declared by the element <"
              + name
              + "> within <"
              + m_owner.getTaskName() 
              + "><parts> under the target <"
              + target  
              + ">.";
            throw new BuildException( error, e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( context );
        }
    }

   /**
    * Return the set of parts contained within this container.
    * @return the contained parts
    */
    public PartReferenceBuilder[] getPartBuilders()
    {
        return (PartReferenceBuilder[]) m_builders.toArray( new PartReferenceBuilder[0] );
    }

   /**
    * Return the set of parts contained within this parts collection.
    * @param classloader the runtime classloader
    * @param type the component type that references are relative to
    * @return the contained parts
    */
    public PartReference[] getParts( ClassLoader classloader, Type type ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        PartReferenceBuilder[] builders = 
          (PartReferenceBuilder[]) m_builders.toArray( new PartReferenceBuilder[0] );
        PartReference[] parts = new PartReference[ builders.length ];
        for( int i=0; i<builders.length; i++ )
        {
            PartReferenceBuilder builder = builders[i];
            parts[i] = builder.buildPartReference( classloader, type );
        }
        return parts;
    }
}
