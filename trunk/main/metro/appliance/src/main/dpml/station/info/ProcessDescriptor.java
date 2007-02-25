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
import dpml.util.ObjectUtils;

import java.util.Map;
import java.util.Properties;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import net.dpml.lang.DecodingException;
import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * Application info description.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ProcessDescriptor
{
    private final Element m_element;
    private final String m_executable;
    private final Map<String,String> m_environment = new Hashtable<String,String>();
    private final Properties m_properties = new Properties();
    private final List<String> m_arguments = new ArrayList<String>();
    private final String m_path;
    private final int m_startup;
    private final int m_shutdown;
    
   /**
    * Creation of a new process descriptor.
    * @param element the elememnt defining the process descriptor
    * @param resolver the symbolic reference resolver
    */
    public ProcessDescriptor( Element element, Resolver resolver )
    {
        m_element = element;
        if( null == element )
        {
            m_executable = "metro";
            m_path = null;
            m_startup = 0;
            m_shutdown = 0;
        }
        else
        {
            m_executable = ElementHelper.getAttribute( element, "executable", null, resolver );
            m_path = ElementHelper.getAttribute( element, "path", null, resolver );
            Element environment = ElementHelper.getChild( element, "environment" );
            if( null != environment )
            {
                Element[] variables = ElementHelper.getChildren( environment, "variable" );
                for( Element variable : variables )
                {
                    String name = ElementHelper.getAttribute( variable, "name", null, resolver );
                    String value = ElementHelper.getAttribute( variable, "value", null, resolver );
                    m_environment.put( name, value );
                }
            }
            
            Element properties = ElementHelper.getChild( element, "properties" );
            if( null != properties )
            {
                Element[] props = ElementHelper.getChildren( properties, "property" );
                for( Element elem : props )
                {
                    String name = ElementHelper.getAttribute( elem, "name", null, resolver );
                    String value = ElementHelper.getAttribute( elem, "value", null, resolver );
                    m_properties.setProperty( name, value );
                }
            }
            
            Element arguments = ElementHelper.getChild( element, "arguments" );
            if( null != arguments )
            {
                Element[] args = ElementHelper.getChildren( properties, "arg" );
                for( Element arg : args )
                {
                    String value = ElementHelper.getAttribute( arg, "value", null, resolver );
                    m_arguments.add( value );
                }
            }
            
            String start = ElementHelper.getAttribute( element, "startup", "0", resolver );
            m_startup = Integer.parseInt( start );
            String stop = ElementHelper.getAttribute( element, "shutdown", "0", resolver );
            m_shutdown = Integer.parseInt( stop );
        }
    }
    
   /**
    * Creation of a new process descriptor.
    * @param executable the java executable name (optional)
    * @param environment a map of overriding environment variables
    * @param properties supplimentary system properties 
    * @param arguments command line arguments
    * @param path the deployment base directory path
    * @param startup startup timeout in seconds
    * @param shutdown startup timeout in seconds
    */
    public ProcessDescriptor( 
      String executable, Map<String,String> environment, Properties properties, 
      List<String> arguments, String path, int startup, int shutdown )
    {
        m_element = null;
        m_executable = executable;
        if( null != environment )
        {
            m_environment.putAll( environment );
        }
        if( null != properties )
        {
            m_properties.putAll( properties );
        }
        if( null != arguments )
        {
            m_arguments.addAll( arguments );
        }
        m_path = path;
        m_startup = startup;
        m_shutdown = shutdown;
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
    * Return the executable name (defaults to metro).
    *
    * @return the executable name
    */
    public String getExecutable()
    {
        return m_executable;
    }
    
   /**
    * Return the executable arguments.
    *
    * @return the executable arguments
    */
    public String[] getArguments()
    {
        return m_arguments.toArray( new String[0] );
    }
    
   /**
    * Get the relative deployment path.
    *
    * @return the path
    */
    public String getPath()
    {
        return m_path;
    }
    
   /**
    * Get the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @return the startup timeout value
    */    
    public int getStartupTimeout()
    {
        return m_startup;
    }
    
   /**
    * Get the duration in seconds to wait for the shutdown
    * of the application before considering the process as non-responsive.
    * 
    * @return the shutdown timeout value
    */
    public int getShutdownTimeout()
    {
        return m_shutdown;
    }
    
   /**
    * Get the system properties.
    * 
    * @return the system properties
    */
    public Properties getSystemProperties()
    {
        return m_properties;
    }
    
   /**
    * Get the environment variable declarations.
    * 
    * @return the environment variable map
    */
    public Map<String,String> getEnvironmentMap()
    {
        return m_environment;
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the other instance
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        if( getClass() != other.getClass() )
        {
            return false;
        }
        ProcessDescriptor profile = (ProcessDescriptor) other;
        if( !ObjectUtils.equals( m_path, profile.m_path )  )
        {
            return false;
        }
        if( !ObjectUtils.equals( m_executable, profile.m_executable )  )
        {
            return false;
        }
        if( !ObjectUtils.equals( m_properties, profile.m_properties ) )
        {
            return false;
        }
        if( m_startup != profile.m_startup )
        {
            return false;
        }
        if( m_shutdown != profile.m_shutdown )
        {
            return false;
        }
        return true;
    }
    
   /**
    * Get the hashcode for this instance.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash ^= ObjectUtils.hashValue( m_executable );
        hash ^= ObjectUtils.hashValue( m_path );
        hash ^= ObjectUtils.hashValue( m_properties );
        hash ^= m_startup;
        hash ^= m_shutdown;
        return hash;
    }
}
