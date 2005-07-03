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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties ;
import java.util.WeakHashMap ;

import net.dpml.part.part.Part;
import net.dpml.part.part.PartHolder;
import net.dpml.part.control.PartNotFoundException;
import net.dpml.part.control.Controller;
import net.dpml.part.control.ControllerContext;
import net.dpml.part.control.ControlException;
import net.dpml.part.control.PartHandler;
import net.dpml.part.control.PartHandlerRuntimeException;
import net.dpml.part.control.HandlerNotFoundException;
import net.dpml.part.control.DelegationException;

import net.dpml.logging.Logger;

import net.dpml.composition.info.Type;
import net.dpml.composition.info.TypeHolder;
import net.dpml.composition.runtime.DefaultLogger;

import net.dpml.transit.Transit;
import net.dpml.transit.artifact.Artifact;
import net.dpml.transit.repository.Repository;

/**
 * Composition part handler.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public abstract class CompositionPartHandler implements PartHandler
{
   /**
    * Map containing the foreign part handlers.  The map keys
    * are the string representation of the handler uri.  The entry is the 
    * handler.
    */
    private final WeakHashMap m_handlers = new WeakHashMap();

   /**
    * Transit repository handler used for resolving foreign handlers.
    */
    private final Repository m_loader;

    private final ControllerContext m_context;

    public CompositionPartHandler( ControllerContext context ) throws ControlException
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
    * @return the part estracted from the part handler referenced by the uri
    */
    public Part loadPart( URI uri )
        throws PartNotFoundException, IOException, DelegationException
    {
        return loadSerializedPart( uri );
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
    * Load a part handler giiven a handler uri.
    * @return the part handler
    */
    public Controller getPrimaryController( URI uri ) 
       throws HandlerNotFoundException, DelegationException
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

    private Part loadSerializedPart( URI uri )
        throws IOException, DelegationException, PartNotFoundException
    {
        URL url = Artifact.toURL( uri );
        InputStream input = url.openStream();
        if( null == input )
        {
            throw new PartNotFoundException( uri );
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
                    PartHandler handler = resolvePartHandler( uri );
                    return handler.loadPart( holder.getByteArray() );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Delegate raised an error while attempting to load a serialized part ["
                      + uri
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
              + uri
              + "].";
            throw new PartHandlerRuntimeException( error, e );
        }
    }

    protected PartHandler resolvePartHandler( URI uri ) throws HandlerNotFoundException
    {
        if( getURI().equals( uri ) )
        {
            return this;
        }
        String key = uri.toString();
        synchronized( m_handlers )
        {
            PartHandler[] handlers = (PartHandler[]) m_handlers.keySet().toArray( new PartHandler[0] );
            for( int i=0; i<handlers.length; i++ )
            {
                try
                {
                    PartHandler handler = handlers[i];
                    if( handler.getURI().equals( uri ) )
                    {
                        return handler;
                    }
                }
                catch( RemoteException e )
                {
                    // TODO: log but continue
                }
            }
            try
            {
                PartHandler handler = loadHandler( uri );
                m_handlers.put( handler, null );
                return handler;
            }
            finally
            {
                // log loading event
            }
        }
    }

    private PartHandler loadHandler( URI uri ) throws HandlerNotFoundException
    {
        ClassLoader classloader = Type.class.getClassLoader();
        DefaultLogger logger = new DefaultLogger( "handler" );
        try
        {
            return (PartHandler) m_loader.getPlugin( classloader, uri, new Object[]{ this, logger } );
        }
        catch( IOException e )
        {
            throw new HandlerNotFoundException( uri, e );
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

}
