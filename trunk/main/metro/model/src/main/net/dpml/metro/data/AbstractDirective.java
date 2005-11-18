/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.metro.data;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Abstract base class for directives.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractDirective implements Directive
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    //--------------------------------------------------------------------------
    // Part
    //--------------------------------------------------------------------------

   /**
    * Return the part handler uri.
    * @return the uri of the part handler
    */
    public URI getPartHandlerURI()
    {
        return PART_HANDLER_URI;
    }

   /**
    * Static utility to create the part handler uri.
    * @param spec the part handler uri string
    * @return the constant part handler uri
    */
    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
    
    private static final URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );

}
