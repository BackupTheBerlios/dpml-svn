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
package net.dpml.cli.commandline;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.dpml.cli.CommandLine;
import net.dpml.cli.Group;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.util.HelpFormatter;

/**
 * A class that implements the <code>Parser</code> interface can parse a
 * String array according to the {@link Group}specified and return a
 * {@link CommandLine}.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Parser
{
    private HelpFormatter m_helpFormatter = new HelpFormatter();
    private Option m_helpOption = null;
    private String m_helpTrigger = null;
    private Group m_group = null;

    /**
     * Parse the arguments according to the specified options and properties.
     *
     * @param arguments the command line arguments
     * @return the list of atomic option and value tokens
     * @throws OptionException if there are any problems encountered while parsing the
     *   command line tokens.
     */
    public CommandLine parse( final String[] arguments ) throws OptionException
    {
        // build a mutable list for the arguments
        final List argumentList = new LinkedList();

        // copy the arguments into the new list
        for( int i = 0; i < arguments.length; i++ )
        {
            final String argument = arguments[i];

            // ensure non intern'd strings are used 
            // so that == comparisons work as expected
            argumentList.add( new String( argument ) );
        }

        // wet up a command line for this group
        final WriteableCommandLine commandLine = 
          new WriteableCommandLineImpl( m_group, argumentList );

        // pick up any defaults from the model
        m_group.defaults( commandLine );

        // process the options as far as possible
        final ListIterator iterator = argumentList.listIterator();
        Object previous = null;
        
        while( m_group.canProcess( commandLine, iterator ) )
        {
            // peek at the next item and backtrack
            final Object next = iterator.next();
            iterator.previous();
            // if we have just tried to process this instance
            if( next == previous )
            {
                // abort
                break;
            }
            // remember previous
            previous = next;
            m_group.process( commandLine, iterator );
        }
        
        // if there are more arguments we have a problem
        if( iterator.hasNext() )
        {
            final String arg = (String) iterator.next();
            throw new OptionException(
              m_group, 
              ResourceConstants.UNEXPECTED_TOKEN, 
              arg );
        }
        
        // no need to validate if the help option is present
        if( !commandLine.hasOption( m_helpOption ) && !commandLine.hasOption( m_helpTrigger ) )
        {
            m_group.validate( commandLine );
        }
        return commandLine;
    }

    /**
     * Parse the arguments according to the specified options and properties and
     * displays the usage screen if the CommandLine is not valid or the help
     * option was specified.
     *
     * @param arguments the command line arguments
     * @return a valid CommandLine or null if the parse was unsuccessful
     * @throws IOException if an error occurs while formatting help
     */
    public CommandLine parseAndHelp( final String[] arguments ) throws IOException
    {
        m_helpFormatter.setGroup( m_group );

        try
        {
            // attempt to parse the command line
            final CommandLine commandLine = parse( arguments );
            if( !commandLine.hasOption( m_helpOption ) && !commandLine.hasOption( m_helpTrigger ) )
            {
                return commandLine;
            }
        } 
        catch( final OptionException oe )
        {
            // display help regarding the exception
            m_helpFormatter.setException( oe );
        }

        // print help
        m_helpFormatter.print();
        return null;
    }

    /**
     * Sets the Group of options to parse against
     * @param group the group of options to parse against
     */
    public void setGroup( final Group group )
    {
        m_group = group;
    }

    /**
     * Sets the HelpFormatter to use with the simplified parsing.
     * @see #parseAndHelp(String[])
     * @param helpFormatter the HelpFormatter to use with the simplified parsing
     */
    public void setHelpFormatter( final HelpFormatter helpFormatter )
    {
        m_helpFormatter = helpFormatter;
    }

    /**
     * Sets the help option to use with the simplified parsing.  For example
     * <code>--help</code>, <code>-h</code> and <code>-?</code> are often used.
     * @see #parseAndHelp(String[])
     * @param helpOption the help Option
     */
    public void setHelpOption( final Option helpOption )
    {
        m_helpOption = helpOption;
    }

    /**
     * Sets the help option to use with the simplified parsing.  For example
     * <code>--help</code>, <code>-h</code> and <code>-?</code> are often used.
     * @see #parseAndHelp(String[])
     * @param helpTrigger the trigger of the help Option
     */
    public void setHelpTrigger( final String helpTrigger )
    {
        m_helpTrigger = helpTrigger;
    }
}
