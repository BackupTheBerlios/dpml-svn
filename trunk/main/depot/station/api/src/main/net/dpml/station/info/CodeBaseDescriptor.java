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

package net.dpml.station.info;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The CodeBaseDescriptor is immutable datastructure used to 
 * describe a codebase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CodeBaseDescriptor extends AbstractDescriptor
{
    private final URI m_codebase;
    private final ValueDescriptor[] m_parameters;
    
   /**
    * Creation of a new codebase descriptor.
    * @param codebase the codebase uri 
    * @param parameters an array of plugin parameter descriptors
    * @exception URISyntaxException if the codebase URI is invalid
    */
    public CodeBaseDescriptor( String codebase, ValueDescriptor[] parameters ) throws URISyntaxException
    {
        m_codebase = new URI( codebase );
        m_parameters = parameters;
    }
    
   /**
    * Return the codebase URI.
    *
    * @return the codebase uri
    */
    public URI getCodeBaseURI()
    {
        return m_codebase;
    }
    
   /**
    * Return the codebase URI as a string.
    *
    * @return the codebase uri specification
    */
    public String getCodeBaseURISpec()
    {
        return m_codebase.toASCIIString();
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
