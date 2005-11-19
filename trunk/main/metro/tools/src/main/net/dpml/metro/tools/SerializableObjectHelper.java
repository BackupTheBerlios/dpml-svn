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

package net.dpml.metro.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import net.dpml.metro.part.BuilderRuntimeException;

/**
 * A datatype that enables custom part builders.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class SerializableObjectHelper
{
    private SerializableObjectHelper()
    {
        // static
    }

   /**
    * Write a serialized object to file.
    * @param object the object to write to file
    * @param file the destination file
    * @exception IOException if an I/O exception occurs
    */
    public static void write( Serializable object, File file ) throws IOException
    {
        ObjectOutputStream output = null;
        FileOutputStream stream = null;
        try
        {
            file.getParentFile().mkdirs();
            stream = new FileOutputStream( file );
            output = new ObjectOutputStream( stream );
            output.writeObject( object );
        }
        catch( IOException ioe )
        {
            throw ioe;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to write an object."
              + "\ndestination: " + file
              + "\nclass: " + object.getClass().getName()
              + "\nreason: " + e.toString();
            throw new BuilderRuntimeException( ComponentBuilderTask.PART_BUILDER_URI, error, e );
        }
        finally
        {
            closeStream( output );
        }
    }

   /**
    * Write a serializable object to a byte array.
    * @param object the serializable object
    * @return the byte array
    * @exception IOException if an I/O exception occurs
    */
    public static byte[] writeToByteArray( Serializable object ) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream output = null;
        try
        {
            output = new ObjectOutputStream( stream );
            output.writeObject( object );
            return stream.toByteArray();
        }
        catch( IOException ioe )
        {
            throw ioe;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to write object to a byte array."
              + "\nclass: " + object.getClass().getName()
              + "\nreason: " + e.toString();
            throw new BuilderRuntimeException( ComponentBuilderTask.PART_BUILDER_URI, error, e );
        }
        finally
        {
            closeStream( output );
        }
    }

    private static void closeStream( OutputStream out )
    {
        if( null != out )
        {
            try
            {
                out.close();
            }
            catch( IOException ioe )
            {
                boolean ignoreMe = true;
            }
        }
    }
}
