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

package org.apache.metro.facilities.reflector.spi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

public class Util
{
    static public Object convPrimitive( Class type, String value )
    {
        Object ret ;
        if( type.isPrimitive() )
        {
            if( type.equals(Boolean.TYPE) )
                ret = new Boolean( value );
            else if( type.equals(Double.TYPE) )
                ret = new Double( value );
            else if( type.equals(Integer.TYPE) )
                ret = new Integer( value );
            else if( type.equals(Short.TYPE) )
                ret = new Short( value );
            else if( type.equals(Byte.TYPE) )
                ret = new Byte( value );
            else if( type.equals(Float.TYPE) )
                ret = new Float( value );
            else if( type.equals(Long.TYPE ) )
                ret = new Long( value );
            else if( type.equals(Character.TYPE) )
                ret = new Character( value.charAt(0) );
            else if( type.equals( String.class ) )
                ret = value;
            else if( type == Void.TYPE )
                throw new IllegalArgumentException( " Can not set void primitives." );
            else
                throw new IllegalArgumentException( " Unknown primitive type." );
        }
        else
        {
            if( Boolean.class.isAssignableFrom(type) )
                ret = new Boolean( value );
            else if( Double.class.isAssignableFrom(type))
                ret = new Double( value );
            else if( Integer.class.isAssignableFrom(type))
                ret = new Integer( value );
            else if( Short.class.isAssignableFrom(type))
                ret = new Short( value );
            else if( Byte.class.isAssignableFrom(type))
                ret = new Byte( value );
            else if( Float.class.isAssignableFrom(type))
                ret = new Float( value );
            else if( Long.class.isAssignableFrom(type))
                ret = new Long( value );
            else if( Character.class.isAssignableFrom(type))
                ret = new Character( value.charAt(0) );
            else if( type.equals( String.class ) )
                ret = value;
            else if( Void.class.isAssignableFrom(type) )
                throw new IllegalArgumentException( " Can not set void primitives." );
            else if( Number.class.isAssignableFrom(type))
            {
                ret = new Double( value );
            }
            else
            {
                // Class name is assumed in value...
                // create a new instance of this class and return as the 
                // value to assign
                try
                {
                    ret = Class.forName(value).newInstance();
                } catch( Exception e )
                {
                    throw new IllegalArgumentException( "Unable to instantiate '" + value + "'" );
                }
            }
        }
        return ret;
    }

    static public void setPrimitive( Field fld, Object obj, String value )
        throws IllegalAccessException
    {
        Class fldClass = fld.getType();
        if( fldClass == Boolean.TYPE )
        {
            fld.setBoolean(obj, (new Boolean(value)).booleanValue());
        }
        else if( fldClass == Double.TYPE )
        {
            fld.setDouble(obj, (new Double(value)).doubleValue());
        }
        else if( fldClass == Integer.TYPE )
        {
            fld.setInt(obj, (new Integer(value)).intValue());
        }
        else if( fldClass == Short.TYPE )
        {
            fld.setShort(obj, (new Short(value)).shortValue());
        }
        else if( fldClass == Byte.TYPE )
        {
            fld.setByte(obj, (new Byte(value)).byteValue());
        }
        else if( fldClass == Float.TYPE )
        {
            fld.setFloat(obj, (new Float(value)).floatValue());
        }
        else if( fldClass == Long.TYPE )
        {
            fld.setLong(obj, (new Long(value)).longValue());
        }
        else if( fldClass == Character.TYPE )
        {
            fld.setChar(obj, value.charAt(0) );
        }
        else if( fldClass == Void.TYPE )
        {
            throw new IllegalArgumentException( " Can not set void primitives." );
        }
        else 
            throw new IllegalArgumentException( "Invalid primitive Class.Type()." );
    }

    static public Class expandNativeClass( Class nativeClass )
    {
        Class ret = nativeClass;
        
        if( nativeClass.equals(Boolean.TYPE) )
        {
            ret = Boolean.class;
        }
        else if( nativeClass.equals(Double.TYPE) )
        {
            ret = Double.class;
        }
        else if( nativeClass.equals(Integer.TYPE) )
        {
            ret = Integer.class;
        }
        else if( nativeClass.equals(Short.TYPE) )
        {
            ret = Short.class;
        }
        else if( nativeClass.equals(Byte.TYPE) )
        {
            ret = Byte.class;
        }
        else if( nativeClass.equals(Float.TYPE) )
        {
            ret = Float.class;
        }
        else if( nativeClass.equals(Long.TYPE ) )
        {
            ret = Long.class;
        }
        else if( nativeClass.equals(Character.TYPE) )
        {
            ret = Character.class;
        }
        return ret;
    }
    
    static public boolean isPrimitive( Object obj )
    {
        if( obj instanceof Boolean )
            return true;
        else if( obj instanceof Double )
            return true;
        else if( obj instanceof Integer )
            return true;
        else if( obj instanceof Long )
            return true;
        else if( obj instanceof Byte )
            return true;
        else if( obj instanceof Float )
            return true;
        else if( obj instanceof Character )
            return true;
        else if( obj instanceof Short )
            return true;
        else if( obj instanceof Void )
            return true;
        else
            return false;
    }

    static public Field findField( Object container, String memberName )
        throws NoSuchFieldException
    {
        Class cls = container.getClass();
        try
        {
            Field fld = cls.getDeclaredField( memberName );
            return fld;
        } catch( NoSuchFieldException e )
        {
            Field fld = cls.getDeclaredField( "m_" + memberName );
            return fld;
        }            
    }

    static public Method findMethod( Object container, String methodName )
    {
        Class clazz = container.getClass();
        if( Modifier.isPublic( clazz.getModifiers() ) )
        {
            return findMethod( clazz, methodName );
        }
        else
        {
            Class[] interfaces = clazz.getInterfaces();
            for( int i=0 ; i < interfaces.length ; i++ )
            {
                Method method = findMethod( interfaces[i], methodName );
                if( method != null )
                    return method;
            }        
        }
        return null;
    }
    
    static public Method findMethod( Class cls, String methodName )
    {
        Method[] methods = cls.getMethods();
        for( int i = 0; i < methods.length ; i++ )
        {
            Method method = methods[i];
            if( method.getName().equals( methodName ) )
            {
                boolean found = false;
                if( methodName.startsWith( "get" ) ||
                    methodName.startsWith( "is" )
                )
                {
                    Class[] params = method.getParameterTypes();
                    if( params.length == 0 )
                        found = true;
                }
                else if( methodName.startsWith( "set" ) )
                {
                    Class[] params = method.getParameterTypes();
                    Class retType = method.getReturnType();
                    if( params.length == 1 && 
                        retType.equals( Void.TYPE ) 
                    )
                    {
                        found = true;
                    }
                }
                if( found )
                    return method;
            }
        }
        return null;
    }

    static public boolean matchField( String programmatic, String global )
    {
        int i = programmatic.indexOf( '_' );
        if( i >= 0 )
            programmatic = programmatic.substring( i + 1 );
        return global.equalsIgnoreCase( programmatic );
    }

} 
