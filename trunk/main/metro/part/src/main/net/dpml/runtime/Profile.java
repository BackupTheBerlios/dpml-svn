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

import net.dpml.annotation.Activation;
import net.dpml.annotation.ActivationPolicy;

import net.dpml.util.Resolver;

import dpml.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A component profile is a datastructure bundled with a class that allows for
 * the declaration of default activation, context and part structure.
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
    private ActivationPolicy m_activation;
    
   /**
    * Creation of a new profile using a supplied class, path and property resolver.
    * @param c the class
    * @param path the path
    * @param resolver the property resolver
    * @exception IOException if an IO error occurs
    */
    Profile( Class<?> c, String path, Resolver resolver ) throws IOException
    {
        m_element = getProfileElement( c );
        ClassLoader classloader = c.getClassLoader();
        m_context = getContextProfile( classloader, m_element, resolver );
        m_parts = getPartsProfile( classloader, m_element, resolver, path );
        m_activation = getActivationPolicy( m_element, c );
    }
    
    ContextDirective getContextDirective()
    {
        return m_context;
    }
    
    PartsDirective getPartsDirective()
    {
        return m_parts;
    }
    
    ActivationPolicy getActivationPolicy()
    {
        return m_activation;
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
    
   /**
    * Return the activation policy.  If the element declares the activation attribute
    * that value (resolved to the policy enum) is returne, otherwise the class is checked for
    * the declaration of an explicit activivation policy and if present the value is returned,
    * otherwise the default SYSTEM activation policy is returned.
    * 
    * @return the resolved activation policy
    */
    private static ActivationPolicy getActivationPolicy( Element element, Class<?> c )
    {
        if( null == element )
        {
            return ActivationPolicy.SYSTEM;
        }
        String value = ElementHelper.getAttribute( element, "activation" );
        if( null == value )
        {
            if( c.isAnnotationPresent( Activation.class ) )
            {
                Activation annotation = 
                  c.getAnnotation( Activation.class );
                return annotation.value();
            }
            return ActivationPolicy.SYSTEM;
        }
        else
        {
            return ActivationPolicy.valueOf( value.toUpperCase() );
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

