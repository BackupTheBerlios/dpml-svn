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

package net.dpml.tools.info;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import net.dpml.tools.process.JarProcess;
import net.dpml.tools.process.PluginProcess;
import net.dpml.tools.process.ModuleProcess;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;

import net.dpml.util.ElementHelper;

import net.dpml.lang.UnknownKeyException;

import org.w3c.dom.Element;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class BuilderDirectiveHelper
{
    private static final String BUILDER_ELEMENT_NAME = "builder";
    private static final String LISTENERS_ELEMENT_NAME = "listeners";
    private static final String LISTENER_ELEMENT_NAME = "listener";
    private static final String PROPERTIES_ELEMENT_NAME = "properties";
    private static final String PROPERTY_ELEMENT_NAME = "property";
    private static final String PROCESSORS_ELEMENT_NAME = "processors";
    private static final String PROCESSOR_ELEMENT_NAME = "processor";
    
    private BuilderDirectiveHelper()
    {
        // static utility class
    }
    
   /**
    * Construct a builder directive from XML source.
    * @param source the XML source file
    * @return the builder directive
    * @exception IOException if an IO exception occurs
    */
    public static BuilderDirective build( File source ) throws IOException
    {
        if( null == source )
        {
            throw new NullPointerException( "source" );
        }
        if( !source.exists() )
        {
            throw new FileNotFoundException( source.toString() );
        }
        if( source.isDirectory() )
        {
            final String error = 
              "File ["
              + source 
              + "] references a directory.";
            throw new IllegalArgumentException( error );
        }
        FileInputStream input = new FileInputStream( source );
        BufferedInputStream buffer = new BufferedInputStream( input );
        try
        {
            final Element root = ElementHelper.getRootElement( input );
            File base = source.getParentFile();
            return buildMainDirective( root );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured while attempting to create the builder configuration from the source: "
              + source;
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
        finally
        {
            input.close();
        }
    }

   /**
    * Creates a builder configuration using the default configuration.
    * @return the builder directive
    * @exception Exception if an error occurs
    */
    public static BuilderDirective build() throws Exception
    {
        File prefs = Transit.DPML_PREFS;
        File config = new File( prefs, "dpml/tools/xmls/builder.xml" );
        if( config.exists() )
        {
            return build( config );
        }
        else
        {
            final String error = 
              "Missing builder configuration: " 
              + config;
            throw new FileNotFoundException( error );
        }
    }
    
    private static URL convertToURL( URI uri ) throws Exception
    {
        if( Artifact.isRecognized( uri ) )
        {
            Artifact artifact = Artifact.createArtifact( uri );
            return artifact.toURL();
        }
        else
        {
            return uri.toURL();
        }
    }
    
   /**
    * Build the configuration using an XML element.
    * @param element the builder root element
    * @return the builder directive
    * @exception IOException if an I/O error occurs
    */
    private static BuilderDirective buildMainDirective( Element element ) throws Exception
    {
        final String elementName = element.getTagName();
        if( !BUILDER_ELEMENT_NAME.equals( elementName ) )
        {
            final String error =
              "Element is not a builder configuration.";
            throw new IllegalArgumentException( error );
        }
        
        // get type descriptors, modules and properties
        
        String phase = ElementHelper.getAttribute( element, "default", "installation" );
        
        Properties properties = null;
        ListenerDirective[] listeners = new ListenerDirective[0];
        ProcessorDirective[] processors = new ProcessorDirective[0];
        Element[] children = ElementHelper.getChildren( element );
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            final String tag = child.getTagName();
            if( PROPERTIES_ELEMENT_NAME.equals( tag ) )
            {
                properties = buildProperties( child );
            }
            else if( LISTENERS_ELEMENT_NAME.equals( tag ) ) 
            {
                listeners = buildListenerDirectives( child );
            }
            else if( PROCESSORS_ELEMENT_NAME.equals( tag ) ) 
            {
                processors = buildProcessorDirectives( child );
            }
            else
            {
                final String error = 
                  "Illegal element name [" + tag + "] within 'builder' element.";
                throw new IllegalArgumentException( error );
            }
        }
        return new BuilderDirective( listeners, phase, processors, properties );
    }
    
    private static ListenerDirective[] buildListenerDirectives( Element element ) throws Exception
    {
        Element[] children = ElementHelper.getChildren( element );
        ListenerDirective[] types = new ListenerDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            types[i] = buildListenerDirective( child );
        }
        return types;
    }
    
    private static ListenerDirective buildListenerDirective( Element element ) throws Exception
    {
        final String tag = element.getTagName();
        if( LISTENER_ELEMENT_NAME.equals( tag ) )
        {
            final String name = ElementHelper.getAttribute( element, "name", null );
            final String spec = ElementHelper.getAttribute( element, "uri", null );
            final URI uri = createURI( spec );
            final String classname = ElementHelper.getAttribute( element, "class", null );
            final String deps = ElementHelper.getAttribute( element, "depends", null );
            final String[] depends = buildDependenciesArray( deps );
            final Properties properties = buildProperties( element );
            return new ListenerDirective( name, uri, classname, depends, properties );
        }
        else
        {
            final String error = 
              "Invalid resource element name [" 
              + tag
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
    private static ProcessorDirective[] buildProcessorDirectives( Element element ) throws Exception
    {
        Element[] children = ElementHelper.getChildren( element );
        ProcessorDirective[] processors = new ProcessorDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            processors[i] = buildProcessorDirective( child );
        }
        return processors;
    }
    
    private static ProcessorDirective buildProcessorDirective( Element element ) throws Exception
    {
        final String tag = element.getTagName();
        if( PROCESSOR_ELEMENT_NAME.equals( tag ) )
        {
            final String name = ElementHelper.getAttribute( element, "name", null );
            final String spec = ElementHelper.getAttribute( element, "uri", null );
            final URI uri = createURI( spec );
            final String classname = ElementHelper.getAttribute( element, "class", null );
            final String deps = ElementHelper.getAttribute( element, "depends", null );
            final String[] depends = buildDependenciesArray( deps );
            final Properties properties = buildProperties( element );
            return new ProcessorDirective( name, uri, classname, depends, properties );
        }
        else
        {
            final String error = 
              "Invalid resource element name [" 
              + tag
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
    private static String[] buildDependenciesArray( String value )
    {
        if( null == value )
        {
            return new String[0];
        }
        else
        {
            return value.split( "," );
        }
    }
    
    private static Properties buildProperties( Element element )
    {
        Properties properties = new Properties();
        Element[] children = ElementHelper.getChildren( element );
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            String tag = child.getTagName();
            if( PROPERTY_ELEMENT_NAME.equals( tag ) )
            {
                String key = ElementHelper.getAttribute( child, "name", null );
                if( null == key )
                {
                    final String error =
                      "Property declaration does not contain a 'name' attribute.";
                    throw new IllegalArgumentException( error );
                }
                else
                {
                    String value = ElementHelper.getAttribute( child, "value", null );
                    properties.setProperty( key, value );
                }
            }
        }
        return properties;
    }
    
    private static URI createURI( String spec ) throws Exception
    {
        if( null == spec )
        {
            return null;
        }
        else
        {
            return new URI( spec );
        }
    }
    
}
