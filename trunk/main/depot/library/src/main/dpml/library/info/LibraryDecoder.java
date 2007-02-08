/*
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

package dpml.library.info;

import dpml.lang.DOM3DocumentBuilder;
import dpml.util.ElementHelper;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import dpml.library.Feature;
import dpml.library.Classifier;
import dpml.library.Scope;

import dpml.util.Category;
import net.dpml.lang.Version;
import net.dpml.lang.DecodingException;

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
        final Element root = getRootElement( source );
        try
        {
            File base = source.getParentFile();
            return buildLibraryDirective( base, root );
        }
        catch( DecodingException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to load library."
              + "\nFile: '" + source + "'";
            throw new DecodingException( error, e, root );
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
    * Resolve the root DOM element of the supplied file.
    * @param source the source XML file
    * @return the root element
    * @exception IOException if an io error occurs
    */
    private Element getRootElement( File source ) throws IOException
    {
        File file = source.getCanonicalFile();
        final Document document = DOCUMENT_BUILDER.parse( file.toURI() );
        return document.getDocumentElement();
    }
    
   /**
    * Build a library directive using an XML element.
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
        List<ResourceDirective> list = new ArrayList<ResourceDirective>();
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
            else
            {
                ResourceDirective resource = buildResourceDirectiveFromElement( base, child, null );
                list.add( resource );
            }
        }
        ResourceDirective[] resources = list.toArray( new ResourceDirective[0] );
        return new LibraryDirective( imports, resources, properties );
    }
    
   /**
    * Build a resource using an XML element.
    * @param base the base directory
    * @param element the module element
    * @param offset the imported file directory offset
    * @throws Exception if an error occurs
    */
    private ResourceDirective buildResourceDirectiveFromElement( 
      File base, Element element, String offset ) throws Exception
    {
        final String elementName = element.getTagName();
        final String path = ElementHelper.getAttribute( element, "file" );
        if( null != path )
        {
            try
            {
                File file = new File( base, path );
                File source = file.getCanonicalFile();
                if( !source.exists() )
                {
                    final String error = 
                      "Local file does not exist."
                      + "\n  base: " + base
                      + "\n  path: " + path
                      + "\n  source: " + source;
                    throw new DecodingException( error, null, element ); 
                }
                else if( source.isDirectory() )
                {
                    final String error = 
                      "Local file references a directory."
                      + "\n  base: " + base
                      + "\n  path: " + path
                      + "\n  source: " + source;
                    throw new DecodingException( error, null, element ); 
                }
                else
                {
                    final File parent = source.getParentFile();
                    final Element root = getRootElement( source );
                    String basedir = getRelativePath( base, parent );
                    if( null != offset )
                    {
                        basedir = offset + "/" + basedir;
                    }
                    return buildResourceDirectiveFromElement( base, root, basedir );
                }
            }
            catch( DecodingException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while attempting to resolve a import directive.";
                    throw new DecodingException( error, e, element ); 
            }
        }
        else
        {
            return buildResourceDirective( base, element, offset );
        }
    }
    
    private ResourceDirective buildResourceDirective( File base, Element element )  throws Exception
    {
        return buildResourceDirective( base, element, null );
    }
    
    private ResourceDirective buildResourceDirective( File base, Element element, String path ) throws Exception
    {
        Classifier classifier = null;
        final String tag = element.getTagName();
        if( RESOURCE_ELEMENT_NAME.equals( tag ) || PROJECT_ELEMENT_NAME.equals( tag ) 
          || MODULE_ELEMENT_NAME.equals( tag ) )
        {
            final String name = ElementHelper.getAttribute( element, "name" );
            final String version = ElementHelper.getAttribute( element, "version" );
            String basedir = ElementHelper.getAttribute( element, "basedir" );
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
                    basedir = ".";
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
            
            final InfoDirective info = 
              buildInfoDirective( 
                ElementHelper.getChild( element, "info" ) );
            
            final TypeDirective[] data = 
              buildTypeDirectives( 
                ElementHelper.getChild( element, "types" ) );
            
            final DependencyDirective[] dependencies = 
              buildDependencyDirectives( 
                ElementHelper.getChild( element, "dependencies" ) );
                
            final FilterDirective[] filters = 
              buildFilterDirectives( 
                ElementHelper.getChild( element, "filters" ) );
            
            final Properties properties = 
              buildProperties( 
                ElementHelper.getChild( element, "properties" ) );
            
            if( MODULE_ELEMENT_NAME.equals( tag ) )
            {
                File anchor = getAnchorDirectory( base, basedir );
                ArrayList<ResourceDirective> list = new ArrayList<ResourceDirective>();
                Element[] children = ElementHelper.getChildren( element );
                for( int i=0; i<children.length; i++ )
                {
                    Element child = children[i];
                    final String t = child.getTagName();
                    
                    if( RESOURCE_ELEMENT_NAME.equals( t ) 
                      || PROJECT_ELEMENT_NAME.equals( t ) 
                      || MODULE_ELEMENT_NAME.equals( t ) )
                    {
                        ResourceDirective directive = 
                          buildResourceDirectiveFromElement( anchor, child, null );
                        list.add( directive );
                    }
                }
                
                ResourceDirective[] resources = list.toArray( new ResourceDirective[0] );
                return ModuleDirective.createModuleDirective( 
                  name, version, classifier, basedir, info, data, dependencies, 
                  properties, filters, resources );
            }
            else
            {
                return ResourceDirective.createResourceDirective( 
                  name, version, classifier, basedir, info, data, dependencies, 
                  properties, filters );
            }
        }
        else
        {
            final String error = 
              "Invalid element name [" 
              + tag
              + "].";
            throw new DecodingException( error, null, element ); 
        }
    }
    
    private File getAnchorDirectory( File base, String path ) throws IOException
    {
        if( null == base )
        {
            return null;
        }
        if( !base.exists() )
        {
            final String error = 
              "Base directory [" + base + "] does not exist.";
            throw new IllegalArgumentException( error );
        }
        if( null == path )
        {
            return base;
        }
        else
        {
            return new File( base, path ).getCanonicalFile();
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
    private ImportDirective[] buildImportDirectives( Element element ) throws DecodingException
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
    
    private ImportDirective buildImportDirective( Element element ) throws DecodingException
    {
        final String tag = element.getTagName();
        final Properties properties = buildProperties( element );
        if( IMPORT_ELEMENT_NAME.equals( tag ) )
        {
            try
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
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while attempting to resolve an import directive.";
                    throw new DecodingException( error, e, element ); 
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

    private DependencyDirective[] buildDependencyDirectives( Element element ) throws DecodingException
    {
        if( null == element )
        {
            return new DependencyDirective[0];
        }
        else
        {
            final String tag = element.getTagName();
            if( DEPENDENCIES_ELEMENT_NAME.equals( tag ) )
            {
                Element[] children = ElementHelper.getChildren( element );
                DependencyDirective[] dependencies = new DependencyDirective[ children.length ];
                for( int i=0; i<children.length; i++ )
                {
                    Element child = children[i];
                    dependencies[i] = buildDependencyDirective( child );
                }
                return dependencies;
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
    }
    
    private DependencyDirective buildDependencyDirective( Element element ) throws DecodingException
    {
        String name = element.getTagName().toUpperCase();
        Scope scope = Scope.valueOf( name );
        if( Scope.BUILD.equals( scope ) )
        {
            IncludeDirective[] includes = buildIncludeDirectives( element, false );
            return new DependencyDirective( Scope.BUILD, includes );
        }
        else if( Scope.RUNTIME.equals( scope ) )
        {
            IncludeDirective[] includes = buildIncludeDirectives( element, true );
            return new DependencyDirective( Scope.RUNTIME, includes );
        }
        else
        {
            IncludeDirective[] includes = buildIncludeDirectives( element, false );
            return new DependencyDirective( Scope.TEST, includes );
        }
    }
    
   /**
    * Build an array of include directives contained within the supplied enclosing element.
    * @param element the enclosing element
    * @param flag if category processing is required
    * @return the array of includes
    */
    private IncludeDirective[] buildIncludeDirectives( Element element, boolean flag ) throws DecodingException
    {
        Element[] children = ElementHelper.getChildren( element );
        IncludeDirective[] includes = new IncludeDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            includes[i] = buildIncludeDirective( child, flag );
        }
        return includes;
    }
    
    private IncludeDirective buildIncludeDirective( Element element, boolean flag ) throws DecodingException
    {
        final String tag = element.getTagName();
        final Properties properties = buildProperties( element );
        if( INCLUDE_ELEMENT_NAME.equals( tag ) )
        {
            Category category = buildCategory( element, flag );
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
            else if( element.hasAttribute( "uri" ) )
            {
                final String value = ElementHelper.getAttribute( element, "uri", null );
                return new IncludeDirective( IncludeDirective.URI, category, value, properties );
            }
            else
            {
                final String error = 
                  "Include directive does not declare a 'uri', 'key' or 'ref' attribute.\n"
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
                    throw new DecodingException( error, null, element ); 
        }
    }
    
    private Category buildCategory( Element element, boolean flag )
    {
        if( !flag ) 
        {
            return Category.PRIVATE; // was UNDEFINED
        }
        else
        {
            final String value = ElementHelper.getAttribute( element, "tag", "private" );
            return Category.valueOf( value.toUpperCase() );
        }
    }
    
    private InfoDirective buildInfoDirective( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            String title = ElementHelper.getAttribute( element, "title" );
            Element child = ElementHelper.getChild( element, "description" );
            if( null == child )
            {
                return new InfoDirective( title, null );
            }
            else
            {
                String value = ElementHelper.getValue( child );
                String description = trim( value );
                return new InfoDirective( title, description );
            }
        }
    }
    
    private String trim( String value )
    {
        if( null == value )
        {
            return null;
        }
        String trimmed = value.trim();
        if( trimmed.startsWith( "\n" ) )
        {
            return trim( trimmed.substring( 1 ) );
        }
        else if( trimmed.endsWith( "\n" ) )
        {
            return trim( trimmed.substring( 0, trimmed.length() - 1 ) );
        }
        else
        {
            return trimmed;
        }
    }
    
    private FilterDirective[] buildFilterDirectives( Element element ) throws Exception
    {
        if( null == element )
        {
            return new FilterDirective[0];
        }
        else
        {
            Element[] children = ElementHelper.getChildren( element );
            FilterDirective[] filters = new FilterDirective[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String token = ElementHelper.getAttribute( child, "token" );
                if( "filter".equals( child.getTagName() ) )
                {
                    String value = ElementHelper.getAttribute( child, "value" );
                    filters[i] = new SimpleFilterDirective( token, value );
                }
                else if( "feature".equals( child.getTagName() ) )
                {
                    String id = ElementHelper.getAttribute( child, "id" ).toUpperCase();
                    Feature feature = Feature.valueOf( id );
                    String ref = ElementHelper.getAttribute( child, "ref" );
                    String type = ElementHelper.getAttribute( child, "type" );
                    boolean alias = ElementHelper.getBooleanAttribute( child, "alias" );
                    filters[i] = new FeatureFilterDirective( token, ref, feature, type, alias );
                }
                else
                {
                    final String error = 
                      "Element name not recognized [" 
                      + child.getTagName()
                      + "] (expecting 'filter').";
                    throw new DecodingException( error, null, element ); 
                }
            }
            return filters;
        }
    }
    
    private TypeDirective[] buildTypeDirectives( Element element ) throws Exception
    {
        if( null == element )
        {
            return new TypeDirective[0];
        }
        Element[] children = ElementHelper.getChildren( element );
        TypeDirective[] data = new TypeDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            data[i] = buildTypeDirective( child );
        }
        return data;
    }

    private TypeDirective buildTypeDirective( Element element ) throws Exception
    {
        final String id = getID( element );
        final Properties properties = getProperties( element );
        final String version = getVersion( element );
        final String source = getSource( element );
        final String name = ElementHelper.getAttribute( element, "name" );
        boolean alias = getAliasPolicy( element );
        ProductionPolicy production = getProductionPolicy( element );
        return new TypeDirective( production, id, name, version, alias, source, properties );
    }
    
    private String getVersion( Element element )
    {
        return ElementHelper.getAttribute( element, "version" );
    }
    
    private Properties buildProperties( Element element )
    {
        if( null == element )
        {
            return null;
        }
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
            throw new DecodingException( error, null, element ); 
        }
        else
        {
            return id;
        }
    }

   /**
    * Return the source attribute of the supplied element.
    * @param element the DOM element
    * @return the source value
    */
    protected String getSource( Element element )
    {
        return ElementHelper.getAttribute( element, "source" );
    }

   /**
    * Return the alias attribute of the supplied element.
    * @param element the DOM element
    * @return the alias production flag value
    */
    protected boolean getAliasPolicy( Element element )
    {
        return ElementHelper.getBooleanAttribute( element, "alias", false );
    }
    
    private ProductionPolicy getProductionPolicy( Element element )
    {
        boolean flag = ElementHelper.getBooleanAttribute( element, "test", false );
        if( flag )
        {
            return ProductionPolicy.TEST;
        }
        else
        {
            return ProductionPolicy.DELIVERABLE;
        }
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
