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
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import net.dpml.cli.Argument;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.Group;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.Parent;
import net.dpml.cli.WriteableCommandLine;

/**
 * A base implementation of Parent providing limited ground work for further
 * Parent implementations.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class ParentImpl extends OptionImpl implements Parent
{
    private static final char NUL = '\0';
    private final Group m_children;
    private final Argument m_argument;
    private final String m_description;

   /**
    * Creation of a new ParaentImpl.
    * @param argument an argument
    * @param children the children
    * @param description the description
    * @param id the id
    * @param required the required flag
    */
    protected ParentImpl(
      final Argument argument, final Group children, final String description,
      final int id, final boolean required )
    {
        super( id, required );
        
        m_children = children;
        m_argument = argument;
        m_description = description;
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
    public void process( final WriteableCommandLine commandLine, final ListIterator arguments )
        throws OptionException
    {
        if( m_argument != null )
        {
            handleInitialSeparator( arguments, m_argument.getInitialSeparator() );
        }

        processParent( commandLine, arguments );

        if( m_argument != null )
        {
            m_argument.processValues( commandLine, arguments, this );
        }

        if( ( m_children != null ) && m_children.canProcess( commandLine, arguments ) ) 
        {
            m_children.process( commandLine, arguments );
        }
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
        final Set triggers = getTriggers();
        if( m_argument != null )
        {
            final char separator = m_argument.getInitialSeparator();

            // if there is a valid separator character
            if( separator != NUL )
            {
                final int initialIndex = arg.indexOf( separator );
                // if there is a separator present
                if( initialIndex > 0 )
                {
                    return triggers.contains( arg.substring( 0, initialIndex ) );
                }
            }
        }

        return triggers.contains( arg );
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
        if( null == m_children )
        {
            return Collections.EMPTY_SET;
        }
        else
        {
            return m_children.getPrefixes();
        }
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
        if( commandLine.hasOption( this ) )
        {
            if( m_argument != null )
            {
                m_argument.validate( commandLine, this );
            }

            if( m_children != null )
            {
                m_children.validate( commandLine );
            }
        }
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
        final boolean displayArgument =
          ( m_argument != null ) 
          && helpSettings.contains( DisplaySetting.DISPLAY_PARENT_ARGUMENT );
        final boolean displayChildren =
          ( m_children != null ) 
          && helpSettings.contains( DisplaySetting.DISPLAY_PARENT_CHILDREN );

        if( displayArgument )
        {
            buffer.append( ' ' );
            m_argument.appendUsage( buffer, helpSettings, comp );
        }

        if( displayChildren )
        {
            buffer.append( ' ' );
            m_children.appendUsage( buffer, helpSettings, comp );
        }
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
     * @see net.dpml.cli.HelpLine
     * @see net.dpml.cli.util.HelpFormatter
     * @param depth the initial indent depth
     * @param helpSettings the HelpSettings that should be applied
     * @param comp a comparator used to sort options when applicable.
     * @return a List of HelpLineImpl objects
     */
    public List helpLines(
      final int depth, final Set helpSettings, final Comparator comp )
    {
        final List helpLines = new ArrayList();
        helpLines.add( new HelpLineImpl( this, depth ) );

        if( helpSettings.contains( DisplaySetting.DISPLAY_PARENT_ARGUMENT ) && ( m_argument != null ) )
        {
            helpLines.addAll( m_argument.helpLines( depth + 1, helpSettings, comp ) );
        }

        if( helpSettings.contains( DisplaySetting.DISPLAY_PARENT_CHILDREN ) && ( m_children != null ) )
        {
            helpLines.addAll( m_children.helpLines( depth + 1, helpSettings, comp ) );
        }

        return helpLines;
    }

   /**
    * Return the argument value if any. 
    * @return Returns the argument.
    */
    public Argument getArgument()
    {
        return m_argument;
    }

    /**
     * Return any children.
     * @return Returns the children.
     */
    public Group getChildren()
    {
        return m_children;
    }

    /**
     * Split the token using the specified separator character.
     * @param arguments the current position in the arguments iterator
     * @param separator the separator char to split on
     */
    private void handleInitialSeparator(
      final ListIterator arguments, final char separator )
    {
        // next token
        final String newArgument = (String) arguments.next();

        // split the token
        final int initialIndex = newArgument.indexOf( separator );

        if( initialIndex > 0 )
        {
            arguments.remove();
            arguments.add( newArgument.substring( 0, initialIndex ) );
            arguments.add( newArgument.substring( initialIndex + 1 ) );
            arguments.previous();
        }
        arguments.previous();
    }
    
   /**
    * Recursively searches for an option with the supplied trigger.
    *
    * @param trigger the trigger to search for.
    * @return the matching option or null.
    */
    public Option findOption( final String trigger )
    {
        final Option found = super.findOption( trigger );
        if( ( found == null ) && ( m_children != null ) )
        {
            return m_children.findOption( trigger );
        } 
        else 
        {
            return found;
        }
    }

    /**
     * Adds defaults to a CommandLine.
     * 
     * Any defaults for this option are applied as well as the defaults for 
     * any contained options
     * 
     * @param commandLine the CommandLine object to store defaults in
     */
    public void defaults( final WriteableCommandLine commandLine )
    {
        super.defaults( commandLine );
        if( m_argument != null )
        {
            m_argument.defaultValues( commandLine, this );
        }
        if( m_children != null )
        {
            m_children.defaults( commandLine );
        }
    }
}
