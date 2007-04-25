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

package net.dpml.lang;

import dpml.lang.Part;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;

import net.dpml.util.Logger;

import dpml.util.DefaultLogger;

/**
 * Abstract component deployment strategy.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class Strategy implements Comparable<Strategy>
{
    private static final Logger LOGGER = new DefaultLogger( "dpml.lang" );
    
   /**
    * Creation of a new management strategy.
    *
    * @param subject the implementation class to be managed
    * @param registry the service registry
    * @param name the path under which the strategy will be established
    * @return the management strategy
    * @exception Exception if a general loading error occurs
    */
    public static Strategy load( Class<?> subject, ServiceRegistry registry, String name ) throws Exception
    {
        if( null == subject )
        {
            throw new NullPointerException( "subject" );
        }
        StrategyHandler handler = PartContentHandler.getStrategyHandler( subject );
        Strategy strategy = handler.newStrategy( subject, name );
        if( null == registry )
        {
            StandardServiceRegistry standard = new StandardServiceRegistry();
            strategy.initialize( standard );
        }
        else
        {
            strategy.initialize( registry );
        }
        return strategy;
    }
    
   /**
    * Load a strategy defined by the supplied uri.
    *
    * @param uri the source uri to a part definition
    * @return the strategy
    * @exception Exception if a general loading error occurs
    * @exception NullPointerException if the uri argument is null
    */
    public static Strategy load( URI uri ) throws Exception, NullPointerException
    {
        return load( null, null, uri, null );
    }
    
   /**
    * Load a strategy defined by the supplied uri, name, classloader and service 
    * registry, and return a value assignable to the supplied type.
    *
    * @param classloader the anchor classloader
    * @param registry the service registry
    * @param uri the source uri to a part definition
    * @param name the path under which the strategy will be established
    * @return an instance of the supplied type
    * @exception Exception if a general loading error occurs
    * @exception NullPointerException if the uri argument is null
    */
    public static Strategy load( 
      ClassLoader classloader, ServiceRegistry registry, URI uri, String name ) throws Exception, NullPointerException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        Transit transit = Transit.getInstance();
        URL url = Artifact.toURL( uri );
        URLConnection connection = url.openConnection();
        Part part = PartContentHandler.getPartContent( classloader, connection, name, true );
        Strategy strategy = part.getStrategy();
        if( null == registry )
        {
            StandardServiceRegistry standard = new StandardServiceRegistry();
            strategy.initialize( standard );
        }
        else
        {
            strategy.initialize( registry );
        }
        return strategy;
    }
    
    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

    private ClassLoader m_classloader;
    
    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------
    
   /**
    * Creation of a new deployment strategy.
    * @param classloader the classloader
    */
    protected Strategy( ClassLoader classloader )
    {
        m_classloader = classloader;
    }
    
    //--------------------------------------------------------------------
    // public operations
    //--------------------------------------------------------------------

   /**
    * Return the classloader establised by the strategy implementation.
    * @return the classloader
    */
    public ClassLoader getClassLoader()
    {
        return m_classloader;
    }
    
   /**
    * Return the priority assigned to this strategy.
    * @return the priority value
    */
    public abstract int getPriority();
    
   /**
    * Return true if this strategy is a candidate with result to the 
    * supply of an instance assignable to the supplied type.
    * 
    * @param type the requested type
    * @return type if this strategy is a condidate
    */
    public abstract boolean isaCandidate( Class<?> type );
    
   /**
    * Instantiate a service returning an instance assigned to the supplied type.
    *
    * @param type the return type
    * @return an instance of the type
    */
    public abstract <T>T getInstance( Class<T> type );
    
   /**
    * Write the strategy to the supplied buffer in XML format.
    * @param buffer the output buffer
    * @param key the optional identifying key
    * @exception IOException if an IO error occurs
    */
    public abstract void encode( Buffer buffer, String key ) throws IOException;

   /**
    * Composite strategies are strategy implementations that contain subidary
    * strategies.  During lookup operations, a subsidiary strategy may request 
    * a service from the enclosing strategy.  To enable traversal from a subsidary
    * to enclosing context a service registry shall be provided to all subsidiary
    * strategies.
    * 
    * @param registry the service registry
    */
    public abstract void initialize( ServiceRegistry registry );
    
   /**
    * Return the short name of this strategy.
    * @return the name
    */
    public abstract String getName();

   /**
    * Compares a supplied strategy with this strategy.  If the supplied
    * strategy has a priority greater than this strategy the returned value
    * is 1, otherwise if the supplied strategy has a priority lower than this
    * strategy then the return value is -1,otherwise the returned value is 0.
    *
    * @param strategy the strategy to evaluate relative to this strategy
    * @return the priority comparative index
    */
    public int compareTo( Strategy strategy )
    {
        int n = getPriority();
        int m = strategy.getPriority();
        if( n > m )
        {
            return -1;
        }
        else if( n == m )
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    //--------------------------------------------------------------------
    // protected operations
    //--------------------------------------------------------------------
    
   /**
    * Return a value assignable to the supplied type or null if the type
    * cannot be resolved from this strategy.
    * @param c the target class
    * @return an instance of the class or null
    * @exception IOException if an IO error occurs
    */
    public abstract <T>T getContentForClass( Class<T> c ) throws IOException;
    
    //--------------------------------------------------------------------
    // private implementation
    //--------------------------------------------------------------------
    
}
