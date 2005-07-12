/*
 * Copyright 2004 Niclas Hedman.
 * Copyright 2005 Stephen McConnell.
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
package net.dpml.http.impl;

import java.io.File;

import net.dpml.activity.Startable;

import net.dpml.transit.util.PropertyResolver;

import org.mortbay.http.NCSARequestLog;
import org.mortbay.http.RequestLog;


/** Wrapper for the Jetty NCSA request logger.
 *
 */
public class NcsaRequestLog extends NCSARequestLog
    implements RequestLog
{
    public interface Context
    {
        boolean getAppend( boolean value );
        boolean getBuffered( boolean value );
        boolean getExtended( boolean value );
        boolean getPreferProxiedForAddress( boolean value );
        String getFilename( String value );
        String getLogDateFormat( String value );
        String getIgnorePaths( String value );
        String getLogTimeZone( String value );
        int getRetainDays( int value );
    }

    public NcsaRequestLog( Context context )
    {
        boolean append = context.getAppend( false );
        setAppend( append );

        boolean buffered = context.getBuffered( false );
        setBuffered( buffered );

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
            String[] paths = StringUtils.tokenize( ignorepaths );
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
}
