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

package net.dpml.library.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import net.dpml.library.Dictionary;
import net.dpml.library.info.AbstractDirective;

import net.dpml.util.PropertyResolver;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultDictionary implements Dictionary
{
    private final DefaultDictionary m_parent;
    private final AbstractDirective m_directive;
    private final Properties m_properties;
    
   /**
    * Creation of a new dictionary.  The dictionary provides support
    * for property inheritance within the hierachy of of modules based
    * an a single root virtual module.  When handling a propety request
    * the dictionary will attempt to resolve the property value using 
    * local property values.  If the value is unresolved, the implemenetation
    * will attempt to delegate the request to a parent dictionary if available.
    *
    * @param parent the parent dictionary (possibly null)
    * @param directive an abstract directive containing local properties
    */
    public DefaultDictionary( DefaultDictionary parent, AbstractDirective directive )
    {
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
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
    }
    
    //----------------------------------------------------------------------------
    // Dictionary
    //----------------------------------------------------------------------------
    
   /**
    * Return the property names associated with the dictionary.
    * @return the array of property names
    */
    public String[] getPropertyNames()
    {
        return getLocalPropertyNames( m_properties );
    }
    
   /**
    * Return the local property names associated with the dictionary.
    * @return the array of local property names
    */
    public String[] getLocalPropertyNames()
    {
        return getLocalPropertyNames( m_directive.getProperties() );
    }
    
   /**
    * Return a property value.
    * @param key the property key
    * @return the property value
    */
    public String getProperty( String key )
    {
        return getProperty( key, null );
    }
    
   /**
    * Return a property value.
    * @param key the property key
    * @param value the default value
    * @return the property value
    */
    public String getProperty( String key, String value )
    {
        String result = m_properties.getProperty( key, value );
        return resolve( result );
    }
    
   /**
    * Return an integer property value.
    * @param key the property key
    * @param value the default value
    * @return the property value as an integer
    * @exception NumberFormatException if underlying property value 
    *    cannot be resolved to an integer
    */
    public int getIntegerProperty( String key, int value )
    {
        String result = m_properties.getProperty( key );
        if( null == result )
        {
            return value;
        }
        else
        {
            String literal = resolve( result );
            return Integer.parseInt( literal );
        }
    }
    
   /**
    * Return an boolean property value.
    * @param key the property key
    * @param value the default value
    * @return the property value as an boolean
    * @exception NumberFormatException if underlying property value 
    *    cannot be resolved to an integer
    */
    public boolean getBooleanProperty( String key, boolean value )
    {
        String result = m_properties.getProperty( key );
        if( null != result )
        {
            return Boolean.valueOf( result ).booleanValue();
        }
        else
        {
            return value;
        }
    }
   /**
    * Evaluate and expand any symbolic references in the supplied value.
    * @param value the value to resolve
    * @return the resolved value
    */
    public String resolve( String value )
    {
        return PropertyResolver.resolve( m_properties, value );
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

    Properties getExportProperties()
    {
        String[] keys = getLocalPropertyNames();
        Properties properties = new Properties();
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            String value = getProperty( key );
            properties.setProperty( key, value );
        }
        return properties;
    }
}
