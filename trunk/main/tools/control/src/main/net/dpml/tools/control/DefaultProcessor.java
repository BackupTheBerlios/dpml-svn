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

package net.dpml.tools.control;

import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.tools.model.Processor;
import net.dpml.tools.info.ProcessDescriptor;


/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class DefaultProcessor implements Processor
{
    private final ProcessDescriptor m_descriptor;
    private final URI m_uri;
    
    public DefaultProcessor( ProcessDescriptor descriptor ) throws URISyntaxException
    {
        if( null == descriptor )
        {
            throw new NullPointerException( "descriptor" );
        }
        
        m_descriptor = descriptor;
        String urn = descriptor.getURN();
        if( null != urn )
        {
            m_uri = new URI( descriptor.getURN() );
        }
        else
        {
            m_uri = null;
        }
        
    }
    
    //----------------------------------------------------------------------------
    // Process
    //----------------------------------------------------------------------------
    
   /**
    * Return the name of the process.
    */
    public String getName()
    {
        return m_descriptor.getName();
    }
    
   /**
    * Return the processor codebase uri.
    */
    public URI getCodeBaseURI()
    {
        return m_uri;
    }
    
   /**
    * Return an array of dependent process names declared by the process type.
    * @return the process names that this process is depends on
    */
    public String[] getDepends()
    {
        return m_descriptor.getDependencies();
    }

}
