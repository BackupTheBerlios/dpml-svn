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
 
package net.dpml.state;

import java.util.Map;
import java.io.Serializable;

/**
 * Basic enum class for type-safe enums. Should be used as an abstract base. For example:
 *
 * <pre>
 * import net.dpml.transit.util.Enum;
 *
 * public final class Color extends Enum {
 *   public static final Color RED = new Color( "Red" );
 *   public static final Color GREEN = new Color( "Green" );
 *   public static final Color BLUE = new Color( "Blue" );
 *
 *   private Color( final String color )
 *   {
 *     super( color );
 *   }
 * }
 * </pre>
 *
 * If further operations, such as iterating over all items, are required, the
 * {@link #Enum(String, Map)} constructor can be used to populate a <code>Map</code>, from which
 * further functionality can be derived:
 * <pre>
 * public final class Color extends Enum {
 *   static final Map map = new HashMap();
 *
 *   public static final Color RED = new Color( "Red", map );
 *   public static final Color GREEN = new Color( "Green", map );
 *   public static final Color BLUE = new Color( "Blue", map );
 *
 *   private Color( final String color, final Map map )
 *   {
 *     super( color, map );
 *   }
 *
 *   public static Iterator iterator()
 *   {
 *     return map.values().iterator();
 *   }
 * }
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class Enum implements Serializable
{
    /**
     * The string representation of the Enum.
     */
    private final String m_name;

    /**
     * Constructor to add a new named item.
     *
     * @param name Name of the item.
     */
    protected Enum( final String name )
    {
        this( name, null );
    }

    /**
     * Constructor to add a new named item.
     *
     * @param name Name of the item.
     * @param map A <code>Map</code> to which newly constructed instances will be added.
     */
    protected Enum( final String name, final Map map )
    {
        m_name = name;
        if( null != map )
        {
            map.put( name, this );
        }
    }

    /**
     * Tests for equality. Two Enum:s are considered equal
     * if they are of the same class and have the same names.
     *
     * @param o the other object
     * @return the equality status
     */
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( !( o instanceof Enum ) )
        {
            return false;
        }
        final Enum enumerated = (Enum) o;
        if( !getClass().equals( enumerated.getClass() ) )
        {
            return false;
        }
        if( m_name != null ) 
        {
            if( !m_name.equals( enumerated.m_name ) )
            {
                return false;
            }
        }
        else
        {
            if( enumerated.m_name != null  )
            {
                return false;
            }
        }
        return true;
    }

   /** 
    * Compute the hashcode.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int result = 0;
        if( m_name != null )
        {
            result = m_name.hashCode();
        }
        result = 29 * result + getClass().hashCode();
        return result;
    }

    /**
     * Retrieve the name of this Enum item, set in the constructor.
     * @return the name <code>String</code> of this Enum item
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Human readable description of this Enum item in the form <code>type:name</code>, eg.:
     * <code>Color:Red</code>.
     * @return the string representation
     */
    public String toString()
    {
        return getClass().getName() + ":" + m_name;
    }
}
