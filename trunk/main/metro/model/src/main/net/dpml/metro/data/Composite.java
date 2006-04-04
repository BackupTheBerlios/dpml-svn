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

package net.dpml.metro.data;

import java.io.IOException;
import java.net.URI;

import net.dpml.lang.Logger;
import net.dpml.lang.Classpath;
import net.dpml.lang.Value;

import net.dpml.part.Info;
import net.dpml.part.Plugin;

/**
 * A component based plugin.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Composite extends Plugin 
{
    private final ComponentDirective m_directive;
    
    public Composite( 
      Logger logger, Info info, Classpath classpath, ComponentDirective directive )
      throws IOException
    {
        super( logger, info, classpath, directive.getClassname(), new Value[0] );
        
        m_directive = directive;
    }
    
    public ComponentDirective getComponentDirective()
    {
        return m_directive;
    }
}

