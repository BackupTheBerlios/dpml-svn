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

package net.dpml.library.impl;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.beans.XMLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.library.info.LibraryDirective;
import net.dpml.library.info.ImportDirective;
import net.dpml.library.info.IncludeDirective;
import net.dpml.library.info.IncludeDirective.Mode;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.ResourceDirective.Classifier;
import net.dpml.library.info.TypeDirective;
import net.dpml.library.info.DependencyDirective;
import net.dpml.library.info.Scope;

import net.dpml.lang.DTD;
import net.dpml.lang.DTDResolver;

import net.dpml.transit.util.ElementHelper;
import net.dpml.lang.Category;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ModuleBuilder
{
    public static final String XML_HEADER = 
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";

    public static final String NAME = "module";

    public static final String PUBLIC_ID = 
      "-//DPML//DTD Library 1.0//EN";
      
    public static final String SYSTEM_ID = 
      "http://www.dpml.net/dtds/library_1_0.dtd";

    public static final String RESOURCE = 
      "net/dpml/library/library_1_0.dtd";

    private static final String DOCTYPE = 
      "\n<!DOCTYPE "
      + NAME
      + " PUBLIC \"" 
      + PUBLIC_ID
      + "\" \""
      + SYSTEM_ID 
      + "\" >";
    
    static final DTD[] DTDS = new DTD[]
    {
        new DTD( 
          PUBLIC_ID, 
          SYSTEM_ID, 
          RESOURCE, null )
    };

    static final DTDResolver DTD_RESOLVER =
        new DTDResolver( DTDS, ModuleBuilder.class.getClassLoader() );

    static final ErrorHandler ERROR_HANDLER =
        new InternalErrorHandler();

    private static final String MODULE_ELEMENT_NAME = "module";
    private static final String MODULES_ELEMENT_NAME = "modules";
    private static final String DEPENDENCIES_ELEMENT_NAME = "dependencies";
    private static final String INCLUDE_ELEMENT_NAME = "include";
    private static final String RESOURCE_ELEMENT_NAME = "resource";
    private static final String TYPES_ELEMENT_NAME = "types";
    private static final String TYPE_ELEMENT_NAME = "type";
    private static final String PROJECT_ELEMENT_NAME = "project";
    private static final String PROPERTIES_ELEMENT_NAME = "properties";
    
   /**
    * Construct a module from a supplied url.
    * @param url the module url
    * @return the module datastructure
    * @exception Exception if an error occurs during module loading
    */
    public ModuleDirective load( final URL url ) throws Exception
    {
        URLConnection connection = url.openConnection();
        InputStream input = connection.getInputStream();

        final DocumentBuilderFactory factory =
          DocumentBuilderFactory.newInstance();
        factory.setValidating( true );
        factory.setNamespaceAware( true );
        factory.setExpandEntityReferences( true );
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver( DTD_RESOLVER );
        builder.setErrorHandler( ERROR_HANDLER );
        
        final Document document = builder.parse( input );
        final DocumentType docType = document.getDoctype();
        final Element root = document.getDocumentElement();
        return build( root );
    }
    
   /**
    * Write a module directive to an output stream as a portable XML definition.
    * @param module the moudle directive to externalize
    * @param output the output stream
    * @exception Exception if an error occurs during module externalization
    */
    public void write( final ModuleDirective module, final OutputStream output ) throws Exception
    {
        final Writer writer = new OutputStreamWriter( output );
        try
        {
            writer.write( XML_HEADER );
            writer.write( DOCTYPE );
            writer.write( "\n" );
            writeModule( writer, module, "" );
            writer.write( "\n" );
        }
        finally
        {
            writer.flush();
            writer.close();
        }
    }
    
    //-----------------------------------------------------------------------
    // internal utilities supporting ModuleDirective to XML creation
    //-----------------------------------------------------------------------
    
    private void writeModule( Writer writer, ModuleDirective module, String lead ) throws IOException
    {
        String name = module.getName();
        String version = module.getVersion();
        
        Properties properties = module.getProperties();
        String basedir = module.getBasedir();
        TypeDirective[] types = module.getTypeDirectives();
        DependencyDirective[] dependencies = new DependencyDirective[0];
        ResourceDirective[] resources = module.getResourceDirectives();
        
        writer.write( "\n" + lead + "<" + NAME );
        if( null != name )
        {
            writer.write( " name=\"" + name + "\"" );
        }
        if( null != version )
        {
            writer.write( " version=\"" + version + "\"" );
        }
        writer.write( ">" );
        
        if( properties.size() > 0 )
        {
            writer.write( "\n" );
            writeProperties( writer, properties, lead + "  ", true );
        }

        if( types.length > 0 )
        {
            writer.write( "\n" );
            writeTypes( writer, types, lead + "  " );
        }
        
        if( dependencies.length > 0 )
        {
            writer.write( "\n" );
            writeDependencies( writer, dependencies, lead + "  " );
        }
        
        if( resources.length > 0 )
        {
            writeResources( writer, resources, lead + "  " );
        }
        writer.write( "\n\n" + lead + "</" + NAME + ">" );
    }
    
    private void writeResource( Writer writer, ResourceDirective resource, String lead ) throws IOException
    {
        String name = resource.getName();
        String version = resource.getVersion();
        
        Properties properties = resource.getProperties();
        String basedir = resource.getBasedir();
        TypeDirective[] types = resource.getTypeDirectives();
        DependencyDirective[] dependencies = resource.getDependencyDirectives();
        
        writer.write( "\n" + lead + "<resource"  );
        if( null != name )
        {
            writer.write( " name=\"" + name + "\"" );
        }
        if( null != version )
        {
            writer.write( " version=\"" + version + "\"" );
        }
        writer.write( ">" );
        
        if( properties.size() > 0 )
        {
            writeProperties( writer, properties, lead + "  ", true );
        }
        if( types.length > 0 )
        {
            writeTypes( writer, types, lead + "  " );
        }
        if( dependencies.length > 0 )
        {
            writeDependencies( writer, dependencies, lead + "  " );
        }
        writer.write( "\n" + lead + "</resource>" );
    }
    
    private void writeProperties( Writer writer, Properties properties, String lead, boolean flag ) throws IOException
    {
        if( properties.size() > 0 )
        {
            if( flag )
            {
                writer.write( "\n" + lead + "<properties>" );
            }
            String[] names = (String[]) properties.keySet().toArray( new String[0] );
            for( int i=0; i<names.length; i++ )
            {
                String name = names[i];
                String value = properties.getProperty( name );
                writer.write( "\n" + lead );
                if( flag )
                {
                    writer.write( "  " );
                }
                writer.write( "<property name=\"" + name + "\" value=\"" + value + "\"/>" );
            }
            if( flag )
            {
                writer.write( "\n" + lead + "</properties>" );
            }
        }
    }
    
    private void writeTypes( Writer writer, TypeDirective[] types, String lead ) throws IOException
    {
        if( types.length > 0 )
        {
            writer.write( "\n" + lead + "<types>" );
            for( int i=0; i<types.length; i++ )
            {
                TypeDirective type = types[i];
                String id = type.getName();
                boolean alias = type.getAlias();
                writer.write( "\n" + lead + "  <type id=\"" + id + "\"" );
                if( alias )
                {
                    writer.write( " alias=\"true\"" );
                }
                Properties properties = type.getProperties();
                if( properties.size() > 0 )
                {
                    writer.write( ">" );
                    writeProperties( writer, properties, lead + "  ", false );
                }
            }
            writer.write( "\n" + lead + "</types>" );
        }
    }
    
    private void writeDependencies( Writer writer, DependencyDirective[] dependencies, String lead ) throws IOException
    {
        if( dependencies.length > 0 )
        {
            for( int i=0; i<dependencies.length; i++ )
            {
                DependencyDirective dependency = dependencies[i];
                IncludeDirective[] includes = dependency.getIncludeDirectives();
                if( includes.length > 0 )
                {
                    Scope scope = dependency.getScope();
                    if( Scope.RUNTIME.equals( scope ) )
                    {
                        writer.write( "\n" + lead + "<dependencies>" );
                    }
                    else
                    {
                        writer.write( "\n" + lead + "<dependencies scope=\"" + scope + "\">" );
                    }
                    
                    for( int j=0; j<includes.length; j++ )
                    {
                        IncludeDirective include = includes[j];
                        Mode mode = include.getMode();
                        String value = include.getValue();
                        writer.write( "\n" + lead + "  <include" );
                        if( Mode.KEY.equals( mode ) )
                        {
                            writer.write( " key=\"" + value + "\"" );
                        }
                        else if( Mode.REF.equals( mode ) )
                        {
                            writer.write( " ref=\"" + value + "\"" );
                        }
                        else if( Mode.URN.equals( mode ) )
                        {
                            writer.write( " urn=\"" + value + "\"" );
                        }
                        
                        if( Scope.RUNTIME.equals( scope ) )
                        {
                            Category category = include.getCategory();
                            if( !Category.PRIVATE.equals( category ) )
                            {
                                writer.write( " tag=\"" + category + "\"" );
                            }
                        }
                        
                        Properties props = include.getProperties();
                        if( props.size() > 0 )
                        {
                            writer.write( ">" );
                            writeProperties( writer, props, lead + "    ", false );
                            writer.write( "\n" + lead + "  </include>");
                        }
                        else
                        {
                            writer.write( "/>" );
                        }
                    }
                    writer.write( "\n" + lead + "</dependencies>" );
                }
            }
        }
    }
    
    private void writeResources( Writer writer, ResourceDirective[] resources, String lead ) throws IOException
    {
        for( int i=0; i<resources.length; i++ )
        {
            ResourceDirective resource = resources[i];
            if( resource instanceof ModuleDirective )
            {
                writer.write( "\n" );
                ModuleDirective module = (ModuleDirective) resource;
                writeModule( writer, module, lead );
            }
            else
            {
                writer.write( "\n" );
                writeResource( writer, resource, lead );
            }
        }
    }
    
    //-----------------------------------------------------------------------
    // internal utilities supporting XML to ModuleDirective creation
    //-----------------------------------------------------------------------
    
    private ModuleDirective build( Element element )
    {
        final String elementName = element.getTagName();
        if( !MODULE_ELEMENT_NAME.equals( elementName ) )
        {
            final String error =
              "Element ["
              + elementName 
              + "] is not a module.";
            throw new IllegalArgumentException( error );
        }
        
        ResourceDirective resource = buildResourceDirective( element );
        ArrayList list = new ArrayList();
        Element[] children = ElementHelper.getChildren( element );
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            final String tag = child.getTagName();
            if( PROPERTIES_ELEMENT_NAME.equals( tag ) )
            {
                boolean ok = true; // already processed
            }
            else if( DEPENDENCIES_ELEMENT_NAME.equals( tag ) ) 
            {
                boolean ok = true; // already processed
            }
            else if( TYPES_ELEMENT_NAME.equals( tag ) ) 
            {
                boolean ok = true; // already processed
            }
            else if( MODULE_ELEMENT_NAME.equals( tag ) )
            {
                ModuleDirective directive = build( child );
                list.add( directive );
            }
            else if( PROJECT_ELEMENT_NAME.equals( tag ) ) 
            {
                ResourceDirective directive = buildResourceDirective( child );
                list.add( directive );
            }
            else if( RESOURCE_ELEMENT_NAME.equals( tag ) ) 
            {
                ResourceDirective directive = buildResourceDirective( child );
                list.add( directive );
            }
            else
            {
                final String error = 
                  "Illegal element name [" 
                  + tag 
                  + "] within 'module' element.";
                throw new IllegalArgumentException( error );
            }
        }
        ResourceDirective[] resources = (ResourceDirective[]) list.toArray( new ResourceDirective[0] );
        return new ModuleDirective( resource, resources );
    }
    
    private DependencyDirective buildDependencyDirective( Element element )
    {
        final String tag = element.getTagName();
        if( DEPENDENCIES_ELEMENT_NAME.equals( tag ) )
        {
            final String spec = ElementHelper.getAttribute( element, "scope", "runtime" );
            Scope scope = Scope.parse( spec );
            Element[] children = ElementHelper.getChildren( element );
            IncludeDirective[] includes = new IncludeDirective[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                includes[i] = buildIncludeDirective( child );
            }
            return new DependencyDirective( scope, includes );
        }
        else
        {
            final String error = 
              "Invalid dependency element name [" 
              + tag
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Build an array of include directives contained within the supplied enclosing element.
    * @param element the enclosing element
    * @return the array of includes
    */
    private IncludeDirective[] buildIncludeDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        IncludeDirective[] includes = new IncludeDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            includes[i] = buildIncludeDirective( child );
        }
        return includes;
    }
    
    private IncludeDirective buildIncludeDirective( Element element )
    {
        final String tag = element.getTagName();
        final Properties properties = buildProperties( element );
        if( INCLUDE_ELEMENT_NAME.equals( tag ) )
        {
        
            final String tagValue = ElementHelper.getAttribute( element, "tag", "private" );
            Category category = Category.parse( tagValue );
            if( element.hasAttribute( "key" ) )
            {
                final String value = ElementHelper.getAttribute( element, "key", null );
                return new IncludeDirective( IncludeDirective.KEY, category, value, properties );
            }
            else if( element.hasAttribute( "ref" ) )
            {
                final String value = ElementHelper.getAttribute( element, "ref", null );
                return new IncludeDirective( IncludeDirective.REF, category, value, properties );
            }
            else if( element.hasAttribute( "urn" ) )
            {
                final String value = ElementHelper.getAttribute( element, "urn", null );
                return new IncludeDirective( IncludeDirective.URN, category, value, properties );
            }
            else
            {
                final String error = 
                  "Include directive does not declare a 'urn', 'key' or 'ref' attribute.\n"
                  + element.toString();
                throw new IllegalArgumentException( error );
            }
        }
        else
        {
            final String error = 
              "Invalid include element name [" 
              + tag 
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
    private ResourceDirective buildResourceDirective( Element element )
    {
        Classifier classifier = null;
        final String tag = element.getTagName();
        if( RESOURCE_ELEMENT_NAME.equals( tag ) || PROJECT_ELEMENT_NAME.equals( tag ) 
          || MODULE_ELEMENT_NAME.equals( tag ) )
        {
            final String name = ElementHelper.getAttribute( element, "name" );
            final String version = ElementHelper.getAttribute( element, "version" );
            final String basedir = ElementHelper.getAttribute( element, "basedir" );
            
            if( PROJECT_ELEMENT_NAME.equals( tag ) )
            {
                classifier = Classifier.LOCAL;
                if( null == basedir )
                {
                    final String error = 
                      "Missing basedir attribute on project [" 
                      + name
                      + "].";
                    throw new IllegalArgumentException( error );
                }
            }
            else if( MODULE_ELEMENT_NAME.equals( tag ) )
            {
                if( null != basedir )
                {
                    classifier = Classifier.LOCAL;
                }
                else
                {
                    classifier = Classifier.EXTERNAL;
                }
            }
            else
            {
                classifier = Classifier.EXTERNAL;
            }
            
            ArrayList dependencies = new ArrayList();
            TypeDirective[] types = new TypeDirective[0];
            Element[] children = ElementHelper.getChildren( element );
            Properties properties = null;
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                final String childTag = child.getTagName();
                if( TYPES_ELEMENT_NAME.equals( childTag ) )
                {
                    types = buildTypeDirectives( child );
                }
                else if( DEPENDENCIES_ELEMENT_NAME.equals( childTag ) )
                {
                    DependencyDirective dependency = buildDependencyDirective( child );
                    dependencies.add( dependency );
                }
                else if( PROPERTIES_ELEMENT_NAME.equals( childTag ) )
                {
                    properties = buildProperties( child );
                }
            }
            DependencyDirective[] deps = (DependencyDirective[]) dependencies.toArray( new DependencyDirective[0] );
            return new ResourceDirective( name, version, classifier, basedir, types, deps, properties );
        }
        else
        {
            final String error = 
              "Invalid element name [" 
              + tag
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
    private TypeDirective[] buildTypeDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        TypeDirective[] types = new TypeDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            types[i] = buildTypeDirective( child );
        }
        return types;
    }

    private TypeDirective buildTypeDirective( Element element )
    {
        final String tag = element.getTagName();
        if( TYPE_ELEMENT_NAME.equals( tag ) )
        {
            final String name = ElementHelper.getAttribute( element, "id", null );
            final boolean alias = ElementHelper.getBooleanAttribute( element, "alias", false );
            final Properties properties = buildProperties( element );
            return new TypeDirective( name, alias, properties );
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
    
    private Properties buildProperties( Element element )
    {
        Properties properties = new Properties();
        Element[] children = ElementHelper.getChildren( element );
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            String tag = child.getTagName();
            if( "property".equals( tag ) )
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

    private static final class InternalErrorHandler implements ErrorHandler
    {
        public void error( SAXParseException e ) throws SAXException
        {
            System.out.println( "ERROR: " + e.getMessage() );
        }
        public void fatalError( SAXParseException e ) throws SAXException
        {
            System.out.println( "FATAL: " + e.getMessage() );
        }
        public void warning( SAXParseException e ) throws SAXException
        {
            System.out.println( "WARN: " + e.getMessage() );
        }
    }
}
