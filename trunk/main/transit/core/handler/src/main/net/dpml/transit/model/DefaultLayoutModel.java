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

import net.dpml.transit.store.LayoutStorage;
import net.dpml.transit.store.Removable;
import net.dpml.transit.store.LocalStrategy;
import net.dpml.transit.store.PluginStrategy;
import net.dpml.transit.store.Strategy;

/**
 * The DefaultLayoutModel is a model supplied to a layout strategy handler. It 
 * provides two mdes of construction - one dealing with local layout handlers
 * (the classic layout and the ecli8pse layout) and the second dealing with
 * plugin layout strategies.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: HostManager.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
class DefaultLayoutModel extends DisposableCodeBaseModel implements LayoutModel
{
    //----------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------

    private final String m_id;
    private final String m_classname;
    private final boolean m_bootstrap;
    private final LayoutStorage m_home;

    private String m_title;

    //----------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------

   /**
    * Creation of a new layout model.
    * @param logger the assigned logging channel
    * @param id the layout identifier
    * @param strategy the layout yhandler creation strategy
    * @param title the layout title
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultLayoutModel( 
      final Logger logger, final String id, Strategy strategy, final String title )
      throws RemoteException
    {
        super( logger, (URI) null );

        m_home = null;

        m_id = id;
        m_title = title;
        if( strategy instanceof PluginStrategy )
        {
            PluginStrategy plugin = (PluginStrategy) strategy;
            setCodeBaseURI( plugin.getURI() );
            m_classname = null;
            m_bootstrap = false;
        }
        else
        {
            LocalStrategy local = (LocalStrategy) strategy;
            m_classname = local.getClassname();
            m_bootstrap = local.isBootstrap();
        }
    }

   /**
    * Creation of a new layout model using a supplied layout store.
    * @param logger the assigned logging channel
    * @param home the layout storage home
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultLayoutModel( final Logger logger, final LayoutStorage home )
      throws RemoteException
    {
        super( logger, home );

        m_home = home;

        m_id = home.getID();
        m_title = home.getTitle();
        Strategy strategy = home.getStrategy();
        if( strategy instanceof PluginStrategy )
        {
            PluginStrategy plugin = (PluginStrategy) strategy;
            setCodeBaseURI( plugin.getURI() );
            m_classname = null;
            m_bootstrap = false;
        }
        else
        {
            LocalStrategy local = (LocalStrategy) strategy;
            m_classname = local.getClassname();
            m_bootstrap = local.isBootstrap();
        }
    }

    //----------------------------------------------------------------------
    // LayoutModel
    //----------------------------------------------------------------------

   /**
    * Set the title of the layout model.
    * @param title the new title
    * @exception RemoteException if a remote exception occurs
    */
    public void setTitle( String title ) throws RemoteException
    {
        synchronized( getLock() )
        {
            m_title = title;

            if( null != m_home )
            {
                m_home.setTitle( title );
            }

            LayoutEvent event = new LayoutEvent( this );
            super.enqueueEvent( event );
        }
    }

   /**
    * Return the immutable resolver identifier.
    * @return the resolver identifier
    * @exception RemoteException if a remote exception occurs
    */
    public String getID() throws RemoteException
    {
        return m_id;
    }

   /**
    * Return true if this is a bootstrap resolver.
    *
    * @return the bootstrap status of the resolver.
    * @exception RemoteException if a remote exception occurs
    */
    public boolean isBootstrap() throws RemoteException
    {
        return m_bootstrap;
    }

   /**
    * Return a possibly null classname.  If the classname is not null the 
    * manager represents a bootstrap resolver.
    *
    * @return the resolver classname
    * @exception RemoteException if a remote exception occurs
    */
    public String getClassname() throws RemoteException
    {
        return m_classname;
    }

   /**
    * Returns the human readable name of the resolver.
    * @return the resolver human readable name
    * @exception RemoteException if a remote exception occurs
    */
    public String getTitle() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_title;
        }
    }

   /**
    * Add a layout listener to the model.
    * @param listener the listener to add
    * @exception RemoteException if a remote exception occurs
    */
    public void addLayoutListener( LayoutListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a layout listener from the director.
    * @param listener the listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    public void removeLayoutListener( LayoutListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

   /**
    * Dispose of the layout model.
    * @exception RemoteException if a remote exception occurs
    */
    public void dispose() throws RemoteException
    {
        super.dispose();
        if( !isBootstrap() && ( null != m_home ) && ( m_home instanceof Removable ) )
        {
            Removable store = (Removable) m_home;
            store.remove();
        }
    }

    // ------------------------------------------------------------------------
    // internal
    // ------------------------------------------------------------------------

   /**
    * Internal event handler.
    * @param event the event to handle
    */
    protected void processEvent( EventObject event )
    {
        if( event instanceof LayoutEvent )
        {
            processLayoutEvent( (LayoutEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processLayoutEvent( LayoutEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof LayoutListener )
            {
                LayoutListener listener = (LayoutListener ) eventListener;
                try
                {
                    listener.titleChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "LayoutListener title change notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }
}

