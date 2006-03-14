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
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Map;
import java.beans.XMLDecoder;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.lang.Type;

import net.dpml.library.info.LibraryDirective;
import net.dpml.library.info.ImportDirective;
import net.dpml.library.info.IncludeDirective;
import net.dpml.library.info.IncludeDirective.Mode;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.ResourceDirective.Classifier;
import net.dpml.library.info.DependencyDirective;
import net.dpml.library.info.TypeDirective;
import net.dpml.library.info.Scope;

import net.dpml.library.TypeBuilder;

import net.dpml.part.PartStrategyBuilder;
import net.dpml.part.PartBuilder;
import net.dpml.part.AbstractBuilder;

import net.dpml.transit.Artifact;
import net.dpml.transit.Repository;
import net.dpml.transit.Transit;
import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.util.ExceptionHelper;

import net.dpml.lang.Category;
import net.dpml.lang.BuilderException;

import net.dpml.part.DOM3DocumentBuilder;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.TypeInfo;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LibraryBuilder extends AbstractBuilder
{
    private static final String PART_XSD_URI = "@PART-XSD-URI@";
    private static final String MODULE_XSD_URI = "@MODULE-XSD-URI@";
    private static final String COMMON_XSD_URI = "@COMMON-XSD-URI@";
    
    public static final String XML_HEADER = 
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
      
    private static final String LIBRARY_ELEMENT_NAME = "library";
    private static final String IMPORTS_ELEMENT_NAME = "imports";
    private static final String IMPORT_ELEMENT_NAME = "import";
    private static final String MODULE_ELEMENT_NAME = "module";
    private static final String MODULES_ELEMENT_NAME = "modules";
    private static final String DEPENDENCIES_ELEMENT_NAME = "dependencies";
    private static final String INCLUDE_ELEMENT_NAME = "include";
    private static final String RESOURCE_ELEMENT_NAME = "resource";
    private static final String TYPES_ELEMENT_NAME = "types";
    private static final String TYPE_ELEMENT_NAME = "type";
    private static final String PROJECT_ELEMENT_NAME = "project";
    private static final String PROPERTIES_ELEMENT_NAME = "properties";
    
    private DOM3DocumentBuilder m_builder = new DOM3DocumentBuilder();
    
    public LibraryBuilder()
    {
        this( null );
    }
    
    public LibraryBuilder( Map map )
    {
        super( map );
    }
    
   /**
    * Construct a library directive from XML source.
    * @param source the XML source file
    * @return the library directive
    * @exception IOException if an IO exception occurs
    */
    public LibraryDirective build( File source ) throws IOException
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
        try
        {
            final Element root = getRootElement( source );
            File base = source.getParentFile();
            return buildLibraryDirective( base, root );
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to load library."
              + "File: '" + source + "'";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }
    
    public ModuleDirective buildModule( URI uri ) throws IOException
    {
        try
        {
            final Document document = m_builder.parse( uri );
            Element root = document.getDocumentElement();
            return buildModuleDirectiveFromElement( null, root, null );
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to load module."
              + "URI: '" + uri + "'";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }
    
   /**
    * Write a module directive to an output stream as a portable XML definition.
    * During export dependencies are limited to runtime concerns (eliminating 
    * build and test scoped dependencies).  Artifact production is strippped down
    * to a generic type declaration.  The resulting XML file is suitable for 
    * publication and usage by external projects.
    *
    * @param module the moudle directive to externalize
    * @param output the output stream
    * @exception Exception if an error occurs during module externalization
    */
    public void export( final ModuleDirective module, final OutputStream output ) throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        try
        {
            writer.write( XML_HEADER );
            writer.write( "\n" );
            
            String name = module.getName();
            String version = module.getVersion();
            
            if( null != name )
            {
                writer.write( "<module name=\"" + name + "\"" );
            }
            if( null != version )
            {
                writer.write( " version=\"" + version + "\"" );
            }
            
            writer.write( 
              "\n    xmlns=\"" 
              + MODULE_XSD_URI
              + "\""
              + "\n    xmlns:xsi=\"" 
              + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
              + "\""
              + "\n    xmlns:common=\"" 
              + COMMON_XSD_URI
              + "\">" );
            
            Properties properties = module.getProperties();
            String basedir = module.getBasedir();
            TypeDirective[] types = module.getTypeDirectives();
            DependencyDirective[] dependencies = module.getDependencyDirectives();
            ResourceDirective[] resources = module.getResourceDirectives();
            if( properties.size() > 0 )
            {
                writer.write( "\n" );
                writeProperties( writer, properties, "  ", true );
            }
            if( types.length > 0 )
            {
                writer.write( "\n" );
                writeTypes( writer, types, "  " );
            }
            if( dependencies.length > 0 )
            {
                writer.write( "\n" );
                writeDependencies( writer, dependencies, "  " );
            }
            if( resources.length > 0 )
            {
                writeResources( writer, resources, "  " );
            }
            writer.write( "\n\n</module>" );
            writer.write( "\n" );
        }
        finally
        {
            writer.flush();
            writer.close();
        }
    }
    
    private Element getRootElement( File source ) throws IOException
    {
        File file = source.getCanonicalFile();
        final Document document = m_builder.parse( file.toURI() );
        return document.getDocumentElement();
    }
    
   /**
    * Build a module using an XML element.
    * @param base the base directory
    * @param element the module element
    * @return the library directive
    * @exception IOException if an IO exception occurs
    */
    private LibraryDirective buildLibraryDirective( File base, Element element ) throws Exception
    {
        final String elementName = element.getTagName();
        if( !LIBRARY_ELEMENT_NAME.equals( elementName ) )
        {
            final String error =
              "Element is not a library.";
            throw new IllegalArgumentException( error );
        }
        
        // get type descriptors, modules and properties
        
        Properties properties = null;
        ImportDirective[] imports = new ImportDirective[0];
        ModuleDirective[] modules = new ModuleDirective[0];
        Element[] children = ElementHelper.getChildren( element );
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            final String tag = child.getTagName();
            if( PROPERTIES_ELEMENT_NAME.equals( tag ) )
            {
                properties = buildProperties( child );
            }
            else if( IMPORTS_ELEMENT_NAME.equals( tag ) )
            {
                imports = buildImportDirectives( child );
            }
            else if( MODULES_ELEMENT_NAME.equals( tag ) )
            {
                modules = buildModuleDirectivesFromElement( base, child );
            }
            else
            {
                final String error = 
                  "Illegal element name [" + tag + "] within 'library' element.";
                throw new IllegalArgumentException( error );
            }
        }
        return new LibraryDirective( imports, modules, properties );
    }
    
   /**
    * Construct a module directive relative to the supplied base directory 
    * using the supplied DOM element.
    * @param base the basedire of the enclosing library or module
    * @param element the element definting the module
    */
    private ModuleDirective[] buildModuleDirectivesFromElement( 
      File base, Element element ) throws Exception
    {
        String tag = element.getTagName();
        if( !"modules".equals( element.getTagName() ) )
        {
            final String error = 
              "Unsupported library element name [" + tag + "].";
            throw new IllegalArgumentException( error );
        }
        Element[] children = ElementHelper.getChildren( element );
        ModuleDirective[] modules = new ModuleDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            modules[i] = buildModuleDirectiveFromElement( base, child, null );
        }
        return modules;
    }
    
   /**
    * Build a module directive an XML file.
    * @param source the XML source
    * @param path the relative path
    * @return the module directive
    * @exception IOException if an IO exception occurs
    */
    public ModuleDirective buildModuleDirective( File source, String path ) throws IOException
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
        try
        {
            final Element root = getRootElement( source );
            final File parent = source.getParentFile();
            final String basedir = path;
            return buildModuleDirectiveFromElement( parent, root, basedir );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured while attempting to build a module directive from the source: "
              + source;
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }

        
   /**
    * Build a module using an XML element.
    * @param base the base directory
    * @param element the module element
    * @param offset the imported file directory offset
    */
    private ModuleDirective buildModuleDirectiveFromElement( 
      File base, Element element, String offset ) throws Exception
    {
        final String elementName = element.getTagName();
        if( "import".equals( elementName ) )
        {
            String path = ElementHelper.getAttribute( element, "file" );
            File file = new File( base, path );
            File dir = file.getParentFile();
            String spec = getRelativePath( base, dir );
            File source = file.getCanonicalFile();
            if( !source.exists() )
            {
                final String error = 
                  "Cannot include module ["
                  + source
                  + "] because the file does not exist.";
                throw new FileNotFoundException( error ); 
            }
            else
            {
                return buildModuleDirective( source, spec );
            }
        }
        else if( MODULE_ELEMENT_NAME.equals( elementName ) )
        {
            ResourceDirective resource = buildResourceDirective( element, offset );
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
                    ModuleDirective directive = 
                      buildModuleDirectiveFromElement( base, child, null );
                    list.add( directive );
                }
                else if( IMPORT_ELEMENT_NAME.equals( tag ) ) 
                {
                    ModuleDirective directive = 
                      buildModuleDirectiveFromElement( base, child, null );
                    list.add( directive );
                }
                else if( PROJECT_ELEMENT_NAME.equals( tag ) ) 
                {
                    ResourceDirective directive = 
                      buildResourceDirective( child );
                    list.add( directive );
                }
                else if( RESOURCE_ELEMENT_NAME.equals( tag ) ) 
                {
                    ResourceDirective directive = 
                      buildResourceDirective( child );
                    list.add( directive );
                }
                else
                {
                    final String error = 
                      "Illegal element name [" + tag + "] within 'module' element.";
                    throw new IllegalArgumentException( error );
                }
            }
            ResourceDirective[] resources = (ResourceDirective[]) list.toArray( new ResourceDirective[0] );
            return new ModuleDirective( resource, resources ); 
        }
        else
        {
            final String error =
              "Element ["
              + elementName 
              + "] is not a module.";
            throw new IllegalArgumentException( error );
        }
    }
    
    private String getRelativePath( File base, File dir ) throws IOException
    {
        String baseSpec = base.getCanonicalPath();
        String dirSpec = dir.getCanonicalPath();
        if( dirSpec.equals( baseSpec ) )
        {
            return ".";
        }
        if( dirSpec.startsWith( baseSpec ) )
        {
            return dirSpec.substring( baseSpec.length() + 1 );
        }
        else
        {
            final String error =
             "Supplied dir [" + dirSpec + "] is not with base [" + baseSpec + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Build an array of include directives contained within the supplied enclosing element.
    * @param element the enclosing element
    * @return the array of includes
    */
    private ImportDirective[] buildImportDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        ImportDirective[] includes = new ImportDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            includes[i] = buildImportDirective( child );
        }
        return includes;
    }
    
    private ImportDirective buildImportDirective( Element element )
    {
        final String tag = element.getTagName();
        final Properties properties = buildProperties( element );
        if( IMPORT_ELEMENT_NAME.equals( tag ) )
        {
            if( element.hasAttribute( "file" ) )
            {
                final String value = ElementHelper.getAttribute( element, "file", null );
                return new ImportDirective( ImportDirective.FILE, value, properties );
            }
            else if( element.hasAttribute( "uri" ) )
            {
                final String value = ElementHelper.getAttribute( element, "uri", null );
                return new ImportDirective( ImportDirective.URI, value, properties );
            }
            else
            {
                final String error = 
                  "Import element does not declare a 'file' or 'uri' attribute.\n"
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
    
    private ResourceDirective buildResourceDirective( Element element )  throws Exception
    {
        return buildResourceDirective( element, null );
    }
    
    private ResourceDirective buildResourceDirective( Element element, String path ) throws Exception
    {
        Classifier classifier = null;
        final String tag = element.getTagName();
        if( RESOURCE_ELEMENT_NAME.equals( tag ) || PROJECT_ELEMENT_NAME.equals( tag ) 
          || MODULE_ELEMENT_NAME.equals( tag ) )
        {
            final String name = ElementHelper.getAttribute( element, "name", null );
            final String version = ElementHelper.getAttribute( element, "version", null );
            String basedir = ElementHelper.getAttribute( element, "basedir", null );
            if( path != null )
            {
                if( basedir == null )
                {
                    basedir = path;
                }
                else
                {
                    basedir = path + "/" + basedir;
                }
            }
            
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
                    types = buildTypes( child );
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
    
    private TypeDirective[] buildTypes( Element element ) throws Exception
    {
        Element[] children = ElementHelper.getChildren( element );
        TypeDirective[] types = new TypeDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            types[i] = buildType( child );
        }
        return types;
    }

    private TypeDirective buildType( Element element ) throws Exception
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        if( null == namespace )
        {
            throw new NullPointerException( "namespace" );
        }
        String typeName = info.getTypeName();
        if( info.isDerivedFrom( COMMON_XSD_URI, "AbstractType", TypeInfo.DERIVATION_EXTENSION ) )
        {
            final boolean alias = getAliasFlag( element );
            if( MODULE_XSD_URI.equals( namespace ) )
            {
                if( "GenericType".equals( typeName ) ) 
                {
                    final String id = getID( element );
                    final Properties properties = getProperties( element );
                    return new TypeDirective( id, alias, properties );
                }
                else
                {
                    System.out.println( "# UNRECOGNIZED MODULE TYPE" );
                    final String error = 
                      "Element namespace is recognized as within the module definition "
                      + " however the type identifier is not recognized."
                      + "\nNamespace: " 
                      + namespace
                      + "\nType Name: " 
                      + info.getTypeName();
                    throw new BuilderException( element, error );
                }
            }
            else if( info.isDerivedFrom( PART_XSD_URI, "StrategyType", TypeInfo.DERIVATION_EXTENSION ) )
            {
                return new TypeDirective( "part", alias, element );
            }
            else
            {
                System.out.println( "# UNRECOGNIZED TYPE" );
                final String error = 
                  "Element is recognized as an AbstractType however the type id is not resolvable."
                  + "\nNamespace: " 
                  + namespace
                  + "\nElement Name (from Schema Info): " 
                  + info.getTypeName();
                throw new BuilderException( element, error );
            }
        }
        else
        {
            System.out.println( "# INVALID ELEMENT" );
            final String error = 
              "Element is not derivived from AbstractType defined under the common namespace."
              + "\nNamespace: " + namespace
              + "\nElement Name (from Schema Info): " + info.getTypeName();
            throw new BuilderException( element, error );
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

    private URI getURIFromSpec( String spec ) 
    {
        if( null == spec )
        {
            return null;
        }
        else
        {
            try
            {
                return new URI( spec );
            }
            catch( URISyntaxException e )
            {
                final String error = 
                  "Type descriptor uri ["
                  + spec 
                  + "] could not be converted to a URI value.";
                throw new IllegalArgumentException( error );
            }
        }
    }

    private TypeBuilder loadTypeBuilder( URI uri ) throws Exception
    {
        ClassLoader classloader = TypeBuilder.class.getClassLoader();
        Repository repository = Transit.getInstance().getRepository();
        Object[] args = new Object[0];
        Object handler = repository.getPlugin( classloader, uri, args );
        if( handler instanceof TypeBuilder )
        {
            return (TypeBuilder) handler;
        }
        else
        {
            final String error = 
              "Plugin ["
              + uri
              + "] does not implement the "
              + TypeBuilder.class.getName()
              + " interface.";
            throw new IllegalArgumentException( error );
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
        DependencyDirective[] dependencies = module.getDependencyDirectives();
        ResourceDirective[] resources = module.getResourceDirectives();
        
        writer.write( "\n" + lead + "<module" );
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
        writer.write( "\n\n" + lead + "</module>" );
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
    
    private void writeProperties( 
      Writer writer, Properties properties, String lead, boolean flag ) throws IOException
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
                writeType( writer, type, lead + "  " );
            }
            writer.write( "\n" + lead + "</types>" );
        }
    }
    
    private void writeType( Writer writer, TypeDirective type, String lead ) throws IOException
    {
        String id = type.getID();
        boolean alias = type.getAlias();
        writer.write( "\n" + lead + "  <type id=\"" + id + "\"" );
        if( alias )
        {
            writer.write( " alias=\"true\"" );
        }
        writer.write( "/>" );
    }
    
    private void writeDependencies( 
      Writer writer, DependencyDirective[] dependencies, String lead ) throws IOException
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
                                String label = category.getName().toLowerCase();
                                writer.write( " tag=\"" + label + "\"" );
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

    protected String getID( Element element )
    {
        final String id = ElementHelper.getAttribute( element, "id" );
        if( null == id )
        {
            final String error = 
              "Missing type 'id'.";
            throw new IllegalArgumentException( error );
        }
        else
        {
            return id;
        }
    }

    protected boolean getAliasFlag( Element element )
    {
        return ElementHelper.getBooleanAttribute( element, "alias", false );
    }
    
    protected Properties getProperties( Element element )
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
}
