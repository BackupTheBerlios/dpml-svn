/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.data;

import java.io.Serializable;
import java.util.Arrays;

import net.dpml.metro.info.Priority;

/**
 * Description of the configuration of a set of categories.
 *
 * @see CategoryDirective
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoriesDirective extends CategoryDirective implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * The root category hierachy.
     */
    private final CategoryDirective[] m_categories;

    /**
     * Create a CategoriesDirective instance.
     */
    public CategoriesDirective()
    {
        this( "" );
    }

    /**
     * Create a CategoriesDirective instance.
     *
     * @param name the base category name
     */
    public CategoriesDirective( final String name )
    {
        this( name, null, null, new CategoryDirective[0] );
    }

    /**
     * Create a CategoriesDirective instance.
     *
     * @param categories the categories to include in the directive
     */
    public CategoriesDirective( CategoryDirective[] categories )
    {
        this( "", null, null, categories );
    }


    /**
     * Create a CategoriesDirective instance.
     *
     * @param name the base category name
     * @param priority the default logging priority
     * @param target the default logging target
     * @param categories the logging category descriptors
     * @exception NullPointerException if a category array value is null
     */
    public CategoriesDirective( 
      final String name, final Priority priority, final String target, final CategoryDirective[] categories )
    {
        super( name, priority, target );
        if( categories == null )
        {
            m_categories = new CategoryDirective[0];
        }
        else
        {
            for( int i=0; i<categories.length; i++ )
            {
                CategoryDirective category = categories[i];
                if( null == category )
                {
                    throw new NullPointerException( "category" );
                }
            }
            m_categories = categories;
        }
    }

    /**
     * Return the set of logging categories.
     *
     * @return the set of category declarations
     */
    public CategoryDirective[] getCategories()
    {
        return m_categories;
    }

    /**
     * Return a named category.
     *
     * @param name the category name
     * @return the category declaration
     */
    public CategoryDirective getCategoryDirective( String name )
    {
        for( int i = 0; i < m_categories.length; i++ )
        {
            final CategoryDirective category = m_categories[ i ];
            if( category.getName().equalsIgnoreCase( name ) )
            {
                return category;
            }
        }
        return null;
    }

   /**
    * Test this object for equality with the suppplied object.
    * @param other the other object
    * @return TRUE if this object equals the supplied object
    *   else FALSE
    */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        else if( !( other instanceof CategoriesDirective ) )
        {
            return false;
        }
        else
        {
            CategoriesDirective directive = (CategoriesDirective) other;
            return Arrays.equals( m_categories, directive.m_categories );
        }
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashArray( m_categories );
        return hash;
    }
}
