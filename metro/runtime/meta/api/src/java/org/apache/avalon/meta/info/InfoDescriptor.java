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

package org.apache.avalon.meta.info;

import java.util.Properties;

import org.apache.avalon.framework.Version;

/**
 * This class is used to provide explicit information to assembler
 * and administrator about the Component. It includes information
 * such as;
 *
 * <ul>
 *   <li>a symbolic name</li>
 *   <li>classname</li>
 *   <li>version</li>
 * </ul>
 *
 * <p>The InfoDescriptor also includes an arbitrary set
 * of attributes about component. Usually these are container
 * specific attributes that can store arbitrary information.
 * The attributes should be stored with keys based on package
 * name of container.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: InfoDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class InfoDescriptor extends Descriptor
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    public static final String TRANSIENT = "transient";
    public static final String SINGLETON = "singleton";
    public static final String THREAD = "thread";
    public static final String POOLED = "pooled";

    public static final String LIBERAL_KEY = "liberal";
    public static final String DEMOCRAT_KEY = "democrat";
    public static final String CONSERVATIVE_KEY = "conservative";
    public static final int UNDEFINED = -1;
    public static final int LIBERAL = 0;
    public static final int DEMOCRAT = 1;
    public static final int CONSERVATIVE = 2;

    public static final String WEAK_KEY = "weak";
    public static final String SOFT_KEY = "soft";
    public static final String HARD_KEY = "hard";
    public static final int WEAK = 0;
    public static final int SOFT = 1;
    public static final int HARD = 2;

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    /**
     * The short name of the Component Type. Useful for displaying
     * human readable strings describing the type in
     * assembly tools or generators.
     */
    private final String m_name;

    /**
     * The implementation classname.
     */
    private final String m_classname;

    /**
     * The version of component that descriptor describes.
     */
    private final Version m_version;

    /**
     * The component lifestyle.
     */
    private final String m_lifestyle;

    /**
     * The component configuration schema.
     */
    private final String m_schema;

    /**
     * The component garbage collection policy. The value returned is either 
     * LIBERAL, DEMOCAT or CONSERVATIVE.  A component implementing a LIBERAL policy 
     * will be decommissioned if no references exist.  A component declaring a 
     * DEMOCRAT policy will exist without reference so long as memory contention
     * does not occur.  A component implementing CONSERVATIVE policies will be 
     * maintained irrespective of usage and memory constraints so long as its 
     * scope exists (the jvm for a "singleton" and Thread for "thread" lifestyles).  
     * The default policy is CONSERVATIVE.
     */
    private final int m_collection;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    /**
     * Creation of a new info descriptor using a supplied name, key, version
     * and attribute set.
     *
     * @param name the component name
     * @param classname the implemetation classname
     * @param version the implementation version
     * @param attributes a set of attributes associated with the component type
     * @exception IllegalArgumentException if the implementation key is not a classname
     * @since 1.2
     */
    public InfoDescriptor( final String name,
                           final String classname,
                           final Version version,
                           final String lifestyle,
                           final String collection,
                           final String schema,
                           final Properties attributes )
            throws IllegalArgumentException
    {
        super( attributes );

        if ( null == classname ) throw new NullPointerException( "classname" );

        if ( classname.indexOf( "/" ) > -1 )
        {
            throw new IllegalArgumentException( "classname: " + classname );
        }

        m_classname = classname;
        m_version = version;
        m_schema = schema;

        if ( lifestyle == null )
        {
            m_lifestyle = TRANSIENT;
        }
        else
        {
            validateLifestyle( lifestyle );
            m_lifestyle = lifestyle;
        }

        int p = getCollectionPolicy( collection );
        if( p > UNDEFINED )
        {
            if(( m_lifestyle == TRANSIENT ) && ( p == HARD ))
            {
                m_collection = SOFT;
            }
            else
            {    
                m_collection = p;
            }
        }
        else
        {
            if( m_lifestyle == TRANSIENT )
            {
                m_collection = SOFT;
            }
            else
            {
                m_collection = HARD;
            }
        }

        if ( name != null )
        {
            m_name = name;
        }
        else
        {
            m_name = getClassName( classname );
        }
    }

    private void validateLifestyle( String lifestyle ) throws IllegalArgumentException
    {
        if ( lifestyle.equals( TRANSIENT )
                || lifestyle.equals( SINGLETON )
                || lifestyle.equals( THREAD )
                || lifestyle.equals( POOLED ) )
        {
            return;
        }
        final String error = "Lifestyle policy not recognized: " + lifestyle;
        throw new IllegalArgumentException( error );
    }

    private String getClassName( String classname )
    {
        int i = classname.lastIndexOf( "." );
        if ( i == -1 )
        {
            return classname.toLowerCase();
        }
        else
        {
            return classname.substring( i + 1, classname.length() ).toLowerCase();
        }
    }

    /**
     * Return the symbolic name of component.
     *
     * @return the symbolic name of component.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the component termination policy as a String.
     *
     * @return the policy
     */
    public int getCollectionPolicy()
    {
        return m_collection;
    }

   /**
    * Test is the component type implements a liberal collection policy.
    *
    * @return the policy
    */
    public boolean isLiberal()
    {
        return m_collection == LIBERAL;
    }

    /**
     * Test is the component type implements a democrat collection policy.
     *
     * @return the policy
     */
    public boolean isDemocrat()
    {
        return m_collection == DEMOCRAT;
    }

    /**
     * Test is the component type implements a coservative collection policy.
     *
     * @return the policy
     */
    public boolean isConservative()
    {
        return m_collection == CONSERVATIVE;
    }

    /**
     * Return the configuration schema.
     *
     * @return the schema declaration (possibly null)
     */
    public String getConfigurationSchema()
    {
        return m_schema;
    }

    /**
     * Return the implementation class name for the component type.
     *
     * @return the implementation class name
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the version of component.
     *
     * @return the version of component.
     */
    public Version getVersion()
    {
        return m_version;
    }

    /**
     * Return the component lifestyle.
     *
     * @return the lifestyle
     */
    public String getLifestyle()
    {
        return m_lifestyle;
    }

    /**
     * Return a string representation of the info descriptor.
     * @return the stringified type
     */
    public String toString()
    {
        return "[" + getName() + "] " + getClassname() + ":" + getVersion();
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals(Object other)
    {
        boolean isEqual = super.equals(other) && other instanceof InfoDescriptor;

        if (isEqual)
        {
            InfoDescriptor info = (InfoDescriptor)other;
            isEqual = isEqual && m_classname.equals( info.m_classname );
            isEqual = isEqual && ( m_collection == info.m_collection );
            isEqual = isEqual && m_name.equals( info.m_name );
            isEqual = isEqual && m_lifestyle.equals( info.m_lifestyle );

            if ( null == m_version )
            {
                isEqual = isEqual && null == info.m_version;
            }
            else
            {
                isEqual = isEqual && m_version.equals(info.m_version);
            }
        }

        return isEqual;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();

        hash >>>= 7;
        hash ^= m_classname.hashCode();
        hash >>>= 7;

        if ( null != m_name )
        {
            hash >>>= 7;
            hash ^= m_name.hashCode();
        }

        if ( null != m_lifestyle )
        {
            hash >>>= 7;
            hash ^= m_lifestyle.hashCode();
        }

        if ( null != m_version )
        {
            hash >>>= 7;
            hash ^= m_version.hashCode();
        }

        return hash;
    }

    public static String getCollectionPolicyKey( int policy )
    {
        if ( policy == UNDEFINED )
        {
            return null;
        }
        else
        {
            if( policy == HARD )
            {
                return HARD_KEY;
            }
            else if( policy == SOFT )
            {
                return SOFT_KEY;
            }
            else if( policy == WEAK )
            {
                return WEAK_KEY;
            }
            else
            {
                final String error =
                  "Unrecognized collection argument [" + policy + "]";
                throw new IllegalArgumentException( error );
            }
        }
    }

    public static int getCollectionPolicy( String policy )
    {
        if ( policy == null )
        {
            return UNDEFINED;
        }
        else
        {
            if( policy.equalsIgnoreCase( CONSERVATIVE_KEY ) || policy.equalsIgnoreCase( HARD_KEY ) )
            {
                return HARD;
            }
            else if( policy.equalsIgnoreCase( DEMOCRAT_KEY ) || policy.equalsIgnoreCase( SOFT_KEY ))
            {
                return SOFT;
            }
            else if( policy.equalsIgnoreCase( LIBERAL_KEY ) || policy.equalsIgnoreCase( WEAK_KEY ))
            {
                return WEAK;
            }
            else
            {
                final String error =
                  "Unrecognized collection argument [" + policy + "]";
                throw new IllegalArgumentException( error );
            }
        }
    }

}
