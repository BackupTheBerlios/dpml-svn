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

package net.dpml.library.impl;

import java.util.Properties;
import java.util.Map;

import net.dpml.lang.Type;
import net.dpml.library.TypeBuilder;
import net.dpml.library.info.TypeDirective;

import net.dpml.transit.util.ElementHelper;

import net.dpml.lang.BuilderException;
import net.dpml.part.AbstractBuilder;

import org.w3c.dom.Element;

/**
 * Utility used to build a type defintion from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultTypeBuilder extends AbstractBuilder implements TypeBuilder
{
    private static final String PART_XSD_URI = "@PART-XSD-URI@";
    private static final String MODULE_XSD_URI = "@MODULE-XSD-URI@";
    private static final String COMMON_XSD_URI = "@COMMON-XSD-URI@";

    DefaultTypeBuilder( Map map )
    {
        super( map );
    }
    
   /**
    * Return the id of the type produced by the builder.  The method
    * always throws an UnsupportedOperationException as the default
    * builder handles generic type production where the type id is 
    * declared within supplied type derectives.
    *
    * @return the type id
    */
    public String getID()
    {
        throw new UnsupportedOperationException( "getID" );
    }
    
   /**
    * Construct a type instance using a supplied classloader and type
    * production directive.
    * @param classloader the base classloader
    * @param type the type production directive
    * @return the type instance
    * @exception Exception if a type instance creation error occurs
    */
    public Type buildType( ClassLoader classloader, TypeDirective type ) throws Exception
    {
        throw new UnsupportedOperationException( "buildType" );
    }
    
   /**
    * Return the id of the type to be produced given a DOM element.  The 
    * implementation assumes that the supplied element will expose an id
    * attribute contining the type identifier.  If no such attribute exists
    * a runtime exception will be thrown.
    *
    * @param element the DOM element
    * @return the type id
    */
    protected String getID( Element element )
    {
        final String id = ElementHelper.getAttribute( element, "id" );
        if( null == id )
        {
            final String error = 
              "Missing type 'id'.";
            throw new BuilderException( element, error );
        }
        else
        {
            return id;
        }
    }

   /**
    * Return the alias production flag.   
    * The implementation assumes that the supplied element may expose an alias
    * attribute contining the flag boolean status.  If no such attribute exists
    * 'false' is returned otherwise the attribute value as a boolean will be 
    * returned.
    *
    * @param element the DOM element
    * @return the type id
    */
    protected boolean getAliasFlag( Element element )
    {
        return ElementHelper.getBooleanAttribute( element, "alias", false );
    }
    
   /**
    * Return a properties instance populated with any property assertions contained
    * as nested <property> elements within the supplied element.
    * @param element the DOM element representing the type production assertion
    * @return the properties
    */
    protected Properties getProperties( Element element )
    {
        Properties properties = new Properties();
        Element[] children = ElementHelper.getChildren( element );
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            String tag = child.getTagName();
            if( "property".equals( tag ) )
            {
                String key = ElementHelper.getAttribute( child, "name", null );
                if( null == key )
                {
                    final String error =
                      "Property declaration does not contain a 'name' attribute.";
                    throw new IllegalArgumentException( error );
                }
                else
                {
                    String value = ElementHelper.getAttribute( child, "value", null );
                    properties.setProperty( key, value );
                }
            }
        }
        return properties;
    }
    
}
