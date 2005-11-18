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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import net.dpml.cli.Argument;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.HelpLine;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;
import net.dpml.cli.validation.InvalidArgumentException;
import net.dpml.cli.validation.Validator;

/**
 * An implementation of an Argument.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArgumentImpl extends OptionImpl implements Argument 
{
    private static final char NUL = '\0';

    /**
     * The default value for the initial separator char.
     */
    public static final char DEFAULT_INITIAL_SEPARATOR = NUL;

    /**
     * The default value for the subsequent separator char.
     */
    public static final char DEFAULT_SUBSEQUENT_SEPARATOR = NUL;

    /**
     * The default token to indicate that remaining arguments should be consumed
     * as values.
     */
    public static final String DEFAULT_CONSUME_REMAINING = "--";
    
    private final String m_name;
    private final String m_description;
    private final int m_minimum;
    private final int m_maximum;
    private final char m_initialSeparator;
    private final char m_subsequentSeparator;
    private final boolean m_subsequentSplit;
    private final Validator m_validator;
    private final String m_consumeRemaining;
    private final List m_defaultValues;
    private final ResourceHelper m_resources = ResourceHelper.getResourceHelper();

    /**
     * Creates a new Argument instance.
     *
     * @param name the name of the argument
     * @param description a description of the argument
     * @param minimum the minimum number of values needed to be valid
     * @param maximum the maximum number of values allowed to be valid
     * @param initialSeparator the char separating option from value
     * @param subsequentSeparator the char separating values from each other
     * @param validator object responsible for validating the values
     * @param consumeRemaining String used for the "consuming option" group
     * @param valueDefaults values to be used if none are specified.
     * @param id the id of the option, 0 implies automatic assignment.
     *
     * @see OptionImpl#OptionImpl(int,boolean)
     */
    public ArgumentImpl(
      final String name, final String description, final int minimum, final int maximum,
      final char initialSeparator, final char subsequentSeparator, final Validator validator,
      final String consumeRemaining, final List valueDefaults, final int id ) 
    {
        super( id, false );

        m_description = description;
        m_minimum = minimum;
        m_maximum = maximum;
        m_initialSeparator = initialSeparator;
        m_subsequentSeparator = subsequentSeparator;
        m_subsequentSplit = subsequentSeparator != NUL;
        m_validator = validator;
        m_consumeRemaining = consumeRemaining;
        m_defaultValues = valueDefaults;

        if( null == name )
        {
            m_name = "arg";
        }
        else
        {
            m_name = name;
        }
        
        if( m_minimum > m_maximum )
        {
            throw new IllegalArgumentException(
              m_resources.getMessage(
                ResourceConstants.ARGUMENT_MIN_EXCEEDS_MAX ) );
        }

        if( ( m_defaultValues != null ) && ( m_defaultValues.size() > 0 ) )
        {
            if( valueDefaults.size() < minimum )
            {
                throw new IllegalArgumentException(
                  m_resources.getMessage(
                    ResourceConstants.ARGUMENT_TOO_FEW_DEFAULTS ) );
            }
            if( m_defaultValues.size() > maximum )
            {
                throw new IllegalArgumentException(
                  m_resources.getMessage( 
                    ResourceConstants.ARGUMENT_TOO_MANY_DEFAULTS ) );
            }
        }
    }

    public String getPreferredName()
    {
        return m_name;
    }

    public void processValues(
      final WriteableCommandLine commandLine, final ListIterator arguments, final Option option )
      throws OptionException
    {
        int argumentCount = commandLine.getValues( option, Collections.EMPTY_LIST ).size();

        while( arguments.hasNext() && ( argumentCount < m_maximum ) )
        {
            final String allValues = stripBoundaryQuotes( (String) arguments.next() );

            // should we ignore things that look like options?
            if( allValues.equals( m_consumeRemaining ) )
            {
                while( arguments.hasNext() && ( argumentCount < m_maximum ) )
                {
                    ++argumentCount;
                    commandLine.addValue( option, arguments.next() );
                }
            }
            // does it look like an option?
            else if( commandLine.looksLikeOption( allValues ) )
            {
                arguments.previous();
                break;
            }
            // should we split the string up?
            else if( m_subsequentSplit)
            {
                final StringTokenizer values =
                  new StringTokenizer( allValues, String.valueOf( m_subsequentSeparator ) );
                arguments.remove();

                while( values.hasMoreTokens() && ( argumentCount < m_maximum ) )
                {
                    ++argumentCount;
                    final String token = values.nextToken();
                    commandLine.addValue( option, token );
                    arguments.add( token );
                }

                if( values.hasMoreTokens() )
                {
                    throw new OptionException(
                      option, 
                      ResourceConstants.ARGUMENT_UNEXPECTED_VALUE,
                      values.nextToken() );
                }
            }
            else 
            {
                // it must be a value as it is
                ++argumentCount;
                commandLine.addValue( option, allValues );
            }
        }
    }

    public boolean canProcess( final WriteableCommandLine commandLine, final String arg )
    {
        return true;
    }

    public Set getPrefixes()
    {
        return Collections.EMPTY_SET;
    }

    public void process( WriteableCommandLine commandLine, ListIterator args )
      throws OptionException
    {
        processValues( commandLine, args, this );
    }

    public char getInitialSeparator()
    {
        return m_initialSeparator;
    }

    public char getSubsequentSeparator()
    {
        return m_subsequentSeparator;
    }

    public Set getTriggers()
    {
        return Collections.EMPTY_SET;
    }

    public String getConsumeRemaining()
    {
        return m_consumeRemaining;
    }
    
    public List getDefaultValues() 
    {
        return m_defaultValues;
    }
    
    public Validator getValidator()
    {
        return m_validator;
    }
    
    public void validate( final WriteableCommandLine commandLine ) throws OptionException 
    {
        validate( commandLine, this );
    }

    public void validate(
      final WriteableCommandLine commandLine, final Option option )
      throws OptionException 
    {
        final List values = commandLine.getValues( option );
        if( values.size() < m_minimum )
        {
            throw new OptionException(
              option, 
              ResourceConstants.ARGUMENT_MISSING_VALUES );
        }

        if( values.size() > m_maximum )
        {
            throw new OptionException(
              option, 
              ResourceConstants.ARGUMENT_UNEXPECTED_VALUE,
              (String) values.get( m_maximum ) );
        }

        if( m_validator != null )
        {
            try 
            {
                m_validator.validate( values );
            } 
            catch( InvalidArgumentException ive )
            {
                throw new OptionException(
                  option, 
                  ResourceConstants.ARGUMENT_UNEXPECTED_VALUE,
                  ive.getMessage() );
            }
        }
    }

    public void appendUsage(
      final StringBuffer buffer, final Set helpSettings, final Comparator comp )
    {
        // do we display the outer optionality
        final boolean optional = helpSettings.contains( DisplaySetting.DISPLAY_OPTIONAL );

        // allow numbering if multiple args
        final boolean numbered =
            ( m_maximum > 1 ) 
            && helpSettings.contains( DisplaySetting.DISPLAY_ARGUMENT_NUMBERED );

        final boolean bracketed = helpSettings.contains( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );

        // if infinite args are allowed then crop the list
        final int max = ( m_maximum == Integer.MAX_VALUE ) ? 2 : m_maximum;

        int i = 0;

        // for each argument
        while( i < max )
        {
            // if we're past the first add a space
            if( i > 0 )
            {
                buffer.append( ' ' );
            }

            // if the next arg is optional
            if( ( i >= m_minimum ) && ( optional || ( i > 0 ) ) )
            {
                buffer.append( '[' );
            }

            if( bracketed )
            {
                buffer.append( '<' );
            }

            // add name
            buffer.append( m_name );
            ++i;

            // if numbering
            if( numbered )
            {
                buffer.append( i );
            }

            if( bracketed )
            {
                buffer.append( '>' );
            }
        }

        // if infinite args are allowed
        if( m_maximum == Integer.MAX_VALUE )
        {
            // append elipsis
            buffer.append( " ..." );
        }

        // for each argument
        while( i > 0 ) 
        {
            --i;
            // if the next arg is optional
            if( ( i >= m_minimum ) && ( optional || ( i > 0 ) ) )
            {
                buffer.append( ']' );
            }
        }
    }

    public String getDescription()
    {
        return m_description;
    }

    public List helpLines( final int depth, final Set helpSettings, final Comparator comp )
    {
        final HelpLine helpLine = new HelpLineImpl( this, depth );
        return Collections.singletonList( helpLine );
    }

    public int getMaximum()
    {
        return m_maximum;
    }

    public int getMinimum()
    {
        return m_minimum;
    }

    /**
     * If there are any leading or trailing quotes remove them from the
     * specified token.
     *
     * @param token the token to strip leading and trailing quotes
     * @return String the possibly modified token
     */
    public String stripBoundaryQuotes( String token ) 
    {
        if( !token.startsWith( "\"" ) || !token.endsWith( "\"" ) )
        {
            return token;
        }
        token = token.substring( 1, token.length() - 1 );
        return token;
    }

    public boolean isRequired()
    {
        return getMinimum() > 0;
    }

    public void defaults( final WriteableCommandLine commandLine )
    {
        super.defaults( commandLine );
        defaultValues( commandLine, this );
    }

    public void defaultValues( final WriteableCommandLine commandLine, final Option option )
    {
        commandLine.setDefaultValues( option, m_defaultValues );
    }
}
