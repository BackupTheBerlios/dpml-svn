/**
 * Copyright 2004 The Apache Software Foundation
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import dpml.cli.Option;

/**
 * A CommandLine implementation using a java Properties instance, useful for
 * constructing a complex DefaultingCommandLine
 *
 * Options are keyed from their property name and presence in the Properties
 * instance is taken as presence in the CommandLine.  Argument values are taken
 * from the property value and are optionally separated using the separator
 * char, defined at construction time.  Switch values can be specified using a
 * simple value of <code>true</code> or <code>false</code>; obviously this means
 * that Switches with Arguments are not supported by this implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 * @see java.util.Properties
 * @see dpml.cli.commandline.DefaultingCommandLine
 * @see dpml.cli.Option#getPreferredName() 
 */
public class PropertiesCommandLine extends CommandLineImpl
{
    
    private static final char NUL = '\0';
    private final Properties m_properties;
    private final Option m_root;
    private final char m_separator;
    
    /**
     * Creates a new PropertiesCommandLine using the specified root Option,
     * Properties instance.  The character 0 is used as the value separator.
     *
     * @param root the CommandLine's root Option
     * @param properties the Properties instance to get values from
     */
    public PropertiesCommandLine( final Option root, final Properties properties )
    {
        this( root, properties, NUL );
    }
    
    /**
     * Creates a new PropertiesCommandLine using the specified root Option,
     * Properties instance and value separator.
     *
     * @param root the CommandLine's root Option
     * @param properties the Properties instance to get values from
     * @param separator the character to split argument values
     */
    public PropertiesCommandLine( final Option root, final Properties properties, final char separator )
    {
        m_root = root;
        m_properties = properties;
        m_separator = separator;
    }
    
    /**
     * Detects the presence of an option in this CommandLine.
     * 
     * @param option the Option to search for
     * @return true iff the option is present
     */
    public boolean hasOption( Option option )
    {
        if( option==null )
        {
            return false;
        }
        else
        {
            return m_properties.containsKey( option.getPreferredName() );
        }
    }

    /**
     * Finds the Option with the specified trigger
     * 
     * @param trigger the name of the option to retrieve
     * @return the Option matching the trigger or null if none exists
     */
    public Option getOption( String trigger )
    {
        return m_root.findOption( trigger );
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
        final String value = m_properties.getProperty( option.getPreferredName() );
        
        if( value==null )
        {
            return defaultValues;
        }
        else if( m_separator > NUL )
        {
            final List values = new ArrayList();
            final StringTokenizer tokens = new StringTokenizer( value, String.valueOf( m_separator ) );
            
            while( tokens.hasMoreTokens() )
            {
                values.add( tokens.nextToken() );
            }
            return values;
        }
        else
        {
            return Collections.singletonList( value );
        }
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
        final String value = m_properties.getProperty( option.getPreferredName() );
        if( "true".equals( value ) )
        {
            return Boolean.TRUE;
        }
        else if( "false".equals( value ) )
        {
            return Boolean.FALSE;
        }
        else
        {
            return defaultValue;
        }
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
        return m_properties.keySet();
    }

    /**
     * Retrieves a list of all Options found in this CommandLine
     * 
     * @return a none null list of Options
     */
    public List getOptions()
    {
        final List options = new ArrayList();
        final Iterator keys = m_properties.keySet().iterator();
        while( keys.hasNext() )
        {
            final String trigger = (String) keys.next();
            final Option option = m_root.findOption( trigger );
            if( option!=null )
            {
                options.add( option );
            }
        }
        return Collections.unmodifiableList( options );
    }

    /**
     * Retrieves a list of all Option triggers found in this CommandLine
     * 
     * @return a none null list of Option triggers
     */
    public Set getOptionTriggers()
    {
        final Set triggers = new HashSet();
        final Iterator options = getOptions().iterator();
        while( options.hasNext() ) 
        {
            final Option option = (Option) options.next();
            triggers.addAll( option.getTriggers() );
        }
        return Collections.unmodifiableSet( triggers );
    }
}
