/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.station.server;

import java.net.URL;
import java.util.Map;
import java.util.Hashtable;
import java.util.EventObject;
import java.util.EventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;

import net.dpml.station.info.ApplicationDescriptor;
import net.dpml.station.info.RegistryDescriptor;
import net.dpml.station.info.RegistryDescriptor.Entry;
import net.dpml.station.info.ApplicationRegistryRuntimeException;
import net.dpml.station.ApplicationRegistry;
import net.dpml.station.RegistryEvent;
import net.dpml.station.RegistryListener;

import net.dpml.transit.Logger;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;

/**
 * Implements of the application registry within which a set of application profiles 
 * are maintained.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RemoteApplicationRegistry extends DefaultModel implements ApplicationRegistry
{
    private final URL m_url;
    private final Map m_map = new Hashtable();
    
   /**
    * Creation of a new application registry model.
    * @param logger the assigned logging channel
    * @param url storage location
    * @exception IOException if a I/O error occurs
    */
    public RemoteApplicationRegistry( Logger logger, URL url ) throws IOException
    {
        super( logger );
        
        try
        {
            RegistryDescriptor registry = loadRegistryDescriptor( url );
            RegistryDescriptor.Entry[] entries = registry.getEntries();
            for( int i=0; i<entries.length; i++ )
            {
                RegistryDescriptor.Entry entry = entries[i];
                if( null == entry )
                {
                    final String error = 
                      "Internal error causes by a null registry array entry.  Probable "
                      + "cause is an incompatible data source.";
                    throw new ApplicationRegistryRuntimeException( error );
                }
                String key = entry.getKey();
                ApplicationDescriptor descriptor = entry.getApplicationDescriptor();
                m_map.put( key, descriptor );
            }
            m_url = url;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected exception raised while loading: " + url;
            throw new ApplicationRegistryRuntimeException( error, e );
        } 
    }

   /**
    * Return the array of application keys.
    * @return the application key array
    */
    public String[] getKeys()
    {
        synchronized( getLock() )
        {
            return (String[]) m_map.keySet().toArray( new String[0] );
        }
    }
    
   /**
    * Return the array of application descriptors.
    * @return the application descriptor array
    */
    Entry[] getEntries()
    {
        synchronized( getLock() )
        {
            String[] keys = getKeys();
            Entry[] entries = new Entry[ keys.length ];
            for( int i=0; i<entries.length; i++ )
            {
                String key = keys[i];
                ApplicationDescriptor descriptor = 
                  (ApplicationDescriptor) m_map.get( key );
                Entry entry = new Entry( key, descriptor );
                entries[i] = entry;
            }
            return entries;
        }
    }
    
   /**
    * Return the number of application descriptors in the registry.
    * @return the application descriptor count
    */
    public int getApplicationDescriptorCount()
    {
        return m_map.size();
    }

   /**
    * Add an application descriptor to the registry.
    * @param key the application key
    * @param descriptor the application descriptor
    * @exception DuplicateKeyException if the key is already assigned
    */
    public void addApplicationDescriptor( String key, ApplicationDescriptor descriptor ) 
      throws DuplicateKeyException
    {
        synchronized( getLock() )
        {
            if( m_map.containsKey( key ) )
            {
                throw new DuplicateKeyException( key );
            }
            m_map.put( key, descriptor );
            ApplicationDescriptorAddedEvent event = 
              new ApplicationDescriptorAddedEvent( this, descriptor );
            enqueueEvent( event );
        }
    }

   /**
    * Remove an application descriptor from the registry.
    * @param key the application key
    * @exception UnknownKeyException if the key is not recognized
    */
    public void removeApplicationDescriptor( String key ) 
      throws UnknownKeyException
    {
        synchronized( getLock() )
        {
            if( !m_map.containsKey( key ) )
            {
                throw new UnknownKeyException( key );
            }
            ApplicationDescriptor descriptor = (ApplicationDescriptor) m_map.get( key );
            ApplicationDescriptorRemovedEvent event = 
              new ApplicationDescriptorRemovedEvent( this, descriptor );
            m_map.remove( key );
            enqueueEvent( event );
        }
    }

   /**
    * Replace an application descriptor within the registry with a supplied descriptor.
    * @param key the application key
    * @param descriptor the updated application descriptor
    * @exception UnknownKeyException if the key is not recognized
    */
    public void updateApplicationDescriptor( String key, ApplicationDescriptor descriptor ) 
      throws UnknownKeyException
    {
        synchronized( getLock() )
        {
            removeApplicationDescriptor( key );
            try
            {
                addApplicationDescriptor( key, descriptor );
            }
            catch( DuplicateKeyException e )
            {
                e.printStackTrace();
            }
        }
    }

   /**
    * Return an array of all profiles in the registry.
    * @return the application profiles
    */
    public ApplicationDescriptor[] getApplicationDescriptors()
    {
        synchronized( getLock() )
        {
            return (ApplicationDescriptor[]) m_map.values().toArray( new ApplicationDescriptor[0] );
        }
    }
    
   /**
    * Retrieve an application profile.
    * @param key the application profile key
    * @return the application profile
    * @exception UnknownKeyException if the key is unknown
    */
    public ApplicationDescriptor getApplicationDescriptor( String key ) 
      throws UnknownKeyException
    {
        synchronized( getLock() )
        {
            if( !m_map.containsKey( key ) )
            {
                throw new UnknownKeyException( key );
            }
            return (ApplicationDescriptor) m_map.get( key );
        }
    }

   /**
    * Flush the state of the server to external storage.
    * @exception IOException if an I/O error occurs
    */
    public void flush() throws IOException
    {
        synchronized( getLock() )
        {
            //
            // TODO: we are writing out the encoded stream directly into 
            // output file and if an error occurs its too late - we have 
            // already corrupted the file - need to update this so we flush to 
            // a temp file then copy the temp file to the destination
            //
            
            if( null == m_url )
            {
                return;
            }
            Entry[] entries = getEntries();
            RegistryDescriptor descriptor = new RegistryDescriptor( entries );
            OutputStream output = m_url.openConnection().getOutputStream();
            BufferedOutputStream buffer = new BufferedOutputStream( output );
            XMLEncoder encoder = new XMLEncoder( buffer );
            encoder.setExceptionListener( new RegistryEncoderListener() );
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            try
            {
                ClassLoader context = RegistryDescriptor.class.getClassLoader();
                Thread.currentThread().setContextClassLoader( context );
                encoder.writeObject( descriptor );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( current );
                encoder.close();
            }
        }
    }
    
   /**
    * Encoding exception listener.
    */
    private final class RegistryEncoderListener implements ExceptionListener
    {
       /**
        * Catch an encoding exception.
        * @param e the encoding exception
        */
        public void exceptionThrown( Exception e )
        {
            Throwable cause = e.getCause();
            if( null != cause )
            {
                if( cause instanceof EncodingRuntimeException )
                {
                    EncodingRuntimeException ere = (EncodingRuntimeException) cause;
                    throw ere;
                }
                else
                {
                    final String error = 
                      "An error occured while attempting to encode registry."
                      + "\nTarget URL: " + m_url
                      + "]\nCause: " + cause.toString();
                    throw new EncodingRuntimeException( error, cause );
                }
            }
            else
            {
                final String error = 
                  "An unexpected error occured while attempting to encode registry ["
                  + "\nTarget URL: " + m_url
                  + "\nCause: " + e.toString();
                throw new EncodingRuntimeException( error, e );
            }
        }
    }
    
   /**
    * EncodingRuntimeException.
    */
    private static class EncodingRuntimeException extends RuntimeException
    {
       /**
        * Creation of a new <tt>EncodingRuntimeException</tt>.
        * @param message the exception message
        * @param cause the causal exception 
        */
        public EncodingRuntimeException( String message, Throwable cause )
        {
            super( message, cause );
        }
    }
    
   /**
    * Add a depot content change listener.
    * @param listener the registry change listener to add
    */
    public void addRegistryListener( RegistryListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Add a registry change listener.
    * @param listener the registry change listener to add
    */
    public void removeRegistryListener( RegistryListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Proces a registry event.
    * @param event the event top process
    */
    protected void processEvent( EventObject event )
    {
        if( event instanceof RegistryEvent )
        {
            processRegistryEvent( (RegistryEvent) event );
        }
        else
        {
            final String error = 
              "Event class not recognized: " 
              + event.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Return a string representation of the registy model.
    * @return the string value
    */
    public String toString()
    {
        return "[registry]";
    }

    private RegistryDescriptor loadRegistryDescriptor( URL url ) throws IOException
    {
        if( null == url )
        {
            return new RegistryDescriptor( new Entry[0] );
        }
        
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try
        {
            InputStream input = url.openStream();
            ClassLoader context = RegistryDescriptor.class.getClassLoader();
            Thread.currentThread().setContextClassLoader( context );
            XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
            return (RegistryDescriptor) decoder.readObject();
        }
        catch( FileNotFoundException e )
        {
            return new RegistryDescriptor( new Entry[0] );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( loader );
        }
    }
    
    private void processRegistryEvent( RegistryEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof RegistryListener )
            {
                RegistryListener rl = (RegistryListener) listener;
                if( event instanceof ApplicationDescriptorAddedEvent )
                {
                    try
                    {
                        rl.profileAdded( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "RegistryListener profile addition notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof ApplicationDescriptorRemovedEvent )
                {
                    try
                    {
                        rl.profileRemoved( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "RegistryListener profile removed notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

   /**
    * ApplicationDescriptorAddedEvent.
    */
    static class ApplicationDescriptorAddedEvent extends RegistryEvent
    {
       /**
        * Creation of a new ProfileAddedEvent.
        * @param source the source registry
        * @param profile the profile that was added
        */
        public ApplicationDescriptorAddedEvent( 
          ApplicationRegistry source, ApplicationDescriptor descriptor )
        {
            super( source, descriptor );
        }
    }

   /**
    * ApplicationDescriptorRemovedEvent.
    */
    static class ApplicationDescriptorRemovedEvent extends RegistryEvent
    {
       /**
        * Creation of a new ProfileRemovedEvent.
        * @param source the source registry
        * @param profile the profile that was removed
        */
        public ApplicationDescriptorRemovedEvent( ApplicationRegistry source, ApplicationDescriptor descriptor )
        {
            super( source, descriptor );
        }
    }
}
