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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Iterator;
import java.util.Set;

import org.apache.metro.facilities.reflector.ReflectionException;

import org.apache.metro.facilities.reflector.spi.ReflectorProvider;
import org.apache.metro.facilities.reflector.spi.TypeHandler;
import org.apache.metro.facilities.reflector.spi.Util;

public abstract class AbstractObjectTypeHandler
    implements TypeHandler
{
    protected void getNames( Set names, Object container )
        throws ReflectionException
    {
        
        Class clazz = container.getClass();
        
        if( Modifier.isPublic( clazz.getModifiers() ) )
            getNames( names, clazz );
        else
        {
            Class[] interfaces = clazz.getInterfaces();
            for( int i=0 ; i < interfaces.length ; i++ )
            {
                getNames( names, interfaces[i] );
            }
        }
    }
    
    protected String[] packageNames( Set names )
    {
        String[] ret = new String[ names.size() ];
        Iterator set = names.iterator();
        for( int i=0 ; set.hasNext() ; i++ )
            ret[i] = (String) set.next();
        return ret;
    }
    
    protected void getNames( Set names, Class clazz )
        throws ReflectionException
    {
        /////  NORMAL OBJECT
        // Retrieve all PUBLIC, non-STATIC fields.
        Field[] flds = clazz.getDeclaredFields();
        for( int i=0 ; i < flds.length ; i++ )
        {
            int mod = flds[i].getModifiers();
            if( Modifier.isPublic( mod ) && !Modifier.isStatic( mod ) )
            {
                String str = flds[i].getName();
                int j = str.indexOf( '_' );
                if( j == 1 )
                    str = str.substring( 2 );
                names.add( str);
            }
        }
        // Retrieve PUBLIC non-STATIC GET methods
        Method[] methods = clazz.getMethods();
        for( int i=0 ; i < methods.length ; i++ )
        {
            int mod = methods[i].getModifiers();
            if( Modifier.isPublic( mod ) && ! Modifier.isStatic( mod ))
            {
                String str = methods[i].getName();
                if( str.startsWith("get") )
                {
                    if( methods[i].getParameterTypes().length == 0 )
                    {
                        str = str.substring( 3 );
                        if( ! names.contains(str) )
                            names.add( str );
                    }
                }
                else if( str.startsWith("is") )
                {
                    if( methods[i].getParameterTypes().length == 0 )
                    {
                        str = str.substring( 2 );
                        if( ! names.contains(str) )
                            names.add( str );
                    }
                }
            }
        }
    }

    public Object getMemberObject( Object container, String memberName )
        throws ReflectionException
    {
        Method method = Util.findMethod( container, "get" + memberName );
        
        if( method == null )
            method = Util.findMethod( container, "is" + memberName );
        
        if( method != null && ! Modifier.isPublic( method.getModifiers() ) )
            method = null;
            
        if( method != null )
        {
            try
            {
                if( method.getName().equals( "getTypes" ) )
                {
                    System.out.println( method.getName() );
                    System.out.println( Modifier.isPublic( method.getModifiers() ) );
                    System.out.println( method.getParameterTypes().length );
                    System.out.println( method.getReturnType() );
                    System.out.println( method.getDeclaringClass() );
                    System.out.println( method.getExceptionTypes().length );
                }
                return method.invoke(container, new Object[0] );
            } catch( IllegalAccessException e )
            {
                throw new ReflectionException( container.getClass().toString() + ".get" + memberName + "() is not declared public.", e );
            } catch( IllegalArgumentException e )
            {
                throw new ReflectionException(container.getClass().toString() + ".get" + memberName + "() received illegal arguments. Internal Error?", e);
            } catch( InvocationTargetException e )
            {
                Exception exc;
                Throwable t = e.getTargetException();
                if( t instanceof Exception )
                    exc = (Exception) t;
                else
                    exc = null;
                throw new ReflectionException( container.getClass().toString() + ".get" + memberName + "() resulted in an exception.\n" + e , exc );
            }
        }
        else
        {
            try
            {
                Field fld = Util.findField( container, memberName );
                return fld.get( container );
            } catch( Exception e )
            {
                throw new ReflectionException( "Unable to access the field: " + memberName, e );
            }
        }
    }

    public void setMemberObject( Object container, String member, Object value )
        throws ReflectionException
    {
        Class cls = container.getClass();
        Field[] flds = cls.getDeclaredFields();
        Method method = Util.findMethod( container, "set" + member );
        if( method != null )
        {
            try
            {
                Class[] argTypes = method.getParameterTypes();
                Object[] args = new Object[1];
                args[0] = value;
                Object obj = method.invoke( container, args );
            } catch( IllegalAccessException e )
            {
                throw new ReflectionException( container.getClass().toString() + ".set" + member + "() is not declared public.", e);
            } catch( Exception e )
            {
                throw new ReflectionException( "Problem accessing " + container.getClass().toString() + ".set" + member + "().", e);
            }
        }
        if( method == null )
        {
            for( int i=0; i < flds.length ; i++ )
            {
                if( Util.matchField( flds[i].getName(), member ) )
                {
                    if( flds[i].getType().isPrimitive() )
                    {
                        if( value instanceof String )
                        {
                            try
                            {
                                Util.setPrimitive( flds[i], container, (String) value );
                            } catch( IllegalAccessException e )
                            {
                                throw new ReflectionException( "The field '" + member + "' of " + container.toString() +  " is not declared public.", e);
                            }
                        }
                    }
                    else
                        setObject( flds[i], container, value );
                }
            }
        }
    }
    
    public Class getClass( Object container, String memberName )
        throws ReflectionException
    {
        Method method = Util.findMethod( container, "get" + memberName );
        if( method == null )
            method = Util.findMethod( container, "is" + memberName );

        if( method != null )
        {
            Class retClass = method.getReturnType();
            if( Modifier.isNative(retClass.getModifiers()) )
                return retClass;

            // Native classes are not properly serialized/deserialized over RMI.

            return Util.expandNativeClass( retClass );
        }
        try
        {
            Field fld = Util.findField( container, memberName );
            return fld.getType();
        } catch( NoSuchFieldException e )
        {
            throw new ReflectionException( "Member '" + memberName + "' not found in " + container );
        }
    }


    public boolean isSettable( Object container, String memberName )
        throws ReflectionException
    {
        Method method = Util.findMethod( container, "set" + memberName );

        if( method != null )
            return true;
        else
            return false;
    }
    
    private void setObject( Field fld, Object obj, Object value )
        throws ReflectionException
    {
        // Create a new instance of the object
        // Assign instance to reference.
        Class fldClass = fld.getType();
        
        Constructor[] constructors = fldClass.getConstructors();
        Constructor constr = null;
        for( int j=0; j < constructors.length ; j++ )
        {
            Class[] params = constructors[j].getParameterTypes();
            if( params.length == 1 && params[0].getName().equals("String"))
            {
                constr = constructors[j];
                break;
            }
        }
        
        if( constr != null )
        {
            try
            {
                Object[] args = {value};
                Object newObj = constr.newInstance( args );
                fld.set(obj, newObj );
            } catch( Exception e )
            {
                throw new ReflectionException( "Unable to construct a " + fldClass, e );
            }
        }
        else
            throw new ReflectionException( "No valid Constructors available for object: " + fldClass );
    }   
} 
