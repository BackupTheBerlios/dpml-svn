/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.library.impl;

import java.net.URI;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.library.info.TypeDirective;

import net.dpml.lang.Type;

import net.dpml.lang.Builder;
import net.dpml.lang.BuilderException;
import net.dpml.part.AbstractBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Internal exception throw to indicate a bad name reference.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultType extends DefaultDictionary implements Type
{
    private final DefaultResource m_resource;
    private final TypeDirective m_directive;
    
   /**
    * Creation of a new DefaultType.
    * @param message the exception message
    */
    DefaultType( DefaultResource resource, TypeDirective directive )
    {
        super( resource, directive );
        
        m_resource = resource;
        m_directive = directive;
    }

    public String getID()
    {
        return m_directive.getID();
    }
    
    public boolean getAlias()
    {
        return m_directive.getAlias();
    }
    
    public Object getData()
    {
        Element element = m_directive.getElement();
        if( null == element )
        {
            return null;
        }
        try
        {
            TypeInfo info = element.getSchemaTypeInfo();
            String namespace = info.getTypeNamespace();
            String builderURI = m_resource.getProperty( "project.xsd.builder." + namespace );
            Map map = new Hashtable();
            if( null != builderURI )
            {
                try
                {
                    map.put( namespace, new URI( builderURI ) );
                }
                catch( Throwable e )
                {
                    final String error =
                      "Bad builder uri [" + builderURI + "].";
                    throw new IllegalStateException( error );
                }
            }
            AbstractBuilder factory = new AbstractBuilder( map );
            Builder builder = factory.getBuilder( element );
            return builder.build( getClass().getClassLoader(), element );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to load foreign builder.";
            throw new BuilderException( element, error, e );
        }
    }
    
}
