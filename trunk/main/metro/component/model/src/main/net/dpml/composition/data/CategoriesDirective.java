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

package net.dpml.composition.data;

import java.io.Serializable;


/**
 * Description of the configuration of a set of categories.
 *
 * @see CategoryDirective
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: CategoriesDirective.java 259 2004-10-30 07:24:40Z mcconnell $
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
    private CategoryDirective[] m_categories;

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
     */
    public CategoriesDirective( 
      final String name, final String priority, final String target, final CategoryDirective[] categories )
    {
        super( name, priority, target );
        if( categories == null )
        {
            m_categories = new CategoryDirective[ 0 ];
        }
        else
        {
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
    *
    * @return TRUE if this object equals the supplied object
    *   else FALSE
    */
    public boolean equals( Object other )
    {
        boolean isEqual = other instanceof CategoriesDirective;
        if ( isEqual ) isEqual = super.equals( other );

        if ( isEqual )
        {
            CategoriesDirective cat = (CategoriesDirective) other;
            if ( isEqual ) isEqual = m_categories.length == cat.m_categories.length;
            if ( isEqual )
            {
                for ( int i = 0; i < m_categories.length && isEqual; i++ )
                {
                    isEqual = m_categories[i].equals( cat.m_categories[i] );
                }
            }
        }
        return isEqual;
    }

   /**
    * Return the hashcode for the object.
    * @return the cashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        for ( int i = 0; i < m_categories.length; i++ )
        {
            hash = hash + 687364287;
            hash ^= m_categories[i].hashCode();
        }
        return hash;
    }
}
