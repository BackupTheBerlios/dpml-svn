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

package org.apache.metro.facilities.reflector.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.List;

import org.apache.metro.facilities.reflector.ReflectorService;
import org.apache.metro.facilities.reflector.ReflectionException;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

final class RequestContext
{
    HttpRequest req;
    HttpResponse res;
    PrintWriter out;
    
    String      objectname;
    
    long        start;
    boolean     debug;


    RequestContext( String path,
                           HttpRequest request,
                           HttpResponse response,
                           String encoding
    )
        throws IOException
    {
        start = System.currentTimeMillis();

        req = request;
        res = response;
        OutputStreamWriter osw = new OutputStreamWriter( response.getOutputStream(), encoding );
        
        out = new PrintWriter( osw, true );
        
        List dblist = req.getParameterValues( "debug" );
        debug = (dblist != null) && ( dblist.size() > 0 );
        
        String str = path;
        if( str.startsWith("//") )
            str = str.substring(2);
        if( str.startsWith("/") )
            str = str.substring(1);

        objectname = decode( str );
        objectname = replace( objectname, '/', '.' );
        
        if( objectname.endsWith( "/" ) )
            objectname = objectname.substring( 0, objectname.length()-1 );
        
        if( debug )
            out.print( "object=" + objectname + "<hr>" );
    }

    void dispose()
        throws IOException
    {
        if( out != null )
        {
            out.flush();
            out.close();
        }
    }
        
    
    private String replace( String text, char original, char replacement )
    {
        StringBuffer buf = new StringBuffer();
        int mode = 0;

        for( int i=0 ; i < text.length() ; i++ )
        {
            char ch = text.charAt(i);
            switch( mode )
            {
            case 0:
                if( ch == '"' )
                    mode = 1;
                else if( ch == '\'' )
                    mode = 2;
                else if( ch == original )
                    ch = replacement;
                buf.append(ch);
                break;
            case 1:
                if( ch == '"' )
                    mode = 0;
                buf.append(ch);
                break;
            case 2:
                if( ch == '\'' )
                    mode = 0;
                buf.append(ch);
                break;
            }
        }
        return buf.toString();
    }

    private String decode( String text )
    {
        StringBuffer buf = new StringBuffer();
        for( int i=0 ; i < text.length() ; i++ )
        {
            char ch = text.charAt(i);
            if( ch == '{' )
            {
                int pos = text.indexOf( '}', i );
                String numtxt = text.substring( i+1, pos );
                int num = Integer.parseInt( numtxt );
                buf.append( (char) num );
                i = pos;
            }
            else
                buf.append( ch );
        }
        return buf.toString();
    }
    
    
}
