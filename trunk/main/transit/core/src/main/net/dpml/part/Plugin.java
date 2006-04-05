/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.part;

import java.io.Writer;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;
import java.beans.Expression;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import net.dpml.lang.Value;
import net.dpml.lang.Logger;
import net.dpml.lang.Classpath;
import net.dpml.lang.Construct;

/**
 * Plugin part strategy implementation datatype.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Plugin extends Part
{
    private final String m_classname;
    private final Value[] m_params;
    
   /**
    * Creation of an new plugin datatype.
    * @param logger the assigned logging channel
    * @param info the part info descriptor
    * @param classpath the classpath descriptor
    * @param classname the target class
    * @exception IOException if an I/O error occurs
    */ 
    public Plugin( 
      Logger logger, Info info, Classpath classpath, String classname )
      throws IOException
    {
        this( logger, info, classpath, classname, new Value[0] );
    }
    
   /**
    * Creation of an new plugin datatype.
    * @param logger the assigned logging channel
    * @param info the part info descriptor
    * @param classpath the classpath descriptor
    * @param classname the target class
    * @param params an array of default value arguments
    * @exception IOException if an I/O error occurs
    */ 
    public Plugin( 
      Logger logger, Info info, Classpath classpath, String classname, Value[] params )
      throws IOException
    {
        super( logger, info, classpath );
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if( null == params )
        {
            throw new NullPointerException( "params" );
        }
        m_classname = classname;
        m_params = params;
    }
    
   /**
    * Return the part content or null if the result type is unresolvable 
    * relative to the supplied class argument. 
    * @param c the content class
    * @return the content
    * @exception IOException if an IO error occurs
    */
    protected Object getContent( Class c ) throws IOException
    {
        if( Class.class.equals( c ) )
        {
            return getPluginClass();
        }
        else
        {
            return super.getContent( c );
        }
    }
    
   /**
    * Get the target classname.
    * @return the classname
    */ 
    public String getClassname()
    {
        return m_classname;
    }
    
   /**
    * Get the array of default constructor values.
    * @return the value array
    */ 
    public Value[] getValues()
    {
        return m_params;
    }
    
   /**
    * Get the default plugin class.
    * @return the plugin class
    */
    public Class getPluginClass()
    {
        ClassLoader classloader = getClassLoader();
        String classname = getClassname();
        try
        {
            return classloader.loadClass( classname );
        }
        catch( ClassNotFoundException e )
        {
            final String error = 
              "Plugin class [" + classname + "] not found.";
            throw new IllegalStateException( error );
        }
    }
    
   /**
    * Instantiate a value.
    * @param args supplimentary arguments
    * @return the resolved instance
    * @exception Exception if a deployment error occurs
    */
    public Object instantiate( Object[] args ) throws Exception
    {
        ClassLoader classloader = getClassLoader();
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( classloader );
        try
        {
            Value[] values = getValues();
            Class c = getPluginClass();
            Object[] params = Construct.getArgs( null, values, args );
            return instantiate( c, params );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( context );
        }
    }
    
   /**
    * Test if this instance is equal to the supllied object.
    * @param other the other object
    * @return true if the supplied object is equal to this instance
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) )
        {
            if( other instanceof Plugin )
            {
                Plugin plugin = (Plugin) other;
                if( !m_classname.equals( plugin.m_classname ) )
                {
                    return false;
                }
                else
                {
                    return Arrays.equals( m_params, plugin.m_params );
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Get the has code for this instance.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = m_classname.hashCode();
        for( int i=0; i<m_params.length; i++ )
        {
            hash ^= m_params[i].hashCode();
        }
        return hash;
    }
    
    
   /**
    * Encode the pluginstrategy to XML.
    * @param writer the output stream writer
    * @param pad the character offset
    * @exception IOException if an I/O error occurs
    */
    protected void encodeStrategy( Writer writer, String pad ) throws IOException
    {
        String classname = getClassname();
        writer.write( "\n" + pad + "<strategy xsi:type=\"plugin\" class=\"" );
        writer.write( classname );
        writer.write( "\"" );
        if( getValues().length > 0 )
        {
            writer.write( ">" );
            Value[] values = getValues();
            VALUE_ENCODER.encodeValues( writer, values, pad + "  " );
            writer.write( "\n" + pad + "</strategy>" );
        }
        else
        {
            writer.write( "/>" );
        }
    }

   /**
    * Create a factory using a supplied class and command line arguments.
    *
    * @param clazz the the factory class
    * @param args the command line args
    * @return the plugin instance
    * @exception IOException if a plugin creation error occurs
    * @exception InvocationTargetException if a plugin constructor invocation error occurs
    */
    public static Object instantiate( Class clazz, Object[] args ) throws IOException, InvocationTargetException
    {
        if( null == clazz )
        {
            throw new NullPointerException( "clazz" );
        }
        if( null == args )
        {
            throw new NullPointerException( "args" );
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
                throw new NullPointerException( error );
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
            catch( PartException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                final String error = 
                "Class instantiation error [" + clazz.getName() + "]";
                throw new PartException( error, e );
            }
        }
    }
    
    private static Object instantiate( Constructor constructor, Object[] args ) 
      throws PartException, InvocationTargetException
    {
        Object[] arguments = populate( constructor, args );
        return newInstance( constructor, arguments );
    }
    
    private static Object[] populate( Constructor constructor, Object[] args ) throws PartException
    {
        if( null == constructor )
        {
            throw new NullPointerException( "constructor" );
        }
        if( null == args )
        {
            throw new NullPointerException( "args" );
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
                    throw new PartException( error );
                }
            }
        }
        return arguments;
    }

    private static boolean isAssignableFrom( Class clazz, Class c )
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

    private static Object newInstance( Constructor constructor, Object[] arguments )
      throws PartException, InvocationTargetException
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
            throw new PartException( error, e );
        }
    }
    
    private static Constructor getSingleConstructor( Class clazz ) throws PartException
    {
        if( null == clazz )
        {
            throw new NullPointerException( "clazz" );
        }
        Constructor[] constructors = clazz.getConstructors();
        if( constructors.length < 1 )
        {
            final String error =
              "Target class ["
              + clazz.getName()
              + "] does not declare a public constructor.";
            throw new PartException( error );
        }
        else if( constructors.length > 1 )
        {
            final String error =
              "Target class ["
              + clazz.getName()
              + "] declares multiple public constructors.";
            throw new PartException( error );
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
    private static Object[] getEmptyArrayInstance( Class clazz ) throws PartException
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
            throw new PartException( error, e );
        }
    }
}
