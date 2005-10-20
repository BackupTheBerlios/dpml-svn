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

package net.dpml.composition.tools.datatypes;

import java.util.List;
import java.util.LinkedList;

import net.dpml.component.data.CategoryDirective;
import net.dpml.component.data.CategoriesDirective;

/**
 * Build datatype used to construct a categories descriptor.
 */
public class CategoriesDataType
{
    private List m_list = new LinkedList();

   /**
    * CategoryDataType creation function incvolved by the ant builder
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

        public void setName( String name )
        {
            m_name = name;
        }

        public void setPriority( String priority )
        {
            m_priority = priority;
        }

        public void setTarget( String target )
        {
            m_target = target;
        }

        public CategoryDirective getCategoryDirective()
        {
            return new CategoryDirective( m_name, m_priority, m_target );
        }
    } 
}

