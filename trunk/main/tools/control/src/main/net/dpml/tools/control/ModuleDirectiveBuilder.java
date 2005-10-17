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

package net.dpml.tools.control;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Properties;

import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.ModuleIncludeDirective;
import net.dpml.tools.info.ResourceIncludeDirective;
import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.ProjectDirective;
import net.dpml.tools.info.TypeDirective;
import net.dpml.tools.info.ProductionDirective;
import net.dpml.tools.info.DependencyDirective;
import net.dpml.tools.info.Scope;

import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Element;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ModuleDirectiveBuilder
{
    private static final String MODULE_ELEMENT_NAME = "module";
    private static final String RESOURCES_ELEMENT_NAME = "resources";
    private static final String DEPENDENCIES_ELEMENT_NAME = "dependencies";
    private static final String PROJECTS_ELEMENT_NAME = "projects";
    private static final String INCLUDE_ELEMENT_NAME = "include";
    private static final String RESOURCE_ELEMENT_NAME = "resource";
    private static final String TYPES_ELEMENT_NAME = "types";
    private static final String TYPE_ELEMENT_NAME = "type";
    private static final String PROJECT_ELEMENT_NAME = "project";
    private static final String PRODUCES_ELEMENT_NAME = "production";
    private static final String PROPERTIES_ELEMENT_NAME = "properties";
    
    public static ModuleDirective build( File source ) throws IOException
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
            return buildModuleDirective( base, root );
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
    
    public static ModuleDirective build( InputStream input ) throws Exception
    {
        try
        {
            final Element root = ElementHelper.getRootElement( input );
            return buildModuleDirective( null, root );
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
            input.close();
        }
    }
    
   /**
    * Build a module using an XML element.
    * @param element the module element
    */
    private static ModuleDirective buildModuleDirective( File base, Element element ) throws IOException
    {
        final String elementName = element.getTagName();
        if( !MODULE_ELEMENT_NAME.equals( elementName ) )
        {
            final String error =
              "Element is not a module.";
            throw new IllegalArgumentException( error );
        }
        
        // get name, version and basedir, properties, dependent module links, local projects and 
        // resources, then build the nested modules
        
        final String name = ElementHelper.getAttribute( element, "name", null );
        if( null == name )
        {
            final String error = 
              "Module does not declare a name attribute.";
            throw new IllegalArgumentException( error );
        }
        
        final String path = getBaseDir( base );
        final String version = ElementHelper.getAttribute( element, "version", null );
        final String basedir = ElementHelper.getAttribute( element, "basedir", path );
        
        Properties properties = null;
        ModuleIncludeDirective[] includes = new ModuleIncludeDirective[0];
        ResourceDirective[] resources = new ResourceDirective[0];
        ProjectDirective[] projects = new ProjectDirective[0];
        ArrayList list = new ArrayList();
        
        Element[] children = ElementHelper.getChildren( element );
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            final String tag = child.getTagName();
            if( PROPERTIES_ELEMENT_NAME.equals( tag ) )
            {
                properties = buildProperties( element );
            }
            else if( MODULE_ELEMENT_NAME.equals( tag ) )
            {
                ModuleDirective directive = buildModuleDirective( null, child );
                list.add( directive );
            }
            else if( RESOURCES_ELEMENT_NAME.equals( tag ) ) 
            {
                resources = buildResourceDirectives( child );
            }
            else if( DEPENDENCIES_ELEMENT_NAME.equals( tag ) ) 
            {
                includes = buildModuleIncludeDirectives( child );
            }
            else if( PROJECTS_ELEMENT_NAME.equals( tag ) ) 
            {
                projects = buildProjectDirectives( child );
            }
            else
            {
                final String error = 
                  "Illegal element name [" + tag + "] within 'module' element.";
                throw new IllegalArgumentException( error );
            }
        }
        ModuleDirective[] modules = (ModuleDirective[]) list.toArray( new ModuleDirective[0] );
        return new ModuleDirective( 
          name, version, basedir, includes, modules, projects, resources, properties );
    }
    
    private static String getBaseDir( File file ) throws IOException
    {
        if( null == file )
        {
            return null;
        }
        else
        {
            return file.getCanonicalPath();
        }
    }
    
   /**
    * Build an array of include directives contained within the supplied enclosing element.
    * @param element the enclosing element
    * @return the array of includes
    */
    private static ModuleIncludeDirective[] buildModuleIncludeDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        ModuleIncludeDirective[] includes = new ModuleIncludeDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            includes[i] = buildModuleIncludeDirective( child );
        }
        return includes;
    }
    
   /**
    * Build an array of include directives contained within the supplied enclosing element.
    * @param element the enclosing element
    * @return the array of includes
    */
    private static ResourceIncludeDirective[] buildResourceIncludeDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        ResourceIncludeDirective[] includes = new ResourceIncludeDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            includes[i] = buildResourceIncludeDirective( child );
        }
        return includes;
    }
    
    private static ResourceDirective[] buildResourceDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        ResourceDirective[] resources = new ResourceDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            resources[i] = buildResourceDirective( child );
        }
        return resources;
    }
    
    private static ProjectDirective[] buildProjectDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        ProjectDirective[] projects = new ProjectDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            projects[i] = buildProjectDirective( child );
        }
        return projects;
    }
    
    private static ProjectDirective buildProjectDirective( Element element )
    {
        final String elementName = element.getTagName();
        if( !PROJECT_ELEMENT_NAME.equals( elementName ) )
        {
            final String error =
              "Element ["
              + elementName
              + "] is not a project.";
            throw new IllegalArgumentException( error );
        }
        
        // get name and basedir, dependent resources, and artifact production directives
        
        final String name = ElementHelper.getAttribute( element, "name", null );
        if( null == name )
        {
            final String error = 
              "Project does not declare a name attribute.";
            throw new IllegalArgumentException( error );
        }
        Properties properties = null;
        final String basedir = ElementHelper.getAttribute( element, "basedir", null );
        Element[] children = ElementHelper.getChildren( element );
        ArrayList list = new ArrayList();
        ProductionDirective[] artifacts = new ProductionDirective[0];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            final String tag = child.getTagName();
            if( PRODUCES_ELEMENT_NAME.equals( tag ) )
            {
                artifacts = buildProductionDirectives( child );
            }
            else if( PROPERTIES_ELEMENT_NAME.equals( tag ) )
            {
                properties = buildProperties( element );
            }
            else if( DEPENDENCIES_ELEMENT_NAME.equals( tag ) )
            {
                DependencyDirective dependency = buildDependencyDirective( child );
                list.add( dependency );
            }
            else
            {
                final String error = 
                  "Illegal element name [" 
                  + tag 
                  + "] within ["
                  + elementName 
                  + "]";
                throw new IllegalArgumentException( error );
            }
        }
        DependencyDirective[] dependencies = (DependencyDirective[]) list.toArray( new DependencyDirective[0] );
        return new ProjectDirective( name, basedir, artifacts, dependencies, properties );
    }
    
    private static ModuleIncludeDirective buildModuleIncludeDirective( Element element )
    {
        final String tag = element.getTagName();
        final Properties properties = buildProperties( element );
        if( INCLUDE_ELEMENT_NAME.equals( tag ) )
        {
            if( element.hasAttribute( "file" ) )
            {
                final String value = ElementHelper.getAttribute( element, "file", null );
                return new ModuleIncludeDirective( ModuleIncludeDirective.FILE, value, properties );
            }
            else if( element.hasAttribute( "uri" ) )
            {
                final String value = ElementHelper.getAttribute( element, "uri", null );
                return new ModuleIncludeDirective( ModuleIncludeDirective.URI, value, properties );
            }
            else
            {
                final String error = 
                  "Module include element does not declare a recognized attribute.";
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
    
    private static ResourceIncludeDirective buildResourceIncludeDirective( Element element )
    {
        final String tag = element.getTagName();
        final Properties properties = buildProperties( element );
        if( INCLUDE_ELEMENT_NAME.equals( tag ) )
        {
            if( element.hasAttribute( "key" ) )
            {
                final String value = ElementHelper.getAttribute( element, "key", null );
                return new ResourceIncludeDirective( ResourceIncludeDirective.KEY, value, properties );
            }
            else if( element.hasAttribute( "ref" ) )
            {
                final String value = ElementHelper.getAttribute( element, "ref", null );
                return new ResourceIncludeDirective( ResourceIncludeDirective.REF, value, properties );
            }
            else
            {
                final String error = 
                  "Resource include element does not declare a recognized attribute.";
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
        final String tag = element.getTagName();
        if( RESOURCE_ELEMENT_NAME.equals( tag ) )
        {
            final String name = ElementHelper.getAttribute( element, "name", null );
            final String version = ElementHelper.getAttribute( element, "version", null );
            TypeDirective[] types = new TypeDirective[0];
            ResourceIncludeDirective[] includes = new ResourceIncludeDirective[0];
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
                    includes = buildResourceIncludeDirectives( child );
                }
                else if( PROPERTIES_ELEMENT_NAME.equals( tag ) )
                {
                    properties = buildProperties( element );
                }
                else
                {
                    final String error = 
                      "Illegal element [" + childTag + "] within ["
                      + tag + "]";
                    throw new IllegalArgumentException( error );
                }
            }
            return new ResourceDirective( name, version, types, includes, properties );
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
            final Properties properties = buildProperties( element );
            return new TypeDirective( name, properties );
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
    
    private static DependencyDirective buildDependencyDirective( Element element )
    {
        final String tag = element.getTagName();
        if( DEPENDENCIES_ELEMENT_NAME.equals( tag ) )
        {
            final String spec = ElementHelper.getAttribute( element, "scope", "runtime" );
            Scope scope = Scope.parse( spec );
            final String anchor = ElementHelper.getAttribute( element, "anchor", null );
            Element[] children = ElementHelper.getChildren( element );
            ResourceIncludeDirective[] includes = new ResourceIncludeDirective[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                includes[i] = buildResourceIncludeDirective( child );
            }
            final Properties properties = buildProperties( element );
            return new DependencyDirective( scope, includes, anchor, properties );
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

    private static ProductionDirective[] buildProductionDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        ProductionDirective[] artifacts = new ProductionDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            artifacts[i] = buildProductionDirective( child );
        }
        return artifacts;
    }
    
    private static ProductionDirective buildProductionDirective( Element element )
    {
        final String tag = element.getTagName();
        if( TYPE_ELEMENT_NAME.equals( tag ) )
        {
            final String type = ElementHelper.getAttribute( element, "id", null );
            if( null == type )
            {
                final String error =
                  "Artifact element does not declare a type.";
                throw new IllegalArgumentException( error );
            }
            final Properties properties = buildProperties( element );
            return new ProductionDirective( type, properties );
        }
        else
        {
            final String error = 
              "Invalid artifact element name [" 
              + tag
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
    private static Properties buildProperties( Element element )
    {
        Element elem = ElementHelper.getChild( element, "properties" );
        if( null == elem )
        {
            return null;
        }
        else
        {
            Properties properties = new Properties();
            Element[] children = ElementHelper.getChildren( elem );
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
}
