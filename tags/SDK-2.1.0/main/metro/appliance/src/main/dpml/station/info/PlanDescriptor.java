/*
 * Copyright 2007 Stephen J. McConnell.
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
 
package dpml.station.info;

import dpml.util.ElementHelper;

import java.io.IOException;
import java.net.URI;

import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * Immutable datastructure used to describe an deployment scenario.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class PlanDescriptor
{
    private final Element m_element;
    private final String m_name;
    private final String m_title;
    private final EntryDescriptor[] m_entries;
    private final URI m_codebase;
    
   /**
    * Creation of a new plan descriptor.
    * @param element the element defining the plan
    * @param resolver the symbol resolver
    * @param codebase the uri used to establish the plan element
    * @exception IOException if an IO error occurs
    */
    public PlanDescriptor( Element element, Resolver resolver, URI codebase ) throws IOException
    {
        m_element = element;
        m_codebase = codebase;
        m_name = ElementHelper.getAttribute( element, "name", null, resolver );
        m_title = ElementHelper.getAttribute( element, "title", null, resolver );
        Element[] elements = ElementHelper.getChildren( element );
        m_entries = new EntryDescriptor[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            Element elem = elements[i];
            String name = elem.getLocalName();
            if( name.equals( "include" ) )
            {
                m_entries[i] = new IncludeDescriptor( elem, resolver );
            }
            else if( name.equals( "appliance" ) )
            {
                m_entries[i] = new ApplianceDescriptor( elem, resolver, codebase, true );
            }
            else
            {
                throw new UnsupportedOperationException( "Element name not recognized: " + name );
            }
        }
    }
    
   /**
    * Return the element defining the scenario descriptor.
    * @return the defining element
    */
    public Element getElement()
    {
        return m_element;
    }
    
   /**
    * Get the codebase uri.
    * 
    * @return the uri
    */
    public URI getCodebaseURI()
    {
        return m_codebase;
    }
    
   /**
    * Get the scenario name.
    * 
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Get the scenario title.
    * 
    * @return the title
    */
    public String getTitle()
    {
        return m_title;
    }
    
   /**
    * Get the deployment uris.
    * 
    * @return the deployment uris
    */
    public EntryDescriptor[] getEntryDescriptors()
    {
        return m_entries;
    }
    
    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object is equivalent
     */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( getClass() != other.getClass() )
        {
            return false;
        }
        else
        {
            PlanDescriptor scenario = (PlanDescriptor) other;
            int n = m_entries.length;
            int m = scenario.m_entries.length;
            if( n != m )
            {
                return false;
            }
            for( int i=0; i<m_entries.length; i++ )
            {
                EntryDescriptor local = m_entries[i];
                EntryDescriptor external = scenario.m_entries[i];
                if( !local.equals( external ) )
                {
                    return false;
                }
            }
            return true;
        }
    }
    
   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        for( EntryDescriptor entry : m_entries )
        {
            hash ^= entry.hashCode();
        }
        return hash;
    }
}
