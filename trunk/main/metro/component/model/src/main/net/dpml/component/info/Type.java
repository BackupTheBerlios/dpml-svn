/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.component.info;

import java.beans.Encoder;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.io.Serializable;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import net.dpml.configuration.Configuration;

import net.dpml.part.Part;

/**
 * This class contains the meta information about a particular
 * component type. It describes;
 *
 * <ul>
 *   <li>Human presentable meta data such as name, version, description etc
 *   useful when assembling a system.</li>
 *   <li>the context that this component requires</li>
 *   <li>the services that this component type is capable of providing</li>
 *   <li>the services that this component type requires to operate (and the
 *   names via which services are accessed)</li>
 *   <li>information about the component lifestyle</li>
 *   <li>component collection preferences</li>
 * </ul>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class Type implements Serializable
{
    static final long serialVersionUID = 1L;

    private static final Type OBJECT_TYPE = createObjectType();
    
    public static void encode( Type type, OutputStream output ) throws IOException, EncodingException
    {
        XMLEncoder encoder = new XMLEncoder( output );
        encoder.setExceptionListener( new TypeEncoderListener( type ) );
        encoder.setPersistenceDelegate( URI.class, new URIPersistenceDelegate() );
        try
        {
            encoder.writeObject( type );
        }
        finally
        {
            encoder.close();
        }
    }
    
    private static class TypeEncoderListener implements ExceptionListener
    {
        private Type m_type;
        
        private TypeEncoderListener( Type type )
        {
            m_type = type;
        }
        
        public void exceptionThrown( Exception e )
        {
            Throwable cause = e.getCause();
            if( null != cause )
            {
                if( cause instanceof EncodingRuntimeException )
                {
                    EncodingRuntimeException ere = (EncodingRuntimeException) cause;
                    throw ere;
                }
                else
                {
                    final String error = 
                      "An error occured while attempting to encode the type ["
                      + m_type.getInfo().getClassname()
                      + "]\nCause: " + cause.toString();
                    throw new EncodingRuntimeException( error, cause );
                }
            }
            else
            {
                final String error = 
                  "An unexpected error occured while attempting to encode the type ["
                  + m_type.getInfo().getClassname()
                  + "] due to: " + e.toString();
                throw new EncodingRuntimeException( error, e );
            }
        }
    }
    
    private static class EncodingRuntimeException extends RuntimeException
    {
        public EncodingRuntimeException( String message, Throwable cause )
        {
            super( message, cause );
        }
    }
    
    public static Type decode( Class clazz ) throws IOException
    {
        ClassLoader context = Type.class.getClassLoader();
        return decode( context, clazz );
    }

    public static Type decode( ClassLoader context, Class clazz ) throws IOException
    {
        String path = clazz.getName().replace( '.', '/' ) + ".type";
        URL url = clazz.getClassLoader().getResource( path );
        InputStream input = url.openStream();
        try
        {
            return decode( context, input );
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error occured while attempting to load an encoded type."
              + "\nResource path: " + path;
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }
    
    public static Type decode( InputStream input ) throws IOException
    {
        ClassLoader context = Type.class.getClassLoader();
        return decode( context, input );
    }
    
    public static Type decode( ClassLoader context, InputStream input ) throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try
        {
            Class c = context.loadClass( "net.dpml.configuration.impl.DefaultConfiguration" );
        }
        catch( ClassNotFoundException ce )
        {
            final String error = 
              "Context classloader does not include the configuration implementation in the supplied classloader:\n"
              + context;
            throw new IllegalStateException( error );
        }
        try
        {
            Thread.currentThread().setContextClassLoader( context );
            XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
            return (Type) decoder.readObject();
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error occured while attempting to load an encoded type.";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( loader );
            input.close();
        }
    }

    private static class URIPersistenceDelegate extends DefaultPersistenceDelegate
    {
        public Expression instantiate( Object old, Encoder encoder )
        {
            URI uri = (URI) old;
            String spec = uri.toString();
            Object[] args = new Object[]{ spec };
            return new Expression( old, old.getClass(), "new", args );
        }
    }
    
    private final InfoDescriptor m_info;
    private final CategoryDescriptor[] m_categories;
    private final ContextDescriptor m_context;
    private final ServiceDescriptor[] m_services;
    //private final Configuration m_configuration;
    private final PartReference[] m_parts;

   /**
    * Creation of a new Type instance using a supplied component descriptor,
    * logging, context, services, and part references.
    *
    * @param info information about the component type
    * @param loggers a set of logger descriptors the declare the logging channels
    *   required by the type
    * @param context a component context descriptor that declares the context type
    *   and context entry key and value classnames
    * @param services a set of service descriptors that detail the service that
    *   this component type is capable of supplying
    * @param parts an array of part descriptors
    * @exception NullPointerException if the info, loggers, or context is null
    */
    public Type( final InfoDescriptor info,
                 final CategoryDescriptor[] loggers,
                 final ContextDescriptor context,
                 final ServiceDescriptor[] services,
                 //final Configuration defaults,
                 final PartReference[] parts )
            throws NullPointerException 
    {
        if ( null == info )
        {
            throw new NullPointerException( "info" );
        }
        if ( null == loggers )
        {
            throw new NullPointerException( "loggers" );
        }
        if ( null == context )
        {
            throw new NullPointerException( "context" );
        }
        if( null == services )
        {
            m_services = new ServiceDescriptor[0];
        }
        else
        {
            m_services = services;
        }

        m_info = info;
        m_categories = loggers;
        m_context = context;
        //m_configuration = defaults;

        if( null == parts )
        {
            m_parts = new PartReference[0];
        }
        else
        {
            m_parts = parts;
        }
    }

   /**
    * Return the info descriptor.
    *
    * @return the component info descriptor.
    */
    public InfoDescriptor getInfo()
    {
        return m_info;
    }

    /**
     * Return the set of Logger that this Component will use.
     *
     * @return the set of Logger that this Component will use.
     */
    public CategoryDescriptor[] getCategoryDescriptors()
    {
        return m_categories;
    }

    /**
     * Return TRUE if the logging categories includes a category with
     * a matching name.
     *
     * @param name the logging category name
     * @return TRUE if the logging category is declared.
     */
    public boolean isaCategory( String name )
    {
        CategoryDescriptor[] loggers = getCategoryDescriptors();
        for( int i = 0; i < loggers.length; i++ )
        {
            CategoryDescriptor logger = loggers[ i ];
            if( logger.getName().equals( name ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the ContextDescriptor for component.
     *
     * @return the ContextDescriptor for component.
     */
    public ContextDescriptor getContextDescriptor()
    {
        return m_context;
    }

    /**
     * Get the set of service descriptors defining the set of services that
     * the component type exports.
     *
     * @return the array of service descriptors
     */
    public ServiceDescriptor[] getServiceDescriptors()
    {
        return m_services;
    }

    /**
     * Retrieve a service descriptor matching the supplied reference.
     *
     * @param reference a service descriptor to match against
     * @return a matching service descriptor or null if no match found
     */
    public ServiceDescriptor getServiceDescriptor( final ServiceDescriptor reference )
    {
        for ( int i = 0; i < m_services.length; i++ )
        {
            final ServiceDescriptor service = m_services[i];
            if ( service.matches( reference ) )
            {
                return service;
            }
        }
        return null;
    }

    /**
     * Retrieve a service descriptor matching the supplied classname.
     *
     * @param classname the service classname
     * @return the matching service descriptor or null if it does not exist
     */
    public ServiceDescriptor getServiceDescriptor( final String classname )
    {
        for ( int i = 0; i < m_services.length; i++ )
        {
            final ServiceDescriptor service = m_services[i];
            if ( service.getClassname().equals( classname ) )
            {
                return service;
            }
        }
        return null;
    }

    /**
     * Returns the default configuration supplied with the type.
     *
     * @return the default configuration or null if no packaged defaults
     */
    //public Configuration getConfiguration()
    //{
    //    return m_configuration;
    //}

    /**
     * Returns the parts declared by this component type.
     *
     * @return the part descriptors
     */
    public PartReference[] getPartReferences()
    {
        return m_parts;
    }

    /**
     * Retrieve an identified part.
     *
     * @param key the part reference key
     * @return the part or null if the part key is unknown
     */
    public Part getPart( final String key )
    {
        for ( int i = 0; i < m_parts.length; i++ )
        {
            PartReference reference = m_parts[i];
            if( reference.getKey().equals( key ) )
            {
                return reference.getPart();
            }
        }
        return null;
    }

    /**
     * Return a string representation of the type.
     * @return the stringified type
     */
    public String toString()
    {
        return getInfo().toString();
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals(Object other)
    {
        if( null == other )
        {
            return false;
        }
        if( !( other instanceof Type ) )
        {
            return false;
        }
        Type t = (Type) other;
        if( !m_info.equals( t.m_info ) )
        {
            return false;
        }
        /*
        if( null == m_configuration )
        {
            if( !( null == t.m_configuration ) )
            {
                return false;
            }
        }
        else
        {
            if( !m_configuration.equals( t.m_configuration ) )
            {
                return false;
            }
        }
        */
        if( m_parts.length != t.m_parts.length )
        {
            return false;
        }
        else
        {
            for( int i=0; i<m_parts.length; i++ )
            {
                if( ! m_parts[i].equals( t.m_parts[i] ) )
                {
                    return false;
                }
            }
        }
        if( ! m_context.equals( t.m_context ) )
        {
            return false;
        }
        for( int i=0; i<m_categories.length; i++ )
        {
            if( ! m_categories[i].equals( t.m_categories[i] ) )
            {
                return false;
            }
        }
        for( int i=0; i<m_services.length; i++ )
        {
            if( ! m_services[i].equals( t.m_services[i] ) )
            {
                return false;
            }
        }
        return true;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = m_info.hashCode();
        hash ^= m_context.hashCode();
        //if( m_configuration != null )
        //{
        //    hash ^= m_configuration.hashCode();
        //}
        for( int i = 0; i < m_parts.length; i++ )
        {
            hash ^= m_parts[i].hashCode();
            hash = hash - 163611323;
        }
        for( int i = 0; i < m_services.length; i++ )
        {
            hash ^= m_services[i].hashCode();
            hash = hash - 163611323;
        }
        for( int i = 0; i < m_categories.length; i++ )
        {
            hash ^= m_categories[i].hashCode();
            hash = hash + 471312761;
        }
        return hash;
    }

    private static Type createObjectType()
    {
        final InfoDescriptor info = new InfoDescriptor( "object", Object.class.getName() );
        final CategoryDescriptor[] loggers = new CategoryDescriptor[0];
        final ContextDescriptor context = new ContextDescriptor( new EntryDescriptor[0] );
        final ServiceDescriptor[] services = new ServiceDescriptor[0];
        //final Configuration configuration = null;
        final PartReference[] parts = new PartReference[0];
        return new Type( info, loggers, context, services, parts );
    }
}
