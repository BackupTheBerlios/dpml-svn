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

package org.apache.avalon.composition.data;

import java.io.Serializable;
import java.lang.Comparable;

import org.apache.avalon.logging.data.CategoriesDirective;

/**
 * Abstract base class for ComponentProfile and ContainmentProfile.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DeploymentProfile.java 46177 2004-09-16 13:33:18Z niclas $
 */
public abstract class DeploymentProfile implements Serializable, Comparable
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

   /**
    * System default activation policy.
    */
    public static final int DEFAULT = -1;

   /**
    * Activation on startup enabled.
    */
    public static final int ENABLED = 1;

   /**
    * Activation on startup disabled.
    */
    public static final int DISABLED = 0;

    private static final CategoriesDirective EMPTY_CATEGORIES = 
      new CategoriesDirective();

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    /**
     * The name of the component profile. This is an
     * abstract name used during assembly.
     */
    private String m_name;

    /**
     * The activation policy.
     */
    private final int m_activation;

   /**
    * The mode under which this profile was established.
    */
    private final Mode m_mode;

    private final CategoriesDirective m_categories;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

    public DeploymentProfile( 
      final String name, int activation, Mode mode, CategoriesDirective categories ) 
    {
        m_activation = activation;
        m_categories = categories;

        if( mode == null )
        {
            m_mode = Mode.IMPLICIT;
        }
        else
        {
            m_mode = mode;
        }
        
        if( name == null )
        {
            m_name = "untitled";
        }
        else
        {
            m_name = name;
        }
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    /**
     * Return the name of meta-data instance.
     *
     * @return the name of the component.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the logging categories for the profile.
     *
     * @return the categories
     */
    public CategoriesDirective getCategories()
    {
        if( m_categories == null ) return EMPTY_CATEGORIES;
        return m_categories;
    }

   /**
    * Get the activation directive for the profile.
    *
    * @return the declared activation policy
    * @see #DEFAULT
    * @see #ENABLED
    * @see #DISABLED 
    */
    public int getActivationDirective()
    {
        return m_activation;
    }

    /**
     * Returns the creation mode for this profile.
     * @return a value of EXPLICIT, PACKAGED or IMPLICIT
     */
    public Mode getMode()
    {
        return m_mode;
    }

    /**
     * Returns a string representation of the profile.
     * @return a string representation
     */
    public String toString()
    {
        return "[" + getName() + "]";
    }

    public int compareTo( Object object )
    {
        String name = this.toString();
        String other = object.toString();
        return name.compareTo( other );
    }
}
