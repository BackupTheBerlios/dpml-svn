/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.transit.tools;

import java.util.LinkedList;
import java.util.prefs.Preferences;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task supports the creation of preference as readable xml files.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class NodeTask extends Task
{
    private String m_name;
    private LinkedList m_nodes = new LinkedList();
    private LinkedList m_entries = new LinkedList();
        
   /**
    * Set the preference node name.
    * @param name the node name
    */
    public void setName( String name )
    {
        m_name = name;
    }
    
   /**
    * Create and return a new nested node.
    * @return the new node
    */
    public NodeTask createNode()
    {
        NodeTask node = new NodeTask();
        m_nodes.add( node );
        return node;
    }

   /**
    * Create and return a new attribute entry.
    * @return the new attribute
    */
    public EntryTask createEntry()
    {
        EntryTask entry = new EntryTask();
        m_entries.add( entry );
        return entry;
    }
    
    void apply( Preferences prefs )
    {
        EntryTask[] entries = (EntryTask[]) m_entries.toArray( new EntryTask[0] );
        for( int i=0; i<entries.length; i++ )
        {
            EntryTask entry = entries[i];
            entry.apply( prefs );
        }
        NodeTask[] nodes = (NodeTask[]) m_nodes.toArray( new NodeTask[0] );
        for( int i=0; i<nodes.length; i++ )
        {
            NodeTask node = nodes[i];
            String name = node.getNodeName();
            Preferences child = prefs.node( name );
            node.apply( child );
        }
    }

    private String getNodeName()
    {
        if( null == m_name )
        {
            final String error = 
              "Missing name attribute.";
            throw new BuildException( error, getLocation() );
        }
        return m_name;
    }
    
   /**
    * An attribute entry.
    */
    public class EntryTask extends Task
    {
        private String m_key;
        private Object m_value;
        private Preferences m_prefs;
        private Class m_type;

       /**
        * Set the attribute key.
        * @param key the attribute name
        */
        public void setKey( String key )
        {
            m_key = key;
        }
        
       /**
        * Set the attribute string value.
        * @param value the string value
        */
        public void setValue( String value )
        {
            checkValue();
            m_value = value;
        }
        
       /**
        * Set the attribute as a boolean value.
        * @param value the boolean value
        */
        public void setBoolean( boolean value )
        {
            checkValue();
            m_value = new Boolean( value );
        }
        
       /**
        * Set the attribute as a int value.
        * @param value the int value
        */
        public void setInt( int value )
        {
            checkValue();
            m_value = new Integer( value );
        }
        
       /**
        * Set the attribute as a double value.
        * @param value the double value
        */
        public void setDouble( double value )
        {
            checkValue();
            m_value = new Double( value );
        }
        
       /**
        * Set the attribute as a float value.
        * @param value the float value
        */
        public void setFloat( float value )
        {
            checkValue();
            m_value = new Float( value );
        }
        
        private void checkValue()
        {
            if( null != m_value )
            {
                final String error = 
                  "Entry value is already assigned to an instance of " 
                  + m_value.getClass().getName()
                  + ".";
                throw new BuildException( error, getLocation() );
            }
        }
        
        void apply( Preferences prefs )
        {
            if( null == m_key )
            {
                final String error = 
                  "Missing key attribute.";
                throw new BuildException( error, getLocation() );
            }
            if( null == m_value )
            {
                final String error = 
                  "Missing value attribute.";
                throw new BuildException( error, getLocation() );
            }
            Class c = m_value.getClass();
            if( c == String.class )
            {
                prefs.put( m_key, (String) m_value );
            }
            else if( c == Boolean.class )
            {
                prefs.putBoolean( m_key, ( (Boolean) m_value ).booleanValue() );
            }
            else if( c == Integer.class )
            {
                prefs.putInt( m_key, ( (Integer) m_value ).intValue() );
            }
            else if( c == Double.class )
            {
                prefs.putDouble( m_key, ( (Double) m_value ).doubleValue() );
            }
            else if( c == Float.class )
            {
                prefs.putFloat( m_key, ( (Float) m_value ).floatValue() );
            }
        }
    }
}
