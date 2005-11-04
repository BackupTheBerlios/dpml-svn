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

package net.dpml.tools.library;

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

import net.dpml.tools.info.ProcessDescriptor;
import net.dpml.tools.info.LibraryDirective;
import net.dpml.tools.info.ImportDirective;
import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.ResourceDirective.Classifier;
import net.dpml.tools.info.TypeDirective;
import net.dpml.tools.info.DependencyDirective;
import net.dpml.tools.info.Scope;

import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.Category;

import org.w3c.dom.Element;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LibraryDirectiveBuilder
{
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
    private static final String PROCESSORS_ELEMENT_NAME = "processors";
    private static final String PROCESSOR_ELEMENT_NAME = "processor";
    
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
        FileInputStream input = new FileInputStream( source );
        BufferedInputStream buffer = new BufferedInputStream( input );
        try
        {
            final Element root = ElementHelper.getRootElement( input );
            File base = source.getParentFile();
            return buildLibraryDirective( base, root );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured while attempting to build a library directive from the source: "
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
        ProcessDescriptor[] types = new ProcessDescriptor[0];
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
            else if( PROCESSORS_ELEMENT_NAME.equals( tag ) ) 
            {
                types = buildProcessDescriptors( child );
            }
            else
            {
                final String error = 
                  "Illegal element name [" + tag + "] within 'library' element.";
                throw new IllegalArgumentException( error );
            }
        }
        return new LibraryDirective( types, imports, modules, properties );
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
    
    
    private static ProcessDescriptor[] buildProcessDescriptors( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        ProcessDescriptor[] types = new ProcessDescriptor[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            types[i] = buildProcessDescriptor( child );
        }
        return types;
    }
    
    private static ProcessDescriptor buildProcessDescriptor( Element element )
    {
        final String tag = element.getTagName();
        if( PROCESSOR_ELEMENT_NAME.equals( tag ) )
        {
            final String name = ElementHelper.getAttribute( element, "name", null );
            final String urn = ElementHelper.getAttribute( element, "uri", null );
            final String deps = ElementHelper.getAttribute( element, "depends", null );
            final String[] depends = buildProcessDependenciesArray( deps );
            final Properties properties = buildProperties( element );
            return new ProcessDescriptor( name, urn, depends, properties );
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
    
    private static String[] buildProcessDependenciesArray( String value )
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
    
   /**
    * Build a module directive an XML file.
    * @param source the XML source
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
        FileInputStream input = new FileInputStream( source );
        BufferedInputStream buffer = new BufferedInputStream( input );
        try
        {
            final Element root = ElementHelper.getRootElement( input );
            final File parent = source.getParentFile();
            //String baseSpec = base.getCanonicalPath();
            //String parentSpec = parent.getCanonicalPath();
            ///String fragment = parentSpec.substring( baseSpec.length() + 1 );
            return buildModuleDirectiveFromElement( parent, root, path );
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
        finally
        {
            input.close();
        }
    }
        
   /**
    * Build a module directive from an input stream.
    * @param input the input stream
    * @return the module directive
    * @exception Exception if an exception occurs
    */
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
    
   /**
    * Build a module using an XML element.
    * @param element the module element
    */
    private static ModuleDirective buildModuleDirectiveFromElement( 
      File base, Element element, String path ) throws IOException
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
        
        String spec = ElementHelper.getAttribute( element, "base", null );
        if( null != spec )
        {
            File dir = new File( base, spec );
            if( !dir.exists() )
            {
                final String error = 
                  "Cannot include module.xml from the dir ["
                  + dir
                  + "] because the directory does not exist.";
                throw new FileNotFoundException( error ); 
            }
            File source = new File( dir, "module.xml" );
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
        
        ResourceDirective resource = buildResourceDirective( element, path );
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
                  "Include directive does not declare a 'key' or 'ref' attribute.\n"
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
            final String basedir = ElementHelper.getAttribute( element, "basedir", path );
            
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
}
