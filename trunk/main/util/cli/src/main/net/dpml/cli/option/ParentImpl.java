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

    protected ParentImpl(
      final Argument argument, final Group children, final String description,
      final int id, final boolean required )
    {
        super( id, required );
        
        m_children = children;
        m_argument = argument;
        m_description = description;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.Option#process(net.dpml.cli.CommandLine,
     *      java.util.ListIterator)
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

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.Option#canProcess(java.lang.String)
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
                    return triggers.contains( arg.substring(0, initialIndex ) );
                }
            }
        }

        return triggers.contains( arg );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.Option#prefixes()
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

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.Option#validate(net.dpml.cli.CommandLine)
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

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.Option#appendUsage(java.lang.StringBuffer,
     *      java.util.Set, java.util.Comparator)
     */
    public void appendUsage(
      final StringBuffer buffer, final Set helpSettings, final Comparator comp )
    {
        final boolean displayArgument =
            ( m_argument != null ) &&
            helpSettings.contains( DisplaySetting.DISPLAY_PARENT_ARGUMENT );
        final boolean displayChildren =
            ( m_children != null ) &&
            helpSettings.contains( DisplaySetting.DISPLAY_PARENT_CHILDREN );

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
     * @return a description of this parent option
     */
    public String getDescription()
    {
        return m_description;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.Option#helpLines(int, java.util.Set,
     *      java.util.Comparator)
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
     * @return Returns the m_argument.
     */
    public Argument getArgument()
    {
        return m_argument;
    }

    /**
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
            arguments.add(newArgument.substring( 0, initialIndex ) );
            arguments.add(newArgument.substring( initialIndex + 1 ) );
            arguments.previous();
        }
        arguments.previous();
    }

    /*
     * @see net.dpml.cli.Option#findOption(java.lang.String)
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
