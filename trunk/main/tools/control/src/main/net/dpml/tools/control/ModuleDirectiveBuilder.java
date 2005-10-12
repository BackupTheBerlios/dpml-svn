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

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.ProjectDirective;
import net.dpml.tools.info.TypeDirective;

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
    
    public static ModuleDirective build( InputStream input ) throws IOException
    {
        try
        {
            final Element root = ElementHelper.getRootElement( input );
            return buildModuleDirective( root );
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error during module construction.";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }
    
   /**
    * Build a module using an XML element.
    * @param element the module element
    */
    private static ModuleDirective buildModuleDirective( Element element )
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
        
        final String version = ElementHelper.getAttribute( element, "version", null );
        final String basedir = ElementHelper.getAttribute( element, "basedir", null );
        
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
                ModuleDirective directive = buildModuleDirective( child );
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
        return new ProjectDirective[0];
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
}
