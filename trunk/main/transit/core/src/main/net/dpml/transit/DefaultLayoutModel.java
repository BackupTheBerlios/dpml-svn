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

import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.info.LayoutDirective;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.model.LayoutListener;
import net.dpml.transit.model.LayoutEvent;

import net.dpml.util.EventQueue;
import net.dpml.util.Logger;

/**
 * The DefaultLayoutModel is a model supplied to a layout strategy handler. It 
 * provides two mdes of construction - one dealing with local layout handlers
 * (the classic layout and the ecli8pse layout) and the second dealing with
 * plugin layout strategies.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultLayoutModel extends DefaultCodeBaseModel implements LayoutModel, Disposable
{
    //----------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------

    private final String m_id;
    private final String m_title;

    //----------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------

   /**
    * Creation of a new layout model using a supplied layout configuration.
    * @param logger the assigned logging channel
    * @param directive the layout configuration
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultLayoutModel( final EventQueue queue, final Logger logger, final LayoutDirective directive )
      throws RemoteException
    {
        super( queue, logger, directive );

        m_id = directive.getID();
        m_title = directive.getTitle();
    }

    //----------------------------------------------------------------------
    // LayoutModel
    //----------------------------------------------------------------------

   /**
    * Returns the human readable name of the resolver.
    * @return the resolver human readable name
    */
    public String getID()
    {
        return m_id;
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
    * Returns the layout classname.
    * @return the classname (always returns null)
    */
    public String getClassname()
    {
        return null;
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
    // Disposable
    //----------------------------------------------------------------------

   /**
    * Dispose of the layout model.
    */
    public void dispose()
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
    public void processEvent( EventObject event )
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
        EventListener[] listeners = super.getEventListeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof LayoutListener )
            {
                LayoutListener listener = (LayoutListener) eventListener;
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

