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
   /** 
    * The default property option name.
    */
    public static final String DEFAULT_OPTION_STRING = "-D";
    
   /** 
    * The default property option description.
    */
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

    /**
     * Indicates whether this Option will be able to process the particular
     * argument.
     * 
     * @param commandLine the CommandLine object to store defaults in
     * @param argument the argument to be tested
     * @return true if the argument can be processed by this Option
     */
    public boolean canProcess(
      final WriteableCommandLine commandLine, final String argument )
    {
        return ( argument != null ) 
          && argument.startsWith( m_optionString ) 
          && ( argument.length() > m_optionString.length() );
    }

    /**
     * Identifies the argument prefixes that should be considered options. This
     * is used to identify whether a given string looks like an option or an
     * argument value. Typically an option would return the set [--,-] while
     * switches might offer [-,+].
     * 
     * The returned Set must not be null.
     * 
     * @return The set of prefixes for this Option
     */
    public Set getPrefixes() 
    {
        return m_prefixes;
    }

    /**
     * Processes String arguments into a CommandLine.
     * 
     * The iterator will initially point at the first argument to be processed
     * and at the end of the method should point to the first argument not
     * processed. This method MUST process at least one argument from the
     * ListIterator.
     * 
     * @param commandLine the CommandLine object to store results in
     * @param arguments the arguments to process
     * @throws OptionException if any problems occur
     */
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

    /**
     * Identifies the argument prefixes that should trigger this option. This
     * is used to decide which of many Options should be tried when processing
     * a given argument string.
     * 
     * The returned Set must not be null.
     * 
     * @return The set of triggers for this Option
     */
    public Set getTriggers()
    {
        return Collections.singleton( m_optionString );
    }

    /**
     * Checks that the supplied CommandLine is valid with respect to this
     * option.
     * 
     * @param commandLine the CommandLine to check.
     * @throws OptionException if the CommandLine is not valid.
     */
    public void validate( WriteableCommandLine commandLine ) throws OptionException
    {
        // PropertyOption needs no validation
    }

    /**
     * Appends usage information to the specified StringBuffer
     * 
     * @param buffer the buffer to append to
     * @param helpSettings a set of display settings @see DisplaySetting
     * @param comp a comparator used to sort the Options
     */
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
            if( bracketed ) 
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

    /**
     * The preferred name of an option is used for generating help and usage
     * information.
     * 
     * @return The preferred name of the option
     */
    public String getPreferredName() 
    {
        return m_optionString;
    }

    /**
     * Returns a description of the option. This string is used to build help
     * messages as in the HelpFormatter.
     * 
     * @see net.dpml.cli.util.HelpFormatter
     * @return a description of the option.
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Builds up a list of HelpLineImpl instances to be presented by HelpFormatter.
     * 
     * @see HelpLine
     * @see net.dpml.cli.util.HelpFormatter
     * @param depth the initial indent depth
     * @param helpSettings the HelpSettings that should be applied
     * @param comp a comparator used to sort options when applicable.
     * @return a List of HelpLineImpl objects
     */
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
