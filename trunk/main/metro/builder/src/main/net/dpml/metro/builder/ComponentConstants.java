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

package net.dpml.metro.builder;

import java.net.URI;

/**
 * Component constants.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class ComponentConstants
{
    static final URI CONTROLLER_URI = createURI( "@CONTROLLER-URI@" );
    static final URI BUILDER_URI = createURI( "@BUILDER-URI@" );
    
    private boolean m_checkstyleIsPainful;
    
    ComponentConstants()
    {
        m_checkstyleIsPainful = true;
    }
    
    private static URI createURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return null;
        }
    }
}
