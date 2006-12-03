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

import java.util.ArrayList;
import java.util.List;

import net.dpml.metro.info.CategoryDescriptor;

import net.dpml.metro.info.Priority;

import org.apache.tools.ant.BuildException;

/**
 * Datatype supporting the declaration of a logging channel at the level of a component type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoriesDescriptorDataType
{
    private List m_categories = new ArrayList();
    
  /**
    * Create a new services datatype.
    * @return a new services datatype
    */
    public CategoryDescriptorDataType createCategory()
    {
        CategoryDescriptorDataType data = new CategoryDescriptorDataType();
        m_categories.add( data );
        return data;
    }
    
    CategoryDescriptor[] getCategoryDescriptors()
    {
        CategoryDescriptorDataType[] entries = 
         (CategoryDescriptorDataType[]) m_categories.toArray( new CategoryDescriptorDataType[0] );
        CategoryDescriptor[] categories = new CategoryDescriptor[ entries.length ];
        for( int i=0; i<entries.length; i++ )
        {
            CategoryDescriptorDataType data = entries[i];
            categories[i] = data.getCategoryDescriptor();
        }
        return categories;
    }

   /**
    * Description of a single logging category.
    */
    public class CategoryDescriptorDataType
    {
        private String m_name;
        private Priority m_priority;
    
       /**
        * Set the service classname.
        * @param name the name of the service interface class
        */
        public void setName( final String name )
        {
            if( null == name )
            {
                throw new NullPointerException( "name" );
            }
            m_name = name;
        }
        
       /**
        * Set the service version.
        * @param spec the version value
        */
        public void setPriority( final String spec )
        {
            if( null == spec )
            {
                throw new NullPointerException( "spec" );
            }
            m_priority = Priority.parse( spec );
        }
        
        CategoryDescriptor getCategoryDescriptor()
        {
            if( null == m_name )
            {
                throw new BuildException( "Missing category 'name' attribute." );
            }
            else
            {
                return new CategoryDescriptor( m_name, m_priority, null );
            }
        }
    }
}
