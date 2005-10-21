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

package net.dpml.tools.model;

import java.io.File;

/**
 * A ModelNotFoundException is thrown when a request for the lookup of 
 * a project or module relative to a base dir cannot be resolved to a
 * value.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ModelNotFoundException extends Exception
{
    final File m_base;
    
    public ModelNotFoundException( File base )
    {
        super( base.toString() );
        m_base = base;
    }
    
    public File getBase()
    {
        return m_base;
    }
}
