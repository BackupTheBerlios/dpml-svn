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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.avalon.framework.activity.Initializable;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.avalon.http.MimeTypes;

/**
 * @avalon.component name="mimetypes-system" lifestyle="singleton"
 * @avalon.service  type="org.apache.avalon.http.MimeTypes"
 */
public class SystemMimeTypes
    implements Parameterizable, Initializable, MimeTypes
{
    private String m_Filename;
    private HashMap m_MimeTypeToExtMap;
    private HashMap m_ExtToMimeTypeMap;
    
    public SystemMimeTypes()
    {
    }
    
    public void parameterize( Parameters params )
        throws ParameterException
    {
        m_Filename = params.getParameter( "filename", "/etc/mime.types" );
    }
    
    public void initialize()
        throws Exception
    {
        m_MimeTypeToExtMap = new HashMap();
        m_ExtToMimeTypeMap = new HashMap();
        
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        try
        {
            fis = new FileInputStream( m_Filename );
            isr = new InputStreamReader( fis );
            in = new BufferedReader( isr );
            
            String line;
            while( (line = in.readLine() ) != null )
            {
                processLine( line );
            }
        } finally
        {
            if( in != null )
                in.close();
            if( isr != null )
                isr.close();
            if( fis != null )
                fis.close();
        }
    }

    public Map getExtensionMap()
    {
        return m_ExtToMimeTypeMap;
    }
    
    public String getMimeType( String extension )
    {
        return (String) m_ExtToMimeTypeMap.get( extension );
    }
    
    public String[] getExtensions( String mimetype )
    {
        String[] result = (String[]) m_MimeTypeToExtMap.get( mimetype );
        if( result == null )
            result = new String[0];
        return result;
    }
    
    private void processLine( String line )
    {
        if( "".equals( line ) )
            return;

        StringTokenizer st = new StringTokenizer( line, " ", false );
        String mimetype = st.nextToken();
        ArrayList exts = new ArrayList();        
        while( st.hasMoreTokens() )
        {
            String extension = st.nextToken();
            exts.add( extension );
            m_ExtToMimeTypeMap.put( extension, mimetype );
        }
        String[] extensions = new String[ exts.size() ];
        exts.toArray( extensions );
        m_MimeTypeToExtMap.put( mimetype, exts );
    }
} 
