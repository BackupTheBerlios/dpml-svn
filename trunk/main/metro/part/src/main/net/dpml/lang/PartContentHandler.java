/*
 * Copyright 2006 Stephen J. McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.lang;

import dpml.lang.Info;
import dpml.lang.Classpath;
import dpml.lang.Part;
import dpml.lang.DOM3DocumentBuilder;

import dpml.util.DefaultLogger;
import dpml.util.SimpleResolver;
import dpml.util.ElementHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.lang.management.ManagementFactory;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Hashtable;
import java.util.ServiceLoader;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.InstanceAlreadyExistsException;

import net.dpml.annotation.Component;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceFactory;

import net.dpml.lang.PartManager;
import net.dpml.lang.PartContentManager;

import net.dpml.runtime.ComponentStrategyHandler;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.ContentHandler;

import net.dpml.util.Logger;
import net.dpml.util.Resolver;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.TypeInfo;

/**
 * Content handler for the 'part' artifact type.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class PartContentHandler extends ContentHandler implements PartContentManager, ApplianceFactory
{
   /**
    * Part XSD uri.
    */
    public static final String NAMESPACE = "dpml:part";

    private static final String PART_CONTENT_TYPE = "part";
    
    private static final Logger LOGGER = new DefaultLogger( "dpml.part" );
    
    // DOES NOT WORK
    //private static final Map<String, WeakReference<Part>> CACHE = 
    //  new Hashtable<String, WeakReference<Part>>();

    // POTENTIAL SOLUTION: map of anchor classloader (keys) to a map of part classloader and parts
    //private static final Map<ClassLoader,Map<ClassLoader,Part>> CACHE = 
    //  new Hashtable<ClassLoader,Map<ClassLoader,Part>>();
    
    private static final Map<String,Part> CACHE = new Hashtable<String,Part>();

    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = 
      new DOM3DocumentBuilder();
    
    private static final ClassLoaderHelper CLASSLOADER_HELPER = new ClassLoaderHelper();

    private static final ComponentStrategyHandler STANDARD_STRATEGY_HANDLER = 
      new ComponentStrategyHandler();

   /**
    * Return a strategy handler based on the supplied component annotation.
    * @param annotation the component annotation
    * @return the strategy handler
    * @exception Exception if an error ocurrs during handler establishment
    */
    public static StrategyHandler getStrategyHandler( Component annotation ) throws Exception
    {
        String classname = annotation.handlerClassname();
        try
        {
            return getStrategyHandler( classname );
        }
        catch( UnknownServiceException e )
        {
            String urn = annotation.handlerURI();
            URI uri = new URI( urn );
            return getStrategyHandler( uri );
        }
    }
    
   /**
    * Return the strategy handler supporting the supplied class.  If the class 
    * contains the component annotation the handler is resolved relative to the 
    * annotation properties, otherwise a default strategy handler is returned.
    *
    * @param subject the subject class
    * @return the strategy handler
    * @exception Exception if a general loading error occurs
    */
    public static StrategyHandler getStrategyHandler( Class<?> subject ) throws Exception
    {   
        if( subject.isAnnotationPresent( Component.class ) )
        {
            Component annotation = 
              subject.getAnnotation( Component.class );
            return PartContentHandler.getStrategyHandler( annotation );
        }
        else
        {
            return STANDARD_STRATEGY_HANDLER;
        }
    }
    
   /**
    * Load a potentially foreign strategy handler.
    * Strategy resolution is based on the following rules relative to the namespace
    * of the supplied element:
    * <ul>
    *  <li>if a system property named <tt>handler:[namespace]</tt> is defined, 
    *     it is assumed to be a handler classname and the implementation will 
    *     load the class and instantiate the handler instance.</li>
    *  <li>if the namespace is recognized as a standard strategy the appropriate 
    *     strategy handler is returned</li>
    *  <li>the implementation will evaluate the <tt<handler</tt> attribute on the
    *     supplied element - if the attribute value is not null, then 
    *     the values will interprited as either a classname or a uri.  If the 
    *     value contains the ':' character the value will be interprited as a 
    *     uri to a part definition from which a strategy handler can be established, 
    *     otherwise, the implementation assumes the the value is a strategy handler
    *     classname resolvable form the current context classloader</li>
    * </ul>
    *
    * @param element the strategy element
    * @exception Exception if loading error occurs
    */
    public static StrategyHandler getStrategyHandler( Element element ) throws Exception
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        String override = System.getProperty( "handler:" + namespace );
        if( null != override )
        {
            return getStrategyHandler( override );
        }
        if( AntlibStrategyHandler.NAMESPACE.equals( namespace ) )
        {
            return new AntlibStrategyHandler();
        }
        else if( ComponentStrategyHandler.NAMESPACE.equals( namespace ) )
        {
            return new ComponentStrategyHandler();
        }
        else
        {
            String urn = ElementHelper.getAttribute( element, "handler" );
            if( null != urn )
            {
                if( urn.indexOf( ":" ) > -1 )
                {
                    URI uri = new URI( urn );
                    return getStrategyHandler( uri );
                }
                else
                {
                    return getStrategyHandler( urn );
                }
            }
            else
            {
                try
                {
                    URI uri = getURIFromNamespaceURI( namespace );
                    return getStrategyHandler( uri );
                }
                catch( Exception e )
                {
                    final String error = 
                      "Cannot resolve strategy handler for element.";
                    throw new DecodingException( error, element );
                }
            }
        }
    }

   /**
    * Load a strategy handler.  The implementation will attempt to resolve a 
    * part defintion from the supplied uri, caching a reference to
    * the handler, and returning the strategy handler instance.
    *
    * @param uri the part handler part uri
    * @exception Exception if part loading error occurs
    */
    static StrategyHandler getStrategyHandler( URI uri ) throws Exception
    {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( ClassLoader.getSystemClassLoader() ); // TODO
        try
        {
            Strategy strategy = Strategy.load( null, null, uri, null );
            StrategyHandler handler = strategy.getContentForClass( StrategyHandler.class );
            if( null == handler )
            {
                final String error = 
                  "URI does not resolve to a strategy handler ["
                  + uri
                  + "]";
                throw new ServiceError( error );
            }
            else
            {
                return handler;
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( context );
        }
    }
    
    static StrategyHandler getStrategyHandler( String classname ) throws Exception
    {
        ServiceLoader<StrategyHandler> handlers = ServiceLoader.load( StrategyHandler.class );
        for( StrategyHandler handler : handlers )
        {
            if( handler.getClass().getName().equals( classname ) )
            {
                return handler;
            }
        }
        throw new UnknownServiceException( classname );
    }
    
    //--------------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------------
    
    public PartContentHandler() throws Exception
    {
        String flag = System.getProperty( "dpml.jmx.enabled", "false" );
        if( "true".equals( flag ) )
        {
            try
            {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                Hashtable<String,String> table = new Hashtable<String,String>();
                table.put( "type", "Parts" );
                ObjectName name =
                  ObjectName.getInstance( "net.dpml.transit", table );
                server.registerMBean( this, name );
            }
            catch( InstanceAlreadyExistsException e )
            {
                //e.printStackTrace();
            }
        }
    }
    
    //--------------------------------------------------------------------------------
    // ContentHandler
    //--------------------------------------------------------------------------------

   /**
    * Returns the type tha the content handler supports.
    * @return the content type name
    */
    public String getType()
    {
        return PART_CONTENT_TYPE;
    }
    
   /**
    * Returns the content in the form of a {@link net.dpml.lang.Strategy} datatype.
    * @param connection the url connection
    * @return the part datatype
    * @exception IOException if an IO error occurs
    */
    public Object getContent( URLConnection connection ) throws IOException
    {
        Part part = getPartContent( connection );
        return part.getStrategy();
    }
    
   /**
    * Returns the content assignable to the first recognized class in the list
    * os supppied classes.  If the class array is empty the part datatype is returned.
    * If none of the classes are recognized, null is returned.
    * @param connection the url connection
    * @param classes the selection class array
    * @return the resolved instance
    * @exception IOException if an IO error occurs
    */
    public Object getContent( URLConnection connection, Class[] classes ) throws IOException
    {
        Part part = getPartContent( connection );
        return getContentForClasses( part, classes );
    }
    
    //--------------------------------------------------------------------------------
    // ApplianceFactory
    //--------------------------------------------------------------------------------
    
   /**
    * Create a new appliance using the supplied connection object.
    * @param connection the URL connection
    * @param partition an optional partition name
    * @return the appliance
    * @exception IOException if an IO error occurs
    */
    public Appliance newAppliance( URLConnection connection, String partition ) throws IOException
    {
        Part part = getPartContent( connection, partition );
        return getContentForClass( part, Appliance.class );
    }
    
    //--------------------------------------------------------------------------------
    // ContentManager
    //--------------------------------------------------------------------------------
    
    public PartManager[] getPartManagers()
    {
        ArrayList<PartManager> list = new ArrayList<PartManager>();
        Part[] parts = CACHE.values().toArray( new Part[0] );
        for( Part part : parts )
        {
            if( part instanceof PartManager )
            {
                PartManager manager = (PartManager) part;
                list.add( manager );
            }
        }
        return list.toArray( new PartManager[0] );
    }
    
    //--------------------------------------------------------------------------------
    // internals
    //--------------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return LOGGER;
    }
    
   /**
    * Return a resolved value given a supplied part instance and class.
    * @param part the part to resolve against
    * @param c the return type
    * @return the resolved value
    * @exception IOException if an error occurs
    */
    private <T>T getContentForClass( Part part, Class<T> c ) throws IOException
    {
        if( Part.class == c )
        {
            return c.cast( part );
        }
        Strategy strategy = part.getStrategy();
        return strategy.getContentForClass( c );
    }
    
    private Object getContentForClasses( Part part, Class[] classes ) throws IOException
    {
        Strategy strategy = part.getStrategy();
        for( Class<?> c : classes )
        {
            Object content = strategy.getContentForClass( c );
            if( null != content )
            {
                return content;
            }
        }
        return null;
    }
    
    static Part getPartContent( URLConnection connection ) throws IOException
    {
        return getPartContent( connection, null, true );
    }
    
    static Part getPartContent( URLConnection connection, String name ) throws IOException
    {
        return getPartContent( connection, name, true );
    }
    
    static Part getPartContent( URLConnection connection, String name, boolean cache ) throws IOException
    {
        return getPartContent( null, connection, name, cache );
    }
    
    static Part getPartContent( ClassLoader anchor, URLConnection connection, String name, boolean cache ) throws IOException
    {
        ClassLoader classloader = getAnchorClassLoader( anchor );
        URL url = connection.getURL();
        try
        {
            String key = buildKey( classloader, url );
            if( cache )
            {
                //WeakReference ref = CACHE.get( key );
                //if( null != ref )
                //{
                //    Part part = (Part) ref.get();
                    //if( null != part )
                    //{
                    //    if( getLogger().isTraceEnabled() )
                    //    {
                    //        getLogger().trace( "located cached part: " + url );
                    //    }
                    //    return part;
                    //}
                    //else
                    //{
                    //    if( getLogger().isTraceEnabled() )
                    //    {
                    //        getLogger().trace( "located disgarded ref: " + key );
                    //    }
                    //}
                //}
                
                Part part = CACHE.get( key );
                if( null != part )
                {
                    return part;
                }
                else
                {
                    // otherwise we need to build it
                    
                    part = buildPart( classloader, connection, name, true );
                    //if( part instanceof PartManager )
                    //{
                    //    // register it with the mbean server
                    //}
                    
                    //WeakReference<Part> reference = new WeakReference<Part>( part );
                    //CACHE.put( key, reference );
                    CACHE.put( key, part );
                    if( LOGGER.isTraceEnabled() )
                    {
                        LOGGER.trace( "caching part" 
                          + "\n  url: " + url
                          + "\n  key: " + key ); 
                    }
                    return part;
                }
            }
            else
            {
                if( LOGGER.isTraceEnabled() )
                {
                    LOGGER.trace( "building new part: " + url );
                }
                return buildPart( classloader, connection, name, true );
            }
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( NoClassDefFoundError e )
        {
            LOGGER.error( 
              e.toString() 
              + "\n" 
              + classloader.toString() );
            throw e;
        }
        catch( Exception e )
        {
            final String error = "Unexpected error in part handling: " + url;
            IOException ioe = new IOException();
            ioe.initCause( e );
            throw ioe;
        }
    }
    
    private static Part buildPart( 
      ClassLoader anchor, URLConnection connection, String name, boolean validate ) throws Exception
    {
        URL url = connection.getURL();
        if( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( 
              "building part ["
              + url
              + "] with anchor ["
              + System.identityHashCode( anchor )
              + "]" );
        }
        
        Document document = DOCUMENT_BUILDER.parse( connection );
        final Element element = document.getDocumentElement();
        TypeInfo type = element.getSchemaTypeInfo();
        String namespace = type.getTypeNamespace();
        if( isNamespaceRecognized( namespace ) )
        {
            URI uri = url.toURI();
            Resolver resolver = new SimpleResolver();
            Info info = getInfo( uri, element );
            Classpath classpath = getClasspath( element );
            ClassLoader classloader = CLASSLOADER_HELPER.newClassLoader( anchor, uri, classpath );
            ClassLoader context = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( classloader );
            try
            {
                Element elem = getStrategyElement( element );
                StrategyHandler handler = getStrategyHandler( elem );
                String query = url.getQuery();
                Strategy strategy = handler.build( classloader, elem, resolver, name, query, validate );
                if( LOGGER.isTraceEnabled() )
                {
                    LOGGER.trace( 
                      "establised new part using [" 
                      + strategy.getClass().getName() 
                      + "]" );
                }
                return new Part( info, classpath, strategy );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( context );
            }
        }
        else
        {
            final String error = 
              "Part namespace not recognized."
              + "\nFound: " + namespace
              + "\nExpecting: " + PartContentHandler.NAMESPACE;
            throw new DecodingException( error, element );
        }
    }
    
    private static boolean isNamespaceRecognized( String namespace )
    {
        if( NAMESPACE.equals( namespace ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private static Info getInfo( URI uri, Element root )
    {
        Element element = ElementHelper.getChild( root, "info" );
        if( null == element )
        {
            return new Info( uri, null, null );
        }
        String title = ElementHelper.getAttribute( element, "title" );
        Element descriptionElement = ElementHelper.getChild( element, "description" );
        String description = ElementHelper.getValue( descriptionElement );
        return new Info( uri, title, description );
    }

   /**
    * Construct the classpath defintion.
    * @param root the element containing a 'classpath' element.
    * @return the classpath definition
    * @exception DecodingException if an error occurs during element evaluation
    */
    private static Classpath getClasspath( Element root ) throws DecodingException
    {
        // TODO: update to support different classpath defintions (e.g. 277 module scenario)
        
        Element classpath = ElementHelper.getChild( root, "classpath" );
        if( null == classpath )
        {
            return new Classpath();
        }
        
        try
        {
            Element[] children = ElementHelper.getChildren( classpath );
            URI[] sys = buildURIs( classpath, "system" );
            URI[] pub = buildURIs( classpath, "public" );
            URI[] prot = buildURIs( classpath, "protected" );
            URI[] priv = buildURIs( classpath, "private" );
            return new Classpath( sys, pub, prot, priv );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to decode classpath due to an unexpected error.";
            throw new DecodingException( error, e, classpath );
        }
    }
    
    private static URI[] buildURIs( Element classpath, String key ) throws Exception
    {
        Element category = ElementHelper.getChild( classpath, key );
        Element[] children = ElementHelper.getChildren( category, "uri" );
        URI[] uris = new URI[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            String value = ElementHelper.getValue( child );
            uris[i] = new URI( value );
        }
        return uris;
    }
    
    // cache utils
    
    private static String buildKey( ClassLoader classloader, URL url ) throws IOException
    {
        try
        {
            int n = System.identityHashCode( classloader );
            return "" + n + "#" + url.toURI().toASCIIString();
        }
        catch( Exception e )
        {
            final String error = "Internal error while resolving part key from url: " + url;
            IOException ioe = new IOException();
            ioe.initCause( e );
            throw ioe;
        }
    }

    static ClassLoader getAnchorClassLoader( ClassLoader parent )
    {
        if( null != parent )
        {
            return parent;
        }
        else
        {
            return Strategy.class.getClassLoader();
            //ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            //if( null == classloader )
            //{
            //    return getClass().getClassLoader();
            //}
            //else
            //{
            //    return classloader;
            //}
        }
    }

    private static Element getStrategyElement( Element root ) throws DecodingException
    {
        // TODO: update this to select the strategy element based on type overwise
        // we risk issues when dealing with a non-standard classpath element
                
        Element[] children = ElementHelper.getChildren( root );
        for( Element element : children )
        {
            String name = element.getLocalName();
            if( !name.equals( "info" ) && !name.equals( "classpath" ) )
            {
                return element;
            }
        }
        final String error = 
          "Missing part strategy element.";
        throw new DecodingException( error, root );
    }

   /**
    * Resolve the part handler given an element namespace.
    * @param urn the namespace value
    * @return the decoder uri
    * @exception Exception if an error occurs
    */
    private static URI getURIFromNamespaceURI( String urn ) throws Exception
    {
        URI raw = new URI( urn );
        Artifact artifact = Artifact.createArtifact( raw );
        String group = artifact.getGroup();
        String name = artifact.getName();
        String path = "link:part:" + group + "/" + name;
        Artifact link = Artifact.createArtifact( path );
        return link.toURI();
    }
}