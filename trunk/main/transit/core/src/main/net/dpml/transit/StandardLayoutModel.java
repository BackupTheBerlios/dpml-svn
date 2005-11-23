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

import net.dpml.transit.Logger;
import net.dpml.transit.store.LayoutStorage;
import net.dpml.transit.store.Removable;
import net.dpml.transit.store.LocalStrategy;
import net.dpml.transit.store.PluginStrategy;
import net.dpml.transit.store.Strategy;
import net.dpml.transit.model.*;

/**
 * The StandardLayoutModel represents a standard layout included with the 
 * core Transit system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class StandardLayoutModel extends DefaultModel implements LayoutModel
{
    //----------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------

    private final String m_id;
    private final String m_title;
    private final String m_classname;

    //----------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------

   /**
    * Creation of a new layout model using a supplied layout configuration.
    * @param logger the assigned logging channel
    * @param directive the layout configuration
    * @exception RemoteException if a remote exception occurs
    */
    public StandardLayoutModel( 
      final Logger logger, final String id, final String title, final String classname )
      throws RemoteException
    {
        super( logger );

        m_id = id;
        m_title = title;
        m_classname = classname;
    }

    //----------------------------------------------------------------------
    // LayoutModel
    //----------------------------------------------------------------------

   /**
    * Return a possibly null classname.  If the classname is not null the 
    * manager represents a bootstrap layout model.
    *
    * @return the layout classname
    * @exception RemoteException if a remote exception occurs
    */
    public String getClassname()
    {
        return m_classname;
    }

   /**
    * Returns the human readable name of the resolver.
    * @return the resolver human readable name
    */
    public String getTitle()
    {
        return m_title;
    }

   /**
    * Add a layout listener to the model.
    * @param listener the listener to add
    */
    public void addLayoutListener( LayoutListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a layout listener from the director.
    * @param listener the listener to remove
    */
    public void removeLayoutListener( LayoutListener listener )
    {
        super.removeListener( listener );
    }

    //----------------------------------------------------------------------
    // CodeBaseModel
    //----------------------------------------------------------------------
    
   /**
    * Return the immutable model identifier.
    * @return the resolver identifier
    * @exception RemoteException if a remote exception occurs
    */
    public String getID()
    {
        return m_id;
    }

   /**
    * Return the uri of the plugin to be used for the subsystem.
    * @return the codebase plugin uri
    * @exception RemoteException if a remote exception occurs
    */
    public URI getCodeBaseURI()
    {
        return null;
    }

   /**
    * Add a codebase listener to the model.
    * @param listener the listener to add
    * @exception RemoteException if a remote exception occurs
    */
    public void addCodeBaseListener( CodeBaseListener listener )
    {
    }

   /**
    * Remove a codebase listener from the model.
    * @param listener the listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    public void removeCodeBaseListener( CodeBaseListener listener )
    {
    }

   /**
    * Return the array of codebase parameter values.
    *
    * @return the parameter value array
    * @exception RemoteException if a remote exception occurs
    */
    public Value[] getParameters()
    {
        return new Value[0];
    }
    
    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

   /**
    * Dispose of the layout model.
    */
    void dispose()
    {
        super.dispose();
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

