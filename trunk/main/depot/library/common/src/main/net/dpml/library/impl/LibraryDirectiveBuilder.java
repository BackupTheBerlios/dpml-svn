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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.beans.XMLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.library.info.LibraryDirective;
import net.dpml.library.info.ImportDirective;
import net.dpml.library.info.IncludeDirective;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.ResourceDirective.Classifier;
import net.dpml.library.info.TypeDirective;
import net.dpml.library.info.DependencyDirective;
import net.dpml.library.info.Scope;

import net.dpml.lang.DTD;
import net.dpml.lang.DTDResolver;

import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.util.ExceptionHelper;
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
public final class LibraryDirectiveBuilder
{
    private static final DTD[] DTDS = new DTD[]
    {
        new DTD( 
          ModuleBuilder.PUBLIC_ID, 
          ModuleBuilder.SYSTEM_ID, 
          ModuleBuilder.RESOURCE, null )
    };
    
    private static final DTDResolver DTD_RESOLVER =
        new DTDResolver( DTDS, LibraryDirectiveBuilder.class.getClassLoader() );

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
    
    private LibraryDirectiveBuilder()
    {
    }
    
   /**
    * Construct a library directive from XML source.
    * @param source the XML source file
    * @return the library directive
    * @exception IOException if an IO exception occurs
    */
    public static LibraryDirective build( File source ) throws IOException
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
        catch( Throwable e )
        {
            System.out.println( "# ERROR: " + e.toString() );
            String message = ExceptionHelper.packException( e, true );
            System.out.println( message );
            
            final String error = 
              "An error occured while attempting to build a library directive from the source: "
              + source;
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
            
        }
    }
    
    private static Element getRootElement( File source ) throws IOException
    {
        FileInputStream input = new FileInputStream( source );
        try
        {
            final DocumentBuilderFactory factory =
              DocumentBuilderFactory.newInstance();
            factory.setValidating( true );
            factory.setNamespaceAware( true );
            factory.setExpandEntityReferences( true );
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver( DTD_RESOLVER );
            builder.setErrorHandler( new InternalErrorHandler( source ) );
            
            final Document document = builder.parse( input );
            return document.getDocumentElement();
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            System.out.println( "# ERROR: " + e.toString() );
            String message = ExceptionHelper.packException( e, true );
            System.out.println( message );
            
            final String error = 
              "An unexpected error occured while attempting to load library or module source.";
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
    * Build a module using an XML element.
    * @param base the base directory
    * @param element the module element
    * @return the library directive
    * @exception IOException if an IO exception occurs
    */
    private static LibraryDirective buildLibraryDirective( File base, Element element ) throws IOException
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
    
    private static ModuleDirective[] buildModuleDirectivesFromElement( 
      File base, Element element ) throws IOException
    {
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
    public static ModuleDirective buildModuleDirective( File source, String path ) throws IOException
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
    * Build a module directive from an input stream.
    * @param input the input stream
    * @return the module directive
    * @exception Exception if an exception occurs
    */
    /*
    public static ModuleDirective buildModuleDirective( InputStream input ) throws Exception
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try
        {
            ClassLoader context = ModuleDirective.class.getClassLoader();
            Thread.currentThread().setContextClassLoader( context );
            XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
            return (ModuleDirective) decoder.readObject();
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured while attempting to build a module directive from an input stream.";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( loader );
            input.close();
        }
    }
    */
    
   /**
    * Build a module using an XML element.
    * @param base the base directory
    * @param element the module element
    */
    private static ModuleDirective buildModuleDirectiveFromElement( 
      File base, Element element, String basedir ) throws IOException
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
        
        String path = ElementHelper.getAttribute( element, "file" );
        if( null != path )
        {
            File file = new File( base, path );
            File dir = file.getParentFile();
            String spec = getRelativePath( base, dir );
            
            if( !dir.exists() )
            {
                final String error = 
                  "Cannot include module.xml from the dir ["
                  + dir
                  + "] because the directory does not exist.";
                throw new FileNotFoundException( error ); 
            }
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
        
        ResourceDirective resource = buildResourceDirective( element, basedir );
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
    
    private static String getRelativePath( File base, File dir ) throws IOException
    {
        String baseSpec = base.getCanonicalPath();
        String dirSpec = dir.getCanonicalPath();
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
    private static ImportDirective[] buildImportDirectives( Element element )
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
    
    private static ImportDirective buildImportDirective( Element element )
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

    private static DependencyDirective buildDependencyDirective( Element element )
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
    private static IncludeDirective[] buildIncludeDirectives( Element element )
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
    
    private static IncludeDirective buildIncludeDirective( Element element )
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
    
    private static ResourceDirective buildResourceDirective( Element element )
    {
        return buildResourceDirective( element, null );
    }
    
    private static ResourceDirective buildResourceDirective( Element element, String path )
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
    
    private static TypeDirective[] buildTypeDirectives( Element element )
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

    private static TypeDirective buildTypeDirective( Element element )
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
    
    private static Properties buildProperties( Element element )
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

    private static URI getURIFromSpec( String spec ) 
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
    
    private static final class InternalErrorHandler implements ErrorHandler
    {
        private final File m_file;
        
        InternalErrorHandler( File file )
        {
            m_file = file;
        }
        
        public void error( SAXParseException e ) throws SAXException
        {
            System.out.println( "ERROR: " 
              + e.getMessage()
              + "\nFile: " + m_file
              + "\nLine " + e.getLineNumber() + " Column: " + e.getColumnNumber() );
        }
        public void fatalError( SAXParseException e ) throws SAXException
        {
            System.out.println( "FATAL: " 
              + e.getMessage()
              + "\nFile: " + m_file
              + "\nLine " + e.getLineNumber() + " Column: " + e.getColumnNumber() );
        }
        public void warning( SAXParseException e ) throws SAXException
        {
            System.out.println( "WARNING: " 
              + e.getMessage()
              + "\nFile: " + m_file
              + "\nLine " + e.getLineNumber() + " Column: " + e.getColumnNumber() );
        }
    }
}
