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
import java.util.Date;
import java.util.EventObject;
import java.util.EventListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.store.CodeBaseStorage;

/**
 * An abstract plugin manager is an implementation that monitors configuration changes 
 * to a preferences node containg a uri attribute.  Modifications to the uri attribute 
 * will trigger a PluginChangeEvent which can be monitored by controllers dealing with  
 * pluggable system maintenance.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: StandardTransitDirector.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public abstract class DefaultCodeBaseModel extends DefaultModel implements CodeBaseModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final CodeBaseStorage m_home;

    private URI m_uri;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultCodeBaseModel( Logger logger, URI uri )
      throws RemoteException
    {
        super( logger );
        m_uri = uri;
        m_home = null;
    }

    public DefaultCodeBaseModel( Logger logger, CodeBaseStorage home )
      throws RemoteException
    {
        super( logger );
        if( null == home )
        {
            throw new NullPointerException( "home" );
        }
        m_home = home;
        m_uri = home.getCodeBaseURI();
    }

    // ------------------------------------------------------------------------
    // PluginModel
    // ------------------------------------------------------------------------

   /**
    * Set the plugin uri value.
    * @return the plugin uri
    */
    public void setCodeBaseURI( URI uri )
    {
        setCodeBaseURI( uri, true );
    }

   /**
    * Set the plugin uri value.
    * @return the plugin uri
    */
    protected void setCodeBaseURI( URI uri, boolean notify )
    {
        synchronized( m_lock )
        {
            m_uri = uri;
            if( null != m_home )
            {
                m_home.setCodeBaseURI( uri );
            }
            if( notify )
            {
                CodeBaseEvent e = new CodeBaseEvent( this, m_uri );
                super.enqueueEvent( e );
            }
        }
    }

   /**
    * Return the plugin uri.
    * @return the plugin uri
    */
    public URI getCodeBaseURI()
    {
        synchronized( m_lock )
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

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    protected CodeBaseStorage getCodeBaseStorage()
    {
        return m_home;
    }

    protected void processEvent( EventObject eventObject )
    {
        if( eventObject instanceof CodeBaseEvent )
        {
            CodeBaseEvent event = (CodeBaseEvent) eventObject;
            processCodeBaseEvent( event );
        }
        else
        {
            super.processEvent( eventObject );
        }
    }

    private void processCodeBaseEvent( CodeBaseEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof CodeBaseListener )
            {
                CodeBaseListener pl = (CodeBaseListener) listener;
                try
                {
                    pl.codeBaseChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "CodeBaseListener notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }
}

