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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;
import net.dpml.http.spi.MimeTypes;

/**
 * Mime type registry configurator.
 */
public class ConfigurationMimeTypes implements MimeTypes
{
    private final HashMap m_mimeTypeToExtMap = new HashMap();
    private final HashMap m_extToMimeTypeMap = new HashMap();

   /**
    * Creation of a new ConfigurationMimeTypes instance.
    * @param conf the mime type configuration
    * @exception ConfigurationException if a configuration error occurs
    */
    public ConfigurationMimeTypes( Configuration conf )
        throws ConfigurationException
    {
        Configuration typesConf = conf.getChild( "mimetypes" );
        configureTypes( typesConf );
    }

    private void configureTypes( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] children = conf.getChildren( "type" );
        for( int i=0; i<children.length; i++ )
        {
            configureType( children[i] );
        }
    }

    private void configureType( Configuration conf )
        throws ConfigurationException
    {
        ArrayList extList = new ArrayList();
        String mime = conf.getChild( "mime" ).getValue();
        Configuration[] extConfs = conf.getChildren( "ext" );
        for( int i=0; i<extConfs.length; i++ )
        {
            String ext = extConfs[i].getValue();
            extList.add( ext );
            m_extToMimeTypeMap.put( ext, mime );
        }
        String[] exts = new String[ extList.size() ];
        extList.toArray( exts );
        m_mimeTypeToExtMap.put( mime, exts );
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
}

