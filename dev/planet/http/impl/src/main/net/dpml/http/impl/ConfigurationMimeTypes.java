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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.dpml.configuration.Configurable;
import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;
import net.dpml.http.MimeTypes;

/**
 * @metro.component name="mimetypes-configuration" lifestyle="singleton"
 * @metro.service  type="net.dpml.http.MimeTypes"
 */
public class ConfigurationMimeTypes
    implements Configurable, MimeTypes
{
    private HashMap m_MimeTypeToExtMap;
    private HashMap m_ExtToMimeTypeMap;

    public ConfigurationMimeTypes()
    {
        m_MimeTypeToExtMap = new HashMap();
        m_ExtToMimeTypeMap = new HashMap();
    }

    public void configure( Configuration conf )
        throws ConfigurationException
    {
        Configuration typesConf = conf.getChild( "mimetypes" );
        configureTypes( typesConf );
    }

    private void configureTypes( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] children = conf.getChildren( "type" );
        for( int i=0 ; i < children.length ; i++ )
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
        for( int i = 0 ; i < extConfs.length ; i++ )
        {
            String ext = extConfs[i].getValue();
            extList.add( ext );
            m_ExtToMimeTypeMap.put( ext, mime );
        }
        String[] exts = new String[ extList.size() ];
        extList.toArray( exts );
        m_MimeTypeToExtMap.put( mime, exts );
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
}

