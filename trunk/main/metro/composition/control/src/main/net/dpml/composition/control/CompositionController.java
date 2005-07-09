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
import java.net.URLClassLoader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;

import net.dpml.logging.Logger;

import net.dpml.composition.data.ClassLoaderDirective;
import net.dpml.composition.data.ClasspathDirective;
import net.dpml.composition.data.ComponentProfile;
import net.dpml.composition.data.ValueDirective;

import net.dpml.composition.runtime.ComponentHandler;
import net.dpml.composition.runtime.ValueHandler;
import net.dpml.composition.runtime.ValueController;
import net.dpml.composition.runtime.ComponentController;
import net.dpml.composition.runtime.CompositionHandler;
import net.dpml.composition.runtime.DefaultLogger;

import net.dpml.part.DelegationException;
import net.dpml.part.Part;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.PartNotFoundException;
import net.dpml.part.control.ControllerContext;
import net.dpml.part.control.ControlException;
import net.dpml.part.control.ControllerRuntimeException;
import net.dpml.part.control.Controller;
import net.dpml.part.control.Disposable;
import net.dpml.part.control.LifecycleException;
import net.dpml.part.control.UnsupportedPartTypeException;
import net.dpml.part.control.Component;
import net.dpml.part.control.ClassLoadingContext;
import net.dpml.part.control.ComponentException;
import net.dpml.part.control.Container;

import net.dpml.transit.adapter.LoggingAdapter;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.DefaultContentModel;

