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

package org.apache.avalon.repository.main ;

import java.lang.reflect.Constructor;

import org.apache.avalon.repository.RepositoryException;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.provider.InitialContext;


/**
 * Application and component bootstrapper used to instantiate, and or invoke
 * Classes and their methods within newly constructed Repository ClassLoaders.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class AbstractBuilder
{
   /**
    * Load a factory class using a supplied classloader and factory classname.
    * @param classloader the classloader to load the class from
    * @param factory the factory classname
    * @return the factory class
    * @exception RepositoryException if a factory class loading error occurs
    */
    protected Class loadFactoryClass( ClassLoader classloader, String factory )
        throws RepositoryException
    {
        try
        {
            return classloader.loadClass( factory );
        }
        catch( ClassNotFoundException e )
        {
            final String error = 
              "Could not find the factory class: " + factory;
            throw new RepositoryException( error, e );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to load factory class: [" 
              + factory 
              + "].";
            throw new RepositoryException( error, e );
        }
    }

   /**
    * <p>Create a factory delegate using a supplied class and command line arguemnts.
    * The implementation will conduct an ordered search for a constructor matching
    * one of the four standard constructor patterns.</p>
    * <ul>
    * <li>[FactoryClass]( InitialContext context, ClassLoader loader )</li>
    * <li>[FactoryClass]( InitialContext context )</li>
    * <li>[FactoryClass]( ClassLoader loader )</li>
    * <li>[FactoryClass]( )</li>
    * </ul>
    * 
    * @param classloader the classloader
    * @param clazz the the factory class
    * @param context the inital repository context
    * @return the instantiated factory
    * @exception RepositoryException if a factory creation error occurs
    */
    protected Factory createDelegate( 
      ClassLoader classloader, Class clazz, InitialContext context ) 
      throws RepositoryException
    {
        if( null == classloader ) throw new NullPointerException( "classloader" );
        if( null == clazz ) throw new NullPointerException( "clazz" );
        if( null == context ) throw new NullPointerException( "context" );

        try
        {
            Constructor constructor = 
              clazz.getConstructor( 
                new Class[]{ InitialContext.class, ClassLoader.class } );
            return createFactory( 
              constructor, new Object[]{ context, classloader } );
        }
        catch( NoSuchMethodException e )
        {
            try
            {
                Constructor constructor = 
                  clazz.getConstructor( 
                    new Class[]{ InitialContext.class } );
                return createFactory( 
                  constructor, new Object[]{ context } );
            }
            catch( NoSuchMethodException ee )
            {
                try
                {
                    Constructor constructor = 
                      clazz.getConstructor( 
                        new Class[]{ ClassLoader.class } );
                    return createFactory( 
                      constructor, new Object[]{ classloader } );
                }
                catch( NoSuchMethodException eee )
                {
                    try
                    {
                        Constructor constructor = 
                          clazz.getConstructor( 
                            new Class[0] );
                        return createFactory( 
                          constructor, new Object[0] );
                    }
                    catch( NoSuchMethodException eeee )
                    {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append( "Supplied factory class [" );
                        buffer.append( clazz.getName() );
                        buffer.append( 
                          " ] does not implement a recognized constructor." );
                        throw new RepositoryException( buffer.toString() );
                    }
                }
            }
        }
    }

   /**
    * Instantiation of a factory instance using a supplied constructor 
    * and arguments.
    * 
    * @param constructor the factory constructor
    * @param args the constructor arguments
    * @return the factory instance
    * @exception RepositoryException if an instantiation error occurs
    */
    private Factory createFactory( 
      Constructor constructor, Object[] args ) 
      throws RepositoryException
    {
        Class clazz = constructor.getDeclaringClass();
        try
        {
            return (Factory) constructor.newInstance( args );
        }
        catch( Throwable e )
        {
            final String error = 
              "Error while attempting to instantiate the factory: [" 
              + clazz.getName() 
              + "]."; 
            throw new RepositoryException( error, e );
        }
    }
}
