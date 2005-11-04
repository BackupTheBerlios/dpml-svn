/*
 * Copyright 2004 Apache Software Foundation
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

package net.dpml.magic;

import org.apache.tools.ant.BuildException;

/**
 * UnknownResourceException is thrown in response to a request for a resource 
 * that is unknown.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class UnknownResourceException extends BuildException
{
    private final String m_key;

   /**
    * Creation of a new UnknownResourceException.
    * @param key the request resource key
    */
    public UnknownResourceException( final String key )
    {
        super( "Requested key [" + key + "] is unknown." );
        m_key = key;
    }

   /**
    * Return the key.
    * @return the resource key
    */
    public String getKey()
    {
        return m_key;
    }
}
