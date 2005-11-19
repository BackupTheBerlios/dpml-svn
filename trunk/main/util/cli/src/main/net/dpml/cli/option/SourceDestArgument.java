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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.dpml.cli.Argument;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * An Argument implementation that allows a variable size Argument to precede a
 * fixed size argument.  The canonical example of it's use is in the unix
 * <code>cp</code> command where a number of source can be specified with
 * exactly one destination specfied at the end.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SourceDestArgument extends ArgumentImpl
{
    private final Argument m_source;
    private final Argument m_dest;

    /**
     * Creates a SourceDestArgument using defaults where possible.
     *
     * @param source the variable size Argument
     * @param dest the fixed size Argument
     */
    public SourceDestArgument(
      final Argument source, final Argument dest )
    {
        this( 
          source, 
          dest, 
          DEFAULT_INITIAL_SEPARATOR, 
          DEFAULT_SUBSEQUENT_SEPARATOR,
          DEFAULT_CONSUME_REMAINING, 
          null );
    }

    /**
     * Creates a SourceDestArgument using the specified parameters.
     *
     * @param source the variable size Argument
     * @param dest the fixed size Argument
     * @param initialSeparator the inistial separator to use
     * @param subsequentSeparator the subsequent separator to use
     * @param consumeRemaining the token triggering consume remaining behaviour
     * @param defaultValues the default values for the SourceDestArgument
     */
    public SourceDestArgument(
      final Argument source, final Argument dest, final char initialSeparator,
      final char subsequentSeparator, final String consumeRemaining,
      final List defaultValues )
    {
        super( 
          "SourceDestArgument", null, sum( source.getMinimum(), dest.getMinimum() ),
          sum( source.getMaximum(), dest.getMaximum() ), initialSeparator, 
          subsequentSeparator, null, consumeRemaining, defaultValues, 0 );

        m_source = source;
        m_dest = dest;

        if( dest.getMinimum() != dest.getMaximum() )
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.SOURCE_DEST_MUST_ENFORCE_VALUES ) );
        }
    }

    private static int sum( final int a, final int b )
    {
        return Math.max( a, Math.max( b, a + b ) );
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
        final int length = buffer.length();

        m_source.appendUsage( buffer, helpSettings, comp );

        if( buffer.length() != length )
        {
            buffer.append( ' ' );
        }

        m_dest.appendUsage( buffer, helpSettings, comp );
    }

    /**
     * Builds up a list of HelpLineImpl instances to be presented by HelpFormatter.
     * 
     * @see net.dpml.cli.HelpLine
     * @see net.dpml.cli.util.HelpFormatter
     * @param depth the initial indent depth
     * @param helpSettings the HelpSettings that should be applied
     * @param comp a comparator used to sort options when applicable.
     * @return a List of HelpLineImpl objects
     */
    public List helpLines(
      int depth, Set helpSettings, Comparator comp )
    {
        final List helpLines = new ArrayList();
        helpLines.addAll( m_source.helpLines( depth, helpSettings, comp ) );
        helpLines.addAll( m_dest.helpLines( depth, helpSettings, comp ) );
        return helpLines;
    }

    /**
     * Checks that the supplied CommandLine is valid with respect to the
     * suppled option.
     * 
     * @param commandLine the CommandLine to check.
     * @param option the option to evaluate
     * @throws OptionException if the CommandLine is not valid.
     */
    public void validate( WriteableCommandLine commandLine, Option option )
      throws OptionException
    {
        final List values = commandLine.getValues( option );

        final int limit = values.size() - m_dest.getMinimum();
        int count = 0;

        final Iterator i = values.iterator();

        while( count++ < limit )
        {
            commandLine.addValue( m_source, i.next() );
        }
        
        while( i.hasNext() )
        {
            commandLine.addValue( m_dest, i.next() );
        }
        
        m_source.validate( commandLine, m_source );
        m_dest.validate( commandLine, m_dest );
    }

    /**
     * Indicates whether this Option will be able to process the particular
     * argument.
     * 
     * @param commandLine the CommandLine object to store defaults in
     * @param arg the argument to be tested
     * @return true if the argument can be processed by this Option
     */
    public boolean canProcess(
      final WriteableCommandLine commandLine, final String arg )
    {
        return m_source.canProcess( commandLine, arg ) || m_dest.canProcess( commandLine, arg );
    }
}
