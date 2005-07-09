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
import java.lang.Comparable;
import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.part.Part;

/**
 * Abstract base class for ComponentProfile and ContainmentProfile.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: DeploymentProfile.java 2991 2005-07-07 00:00:04Z mcconnell@dpml.net $
 */
public abstract class DeploymentProfile implements Serializable, Comparable, Part
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

    private static final ClassLoaderDirective EMPTY_CLASSLOADER_DIRECTIVE =
      new ClassLoaderDirective();

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
    * Logging category directives.
    */
    private final CategoriesDirective m_categories;

    /**
     * The classpath directive.
     */
    private ClassLoaderDirective m_classloader;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new deployment profile instance.
    * @param name the profile name
    * @param activation the activation policy
    * @param categories logging category directives
    */
    public DeploymentProfile( 
      final String name, int activation, CategoriesDirective categories ) 
    {
        this( name, activation, categories, null );
    }

   /**
    * Creation of a new deployment profile instance.
    * @param name the profile name
    * @param activation the activation policy
    * @param categories logging category directives
    */
    public DeploymentProfile( 
      final String name, int activation, CategoriesDirective categories, ClassLoaderDirective classloader ) 
    {
        m_activation = activation;

        if( null == categories )
        {
            m_categories = EMPTY_CATEGORIES;
        }
        else
        {
            m_categories = categories;
        }
        
        if( name == null )
        {
            m_name = "untitled";
        }
        else
        {
            m_name = name;
        }

        if( null == classloader )
        {
            m_classloader = EMPTY_CLASSLOADER_DIRECTIVE;
        }
        else
        {
            m_classloader = classloader;
        }
    }

    //--------------------------------------------------------------------------
    // Part
    //--------------------------------------------------------------------------

    /**
     * Return the part handler uri.
     * @return the uri of the part handler
     */
     public URI getPartHandlerURI()
     {
         return PART_HANDLER_URI;
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
    * @see #DEFAULT
    * @see #ENABLED
    * @see #DISABLED 
    */
    public int getActivationPolicy()
    {
        return m_activation;
    }

    /**
     * Return the classloader directive that describes the creation
     * arguments for the classloader required by this profile.
     *
     * @return the classloader directive
     */
    public ClassLoaderDirective getClassLoaderDirective()
    {
        return m_classloader;
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

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            if( other instanceof DeploymentProfile )
            {
                DeploymentProfile profile = (DeploymentProfile) other;
                if( false == m_name.equals( profile.getName() ) )
                {
                    return false;
                }
                if( m_activation != profile.getActivationPolicy() )
                {
                    return false;
                }
                if( false == m_categories.equals( profile.getCategoriesDirective() ) )
                {
                    return false;
                }
                if( false == m_classloader.equals( profile.getClassLoaderDirective() ) )
                {
                    return false;
                }
                return true;
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
        int hash = m_name.hashCode();
        hash ^= m_activation;
        hash ^= m_categories.hashCode();
        hash ^= m_classloader.hashCode();
        return hash;
    }

    private static URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );
    private static URI PART_BUILDER_URI = setupURI( "@PART-BUILDER-URI@" );

    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
}
