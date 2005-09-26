/*
 * Copyright 2004 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.composition.control;

import java.rmi.RemoteException;
import java.util.EventObject;

import net.dpml.composition.event.EventProducer;
import net.dpml.composition.data.ValueDirective;

import net.dpml.part.Context;
import net.dpml.part.ContextException;
import net.dpml.part.EntryDescriptor;

import net.dpml.transit.model.UnknownKeyException;

/**
 * Default logging adapter.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: LoggingAdapter.java 2684 2005-06-01 00:22:50Z mcconnell@dpml.net $
 */
public class ValueContext extends EventProducer implements Context
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private ValueDirective m_directive;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new console adapter that is used to redirect transit events
    * the system output stream.
    */
    public ValueContext( ValueDirective directive ) throws RemoteException
    {
        super();
        m_directive = directive;
    }

    // ------------------------------------------------------------------------
    // Context
    // ------------------------------------------------------------------------

    public EntryDescriptor[] getEntryDescriptors() throws RemoteException
    {
        return new EntryDescriptor[0];
    }

    public Object getBaseValue( String key ) throws UnknownKeyException, RemoteException
    {
        throw new UnknownKeyException( key );
    }

    public void setBaseValue( String key, Object value ) 
      throws UnknownKeyException, ContextException, RemoteException
    {
        throw new UnknownKeyException( key );
    }

    public String[] getChildKeys() throws RemoteException
    {
        return new String[0];
    }

    public Context getChild( String key ) throws UnknownKeyException, RemoteException
    {
        throw new UnknownKeyException( key );
    }

    protected void processEvent( EventObject event )
    {
    }

}

