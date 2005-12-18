/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.state;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.beans.Encoder;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.DefaultPersistenceDelegate;

/**
 * Utility class the supports the construction of a state graph.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class StateBuilder
{
    private StateBuilder()
    {
        // static utility
    }

   /**
    * Write a graph to a file.  For correct operation the context classloader 
    * must contain both the api classes for the state package together with 
    * any implementation classes using in the state graph implementation.
    * 
    * @param graph the state graph
    * @param file destination file
    */
    public static void write( State graph, File file )
    {
        try
        {
            FileOutputStream output = new FileOutputStream( file );
            BufferedOutputStream buffer = new BufferedOutputStream( output );
            XMLEncoder encoder = new XMLEncoder( buffer );
            encoder.setPersistenceDelegate( URI.class, new URIPersistenceDelegate() );
            encoder.setExceptionListener( 
              new ExceptionListener()
              {
                public void exceptionThrown( Exception e )
                {
                    throw new StateBuilderRuntimeException( 
                      "State graph encoding failure.", e );
                }
              } );
            try
            {
                encoder.writeObject( graph );
            }
            catch( Exception e )
            {
                throw new StateBuilderRuntimeException( 
                  "Part encoding error.", e );
            }
            finally
            {
                encoder.close();
            }
        }
        catch( IOException e )
        {
            final String error = 
              "Internal error while attempting to write state graph to file ["
              + file 
              + "]";
            throw new StateBuilderRuntimeException( error, e );
        }
        catch( StateBuilderRuntimeException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to build the part.";
            throw new StateBuilderRuntimeException( error, e );
        }
    }
    
   /**
    * Read in a graph using the context classloader.
    * @param uri the graph uri
    * @return the state graph
    */
    public static State readGraph( URI uri )
    {
        try
        {
            URL url = uri.toURL();
            InputStream input = url.openStream();
            XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
            decoder.setExceptionListener( 
              new ExceptionListener()
              {
                public void exceptionThrown( Exception e )
                {
                    throw new StateBuilderRuntimeException( 
                      "State graph decoding error.", e );
                }
              } );
            State graph = (State) decoder.readObject();
            if( null == graph )
            {
                final String error = 
                  "The decoder returned a null graph."
                  + "\nURI: " + uri;
                throw new IllegalStateException( error );
            }
            return graph;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while loading graph: "
              + uri;
            throw new StateBuilderRuntimeException( error, e );
        }
    }
    
   /**
    * Utility class used to handle uri persistence.
    */
    public static class URIPersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Return an expression to create a uri.
        * @param old the old value
        * @param encoder the encoder
        * @return the expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            URI uri = (URI) old;
            String spec = uri.toString();
            Object[] args = new Object[]{spec};
            return new Expression( old, old.getClass(), "new", args );
        }
    }
}

