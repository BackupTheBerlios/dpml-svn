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

package net.dpml.metro.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URL;
import java.util.WeakHashMap;

import net.dpml.logging.Logger;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.model.ComponentModel;

import net.dpml.metro.part.ControllerContext;
import net.dpml.metro.part.Context;
import net.dpml.metro.part.Component;
import net.dpml.metro.part.Controller;
import net.dpml.metro.part.Part;
import net.dpml.metro.part.ControlException;
import net.dpml.metro.part.ControllerNotFoundException;
import net.dpml.metro.part.DelegationException;
import net.dpml.metro.part.PartNotFoundException;
import net.dpml.metro.part.PartHolder;

import net.dpml.transit.Artifact;
import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

/**
 * The composition controller is the controller used to establish remotely accessible
 * component controls.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CompositionController implements Controller
{
    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

    private final Logger m_logger;
    private final ControllerContext m_context;
    private final ComponentController m_controller;
    private final WeakHashMap m_handlers = new WeakHashMap(); // foreign controllers
    private final Repository m_loader;
    
    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

   /**
    * Creation of a new controller.
    * @param logger the logging chanel
    * @exception ControlException if an error occurs during controller creation
    */
    public CompositionController( net.dpml.transit.Logger logger )
       throws ControlException
    {
        this( new CompositionContext( logger, null, null ) );
    }

   /**
    * Creation of a new controller.
    * @param context the control context
    * @exception ControlException if an error occurs during controller creation
    */
    public CompositionController( ControllerContext context )
       throws ControlException
    {
        super();

        m_context = context;
        m_logger = new StandardLogger( context.getLogger() );
        m_loader = Transit.getInstance().getRepository();
        m_logger.debug( "controller: " + CONTROLLER_URI );
        m_controller = new ComponentController( m_logger, this );
    }
    
    //--------------------------------------------------------------------
    // Controller
    //--------------------------------------------------------------------
    
   /**
    * Create a classloader using the supplied anchor classloader and 
    * component directive.
    * 
    * @param anchor the anchor classloader
    * @param context a component context 
    * @return the new classloader
    * @exception ControlException if a classloader creation error occurs
    */
    public ClassLoader createClassLoader( 
      ClassLoader anchor, Context context ) throws ControlException
    {
        if( context instanceof ComponentDirective )
        {
            ComponentDirective directive = (ComponentDirective) context;
            return m_controller.createClassLoader( anchor, directive );
        }
        else
        {
            final String error =
              "Construction of a classloader from the context class ["
              + context.getClass().getName() 
              + "] is not supported.";
            throw new ControllerException( error );
        }
    }
    
   /**
    * Returns the uri of this controller.
    * @return the controller uri
    */
    public URI getURI()
    {
        return CONTROLLER_URI;
    }
    
   /**
    * Create and return a new management context using the supplied part
    * as the inital management state.
    *
    * @param part the part data structure
    * @return the management context instance
    * @exception ControlException if an error occurs during context construction
    */
    public Context createContext( Part part ) throws ControlException
    {
        if( part instanceof ComponentDirective )
        {
            ComponentDirective directive = (ComponentDirective) part;
            return m_controller.createComponentModel( directive );
        }
        else
        {
            //
            // TODO delegate to foreign controller
            //
            
            final String error =
              "Construction of a managment context for the part class ["
              + part.getClass().getName() 
              + "] is not supported.";
            throw new ControllerException( error );
        }
    }
    
   /**
    * Create and return a remote reference to a component handler.
    * @param context the component context
    * @return the component handler
    * @exception Exception if an error occurs during component creation
    */
    public Component createComponent( Context context ) throws Exception
    {
        if( context instanceof ComponentModel )
        {
            ComponentModel model = (ComponentModel) context;
            return m_controller.createComponentHandler( model );
        }
        else
        {
            //
            // TODO delegate to foreign controller
            //
            
            final String error =
              "Construction of a handler for the context class ["
              + context.getClass().getName() 
              + "] is not supported.";
            throw new ControllerException( error );
        }
    }
    
   /**
    * Return the controllers runtime context. The runtime context holds infromation 
    * about the working and temporary directories and a uri identifying the execution 
    * domain.
    * 
    * @return the runtime context
    */
    public ControllerContext getControllerContext()
    {
        return m_context;
    }

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Load a part from serialized form.  The uri is assumed to be a uri that 
    * can be transformed to a URL from which an input stream to a PartHolder 
    * can be established.  If the uri references a foreign part handler the 
    * implementation will attempt to locate and delegate part loading requests 
    * to the foreign handler.
    *
    * @param uri the part uri
    * @return the part estracted from the part holder referenced by the uri
    * @exception ControlException if an error is raised related to part creation
    * @exception IOException if an I/O error occurs while reading the part
    */
    public Part loadPart( URI uri ) throws ControlException, IOException
    {
        return loadSerializedPart( uri );
    }

   /**
    * Load a part from serialized form. 
    *
    * @param url the part url
    * @return the part estracted from the part referenced by the url
    * @exception ControlException if an error is raised related to part creation
    * @exception IOException if an I/O error occurs while reading the part
    */
    public Part loadPart( URL url ) throws ControlException, IOException
    {
        return loadSerializedPart( url );
    }

   /**
    * Load a part from a byte array.
    *
    * @param bytes the byte array
    * @return the part datastructure
    * @exception IOException if an I/O error occurs while reading the part
    */
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
            throw new ControllerRuntimeException( error, e );
        }
    }
    
   /**
    * Load a controller given a uri.
    * @param uri the forign controller uri
    * @return the part controller
    * @exception ControllerNotFoundException if the controller could not be found
    * @exception DelegationException if an error occured in the foreign controller
    */
    public Controller getPrimaryController( URI uri ) 
       throws ControllerNotFoundException, DelegationException
    {
        if( !getURI().equals( uri ) )
        {
            return resolveController( uri );
        }
        else
        {
            return this;
        }
    }

   /*
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
    */

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
            throw new PartNotFoundException( CONTROLLER_URI, getURIFromURL( url ) );
        }
        ObjectInputStream stream = new ObjectInputStream( input );
        try
        {
            PartHolder holder = (PartHolder) stream.readObject();
            URI foreign = holder.getPartHandlerURI();
            if( !getURI().equals( foreign ) )
            {
                try
                {
                    Controller handler = resolveController( foreign );
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
                    throw new DelegationException( CONTROLLER_URI, foreign, error, e );
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
            throw new ControllerRuntimeException( error, e );
        }
    }

    private Controller resolveController( URI uri ) throws ControllerNotFoundException
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
                if( handler.getURI().equals( uri ) )
                {
                    return handler;
                }
            }
            Controller handler = loadHandler( uri );
            m_handlers.put( handler, null );
            return handler;
        }
    }

    private Controller loadHandler( URI uri ) throws ControllerNotFoundException
    {
        ClassLoader classloader = Part.class.getClassLoader();
        try
        {
            return (Controller) m_loader.getPlugin( 
              classloader, uri, 
              new Object[]{m_logger, m_context, m_context.getLogger()} );
        }
        catch( IOException e )
        {
            throw new ControllerNotFoundException( CONTROLLER_URI, uri, e );
        }
    }

   /**
    * Static URI of this controller.
    */
    public static final URI CONTROLLER_URI = createStaticURI( "@PART-HANDLER-URI@" );
    
    static final URI ROOT_URI = createStaticURI( "metro:/" );

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
