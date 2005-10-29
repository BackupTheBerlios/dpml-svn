/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.control;

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import net.dpml.tools.model.Dictionary;
import net.dpml.tools.info.AbstractDirective;

import net.dpml.transit.util.PropertyResolver;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultDictionary implements Dictionary
{
    private final DefaultDictionary m_parent;
    private final AbstractDirective m_directive;
    private final Properties m_properties;
    //private final String[] m_names;
    
    DefaultDictionary( AbstractDirective directive )
    {
        this( null, directive );
    }
    
    DefaultDictionary( DefaultDictionary parent, AbstractDirective directive )
    {
        m_parent = parent;
        m_directive = directive;
        
        Properties properties = getParentProperties();
        m_properties = new Properties( properties );
        Properties local = directive.getProperties();
        String[] keys = getLocalPropertyNames( local );
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            String value = local.getProperty( key );
            m_properties.setProperty( key, value );
        }
        // m_names = getLocalPropertyNames( m_properties );
    }
    
    //----------------------------------------------------------------------------
    // Dictionary
    //----------------------------------------------------------------------------
    
    public String[] getPropertyNames()
    {
        return getLocalPropertyNames( m_properties );
        //return m_names;
    }
    
    public String getProperty( String key )
    {
        return getProperty( key, null );
    }
    
    public String getProperty( String key, String value )
    {
        String result = m_properties.getProperty( key, value );
        return PropertyResolver.resolve( m_properties, result );
    }
    
    //----------------------------------------------------------------------------
    // internal
    //----------------------------------------------------------------------------
    
    void setProperty( String name, String value )
    {
        m_properties.setProperty( name, value );
    }
    
    AbstractDirective getAbstractDirective()
    {
        return m_directive;
    }
    
    Properties getProperties()
    {
        return m_properties;
    }
    
    private Properties getParentProperties()
    {
        if( null == m_parent )
        {
            return new Properties();
        }
        else
        {
            return m_parent.getProperties();
        }
    }
    
    private String[] getLocalPropertyNames( Properties properties )
    {
        ArrayList list = new ArrayList();
        Enumeration names = properties.propertyNames();
        while( names.hasMoreElements() )
        {
            list.add( (String) names.nextElement() );
        }
        return (String[]) list.toArray( new String[0] );
    }
}
