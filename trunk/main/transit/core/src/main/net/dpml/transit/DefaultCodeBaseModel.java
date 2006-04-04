/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.lang.Value;
import net.dpml.lang.ValueDirective;
import net.dpml.lang.Construct;
import net.dpml.lang.Logger;

import net.dpml.transit.info.CodeBaseDirective;
import net.dpml.transit.model.CodeBaseModel;
import net.dpml.transit.model.CodeBaseListener;
import net.dpml.transit.model.CodeBaseEvent;
import net.dpml.transit.model.LocationEvent;
import net.dpml.transit.model.ParametersEvent;

/**
 * The abstract codebase is an implementation that monitors configuration changes 
 * to a a codebase storage unit containg a uri attribute.  Modifications to the uri value 
 * will trigger a CodeBaseEvent which can be monitored by controllers dealing with  
 * pluggable system maintenance.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class DefaultCodeBaseModel extends DefaultModel implements CodeBaseModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private Value[] m_parameters;
    private URI m_uri;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Construction of a new codebase model using a supplied codebase storage unit.
    * @param logger the assigned logging channel
    * @param directive the codebase storage directive
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultCodeBaseModel( Logger logger, CodeBaseDirective directive )
      throws RemoteException
    {
        super( logger );
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        m_uri = directive.getCodeBaseURI();
        ValueDirective[] values = directive.getValueDirectives();
        m_parameters = new Value[ values.length ];
        for( int i=0; i<values.length; i++ )
        {
            ValueDirective value = values[i];
            m_parameters[i] = new Construct( value );
        }
    }

    // ------------------------------------------------------------------------
    // CodeBaseModel
    // ------------------------------------------------------------------------

   /**
    * Return the codebase uri.
    * @return the codebase uri
    */
    public URI getCodeBaseURI()
    {
        synchronized( getLock() )
        {
            return m_uri;
        }
    }

   /**
    * Add a codebase listener to the model.
    * @param listener the listener to add
    */
    public void addCodeBaseListener( CodeBaseListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a codebase listener from the model.
    * @param listener the listener to remove
    */
    public void removeCodeBaseListener( CodeBaseListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Return the array of codebase parameters.
    *
    * @return the parameters array
    */
    public Value[] getParameters()
    {
        synchronized( getLock() )
        {
            return m_parameters;
        }
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

   /**
    * Internal event handler.
    * @param eventObject the event
    */
    protected void processEvent( EventObject eventObject )
    {
        if( eventObject instanceof CodeBaseEvent )
        {
            CodeBaseEvent event = (CodeBaseEvent) eventObject;
            processCodeBaseEvent( event );
        }
        else
        {
            final String error = 
              "Event class not recognized: " + eventObject.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

    private void processCodeBaseEvent( CodeBaseEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof CodeBaseListener )
            {
                CodeBaseListener pl = (CodeBaseListener) listener;
                if( event instanceof LocationEvent )
                {
                    try
                    {
                        pl.codeBaseChanged( (LocationEvent) event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "CodeBaseListener notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof ParametersEvent )
                {
                    try
                    {
                        pl.parametersChanged( (ParametersEvent) event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "CodeBaseListener notification error.";
                        getLogger().error( error, e );
                    }
                }
                else
                {
                    final String error = 
                      "Event class not recognized: " + event.getClass().getName();
                    throw new IllegalArgumentException( error );
                }
            }
        }
    }
}

