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
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;

import net.dpml.library.TypeBuilder;
import net.dpml.library.info.LibraryDirective;
import net.dpml.library.info.ImportDirective;
import net.dpml.library.info.IncludeDirective;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.ResourceDirective.Classifier;
import net.dpml.library.info.DependencyDirective;
import net.dpml.library.info.TypeDirective;
import net.dpml.library.info.Scope;

import net.dpml.transit.Repository;
import net.dpml.transit.Transit;
import net.dpml.transit.util.ElementHelper;

import net.dpml.lang.Category;
import net.dpml.lang.DecodingException;

import net.dpml.part.DOM3DocumentBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.TypeInfo;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LibraryDecoder extends LibraryConstants
{
    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = new DOM3DocumentBuilder();
    
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
    
   /**
    * Construct a resource directive from source.
    * @param uri the source uri
    * @return the resource directive
    * @exception IOException if an IO exception occurs
    */
    public ResourceDirective buildResource( URI uri ) throws IOException
    {
        try
        {
            final Document document = DOCUMENT_BUILDER.parse( uri );
            Element root = document.getDocumentElement();
            return buildResourceDirectiveFromElement( null, root, null );
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
    
    private Element getRootElement( File source ) throws IOException
    {
        File file = source.getCanonicalFile();
        final Document document = DOCUMENT_BUILDER.parse( file.toURI() );
        return document.getDocumentElement();
    }
    
   /**
    * Build a module using an XML element.
    * @param base the base directory
    * @param element the module element
    * @return the library directive
    * @exception Exception if an exception occurs
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
        ResourceDirective[] resources = new ResourceDirective[0];
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
                resources = buildResourceDirectivesFromElement( base, child );
            }
            else
            {
                final String error = 
                  "Illegal element name [" + tag + "] within 'library' element.";
                throw new IllegalArgumentException( error );
            }
        }
        return new LibraryDirective( imports, resources, properties );
    }
    
   /**
    * Construct a module directive relative to the supplied base directory 
    * using the supplied DOM element.
    * @param base the basedire of the enclosing library or module
    * @param element the element definting the module
    */
    private ResourceDirective[] buildResourceDirectivesFromElement( 
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
        ResourceDirective[] resources = new ModuleDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            resources[i] = buildResourceDirectiveFromElement( base, child, null );
        }
        return resources;
    }
    
   /**
    * Build a resource directive from an XML file.
    * @param source the XML source
    * @param path the relative path
    * @return the resource directive
    * @exception IOException if an IO exception occurs
    */
    public ResourceDirective buildResourceDirective( File source, String path ) throws IOException
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
            final File parent = source.getParentFile();
            final String basedir = path;
            final Element root = getRootElement( source );
            final String tag = root.getTagName();
            if( "module".equals( tag ) || "project".equals( tag ) || "resource".equals( tag ) )
            {
                return buildResourceDirectiveFromElement( parent, root, basedir );
            }
            else
            {
                throw new IllegalArgumentException( tag );
            }
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
    * Build a resource using an XML element.
    * @param base the base directory
    * @param element the module element
    * @param offset the imported file directory offset
    * @param Exception if an error occurs
    */
    private ResourceDirective buildResourceDirectiveFromElement( 
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
                return buildResourceDirective( source, spec ); // ##
            }
        }
        else if( RESOURCE_ELEMENT_NAME.equals( elementName ) )
        {
            return buildResourceDirective( element, offset );
        }
        else if( PROJECT_ELEMENT_NAME.equals( elementName ) )
        {
            return buildResourceDirective( element, offset );
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
                    ResourceDirective directive = 
                      buildResourceDirectiveFromElement( base, child, null );
                    list.add( directive );
                }
                else if( IMPORT_ELEMENT_NAME.equals( tag ) ) 
                {
                    ResourceDirective directive = 
                      buildResourceDirectiveFromElement( base, child, null );
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
            DependencyDirective[] deps = 
              (DependencyDirective[]) dependencies.toArray( new DependencyDirective[0] );
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
                    throw new DecodingException( element, error );
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
                throw new DecodingException( element, error );
            }
        }
        else
        {
            System.out.println( "# INVALID ELEMENT" );
            final String error = 
              "Element is not derivived from AbstractType defined under the common namespace."
              + "\nNamespace: " + namespace
              + "\nElement Name (from Schema Info): " + info.getTypeName();
            throw new DecodingException( element, error );
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
    
   /**
    * Return the id attribute of the supplied element.
    * @param element the DOM element
    * @return the id value
    * @exception DecodingException if an error occurs during element evaluation
    */
    protected String getID( Element element ) throws DecodingException
    {
        final String id = ElementHelper.getAttribute( element, "id" );
        if( null == id )
        {
            final String error = 
              "Missing type 'id'.";
            throw new DecodingException( element, error );
        }
        else
        {
            return id;
        }
    }

   /**
    * Return the alias attribute of the supplied element.
    * @param element the DOM element
    * @return the alias production flag value
    */
    protected boolean getAliasFlag( Element element )
    {
        return ElementHelper.getBooleanAttribute( element, "alias", false );
    }
    
   /**
    * Return properties declared within the supplied element as immediate
    * child <property> elements.
    * @param element the enclosing DOM element
    * @return the resolved properties instance
    */
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