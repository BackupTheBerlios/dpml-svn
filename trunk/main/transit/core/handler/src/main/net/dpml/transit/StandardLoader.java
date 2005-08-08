/*
 * Copyright 2004 Stephen J. McConnell.
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
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.prefs.Preferences;

import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.artifact.Handler;
import net.dpml.transit.monitor.RepositoryMonitorRouter;

/**
 * Utility class supporting downloading of resources based on
 * artifact references.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
class StandardLoader
    implements Repository
{
    // ------------------------------------------------------------------------
    // Repository
    // ------------------------------------------------------------------------

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
        if( null == uri )
        {
            throw new NullArgumentException( "uri" );
        }
        if( null == parent )
        {
            throw new NullArgumentException( "parent" );
        }
        try
        {
            Plugin descriptor = getPluginDescriptor( uri );
            return createClassLoader( parent, descriptor );
        }
        catch( Exception ce )
        {
            String error =
              "Unable to create a plugin classloader using ["
              + uri
              + "].";
            throw new RepositoryException( error, ce );
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
        if( null == uri )
        {
            throw new NullArgumentException( "uri" );
        }
        if( null == parent )
        {
            throw new NullArgumentException( "parent" );
        }

        Plugin descriptor = getPluginDescriptor( uri );
        return getPluginClass( parent, descriptor );
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
    * @exception NullArgumentException if the supplied parent classloader,
    *    uri or args are null
    */
    public Object getPlugin( ClassLoader parent, URI uri, Object[] args  )
      throws IOException, NullArgumentException
    {
        if( null == uri )
        {
            throw new NullArgumentException( "uri" );
        }
        if( null == parent )
        {
            throw new NullArgumentException( "parent" );
        }
        try
        {
            Plugin descriptor = getPluginDescriptor( uri );
            getMonitor().sequenceInfo( "building classload stack" );
            ClassLoader classloader = createClassLoader( parent, descriptor );
            String classname = descriptor.getClassname();
            if( null == classname )
            {
                final String error =
                  "Supplied plugin descriptor ["
                  + uri
                  + "] does not declare a main class.";
                throw new IOException( error );
            }
            Class clazz = loadPluginClass( classloader, classname );
            getMonitor().establishedPluginClass( clazz );
            return createPlugin( classloader, descriptor, clazz, args );
        }
        catch( Exception ce )
        {
            String error = "Unable to create a plugin using [" + uri + "].";
            getMonitor().exceptionOccurred( "getPlugin", ce );
            throw new RepositoryException( error, ce );
        }
    }

    //---------------------------------------------------------------------
    // implementation
    //---------------------------------------------------------------------

   /**
    * Get a plugin class relative to a supplied descriptor.
    *
    * @param parent the parent classloader
    * @param descriptor the plugin descriptor
    * @return the plugin class
    * @exception IOException if a cload establishement error occurs
    * @exception NullArgumentException if the parent classloader or descriptor is null
    * @exception IllegalArgumentException if the classname could not be resolved
    */
    private Class getPluginClass( ClassLoader parent, Plugin descriptor )
       throws IOException, NullArgumentException, IllegalArgumentException
    {
        if( null == descriptor )
        {
            throw new NullArgumentException( "descriptor" );
        }
        if( null == parent )
        {
            throw new NullArgumentException( "parent" );
        }

        try
        {
            ClassLoader classloader = createClassLoader( parent, descriptor );
            String classname = descriptor.getClassname();
            if( null == classname )
            {
                throw new IllegalArgumentException( "No classname in descriptor: " + descriptor.getURI() );
            }
            return loadPluginClass( classloader, classname );
        }
        catch( IllegalArgumentException iae )
        {
            throw iae;
        }
        catch( Throwable ce )
        {
            final String error =
              "Unable to load the plugin class ["
              + descriptor.getClassname()
              + "].";
            throw new RepositoryException( error, ce );
        }
    }

   /**
    * Creates a plugin descriptor from an artifact.
    *
    * @param uri the reference to the application
    * @return the plugin descriptor
    * @exception IOException if a plugin creation error occurs
    * @exception NullArgumentException if the supplied uri is null
    */
    public Plugin getPluginDescriptor( URI uri )
        throws IOException, NullArgumentException
    {
        if( null == uri )
        {
            throw new NullArgumentException( "uri" );
        }

        Artifact artifact = getArtifact( uri );

        if( !"plugin".equals( artifact.getType() ) )
        {
            final String error =
              "Supplied artifact [" + artifact + "] is not of a 'plugin' type.";
            throw new RepositoryException( error );
        }
        try
        {
            Properties attributes = getAttributes( artifact );
            return new PropertiesPlugin( attributes );
        }
        catch( RepositoryException ce )
        {
            final String error =
              "Unable to resolve a plugin descriptor for ["
              + artifact
              + "].";
            throw new RepositoryException( error, ce );
        }
    }

   /**
    * Create a factory using a supplied class and command line arguments.
    *
    * @param descriptor the plugin descriptor
    * @param classloader the classloader to be used for plugin creation
    * @param clazz the the factory class
    * @param args the command line args
    * @return the plugin instance
    * @exception IOException if a plugin creation error occurs
    * @exception NullArgumentException if the any one of supplied classloader, descriptor,
    *   class arguments are null
    */
    private Object createPlugin( ClassLoader classloader, Plugin descriptor, Class clazz, Object[] args )
        throws IOException, NullArgumentException
    {
        if( null == clazz )
        {
            throw new NullArgumentException( "clazz" );
        }
        if( null == args )
        {
            throw new NullArgumentException( "args" );
        }
        if( null == classloader )
        {
            throw new NullArgumentException( "classloader" );
        }
        if( null == descriptor )
        {
            throw new NullArgumentException( "descriptor" );
        }
        Object[] params = new Object[ args.length + THREE ];
        for( int i=0; i < args.length; i++ )
        {
            params[i] = args[i];
        }
        params[ args.length ] = classloader;
        params[ args.length + 1 ] = descriptor;
        params[ args.length + 2 ] = this;
        return instantiate( clazz, params );
    }

    public Object instantiate( Class clazz, Object[] args ) throws RepositoryException
    {
        checkArgs( args );
        if( null == clazz )
        {
            throw new NullArgumentException( "clazz" );
        }
        Constructor constructor = getSingleConstructor( clazz );
        return instantiate( constructor, args );
    }

    protected Object instantiate( Constructor constructor, Object[] args ) throws RepositoryException
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

        //
        // sweep though the construct arguments one by one and
        // see if we can assign a value based on the supplied args
        //

        for( int i=0; i < classes.length; i++ )
        {
            Class c = classes[i];
            for( int j=0; j < args.length; j++ )
            {
                Object object = args[j];
                if( c.isAssignableFrom( object.getClass() ) )
                {
                    arguments[i] = object;
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
                if( ClassLoader.class.isAssignableFrom( c ) )
                {
                    arguments[i] = constructor.getDeclaringClass().getClassLoader();
                }
                else if( Preferences.class.isAssignableFrom( c ) )
                {
                    arguments[i] = Preferences.userNodeForPackage( c );
                }
                else if( c.isArray() )
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

    private Object newInstance( Constructor constructor, Object[] arguments ) throws RepositoryException
    {
        try
        {
            Object instance = constructor.newInstance( arguments );
            getMonitor().pluginInstantiated( instance );
            return instance;
        }
        catch( InvocationTargetException e )
        {
            final String error =
              "Cannot create an instance of ["
              + constructor.getDeclaringClass().getName()
              + "] due to an invocation failure.";
            Throwable cause = e.getTargetException();
            throw new RepositoryException( error, cause );
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

    private void checkArgs( Object[] args )
    {
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
                  "User supplied constructor argument [" + i + "] is a null value.";
                throw new NullArgumentException( error );
            }
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
     * Returns a classloader based on supplied plugin descriptor.
     * @param base the parent classloader
     * @param descriptor the plugin descriptor
     * @return the classloader
     * @exception IOException if a classloader construction error occurs
     * @exception NullArgumentException if either the base or the descriptor
     *            argument is null.
     */
    private ClassLoader createClassLoader( ClassLoader base, Plugin descriptor )
        throws IOException, NullArgumentException
    {
        if( null == descriptor )
        {
            throw new NullArgumentException( "descriptor" );
        }
        if( null == base )
        {
            throw new NullArgumentException( "base" );
        }

        URI[] apiArtifacts = descriptor.getDependencies( Plugin.API_KEY );
        URL[] apis = getURLs( apiArtifacts  );
        String apiLabel = appendFragment( descriptor.getURI(), "api" );
        ClassLoader api = buildClassLoader( apiLabel, base, apis );

        URI[] spiArtifacts = descriptor.getDependencies( Plugin.SPI_KEY );
        URL[] spis = getURLs( spiArtifacts );
        String spiLabel = appendFragment( descriptor.getURI(), "spi" );
        ClassLoader spi = buildClassLoader( spiLabel, api, spis );

        URI[] impArtifacts = descriptor.getDependencies( Plugin.IMPL_KEY );
        URL[] imps = getURLs( impArtifacts );
        String implLabel = appendFragment( descriptor.getURI(), "impl" );
        ClassLoader classloader = buildClassLoader( implLabel, spi, imps );

        return classloader;
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
            urls[i] = new URL( null, uris[i].toString(), new Handler() );
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
    private ClassLoader buildClassLoader( String label, ClassLoader parent, URL[] urls )
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
            ClassLoader loader = new StandardClassLoader( label, qualified, parent );
            getMonitor().classloaderConstructed( label, loader );
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
    */
    protected Class loadPluginClass( ClassLoader classloader, String classname )
        throws RepositoryException, NullArgumentException
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
            final String error =
              "Could not load factory class[ " + classname + "].";
            throw new RepositoryException( error, e );
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

    /**
     * Return the plugin attributes associated with an artifact.
     * @param artifact the relative artifact from which a .plugin resource will
     *   be resolved to establish the artifact attributes
     * @return the properties associated with the artifact
     * @exception RepositoryException if an error occurs while retrieving
     *   or building the attributes
     * @exception NullArgumentException if the supplied artifact is null
     */
    private Properties getAttributes( Artifact artifact )
        throws RepositoryException, NullArgumentException
    {
        if( null == artifact )
        {
            throw new NullArgumentException( "artifact" );
        }

        try
        {
            URL url = artifact.toURL();
            Properties props = new Properties();
            InputStream input = url.openConnection().getInputStream();
            props.load( input );
            return props;
        }
        catch( ArtifactNotFoundException e )
        {
            throw new RepositoryException( e.getMessage(), e.getCause() );
        }
        catch( RepositoryException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to resolve plugin for ["
              + artifact
              + "].";
            throw new RepositoryException( error, e );
        }
    }

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

    private String appendFragment( URI base, String fragment )
    {
        String path = base.toString();
        return "[" + path + "] (" + fragment + ")";
    }

    private RepositoryMonitorRouter getMonitor()
    {
        return Transit.getInstance().getRepositoryMonitorRouter();
    }

    private static final int THREE = 3;
}
