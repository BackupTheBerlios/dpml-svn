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

import java.util.List;
import java.util.LinkedList;

import net.dpml.metro.info.Priority;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.data.CategoriesDirective;

import org.apache.tools.ant.BuildException;

/**
 * Build datatype used to construct a categories descriptor.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoriesDataType
{
    private List m_list = new LinkedList();

   /**
    * CategoryDataType creation function invoked by the ant builder
    * for all nested 'category' elements.
    * 
    * @return a datatype used to construct a category descriptor
    */
    public CategoryDataType createCategory()
    {
        CategoryDataType category = new CategoryDataType();
        m_list.add( category );
        return category;
    }

   /**
    * Utility method used to construct a CategoriesDirective based on 
    * build time features assigned by ant.
    * @return the CategoriesDirective containing zero or more CategoryDirective instances
    */
    public CategoriesDirective getCategoriesDirective()
    {
         CategoryDataType[] types = (CategoryDataType[]) m_list.toArray( new CategoryDataType[0] );
         CategoryDirective[] directives = new CategoryDirective[ types.length ];
         for( int i=0; i<types.length; i++ )
         {
             CategoryDataType type = types[i];
             CategoryDirective directive = type.getCategoryDirective();
         }
         return new CategoriesDirective( directives );
    }

   /**
    * Build datatype used to construct a categories descriptor.
    */
    public class CategoryDataType
    {
        private String m_name;
        private String m_priority;
        private String m_target;

       /**
        * Set the category name.
        * @param name the category name
        */
        public void setName( String name )
        {
            m_name = name;
        }

       /**
        * Set the category priority.
        * @param priority the category priority
        */
        public void setPriority( String priority )
        {
            m_priority = priority;
        }

       /**
        * Set the category target.
        * @param target the category target
        */
        public void setTarget( String target )
        {
            m_target = target;
        }

       /**
        * Return the category directive.
        * @return the directive
        */
        public CategoryDirective getCategoryDirective()
        {
            if( null == m_name )
            {
                throw new BuildException( "Missing category name." );
            }
            Priority priority = getPriority();
            return new CategoryDirective( m_name, priority, m_target );
        }
        
        private Priority getPriority()
        {
            if( null == m_priority )
            {
                return null;
            }
            else
            {
                return Priority.parse( m_priority );
            }
        }
    } 
}

