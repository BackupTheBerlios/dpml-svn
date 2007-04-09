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
package dpml.cli.option;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

import dpml.cli.DisplaySetting;
import dpml.cli.Option;
import dpml.cli.WriteableCommandLine;
import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

/**
 * A base implementation of Option providing limited ground work for further
 * Option implementations.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class OptionImpl implements Option 
{
    private final int m_id;
    private final boolean m_required;

    /**
     * Creates an OptionImpl with the specified id
     * @param id the unique id of this Option
     * @param required true iff this Option must be present
     */
    public OptionImpl( final int id, final boolean required ) 
    {
        m_id = id;
        m_required = required;
    }

    /**
     * Indicates whether this Option will be able to process the particular
     * argument. The ListIterator must be restored to the initial state before
     * returning the boolean.
     * 
     * @see #canProcess(WriteableCommandLine,String)
     * @param commandLine the CommandLine object to store defaults in
     * @param arguments the ListIterator over String arguments
     * @return true if the argument can be processed by this Option
     */
    public boolean canProcess( 
      final WriteableCommandLine commandLine, final ListIterator arguments )
    {
        if( arguments.hasNext() )
        {
            final String argument = (String) arguments.next();
            arguments.previous();
            return canProcess( commandLine, argument );
        } 
        else 
        {
            return false;
        }
    }

   /**
    * Returns a string representation of the option.
    * @return the string value
    */
    public String toString() 
    {
        final StringBuffer buffer = new StringBuffer();
        appendUsage( buffer, DisplaySetting.ALL, null );
        return buffer.toString();
    }

    /**
     * Returns the id of the option.  This can be used in a loop and switch 
     * construct:
     * 
     * <code>
     * for(Option o : cmd.getOptions()){
     *     switch(o.getId()){
     *         case POTENTIAL_OPTION:
     *             ...
     *     }
     * }
     * </code> 
     * 
     * The returned value is not guarenteed to be unique.
     * 
     * @return the id of the option.
     */
    public int getId() 
    {
        return m_id;
    }

   /**
    * Evaluate this instance against the supplied instance for equality.
    * @param thatObj the other object
    * @return true if the supplied instance is equal to this instance
    */
    public boolean equals( final Object thatObj )
    {
        if( thatObj instanceof OptionImpl )
        {
            final OptionImpl that = (OptionImpl) thatObj;
            return ( getId() == that.getId() ) 
              && equals( getPreferredName(), that.getPreferredName() ) 
              && equals( getDescription(), that.getDescription() ) 
              && equals( getPrefixes(), that.getPrefixes() ) 
              && equals( getTriggers(), that.getTriggers() );
        }
        else
        {
            return false;
        }
    }

    private boolean equals( Object left, Object right )
    {
        if( ( left == null ) && ( right == null ) )
        {
            return true;
        }
        else if( ( left == null ) || ( right == null ) )
        {
            return false;
        } 
        else
        {
            return left.equals( right );
        }
    }

   /**
    * Return the hashcode value for this instance.
    * @return the hash value
    */
    public int hashCode()
    {
        int hashCode = getId();
        hashCode = ( hashCode * 37 ) + getPreferredName().hashCode();
        if( getDescription() != null )
        {
            hashCode = ( hashCode * 37 ) + getDescription().hashCode();
        }
        hashCode = ( hashCode * 37 ) + getPrefixes().hashCode();
        hashCode = ( hashCode * 37 ) + getTriggers().hashCode();
        return hashCode;
    }

   /**
    * Recursively searches for an option with the supplied trigger.
    *
    * @param trigger the trigger to search for.
    * @return the matching option or null.
    */
    public Option findOption( String trigger )
    {
        if( getTriggers().contains( trigger ) )
        {
            return this;
        } 
        else 
        {
            return null;
        }
    }

    /**
     * Indicates whether this option is required to be present.
     * @return true if the CommandLine will be invalid without this Option
     */
    public boolean isRequired() 
    {
        return m_required;
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
        // nothing to do normally
    }

   /**
    * Check prefixes.
    * @param prefixes the prefixes set
    */
    protected void checkPrefixes( final Set prefixes ) 
    {
        // nothing to do if empty prefix list
        if( prefixes.isEmpty() )
        {
            return;
        }

        // check preferred name
        checkPrefix( prefixes, getPreferredName() );

        // check triggers
        getTriggers();

        for( final Iterator i = getTriggers().iterator(); i.hasNext();)
        {
            checkPrefix( prefixes, (String) i.next() );
        }
    }

   /**
    * Check prefixes.
    * @param prefixes the prefixes set
    * @param trigger the trigger
    */
    private void checkPrefix( final Set prefixes, final String trigger )
    {
        for( final Iterator i = prefixes.iterator(); i.hasNext();) 
        {
            String prefix = (String) i.next();
            if( trigger.startsWith( prefix ) ) 
            {
                return;
            }
        }

        final ResourceHelper helper = ResourceHelper.getResourceHelper();
        final String message =
          helper.getMessage( 
            ResourceConstants.OPTION_TRIGGER_NEEDS_PREFIX, 
            trigger, 
            prefixes.toString() );
        throw new IllegalArgumentException( message );
    }
}
