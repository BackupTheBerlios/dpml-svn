/**
 * Copyright 2003-2004 The Apache Software Foundation
 * Copyright 2005 Stephen McConnell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.cli;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An enum of possible display settings. These settings are used to control the
 * presence of various features in the String representations of options,
 * CommandLines and usage strings.  Usually a Set of DisplaySetting instances
 * will be passed to a method that will lookup the presence of the values.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DisplaySetting 
{
    private static final Set ALL_SETTINGS = new HashSet();

    /**
     * A Set guarenteed to contain all possible DisplaySetting values
     */
    public static final Set ALL = Collections.unmodifiableSet( ALL_SETTINGS );
    
    /**
     * A Set guarenteed to contain no DisplaySetting values
     */
    public static final Set NONE = Collections.EMPTY_SET;
    
    /**
     * Indicates that aliases should be included
     */
    public static final DisplaySetting DISPLAY_ALIASES =
        new DisplaySetting( "DISPLAY_ALIASES" );
    
    /**
     * Indicates that optionality should be included
     */
    public static final DisplaySetting DISPLAY_OPTIONAL =
        new DisplaySetting( "DISPLAY_OPTIONAL" );
    
    /**
     * Indicates that property options should be included
     */
    public static final DisplaySetting DISPLAY_PROPERTY_OPTION =
        new DisplaySetting( "DISPLAY_PROPERTY_OPTION" );
    
    /**
     * Indicates that switches should be included enabled
     */
    public static final DisplaySetting DISPLAY_SWITCH_ENABLED =
        new DisplaySetting( "DISPLAY_SWITCH_ENABLED" );
    
    /**
     * Indicates that switches should be included disabled
     */
    public static final DisplaySetting DISPLAY_SWITCH_DISABLED =
        new DisplaySetting( "DISPLAY_SWITCH_DISABLED" );
    
    /**
     * Indicates that group names should be included
     */
    public static final DisplaySetting DISPLAY_GROUP_NAME =
        new DisplaySetting( "DISPLAY_GROUP_NAME" );
    
    /**
     * Indicates that groups should be included expanded
     */
    public static final DisplaySetting DISPLAY_GROUP_EXPANDED =
        new DisplaySetting( "DISPLAY_GROUP_EXPANDED" );
    
    /**
     * Indicates that group arguments should be included
     */
    public static final DisplaySetting DISPLAY_GROUP_ARGUMENT =
        new DisplaySetting( "DISPLAY_GROUP_ARGUMENT" );
    
    /**
     * Indicates that group outer brackets should be included
     */
    public static final DisplaySetting DISPLAY_GROUP_OUTER =
        new DisplaySetting( "DISPLAY_GROUP_OUTER" );
    
    /**
     * Indicates that arguments should be included numbered
     */
    public static final DisplaySetting DISPLAY_ARGUMENT_NUMBERED =
        new DisplaySetting( "DISPLAY_ARGUMENT_NUMBERED" );
    
    /**
     * Indicates that arguments should be included bracketed
     */
    public static final DisplaySetting DISPLAY_ARGUMENT_BRACKETED =
        new DisplaySetting( "DISPLAY_ARGUMENT_BRACKETED" );
    
    /**
     * Indicates that arguments of Parents should be included
     */
    public static final DisplaySetting DISPLAY_PARENT_ARGUMENT =
        new DisplaySetting( "DISPLAY_PARENT_ARGUMENT" );
    
    /**
     * Indicates that children of Parents should be included
     */
    public static final DisplaySetting DISPLAY_PARENT_CHILDREN =
        new DisplaySetting( "DISPLAY_PARENT_CHILDREN" );
    
    /**
     * The name of the setting
     */
    private final String m_name;
    
    /**
     * The hashCode of the setting
     */
    private final int m_hashCode;

    /**
     * Creates a new DisplaySetting with the specified name
     * @param name the name of the setting
     */
    private DisplaySetting( final String name ) 
    {
        m_name = name;
        m_hashCode = name.hashCode();
        ALL_SETTINGS.add( this );
    }

   /**
    * Return the instance hashcode value.
    * @return the hash value
    */
    public int hashCode() 
    {
        return m_hashCode;
    }

   /**
    * Test this object for equality with the supplied object.
    * @param that the other object
    * @return true if the objects are equal
    */
    public boolean equals( final Object that ) 
    {
        if( that instanceof DisplaySetting )
        {
            return m_name.compareTo( that.toString() ) == 0;
        }
        return false;
    }

   /**
    * Return a string representation of the instance.
    * @return the string
    */
    public String toString()
    {
        return m_name;
    }
}
