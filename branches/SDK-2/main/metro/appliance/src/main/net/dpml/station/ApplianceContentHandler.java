/*
 * Copyright 2006 Stephen McConnell.
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

package net.dpml.station;

import dpml.lang.DOM3DocumentBuilder;
import dpml.util.SimpleResolver;
import dpml.util.ElementHelper;
import dpml.util.DefaultLogger;
import dpml.station.util.LoggingServer;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.util.Properties;
import java.util.Map;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

import net.dpml.transit.Artifact;
import net.dpml.transit.ContentHandler;
import net.dpml.transit.ContentManager;
import net.dpml.lang.DecodingException;
import net.dpml.util.Logger;
import net.dpml.util.Resolver;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceException;
import net.dpml.appliance.ApplianceManager;
import net.dpml.appliance.ApplianceContentManager;
import net.dpml.appliance.ApplianceFactory;

import dpml.station.info.ApplianceDescriptor;
import dpml.station.info.InfoDescriptor;
import dpml.station.info.ProcessDescriptor;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.TypeInfo;

/**
 * Content handler for the <tt>appliance</tt> artifact type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ApplianceContentHandler extends ContentHandler implements ApplianceContentManager, ApplianceFactory
{
    static final String NAMESPACE = "dpml:station";
    static final String TYPE = "appliance";
    
    private static final Logger LOGGER = new DefaultLogger( "dpml.metro.appliance" );
    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = 
      new DOM3DocumentBuilder();
      
    private static final List<ApplianceManager> MANAGERS = 
      new ArrayList<ApplianceManager>();
      
    private static final ApplianceContentHandler SINGLETON = newApplianceContentHandler();
    
    private static ApplianceContentHandler newApplianceContentHandler()
    {
        ApplianceContentHandler handler = new ApplianceContentHandler();
        String flag = System.getProperty( "dpml.jmx.enabled", "false" );
        if( "true".equals( flag ) )
        {
            try
            {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                Hashtable<String,String> table = new Hashtable<String,String>();
                table.put( "type", "Appliances" );
                ObjectName name =
                  ObjectName.getInstance( "net.dpml.transit", table );
                server.registerMBean( handler, name );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        return handler;
    }
    
    public ApplianceContentHandler()
    {
        this( LOGGER );
    }
    
    protected ApplianceContentHandler( Logger logger )
    {
        logger.debug( "instantiating" );
    }
    
    /*
    protected MBeanServerConnection getMBeanServerConnection() throws MalformedURLException, IOException
    {
        String spec = System.getProperty( Station.STATION_JMX_URI_KEY );
        if( null != spec )
        {
            JMXServiceURL url = new JMXServiceURL( spec );
            JMXConnector connector = JMXConnectorFactory.connect( url, null ); 
            return connector.getMBeanServerConnection();
        }
        else
        {
            return ManagementFactory.getPlatformMBeanServer();
        }
    }
    */
    
    public ApplianceManager[] getApplianceManagers()
    {
        return MANAGERS.toArray( new ApplianceManager[0] );
    }
    
   /**
    * Returns the type thar the content handler supports.
    * @return the content type name
    */
    public String getType()
    {
        return TYPE;
    }
    
   /**
    * Returns the content in the form of a {@link net.dpml.appliance.Appliance}.
    * @param connection the url connection
    * @return the application handler
    * @exception IOException if an IO error occurs
    */
    public Object getContent( URLConnection connection ) throws IOException
    {
        return newAppliance( connection, null );
    }
    
   /**
    * Returns the content assignable to the first recognized class in the list
    * of suppied classes.  If the class array is empty the application handler is returned.
    * If none of the classes are recognized, null is returned.
    * @param connection the url connection
    * @param classes the selection class array
    * @return the resolved instance
    * @exception IOException if an IO error occurs
    */
    public Object getContent( URLConnection connection, Class[] classes ) throws IOException
    {
        ApplianceDescriptor descriptor = getApplianceDescriptor( connection );
        return getContentForClasses( descriptor, classes );
    }
    
   /**
    * Create a new appliance using the supplied connection object.
    * @param connection the URL connection
    * @param partition an optional partition name
    * @return the appliance
    * @exception IOException if an IO error occurs
    */
    public Appliance newAppliance( URLConnection connection, String partition ) throws IOException
    {
        ApplianceDescriptor descriptor = getApplianceDescriptor( connection );
        return newAppliance( partition, descriptor );
    }

    static <T>T getContentForClass( URLConnection connection, Class<T> type ) throws IOException
    {
        ApplianceDescriptor descriptor = getApplianceDescriptor( connection );
        return getContentForClass( descriptor, type );
    }
    
    private static Object getContentForClasses( ApplianceDescriptor descriptor, Class<?>[] classes ) throws IOException
    {
        for( Class<?> c : classes )
        {
            Object value = getContentForClass( descriptor, c );
            if( null != value )
            {
                return value;
            }
        }
        return null;
    }
    
    private static <T>T getContentForClass( ApplianceDescriptor descriptor, Class<T> type ) throws IOException
    {
        if( ApplianceDescriptor.class == type )
        {
            return type.cast( descriptor );
        }
        else if( Appliance.class == type )
        {
            String key = "" + System.identityHashCode( descriptor );
            Appliance appliance = newAppliance( key, descriptor );
            return type.cast( appliance );
        }
        else
        {
            return null;
        }
    }
    
    static final Appliance newAppliance( String key, ApplianceDescriptor descriptor ) throws IOException
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        Logger logger = getLogger();
        Application application = new Application( logger, key, descriptor );
        Appliance appliance = application.getAppliance();
        register( key, appliance );
        return appliance;
    }
    
    static void register( String id, Appliance appliance )
    {
        if( appliance instanceof ApplianceManager )
        {
            if( null == id )
            {
                throw new NullPointerException( "id" );
            }
            ApplianceManager manager = (ApplianceManager) appliance;
            if( !MANAGERS.contains( manager ) )
            {
                try
                {
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                    Hashtable<String, String> table = new Hashtable<String, String>();
                    table.put( "type", "Appliances" );
                    table.put( "name", id );
                    ObjectName name =
                      ObjectName.getInstance( "net.dpml.transit", table );
                    server.registerMBean( manager, name );
                    MANAGERS.add( manager );
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    static void deregister( Appliance appliance )
    {
        if( appliance instanceof ApplianceManager )
        {
            ApplianceManager manager = (ApplianceManager) appliance;
            if( MANAGERS.contains( manager ) )
            {
                MANAGERS.remove( manager );
            }
        }
    }
    
    static Appliance newAppliance( String key, URI uri ) throws IOException
    {
        URL url = Artifact.toURL( uri );
        URLConnection connection = url.openConnection();
        ApplianceDescriptor descriptor = getApplianceDescriptor( connection );
        return newAppliance( key, descriptor );
    }
    
    //static final Appliance newLocalAppliance( String partition, URI codebase ) throws IOException
    //{
    //    return new StandardAppliance( LOGGER, partition, codebase );
    //}
    
    private static ApplianceDescriptor getApplianceDescriptor( URLConnection connection ) throws IOException
    {
        URL url = connection.getURL();
        try
        {
            Document document = DOCUMENT_BUILDER.parse( url );
            final Element element = document.getDocumentElement();
            TypeInfo type = element.getSchemaTypeInfo();
            String namespace = type.getTypeNamespace();
            if( NAMESPACE.equals( namespace ) )
            {
                URI uri = URI.create( url.toExternalForm() );
                Resolver resolver = new SimpleResolver();
                return new ApplianceDescriptor( element, resolver, uri );
            }
            else
            {
                final String error = 
                  "ApplianceDescriptor namespace not recognized."
                  + "\nFound: " + namespace
                  + "\nExpecting: " + NAMESPACE;
                throw new DecodingException( error, element );
            }
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = "Unexpected error while constructing application profile: " + url;
            IOException ioe = new IOException();
            ioe.initCause( e );
            throw ioe;
        }
    }
    
    private static Logger getLogger()
    {
        return LOGGER;
    }
}
