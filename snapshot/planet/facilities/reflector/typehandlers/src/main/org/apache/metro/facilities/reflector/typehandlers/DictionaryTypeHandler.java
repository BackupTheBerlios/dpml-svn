/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.metro.facilities.reflector.typehandlers;

import java.util.Dictionary;
import java.util.Enumeration;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.metro.facilities.reflector.ReflectionException;

import org.apache.metro.facilities.reflector.spi.ReflectorProvider;
import org.apache.metro.facilities.reflector.spi.TypeHandler;
import org.apache.metro.facilities.reflector.spi.Util;

/**
 * @avalon.component name="type-handler-dictionary" lifestyle="singleton"
 * @avalon.service type="org.apache.metro.facilities.reflector.spi.TypeHandler"
 */
public class DictionaryTypeHandler
    implements TypeHandler
{
    
    /**
     * @avalon.dependency type="org.apache.metro.facilities.reflector.spi.ReflectorProvider"
     *                    key="provider"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        ReflectorProvider provider = (ReflectorProvider) man.lookup( "provider" );
        provider.addTypeHandler( this, Dictionary.class );
    }
    
    public boolean isDefault()
    {
        return false;
    }
    
    public String[] getNames( Object object )
        throws ReflectionException
    {
        if( ! ( object instanceof Dictionary ) )
            throw new ReflectionException( object + " is not a Dictionary: " + object.getClass().getName() );
            
        Dictionary container = (Dictionary) object;
        String[] sa = new String[container.size()];
        int i=0;
        
        Enumeration list = container.keys();
        while( list.hasMoreElements() )
        {
            sa[i++] = list.nextElement().toString();
        }
        return sa;
    }
    
    public Object getMemberObject( Object container, String membername )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not a Dictionary: " + container.getClass().getName() );
        membername = getIndex( membername );
        
        synchronized( container )
        {
            return ((Dictionary) container).get(membername);
        }
    }
    
    public void setMemberObject( Object container, String membername, Object value )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not a Dictionary: " + container.getClass().getName() );
        
        membername = getIndex( membername );
        Object obj = ((Dictionary) container).get( membername );
        if( ! obj.getClass().isInstance( value ) )
        {
            if( value instanceof String )
                value = Util.convPrimitive( obj.getClass(), (String) value);
        }
        ((Dictionary) container).put( membername, value );
    }        

    public Class getClass( Object container, String membername )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not a Dictionary: " + container.getClass().getName() );
        String index = getIndex( membername );
        Object obj = ((Dictionary) container).get(index);
        if( obj == null )
            return null;
        return obj.getClass();
    }    
    
    public boolean isSettable( Object container, String membername )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not a Dictionary: " + container.getClass().getName() );
        return true;
    }
    
    private String getIndex( String membername )
    {
        if( membername.startsWith( "[" ) )
            membername = membername.substring( 1, membername.length() - 1);
        if( membername.startsWith( "\'" ) )
            membername = membername.substring( 1, membername.length() - 1);
        return membername;
    }
} 
