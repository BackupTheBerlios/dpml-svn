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
import java.util.Properties;

import net.dpml.transit.store.ContentStorage;
import net.dpml.transit.store.Removable;

/**
 * Default implementation of a content model that maintains an active 
 * configuration of a pluggable content handler.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultContentModel extends DisposableCodeBaseModel implements ContentModel
{
    //----------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------

    private final ContentStorage m_home;
    private final String m_type;
    private final Properties m_properties;

    private String m_title;

    //----------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------

   /**
    * Construction of a new content model.
    * @param logger the assigned logging channel
    * @param uri the codebase uri
    * @param type the content type key
    * @param title the title of the content type
    * @param properties properties to assign to the content model
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultContentModel( 
       Logger logger, URI uri, String type, String title, Properties properties ) throws RemoteException
    {
        super( logger, uri );

        m_home = null;
        m_type = type;
        m_title = title;
        m_properties = properties;
    }

   /**
    * Construction of a new content model using a supplied storage unit.
    * @param logger the assigned logging channel
    * @param home the content model persistent storage home
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultContentModel( Logger logger, ContentStorage home ) throws RemoteException
    {
        super( logger, home );

        m_home = home;
        m_type = home.getType();
        m_title = home.getTitle();
        m_properties = home.getProperties();
    }

    //----------------------------------------------------------------------
    // ContentModel
    //----------------------------------------------------------------------

   /**
    * Return the immutable content type identifier.
    * @return the content type
    * @exception RemoteException if a remote exception occurs
    */
    public String getContentType() throws RemoteException
    {
        return m_type;
    }

   /**
    * Returns the human readable name of the content model.
    * @return the content type human readable name
    * @exception RemoteException if a remote exception occurs
    */
    public String getTitle() throws RemoteException
    {
        return m_title;
    }

   /**
    * Set the content model title to the supplied value.
    * @param title the content type title
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

            ContentEvent event = new ContentEvent( this );
            super.enqueueEvent( event );
        }
    }

   /**
    * Reutn the value of a property associated with the content model.
    * @param key the property key
    * @return the property value (possibly null)
    */
    public String getProperty( String key )
    {
        return m_properties.getProperty( key );
    }
 
   /**
    * Return the value of a property associated with the content model.
    * @param key the property key
    * @param value the value to return if the property is unknown
    * @return the resolved value
    */
    public String getProperty( String key, String value )
    {
        return m_properties.getProperty( key, value );
    }

   /**
    * Set a property on the content model.
    * @param key the property key
    * @param value the property value
    */
    public void setProperty( String key, String value )
    {
        synchronized( getLock() )
        {
            Object result = m_properties.setProperty( key, value );
            if( null != m_home )
            {
                m_home.setProperty( key, value );
            }
            PropertyChangeEvent event = new PropertyChangeEvent( this, key, value );
            super.enqueueEvent( event );
        }
    }

   /**
    * Remove a property from the content model.
    * @param key the property key
    */
    public void removeProperty( String key )
    {
        synchronized( getLock() )
        {
            Object result = m_properties.remove( key );
            if( null != m_home )
            {
                m_home.removeProperty( key );
            }
            PropertyChangeEvent event = new PropertyChangeEvent( this, key, null );
            super.enqueueEvent( event );
        }
    }

   /**
    * Add a content listener to the director.
    * @param listener the listener to add
    * @exception RemoteException if a remote exception occurs
    */
    public void addContentListener( ContentListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a content listener from the director.
    * @param listener the listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    public void removeContentListener( ContentListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

   /**
    * Dispose of the content model.
    * @exception RemoteException if a remote exception occurs
    */
    public void dispose() throws RemoteException
    {
        super.dispose();
        if( null != m_home && ( m_home instanceof Removable ) )
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
        if( event instanceof PropertyChangeEvent )
        {
            processPropertyChangeEvent( (PropertyChangeEvent) event );
        }
        else if( event instanceof ContentEvent )
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

    private void processPropertyChangeEvent( PropertyChangeEvent event )
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
                    listener.propertyChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ContentListener property change notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }
}

