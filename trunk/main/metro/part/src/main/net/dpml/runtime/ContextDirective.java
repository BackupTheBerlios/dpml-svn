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

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Hashtable;
import java.beans.Expression;
import java.lang.reflect.Constructor;

import dpml.lang.Construct;
import dpml.lang.ValueDecoder;

import dpml.lang.Value;
import net.dpml.lang.DecodingException;
import net.dpml.lang.Buffer;

import net.dpml.runtime.Directive.Resolvable;
import net.dpml.runtime.Directive.Encodable;

import dpml.util.ElementHelper;
import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ContextDirective extends Directive implements Encodable 
{
    private static final ValueDecoder VALUE_DECODER = new ValueDecoder();
    
    private final Element m_element;
    
    private final Map<String,Encodable> m_entries = new Hashtable<String,Encodable>();
    
   /**
    * Creation of a new top-level context argument.
    */
    ContextDirective()
    {
        m_element = null;
    }
    
   /**
    * Creation of a new context argument resolved from a packaged context definition.
    */
    ContextDirective( ClassLoader classloader, Element element, Resolver resolver ) throws DecodingException
    {
        m_element = element;
        
        if( null != element )
        {
            Element[] elements = ElementHelper.getChildren( element );
            for( Element child : elements )
            {
                String key = ElementHelper.getAttribute( child, "key" );
                if( null == key )
                {
                    final String error = 
                      "Context entry does not declare a key.";
                    throw new DecodingException( error, null, child );
                }
                if( "entry".equals( child.getLocalName() ) )
                {
                    String service = ElementHelper.getAttribute( child, "lookup" );
                    if( null == service )
                    {
                        Value value = VALUE_DECODER.decodeValue( child, resolver );
                        ValueWrapper wrapper = new ValueWrapper( child, value );
                        m_entries.put( key, wrapper );
                    }
                    else
                    {
                        Lookup loookup = new Lookup( classloader, child, service );
                        m_entries.put( key, loookup );
                    }
                }
                else if( "context".equals( child.getLocalName() ) )
                {
                    ContextDirective directive = new ContextDirective( classloader, child, resolver );
                    m_entries.put( key, directive );
                }
                else if( "map".equals( child.getLocalName() ) )
                {
                    Map<String,Value> map = buildMapValue( child, resolver );
                    String classname = ElementHelper.getAttribute( child, "class", null, resolver );
                    MapWrapper wrapper = new MapWrapper( classloader, child, map, classname );
                    m_entries.put( key, wrapper );
                }
                else
                {
                    final String error = 
                      "Context entry element not recognized.";
                    throw new ComponentException( error, null, child );
                }
            }
        }
    }
    
    private Map<String,Value> buildMapValue( Element element, Resolver resolver ) throws ComponentException
    {
        Map<String,Value> map = new Hashtable<String,Value>();
        Element[] elements = ElementHelper.getChildren( element );
        for( Element elem : elements )
        {
            String key = ElementHelper.getAttribute( elem, "key", null, resolver );
            if( null == key )
            {
                final String error = 
                  "Map entry does not declare a key.";
                throw new ComponentException( error, null, elem );
            }
            Value value = VALUE_DECODER.decodeValue( elem, resolver );
            map.put( key, value );
        }
        return map;
    }
    
    int size()
    {
        return m_entries.size();
    }
    
    public Element getElement()
    {
        return m_element;
    }
    
    public void encode( Buffer buffer, String key ) throws IOException
    {
        if( null != m_element )
        {
            buffer.nl( "<context" );
            if( null != key )
            {
                buffer.write( " key=\"" + key + "\"" );
            }
            buffer.write( ">" );
            Buffer b2 = buffer.indent();
            for( String k : m_entries.keySet().toArray( new String[0] ) )
            {
                Encodable entry = m_entries.get( k );
                entry.encode( b2, k );
            }
            buffer.nl( "</context>" );
        }
    }

    Resolvable getEntry( String key )
    {
        Encodable entry = m_entries.get( key );
        if( null != entry )
        {
            if( entry instanceof Resolvable )
            {
                return (Resolvable) entry;
            }
        }
        return null;
    }
    
    ContextDirective getContextDirective( String key )
    {
        Encodable entry = m_entries.get( key );
        if( null != entry )
        {
            if( entry instanceof ContextDirective )
            {
                return (ContextDirective) entry;
            }
        }
        return null;
    }

    private static abstract class AbstractResolvable implements Resolvable, Encodable
    {
        private final Element m_element;
        
        AbstractResolvable( Element element )
        {
            m_element = element;
        }
        
        public Element getElement()
        {
            return m_element;
        }
    }
    
    static class ValueWrapper extends AbstractResolvable
    {
        private final Value m_value;
        
        ValueWrapper( Value value )
        {
            this( null, value );
        }
        
        ValueWrapper( Element element, Value value )
        {
            super( element );
            m_value = value;
        }
        
        public <T>T resolve( ComponentStrategy parent, Class<T> type ) throws Exception
        {
            Object value = m_value.resolve( type, parent.getContextMap() );
            return type.cast( value );
        }
        
        public void encode( Buffer buffer, String key ) throws IOException
        {
            m_value.encode( buffer, "entry", key );
        }
    }
    
    static class MapWrapper extends AbstractResolvable
    {
        private final Map<String,Value> m_values;
        private final String m_classname;
        private final ClassLoader m_classloader;
        
        MapWrapper( ClassLoader classloader, Element element, Map<String,Value> values, String classname )
        {
            super( element );
            m_values = values;
            m_classname = classname;
            m_classloader = classloader;
        }
        
        @SuppressWarnings( "unchecked" )
        public <T>T resolve( ComponentStrategy parent, Class<T> type ) throws Exception
        {
            Map map = new Hashtable();
            for( String k : m_values.keySet() )
            {
                Value v = m_values.get( k );
                Object o = v.resolve( parent.getContextMap() );
                map.put( k, o );
            }
            if( null == m_classname )
            {
                return type.cast( map );
            }
            else
            {
                Class c = m_classloader.loadClass( m_classname );
                Constructor<T> constructor = c.getConstructor( Map.class );
                return constructor.newInstance( map );
            }
        }
        
        public void encode( Buffer buffer, String key ) throws IOException
        {
            buffer.nl( "<map key=\"" + key + "\"" );
            if( null != m_classname )
            {
                buffer.write( " class=\"" + m_classname + "\"" );
            }
            buffer.nl( ">" );
            for( String k : m_values.keySet() )
            {
                Value v = m_values.get( k );
                v.encode( buffer, "entry", k );
            }
            buffer.nl( "</map>" );
        }
    }
    
    static class Lookup extends AbstractResolvable
    {
        private final Class m_class;
        
        Lookup( ClassLoader classloader, Element element, String classname ) throws ComponentException
        {
            super( element );
            try
            {
                m_class = classloader.loadClass( classname );
            }
            catch( ClassNotFoundException cnfe )
            {
                final String error = 
                  "Class not found: ["
                  + classname 
                  + "].";
                throw new ComponentException( error, cnfe, element );
            }
        }
        
        public <T>T resolve( ComponentStrategy strategy, Class<T> type ) throws Exception
        {
            T value = strategy.getService( m_class, type );
            if( null != value )
            {
                return value;
            }
            else
            {
                Element element = getElement();
                final String error = 
                  "Service not found [" 
                  + m_class.getName() 
                  + "].";
                throw new ComponentException( error, null, element );
            }
        }
        
        public void encode( Buffer buffer, String key ) throws IOException
        {
            buffer.nl( "<entry key=\"" + key + "\" lookup=\"" + m_class.getName() + "\"/>" );
        }
    }
}