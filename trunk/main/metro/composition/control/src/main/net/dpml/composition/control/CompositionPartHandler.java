/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.composition.control;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.WeakHashMap;

import net.dpml.part.Part;
import net.dpml.part.PartHolder;
import net.dpml.part.PartHandlerRuntimeException;
import net.dpml.part.PartNotFoundException;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.DelegationException;
import net.dpml.part.PartEditor;

import net.dpml.component.control.Controller;
import net.dpml.component.control.ControllerContext;
import net.dpml.component.control.ControllerException;

import net.dpml.composition.info.Type;

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;
import net.dpml.transit.Repository;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.Logger;

/**
 * Composition part handler.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public abstract class CompositionPartHandler extends UnicastRemoteObject implements Controller
{
   /**
    * Map containing the foreign part handlers.
    */
    private final WeakHashMap m_handlers = new WeakHashMap();

   /**
    * Transit repository handler used for resolving foreign handlers.
    */
    private final Repository m_loader;

    private final ControllerContext m_context;

    public CompositionPartHandler( ControllerContext context ) 
       throws ControllerException, RemoteException
    {
        super();

        m_context = context;
        m_loader = Transit.getInstance().getRepository();
    }

   /**
    * Returns the uri of this handler.
    * @return the handler uri
    */
    public URI getURI()
    {
        return PART_HANDLER_URI;
    }

   /**
    * Load a part from serialized form.  The uri is assumed to be a uri that 
    * can be transformed to a URL from which an input stream to a PartHolder 
    * can be established.  If the uri references a foreign part handler the 
    * implementation will attempt to locate and delegate part loading requests 
    * to the foreign handler.
    *
    * @param uri the part uri
    * @return the part estracted from the part referenced by the uri
    */
    public Part loadPart( URI uri )
        throws PartNotFoundException, IOException, DelegationException
    {
        return loadSerializedPart( uri );
    }

   /**
    * Load a part from serialized form. 
    *
    * @param url the part url
    * @return the part estracted from the part referenced by the url
    */
    public Part loadPart( URL url )
        throws PartNotFoundException, IOException, DelegationException
    {
        return loadSerializedPart( url );
    }

    public Part loadPart( byte[] bytes ) throws IOException
    {
        try
        {
            ByteArrayInputStream input = new ByteArrayInputStream( bytes );
            ObjectInputStream stream = new ObjectInputStream( input );
            return (Part) stream.readObject();
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
             "Unexpected error while attempting to load a part from a byte array.";
            throw new PartHandlerRuntimeException( error, e );
        }
    }

   /**
    * Load a part handler given a handler uri.
    * @return the part handler
    */
    public Controller getPrimaryController( URI uri ) 
       throws PartHandlerNotFoundException, DelegationException
    {
        if( false == getURI().equals( uri ) )
        {
            return (Controller) resolvePartHandler( uri );
        }
        else
        {
            return (Controller) this;
        }
    }

    public Object getContent( URLConnection connection, Class[] classes ) throws IOException
    {
        URL url = connection.getURL();
        try
        {
            if( classes.length == 0 )
            {
                return loadPart( url );
            }
            else
            {
                for( int i=0; i<classes.length; i++ )
                {
                    Class c = classes[i];
                    if( Part.class.isAssignableFrom( c ) )
                    {
                        return loadPart( url );
                    }
                    else if( URI.class.isAssignableFrom( c ) )
                    {
                        Part part = loadPart( url );
                        return part.getPartHandlerURI();
                    }
                }
            }
            return null;
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error whuile attempting to establish content for: " + url;
            IOException cause = new IOException( error );
            cause.initCause( e );
            throw cause;
        }
    }

    private Part loadSerializedPart( URI uri )
        throws IOException, DelegationException, PartNotFoundException
    {
        URL url = Artifact.toURL( uri );
        return loadSerializedPart( url );
    }

    private Part loadSerializedPart( URL url )
        throws IOException, DelegationException, PartNotFoundException
    {
        InputStream input = url.openStream();
        if( null == input )
        {
            throw new PartNotFoundException( getURIFromURL( url ) );
        }
        ObjectInputStream stream = new ObjectInputStream( input );
        try
        {
            PartHolder holder = (PartHolder) stream.readObject();
            URI foreign = holder.getPartHandlerURI();
            if( false == getURI().equals( foreign ) )
            {
                try
                {
                    Controller handler = resolvePartHandler( foreign );
                    return handler.loadPart( holder.getByteArray() );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Delegate raised an error while attempting to load a serialized part ["
                      + url
                      + "] using the external handler ["
                      + foreign 
                      + "].";
                    throw new DelegationException( foreign, error, e );
                }
            }
            else
            {
                return loadPart( holder.getByteArray() );
            }
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( DelegationException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
             "Error loading part ["
              + url
              + "].";
            throw new PartHandlerRuntimeException( error, e );
        }
    }

    protected Controller resolvePartHandler( URI uri ) throws PartHandlerNotFoundException
    {
        if( getURI().equals( uri ) )
        {
            return this;
        }

        synchronized( m_handlers )
        {
            Controller[] handlers = (Controller[]) m_handlers.keySet().toArray( new Controller[0] );
            for( int i=0; i<handlers.length; i++ )
            {
                Controller handler = handlers[i];
                try
                {
                    if( handler.getURI().equals( uri ) )
                    {
                        return handler;
                    }
                }
                catch( RemoteException e )
                {
                    final String error = 
                      "Unexpected remote exception while resolving remote controller.";
                    throw new RuntimeException( error, e );
                }
            }
            Controller handler = loadHandler( uri );
            m_handlers.put( handler, null );
            return handler;
        }
    }

    private Controller loadHandler( URI uri ) throws PartHandlerNotFoundException
    {
        ClassLoader classloader = Part.class.getClassLoader();
        Logger logger = m_context.getLogger();
        try
        {
            return (Controller) m_loader.getPlugin( classloader, uri, new Object[]{logger, m_context} );
        }
        catch( IOException e )
        {
            throw new PartHandlerNotFoundException( uri, e );
        }
    }

    private static final URI PART_HANDLER_URI = createStaticURI( "@PART-HANDLER-URI@" );

    private static URI createStaticURI( String path )
    {
        try
        {
            return new URI( path );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    private URI getURIFromURL( URL url )
    {
        try
        {
            return new URI( url.toExternalForm() );
        }
        catch( Throwable e )
        {
            throw new RuntimeException( e );
        }
    }
}
