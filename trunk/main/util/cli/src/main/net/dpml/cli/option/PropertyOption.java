/*
 * Copyright 2003-2005 The Apache Software Foundation
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import net.dpml.cli.DisplaySetting;
import net.dpml.cli.HelpLine;
import net.dpml.cli.OptionException;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.resource.ResourceConstants;

/**
 * Handles the java style "-Dprop=value" opions
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PropertyOption extends OptionImpl
{
    public static final String DEFAULT_OPTION_STRING = "-D";
    public static final String DEFAULT_DESCRIPTION = "Set property values.";

    /**
     * A default PropertyOption instance
     */
    public static final PropertyOption INSTANCE = new PropertyOption();
    
    private final String m_optionString;
    private final String m_description;
    private final Set m_prefixes;

    /**
     * Creates a new PropertyOption using the default settings of a "-D" trigger
     * and an id of 'D'
     */
    public PropertyOption() 
    {
        this( DEFAULT_OPTION_STRING, DEFAULT_DESCRIPTION, 'D' );
    }

    /**
     * Creates a new PropertyOption using the specified parameters
     * @param optionString the trigger for the Option
     * @param description the description of the Option
     * @param id the id of the Option
     */
    public PropertyOption(
      final String optionString, final String description, final int id )
    {
        super( id, false );
        m_optionString = optionString;
        m_description = description;
        m_prefixes = Collections.singleton( optionString );
    }

    public boolean canProcess(
      final WriteableCommandLine commandLine, final String argument )
    {
        return ( argument != null ) 
          && argument.startsWith( m_optionString ) 
          && ( argument.length() > m_optionString.length() );
    }

    public Set getPrefixes() 
    {
        return m_prefixes;
    }

    public void process(
      final WriteableCommandLine commandLine, final ListIterator arguments )
      throws OptionException 
    {
        final String arg = (String) arguments.next();

        if( !canProcess( commandLine, arg ) )
        {
            throw new OptionException(
              this,
              ResourceConstants.UNEXPECTED_TOKEN, 
              arg );
        }
        
        final int propertyStart = m_optionString.length();
        final int equalsIndex = arg.indexOf( '=', propertyStart );
        final String property;
        final String value;

        if( equalsIndex < 0 )
        {
            property = arg.substring( propertyStart );
            value = "true";
        }
        else
        {
            property = arg.substring( propertyStart, equalsIndex );
            value = arg.substring( equalsIndex + 1 );
        }
        commandLine.addProperty( property, value );
    }

    public Set getTriggers()
    {
        return Collections.singleton( m_optionString );
    }

    public void validate( WriteableCommandLine commandLine )
    {
        // PropertyOption needs no validation
    }

    public void appendUsage(
      final StringBuffer buffer, final Set helpSettings, final Comparator comp ) 
    {
        final boolean display = helpSettings.contains( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        final boolean bracketed = helpSettings.contains( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );

        if( display )
        {
            buffer.append( m_optionString );
            if( bracketed ) 
            {
                buffer.append( '<' );
            }
            buffer.append( "property" );
            if (bracketed) 
            {
                buffer.append( '>' );
            }
            buffer.append( "=" );
            if( bracketed )
            {
                buffer.append( '<' );
            }
            buffer.append( "value" );
            if( bracketed )
            {
                buffer.append( '>' );
            }
        }
    }

    public String getPreferredName() 
    {
        return m_optionString;
    }

    public String getDescription()
    {
        return m_description;
    }

    public List helpLines(
      final int depth, final Set helpSettings, final Comparator comp )
    {
        if( helpSettings.contains( DisplaySetting.DISPLAY_PROPERTY_OPTION ) ) 
        {
            final HelpLine helpLine = new HelpLineImpl( this, depth );
            return Collections.singletonList( helpLine );
        } 
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
}
