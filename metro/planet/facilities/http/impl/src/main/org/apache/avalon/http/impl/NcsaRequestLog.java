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

package org.apache.avalon.http.impl;

import org.apache.avalon.framework.activity.Startable;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.mortbay.http.NCSARequestLog;
import org.mortbay.http.RequestLog;


/** Wrapper for the Jetty NCSA request logger.
 *
 * @avalon.component name="http-ncsa-log" lifestyle="singleton"
 * @avalon.service type="org.mortbay.http.RequestLog"
 */
public class NcsaRequestLog extends NCSARequestLog
    implements RequestLog, Parameterizable, Startable
{
    public NcsaRequestLog()
    {
    }

    public void parameterize( Parameters params )
        throws ParameterException
    {
        boolean append = params.getParameterAsBoolean( "append", false );
        setAppend( append );
        
        boolean buffered = params.getParameterAsBoolean( "buffered", false );
        setBuffered( buffered );
        
        boolean extended = params.getParameterAsBoolean( "extended", false );
        setExtended( extended );
    
        boolean preferProxiedFor = params.getParameterAsBoolean( "prefer-proxied-for", false );
        setPreferProxiedForAddress( preferProxiedFor );
    
        String filename = params.getParameter( "filename", null );
        if( filename != null )
            setFilename( filename );
    
        String dateformat = params.getParameter( "dateformat", null );
        if( dateformat != null )
            setLogDateFormat( dateformat );
    
        String ignorepaths = params.getParameter( "ignore-paths", null );
        if( ignorepaths != null )
        {
            String[] paths = StringUtils.tokenize( ignorepaths );
            setIgnorePaths( paths );
        }
        
        String timezone = params.getParameter( "timezone", null );
        if( timezone != null )
            setLogTimeZone( timezone );
            
        int retain = params.getParameterAsInteger( "retain-days", -1 );
        if( retain > 0 )
            setRetainDays( retain );
    }
        
    public void start()
        throws Exception
    {
        super.start();
    }
    
    public void stop()
    {
        super.stop();
    }
} 
