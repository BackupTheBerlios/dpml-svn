/*
 * Copyright 2006-2007 Stephen McConnell.
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

package dpml.transit;

import dpml.util.PID;

import java.io.File;
import java.net.URL;

import net.dpml.transit.Artifact;
import net.dpml.transit.Monitor;

import net.dpml.util.Logger;

/**
 * Event monitor that redirects to an underlying logging channel.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardMonitor implements Monitor
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The assigned logging channel.
    */
    private final Logger m_logger;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new adaptive monitor.
    * @param logger the underlying logging channel
    */
    public StandardMonitor( Logger logger )
    {
        m_logger = logger;
    }
    
    // ------------------------------------------------------------------------
    // Monitor
    // ------------------------------------------------------------------------

   /**
    * Notify the monitor that an artifact has been requested.
    * @param artifact the requested artifact
    */
    public void resourceRequested( Artifact artifact )
    {
    }

   /**
    * Notify the monitor that an artifact has been added to the local cache.
    * @param resource the url of the resource added to the local cache
    * @param localFile the local file resident in the cache
    */
    public void addedToLocalCache( URL resource, File localFile )
    {
    }

   /**
    * Notify the monitor that an artifact in the local cache has been updated.
    * @param resource the url of the resource updating the local cache
    * @param localFile the local file that has been updated
    */
    public void updatedLocalCache( URL resource, File localFile )
    {
    }

   /**
    * Notify the monitor that an artifact has been removed from the local cache.
    * @param resource the url of the resource removed from the local cache
    * @param localFile the local file removed from the cache
    */
    public void removedFromLocalCache( URL resource, File localFile )
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "removed [" + localFile + "] representing [" + resource + "]" );
        }
    }

   /**
    * Notify the monitor of a failed download attempt relative to an identified host.
    * @param host the host raising the fail status
    * @param artifact the requested artifact
    * @param e the exception causing the failure
    */
    public void failedDownloadFromHost( String host, Artifact artifact, Throwable e )
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
              "download failure on ["
              + host + "] for ["
              + artifact
              + "] due to: "
              + e.getMessage() );
        }
    }

   /**
    * Notify the monitor of a failed download attempt.
    * @param artifact the requested artifact
    */
    public void failedDownload( Artifact artifact )
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().warn( "failed to download " + artifact );
        }
    }
    
    // ------------------------------------------------------------------------
    // NetworkMonitor
    // ------------------------------------------------------------------------

   /**
    * Handle the notification of an update in the download status.
    *
    * @param resource the name of the remote resource being downloaded.
    * @param total the expected number of bytes to be downloaded.
    * @param count the number of bytes downloaded.
    */
    public void notifyUpdate( URL resource, int total, int count )
    {
        String path = resource.toString();
        if( path.startsWith( "file:" ) )
        {
            return;
        }
        if( ( null != System.getProperty( "ant.home" ) ) 
          || ( "true".equals( System.getProperty( "dpml.subprocess" ) ) ) )
        {
            if( count == 0 )
            {
                if( getLogger().isInfoEnabled() )
                {
                    getLogger().info( 
                      "downloading [" 
                      + resource 
                      + "] (" 
                      + getFranctionalValue( total ) 
                      + ")" );
                }
            }
            return;
        }
        
        if( getLogger().isInfoEnabled() )
        {
            String max = getFranctionalValue( total );
            String value = getFranctionalValue( count );
            int pad = max.length() - value.length();
            String level = "[INFO]  ";
            String process = getProcessHeader();
            StringBuffer buffer = new StringBuffer( process + level );
            String name = path.substring( path.lastIndexOf( '/' ) + 1 );
            buffer.append( "(" + CATEGORY+ "): " );
            buffer.append( "retrieving: " + name + " " );
            for( int i=0; i < pad; i++ )
            {
                buffer.append( " " );
            }
            buffer.append( value );
            buffer.append( "k/" );
            if( total == 0 )
            {
                buffer.append( "?" );
            }
            else
            {
                buffer.append( max );
                buffer.append( "k\r" );
            }
            if( total == count )
            {
                System.out.println( buffer.toString() );
            }
            else
            {
                System.out.print( buffer.toString() );
            }
        }
    }

   /**
    * Handle the notification of the completion of of download process.
    * @param resource the url of the completed resource
    */
    public void notifyCompletion( URL resource )
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "downloaded: " + resource );
        }
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }

    private String getProcessHeader()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "[" );
        buffer.append( ID.getValue() );
        buffer.append( "        " );
        String tag = buffer.toString();
        return tag.substring( 0, PROCESS_HEADER_WIDTH ) + "] ";
    }
    
   /**
    * Return a string representing the number of kilobytes relative to the supplied
    * total bytes.
    * @param total the byte value
    * @return the string to log
    */
    private static String getFranctionalValue( int total )
    {
        final int offset = 3;

        float realTotal = new Float( total ).floatValue();
        float realK = new Float( KBYTE ).floatValue();
        float r = ( realTotal / realK );

        String value = new Float( r ).toString();
        int j = value.indexOf( "." );
        if( j > -1 )
        {
             int q = value.length();
             int k = q - j;
             if( k > offset )
             {
                 return value.substring( 0, j + offset );
             }
             else
             {
                 return value;
             }
        }
        else
        {
             return value;
        }
    }
    
    private static final int PROCESS_HEADER_WIDTH = 6;

    private static final int KBYTE = 1024;

    private static final String CATEGORY = "dpml.transit";

    private static final PID ID = new PID();
    
}
