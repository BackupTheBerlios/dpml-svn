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
package net.dpml.cli.option;

import java.util.Comparator;
import java.util.Set;

import net.dpml.cli.HelpLine;
import net.dpml.cli.Option;

/**
 * Represents a line in the help screen.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class HelpLineImpl implements HelpLine
{
    /** The option that this HelpLineImpl describes */
    private final Option m_option;

    /** The level of indenting for this item */
    private final int m_indent;

    /** The help settings used to obtain the previous usage */
    private transient Set m_cachedHelpSettings;
    
    /** The comparator used to obtain the previous usage */
    private transient Comparator m_cachedComparator;
    
    /** The previously obtained usage */
    private transient String m_cachedUsage;
    
    /**
     * Creates a new HelpLineImpl to represent a particular Option in the online
     * help.
     * 
     * @param option the Option that the HelpLineImpl describes
     * @param indent the level of indentation for this line
     */
    public HelpLineImpl( final Option option, final int indent )
    {
        m_option = option;
        m_indent = indent;
    }

    /**
     * @return The description of the option
     */
    public String getDescription() 
    {
        return m_option.getDescription();
    }

    /**
     * @return The level of indentation for this line
     */
    public int getIndent()
    {
        return m_indent;
    }

    /**
     * @return The Option that the help line relates to
     */
    public Option getOption()
    {
        return m_option;
    }
    
    /**
     * Builds a usage string for the option using the specified settings and 
     * comparator.
     * 
     * @param helpSettings the settings to apply
     * @param comparator a comparator to sort options when applicable
     * @return the usage string
     */
    public String usage( final Set helpSettings, final Comparator comparator )
    {
        if( m_cachedUsage == null
            || m_cachedHelpSettings != helpSettings
            || m_cachedComparator != comparator) 
        {
            
            // cache the arguments to avoid redoing work
            m_cachedHelpSettings = helpSettings;
            m_cachedComparator = comparator;
            
            // build the new buffer
            final StringBuffer buffer = new StringBuffer();
            for( int i = 0; i < m_indent; ++i )
            {
                buffer.append("  ");
            }
            m_option.appendUsage( buffer, helpSettings, comparator );
            
            // cache the usage string
            m_cachedUsage = buffer.toString();
        }
        return m_cachedUsage;
    }
}
