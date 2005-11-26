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

import net.dpml.transit.info.ContentDirective;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.ContentListener;
import net.dpml.transit.model.ContentEvent;

/**
 * Default implementation of a content model that maintains an active 
 * configuration of a pluggable content handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultContentModel extends DefaultCodeBaseModel implements ContentModel
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
    * Construction of a new content model using a supplied storage unit.
    * @param logger the assigned logging channel
    * @param home the content model persistent storage home
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultContentModel( Logger logger, ContentDirective directive ) throws RemoteException
    {
        super( logger, directive );

        m_id = directive.getID();
        m_title = directive.getTitle();
    }

    //----------------------------------------------------------------------
    // ContentModel
    //----------------------------------------------------------------------

   /**
    * Return the immutable resolver identifier.
    * @return the resolver identifier
    */
    public String getID()
    {
        return m_id;
    }

   /**
    * Returns the human readable name of the content model.
    * @return the content type human readable name
    */
    public String getTitle()
    {
        return m_title;
    }

   /**
    * Add a content listener to the director.
    * @param listener the listener to add
    */
    public void addContentListener( ContentListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a content listener from the director.
    * @param listener the listener to remove
    */
    public void removeContentListener( ContentListener listener )
    {
        super.removeListener( listener );
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

   /**
    * Dispose of the content model.
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
        if( event instanceof ContentEvent )
        {
            processContentEvent( (ContentEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processContentEvent( ContentEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof ContentListener )
            {
                ContentListener listener = (ContentListener) eventListener;
                try
                {
                    listener.titleChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ContentListener title change notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }
}

