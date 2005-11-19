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

package net.dpml.metro.tools.datatypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.beans.IntrospectionException;

import net.dpml.metro.info.PartReference;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.info.Type;

import net.dpml.metro.tools.ComponentBuilderTask;
import net.dpml.metro.tools.PartReferenceBuilder;

import org.apache.tools.ant.Task;

/**
 * A context directive class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextDataType
{
    private String m_class;
    private List m_builders = new ArrayList();

   /**
    * Declare a custom context implementation classname.
    * @param classname the classname of an optional context implementation class
    */
    public void setClass( final String classname )
    {
        m_class = classname;
    }

   /**
    * Return the optional context implementation classname.
    * @return the classname
    */
    public String getClassname()
    {
        return m_class;
    }

   /**
    * Create a new constructed part builder.
    * @return a part builder
    */
    public EntryDataType createEntry()
    {
        final EntryDataType builder = new EntryDataType();
        m_builders.add( builder );
        return builder;
    }

   /**
    * Create a name component builder.
    * @return a new component builder
    */
    public ComponentBuilderTask createComponent()
    {
        ComponentBuilderTask builder = new ComponentBuilderTask();
        m_builders.add( builder );
        return builder;
    }

   /**
    * Return all of the part reference builders.
    * @return the set of part reference builders
    */
    public PartReferenceBuilder[] getBuilders()
    {
        return (PartReferenceBuilder[]) m_builders.toArray( new PartReferenceBuilder[0] );
    }

   /**
    * Create a new context directive.
    * @param classloader the classloader to use
    * @param type the underlying component type
    * @return the context directive
    * @exception IntrospectionException if a class introspection error occurs
    * @exception IOException if an I/O error occurs
    * @exception ClassNotFoundException if a context class was not found
    */
    public ContextDirective getContextDirective( ClassLoader classloader, Type type ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String classname = getClassname();
        PartReferenceBuilder[] builders = getBuilders();
        PartReference[] references = new PartReference[ builders.length ];
        for( int i=0; i<builders.length; i++ )
        {
            PartReferenceBuilder builder = builders[i];
            if( builder instanceof ComponentBuilderTask )
            {
                Task task = (Task) builder;
                task.setTaskName( "component" );
            }
            PartReference reference = builder.buildPartReference( classloader, type );
            references[i] = reference;
        }
        return new ContextDirective( classname, references );
    }
}

