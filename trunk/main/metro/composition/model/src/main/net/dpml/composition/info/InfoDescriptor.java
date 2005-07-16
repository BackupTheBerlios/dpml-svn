/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.composition.info;

import java.util.Properties;
import net.dpml.part.service.Version;

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
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: InfoDescriptor.java 2522 2005-05-12 11:23:50Z mcconnell@dpml.net $
 */
public final class InfoDescriptor extends Descriptor
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    public static final String TRANSIENT = "transient";
    public static final String SINGLETON = "singleton";
    public static final String THREAD = "thread";

    public static final String WEAK = "weak";
    public static final String SOFT = "soft";
    public static final String HARD = "hard";

    public static final int TRANSIENT_LIFESTYLE = 0;
    public static final int THREAD_LIFESTYLE = 1;
    public static final int SINGLETON_LIFESTYLE = 2;

    public static final int UNDEFINED_COLLECTION = -1;
    public static final int WEAK_COLLECTION = 0;
    public static final int SOFT_COLLECTION = 1;
    public static final int HARD_COLLECTION = 2;

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
     * WEAK_COLLECTION, SOFT_COLLECTION or HARD_COLLECTION.  A component implementing a WEAK_COLLECTION policy
     * will be decommissioned if no references exist.  A component declaring a
     * SOFT_COLLECTION policy will exist without reference so long as memory contention
     * does not occur.  A component implementing HARD_COLLECTION policies will be
     * maintained irrespective of usage and memory constraints so long as its
     * scope exists (the jvm for a "singleton" and Thread for "thread" lifestyles).
     * The default policy is HARD_COLLECTION.
     */
    private final int m_collection;

    /**
     * Flag indicating if this is a native metro descriptor or avalon descriptor.
     * TODO: change this to a versioned service protocol spec
     */
    private final boolean m_native;

    /**
     * Flag indicating if the type is threadsafe.
     */
    private final boolean m_threadsafe;


    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    /**
     * Creation of a new info descriptor using a supplied name, key, version
     * and attribute set.
     *
     * @param name the default component name
     * @param classname the implemetation classname
     */
    public InfoDescriptor( final String name, final String classname )
            throws IllegalArgumentException, NullPointerException
    {
        this( name, classname, null, null, null, null, false, true, null );
    }

    /**
     * Creation of a new info descriptor using a supplied name, key, version
     * and attribute set.
     *
     * @param name the default component name
     * @param classname the implemetation classname
     * @param version the implementation version
     * @param lifestyle the component lifestyle (singleton, per-thread, etc.)
     * @param collection the garbage collection policy for the component
     * @param threadsafe if TRUE the type is declaring itself as threadsafe
     * @param schema the configuration schema
     * @param flag native metro flag
     * @param attributes a set of attributes associated with the component type
     * @exception IllegalArgumentException if the implementation key is not a classname
     * @exception NullArgumentException if the classname argument is null.
     * @since 1.2
     */
    public InfoDescriptor( final String name,
                           final String classname,
                           final Version version,
                           final String lifestyle,
                           final String collection,
                           final String schema,
                           final boolean threadsafe,
                           final boolean flag,
                           final Properties attributes )
            throws IllegalArgumentException, NullPointerException
    {
        super( attributes );

        m_native = flag;
        m_threadsafe = threadsafe;

        if ( null == classname )
        {
            throw new NullPointerException( "classname" );
        }

        if ( classname.indexOf( "/" ) > -1 )
        {
            throw new IllegalArgumentException( "classname: " + classname );
        }

        m_classname = classname;

        if( null == version )
        {
            m_version = Version.getVersion( "1.0.0" );
        }
        else
        {
            m_version = version;
        }
        m_schema = schema;

        if( lifestyle == null )
        {
            if( threadsafe )
            {
                m_lifestyle = SINGLETON;
            }
            else
            {
                m_lifestyle = TRANSIENT;
            }
        }
        else
        {
            validateLifestyle( lifestyle );
            m_lifestyle = lifestyle;
        }

        int p = getCollectionPolicy( collection );
        if( p > UNDEFINED_COLLECTION )
        {
            if(( m_lifestyle == TRANSIENT ) && ( p == HARD_COLLECTION ))
            {
                m_collection = SOFT_COLLECTION;
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
                m_collection = SOFT_COLLECTION;
            }
            else
            {
                m_collection = HARD_COLLECTION;
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

   /**
    * Check that the supplied lifestyle attribute is a valid value.
    * @param lifestyle the lifestyle string
    * @exception IllegalArgumentException if the value is not recognized
    */
    private void validateLifestyle( String lifestyle )
        throws IllegalArgumentException
    {
        if ( lifestyle.equals( TRANSIENT )
                || lifestyle.equals( SINGLETON )
                || lifestyle.equals( THREAD ) )
        {
            return;
        }
        final String error = "Lifestyle policy not recognized: " + lifestyle;
        throw new IllegalArgumentException( error );
    }

   /**
    * Internal utility to get the name of the class without the package name. Used
    * when constructing a default component name.
    * @param classname the fully qualified classname
    * @return the short class name without the package name
    */
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
    * Test is the component type implements a weak collection policy.
    *
    * @return TRUE if the policy is weak
    */
    public boolean isWeak()
    {
        return m_collection == WEAK_COLLECTION;
    }

    /**
     * Test is the component type implements a soft collection policy.
     *
     * @return TRUE if the policy is soft
     */
    public boolean isSoft()
    {
        return m_collection == SOFT_COLLECTION;
    }

    /**
     * Test is the component type implements a hard collection policy.
     *
     * @return TRUE if the policy is hard
     */
    public boolean isHard()
    {
        return m_collection == HARD_COLLECTION;
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
     * Ruturn TRUE is this type is threadsafe.
     *
     * @return the threadsafe status
     */
    public boolean isThreadsafe()
    {
        return m_threadsafe;
    }

    /**
     * Ruturn TRUE is this is a native descriptor else FALSE.
     *
     * @return the native status
     */
    public boolean isNative()
    {
        return m_native;
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
    public boolean equals( Object other )
    {
        boolean isEqual = super.equals(other) && other instanceof InfoDescriptor;

        if (isEqual)
        {
            InfoDescriptor info = (InfoDescriptor)other;
            isEqual = isEqual && m_native == info.m_native;
            isEqual = isEqual && m_threadsafe == info.m_threadsafe;
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

        if( m_native )
        {
            hash = hash + 818972391;
        }
        else
        {
            hash = hash - 293741397;
        }
        if( m_threadsafe )
        {
            hash = hash + 383972391;
        }
        else
        {
            hash = hash - 113741397;
        }
        hash ^= m_classname.hashCode();
        if ( null != m_name )
        {
            hash ^= m_name.hashCode();
        }

        if ( null != m_lifestyle )
        {
            hash = hash + 9873234;
            hash ^= m_lifestyle.hashCode();
        }

        if ( null != m_version )
        {
            hash ^= m_version.hashCode();
        }
        return hash;
    }

   /**
    * Return a string value of a lifestyle preference.
    * @param policy the lifestyle preference
    * @return the value as a string
    */
    public static String getLifestylePreferenceKey( int policy )
    {
        if( policy == TRANSIENT_LIFESTYLE )
        {
            return TRANSIENT;
        }
        else if( policy == THREAD_LIFESTYLE )
        {
            return THREAD;
        }
        else if( policy == SINGLETON_LIFESTYLE )
        {
            return SINGLETON;
        }
        else
        {
            final String error = 
              "Invalid lifestyle policy value ["
              + policy
              + "]";
            throw new IllegalArgumentException( error );
        }
    }


   /**
    * Return a string value of a collection policy.
    * @param policy the collection policy
    * @return the value as a string
    */
    public static String getCollectionPolicyKey( int policy )
    {
        if ( policy == UNDEFINED_COLLECTION )
        {
            return null;
        }
        else
        {
            if( policy == HARD_COLLECTION )
            {
                return HARD;
            }
            else if( policy == SOFT_COLLECTION )
            {
                return SOFT;
            }
            else if( policy == WEAK_COLLECTION )
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

   /**
    * Return the integer value of a collection policy string.
    * @param policy the collection policy string value
    * @return the value as a collection policy integer value
    */
    public static int getCollectionPolicy( String policy )
    {
        if ( policy == null )
        {
            return UNDEFINED_COLLECTION;
        }
        else
        {
            if( policy.equalsIgnoreCase( HARD ) )
            {
                return HARD_COLLECTION;
            }
            else if( policy.equalsIgnoreCase( SOFT ))
            {
                return SOFT_COLLECTION;
            }
            else if(  policy.equalsIgnoreCase( WEAK ))
            {
                return WEAK_COLLECTION;
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
