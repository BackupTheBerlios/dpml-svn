/*
 * Copyright 1997-2004 The Apache Software Foundation
 * Copyright 2005 Stephen J. McConnell, OSM
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

package net.dpml.lang;

import java.util.Map;

/**
 * Basic enum class for type-safe enums with values. Valued enum items can be compared and ordered
 * with the provided methods. Should be used as an abstract base. For example:
 *
 * <pre>
 * import net.dpml.lang.ValuedEnum;
 *
 * public final class JavaVersion
 *   extends ValuedEnum
 * {
 *   //standard enums for version of JVM
 *   public static final JavaVersion  JAVA1_0  = new JavaVersion( "Java 1.0", 100 );
 *   public static final JavaVersion  JAVA1_1  = new JavaVersion( "Java 1.1", 110 );
 *   public static final JavaVersion  JAVA1_2  = new JavaVersion( "Java 1.2", 120 );
 *   public static final JavaVersion  JAVA1_3  = new JavaVersion( "Java 1.3", 130 );
 *   public static final JavaVersion  JAVA1_4  = new JavaVersion( "Java 1.4", 140 );
 *
 *   private JavaVersion( final String name, final int value )
 *   {
 *     super( name, value );
 *   }
 * }
 * </pre>
 * Usage:
 * <pre>
 *   JavaVersion requiredVer = JavaVersion.JAVA1_2;
 *
 *   public Widget( Context context )
 *   {
 *     JavaVersion ver = (JavaVersion)context.get("java.version");
 *     if( ver.isLessThan( requiredVer ) )
 *     {
 *       throw new RuntimeException( requiredVer.getName()+ " or higher required" );
 *     }
 *   }
 * }
 * </pre>
 *
 * As with <code>Enum</code>, the {@link #ValuedEnum(String, int, Map)} constructor can be used to
 * populate a <code>Map</code>, from which further functionality can be derived.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class ValuedEnum extends Enum implements Comparable
{
    /**
     * The value contained in enum.
     */
    private final int m_value;

    /**
     * Constructor for enum item.
     *
     * @param name the name of enum item.
     * @param value the value of enum item.
     */
    protected ValuedEnum( final String name, final int value )
    {
        this( name, value, null );
    }

    /**
     * Constructor for enum item so that it gets added to Map at creation.
     * Adding to a map is useful for implementing find...() style methods.
     *
     * @param name the name of enum item.
     * @param value the value of enum item.
     * @param map the <code>Map</code> to add enum item to.
     */
    protected ValuedEnum( final String name, final int value, final Map map )
    {
        super( name, map );
        m_value = value;
    }

    /**
     * Get value of enum item.
     *
     * @return the enum item's value.
     */
    public final int getValue()
    {
        return m_value;
    }

    /**
     * Test if enum item is equal in value to other enum.
     *
     * @param other the other enum
     * @return true if equal
     */
    public final boolean isEqualTo( final ValuedEnum other )
    {
        return m_value == other.m_value;
    }

    /**
     * Test if enum item is greater than in value to other enum.
     *
     * @param other the other enum
     * @return true if greater than
     */
    public final boolean isGreaterThan( final ValuedEnum other )
    {
        return m_value > other.m_value;
    }

    /**
     * Test if enum item is greater than or equal in value to other enum.
     *
     * @param other the other enum
     * @return true if greater than or equal
     */
    public final boolean isGreaterThanOrEqual( final ValuedEnum other )
    {
        return m_value >= other.m_value;
    }

    /**
     * Test if enum item is less than in value to other enum.
     *
     * @param other the other enum
     * @return true if less than
     */
    public final boolean isLessThan( final ValuedEnum other )
    {
        return m_value < other.m_value;
    }

    /**
     * Test if enum item is less than or equal in value to other enum.
     *
     * @param other the other enum
     * @return true if less than or equal
     */
    public final boolean isLessThanOrEqual( final ValuedEnum other )
    {
        return m_value <= other.m_value;
    }

    /**
     * Tests for equality. Two Enum:s are considered equal
     * if they are of the same class, have the same name, and same value.
     *
     * @param o the other object
     * @return the equality status
     */
    public boolean equals( Object o )
    {
        boolean prelim = super.equals( o );
        if( !prelim )
        {
            return false;
        }
        if( !( o instanceof ValuedEnum ) )
        {
            return false;
        }
        final ValuedEnum enumerated = (ValuedEnum) o;
        return m_value == enumerated.m_value;
    }
    
   /**
    * Compute the hashcode value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_value;
        hash >>>= ( m_value & 31 );
        return hash;
    }
    
    /**
     * Override toString method to produce human readable description.
     *
     * @return String in the form <code>type:name#value</code>, eg.:
     * <code>JavaVersion[Java 1.0=100]</code>.
     */
    public String toString()
    {
        return getClass().getName() + ":" + getName() + "#" + m_value;
    }
    
   /**
    * Compare this instance with the supplied object.
    * @param other the object to compare against
    * @return int 1 if this instance is ranked higher than the supplied instance, 
    *    0 if the ranking is equivalent, and -1 if this instance is ranked lower than
    *    the supplied instance
    */
    public int compareTo( Object other )
    {
        if( other instanceof ValuedEnum )
        {
            ValuedEnum enummeration = (ValuedEnum) other;
            int value = enummeration.m_value;
            if( m_value > value )
            {
                return 1;
            }
            else if( m_value == value )
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return 1;
        }
    }
}

