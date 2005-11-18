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

package net.dpml.metro.info;

import java.util.Properties;

import net.dpml.metro.part.Version;

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
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class InfoDescriptor extends Descriptor
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

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
    private final LifestylePolicy m_lifestyle;

    /**
     * The component garbage collection policy. The value returned is either
     * WEAK, SOFT, HARD or HARD.  A component implementing a WEAK policy
     * will be decommissioned if no references exist.  A component declaring a
     * SOFT policy will exist without reference so long as memory contention
     * does not occur.  A component implementing HARD policies will be
     * maintained irrespective of usage and memory constraints.  The default
     * policy is SYSTEM which implies delegation of policy selection to the 
     * component's container.
     */
    private final CollectionPolicy m_collection;

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
     * @exception IllegalArgumentException if the classname is invalid
     * @exception NullPointerException if the classname is null
     */
    public InfoDescriptor( final String name, final String classname )
            throws IllegalArgumentException, NullPointerException
    {
        this( name, classname, null, null, CollectionPolicy.SYSTEM, false, null );
    }

    /**
     * Creation of a new info descriptor using a supplied name, key, version
     * and attribute set.
     *
     * @param name the default component name
     * @param classname the implemetation classname
     * @param version the implementation version
     * @param lifestyle the component lifestyle (singleton, thread, etc.)
     * @param collection the garbage collection policy for the component
     * @param threadsafe if TRUE the type is declaring itself as threadsafe
     * @param attributes a set of attributes associated with the component type
     * @exception IllegalArgumentException if the implementation classname is invalid
     * @exception NullPointerException if the classname argument is null.
     */
    public InfoDescriptor( final String name,
                           final String classname,
                           final Version version,
                           final LifestylePolicy lifestyle,
                           final CollectionPolicy collection,
                           final boolean threadsafe,
                           final Properties attributes )
            throws IllegalArgumentException, NullPointerException
    {
        super( attributes );

        m_threadsafe = threadsafe;

        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }

        if( classname.indexOf( "/" ) > -1 )
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
        
        if( lifestyle == null )
        {
            if( threadsafe )
            {
                m_lifestyle = LifestylePolicy.SINGLETON;
            }
            else
            {
                m_lifestyle = LifestylePolicy.TRANSIENT;
            }
        }
        else
        {
            m_lifestyle = lifestyle;
        }
        
        if( null == collection )
        {
            m_collection = CollectionPolicy.SYSTEM;
        }
        else
        {
            m_collection = collection;
        }
        
        if( name != null )
        {
            m_name = name;
        }
        else
        {
            m_name = getClassName( classname );
        }
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
        if( i == -1 )
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
     * Return the component collection policy.
     *
     * @return the policy
     */
    public CollectionPolicy getCollectionPolicy()
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
        return m_collection.equals( CollectionPolicy.WEAK );
    }

    /**
     * Test is the component type implements a soft collection policy.
     *
     * @return TRUE if the policy is soft
     */
    public boolean isSoft()
    {
        return m_collection.equals( CollectionPolicy.SOFT );
    }

    /**
     * Test is the component type implements a hard collection policy.
     *
     * @return TRUE if the policy is hard
     */
    public boolean isHard()
    {
        return m_collection.equals( CollectionPolicy.HARD );
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
    public LifestylePolicy getLifestyle()
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
     * Return a string representation of the info descriptor.
     * @return the stringified type
     */
    public String toString()
    {
        return "[" + getName() + "] " + getClassname() + ":" + getVersion();
    }

   /**
    * Test is the supplied object is equal to this object.
    * @param other the other object
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        boolean isEqual = super.equals( other ) && other instanceof InfoDescriptor;
        if( isEqual )
        {
            InfoDescriptor info = (InfoDescriptor) other;
            isEqual = isEqual && m_threadsafe == info.m_threadsafe;
            isEqual = isEqual && m_classname.equals( info.m_classname );
            isEqual = isEqual && m_collection.equals( info.m_collection );
            isEqual = isEqual && m_name.equals( info.m_name );
            isEqual = isEqual && m_lifestyle.equals( info.m_lifestyle );
            if( null == m_version )
            {
                isEqual = isEqual && null == info.m_version;
            }
            else
            {
                isEqual = isEqual && m_version.equals( info.m_version );
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
        hash ^= m_collection.hashCode();
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
}
