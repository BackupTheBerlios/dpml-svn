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

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

import net.dpml.cli.DisplaySetting;
import net.dpml.cli.Option;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

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

    public String toString() 
    {
        final StringBuffer buffer = new StringBuffer();
        appendUsage( buffer, DisplaySetting.ALL, null );
        return buffer.toString();
    }

    public int getId() 
    {
        return m_id;
    }

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

    public boolean isRequired() 
    {
        return m_required;
    }

    public void defaults( final WriteableCommandLine commandLine ) 
    {
        // nothing to do normally
    }

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

        for( final Iterator i = getTriggers().iterator(); i.hasNext(); )
        {
            checkPrefix( prefixes, (String) i.next() );
        }
    }

    private void checkPrefix( final Set prefixes, final String trigger )
    {
        for( final Iterator i = prefixes.iterator(); i.hasNext(); ) 
        {
            String prefix = (String) i.next();
            if (trigger.startsWith(prefix)) 
            {
                return;
            }
        }

        final ResourceHelper helper = ResourceHelper.getResourceHelper();
        final String message =
          helper.getMessage( 
            ResourceConstants.OPTION_TRIGGER_NEEDS_PREFIX, trigger, prefixes.toString() );
        throw new IllegalArgumentException( message );
    }
}
