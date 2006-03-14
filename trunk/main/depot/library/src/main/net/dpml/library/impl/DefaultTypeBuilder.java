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

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Properties;
import java.util.Map;

import net.dpml.lang.Type;
import net.dpml.library.TypeBuilder;
import net.dpml.library.info.TypeDirective;

import net.dpml.transit.util.ElementHelper;

import net.dpml.part.AbstractBuilder;
import net.dpml.lang.Builder;
import net.dpml.part.Strategy;
import net.dpml.lang.BuilderException;
import net.dpml.part.PartStrategyBuilder;

import org.w3c.dom.TypeInfo;
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
    
    public String getID()
    {
        throw new UnsupportedOperationException( "getID" );
    }
    
    //public Object build( ClassLoader classloader, Element element ) throws Exception
    //{
    //    return buildType( classloader, element );
    //}
    
    public Type buildType( ClassLoader classloader, TypeDirective type ) throws Exception
    {
        throw new UnsupportedOperationException( "buildType" );
    }
    
   /*
    public TypeDirective buildType( ClassLoader classloader, TypeDirective type ) throws Exception
    {
        Element element = type.getElement();
        TypeInfo info = element.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        if( null == namespace )
        {
            throw new NullPointerException( "namespace" );
        }
        String typeName = info.getTypeName();
        if( info.isDerivedFrom( COMMON_XSD_URI, "AbstractType", TypeInfo.DERIVATION_EXTENSION ) )
        {
            final boolean alias = getAliasFlag( element );
            if( MODULE_XSD_URI.equals( namespace ) )
            {
                if( "GenericType".equals( typeName ) ) 
                {
                    final String id = getID( element );
                    final Properties properties = getProperties( element );
                    return new TypeDirective( id, alias, properties );
                }
                else
                {
                    System.out.println( "# UNRECOGNIZED MODULE TYPE" );
                    final String error = 
                      "Element namespace is recognized as within the module definition "
                      + " however the type identifier is not recognized."
                      + "\nNamespace: " 
                      + namespace
                      + "\nType Name: " 
                      + info.getTypeName();
                    throw new BuilderException( element, error );
                }
            }
            else if( info.isDerivedFrom( PART_XSD_URI, "StrategyType", TypeInfo.DERIVATION_EXTENSION ) )
            {
                return new TypeDirective( "part", alias, element );
            }
            else
            {
                System.out.println( "# UNRECOGNIZED TYPE" );
                final String error = 
                  "Element is recognized as an AbstractType however the type id is not resolvable."
                  + "\nNamespace: " 
                  + namespace
                  + "\nElement Name (from Schema Info): " 
                  + info.getTypeName();
                throw new BuilderException( element, error );
            }
        }
        else
        {
            System.out.println( "# INVALID ELEMENT" );
            final String error = 
              "Element is not derivived from AbstractType defined under the common namespace."
              + "\nNamespace: " + namespace
              + "\nElement Name (from Schema Info): " + info.getTypeName();
            throw new BuilderException( element, error );
        }
    }
    */
    
    protected String getID( Element element )
    {
        final String id = ElementHelper.getAttribute( element, "id" );
        if( null == id )
        {
            final String error = 
              "Missing type 'id'.";
            throw new IllegalArgumentException( error );
        }
        else
        {
            return id;
        }
    }

    protected boolean getAliasFlag( Element element )
    {
        return ElementHelper.getBooleanAttribute( element, "alias", false );
    }
    
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
