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

package net.dpml.tools.impl;

import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.library.impl.DefaultDictionary;

import net.dpml.tools.info.ListenerDirective;
import net.dpml.tools.model.Processor;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class DefaultProcessor extends DefaultDictionary implements Processor
{
    private final ListenerDirective m_directive;
    
    public DefaultProcessor( ListenerDirective directive ) throws URISyntaxException
    {
        super( null, directive );
        
        m_directive = directive;
    }
    
    //----------------------------------------------------------------------------
    // Process
    //----------------------------------------------------------------------------
    
   /**
    * Return the name of the process.
    */
    public String getName()
    {
        return m_directive.getName();
    }
    
   /**
    * Return the processor codebase uri.
    */
    public URI getCodeBaseURI()
    {
        return m_directive.getURI();
    }
    
   /**
    * Return the processor classname.
    * @return a possibly null classname
    */
    public String getClassname()
    {
        return m_directive.getClassname();
    }
    
   /**
    * Return an array of dependent process names declared by the process type.
    * @return the process names that this process is depends on
    */
    public String[] getDepends()
    {
        return m_directive.getDependencies();
    }

}
