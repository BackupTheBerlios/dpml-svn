/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.part;

import java.net.URI;

/**
 * Exception thrown when an attempt is made to reference an unknown part.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartNotFoundException extends PartException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private URI m_target;
    private URI m_parent;

    public PartNotFoundException( URI target )
    {
        this( target, null );
    }

    public PartNotFoundException( URI parent, URI target )
    {
        super( buildMessage( parent, target ) );
        m_target = target;
        m_parent = parent;
    }

    public URI getParentURI()
    {
        return m_parent;
    }

    public URI getTargetURI()
    {
        return m_target;
    }

    private static String buildMessage( URI parent, URI target )
    {
        if( null != parent )
        {
            final String error = 
              "Could not find the a part uri ["
              + target
              + "] relative to the enclosing part ["
              + parent.toString()
              + "].";
            return error;
        }
        else
        {
            final String error = 
              "Could not find a part ["
              + target
              + "].";
            return error;
        }
    }

}

