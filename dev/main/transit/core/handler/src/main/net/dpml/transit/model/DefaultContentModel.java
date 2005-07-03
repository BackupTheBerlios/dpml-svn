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
import java.util.Date;
import java.util.Properties;

import net.dpml.transit.store.ContentStorage;
import net.dpml.transit.store.Removable;

/**
 * Default implementation of a content manager that maintains an active 
 * configuration model of a pluggable content handler.
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

    public DefaultContentModel( 
       Logger logger, URI uri, String type, String title, Properties properties ) throws RemoteException
    {
        super( logger, uri );

        m_home = null;

        m_type = type;
        m_title = title;
        m_properties = properties;
    }

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
    */
    public String getContentType() throws RemoteException
    {
        return m_type;
    }

   /**
    * Returns the human readable name of the content type handler.
    * @return the content type human readable name
    */
    public String getTitle() throws RemoteException
    {
        return m_title;
    }

    public void setTitle( String title ) throws RemoteException
    {
        synchronized( m_lock )
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

    public String getProperty( String key )
    {
        return m_properties.getProperty( key );
    }
 
    public String getProperty( String key, String value )
    {
        return m_properties.getProperty( key, value );
    }

    public void setProperty( String key, String value )
    {
        synchronized( m_lock )
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

    public void removeProperty( String key )
    {
        synchronized( m_lock )
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
    */
    public void addContentListener( ContentListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a content listener from the director.
    * @param listener the listener to remove
    */
    public void removeContentListener( ContentListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

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

    public void processContentEvent( ContentEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
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

    public void processPropertyChangeEvent( PropertyChangeEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
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

