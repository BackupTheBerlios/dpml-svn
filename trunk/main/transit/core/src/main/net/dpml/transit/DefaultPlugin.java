/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit;

import java.net.URI;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Properties;

import net.dpml.lang.Plugin;
import net.dpml.lang.Version;
import net.dpml.lang.Classpath;
import net.dpml.lang.Strategy;
import net.dpml.lang.Category;

/**
 * A Plugin class contains immutable data about a plugin based on a descriptor resolved
 * from a 'plugin' artifact.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultPlugin implements Plugin
{
   /**
    * The plugin builder version value that will be used when reading and writing 
    * plugin defintions.
    */
    public static final Version VERSION = new Version( 1, 0, 0 );

    public static final String XML_HEADER = 
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";

    public static final String STANDARD_NAME = "plugin";

    public static final String STANDARD_PUBLIC_ID = 
      "-//DPML//DTD Standard Plugin Version 1.0//EN";
      
    public static final String STANDARD_SYSTEM_ID = 
      "http://download.dpml.net/dtds/plugin_1_0.dtd";

    public static final String STANDARD_RESOURCE = 
      "net/dpml/lang/plugin_1_0.dtd";

    private static final String PLUGIN_DOCTYPE = 
      "\n<!DOCTYPE "
      + STANDARD_NAME
      + " PUBLIC \"" 
      + STANDARD_PUBLIC_ID
      + "\" \""
      + STANDARD_SYSTEM_ID 
      + "\" >";
    
    private final String m_title;
    private final String m_description;
    private final URI m_uri;
    private final Strategy m_strategy;
    private final Classpath m_classpath;
    
   /**
    * Creation of a new plugin definition.
    * @param title short title
    * @param description textual description
    * @param uri the uri from which the plugin was resolved
    * @param strategy the plugin instantiation strategy definition
    * @param classpath the classpath defintion
    */
    public DefaultPlugin( 
      String title, String description, URI uri, Strategy strategy, Classpath classpath )
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        if( null == strategy )
        {
            throw new NullPointerException( "strategy" );
        }
        if( null == classpath )
        {
            throw new NullPointerException( "classpath" );
        }
        m_uri = uri;
        m_strategy = strategy;
        m_title = title;
        m_description = description;
        m_classpath = classpath;
    }
    
   /**
    * Return the plugin title.
    * @return the plugin title
    */
    public String getTitle()
    {
        return m_title;
    }
    
   /**
    * Return the plugin description.
    * @return the plugin description
    */
    public String getDescription()
    {
        return m_description;
    }
    
   /**
    * Return the plugin version.
    * @return the plugin version identifier
    */
    public Version getVersion()
    {
        return VERSION;
    }
    
   /**
    * Return the uri to the plugin descriptor.
    * @return the plugin uri
    */
    public URI getURI()
    {
        return m_uri;
    }

   /**
    * Return the plugin strategy.
    * @return the plugin strategy
    */
    public Strategy getStrategy()
    {
        return m_strategy;
    }
    
   /**
    * Return the classpath definition.
    */
    public Classpath getClasspath()
    {
        return m_classpath;
    }
    
   /**
    * Write an XML representation of the plugin to the output stream.
    * @param output the output stream
    * @exception Exception if an error ocucrs during externalization
    */
    public void write( OutputStream output ) throws Exception
    {
        final Writer writer = new OutputStreamWriter( output );
        writer.write( XML_HEADER );
        writer.write( PLUGIN_DOCTYPE );
        writeHeader( writer, "plugin", null, VERSION );
        writeTitle( writer, getTitle() );
        writeDescription( writer, getDescription() );
        writeStrategy( writer, getStrategy() );
        writeClasspath( writer, getClasspath() );
        writeFooter( writer, "plugin" );
        writer.write( "\n" );
        writer.flush();
        writer.close();
    }
    
    protected void writeHeader( Writer writer, String root, String namespace, Version version ) throws IOException
    {
        if( null == namespace )
        {
            writer.write( "\n\n<" + root + " version=\"" + version + "\">" );
        }
        else
        {
            writer.write( "\n\n<" + root + " xmlns=\"" + namespace + "\" version=\"" + version + "\">" );
        }
    }
    
    protected void writeFooter( Writer writer, String root ) throws IOException
    {
        writer.write( "\n</" + root + ">" );
    }
    
    protected void writeTitle( Writer writer, String title ) throws IOException
    {
        if( null != title )
        {
            writer.write( 
              "\n  <title>" 
              + title 
              + "</title>" );
        }
    }
    
    protected void writeDescription( Writer writer, String description ) throws IOException
    {
        if( null != description )
        {
            writer.write( 
              "\n  <description>" 
              + description 
              + "</description>" );
        }
    }
    
    protected void writeStrategy( Writer writer, Strategy strategy ) throws Exception
    {
        String handler = strategy.getHandlerClassname();
        Properties properties = strategy.getProperties();
        String[] keys = (String[]) properties.keySet().toArray( new String[0] );
        if( StandardHandler.class.getName().equals( handler ) )
        {
            writer.write( "\n  <strategy>" );
        }
        else
        {
            writer.write( "\n  <strategy class=\"" + handler + "\">" );
        }
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            String value = properties.getProperty( key );
            writer.write( 
              "\n    <property name=\"" 
              + key 
              + "\" value=\"" 
              + value 
              + "\"/>" );
        }
        writer.write( "\n  </strategy>" );
    }
    
    protected void writeClasspath( Writer writer, Classpath classpath ) throws Exception
    {
        writer.write( "\n  <classpath>" );
        URI[] sysUris = classpath.getDependencies( Category.SYSTEM );
        if( sysUris.length > 0 )
        {
            writer.write( "\n    <system>" );
            for( int i=0; i<sysUris.length; i++ )
            {
                URI uri = sysUris[i];
                writer.write( "\n      <uri>" + uri + "</uri>" );
            }
            writer.write( "\n    </system>" );
        }
        URI[] publicUris = classpath.getDependencies( Category.PUBLIC );
        if( publicUris.length > 0 )
        {
            writer.write( "\n    <public>" );
            for( int i=0; i<publicUris.length; i++ )
            {
                URI uri = publicUris[i];
                writer.write( "\n      <uri>" + uri + "</uri>" );
            }
            writer.write( "\n    </public>" );
        }
        URI[] protectedUris = classpath.getDependencies( Category.PROTECTED );
        if( protectedUris.length > 0 )
        {
            writer.write( "\n    <protected>" );
            for( int i=0; i<protectedUris.length; i++ )
            {
                URI uri = protectedUris[i];
                writer.write( "\n      <uri>" + uri + "</uri>" );
            }
            writer.write( "\n    </protected>" );
        }
        URI[] privateUris = classpath.getDependencies( Category.PRIVATE );
        if( privateUris.length > 0 )
        {
            writer.write( "\n    <private>" );
            for( int i=0; i<privateUris.length; i++ )
            {
                URI uri = privateUris[i];
                writer.write( "\n      <uri>" + uri + "</uri>" );
            }
            writer.write( "\n    </private>" );
        }
        writer.write( "\n  </classpath>" );
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return TRUE if equal else FALSE
    */
    public boolean equals( Object other )
    {
        if( other instanceof DefaultPlugin )
        {
            DefaultPlugin plugin = (DefaultPlugin) other;
            return m_uri.equals( plugin.getURI() );
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Return the hashcode for the plugin definition.
    * @return the hash code
    */
    public int hashCode()
    {
        return m_uri.hashCode();
    }

    private static final URI[] EMPTY_URIS = new URI[0];
    
}
