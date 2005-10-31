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

package net.dpml.tools.info;

import java.io.Serializable;
import java.util.Properties;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractDirective implements Serializable
{
    private Properties m_properties;
    
    public AbstractDirective( Properties properties )
    {
        if( null == properties )
        {
            m_properties = new Properties();
        }
        else
        {
            m_properties = properties;
        }
    }
    
    public String getProperty( String key )
    {
        return m_properties.getProperty( key );
    }
    
    public Properties getProperties()
    {
        return m_properties;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            return ( other instanceof AbstractDirective );
        }
    }
    
    public int hashCode()
    {
        return getClass().hashCode();
    }
    
    protected int hashArray( Object[] array )
    {
        if( null == array )
        {
            return 0;
        }
        int hash = 0;
        for( int i=0; i<array.length; i++ )
        {
            Object object = array[i];
            hash ^= hashValue( object );
        }
        return hash;
    }
    
    protected int hashValue( Object value )
    {
        if( null == value )
        {
            return 0;
        }
        else if( value instanceof Object[] )
        {
            return hashArray( (Object[]) value );
        }
        else
        {
            return value.hashCode();
        }
    }
    
    protected boolean equals( Object a, Object b )
    {
        if( null == a )
        {
            return ( null == b );
        }
        else
        {
            return a.equals( b );
        }
    }
}
