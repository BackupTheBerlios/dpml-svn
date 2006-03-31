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

package net.dpml.part;

import net.dpml.lang.Value;
import net.dpml.lang.Decoder;
import net.dpml.lang.DecodingException;

import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.TypeInfo;
import org.w3c.dom.Element;

/**
 * Utility used to build a plugin strategy from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class PartStrategyDecoder implements Decoder
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------
    
    private static final PartDirective TRANSIT_DIRECTIVE = 
      new PartDirective( DecoderFactory.LOCAL_URI );
    
    private static final ValueDecoder VALUE_DECODER = new ValueDecoder();
    
    // ------------------------------------------------------------------------
    // Decoder
    // ------------------------------------------------------------------------
    
   /**
    * Build a strategy from a supplied DOM element.
    * @param classloader the classloader
    * @param element the strategy element
    * @return the resolve instance
    * @exception Exception if an error occurs
    */
    public Object decode( ClassLoader classloader, Element element ) throws DecodingException
    {
        return decodeStrategy( classloader, element );
    }
    
    // ------------------------------------------------------------------------
    // PartStrategyDecoder
    // ------------------------------------------------------------------------
    
   /**
    * Build a strategy from a supplied DOM element.
    * @param classloader the classloader
    * @param element the strategy element
    * @return the resolve strategy
    * @exception Exception if an error occurs
    */
    public Strategy decodeStrategy( ClassLoader classloader, Element element ) throws DecodingException
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String type = info.getTypeName();
        boolean alias = ElementHelper.getBooleanAttribute( element, "alias", false );
        if( "plugin".equals( type ) )
        {
            String classname = ElementHelper.getAttribute( element, "class" );
            Element[] elements = ElementHelper.getChildren( element );
            Value[] values = VALUE_DECODER.decodeValues( elements );
            Plugin plugin = new Plugin( classname, values );
            return new Strategy( DecoderFactory.LOCAL_URI, TRANSIT_DIRECTIVE, plugin, alias );
        }
        else if( "resource".equals( type ) )
        {
            String urn = ElementHelper.getAttribute( element, "urn" );
            String path = ElementHelper.getAttribute( element, "path" );
            Resource resource = new Resource( urn, path );
            return new Strategy( DecoderFactory.LOCAL_URI, TRANSIT_DIRECTIVE, resource, alias );
        }
        else
        {
            final String error = 
              "Strategy element type [" 
              + type
              + "] is not recognized.";
            throw new DecodingException( element, error );
        }
    }
}
