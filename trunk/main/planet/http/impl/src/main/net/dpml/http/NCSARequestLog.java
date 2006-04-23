/*
 * Copyright 2004 Niclas Hedman.
 * Copyright 2005-2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.http;

import java.io.File;

import net.dpml.util.PropertyResolver;

/** 
 * Wrapper for the Jetty NCSA request logger.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class NCSARequestLog extends org.mortbay.jetty.NCSARequestLog
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Get the array of ignore paths.
        * @param value the default value
        * @return the ignore path array
        */
        String[] getIgnorePaths( String[] value );
        
       /**
        * Return the append policy.
        * @param value the default policy value
        * @return the resolved value
        */
        boolean getAppend( boolean value );
        
       /**
        * Return the extended policy.
        * @param value the default policy value
        * @return the resolved value
        */
        boolean getExtended( boolean value );
        
       /**
        * Return the prefer-proxy-for-address policy.
        * @param value the default policy value
        * @return the resolved value
        */
        boolean getPreferProxiedForAddress( boolean value );
        
       /**
        * Return the log filename.
        * @param value the default filename value
        * @return the resolved filename value
        */
        String getFilename( String value );
        
       /**
        * Return the log date format.
        * @param value the default value
        * @return the resolved value
        */
        String getLogDateFormat( String value );
        
       /**
        * Return the log time zone.
        * @param value the default value
        * @return the resolved value
        */
        String getLogTimeZone( String value );
        
       /**
        * Return the retain days value.
        * @param value the default value
        * @return the resolved value
        */
        int getRetainDays( int value );
        
       /**
        * Get the log latency policy. Ig true the request processing latency will
        * included in the reqwuest log messages.
        * @param flag the log latency default value
        * @return the resulted log latency policy
        */
        boolean getLogLatency( boolean flag );
        
       /**
        * Get the preference policy concerning address registration.
        * @param flag the proxy preferred policy - if tue the proxy 
        *   address will be used in preference to the request header address
        * @return the resulted proxy preferred policy
        */
        boolean getUseProxyPreference( boolean flag );
    }

   /**
    * Creation of a new NCSA request log.
    * @param context the deployment context
    */
    public NCSARequestLog( Context context )
    {
        boolean append = context.getAppend( false );
        setAppend( append );

        boolean extended = context.getExtended( false );
        setExtended( extended );

        boolean preferProxiedFor = context.getPreferProxiedForAddress( false );
        setPreferProxiedForAddress( preferProxiedFor );

        String filename = context.getFilename( null );
        if( filename != null )
        {
            filename = PropertyResolver.resolve( System.getProperties(), filename );
            File file = new File( filename );
            File parent = file.getParentFile();
            parent.mkdirs();
            setFilename( filename );
        }

        String dateformat = context.getLogDateFormat( null );
        if( dateformat != null )
        {
            setLogDateFormat( dateformat );
        }

        String[] ignorepaths = context.getIgnorePaths( null );
        if( ignorepaths != null )
        {
            setIgnorePaths( ignorepaths );
        }

        String timezone = context.getLogTimeZone( null );
        if( timezone != null )
        {
            setLogTimeZone( timezone );
        }

        int retain = context.getRetainDays( -1 );
        if( retain > 0 )
        {
            setRetainDays( retain );
        }
        
        boolean recordLatencyPolicy = context.getLogLatency( false );
        setLogLatency( recordLatencyPolicy );
        
        boolean useProxyAddressPolicy = context.getUseProxyPreference( false );
        setPreferProxiedForAddress( useProxyAddressPolicy );
    }
}
