/*
 * Copyright 2004 Niclas Hedman.
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

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import net.dpml.http.spi.MimeTypes;

/**
 * System mime type registry.
 */
public class SystemMimeTypes implements MimeTypes
{
   /**
    * Component deployment context.
    */
    public interface Context
    {
       /**
        * Return the filename of the system mimetypes.
        * @param value the default value
        * @return the system mimetypes filename
        */
        String getFilename( String value );
    }

    private String m_filename;
    private HashMap m_mimeTypeToExtMap;
    private HashMap m_extToMimeTypeMap;

   /**
    * Creation of a new SystemMimeTypes instance.
    * @param context the deployment context
    * @exception IOException if an I/O error occurs
    */
    public SystemMimeTypes( Context context ) throws IOException
    {
        m_filename = context.getFilename( "/etc/mime.types" );

        m_mimeTypeToExtMap = new HashMap();
        m_extToMimeTypeMap = new HashMap();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        try
        {
            fis = new FileInputStream( m_filename );
            isr = new InputStreamReader( fis );
            in = new BufferedReader( isr );

            String line;
            while( ( line = in.readLine() ) != null )
            {
                processLine( line );
            }
        } 
        finally
        {
            if( in != null )
            {
                in.close();
            }
            if( isr != null )
            {
                isr.close();
            }
            if( fis != null )
            {
                fis.close();
            }
        }
    }

   /**
    * Get the MIME types extensions map.
    * @return the extensions map
    */
    public Map getExtensionMap()
    {
        return m_extToMimeTypeMap;
    }

   /**
    * Get the mime type matching the supplied extension.
    * @param extension the extension
    * @return the mime type
    */
    public String getMimeType( String extension )
    {
        return (String) m_extToMimeTypeMap.get( extension );
    }

   /**
    * Return the registered extensions as a String array.
    * @param mimetype the mimetype
    * @return the extensions
    */
    public String[] getExtensions( String mimetype )
    {
        String[] result = (String[]) m_mimeTypeToExtMap.get( mimetype );
        if( result == null )
        {
            result = new String[0];
        }
        return result;
    }

    private void processLine( String line )
    {
        if( "".equals( line ) )
        {
            return;
        }
        StringTokenizer st = new StringTokenizer( line, " ", false );
        String mimetype = st.nextToken();
        ArrayList exts = new ArrayList();
        while( st.hasMoreTokens() )
        {
            String extension = st.nextToken();
            exts.add( extension );
            m_extToMimeTypeMap.put( extension, mimetype );
        }
        String[] extensions = new String[ exts.size() ];
        exts.toArray( extensions );
        m_mimeTypeToExtMap.put( mimetype, exts );
    }
}
