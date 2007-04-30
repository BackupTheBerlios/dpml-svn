/*
 * Copyright 2005-2007 Stephen J. McConnell.
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

package net.dpml.runtime;

import dpml.util.ElementHelper;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Hashtable;
import java.lang.reflect.Array;

import net.dpml.lang.Buffer;
import net.dpml.lang.DecodingException;
import net.dpml.lang.ServiceRegistry;
import net.dpml.lang.Strategy;
import net.dpml.lang.StrategyHandler;
import net.dpml.lang.PartContentHandler;

import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class PartsDirective
{
    private final PartsDirective m_parent;
    private final Map<String,Strategy> m_parts = new Hashtable<String,Strategy>();
    
   /**
    * Creation of parts datastructure.
    */
    PartsDirective()
    {
        m_parent = null;
    }
    
    PartsDirective( 
      ClassLoader classloader, 
      Element element, Resolver resolver, String partition ) throws DecodingException
    {
        this( null, classloader, element, resolver, partition );
    }
    
    PartsDirective( 
      PartsDirective parent, ClassLoader classloader, 
      Element element, Resolver resolver, String partition ) throws DecodingException
    {
        m_parent = parent;
        if( null != element )
        {
            Element[] elements = ElementHelper.getChildren( element );
            for( Element child : elements ) 
            {
                boolean implicit = false;
                String key = ElementHelper.getAttribute( child, "key" );
                Strategy strategy = buildStrategy( classloader, child, resolver, partition );
                if( null == key )
                {
                    implicit = true;
                    key = strategy.getName();
                }
                if( m_parts.containsKey( key ) )
                {
                    if( implicit )
                    {
                        final String error = 
                          "Strategy declaration resolving to the implicit key ["
                          + key
                          + "] cannot be added because the key has already been assigned.";
                        throw new DecodingException( error, null, child );
                    }
                    else
                    {
                        final String error = 
                          "Strategy declaration references a duplicate key ["
                          + key
                          + "].";
                        throw new DecodingException( error, null, child );
                    }
                }
                else
                {
                    m_parts.put( key, strategy );
                }
            }
        }
    }
    
    private Strategy buildStrategy( ClassLoader classloader, 
      Element element, Resolver resolver, String partition ) throws DecodingException
    {
        String name = element.getLocalName();
        if( name.equals( "part" ) )
        {
            URI codebase = getCodebase( element, resolver );
            try
            {
                ComponentStrategy strategy = 
                  (ComponentStrategy) Strategy.load( classloader, null, codebase, partition );
                strategy.setElement( element );
                return strategy;
            }
            catch( Exception e )
            {
                final String error = 
                  "Unable to create strategy from nested reference ["
                  + codebase
                  + "].";
                throw new DecodingException( error, e, element );
            }
        }
        else
        {
            return buildLocalStrategy( classloader, element, resolver, partition );
        }
    }
    
    private Strategy buildLocalStrategy( 
      ClassLoader classloader, Element element, Resolver resolver, String partition ) 
      throws DecodingException
    {
        try
        {
            StrategyHandler handler = PartContentHandler.getStrategyHandler( element );
            return handler.build( classloader, element, resolver, partition, null, true );
        }
        catch( DecodingException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            if( null != partition )
            {
                final String error = 
                  "Error building part strategy in partition [" 
                  + partition
                  + "]";
                throw new DecodingException( error, e, element );
            }
            else
            {
                final String error = 
                  "Error building part strategy.";
                throw new DecodingException( error, e, element );
            }
        }
    }
    
    void initialize( ServiceRegistry registry )
    {
        if( null != m_parent )
        {
            m_parent.initialize( registry );
        }
        for( Strategy strategy : m_parts.values() )
        {
            strategy.initialize( registry );
        }
    }
    
    int size()
    {
        return m_parts.size();
    }
    
    String[] getKeys()
    {
        return m_parts.keySet().toArray( new String[0] );
    }
    
    Strategy getStrategy( String key )
    {
        Strategy strategy = m_parts.get( key );
        if( null != strategy )
        {
            return strategy;
        }
        else if( null != m_parent )
        {
            return m_parent.getStrategy( key );
        }
        else
        {
            return null;
        }
    }
    
    String[] getAllKeys()
    {
        ArrayList<String> list = new ArrayList<String>();
        if( null != m_parent )
        {
            String[] keys = m_parent.getAllKeys();
            for( String key : keys )
            {
                if( !list.contains( key ) )
                {
                    list.add( key );
                }
            }
        }
        for( String key : getKeys() )
        {
            if( !list.contains( key ) )
            {
                list.add( key );
            }
        }
        return list.toArray( new String[0] );
    }
    
   /**
    * Returns an array of type T where the contents of the array 
    * will be composed of all parts returning a non-null value
    * from getContentForClass( T ).
    * @param type the component type of the returned array
    * @return an array of values matching the type selection criteria
    * @exception IOException if a content resolution error occurs
    */
    @SuppressWarnings( "unchecked" )
    <T>T[] select( Class<T> type ) throws IOException
    {
        return select( type, null );
    }
    
   /**
    * Returns an array of type T where the contents of the array 
    * will be composed of all parts returning a non-null value
    * from getContentForClass( c ).
    * @param type the component type of the returned array
    * @param criteria the content type selection constraint
    * @return an array of values matching the type selection criteria
    * @exception IOException if a content resolution error occurs
    */
    @SuppressWarnings( "unchecked" )
    <T>T[] select( Class<T> type, Class criteria ) throws IOException
    {
        Strategy[] strategies = getAllStrategies();
        
        ArrayList<T> list = new ArrayList<T>();
        for( Strategy strategy : strategies )
        {
            if( null != criteria )
            {
                if( strategy.isaCandidate( criteria ) )
                {
                    T value = strategy.getInstance( type );
                    list.add( value );
                }
            }
            else
            {
                T value = strategy.getContentForClass( type );
                if( null != value )
                {
                    list.add( value );
                }
            }
        }
        T[] result = (T[]) Array.newInstance( type, list.size() );
        int n = list.size();
        for( int i=0; i<n; i++ )
        {
            result[i] = list.get( i );
        }
        return result;
    }
    
    Strategy[] getAllStrategies()
    {
        return getAllStrategies( true );
    }
    
    Strategy[] getAllStrategies( boolean sort )
    {
        ArrayList<Strategy> list = new ArrayList<Strategy>();
        if( null != m_parent )
        {
            Strategy[] values = m_parent.getAllStrategies( sort );
            for( Strategy strategy : values )
            {
                list.add( strategy );
            }
        }
        Collection<Strategy> collection = m_parts.values();
        for( Strategy strategy : collection )
        {
            list.add( strategy );
        }
        Strategy[] result = list.toArray( new Strategy[0] );
        if( sort )
        {
            Arrays.sort( result );
        }
        return result;
    }
    
    public void encode( Buffer buffer ) throws IOException
    {
        if( size() > 0 )
        {
            buffer.nl( "<parts>" );
            Buffer b2 = buffer.indent();
            for( String key : getKeys() )
            {
                Strategy strategy = getStrategy( key );
                strategy.encode( b2, key );
            }
            buffer.nl( "</parts>" );
        }
    }

    private static URI getCodebase( Element element, Resolver resolver ) throws DecodingException
    {
        String spec = ElementHelper.getAttribute( element, "uri", null, resolver );
        if( null == spec )
        {
            final String error = 
              "Mising uri attribute.";
            throw new DecodingException( error, element );
        }
        
        Element[] params = ElementHelper.getChildren( element, "param" );
        for( int i=0; i<params.length; i++ )
        {
            Element e = params[i];
            String key = ElementHelper.getAttribute( e, "key", null, resolver );
            String value = ElementHelper.getAttribute( e, "value", null, resolver );
            if( i==0 )
            {
                spec = spec + "?" + key + "=" + value;
            }
            else
            {
                spec = spec + "&" + key + "=" + value;
            }
        }
        return URI.create( spec );
    }
}
