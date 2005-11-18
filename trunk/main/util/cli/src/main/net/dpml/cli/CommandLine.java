/**
 * Copyright 2003-2004 The Apache Software Foundation
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
package net.dpml.cli;

import java.util.List;
import java.util.Set;

/**
 * Instances of CommandLine represent a command line that has been processed
 * according to the definition supplied to the parser.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface CommandLine 
{
    
    /**
     * Detects the presence of an option with the specified trigger in this 
     * CommandLine.
     * 
     * @param trigger the trigger to search for
     * @return true iff an option with this trigger is present
     */
    boolean hasOption( String trigger );
    
    /**
     * Detects the presence of an option in this CommandLine.
     * 
     * @param option the Option to search for
     * @return true iff the option is present
     */
    boolean hasOption( Option option );
    
    /**
     * Finds the Option with the specified trigger
     * 
     * @param trigger the name of the option to retrieve
     * @return the Option matching the trigger or null if none exists
     */
    Option getOption( String trigger );
    
    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param trigger a trigger used to lookup the Option
     * @return a list of values or an empty List if none are found
     */
    List getValues( String trigger );
    
    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param trigger a trigger used to lookup the Option
     * @param defaultValues the result to return if no values are found
     * @return a list of values or defaultValues if none are found
     */
    List getValues( String trigger, List defaultValues );
    
    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param option the Option associated with the values
     * @return a list of values or an empty List if none are found
     */
    List getValues( Option option );
    
    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param option the Option associated with the values
     * @param defaultValues the result to return if no values are found
     * @return a list of values or defaultValues if none are found
     */
    List getValues( Option option, List defaultValues );
    
    /**
     * Retrieves the single Argument value associated with the specified Option
     * 
     * @param trigger a trigger used to lookup the Option
     * @return the matching value or null if none exists
     * @throws IllegalStateException if more than one values are found
     */
    Object getValue( String trigger ) throws IllegalStateException;
    
    /**
     * Retrieves the single Argument value associated with the specified Option
     * 
     * @param trigger a trigger used to lookup the Option
     * @param defaultValue the result to use if no values are found
     * @return the matching value or defaultValue if none exists
     * @throws IllegalStateException if more than one values are found
     */
    Object getValue( String trigger, Object defaultValue ) throws IllegalStateException;
    
    /**
     * Retrieves the single Argument value associated with the specified Option
     * 
     * @param option the Option associated with the value
     * @return the matching value or null if none exists
     * @throws IllegalStateException if more than one values are found
     */
    Object getValue( Option option ) throws IllegalStateException;
    
    /**
     * Retrieves the single Argument value associated with the specified Option
     * 
     * @param option the Option associated with the value
     * @param defaultValue the result to use if no values are found
     * @return the matching value or defaultValue if none exists
     * @throws IllegalStateException if more than one values are found
     */
    Object getValue( Option option, Object defaultValue ) throws IllegalStateException;
    
    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param trigger a trigger used to lookup the Option
     * @return the Boolean associated with trigger or null if none exists
     */
    Boolean getSwitch( String trigger );
    
    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param trigger a trigger used to lookup the Option
     * @param defaultValue the Boolean to use if none match
     * @return the Boolean associated with trigger or defaultValue if none exists
     */
    Boolean getSwitch( String trigger, Boolean defaultValue );
    
    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param option the Option associated with the value
     * @return the Boolean associated with option or null if none exists
     */
    Boolean getSwitch( Option option );
    
    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param option the Option associated with the value
     * @param defaultValue the Boolean to use if none match
     * @return the Boolean associated with option or defaultValue if none exists
     */
    Boolean getSwitch( Option option, Boolean defaultValue );
    
    /**
     * Retrieves the value associated with the specified property 
     * 
     * @param property the property name to lookup
     * @return the value of the property or null
     */
    String getProperty( String property );
    
    /**
     * Retrieves the value associated with the specified property 
     * 
     * @param property the property name to lookup
     * @param defaultValue the value to use if no other is found
     * @return the value of the property or defaultValue
     */
    String getProperty( String property, String defaultValue );
    
    /**
     * Retrieves the set of all property names associated with this CommandLine
     * 
     * @return a none null set of property names 
     */
    Set getProperties();
    
    /**
     * Retrieves the number of times the specified Option appeared in this 
     * CommandLine
     * 
     * @param trigger a trigger used to lookup the Option
     * @return the number of occurrences of the option
     */
    int getOptionCount( String trigger );
    
    /**
     * Retrieves the number of times the specified Option appeared in this 
     * CommandLine
     * 
     * @param option the Option associated to check
     * @return the number of occurrences of the option
     */
    int getOptionCount( Option option );
    
    /**
     * Retrieves a list of all Options found in this CommandLine
     * 
     * @return a none null list of Options
     */
    List getOptions();
    
    /**
     * Retrieves a list of all Option triggers found in this CommandLine
     * 
     * @return a none null list of Option triggers
     */
    Set getOptionTriggers();
}
