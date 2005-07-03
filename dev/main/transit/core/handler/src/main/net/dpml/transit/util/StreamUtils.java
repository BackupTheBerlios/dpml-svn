/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman
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

package net.dpml.transit.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;

import net.dpml.transit.NullArgumentException;

import net.dpml.transit.monitors.NetworkMonitor;

public class StreamUtils
{
   /**
    * Buffer size.
    */
    private static final int BUFFER_SIZE = 102400;


   /** Copy a stream.
    * @param src the source input stream
    * @param dest the destination output stream
    * @param closeStreams TRUE if the streams should be closed on completion
    * @exception IOException if an IO error occurs
    */
    static public void copyStream( InputStream src, OutputStream dest, boolean closeStreams )
        throws IOException, NullArgumentException
    {
        copyStream( null, null, 0, src, dest, closeStreams );
    }

   /** Copy a stream.
    * @param source the source url
    * @param expected the expected size in bytes
    * @param src the source input stream
    * @param dest the destination output stream
    * @param closeStreams TRUE if the streams should be closed on completion
    * @exception IOException if an IO error occurs
    */
    static public void copyStream( NetworkMonitor monitor, URL source, int expected,
                            InputStream src, OutputStream dest, boolean closeStreams )
        throws IOException, NullArgumentException
    {
        if( src == null )
        {
            throw new NullArgumentException( "src" );
        }

        if( dest == null )
        {
            throw new NullArgumentException( "dest" );
        }

        int length;
        int count = 0; // cumulative total read
        byte[] buffer = new byte[BUFFER_SIZE];
        if( dest instanceof BufferedOutputStream == false )
        {
            dest = new BufferedOutputStream( dest );
        }
        if( src instanceof BufferedInputStream == false )
        {
            src = new BufferedInputStream( src );
        }

        try
        {
            while ( ( length = src.read( buffer ) ) >= 0 )
            {
                count = count + length;
                dest.write( buffer, 0, length );
                if( null != monitor )
                {
                    monitor.notifyUpdate( source, expected, count );
                }
            }
        }
        finally
        {
            if( closeStreams )
            {
                try
                {
                    src.close();
                }
                catch( Throwable e )
                {
                    e.printStackTrace();
                }

                try
                {
                    dest.close();
                }
                catch( Throwable e )
                {
                    e.printStackTrace();
                }
            }
            if( null != monitor )
            {
                monitor.notifyCompletion( source );
            }
        }
    }

    static public boolean compareStreams( InputStream in1, InputStream in2 )
        throws IOException
    {
        boolean result = true;
        do
        {
            int v1 = in1.read();
            int v2 = in2.read();
            if( v1 != v2 )
            {
                return false;
            }
            if( v1 == -1 )
                break;
        } while( true );
        return result;
    }
}
