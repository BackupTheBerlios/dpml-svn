/*
 * Copyright 2006 Stephen J. McConnell.
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

import dpml.lang.Value;
import dpml.lang.Construct;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.dpml.runtime.Directive.Resolvable;
import net.dpml.runtime.ContextDirective.ValueWrapper;

/**
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class Query
{
    private final Map<String,Resolvable> m_entries = new Hashtable<String,Resolvable>();
    
    Query( String query )
    {
        this( expand( query ) );
    }
    
    Query( List<Binding> values )
    {
        for( Binding binding : values )
        {
            String key = binding.getKey();
            Resolvable value = binding.getValue();
            m_entries.put( key, value );
        }
    }
    
    public Resolvable getEntry( String key )
    {
        return m_entries.get( key );
    }
    
    private static List<Binding> expand( String query )
    {
        final List<Binding> list = new ArrayList<Binding>();
        if( null == query )
        {
            return list;
        }
        String[] segments = query.split( "&" );
        for( String segment : segments )
        {
            String[] args = segment.split( "=" );
            if( args.length != 2 )
            {
                throw new IllegalArgumentException( 
                  "Invalid query [" + segment + "]" );
            }
            String address = args[0];
            String param = args[1];
            String decoded = decode( param );
            Value value = new Construct( decoded );
            Resolvable entry = new ValueWrapper( value );
            Binding binding = new Binding( address, entry );
            list.add( binding );
        }
        return list;
    }
    
    private static String decode( String param )
    {
        try
        {
            return URLDecoder.decode( param, "UTF8"  );
        }
        catch( UnsupportedEncodingException uee )
        {
            // will not happen
            uee.printStackTrace();
            return param;
        }
    }
    
    private static class Binding
    {
        private final String m_key;
        private final Resolvable m_value;
        
        Binding( String key, Resolvable value )
        {
            m_key = key;
            m_value = value;
        }
        
        String getKey()
        {
            return m_key;
        }
        
        Resolvable getValue()
        {
            return m_value;
        }
    }
        
    //public <T>T resolve( ComponentStrategy parent, Class<T> clazz ) throws Exception
    //{
    //    return null;
    //}
    
    /*
    private final List<Binding> m_list = new ArrayList<Binding>();
    
    Query( String query )
    {
        if( null != query )
        {
            String[] segments = query.split( "," );
            for( String segment : segments )
            {
                String[] args = segment.split( "=" );
                if( args.length != 2 )
                {
                    throw new IllegalArgumentException( 
                      "Invalid query [" + segment + "]" );
                }
                String address = args[0];
                String param = args[1];
                String[] keys = address.split( "." );
                Binding binding = new Binding( keys, param );
                m_list.add( binding );
            }
        }
    }
    
    Binding getBinding( String key )
    {
        for( Binding binding : m_list )
        {
            if( key.equals( binding.getKey() ) )
            {
                return binding;
            }
        }
        return null;
    }
    
    static class Binding
    {
        private final String m_key;
        private final Object m_value;
        
        Binding( String[] keys, String value )
        {
            int n = keys.length;
            if( n == 0 )
            {
                throw new IllegalArgumentException( 
                  "Invalid query expression." );
            }
            else if( n == 1 )
            {
                m_key = keys[0];
                m_value = value;
            }
            else
            {
                int m = n-1;
                m_key = keys[0];
                String[] newKeys = new String[ m ];
                System.arrayCopy( keys, 1, newKeys, 0, m );
                m_value = new Binding( newKeys, value );
            }
        }
        
        String getKey()
        {
            return m_key;
        }
        
        Object getValue()
        {
            return m_value;
        }
    }
    */
}
