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

package net.dpml.transit.model;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.Logger;
import net.dpml.transit.store.CodeBaseStorage;

/**
 * The abstract codebase is an implementation that monitors configuration changes 
 * to a a codebase storage unit containg a uri attribute.  Modifications to the uri value 
 * will trigger a CodeBaseEvent which can be monitored by controllers dealing with  
 * pluggable system maintenance.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public abstract class DefaultCodeBaseModel extends DefaultModel implements CodeBaseModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final String m_id;
    private final CodeBaseStorage m_home;

    private Value[] m_parameters;
    private URI m_uri;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Construction of a new codebase model.
    * @param logger the assigned logging channel
    * @param id the model id
    * @param uri the codebase uri
    * @param values the codebase parameters
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultCodeBaseModel( Logger logger, String id, URI uri, Value[] values )
      throws RemoteException
    {
        super( logger );

        m_id = id;
        m_uri = uri;

        if( null == values )
        {
            m_parameters = new Value[0];
        }
        else
        {
            m_parameters = values;
        }
        m_home = null;
    }

   /**
    * Construction of a new codebase model using a supplied codebase storage unit.
    * @param logger the assigned logging channel
    * @param home the codebase storage unit
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultCodeBaseModel( Logger logger, CodeBaseStorage home )
      throws RemoteException
    {
        super( logger );
        if( null == home )
        {
            throw new NullPointerException( "home" );
        }
        m_home = home;
        m_id = home.getID();
        m_uri = home.getCodeBaseURI();
        m_parameters = home.getParameters();
    }

    // ------------------------------------------------------------------------
    // CodeBaseModel
    // ------------------------------------------------------------------------

   /**
    * Return the immutable resolver identifier.
    * @return the resolver identifier
    */
    public String getID()
    {
        return m_id;
    }

   /**
    * Set the codebase uri value.
    * @param uri the codebase uri
    */
    public void setCodeBaseURI( URI uri )
    {
        setCodeBaseURI( uri, true );
    }

   /**
    * Set the codebase uri value.
    * @param uri the codebase uri
    * @param notify if TRUE raise a notification event
    */
    protected void setCodeBaseURI( URI uri, boolean notify )
    {
        synchronized( getLock() )
        {
            m_uri = uri;
            if( null != m_home )
            {
                m_home.setCodeBaseURI( uri );
            }
            if( notify )
            {
                CodeBaseEvent e = new LocationEvent( this, m_uri );
                super.enqueueEvent( e );
            }
        }
    }

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

   /**
    * Set the array of parameters assigned to the codebase model.
    * @param parameters the parameters array
    */
    public void setParameters( Value[] parameters )
    {
        synchronized( getLock() )
        {
            m_parameters = parameters;
            if( null != m_home )
            {
                m_home.setParameters( parameters );
            }
            ParametersEvent e = new ParametersEvent( this, parameters );
            super.enqueueEvent( e );
        }
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

   /**
    * Return the codebase storage object.
    * @return the codebase storage
    */
    protected CodeBaseStorage getCodeBaseStorage()
    {
        return m_home;
    }

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

