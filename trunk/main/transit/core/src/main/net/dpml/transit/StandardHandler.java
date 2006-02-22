/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit;

import java.io.IOException;
import java.beans.Expression;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import net.dpml.lang.Handler;

/**
 * Default plugin instantiation handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardHandler implements Handler
{
   /**
    * Instantiate the plugin.
    *
    * @param classloader the classloader
    * @param properties plugin strategy properties
    * @param args commandline arguments
    * @return the instance
    * @exception Exception if an error occurs
    */
    public Object getPlugin( ClassLoader classloader, Properties properties, Object[] args  )
      throws Exception
    {
        Class clazz = getPluginClass( classloader, properties );
        return instantiate( clazz, args );
    }
    
   /**
    * Get a plugin class.
    *
    * @param classloader the plugin classloader
    * @param properties the supplimentary properties
    * @return the plugin class
    * @exception Exception if an error occurs
    */
    public Class getPluginClass( ClassLoader classloader, Properties properties )
       throws Exception
    {
        if( null == classloader )
        {
            throw new NullArgumentException( "classloader" );
        }
        if( null == properties )
        {
            throw new NullArgumentException( "properties" );
        }

        String target = properties.getProperty( "project.plugin.class" );
        if( null == target )
        {
            final String error = 
              "Missing 'project.plugin.class' property.";
            throw new RepositoryException( error );
        }
        else
        {
            return loadPluginClass( classloader, target );
        }
    }
    
   /**
    * Instantiate an instance of a class using the supplied arguments.
    * @param clazz the target class
    * @param args the commandline arguments
    * @return the instantiated instance
    * @exception RepositoryException if an unexpected error occurs
    * @exception InvocationTargetException if the plugin instance raises an exception 
    *  during instantiation
    */
    public Object instantiate( Class clazz, Object[] args ) throws RepositoryException, InvocationTargetException
    {
        if( null == clazz )
        {
            throw new NullArgumentException( "clazz" );
        }
        if( null == args )
        {
            throw new NullArgumentException( "args" );
        }
        for( int i=0; i < args.length; i++ )
        {
            Object p = args[i];
            if( null == p )
            {
                final String error = 
                  "User supplied instantiation argument at position [" 
                  + i 
                  + "] for the class ["
                  + clazz.getName()
                  + "] is a null value.";
                throw new NullArgumentException( error );
            }
        }
        
        if( clazz.getConstructors().length == 1 )
        {
            Constructor constructor = getSingleConstructor( clazz );
            return instantiate( constructor, args );
        }
        else
        {
            try
            {
                Expression expression = new Expression( clazz, "new", args );
                return expression.getValue();
            }
            catch( InvocationTargetException e )
            {
                throw e;
            }
            catch( RepositoryException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                final String error = 
                "Class instantiation error [" + clazz.getName() + "]";
                throw new RepositoryException( error, e );
            }
        }
    }
    
   /**
    * Instantiate an instance of a class using the supplied constructor and arguments.
    * @param constructor the target constructor
    * @param args the commandline arguments
    * @return the instantiated instance
    * @exception RepositoryException if an unexpected error occurs
    * @exception InvocationTargetException if the plugin instance raises an exception 
    *  during instantiation
    */
    public Object instantiate( Constructor constructor, Object[] args ) 
      throws RepositoryException, InvocationTargetException
    {
        Object[] arguments = populate( constructor, args );
        return newInstance( constructor, arguments );
    }
    
   /**
    * Create a factory using a supplied class and command line arguments.
    *
    * @param clazz the the factory class
    * @param args the command line args
    * @return the plugin instance
    * @exception IOException if a plugin creation error occurs
    * @exception InvocationTargetException if a plugin constructor invocation error occurs
    * @exception NullArgumentException if the class or args argument is null
    */
    private Object createPlugin( Class clazz, Object[] args )
        throws IOException, NullArgumentException, InvocationTargetException
    {
        if( null == clazz )
        {
            throw new NullArgumentException( "clazz" );
        }
        if( null == args )
        {
            throw new NullArgumentException( "args" );
        }
        return instantiate( clazz, args );
    }

    private Object[] populate( Constructor constructor, Object[] args ) throws RepositoryException
    {
        if( null == constructor )
        {
            throw new NullArgumentException( "constructor" );
        }
        if( null == args )
        {
            throw new NullArgumentException( "args" );
        }
        
        Class[] classes = constructor.getParameterTypes();
        Object[] arguments = new Object[ classes.length ];
        ArrayList list = new ArrayList();
        for( int i=0; i < args.length; i++ )
        {
            list.add( args[i] );
        }

        //
        // sweep though the construct arguments one by one and
        // see if we can assign a value based on the supplied args
        //

        for( int i=0; i < classes.length; i++ )
        {
            Class clazz = classes[i];
            Iterator iterator = list.iterator();
            while( iterator.hasNext() )
            {
                Object object = iterator.next();
                Class c = object.getClass();
                if( isAssignableFrom( clazz, c ) )
                {
                    arguments[i] = object;
                    list.remove( object );
                    break;
                }
            }
        }

        //
        // if any arguments are unresolved then check if the argument type
        // is something we can implicity establish
        //

        for( int i=0; i < arguments.length; i++ )
        {
            if( null == arguments[i] )
            {
                Class c = classes[i];
                if( c.isArray() )
                {
                    arguments[i] = getEmptyArrayInstance( c );
                }
                else
                {
                    final String error =
                      "Unable to resolve a value for a constructor parameter."
                      + "\nConstructor class: " + constructor.getDeclaringClass().getName()
                      + "\nParameter class: " + c.getName()
                      + "\nParameter position: " + ( i + 1 );
                    throw new RepositoryException( error );
                }
            }
        }
        return arguments;
    }

    private boolean isAssignableFrom( Class clazz, Class c )
    {
        if( clazz.isPrimitive() )
        {
            if( Integer.TYPE == clazz )
            {
                return Integer.class.isAssignableFrom( c );
            }
            else if( Boolean.TYPE == clazz )
            {
                return Boolean.class.isAssignableFrom( c );
            }
            else if( Byte.TYPE == clazz )
            {
                return Byte.class.isAssignableFrom( c );
            }
            else if( Short.TYPE == clazz )
            {
                return Short.class.isAssignableFrom( c );
            }
            else if( Long.TYPE == clazz )
            {
                return Long.class.isAssignableFrom( c );
            }
            else if( Float.TYPE == clazz )
            {
                return Float.class.isAssignableFrom( c );
            }
            else if( Double.TYPE == clazz )
            {
                return Double.class.isAssignableFrom( c );
            }
            else
            {
                final String error =
                  "Primitive type ["
                  + c.getName()
                  + "] not supported.";
                throw new RuntimeException( error );
            }
        }
        else
        {
            return clazz.isAssignableFrom( c );
        }
    }

    private Object newInstance( Constructor constructor, Object[] arguments )
      throws RepositoryException, InvocationTargetException
    {
        try
        {
            Object instance = constructor.newInstance( arguments );
            //getMonitor().pluginInstantiated( instance );
            return instance;
        }
        catch( InvocationTargetException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Cannot create an instance of ["
              + constructor.getDeclaringClass().getName()
              + "] due to an instantiation failure.";
            throw new RepositoryException( error, e );
        }
    }

    private Constructor getSingleConstructor( Class clazz ) throws RepositoryException
    {
        if( null == clazz )
        {
            throw new NullArgumentException( "clazz" );
        }
        Constructor[] constructors = clazz.getConstructors();
        if( constructors.length < 1 )
        {
            final String error =
              "Target class ["
              + clazz.getName()
              + "] does not declare a public constructor.";
            throw new RepositoryException( error );
        }
        else if( constructors.length > 1 )
        {
            final String error =
              "Target class ["
              + clazz.getName()
              + "] declares multiple public constructors.";
            throw new RepositoryException( error );
        }
        else
        {
            return constructors[0];
        }
    }

   /**
    * Constructs an empty array instance.
    * @param clazz the array class
    * @return the empty array instance
    * @exception RepositoryException if an error occurs
    */
    private Object[] getEmptyArrayInstance( Class clazz ) throws RepositoryException
    {
        try
        {
            return (Object[]) Array.newInstance( clazz.getComponentType(), 0 );
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to construct an empty array for the class: "
              + clazz.getName();
            throw new RepositoryException( error, e );
        }
    }

   /**
    * Load a factory class using a supplied classloader and factory classname.
    * @param classloader the classloader to load the class from
    * @param classname the plugin classname
    * @return the factory class
    * @exception RepositoryException if a factory class loading error occurs
    * @exception NullArgumentException if the supplied classloader or classname is null
    */
    protected Class loadPluginClass( ClassLoader classloader, String classname )
        throws RepositoryException, NullArgumentException
    {
        if( null == classloader )
        {
            throw new NullArgumentException( "classloader" );
        }
        if( null == classname )
        {
            throw new NullArgumentException( "classname" );
        }

        try
        {
            return classloader.loadClass( classname );
        }
        catch( ClassNotFoundException e )
        {
            StringBuffer buffer = new StringBuffer();
            packClassLoader( buffer, classloader );
            final String error = 
              "Plugin class not found."
              + "Class: " + classname 
              + "\n" 
              + buffer.toString();
            throw new RepositoryException( error, e );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to load class: ["
              + classname
              + "].";
            throw new RepositoryException( error, e );
        }
    }
    
    private void packClassLoader( StringBuffer buffer, ClassLoader classloader )
    {
        if( classloader instanceof StandardClassLoader )
        {
            StandardClassLoader loader = (StandardClassLoader) classloader;
            buffer.append( loader.toString( true ) );
        }
        else
        {
            buffer.append( "Classloader: " + classloader.toString() );
        }
    }
}
