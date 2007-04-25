/*
 * Copyright 2004-2005 The Apache Software Foundation
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
package dpml.cli.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import dpml.cli.Argument;
import dpml.cli.Option;
import dpml.cli.WriteableCommandLine;
import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

/**
 * A WriteableCommandLine implementation allowing Options to write their
 * processed information to a CommandLine.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class WriteableCommandLineImpl extends CommandLineImpl implements WriteableCommandLine 
{
    private final Properties m_properties = new Properties();
    private final List m_options = new ArrayList();
    private final Map m_nameToOption = new HashMap();
    private final Map m_values = new HashMap();
    private final Map m_switches = new HashMap();
    private final Map m_defaultValues = new HashMap();
    private final Map m_defaultSwitches = new HashMap();
    private final List m_normalised;
    private final Set m_prefixes;

    /**
     * Creates a new WriteableCommandLineImpl rooted on the specified Option, to
     * hold the parsed arguments.
     *
     * @param rootOption the CommandLine's root Option
     * @param arguments the arguments this CommandLine represents
     */
    public WriteableCommandLineImpl(
      final Option rootOption, final List arguments )
    {
        m_prefixes = rootOption.getPrefixes();
        m_normalised = arguments;
    }

   /**
    * Add an option.
    * @param option the option to add
    */
    public void addOption( Option option )
    {
        m_options.add( option );
        m_nameToOption.put( option.getPreferredName(), option );
        for( Iterator i = option.getTriggers().iterator(); i.hasNext();)
        {
            m_nameToOption.put( i.next(), option );
        }
    }

   /**
    * Add an option.
    * @param option the option to add
    * @param value the option value
    */
    public void addValue( final Option option, final Object value )
    {
        if( option instanceof Argument )
        {
            addOption( option );
        }
        List valueList = (List) m_values.get( option );
        if( valueList == null )
        {
            valueList = new ArrayList();
            m_values.put( option, valueList );
        }
        valueList.add( value );
    }

   /**
    * Add a switch.
    * @param option the option to add
    * @param value the option value
    */
    public void addSwitch( final Option option, final boolean value )
    {
        addOption( option );
        if( m_switches.containsKey( option ) )
        {
            throw new IllegalStateException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.SWITCH_ALREADY_SET ) );
        } 
        else 
        {
            if( value )
            {
                m_switches.put( option, Boolean.TRUE );
            }
            else
            {
                m_switches.put( option, Boolean.FALSE );
            }
        }
    }

    /**
     * Detects the presence of an option in this CommandLine.
     * 
     * @param option the Option to search for
     * @return true iff the option is present
     */
    public boolean hasOption( final Option option )
    {
        return m_options.contains( option );
    }

    /**
     * Finds the Option with the specified trigger
     * 
     * @param trigger the name of the option to retrieve
     * @return the Option matching the trigger or null if none exists
     */
    public Option getOption( final String trigger )
    {
        return (Option) m_nameToOption.get( trigger );
    }

    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param option the Option associated with the values
     * @param defaultValues the result to return if no values are found
     * @return a list of values or defaultValues if none are found
     */
    public List getValues( final Option option, final List defaultValues )
    {
        // First grab the command line values
        List valueList = (List) m_values.get( option );

        // Secondly try the defaults supplied to the method
        if( ( valueList == null ) || valueList.isEmpty() )
        {
            valueList = defaultValues;
        }

        // Thirdly try the option's default values
        if( ( valueList == null ) || valueList.isEmpty() )
        {
            valueList = (List) m_defaultValues.get( option );
        }

        // Finally use an empty list
        if( valueList == null )
        {
            valueList = Collections.EMPTY_LIST;
        }

        return valueList;
    }

    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param option the Option associated with the value
     * @param defaultValue the Boolean to use if none match
     * @return the Boolean associated with option or defaultValue if none exists
     */
    public Boolean getSwitch( final Option option, final Boolean defaultValue )
    {
        // First grab the command line values
        Boolean bool = (Boolean) m_switches.get( option );

        // Secondly try the defaults supplied to the method
        if( bool == null )
        {
            bool = defaultValue;
        }

        // Thirdly try the option's default values
        if( bool == null )
        {
            bool = (Boolean) m_defaultSwitches.get( option );
        }

        return bool;
    }

   /**
    * Add a property to the commandline.
    * @param property the property name
    * @param value the property value
    */
    public void addProperty( final String property, final String value )
    {
        m_properties.setProperty( property, value );
    }

    /**
     * Retrieves the value associated with the specified property 
     * 
     * @param property the property name to lookup
     * @param defaultValue the value to use if no other is found
     * @return the value of the property or defaultValue
     */
    public String getProperty( final String property, final String defaultValue )
    {
        return m_properties.getProperty( property, defaultValue );
    }

    /**
     * Retrieves the set of all property names associated with this CommandLine
     * 
     * @return a none null set of property names 
     */
    public Set getProperties()
    {
        return Collections.unmodifiableSet( m_properties.keySet() );
    }

   /**
    * Return true if the trigger argument looks like an option.
    * @param trigger the trigger to evaluate
    * @return true if the trigger looks like an option
    */
    public boolean looksLikeOption( final String trigger )
    {
        for( final Iterator i = m_prefixes.iterator(); i.hasNext();)
        {
            final String prefix = (String) i.next();
            if( trigger.startsWith( prefix ) )
            {
                return true;
            }
        }
        return false;
    }

   /**
    * Return this commandline as a string.
    * @return the string representation
    */
    public String toString() 
    {
        final StringBuffer buffer = new StringBuffer();
        // need to add group header
        for( final Iterator i = m_normalised.iterator(); i.hasNext();) 
        {
            final String arg = ( String ) i.next();
            if( arg.indexOf( ' ' ) >= 0 )
            {
                buffer.append( "\"" ).append( arg ).append( "\"" );
            } 
            else 
            {
                buffer.append( arg );
            }
            if( i.hasNext() )
            {
                buffer.append( ' ' );
            }
        }
        return buffer.toString();
    }

    /**
     * Retrieves a list of all Options found in this CommandLine
     * 
     * @return a none null list of Options
     */
    public List getOptions()
    {
        return Collections.unmodifiableList( m_options );
    }

    /**
     * Retrieves a list of all Option triggers found in this CommandLine
     * 
     * @return a none null list of Option triggers
     */
    public Set getOptionTriggers()
    {
        return Collections.unmodifiableSet( m_nameToOption.keySet() );
    }

   /**
    * Set default values.
    * @param option the option
    * @param defaults a list of defaults
    */
    public void setDefaultValues( final Option option, final List defaults )
    {
        if( defaults == null )
        {
            m_defaultValues.remove( option );
        } 
        else 
        {
            m_defaultValues.put( option, defaults );
        }
    }

   /**
    * Set default switch.
    * @param option the option
    * @param defaultSwitch the default switch state
    */
    public void setDefaultSwitch( final Option option, final Boolean defaultSwitch ) 
    {
        if( defaultSwitch == null )
        {
            m_defaultSwitches.remove( defaultSwitch );
        } 
        else 
        {
            m_defaultSwitches.put( option, defaultSwitch );
        }
    }

   /**
    * Return the normalized collection.
    * @return the moprmalized collection
    */
    public List getNormalised()
    {
        return Collections.unmodifiableList( m_normalised );
    }
}
