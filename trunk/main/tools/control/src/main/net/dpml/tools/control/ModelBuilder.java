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

package net.dpml.tools.control;

import java.io.InputStream;
import java.io.IOException;

import net.dpml.tools.model.Module;

import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Element;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ModelBuilder
{
    private static final String MODULE_ELEMENT_NAME = "module";
    
    public static Module build( InputStream input ) throws IOException
    {
        try
        {
            final Element root = ElementHelper.getRootElement( input );
            return buildModule( root );
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error during module construction.";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }
    
   /**
    * Build a module using an XML element.
    * @param element the module element
    */
    private static Module buildModule( Element element )
    {
        final String elementName = element.getTagName();
        if( !MODULE_ELEMENT_NAME.equals( elementName ) )
        {
            final String error =
              "Element is not a module.";
            throw new IllegalArgumentException( error );
        }
        
        // get name, version and basedir, dependent module links, local projects and 
        // resources, then build the nested modules
        
        final String name = ElementHelper.getAttribute( element, "name", null );
        final String version = ElementHelper.getAttribute( element, "version", null );
        final String basedir = ElementHelper.getAttribute( element, "basedir", null );
        
        return null;
    }
}
