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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.dpml.cli.Argument;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.Group;
import net.dpml.cli.HelpLine;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.resource.ResourceConstants;

/**
 * An implementation of Group
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class GroupImpl extends OptionImpl implements Group 
{
    private final String m_name;
    private final String m_description;
    private final List m_options;
    private final int m_minimum;
    private final int m_maximum;
    private final List m_anonymous;
    private final SortedMap m_optionMap;
    private final Set m_prefixes;

    /**
     * Creates a new GroupImpl using the specified parameters.
     *
     * @param options the Options and Arguments that make up the Group
     * @param name the name of this Group, or null
     * @param description a description of this Group
     * @param minimum the minimum number of Options for a valid CommandLine
     * @param maximum the maximum number of Options for a valid CommandLine
     */
    public GroupImpl(
      final List options, final String name, final String description,
      final int minimum, final int maximum )
    {
        super( 0, false );

        m_name = name;
        m_description = description;
        m_minimum = minimum;
        m_maximum = maximum;

        // store a copy of the options to be used by the 
        // help methods
        m_options = Collections.unmodifiableList( options );

        // m_anonymous Argument temporary storage
        final List newAnonymous = new ArrayList();

        // map (key=trigger & value=Option) temporary storage
        final SortedMap newOptionMap = new TreeMap( ReverseStringComparator.getInstance() );

        // prefixes temporary storage
        final Set newPrefixes = new HashSet();

        // process the options
        for( final Iterator i = options.iterator(); i.hasNext();)
        {
            final Option option = (Option) i.next();
            if( option instanceof Argument ) 
            {
                i.remove();
                newAnonymous.add( option );
            } 
            else
            {
                final Set triggers = option.getTriggers();
                for( Iterator j = triggers.iterator(); j.hasNext();)
                {
                    newOptionMap.put( j.next(), option );
                }
                // store the prefixes
                newPrefixes.addAll( option.getPrefixes() );
            }
        }

        m_anonymous = Collections.unmodifiableList( newAnonymous );
        m_optionMap = Collections.unmodifiableSortedMap( newOptionMap );
        m_prefixes = Collections.unmodifiableSet( newPrefixes );
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
        if( arg == null )
        {
            return false;
        }

        // if arg does not require bursting
        if( m_optionMap.containsKey( arg ) )
        {
            return true;
        }

        // filter
        final Map tailMap = m_optionMap.tailMap( arg );

        // check if bursting is required
        for( final Iterator iter = tailMap.values().iterator(); iter.hasNext();)
        {
            final Option option = (Option) iter.next();
            if( option.canProcess( commandLine, arg ) )
            {
                return true;
            }
        }
        
        if( commandLine.looksLikeOption( arg ) )
        {
            return false;
        }

        // m_anonymous argument(s) means we can process it
        if( m_anonymous.size() > 0 )
        {
            return true;
        }

        return false;
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
        return m_optionMap.keySet();
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
        String previous = null;

        // [START process each command line token
        while( arguments.hasNext() )
        {
            // grab the next argument
            final String arg = (String) arguments.next();

            // if we have just tried to process this instance
            if( arg == previous )
            {
                // rollback and abort
                arguments.previous();
                break;
            }

            // remember last processed instance
            previous = arg;

            final Option opt = (Option) m_optionMap.get( arg );

            // option found
            if( opt != null )
            {
                arguments.previous();
                opt.process( commandLine, arguments );
            }
            // [START option NOT found
            else
            {
                // it might be an m_anonymous argument continue search
                // [START argument may be m_anonymous
                if( commandLine.looksLikeOption( arg ) )
                {
                    // narrow the search
                    final Collection values = m_optionMap.tailMap( arg ).values();
                    boolean foundMemberOption = false;
                    for( Iterator i = values.iterator(); i.hasNext() && !foundMemberOption;)
                    {
                        final Option option = (Option) i.next();
                        if( option.canProcess( commandLine, arg ) )
                        {
                            foundMemberOption = true;
                            arguments.previous();
                            option.process( commandLine, arguments );
                        }
                    }

                    // back track and abort this group if necessary
                    if( !foundMemberOption )
                    {
                        arguments.previous();
                        return;
                    }
                    
                } // [END argument may be m_anonymous
                // [START argument is NOT m_anonymous
                else 
                {
                    // move iterator back, current value not used
                    arguments.previous();

                    // if there are no m_anonymous arguments then this group can't
                    // process the argument
                    if( m_anonymous.isEmpty() )
                    {
                        break;
                    }

                    // why do we iterate over all m_anonymous arguments?
                    // canProcess will always return true?
                    for( final Iterator i = m_anonymous.iterator(); i.hasNext();)
                    {
                        final Argument argument = (Argument) i.next();
                        if( argument.canProcess( commandLine, arguments ) )
                        {
                            argument.process( commandLine, arguments );
                        }
                    }
                } // [END argument is NOT m_anonymous
            } // [END option NOT found
        } // [END process each command line token
    }

    /**
     * Checks that the supplied CommandLine is valid with respect to this
     * option.
     * 
     * @param commandLine the CommandLine to check.
     * @throws OptionException if the CommandLine is not valid.
     */
    public void validate( final WriteableCommandLine commandLine ) throws OptionException 
    {
        // number of options found
        int present = 0;

        // reference to first unexpected option
        Option unexpected = null;

        for( final Iterator i = m_options.iterator(); i.hasNext();)
        {
            final Option option = (Option) i.next();

            // if the child option is required then validate it
            if( option.isRequired() )
            {
                option.validate( commandLine );
            }

            if( option instanceof Group )
            {
                option.validate( commandLine );
            }

            // if the child option is present then validate it
            if( commandLine.hasOption( option ) )
            {
                if( ++present > m_maximum )
                {
                    unexpected = option;
                    break;
                }
                option.validate( commandLine );
            }
        }

        // too many options
        if( unexpected != null )
        {
            throw new OptionException(
              this,
              ResourceConstants.UNEXPECTED_TOKEN,
              unexpected.getPreferredName() );
        }

        // too few option
        if( present < m_minimum )
        {
            throw new OptionException(
              this,
              ResourceConstants.MISSING_OPTION );
        }

        // validate each m_anonymous argument
        for( final Iterator i = m_anonymous.iterator(); i.hasNext();)
        {
            final Option option = (Option) i.next();
            option.validate( commandLine );
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
        return m_name;
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
     * Appends usage information to the specified StringBuffer
     * 
     * @param buffer the buffer to append to
     * @param helpSettings a set of display settings @see DisplaySetting
     * @param comp a comparator used to sort the Options
     */
    public void appendUsage(
      final StringBuffer buffer, final Set helpSettings, final Comparator comp ) 
    {
        if( getMaximum() == 1 )
        {
            appendUsage( buffer, helpSettings, comp, "|" );
        }
        else
        {
            appendUsage( buffer, helpSettings, comp, " " );
        }
    }

    /**
     * Appends usage information to the specified StringBuffer
     * 
     * @param buffer the buffer to append to
     * @param helpSettings a set of display settings @see DisplaySetting
     * @param comp a comparator used to sort the Options
     * @param separator the String used to separate member Options 
     */
    public void appendUsage(
      final StringBuffer buffer, final Set helpSettings, final Comparator comp,
      final String separator )
    {
        final Set helpSettingsCopy = new HashSet( helpSettings );

        final boolean optional =
          ( m_minimum == 0 ) 
          && helpSettingsCopy.contains( DisplaySetting.DISPLAY_OPTIONAL );

        final boolean expanded =
          ( m_name == null ) 
          || helpSettingsCopy.contains( DisplaySetting.DISPLAY_GROUP_EXPANDED );

        final boolean named =
          !expanded 
          || ( ( m_name != null ) && helpSettingsCopy.contains( DisplaySetting.DISPLAY_GROUP_NAME ) );

        final boolean arguments = 
          helpSettingsCopy.contains( DisplaySetting.DISPLAY_GROUP_ARGUMENT );

        final boolean outer = 
          helpSettingsCopy.contains( DisplaySetting.DISPLAY_GROUP_OUTER );

        helpSettingsCopy.remove( DisplaySetting.DISPLAY_GROUP_OUTER );

        final boolean both = named && expanded;

        if( optional )
        {
            buffer.append( '[' );
        }

        if( named )
        {
            buffer.append( m_name );
        }

        if( both )
        {
            buffer.append( " (" );
        }

        if( expanded )
        {
            final Set childSettings;

            if( !helpSettingsCopy.contains( DisplaySetting.DISPLAY_GROUP_EXPANDED ) )
            {
                childSettings = DisplaySetting.NONE;
            }
            else
            {
                childSettings = new HashSet( helpSettingsCopy );
                childSettings.remove( DisplaySetting.DISPLAY_OPTIONAL );
            }

            // grab a list of the group's options.
            final List list;

            if( comp == null )
            {
                // default to using the initial order
                list = m_options;
            } 
            else
            {
                // sort options if comparator is supplied
                list = new ArrayList( m_options );
                Collections.sort( list, comp );
            }

            // for each option.
            for( final Iterator i = list.iterator(); i.hasNext();)
            {
                final Option option = (Option) i.next();

                // append usage information
                option.appendUsage( buffer, childSettings, comp );

                // add separators as needed
                if( i.hasNext() )
                {
                    buffer.append( separator );
                }
            }
        }

        if( both ) 
        {
            buffer.append( ')' );
        }

        if( optional && outer )
        {
            buffer.append( ']' );
        }

        if( arguments )
        {
            for( final Iterator i = m_anonymous.iterator(); i.hasNext();)
            {
                buffer.append( ' ' );
                final Option option = (Option) i.next();
                option.appendUsage( buffer, helpSettingsCopy, comp );
            }
        }

        if( optional && !outer )
        {
            buffer.append( ']' );
        }
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
        final List helpLines = new ArrayList();

        if( helpSettings.contains( DisplaySetting.DISPLAY_GROUP_NAME ) )
        {
            final HelpLine helpLine = new HelpLineImpl( this, depth );
            helpLines.add( helpLine );
        }

        if( helpSettings.contains( DisplaySetting.DISPLAY_GROUP_EXPANDED ) )
        {
            // grab a list of the group's options.
            final List list;

            if( comp == null )
            {
                // default to using the initial order
                list = m_options;
            } 
            else
            {
                // sort options if comparator is supplied
                list = new ArrayList( m_options );
                Collections.sort( list, comp );
            }

            // for each option
            for( final Iterator i = list.iterator(); i.hasNext();)
            {
                final Option option = (Option) i.next();
                helpLines.addAll( option.helpLines( depth + 1, helpSettings, comp ) );
            }
        }

        if( helpSettings.contains( DisplaySetting.DISPLAY_GROUP_ARGUMENT ) )
        {
            for( final Iterator i = m_anonymous.iterator(); i.hasNext();)
            {
                final Option option = (Option) i.next();
                helpLines.addAll( option.helpLines( depth + 1, helpSettings, comp ) );
            }
        }

        return helpLines;
    }

    /**
     * Gets the member Options of thie Group.
     * Note this does not include any Arguments
     * @return only the non Argument Options of the Group
     */
    public List getOptions()
    {
        return m_options;
    }

    /**
     * Gets the m_anonymous Arguments of this Group.
     * @return the Argument options of this Group
     */
    public List getAnonymous() 
    {
        return m_anonymous;
    }

   /**
    * Recursively searches for an option with the supplied trigger.
    *
    * @param trigger the trigger to search for.
    * @return the matching option or null.
    */
    public Option findOption( final String trigger ) 
    {
        final Iterator i = getOptions().iterator();

        while( i.hasNext() ) 
        {
            final Option option = (Option) i.next();
            final Option found = option.findOption( trigger );
            if( found != null )
            {
                return found;
            }
        }
        return null;
    }

    /**
     * Retrieves the minimum number of values required for a valid Argument
     *
     * @return the minimum number of values
     */
    public int getMinimum()
    {
        return m_minimum;
    }

    /**
     * Retrieves the maximum number of values acceptable for a valid Argument
     *
     * @return the maximum number of values
     */
    public int getMaximum() 
    {
        return m_maximum;
    }

    /**
     * Indicates whether argument values must be present for the CommandLine to
     * be valid.
     *
     * @see #getMinimum()
     * @see #getMaximum()
     * @return true iff the CommandLine will be invalid without at least one 
     *         value
     */
    public boolean isRequired()
    {
        return getMinimum() > 0;
    }

   /**
    * Process defaults.
    * @param commandLine the commandline
    */
    public void defaults( final WriteableCommandLine commandLine )
    {
        super.defaults( commandLine );
        for( final Iterator i = m_options.iterator(); i.hasNext();)
        {
            final Option option = (Option) i.next();
            option.defaults( commandLine );
        }

        for( final Iterator i = m_anonymous.iterator(); i.hasNext();)
        {
            final Option option = (Option) i.next();
            option.defaults( commandLine );
        }
    }
}

/**
* A reverse string comparator.
*/
final class ReverseStringComparator implements Comparator 
{
    private static final Comparator INSTANCE = new ReverseStringComparator();

    private ReverseStringComparator() 
    {
        // static
    }

    /**
     * Gets a singleton instance of a ReverseStringComparator
     * @return the singleton instance
     */
    public static final Comparator getInstance() 
    {
        return INSTANCE;
    }

   /**
    * Compare two instances.
    * @param o1 the first instance
    * @param o2 the second instance
    * @return the result
    */
    public int compare( final Object o1, final Object o2 )
    {
        final String s1 = (String) o1;
        final String s2 = (String) o2;
        return -s1.compareTo( s2 );
    }
}
