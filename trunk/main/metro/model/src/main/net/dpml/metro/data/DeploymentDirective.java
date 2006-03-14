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

import net.dpml.part.ActivationPolicy;
import net.dpml.metro.info.Composite;
import net.dpml.metro.info.PartReference;

/**
 * Abstract base class for the ComponentDirective.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class DeploymentDirective extends Composite implements Comparable
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private static final CategoriesDirective EMPTY_CATEGORIES = 
      new CategoriesDirective();

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

   /**
    * The name of the component profile. This is an
    * abstract name used during assembly.
    */
    private final String m_name;

   /**
    * The activation policy.
    */
    private final ActivationPolicy m_activation;

   /**
    * Logging category directives.
    */
    private final CategoriesDirective m_categories;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new deployment profile instance.
    * @param name the profile name
    * @param activation the activation policy
    * @param categories logging category directives
    * @param parts the internal component parts
    */
    public DeploymentDirective( 
      final String name, ActivationPolicy activation, CategoriesDirective categories, PartReference[] parts ) 
    {
        super( parts );
        
        m_activation = activation;

        if( null == categories )
        {
            m_categories = EMPTY_CATEGORIES;
        }
        else
        {
            m_categories = categories;
        }
        
        if( null != name )
        {
            if( name.indexOf( " " ) > 0 || name.indexOf( "." ) > 0 || name.indexOf( "," ) > 0
              || name.indexOf( "/" ) > 0 )
            {
                final String error = 
                  "Directive name ["
                  + name
                  + "] contains an illegal character (' ', ',', or '.')";
                throw new IllegalArgumentException( error );
            }
            else if( name.length() == 0 )
            {
                final String error = 
                  "Directive name [] is not sufficiently descriptor.";
                throw new IllegalArgumentException( error );
            }
            else
            {
                m_name = name;
            }
        }
        else
        {
            m_name = null;
        }
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    /**
     * Return the profile name.
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
    public CategoriesDirective getCategoriesDirective()
    {
        return m_categories;
    }

   /**
    * Get the activation policy for the profile.
    *
    * @return the declared activation policy
    * @see ActivationPolicy#SYSTEM
    * @see ActivationPolicy#STARTUP
    * @see ActivationPolicy#DEMAND
    */
    public ActivationPolicy getActivationPolicy()
    {
        return m_activation;
    }

    /**
     * Returns a string representation of the profile.
     * @return a string representation
     */
    public String toString()
    {
        return "[" + getName() + "]";
    }

   /**
    * Compare this object with the supplied object.
    * @param object the obvject to compare with
    * @return the result
    */
    public int compareTo( Object object )
    {
        String name = this.toString();
        String other = object.toString();
        return name.compareTo( other );
    }

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        else
        {
            if( other instanceof DeploymentDirective )
            {
                DeploymentDirective profile = (DeploymentDirective) other;
                if( !m_name.equals( profile.getName() ) )
                {
                    return false;
                }
                else if( !m_activation.equals( profile.getActivationPolicy() ) )
                {
                    return false;
                }
                else
                {
                    return m_categories.equals( profile.getCategoriesDirective() ) ;
                }
            }
            else
            {
                return false;
            }
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_name.hashCode();
        hash ^= m_activation.hashCode();
        hash ^= m_categories.hashCode();
        return hash;
    }
}
