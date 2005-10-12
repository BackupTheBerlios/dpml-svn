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

import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.ProjectDirective;
import net.dpml.tools.info.TypeDirective;
import net.dpml.tools.info.ArtifactDirective;
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
    private static final String PRODUCTION_ELEMENT_NAME = "production";
    private static final String ARTIFACT_ELEMENT_NAME = "artifact";
    
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
        
        // get name, version and basedir, dependent module links, local projects and 
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
        
        IncludeDirective[] includes = new IncludeDirective[0];
        ResourceDirective[] resources = new ResourceDirective[0];
        ProjectDirective[] projects = new ProjectDirective[0];
        ArrayList list = new ArrayList();
        
        Element[] children = ElementHelper.getChildren( element );
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            final String tag = child.getTagName();
            if( MODULE_ELEMENT_NAME.equals( tag ) )
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
                includes = buildIncludeDirectives( child );
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
        return new ModuleDirective( name, version, basedir, includes, modules, projects, resources );
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
        final String basedir = ElementHelper.getAttribute( element, "basedir", null );
        Element[] children = ElementHelper.getChildren( element );
        ArrayList list = new ArrayList();
        ArtifactDirective[] artifacts = new ArtifactDirective[0];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            final String tag = child.getTagName();
            if( PRODUCTION_ELEMENT_NAME.equals( tag ) )
            {
                artifacts = buildArtifactDirectives( child );
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
        return new ProjectDirective( name, basedir, artifacts, dependencies );
    }
    
    private static IncludeDirective buildIncludeDirective( Element element )
    {
        final String tag = element.getTagName();
        if( INCLUDE_ELEMENT_NAME.equals( tag ) )
        {
            if( element.hasAttribute( "file" ) )
            {
                final String value = ElementHelper.getAttribute( element, "file", null );
                return new IncludeDirective( "file", value );
            }
            else if( element.hasAttribute( "uri" ) )
            {
                final String value = ElementHelper.getAttribute( element, "uri", null );
                return new IncludeDirective( "uri", value );
            }
            else if( element.hasAttribute( "key" ) )
            {
                final String value = ElementHelper.getAttribute( element, "key", null );
                return new IncludeDirective( "key", value );
            }
            else if( element.hasAttribute( "ref" ) )
            {
                final String value = ElementHelper.getAttribute( element, "ref", null );
                return new IncludeDirective( "ref", value );
            }
            else
            {
                final String error = 
                  "Include element does not declare a recognized attribute.";
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
            IncludeDirective[] includes = new IncludeDirective[0];
            Element[] children = ElementHelper.getChildren( element );
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
                    includes = buildIncludeDirectives( child );
                }
                else
                {
                    final String error = 
                      "Illegal element [" + childTag + "] within ["
                      + tag + "]";
                    throw new IllegalArgumentException( error );
                }
            }
            return new ResourceDirective( name, version, types, includes );
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
            final String name = ElementHelper.getAttribute( element, "name", null );
            return new TypeDirective( name );
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

    private static ArtifactDirective[] buildArtifactDirectives( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        ArtifactDirective[] artifacts = new ArtifactDirective[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            artifacts[i] = buildArtifactDirective( child );
        }
        return artifacts;
    }
    
    private static ArtifactDirective buildArtifactDirective( Element element )
    {
        final String tag = element.getTagName();
        if( ARTIFACT_ELEMENT_NAME.equals( tag ) )
        {
            final String type = ElementHelper.getAttribute( element, "type", null );
            if( null == type )
            {
                final String error =
                  "Artifact element does not declare a type.";
                throw new IllegalArgumentException( error );
            }
            return new ArtifactDirective( type );
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
}
