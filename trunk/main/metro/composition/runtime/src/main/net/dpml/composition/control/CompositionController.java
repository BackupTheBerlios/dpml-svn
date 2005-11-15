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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLClassLoader;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

import net.dpml.logging.Logger;

import net.dpml.component.data.ClassLoaderDirective;
import net.dpml.component.data.ClasspathDirective;
import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ValueDirective;
import net.dpml.component.data.Directive;
import net.dpml.component.info.InfoDescriptor;
import net.dpml.component.info.Type;
import net.dpml.component.model.ComponentModel;

//import net.dpml.composition.runtime.ComponentHandler;
//import net.dpml.composition.runtime.ValueHandler;
//import net.dpml.composition.runtime.ValueController;
//import net.dpml.composition.runtime.ComponentController;
//import net.dpml.composition.runtime.CompositionHandler;
//import net.dpml.composition.runtime.DefaultLogger;

import net.dpml.component.control.ClassLoaderManager;
import net.dpml.component.control.ControllerContext;
import net.dpml.component.control.ControllerException;
import net.dpml.component.control.ControllerRuntimeException;
import net.dpml.component.control.Controller;
import net.dpml.component.control.Disposable;
import net.dpml.component.control.LifecycleException;
import net.dpml.component.control.UnsupportedPartTypeException;
import net.dpml.component.runtime.Component;
import net.dpml.component.runtime.ComponentException;

import net.dpml.composition.engine.ComponentController;

//import net.dpml.component.runtime.ClassLoadingContext;
//import net.dpml.component.runtime.Container;
//import net.dpml.component.runtime.Service;

import net.dpml.part.DelegationException;
import net.dpml.part.Part;
import net.dpml.part.PartException;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.PartNotFoundException;
import net.dpml.part.Context;
import net.dpml.part.Handler;

import net.dpml.transit.Plugin;
import net.dpml.transit.Category;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.Value;

/**
 * The composition controller is the controller used to establish remotely accessible
 * component controls.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class CompositionController extends CompositionPartHandler 
  implements Controller, ClassLoaderManager
{
    //--------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------

    public static final String SELF = "self";

    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

    private final Logger m_logger;
    private final ControllerContext m_context;
    private final ComponentController m_controller;

    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

    public CompositionController( net.dpml.transit.Logger logger )
       throws ControllerException
    {
        this( new CompositionContext( logger, null, null ) );
    }

    protected CompositionController( ControllerContext context )
       throws ControllerException
    {
        super( context );

        m_context = context;
        m_logger = new StandardLogger( context.getLogger() );
        m_logger.debug( "controller: " + CONTROLLER_URI );
        
        m_controller = new ComponentController( m_logger, this );
    }
    
    //--------------------------------------------------------------------
    // ClassLoaderManager
    //--------------------------------------------------------------------
    
   /**
    * Create a classloader using the supplied anchor classloader and 
    * component directive.
    * 
    * @param anchor the anchor classloader
    * @param part a component part 
    */
    public ClassLoader createClassLoader( ClassLoader anchor, Part part ) throws PartException
    {
        if( part instanceof ComponentDirective )
        {
            ComponentDirective directive = (ComponentDirective) part;
            return m_controller.createClassLoader( anchor, directive );
        }
        else
        {
            //
            // TODO delegate to foreign controller
            //
            
            final String error =
              "Construction of a classloader from the part class ["
              + part.getClass().getName() 
              + "] is not supported.";
            throw new ControllerException( CONTROLLER_URI, error );
        }
    }
    
    //--------------------------------------------------------------------
    // PartHandler
    //--------------------------------------------------------------------
    
   /**
    * Create and return a new management context using the supplied part
    * as the inital management state.
    *
    * @param part the part data structure
    * @return the management context instance
    */
    public Context createContext( Part part ) throws PartException
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
            throw new PartException( error );
        }
    }
    
   /**
    * Create and return a remote reference to a component handler.
    * @return the component handler
    */
    public Handler createHandler( Context context ) throws Exception
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
            throw new PartException( error );
        }
    }
    
    //--------------------------------------------------------------------
    // CompositionController
    //--------------------------------------------------------------------

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

    Logger getLogger()
    {
        return m_logger;
    }


    static final URI CONTROLLER_URI = setupURI( "@PART-HANDLER-URI@" );
    static final URI ROOT_URI = setupURI( "metro:/" );

    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
}
