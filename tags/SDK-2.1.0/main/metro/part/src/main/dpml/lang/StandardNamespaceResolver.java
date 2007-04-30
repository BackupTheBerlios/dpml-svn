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

package dpml.lang;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import net.dpml.lang.NamespaceError;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * LSInput resolver service.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class StandardNamespaceResolver implements LSResourceResolver
{
   /**
    * Return the schema definition for a supplied namespace. This implementation 
    * handles the resolution of the standard DPML language XDSs through the binding
    * standard schema uris to resource resolvable from the context classloader.
    * 
    * Standard schema uris include:
    *
    * <ul>
    *   <li>dpml:part</li>
    *   <li>dpml:metro</li>
    *   <li>dpml:state</li>
    *   <li>dpml:antlib</li>
    *   <li>dpml:library</li>
    * </ul>
    *
    * @param type the node type
    * @param namespace the node namespace
    * @param publicId the public id
    * @param systemId the system id
    * @param base the base value
    * @return the LS input instance or null if not recognized
    */
    public LSInput resolveResource( 
      String type, String namespace, String publicId, String systemId, String base )
    {
        URL url = getNamespaceURL( namespace );
        if( null == url )
        {
            return null;
        }
        try
        {
            DOMInput input = new DOMInput();
            input.setPublicId( publicId );
            input.setSystemId( systemId );
            input.setBaseURI( base );
            InputStream stream = url.openStream();
            InputStreamReader reader = new InputStreamReader( stream );
            input.setCharacterStream( reader );
            return input;
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while resolving the resource for the namespace ["
              + namespace 
              + "].";
              
            throw new NamespaceError( error, e );
        }
    }
    
    private URL getNamespaceURL( String namespace )
    {
        ClassLoader classloader = getClass().getClassLoader();
        if( "dpml:part".equals( namespace ) )
        {
            return classloader.getResource( "dpml/metro/dpml-metro-part.xsd" );
        }
        else if( "dpml:metro".equals( namespace ) )
        {
            return classloader.getResource( "dpml/metro/dpml-lang-component.xsd" );
        }
        else if( "dpml:state".equals( namespace ) )
        {
            return classloader.getResource( "dpml/metro/dpml-lang-state.xsd" );
        }
        else if( "dpml:antlib".equals( namespace ) )
        {
            return classloader.getResource( "dpml/metro/dpml-lang-resource.xsd" );
        }
        else if( "dpml:library".equals( namespace ) )
        {
            return classloader.getResource( "dpml/metro/dpml-lang-library.xsd" );
        }
        else
        {
            return null;
        }
    }
}
