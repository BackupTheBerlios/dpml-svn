/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman.
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

import java.io.IOException;
import java.beans.Expression;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;

import net.dpml.transit.monitor.RepositoryMonitorRouter;

import net.dpml.lang.Classpath;
import net.dpml.lang.Category;
import net.dpml.lang.Logger;

import net.dpml.part.Part;
import net.dpml.part.PartDirective;
import net.dpml.part.PartHandler;
import net.dpml.part.PartHandlerFactory;
import net.dpml.part.PartBuilder;
import net.dpml.part.Strategy;
import net.dpml.part.Plugin;

/**
 * Utility class supporting downloading of resources based on
 * artifact references.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class StandardLoader implements Repository
{
    private final Logger m_logger;
    private final PartBuilder m_builder = new PartBuilder( null );
    
    private static final PartHandlerFactory FACTORY = PartHandlerFactory.getInstance();
    
   /**
    * Creation of a new repository handler.
    * @param logger the assigned logging channel
    */
    StandardLoader( Logger logger )
    {
        m_logger = logger;
    }

    // ------------------------------------------------------------------------
    // Repository
    // ------------------------------------------------------------------------
    
   /**
    * Creates a plugin descriptor from an artifact.
    *
    * @param uri the artifact reference to the plugin descriptor
    * @return the plugin descriptor
    * @exception IOException if a factory creation error occurs
    */
    public Part getPart( URI uri ) throws IOException
    {
        try
        {
            return m_builder.loadPart( uri );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error during part resolve."
              + "\nURI: " + uri;
            throw new RepositoryException( error, e );
        }
    }
    
   /**
    * Get a plugin classloader relative to a supplied uri.
    *
    * @param parent the parent classloader
    * @param uri the plugin uri
    * @return the plugin classloader.
    * @exception IOException if plugin loading exception occurs.
    * @exception NullArgumentException if the supplied uri or parent classloader
    *            is null.
    */
    public ClassLoader getPluginClassLoader( ClassLoader parent, URI uri )
        throws IOException, NullArgumentException
    {
        Part part = getPart( uri );
        return getPluginClassLoader( parent, part );
    }
    
   /**
    * Get a plugin classloader relative to a supplied uri.
    *
    * @param parent the parent classloader
    * @param uri the plugin uri
    * @return the plugin classloader.
    * @exception IOException if plugin loading exception occurs.
    * @exception NullArgumentException if the supplied uri or parent classloader
    *            is null.
    */
    public ClassLoader getPluginClassLoader( ClassLoader parent, Part part )
        throws IOException, NullArgumentException
    {
        try
        {
            Classpath classpath = part.getClasspath();
            Strategy strategy = part.getStrategy();
            PartDirective directive = strategy.getPartDirective();
            PartHandler handler = 
              PartHandlerFactory.getInstance().getPartHandler( directive );
            return handler.getClassLoader( parent, classpath );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error during part classloader resolve."
              + "\nPart: " + part;
            throw new RepositoryException( error, e );
        }
    }


   /**
    * Get a plugin class relative to a supplied artifact.  The artifact uri
    * must refer to a plugin descriptor (i.e. the artifact type is "plugin").
    * The class returned will be the class named in the plugin descriptor.
    *
    * @param parent the parent classloader
    * @param uri the plugin artifact
    * @return the plugin class
    * @exception IOException if a class resolution error occurs
    * @exception NullArgumentException if the supplied parent classloader or uri is null
    */
    public Class getPluginClass( ClassLoader parent, URI uri )
       throws IOException, NullArgumentException
    {
        try
        {
            Part part = m_builder.loadPart( parent, uri );
            return getPluginClass( parent, part );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error during part class resolve."
              + "\nURI: " + uri;
            throw new RepositoryException( error, e );
        }
    }
    
   /**
    * Get a plugin class relative to a supplied artifact.  The artifact uri
    * must refer to a plugin descriptor (i.e. the artifact type is "plugin").
    * The class returned will be the class named in the plugin descriptor.
    *
    * @param parent the parent classloader
    * @param uri the plugin artifact
    * @return the plugin class
    * @exception IOException if a class resolution error occurs
    * @exception NullArgumentException if the supplied parent classloader or uri is null
    */
    public Class getPluginClass( ClassLoader parent, Part part )
       throws IOException, NullArgumentException
    {
        try
        {
            Classpath classpath = part.getClasspath();
            Strategy strategy = part.getStrategy();
            PartDirective directive = strategy.getPartDirective();
            PartHandler handler = 
              PartHandlerFactory.getInstance().getPartHandler( directive );
            ClassLoader classloader = handler.getClassLoader( parent, classpath );
            Object data = strategy.getDeploymentData();
            if( data instanceof Plugin )
            {
                Plugin plugin = (Plugin) data;
                String classname = plugin.getClassname();
                return classloader.loadClass( classname );
            }
            else
            {
                throw new UnsupportedOperationException( "getPartClass/" + data.getClass().getName() );
            }
        }
        catch( ClassNotFoundException e )
        {
            final String error = 
              "Class: " + e.getMessage()
              + "\nPart: " + part;
            throw new PluginClassNotFoundException( error );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error during part class resolve."
              + "\nPart: " + part;
            throw new RepositoryException( error, e );
        }
    }
    
   /**
    * Instantiate an object using a plugin uri, parent classloader and 
    * a supplied argument array. The plugin uri is used to resolve a plugin
    * descriptor.  A classloader stack will be constructed using the supplied
    * classloader as the anchor for stack construction. A classname declared
    * in the plugin descriptor must has a single public constructor.  The 
    * implementation will resolve constructor parameters relative to the 
    * supplied argument array and return an instance of the class.
    *
    * @param parent the parent classloader
    * @param uri the reference to the application
    * @param args commandline arguments
    * @return the plugin instance
    * @exception IOException if a plugin creation error occurs
    * @exception InvocationTargetException if the plugin constructor invocation error occurs
    * @exception NullArgumentException if the supplied parent classloader,
    *    uri or args are null
    */
    public Object getPlugin( ClassLoader parent, URI uri, Object[] args  )
      throws IOException, InvocationTargetException, NullArgumentException
    {
        try
        {
            net.dpml.part.Part part = m_builder.loadPart( parent, uri );
            Classpath classpath = part.getClasspath();
            net.dpml.part.Strategy strategy = part.getStrategy();
            net.dpml.part.PartDirective directive = strategy.getPartDirective();
            net.dpml.part.PartHandler handler = PartHandlerFactory.getInstance().getPartHandler( directive );
            Object data = strategy.getDeploymentData();
            return handler.getInstance( parent, classpath, data, args );
        }
        catch( RepositoryException re )
        {
            throw re;
        }
        catch( InvocationTargetException e )
        {
            throw e;
        }
        catch( Exception ce )
        {
            String error = "Unable to create a plugin using [" + uri + "].";
            getLogger().error( error, ce );
            throw new RepositoryException( error, ce );
        }
    }
    
    //---------------------------------------------------------------------
    // implementation
    //---------------------------------------------------------------------

    private URL getURL( URI uri ) throws IOException
    {
        return Artifact.toURL( uri );
    }

   /**
    * Create a factory using a supplied class and command line arguments.
    *
    * @param clazz the the factory class
    * @param args the command line args
    * @return the plugin instance
    * @exception IOException if a plugin creation error occurs
    * @exception InvocationTargetException if a plugin constructor invocation error occurs
    * @exception NullArgumentException if the class or args argument is null
    */
    //private Object createPlugin( Class clazz, Object[] args )
    //    throws IOException, NullArgumentException, InvocationTargetException
    //{
    //    if( null == clazz )
    //    {
    //        throw new NullArgumentException( "clazz" );
    //    }
    //    if( null == args )
    //    {
    //        throw new NullArgumentException( "args" );
    //    }
    //    return instantiate( clazz, args );
    //}

    public Object instantiate( Class clazz, Object[] args ) throws RepositoryException, InvocationTargetException
    {
        if( null == clazz )
        {
            throw new NullArgumentException( "clazz" );
        }
        if( null == args )
        {
            throw new NullArgumentException( "args" );
        }
        for( int i=0; i < args.length; i++ )
        {
            Object p = args[i];
            if( null == p )
            {
                final String error = 
                  "User supplied instantiation argument at position [" 
                  + i 
                  + "] for the class ["
                  + clazz.getName()
                  + "] is a null value.";
                throw new NullArgumentException( error );
            }
        }
        
        if( clazz.getConstructors().length == 1 )
        {
            Constructor constructor = getSingleConstructor( clazz );
            return instantiate( constructor, args );
        }
        else
        {
            try
            {
                Expression expression = new Expression( clazz, "new", args );
                return expression.getValue();
            }
            catch( InvocationTargetException e )
            {
                throw e;
            }
            catch( RepositoryException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                final String error = 
                "Class instantiation error [" + clazz.getName() + "]";
                throw new RepositoryException( error, e );
            }
        }
    }
    
    public Object instantiate( Constructor constructor, Object[] args ) 
      throws RepositoryException, InvocationTargetException
    {
        Object[] arguments = populate( constructor, args );
        return newInstance( constructor, arguments );
    }
    
    protected Object[] populate( Constructor constructor, Object[] args ) throws RepositoryException
    {
        if( null == constructor )
        {
            throw new NullArgumentException( "constructor" );
        }
        if( null == args )
        {
            throw new NullArgumentException( "args" );
        }
        
        Class[] classes = constructor.getParameterTypes();
        Object[] arguments = new Object[ classes.length ];
        ArrayList list = new ArrayList();
        for( int i=0; i < args.length; i++ )
        {
            list.add( args[i] );
        }

        //
        // sweep though the construct arguments one by one and
        // see if we can assign a value based on the supplied args
        //

        for( int i=0; i < classes.length; i++ )
        {
            Class clazz = classes[i];
            Iterator iterator = list.iterator();
            while( iterator.hasNext() )
            {
                Object object = iterator.next();
                Class c = object.getClass();
                if( isAssignableFrom( clazz, c ) )
                {
                    arguments[i] = object;
                    list.remove( object );
                    break;
                }
            }
        }

        //
        // if any arguments are unresolved then check if the argument type
        // is something we can implicity establish
        //

        for( int i=0; i < arguments.length; i++ )
        {
            if( null == arguments[i] )
            {
                Class c = classes[i];
                if( c.isArray() )
                {
                    arguments[i] = getEmptyArrayInstance( c );
                }
                else
                {
                    final String error =
                      "Unable to resolve a value for a constructor parameter."
                      + "\nConstructor class: " + constructor.getDeclaringClass().getName()
                      + "\nParameter class: " + c.getName()
                      + "\nParameter position: " + ( i + 1 );
                    throw new RepositoryException( error );
                }
            }
        }
        return arguments;
    }

    private boolean isAssignableFrom( Class clazz, Class c )
    {
        if( clazz.isPrimitive() )
        {
            if( Integer.TYPE == clazz )
            {
                return Integer.class.isAssignableFrom( c );
            }
            else if( Boolean.TYPE == clazz )
            {
                return Boolean.class.isAssignableFrom( c );
            }
            else if( Byte.TYPE == clazz )
            {
                return Byte.class.isAssignableFrom( c );
            }
            else if( Short.TYPE == clazz )
            {
                return Short.class.isAssignableFrom( c );
            }
            else if( Long.TYPE == clazz )
            {
                return Long.class.isAssignableFrom( c );
            }
            else if( Float.TYPE == clazz )
            {
                return Float.class.isAssignableFrom( c );
            }
            else if( Double.TYPE == clazz )
            {
                return Double.class.isAssignableFrom( c );
            }
            else
            {
                final String error =
                  "Primitive type ["
                  + c.getName()
                  + "] not supported.";
                throw new RuntimeException( error );
            }
        }
        else
        {
            return clazz.isAssignableFrom( c );
        }
    }

    private Object newInstance( Constructor constructor, Object[] arguments )
      throws RepositoryException, InvocationTargetException
    {
        try
        {
            Object instance = constructor.newInstance( arguments );
            getMonitor().pluginInstantiated( instance );
            return instance;
        }
        catch( InvocationTargetException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Cannot create an instance of ["
              + constructor.getDeclaringClass().getName()
              + "] due to an instantiation failure.";
            throw new RepositoryException( error, e );
        }
    }
    
    private Constructor getSingleConstructor( Class clazz ) throws RepositoryException
    {
        if( null == clazz )
        {
            throw new NullArgumentException( "clazz" );
        }
        Constructor[] constructors = clazz.getConstructors();
        if( constructors.length < 1 )
        {
            final String error =
              "Target class ["
              + clazz.getName()
              + "] does not declare a public constructor.";
            throw new RepositoryException( error );
        }
        else if( constructors.length > 1 )
        {
            final String error =
              "Target class ["
              + clazz.getName()
              + "] declares multiple public constructors.";
            throw new RepositoryException( error );
        }
        else
        {
            return constructors[0];
        }
    }

   /**
    * Constructs an empty array instance.
    * @param clazz the array class
    * @return the empty array instance
    * @exception RepositoryException if an error occurs
    */
    private Object[] getEmptyArrayInstance( Class clazz ) throws RepositoryException
    {
        try
        {
            return (Object[]) Array.newInstance( clazz.getComponentType(), 0 );
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to construct an empty array for the class: "
              + clazz.getName();
            throw new RepositoryException( error, e );
        }
    }

    /**
     * Returns a classloader.
     * @param base the parent classloader
     * @param plugin the plugin uri
     * @param classpath the classpath descriptor
     * @return the classloader
     * @exception IOException if a classloader construction error occurs
     * @exception NullArgumentException if either the base or the descriptor
     *            argument is null.
     */
    public ClassLoader createClassLoader( ClassLoader base, URI plugin, Classpath classpath )
        throws IOException, NullArgumentException
    {
        if( null == classpath )
        {
            throw new NullArgumentException( "classpath" );
        }
        if( null == base )
        {
            throw new NullArgumentException( "base" );
        }
        
        URI[] systemArtifacts = classpath.getDependencies( Category.SYSTEM );
        URL[] sysUrls = getURLs( systemArtifacts );
        if( sysUrls.length > 0 )
        {
            updateSystemClassLoader( plugin, sysUrls );
        }
        
        URI[] apiArtifacts = classpath.getDependencies( Category.PUBLIC );
        URL[] apis = getURLs( apiArtifacts  );
        ClassLoader api = buildClassLoader( plugin, Category.PUBLIC, base, apis );
        
        URI[] spiArtifacts = classpath.getDependencies( Category.PROTECTED );
        URL[] spis = getURLs( spiArtifacts );
        ClassLoader spi = buildClassLoader( plugin, Category.PROTECTED, api, spis );
        
        URI[] impArtifacts = classpath.getDependencies( Category.PRIVATE );
        URL[] imps = getURLs( impArtifacts );
        ClassLoader classloader = buildClassLoader( plugin, Category.PRIVATE, spi, imps );
        
        return classloader;
    }
    
    private void updateSystemClassLoader( URI plugin, URL[] urls ) throws TransitException
    {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        synchronized( parent )
        {
            if( parent instanceof SystemClassLoader )
            {
                SystemClassLoader loader = (SystemClassLoader) parent;
                loader.addDelegates( urls );
                getMonitor().systemExpanded( plugin, urls );
            }
            else
            {
                final String message =
                  "Cannot load [" 
                  + urls.length 
                  + "] system artifacts from [" 
                  + plugin
                  + "] into a foreign system classloader.";
                getLogger().debug( message );
            }
        }
    }

   /**
    * Convert a sequncence of URIs to URLs.
    * @param uris the uris to convert
    * @return the corresponding urls
    * @exception IOException of a transformation error occurs
    */
    private URL[] getURLs( URI[] uris ) throws IOException
    {
        URL[] urls = new URL[ uris.length ];
        for( int i=0; i < urls.length; i++ )
        {
            URI uri = uris[i];
            if( Artifact.isRecognized( uri ) )
            {
                urls[i] = Artifact.toURL( uri );
            }
            else
            {
                urls[i] = uri.toURL();
            }
        }
        return urls;
    }

   /**
    * Internal utility class to build a classloader.  If the supplied url
    * sequence is zero length the parent classloader is returned directly.
    *
    * @param type the type of classloader (api, spi, impl)
    * @param parent the parent classloader
    * @param urls the urls to assign as classloader content
    * @return the classloader
    */
    private ClassLoader buildClassLoader( URI plugin, Category category, ClassLoader parent, URL[] urls )
    {
        if( 0 == urls.length )
        {
            return parent;
        }
        ArrayList list = new ArrayList();
        for( int i=0; i < urls.length; i++ )
        {
            if( isaCandidate( parent, urls[i] ) )
            {
                list.add( urls[i] );
            }
        }
        URL[] qualified = (URL[]) list.toArray( new URL[0] );
        if( qualified.length == 0 )
        {
            return parent;
        }
        else
        {
            ClassLoader loader = new StandardClassLoader( plugin, category, qualified, parent );
            getMonitor().classloaderConstructed( category.toString(), loader );
            return loader;
        }
    }

   /**
    * Test if the supplied url is already present within the supplied classloader.
    * @param classloader the classloader to validate against
    * @param url to url to check for
    * @return true if the url is not included in the classloader
    */
    private boolean isaCandidate( ClassLoader classloader, URL url )
    {
        if( classloader instanceof URLClassLoader )
        {
            URL[] urls = ( (URLClassLoader) classloader ).getURLs();
            for( int i=0; i < urls.length; i++ )
            {
                if( urls[i].equals( url ) )
                {
                    return false;
                }
            }
            ClassLoader parent = classloader.getParent();
            if( parent == null )
            {
                return true;
            }
            else
            {
                return isaCandidate( parent, url );
            }
        }
        else
        {
            return true;
        }
    }

   /**
    * Load a factory class using a supplied classloader and factory classname.
    * @param classloader the classloader to load the class from
    * @param classname the plugin classname
    * @return the factory class
    * @exception RepositoryException if a factory class loading error occurs
    * @exception NullArgumentException if the supplied classloader or classname is null
    * @exception ClassNotFoundException if the plugin class is not found in the supplied classloader
    */
    /*
    protected Class loadPluginClass( ClassLoader classloader, String classname )
        throws RepositoryException, NullArgumentException, ClassNotFoundException
    {
        if( null == classloader )
        {
            throw new NullArgumentException( "classloader" );
        }
        if( null == classname )
        {
            throw new NullArgumentException( "classname" );
        }

        try
        {
            return classloader.loadClass( classname );
        }
        catch( ClassNotFoundException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to load factory class: ["
              + classname
              + "].";
            throw new RepositoryException( error, e );
        }
    }
    */

   /**
    * Convert a uri to an artifact.
    * @param uri the uri to convert
    * @return the artifact
    * @exception RepositoryException if the convertion cannot be performed
    */
    private Artifact getArtifact( URI uri ) throws RepositoryException
    {
        try
        {
            return Artifact.createArtifact( uri );
        }
        catch( Throwable e )
        {
            final String error =
              "Unable to convert the uri ["
              + uri
              + "] to an artifact.";
            throw new RepositoryException( error, e );
        }
    }

    private RepositoryMonitorRouter getMonitor()
    {
        return Transit.getInstance().getRepositoryMonitorRouter();
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}
