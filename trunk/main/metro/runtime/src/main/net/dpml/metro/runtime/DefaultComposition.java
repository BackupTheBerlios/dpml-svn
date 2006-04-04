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

package net.dpml.metro.runtime;

import java.io.IOException;
import java.io.Writer;

import net.dpml.component.Controller;
import net.dpml.component.Composition;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.builder.ComponentEncoder;

import net.dpml.lang.Classpath;
import net.dpml.lang.Logger;

import net.dpml.part.Info;

/**
 * Component composition.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultComposition extends Composition
{   
    private ComponentDirective m_directive;
     
    public DefaultComposition( 
      Logger logger, Info info, Classpath classpath, Controller controller, ComponentDirective directive )
      throws IOException
    {
        super( logger, info, classpath, controller, directive );
        
        m_directive = directive;
    }
    
    public ComponentDirective getComponentDirective()
    {
        return m_directive;
    }
    
    protected void encodeStrategy( Writer writer, String pad ) throws IOException
    {
        ComponentEncoder encoder = new ComponentEncoder();
        encoder.writeComponent( writer, m_directive, pad );
    }
}
