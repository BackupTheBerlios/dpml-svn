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

import java.util.Map;
import java.util.Properties;
import java.net.URI;

import net.dpml.util.Resolver;
import net.dpml.lang.DecodingException;

import org.w3c.dom.Element;

/**
 * The ApplianceDescriptor is immutable datastructure used to 
 * describe an application.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ApplianceDescriptor
{
    private final Element m_element;
    private final InfoDescriptor m_info;
    private final ProcessDescriptor m_profile;
    private final URI m_codebase;
    private final URI m_uri;
    
    public ApplianceDescriptor( Element element, Resolver resolver, URI uri ) throws DecodingException
    {
        m_uri = uri;
        m_element = element;
        
        Element codebase = ElementHelper.getChild( element, "codebase" );
        m_codebase = getCodebase( codebase, resolver );
        
        Element info = ElementHelper.getChild( element, "info" );
        m_info = new InfoDescriptor( info, resolver );
        
        Element process = ElementHelper.getChild( element, "process" );
        m_profile = new ProcessDescriptor( process, resolver );
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
    * Get the application name.
    *
    * @return the name
    */
    public String getName()
    {
        return m_info.getName();
    }
    
   /**
    * Get the application title.
    *
    * @return the title
    */
    public String getTitle()
    {
        return m_info.getTitle();
    }
    
   /**
    * Get the application description.
    *
    * @return the description
    */
    public String getDescription()
    {
        return m_info.getDescription();
    }
    
   /**
    * Returns the executable name.
    * 
    * @return the name of the target executable
    */
    public String getExecutable()
    {
        return m_profile.getExecutable();
    }

   /**
    * Returns the relative path for establishment of the working directory.
    * 
    * @return the working directory path
    */
    public String getPath()
    {
        return m_profile.getPath();
    }

   /**
    * Get the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @return the startup timeout value
    */    
    public int getStartupTimeout()
    {
        return m_profile.getStartupTimeout();
    }

   /**
    * Get the duration in seconds to wait for the shutdown
    * of the application before considering the process as non-responsive.
    * 
    * @return the shutdown timeout value
    */
    public int getShutdownTimeout()
    {
        return m_profile.getStartupTimeout();
    }
    
   /**
    * Get the environment variable declarations.
    * 
    * @return the environment variable map
    */
    public Map<String,String> getEnvironmentMap()
    {
        return m_profile.getEnvironmentMap();
    }
    
   /**
    * Get the system properties.
    * 
    * @return the system properties
    */
    public Properties getSystemProperties()
    {
        return m_profile.getSystemProperties();
    }
    
   /**
    * Get the target uri.
    * 
    * @return the target uri
    */
    public URI getTargetURI()
    {
        return m_codebase;
    }
    
    
   /**
    * Get the codebase uri.
    * 
    * @return the codebase uri
    */
    public URI getCodebaseURI()
    {
        return m_uri;
    }
    
    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object equivalent
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
            ApplianceDescriptor application = (ApplianceDescriptor) other;
            if( !m_info.equals( application.m_info ) )
            {
                return false;
            }
            if( !m_profile.equals( application.m_profile ) )
            {
                return false;
            }
            if( !m_codebase.equals( application.m_codebase ) )
            {
                return false;
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
        hash ^= m_info.hashCode();
        hash ^= m_profile.hashCode();
        hash ^= m_codebase.hashCode();
        return hash;
    }
    
    private static URI getCodebase( Element element, Resolver resolver ) throws DecodingException
    {
        String spec = ElementHelper.getAttribute( element, "uri", null, resolver );
        if( null == spec )
        {
            final String error = 
              "Mising uri attribute.";
            throw new DecodingException( error, element );
        }
        
        Element[] params = ElementHelper.getChildren( element, "param" );
        for( int i=0; i<params.length; i++ )
        {
            Element e = params[i];
            String key = ElementHelper.getAttribute( e, "key", null, resolver );
            String value = ElementHelper.getAttribute( e, "value", null, resolver );
            if( i==0 )
            {
                spec = spec + "?" + key + "=" + value;
            }
            else
            {
                spec = spec + "&" + key + "=" + value;
            }
        }
        return URI.create( spec );
    }
}
