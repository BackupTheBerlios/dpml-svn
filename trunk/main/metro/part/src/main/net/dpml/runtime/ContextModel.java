/*
 * Copyright 2007 Stephen J. McConnell.
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
import java.util.Map;
import java.util.Hashtable;
import java.lang.reflect.Method;

import net.dpml.lang.Buffer;

import net.dpml.runtime.Directive.Resolvable;

import org.w3c.dom.Element;

/**
 * Context model interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ContextModel implements Resolvable
{
    private ContextDirective m_directive;
    
    private final Map<String,Resolvable> m_map = new Hashtable<String,Resolvable>();
    
    ContextModel( 
      Class clazz, String path, Class subject, boolean policy,
      ContextDirective bundled, ContextDirective directive, Query query, boolean validate ) throws IOException
    {
        this( clazz, path, subject, policy, null, bundled, directive, query, validate );
    }
    
    private ContextModel( 
      Class clazz, String path, Class subject, boolean policy, String keyPath,
      ContextDirective bundled, ContextDirective directive, Query query, boolean validate ) throws IOException
    {
        m_directive = directive;
        
        if( null != subject )
        {
            for( Method method : subject.getMethods() )
            {
                Class c = method.getReturnType();
                String key = ContextInvocationHandler.getKeyForMethod( method );
                boolean optional = isOptionalEntry( method );
                boolean composite = ContextInvocationHandler.isaContext( c, policy );
                
                Resolvable value = null;
                if( composite )
                {
                    ContextDirective parent = getContextDirective( bundled, key );
                    ContextDirective nested = getContextDirective( directive, key );
                    try
                    {
                        ContextModel model = 
                          new ContextModel( clazz, path, c, policy, key, parent, nested, query, validate );
                        if( !optional || ( model.size() > 0 ) )
                        {
                            value = model;
                        }
                    }
                    catch( MissingContextEntryException mcee )
                    {
                        if( !optional )
                        {
                            final String error = 
                              "Non optional context entry ["
                              + key
                              + "] within the nested context ["
                              + c.getName()
                              + "] referenced by ["
                              + clazz.getName()
                              + "] within ["
                              + path
                              + "] is undefined.";
                            if( validate )
                            {
                                throw new ComponentException( error );
                            }
                            else
                            {
                                // m_logger.warn( error );
                            }
                        }
                    }
                }
                else
                {
                    if( null != query )
                    {
                        String scopedKey = getScopedKey( keyPath, key );
                        value = query.getEntry( scopedKey );
                    }
                    if( ( null == value ) && null != directive )
                    {
                        value = directive.getEntry( key );
                    }
                    if( ( null == value ) && null != bundled )
                    {
                        value = bundled.getEntry( key );
                    }
                }
                if( null != value )
                {
                    m_map.put( key, value );
                }
                else if( !optional )
                {
                    final String error = 
                      "No solution declared for the context entry ["
                      + key
                      + "] within the context definition ["
                      + subject.getName()
                      + "] as a constructor argument to the component class ["
                      + clazz.getName()
                      + "] within the component model ["
                      + path
                      + "].";
                    if( validate )
                    {
                        throw new MissingContextEntryException( error );
                    }
                    else
                    {
                        // m_logger.validate( error );
                    }
                }
            }
        }
    }
    
    int size()
    {
        return m_map.size();
    }
    
    private String getScopedKey( String base, String key )
    {
        if( null == base )
        {
            return key;
        }
        else
        {
            return base + "." + key;
        }
    }
    
    public Element getElement()
    {
        if( null != m_directive )
        {
            return m_directive.getElement();
        }
        else
        {
            return null;
        }
    }
    
    public void encode( Buffer buffer, String key ) throws IOException
    {
        if( null != m_directive )
        {
            m_directive.encode( buffer, key );
        }
    }

    private boolean isOptionalEntry( Method method )
    {
        return ( method.getParameterTypes().length > 0 );
    }
    
    public <T>T resolve( ComponentStrategy strategy, Class<T> type ) throws Exception
    {
        return ContextInvocationHandler.getProxiedInstance( type, strategy, this );
    }
    
    Resolvable getResolvable( String key )
    {
        return m_map.get( key );
    }
    
    ContextDirective getDirective()
    {
        return m_directive;
    }

    private ContextDirective getContextDirective( ContextDirective directive, String key )
    {
        if( null == directive )
        {
            return null;
        }
        else
        {
            return directive.getContextDirective( key );
        }
    }
}

