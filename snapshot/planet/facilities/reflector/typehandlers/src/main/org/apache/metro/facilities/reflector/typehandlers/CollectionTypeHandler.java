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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.metro.facilities.reflector.ReflectionException;

import org.apache.metro.facilities.reflector.spi.ReflectorProvider;
import org.apache.metro.facilities.reflector.spi.TypeHandler;
import org.apache.metro.facilities.reflector.spi.Util;

/**
 * @avalon.component name="type-handler-collection" lifestyle="singleton"
 * @avalon.service type="org.apache.metro.facilities.reflector.spi.TypeHandler"
 */
public class CollectionTypeHandler
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
        provider.addTypeHandler( this, Collection.class );
    }
    
    public boolean isDefault()
    {
        return false;
    }
    
    public String[] getNames( Object object )
        throws ReflectionException
    {
        if( ! ( object instanceof Collection ) )
            throw new ReflectionException( object + " is not a Collection: " + object.getClass().getName() );
        Collection container = (Collection) object;
        String[] sa = new String[container.size()];
        for( int i=0; i < container.size() ; i++ )
            sa[i] = "" + i;
        return sa;
    }
    
    public Object getMemberObject( Object container, String membername )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not a Collection: " + container.getClass().getName() );
        int numindex = getIndex( membername );
        if( container instanceof List )
        {
            synchronized( container )
            {
                return ((List) container).get(numindex);
            }
        }
        else
        {
            synchronized( container )
            {
                Object arr[] = ((Collection) container).toArray();
                if( numindex < arr.length )
                    return arr[numindex];
                else
                    return null;
            }
        }
    }
    
    public void setMemberObject( Object container, String membername, Object value )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not a Collection: " + container.getClass().getName() );
        int numindex = getIndex( membername );
        if( container instanceof List )
        {
            Object obj = ((List) container).get(numindex);
            if( ! obj.getClass().isInstance( value ) )
            {
                if( value instanceof String )
                    value = Util.convPrimitive( obj.getClass(), (String) value);
            }
            ((List) container).set(numindex, value);
        }
        else
        {
            Collection c = (Collection) container;
            if( c.size() >= numindex )
                return;
                
            Iterator list = c.iterator();
            for( int i=0; i < numindex && list.hasNext(); list.next() );
            Object obj = list.next();
            if( ! obj.getClass().isInstance( value ) )
            {
                if( value instanceof String )
                    value = Util.convPrimitive( obj.getClass(), (String) value);
            }
            list.remove();
            ((Collection) container).add(value);
        }
    }

    public Class getClass(Object container, String memberName)
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not a Collection: " + container.getClass().getName() );
        int numindex = getIndex( memberName );
        if( container instanceof List )
        {
            Object obj = ((List) container).get(numindex);
            if( obj == null )
                return null;
            return obj.getClass();
        }
        else
        {
            Object arr[] = ((Collection) container).toArray();
            if( numindex < arr.length )
                return arr[numindex].getClass();
            else
                return null;
        }    
    }
    
    public boolean isSettable( Object container, String membername )
        throws ReflectionException
    {
        if( ! container.getClass().isArray() )
            throw new ReflectionException( container + " is not a Collection: " + container.getClass().getName() );
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
