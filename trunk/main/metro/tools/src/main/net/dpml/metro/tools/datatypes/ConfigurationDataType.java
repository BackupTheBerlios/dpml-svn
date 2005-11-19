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

package net.dpml.metro.tools.datatypes;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.impl.DefaultConfiguration;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfiguratorNS;
import org.apache.tools.ant.Project;


/**
 * A configuration directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ConfigurationDataType implements DynamicConfiguratorNS
{
    private final Project m_project;

    private String m_value;
    private Map m_attributes = new Hashtable();
    private List m_children = new LinkedList();
    private String m_name;

   /**
    * Creation of a root configuration directive.
    * @param project the current project
    */
    public ConfigurationDataType( Project project )
    {
        this( project, "configuration" );
    }

   /**
    * Creation of a named configuration element.
    * @param project the current project
    * @param name the element name
    */
    public ConfigurationDataType( Project project, String name )
    {
        m_name = name;
        m_project = project;
    }

   /**
    * Add nested text.
    * @param value the test value
    */
    public void addText( String value )
    {
        String s = value.trim();
        if( s.length() > 0 )
        {
            String parsedValue = m_project.replaceProperties( s );
            m_value = parsedValue;
        }
    }

   /**
    * Set a named attribute to the given value
    *
    * @param uri The namespace uri for this attribute, "" is
    *        used if there is no namespace uri.
    * @param localName The localname of this attribute.
    * @param qName The qualified name for this attribute
    * @param value The value of this attribute.
    * @throws BuildException when any error occurs
    */
    public void setDynamicAttribute(
      String uri, String localName, String qName, String value )
    throws BuildException
    {
        String parsedValue = m_project.replaceProperties( value );
        m_attributes.put( qName, parsedValue );
    }

   /**
    * Create an element with the given name
    *
    * @param uri the element handler uri
    * @param localName the element local name
    * @param qName the element qualified name
    * @throws BuildException when any error occurs
    * @return the element created
    */
    public Object createDynamicElement(
       String uri, String localName, String qName ) throws BuildException
    {
        ConfigurationDataType conf = new ConfigurationDataType( m_project, qName );
        m_children.add( conf );
        return conf;
    }

   /**
    * Return the name of the configuration element.
    * @return the node name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return a value associated with the element.
    * @return the assigned value
    */
    public String getValue()
    {
        return m_value;
    }

   /**
    * Return the map of the assigned attributes.
    * @return the attribute name value map
    */
    public Map getAttributes()
    {
        return m_attributes;
    }

   /**
    * Return he set of nest child configuration directives.
    * @return the configuration directives within this directive
    */
    public ConfigurationDataType[] getChildren()
    {
        return (ConfigurationDataType[]) m_children.toArray( new ConfigurationDataType[m_children.size()] );
    }

   /**
    * Return the constructed configuration instance.
    * @return the configuration
    */
    public Configuration getConfiguration()
    {
        String name = getName();
        DefaultConfiguration config = new DefaultConfiguration( name );
        config.setValue( getValue() );
        Map attributes = getAttributes();
        String[] keys = (String[]) attributes.keySet().toArray( new String[0] );
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            String value = (String) attributes.get( key );
            config.setAttribute( key, value );
        }
        ConfigurationDataType[] nodes = getChildren();
        Configuration[] children = new Configuration[ nodes.length ];
        for( int i=0; i<nodes.length; i++ )
        {
            ConfigurationDataType data = nodes[i];
            Configuration conf = data.getConfiguration();
            config.addChild( conf );
        }
        return config;
    }
}
