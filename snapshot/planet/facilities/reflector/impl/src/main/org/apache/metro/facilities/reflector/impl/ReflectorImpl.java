/*
 * 1.0    1999/07/30 Niclas Hedhman     First Public Release
 *
 * Copyright (c) 1996-1999 Bali Automation. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * BALI AUTOMATION MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE 
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING 
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. BALI AUTOMATION
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A 
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS 
 * DERIVATIVES.
 */
package org.apache.metro.facilities.reflector.impl;

import java.lang.reflect.Proxy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.io.IOException;

import org.apache.avalon.framework.activity.Disposable;

import org.apache.metro.facilities.reflector.ReflectorService;
import org.apache.metro.facilities.reflector.ReflectionException;

import org.apache.metro.facilities.reflector.spi.ReflectorProvider;
import org.apache.metro.facilities.reflector.spi.TypeHandler;
import org.apache.metro.facilities.reflector.spi.Util;


/** The Reflector Implementation is capable of digging through any
*   Java application that is added as a root object.
*
* @avalon.component name="reflector" lifestyle="singleton"
* @avalon.service type="org.apache.metro.facilities.reflector.ReflectorService"
* @avalon.service type="org.apache.metro.facilities.reflector.spi.ReflectorProvider"
*/
public final class ReflectorImpl
    implements ReflectorService, ReflectorProvider, Disposable
{
    private HashMap     m_RootObjects;
    private HashMap     m_TypeHandlerMap;
    private TypeHandler m_DefaultTypeHandler;
    
    public ReflectorImpl()
    {
        super();
        m_RootObjects = new HashMap();
        m_TypeHandlerMap = new HashMap();
    }

    public synchronized void addTypeHandler( TypeHandler handler, Class type )
    {
        if( handler.isDefault() )
            m_DefaultTypeHandler = handler;
        else
            m_TypeHandlerMap.put( type, handler );
    }
    
    public synchronized void removeTypeHandler( TypeHandler handler )
    {
        Iterator list = m_TypeHandlerMap.values().iterator();
        while( list.hasNext() )
        {
            TypeHandler th = (TypeHandler) list.next();
            if( th.equals( handler ) )
                list.remove();
        }
    }
    
    public void addRootObject( String name, Object obj )
    {
        synchronized( m_RootObjects )
        {
            m_RootObjects.put( name, obj );
        }
    }

    public void removeRootObject( String name )
    {
        synchronized( m_RootObjects )
        {
            m_RootObjects.remove(name);
        }
    }

    public Object getRootObject( String name )
    {
        synchronized( m_RootObjects )
        {
            Object result = m_RootObjects.get(name);
            return result;
        }
    }

    public Map getRootObjects()
    {
        return m_RootObjects;
    }
    
    public String[] getAllRootNames() 
    {
        synchronized( m_RootObjects )
        {
            Iterator list = m_RootObjects.keySet().iterator();
            String[] names = new String[m_RootObjects.size()];
            
            for( int i=0; list.hasNext() ; i++ )
                names[i] = (String) list.next();
            
            return names;
        }
    }

    public String[] getNames( String object) 
        throws ReflectionException
    {
        if( object == null )
            return getAllRootNames();
        if( object.equals("") )
            return getAllRootNames();
        Object container = resolveObject(object);
        
        String[] result = getNames( container );
        return result;
    }
    
    public String[] getNames( Object container) 
        throws ReflectionException
    {
        if( container == null )
            return getAllRootNames();

        TypeHandler th = getTypeHandler( container );
        return th.getNames( container );
    }

    public String get( String object )
        throws ReflectionException
    {
        Object obj = resolveObject( object );
        return "" + obj;
    }

    public void set( String object, String value )
        throws ReflectionException
    {
        String container = dropLast( object );
        String member = getLast(object);
        Object obj = resolveObject( container );
        setObject( obj, member, value ); //calls private setObject( Object, String, String)
    }
    
    public Object getObject( String object )
        throws ReflectionException
    {
        Object obj = resolveObject( object );
        return obj;
    }
    
    public Object getObject( Object container, String memberName )
        throws ReflectionException
    {
        if( container == null || container.equals("") )
        {
            synchronized( m_RootObjects )
            {
                Object root = m_RootObjects.get(memberName);
                return root;
            }
        }
        if( container.getClass().isPrimitive() )
            return null;

        TypeHandler th = getTypeHandler( container );
        return th.getMemberObject( container, memberName );
    }

    public void setObject( String objectname, Object object )
        throws ReflectionException
    {
        String containerName = dropLast( objectname );
        String member = getLast(objectname);
        Object container = resolveObject( containerName );
        setObject( container, member, object );
    }

    public void setObject( Object container, String member, Object value )
        throws ReflectionException
    {
        TypeHandler th = getTypeHandler( container );
        synchronized( container )
        {
            th.setMemberObject( container, member, value );
        }
    }

    public String getContainer( String objectname )
    {
        String result = dropLast(objectname);
        return result;
    }
    
    public String getMember( String objectname )
    {
        String result = getLast(objectname);
        return result;
    }
    
    private Object resolveObject( String name )
        throws ReflectionException
    {
        Object obj = null;

        if( "".equals(name) || name == null)
            return null;
        String container = dropLast(name);
        String member = getLast(name);
        obj = resolveObject( container );
        obj = getObject( obj, member );
        return obj;
    }

    private String dropLast( String name )
    {
        int pos1 = name.lastIndexOf( '[' );
        int pos2 = name.lastIndexOf( '.' );
        int pos3 = name.lastIndexOf( ']' );
        if( pos1 == -1 && pos2 == -1 )
            return "";
        String result = name.substring( 0, pos3 > pos2 ? pos1 : pos2 );
        return result;
    }

    private String getLast( String name )
    {
        int pos1 = name.lastIndexOf( '[' );
        int pos2 = name.lastIndexOf( '.' );
        int pos3 = name.lastIndexOf( ']' );
        if( pos1 == -1 && pos2 == -1 )
            return name;
        String result = name.substring( pos3 > pos2 ? pos1 : pos2+1 );
        return result;
    }

    public String getClassName( String name )
        throws ReflectionException
    {
        Class cls = getClass(name);
        if( cls == null )
            return "<null>";
        return cls.getName();
    }
    
    public String getClassName( Object container, String memberName )
        throws ReflectionException
    {
        return getClass(container, memberName).getName();
    }
    
    public Class getClass( String name )
        throws ReflectionException
    {
        if( "".equals(name) || name == null)
            return null;
        String container = dropLast(name);
        String member = getLast(name);
        Object obj = resolveObject( container );
        return getClass( obj, member );
    }
    
    public Class getClass( Object container, String memberName )
        throws ReflectionException
    {
        if( container == null || container.equals("") )
        {
            synchronized( m_RootObjects )
            {
                Object root = m_RootObjects.get(memberName);
                return root.getClass();
            }
        }

        if( Util.isPrimitive(container) )
            return null;

        TypeHandler th = getTypeHandler( container );
        return th.getClass( container, memberName );
    }

    public boolean isSettable( String name )
        throws ReflectionException
    {
        if( "".equals(name) || name == null)
            return false;
        String container = dropLast(name);
        String member = getLast(name);
        Object obj = resolveObject( container );
        return isSettable( obj, member );
    }

    public boolean isSettable( Object container, String memberName)
        throws ReflectionException
    {
        if( container == null || "".equals(container) )
            return false;
        if( Util.isPrimitive(container) )
            return false;
        TypeHandler th = getTypeHandler( container );
        return th.isSettable( container, memberName );
    }

    public void dispose()
    {
        synchronized( m_RootObjects )
        {
            m_RootObjects.clear();
            m_RootObjects = null;
        }
    }
    
    private TypeHandler getTypeHandler( Object obj )
    {
        Class clazz = obj.getClass();
        if( Proxy.isProxyClass( clazz ) )
        {
            TypeHandler th = (TypeHandler) m_TypeHandlerMap.get( Proxy.class );
            if( th != null )
                return th;
        }
        Iterator list = m_TypeHandlerMap.entrySet().iterator();
        while( list.hasNext() )
        {
            Map.Entry entry = (Map.Entry) list.next();
            Class cls = (Class) entry.getKey();
            if( cls.isInstance( obj ) )
            {
                return (TypeHandler) entry.getValue();
            }
        }        
        return m_DefaultTypeHandler;
    }
}
