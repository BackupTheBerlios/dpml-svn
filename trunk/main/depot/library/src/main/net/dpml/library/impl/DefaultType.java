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
import net.dpml.library.Type;

import net.dpml.part.Decoder;
import net.dpml.part.DecoderFactory;
import net.dpml.lang.Logger;

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
    private final Logger m_logger;
    
   /**
    * Creation of a new DefaultType.
    * @param resource the enclosing resource
    * @param directive the type production directive
    */
    DefaultType( Logger logger, DefaultResource resource, TypeDirective directive )
    {
        super( resource, directive );
        
        m_resource = resource;
        m_directive = directive;
        m_logger = logger;
    }

   /**
    * Get the type identifier.
    * @return the type id
    */
    public String getID()
    {
        return m_directive.getID();
    }
    
   /**
    * Get the type alias flag.
    * @return the type alias flag
    */
    public boolean getAlias()
    {
        return m_directive.getAlias();
    }
    
    public Element getElement()
    {
        return m_directive.getElement();
    }
    
   /**
    * Get the type specific datastructure.
    * @return the datastructure
    */
    /*
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
            Logger logger = getLogger();
            DecoderFactory factory = new DecoderFactory( logger, map );
            Decoder decoder = factory.loadDecoder( element );
            return decoder.decode( element );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to data.";
            throw new RuntimeException( error, e );
        }
    }
    */
    
    Logger getLogger()
    {
        return m_logger;
    }
}
