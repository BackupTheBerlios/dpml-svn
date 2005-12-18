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

package net.dpml.part;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.beans.Encoder;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.DefaultPersistenceDelegate;

/**
 * Utility class the supports the construction of a part package.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class PartBuilder
{
    private PartBuilder()
    {
        // static utility
    }

   /**
    * Write a part to a file.
    * 
    * @param part the part directive
    * @param file destination file
    */
    public static void write( Part part, File file )
    {
        try
        {
            final ClassLoader current = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( Directive.class.getClassLoader() );
            FileOutputStream output = new FileOutputStream( file );
            BufferedOutputStream buffer = new BufferedOutputStream( output );
            XMLEncoder encoder = new XMLEncoder( buffer );
            encoder.setPersistenceDelegate( URI.class, new URIPersistenceDelegate() );
            encoder.setExceptionListener( 
              new ExceptionListener()
              {
                public void exceptionThrown( Exception e )
                {
                    throw new BuilderRuntimeException( 
                      PART_BUILDER_URI, "Directive encoding failure.", e );
                }
              } );
            try
            {
                encoder.writeObject( part );
            }
            catch( Exception e )
            {
                e.printStackTrace();
                throw new BuilderRuntimeException( 
                  PART_BUILDER_URI, "Part encoding error.", e );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( current );
                encoder.close();
            }
        }
        catch( IOException e )
        {
            final String error = 
              "Internal error while attempting to write part to file ["
              + file 
              + "]";
            throw new BuilderRuntimeException( PART_BUILDER_URI, error, e );
        }
        catch( BuilderRuntimeException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to build the part.";
            throw new BuilderRuntimeException( PART_BUILDER_URI, error, e );
        }
    }
    
   /**
    * Read in a part using the context classloader.
    * @param uri the part uri
    * @return the part datatype
    */
    public static Part readPart( URI uri )
    {
        try
        {
            ClassLoader loader = Part.class.getClassLoader();
            URL url = uri.toURL();
            InputStream input = url.openStream();
            XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
            decoder.setExceptionListener( 
              new ExceptionListener()
              {
                public void exceptionThrown( Exception e )
                {
                    throw new BuilderRuntimeException( 
                      PART_BUILDER_URI, "Part decoding error.", e );
                }
              } );
            Part part = (Part) decoder.readObject();
            if( null == part )
            {
                final String error = 
                  "The decoder returned a null part."
                  + "\nURI: " + uri;
                throw new IllegalStateException( error );
            }
            return part;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while loading part: "
              + uri;
            throw new BuilderRuntimeException( PART_BUILDER_URI, error, e );
        }
    }

   /**
    * Read in a part header.
    * @param uri the part uri
    * @return the part header
    */
    public static PartHeader readPartHeader( URI uri )
    {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try
        {
            ClassLoader loader = Part.class.getClassLoader();
            Thread.currentThread().setContextClassLoader( loader );
            URL url = uri.toURL();
            InputStream input = url.openStream();
            XMLDecoder decoder = 
              new XMLDecoder(
                new BufferedInputStream( input ), 
                null, 
                new ExceptionListener()
                {
                    public void exceptionThrown( Exception e )
                    {
                        //throw new BuilderRuntimeException( 
                        //  PART_BUILDER_URI, "Part header decoding error.", e );
                    }
                } );
            PartHeader header = (PartHeader) decoder.readObject();
            if( null == header )
            {
                final String error = 
                  "The decoder returned a null part header."
                  + "\nURI: " + uri;
                throw new IllegalStateException( error );
            }
            return header;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while loading part header."
              + "\nURI: " + uri;
            throw new BuilderRuntimeException( PART_BUILDER_URI, error, e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( current );
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

   /**
    * Constant builder uri.
    */
    public static final URI PART_BUILDER_URI = setupURI( "@PART-BUILDER-URI@" );

   /**
    * Utility function to create a static uri.
    * @param spec the uri spec
    * @return the uri
    */
    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
}