/**
 * A initial test controller.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class CompositionController extends CompositionPartHandler implements Controller
{
    //--------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------

    public static final String SELF = "self";

    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

    private final ControllerContext m_context;
    private final ValueController m_valueController;
    private final ComponentController m_componentController;
    private final Logger m_logger;

    //private final LifestyleHandler m_lifestyleHandler;

    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

    public CompositionController( ControllerContext context )
       throws ControlException
    {
        super( context );

        m_context = context;

        if( context instanceof CompositionControllerContext )
        {
            CompositionControllerContext c = (CompositionControllerContext) context;
            m_logger = c.getLogger();
        }
        else
        {
            m_logger = getLoggerForURI( m_context.getURI() );
        }
        m_valueController = new ValueController( this );
        m_componentController = new ComponentController( m_logger, this );
        m_logger.debug( "metro controller established" );
        //m_lifestyleHandler = new LifestyleHandler( m_logger, m_componentController );
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

    Logger getLogger()
    {
        return m_logger;
    }

    public ComponentController getComponentController()
    {
        return m_componentController;
    }

    public ValueController getValueController()
    {
        return m_valueController;
    }

    //--------------------------------------------------------------------
    // Controller
    //--------------------------------------------------------------------

   /**
    * Construct a new top-level component.
    *
    * @param uri a uri identifying a part from which the component will be created.
    * @return the new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception IOException if an error occurs while attempting to resolve the component part uri
    * @exception PartNotFoundException if the uri could not be resolved to a physical resource
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    */
    public Component newComponent( URI uri )
      throws IOException, ComponentException, PartNotFoundException, 
      PartHandlerNotFoundException, DelegationException 
    {
        m_logger.debug( "loading part " + uri );
        Part part = loadPart( uri );
        return newComponent( null, part, null );
    }

   /**
    * Construct a new component using the supplied part as the defintion of the 
    * component type and deployment criteria.
    *
    * @param parent the enclosing parent component (may be null)
    * @param part component definition including type and deployment data
    * @param name the name to assign to the new component
    * @return a new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    * @exception UnsupportedPartTypeException if the component type is recognized but not supported
    */
    public Component newComponent( Component parent, Part part, String name )
      throws ComponentException, PartHandlerNotFoundException, DelegationException
    {
        URI partition = getPartition( parent );
        if( isRecognizedPart( part ) )
        {
            ClassLoader classloader = getClassLoader( parent );

            if( part instanceof ValueDirective )
            {
                ValueDirective directive = (ValueDirective) part;
                String defaultName = directive.getKey();
                String theName = getName( defaultName, name );
                URI id = createURI( partition, theName );
                Logger logger = getLoggerForURI( id );
                logger.debug( "new value" );
                return new ValueHandler( logger, this, classloader, id, directive, parent );
            }
            else if( part instanceof ComponentProfile )
            {
                ComponentProfile profile = (ComponentProfile) part;
                String defaultName = profile.getName();
                String theName = getName( defaultName, name );
                URI id = createURI( partition, theName );
                Logger logger = getLoggerForURI( id );
                logger.debug( "new component" );
                ClassLoader loader = getClassLoader( classloader, id, profile );
                return new CompositionHandler( logger, this, loader, id, profile, parent );
            }
            else
            {
                String classname = part.getClass().getName();
                final String error = 
                  "Unsupported part implementation class ["
                  + classname
                  + "] passed to newComponent/3.";
                throw new UnsupportedPartTypeException( CONTROLLER_URI, classname, error );
            }
        }
        else
        {
            URI handlerUri = part.getPartHandlerURI();
            Controller controller = (Controller) getPrimaryController( handlerUri );
            return controller.newComponent( parent, part, name );
        }
    }

   /**
    * Construct a new component using the supplied part as the defintion of the 
    * component type and deployment criteria.  This method is typically used by
    * buildtime tools where the buildtime classloader is establised prior to 
    * component deployment.
    *
    * @param classloader the root classloader
    * @param part component definition including type and deployment data
    * @return a new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    * @exception UnsupportedPartTypeException if the component type is recognized but not supported
    */
    public Container newContainer( ClassLoader classloader, Part part )
      throws ComponentException, PartHandlerNotFoundException, DelegationException
    {
        URI partition = getPartition();
        if( isRecognizedPart( part ) )
        {
            if( part instanceof ComponentProfile )
            {
                ComponentProfile profile = (ComponentProfile) part;
                String name = profile.getName();
                URI id = createURI( partition, name );
                return new CompositionHandler( m_logger, this, classloader, id, profile, null );
            }
            else
            {
                String classname = part.getClass().getName();
                final String error = 
                  "Unsupported part implementation class ["
                  + classname
                  + "] passed to newComponent/3.";
                throw new UnsupportedPartTypeException( CONTROLLER_URI, classname, error );
            }
        }
        else
        {
            String classname = part.getClass().getName();
            final String error = 
              "Unsupported part implementation class ["
              + classname
              + "] passed to newComponent/2.";
            throw new UnsupportedPartTypeException( CONTROLLER_URI, classname, error );
        }
    }

    private ClassLoader getClassLoader( Component component )
    {
        if( component instanceof ClassLoadingContext )
        {
            ClassLoadingContext context = (ClassLoadingContext) component;
            return context.getClassLoader();
        }
        else
        {
            return Part.class.getClassLoader();
        }
    }

    //--------------------------------------------------------------------
    // CompositionController
    //--------------------------------------------------------------------

    private URI getPartition()
    {
        return m_context.getURI();
    }

    private URI getPartition( Component component )
    {
        if( null == component )
        {
            return getPartition();
        }
        else
        {
            return component.getURI();
        }
    }

    private String getName( String defaultValue, String preferredValue )
    {
        if( null != preferredValue ) 
        {
            return preferredValue;
        }
        else
        {
            return defaultValue;
        }
    }

    //--------------------------------------------------------------------
    // private
    //--------------------------------------------------------------------

    private ClassLoader getClassLoader( ClassLoader parent, URI partition, ComponentProfile profile )
    {
        final String name = profile.getName();
        final ClassLoaderDirective cld = profile.getClassLoaderDirective();
        final ClasspathDirective[] cpds = cld.getClasspathDirectives();
        for( int i=0; i<cpds.length; i++ )
        {
            ClasspathDirective cpd = cpds[i];
            String tag = cpd.getCategoryName();
            URI id = createInstanceURI( partition, tag );
            URI[] uris = filter( cpd.getURIs(), parent );
            if( uris.length > 0 )
            {
                getLogger().debug( "creating " + tag + " classloader with " + uris.length + " entries" );
                parent = new CompositionClassLoader( m_logger, id, i, uris, parent );
            }
        }
        return parent;
    }

    private URI[] filter( URI[] uris, ClassLoader classloader )
    {
        if( classloader instanceof URLClassLoader )
        {
            URLClassLoader loader = (URLClassLoader) classloader;
            return filterURLClassLoader( uris, loader );
        }
        else
        {
            return uris;
        }
    }

    private URI[] filterURLClassLoader( URI[] uris, URLClassLoader parent )
    {
        ArrayList list = new ArrayList();
        for( int i=(uris.length - 1); i>-1; i-- )
        {
            URI uri = uris[i];
            String path = uri.toString();
            if( false == exists( uri, parent ) )
            {
                list.add( uri );
            }
        }
        return (URI[]) list.toArray( new URI[0] );
    }

    private boolean exists( URI uri, URLClassLoader classloader )
    {
        ClassLoader parent = classloader.getParent();
        if( parent instanceof URLClassLoader )
        {
            URLClassLoader loader = (URLClassLoader) parent;
            if( exists( uri, loader ) )
            {
                return true;
            }
        }

        String ref = uri.toString();
        URL[] urls = classloader.getURLs();
        for( int i=0; i<urls.length; i++ )
        {
            URL url = urls[i];
            String spec = url.toString();
            if( spec.equals( ref ) )
            {
                return true;
            }
        }

        return false;
    }


    private URI getLocalPartition( URI base, String name )
    {
        String scheme = base.getScheme();
        String spec = base.getSchemeSpecificPart();
        String path = getPathWithTrailingSlash( spec );
        String cleanName = getNameWithoutLeadingSlash( name );
        return createURI( scheme, path, cleanName );
    }

    private String getNameWithoutLeadingSlash( String path )
    {
        String spec = path.trim();
        if( spec.startsWith( "/" ) )
        {
            return spec.substring( 1 );
        }
        else
        {
            return spec;
        }
    }

    private String getPathWithTrailingSlash( String path )
    {
        String spec = path.trim();
        if( spec.endsWith( "/" ) )
        {
            return spec;
        }
        else
        {
            return spec + "/";
        }
    }

    private boolean isRecognizedPart( Part part )
    {
        return getURI().equals( part.getPartHandlerURI() );
    }

    //--------------------------------------------------------------------
    // static utilities
    //--------------------------------------------------------------------

    public static URI createURI( URI base, String name )
    {
        String scheme = base.getScheme();
        String partition = base.getSchemeSpecificPart();
        if( partition.endsWith( "/" ) )
        {
            final String path = partition + name;
            return createURI( scheme, path );
        }
        else
        {
            final String path = partition + "/" + name;
            return createURI( scheme, path );
        }
    }

    public static URI createInstanceURI( URI base, String id )
    {
        String scheme = base.getScheme();
        String partition = base.getSchemeSpecificPart();
        return createURI( scheme, partition, id );
    }

    public static URI createURI( String scheme, String path )
    {
        return createURI( scheme, path, null );
    }

    private static URI createURI( String scheme, String path, String fragment )
    {
        try
        {
            return new URI( scheme, path, fragment );
        }
        catch( URISyntaxException e )
        {
            final String error = 
              "Internal exception while attempting to construct a part uri using the schme ["
              + scheme
              + "] and path ["
              + path
              + "].";
            throw new ControllerRuntimeException( CONTROLLER_URI, error, e );
        }
    }

    protected static URI createURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException e )
        {
            final String error = 
              "Invalid URI specification [" + spec + "].";
            throw new ControllerRuntimeException( CONTROLLER_URI, error, e );
        }
    }

    private Logger getLoggerForURI( URI uri )
    {
        String path = uri.getSchemeSpecificPart();
        if( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }
        path.replace( '/', '.' );
        return new DefaultLogger( path );
    }

    static final URI CONTROLLER_URI = setupURI( "@PART-CONTROLLER-URI@" );

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

    private static ControllerContext createDefaultContext()
    {
        try
        {
            String title = "Metro Build Context.";
            String type = "part";
            Properties properties = new Properties();
            net.dpml.transit.model.Logger logger = new LoggingAdapter( "metro" );
            DefaultContentModel model = 
              new DefaultContentModel( logger, null, type, title, properties );
            
            return new CompositionControllerContext( model );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return null;
        }
    }
}
