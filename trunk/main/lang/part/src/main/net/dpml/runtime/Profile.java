/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.runtime;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import dpml.lang.DOM3DocumentBuilder;

import net.dpml.util.Resolver;
import dpml.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Component interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class Profile
{
    private static final DOM3DocumentBuilder BUILDER = new DOM3DocumentBuilder();

    private Class m_clazz;
    private Element m_element;
    private ContextDirective m_context;
    private PartsDirective m_parts;
    
    Profile( Class<?> c, String path, Resolver resolver ) throws IOException
    {
        m_element = getProfileElement( c );
        ClassLoader classloader = c.getClassLoader();
        m_context = getContextProfile( classloader, m_element, resolver );
        m_parts = getPartsProfile( classloader, m_element, resolver, path );
    }
    
    ContextDirective getContextDirective()
    {
        return m_context;
    }

    PartsDirective getPartsDirective()
    {
        return m_parts;
    }

    private static Element getProfileElement( Class<?> c ) throws IOException
    {
        ClassLoader classloader = c.getClassLoader();
        String path = c.getName().replace( ".", "/" );
        String profile = path + ".xprofile";
        URL url = classloader.getResource( profile );
        if( null != url )
        {
            try
            {
                URI uri = url.toURI();
                final Document document = BUILDER.parse( uri );
                return document.getDocumentElement();
            }
            catch( Exception e )
            {
                final String error = 
                  "Bad url: " + url;
                IOException ioe = new IOException( error );
                ioe.initCause( e );
                throw ioe;
            }
        }
        else
        {
            return null;
        }
    }

    private static ContextDirective getContextProfile( 
      ClassLoader classloader, Element profile, Resolver resolver ) throws IOException
    {
        if( null == profile )
        {
            return null;
        }
        Element context = ElementHelper.getChild( profile, "context" );
        return new ContextDirective( classloader, context, resolver );
    }
    
    private static PartsDirective getPartsProfile( 
      ClassLoader classloader, Element profile, Resolver resolver, String path ) throws IOException
    {
        if( null == profile )
        {
            return null;
        }
        Element partsElement = ElementHelper.getChild( profile, "parts" );
        return new PartsDirective( classloader, partsElement, resolver, path );
    }
}

