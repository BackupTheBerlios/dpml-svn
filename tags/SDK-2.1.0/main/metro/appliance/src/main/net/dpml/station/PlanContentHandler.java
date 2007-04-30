/*
 * Copyright 2007 Stephen McConnell.
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
import dpml.station.info.PlanDescriptor;
import dpml.util.SimpleResolver;
import dpml.util.DefaultLogger;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import net.dpml.appliance.Appliance;
import net.dpml.lang.DecodingException;
import net.dpml.transit.Artifact;
import net.dpml.util.Resolver;
import net.dpml.util.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.TypeInfo;

/**
 * Content handler for the <tt>plan</tt> artifact type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PlanContentHandler extends ApplianceContentHandler //implements ApplianceFactory
{
    private static final Logger LOGGER = new DefaultLogger( "dpml.station.plan" );
    static final String NAMESPACE = "dpml:station";
    static final String TYPE = "plan";
    
    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = 
      new DOM3DocumentBuilder();
    
   /**
    * Creation of a new plan content handler.
    */
    public PlanContentHandler()
    {
        super( LOGGER );
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
        return getContentForClass( connection, Appliance.class );
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
        PlanDescriptor descriptor = getPlanDescriptor( connection );
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
        PlanDescriptor descriptor = getPlanDescriptor( connection );
        String name = descriptor.getName();
        String path = getQualifiedName( partition, name );
        Appliance appliance = new CompositeAppliance( LOGGER, path, descriptor );
        register( path, appliance );
        return appliance;
    }
    
    private static String getQualifiedName( String partition, String name )
    {
        if( null == partition )
        {
            return name;
        }
        else
        {
            return partition + "." + name;
        }
    }
    
    static <T>T getContentForClass( URLConnection connection, Class<T> type ) throws IOException
    {
        PlanDescriptor descriptor = getPlanDescriptor( connection );
        return getContentForClass( descriptor, type );
    }
    
    private static Object getContentForClasses( 
      PlanDescriptor descriptor, Class<?>[] classes ) throws IOException
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
    
    private static <T>T getContentForClass( 
      PlanDescriptor descriptor, Class<T> type ) throws IOException
    {
        if( PlanDescriptor.class == type )
        {
            return type.cast( descriptor );
        }
        else if( Appliance.class == type )
        {
            String name = descriptor.getName();
            CompositeAppliance appliance = new CompositeAppliance( LOGGER, name, descriptor );
            register( name, appliance );
            return type.cast( appliance );
        }
        else
        {
            return null;
        }
    }
    
   /**
    * Creation of a new appliance using a supplied key and appliance uri.
    * @param key the appliance key
    * @param uri an artifact uri referencing an appliance datastructure
    * @return the new appliance
    * @exception IOException if an IO error occurs
    */
    public static Appliance newAppliance( String key, URI uri ) throws IOException
    {
        URL url = Artifact.toURL( uri );
        URLConnection connection = url.openConnection();
        PlanDescriptor descriptor = getPlanDescriptor( connection );
        Appliance appliance = new CompositeAppliance( LOGGER, key, descriptor );
        register( key, appliance );
        return appliance;
    }
    
    private static PlanDescriptor getPlanDescriptor( URLConnection connection ) throws IOException
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
                URI codebase = url.toURI();
                Resolver resolver = new SimpleResolver();
                return new PlanDescriptor( element, resolver, codebase );
            }
            else
            {
                final String error = 
                  "Document namespace not recognized."
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
            final String error = "Unexpected error while constructing scenario: " + url;
            IOException ioe = new IOException();
            ioe.initCause( e );
            throw ioe;
        }
    }
}
