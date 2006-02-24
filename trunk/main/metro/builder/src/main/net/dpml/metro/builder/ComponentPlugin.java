/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.metro.builder;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.dpml.library.model.Type;
import net.dpml.library.model.Resource;

import net.dpml.lang.Plugin;
import net.dpml.lang.DefaultPlugin;
import net.dpml.lang.Strategy;
import net.dpml.lang.Category;
import net.dpml.lang.Classpath;
import net.dpml.lang.DefaultClasspath;
import net.dpml.lang.DefaultPlugin;
import net.dpml.lang.Version;

import net.dpml.transit.Artifact;

/**
 * Interface implemented by plugins that provide plugin building functionality.
 * Implementations that load plugin factoryies must supply the target Resource
 * as a plugin constructor argument.  Factory implementation shall construct 
 * plugin defintions using the supplied resource as the reference for the 
 * classpath dependencies.  Suppliementary properties may be aquired using 
 * the Type returned from the Resource.getType( "plugin" ) operation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentPlugin extends DefaultPlugin
{
    private static final String NAMESPACE = 
      "link:plugin:dpml/metro/dpml-metro-builder";
    
    private static final Version VERSION = new Version( 1, 0, 0 );
    
    private static final String XML_HEADER = 
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";

    private static final String NAME = "plugin";

    private static final String PUBLIC_ID = 
      "-//DPML//DTD Component 1.0//EN";
      
    private static final String SYSTEM_ID = 
      "http://download.dpml.net/dtds/component_1_0.dtd";

    private static final String RESOURCE = 
      "net/dpml/lang/component_1_0.dtd";

    private static final String DOCTYPE = 
      "\n<!DOCTYPE "
      + NAME
      + " PUBLIC \"" 
      + PUBLIC_ID
      + "\" \""
      + SYSTEM_ID 
      + "\" >";

   /**
    * Creation of a new plugin definition.
    * @param title short title
    * @param description textual description
    * @param uri the uri from which the plugin was resolved
    * @param strategy the plugin instantiation strategy definition
    * @param classpath the classpath definition
    */
    public ComponentPlugin( 
      String title, String description, URI uri, Strategy strategy, Classpath classpath )
    {
        super( title, description, uri, strategy, classpath );
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
        writer.write( DOCTYPE );
        writeHeader( writer, NAME, NAMESPACE, VERSION );
        writeTitle( writer, getTitle() );
        writeDescription( writer, getDescription() );
        writeStrategy( writer, getStrategy() );
        writeClasspath( writer, getClasspath() );
        writeFooter( writer, NAME );
        writer.write( "\n" );
        writer.flush();
        writer.close();
    }

    protected void writeStrategy( Writer writer, Strategy strategy ) throws Exception
    {
        //
        // TODO: cast strategy to a ComponentStragety and fill in details
        //
        
        String handler = strategy.getHandlerClassname();
        Properties properties = strategy.getProperties();
        String[] keys = (String[]) properties.keySet().toArray( new String[0] );
        writer.write( "\n  <strategy class=\"" + handler + "\">" );
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
}
