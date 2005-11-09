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

package net.dpml.profile.info;

import java.net.URI;

import net.dpml.transit.model.Value;

/**
 * The CodeBaseDescriptor is immutable datastructure used to 
 * describe a codebase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class CodeBaseDescriptor extends AbstractDescriptor
{
    private final String m_codebase;
    private final ValueDescriptor[] m_parameters;
    
   /**
    * Creation of a new codebase descriptor.
    * @param codebase the codebase uri 
    * @param parameters an array of plugin parameter descriptors
    */
    public CodeBaseDescriptor( String codebase, ValueDescriptor[] parameters )
    {
        m_codebase = codebase;
        m_parameters = parameters;
    }
    
   /**
    * Return the codebase URI.
    *
    * @return the codebase uri
    */
    public String getCodeBaseURI()
    {
        return m_codebase;
    }
    
   /**
    * Return the array of codebase parameter values.
    *
    * @return the parameter value array
    */
    public ValueDescriptor[] getValueDescriptors()
    {
        return m_parameters;
    }

}
