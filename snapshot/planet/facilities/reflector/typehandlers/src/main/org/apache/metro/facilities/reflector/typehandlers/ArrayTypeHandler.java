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

import java.lang.reflect.Array;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.metro.facilities.reflector.ReflectionException;

import org.apache.metro.facilities.reflector.spi.ReflectorProvider;
import org.apache.metro.facilities.reflector.spi.TypeHandler;
import org.apache.metro.facilities.reflector.spi.Util;

/**
 * @avalon.component name="type-handler-array" lifestyle="singleton"
 * @avalon.service type="org.apache.metro.facilities.reflector.spi.TypeHandler"
 */
public class ArrayTypeHandler
    implements TypeHandler, Serviceable
{
    /**
     * @avalon.dependency type="org.apache.metro.facilities.reflector.spi.ReflectorProvider"
     *                    key="provider"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        ReflectorProvider provider = (ReflectorProvider) man.lookup( "provider" );
        provider.addTypeHandler( this, Object[].class );
    }
    
    public boolean isDefault()
    {
        return false;
    }
    
    public String[] getNames( Object object )
        throws ReflectionException
    {
        if( ! object.getClass().isArray() )
            throw new ReflectionException( object + " is not an Array: " + object.getClass().getName() );
        Object[] container = (Object[]) object;
        String[] sa = new String[container.length];
        for( int i=0 ; i < sa.length ; i++ )
            sa[i] = "" + i;
        return sa;
    }
    
    public Object getMemberObject( Object container, String membername )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not an Array: " + container.getClass().getName() );
        int numindex = getIndex( membername );
        return Array.get(container, numindex);
    }
    
    public void setMemberObject( Object container, String membername, Object value )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not an Array: " + container.getClass().getName() );
        int numindex = getIndex( membername );
        Object obj = Array.get(container, numindex );
        if( ! obj.getClass().isInstance( value ) )
        {
            if( value instanceof String )
                value = Util.convPrimitive( obj.getClass(), (String) value);
        }
        Array.set(container, numindex, value);
    }

    public Class getClass( Object container, String membername )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not an Array: " + container.getClass().getName() );
        int numindex = getIndex( membername );
        return Array.get(container, numindex).getClass();
    }
    
    public boolean isSettable( Object container, String membername )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not an Array: " + container.getClass().getName() );
        return true;
    }
    
    private int getIndex( String membername )
    {
        int numindex = 0;
        try
        {
            if( membername.startsWith( "[" ) )
                membername = membername.substring( 1, membername.length() - 1);
            if( membername.startsWith( "\'" ) )
                membername = membername.substring( 1, membername.length() - 1);
            numindex = Integer.parseInt( membername );
        } catch( NumberFormatException e ){}  // Ignore
        return numindex;        
    }
} 
