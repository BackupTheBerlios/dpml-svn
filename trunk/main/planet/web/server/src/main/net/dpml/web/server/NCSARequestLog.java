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
package net.dpml.web.server;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.dpml.transit.util.PropertyResolver;

/** 
 * Wrapper for the Jetty NCSA request logger.
 */
public class NCSARequestLog extends org.mortbay.jetty.NCSARequestLog
{
   /**
    * Component context.
    */
    public interface Context
    {
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
        * Return the ignore paths value.
        * @param value the default value
        * @return the resolved value
        */
        String getIgnorePaths( String value );
        
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

        String ignorepaths = context.getIgnorePaths( null );
        if( ignorepaths != null )
        {
            String[] paths = tokenize( ignorepaths );
            setIgnorePaths( paths );
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
    }
    
   /**
    * Tokenize the supplied string using a ' ,' delimiter.
    * @param string the string to tokenize
    * @return the array of values
    */
    private static String[] tokenize( String string )
    {
        ArrayList result = new ArrayList();
        StringTokenizer st = new StringTokenizer( string, " ,", false );
        while( st.hasMoreTokens() )
        {
            result.add( st.nextToken() );
        }
        String[] retVal = new String[ result.size() ];
        result.toArray( retVal );
        return retVal;
    }
}