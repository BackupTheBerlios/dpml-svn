/*
 * Copyright 2004 Apache Software Foundation
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

package net.dpml.magic.builder;

import java.io.File;

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Info;
import net.dpml.magic.model.Policy;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.model.Type;

import net.dpml.transit.Plugin;
import net.dpml.transit.Category;
import net.dpml.transit.util.ElementHelper;

import org.apache.tools.ant.BuildException;

import org.w3c.dom.Element;

/**
 * Utility class that supports construction of project and resource datastructures.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public final class XMLDefinitionBuilder
{
    private XMLDefinitionBuilder()
    {
        // static utility class
    }

   /**
    * Create a resource.
    * @param home the project index
    * @param element the DOM element definition
    * @param uri the module uri
    * @return the resource
    */
    public static Resource createResource( final AntFileIndex home, final Element element, String uri )
    {
        boolean external = isExternal( element );
        final Element infoElement = ElementHelper.getChild( element, "info" );
        final Info info = createInfo( home, infoElement, external, uri );
        final String key = getDefinitionKey( element, info );
        final Element dependenciesElement = ElementHelper.getChild( element, "dependencies" );
        final ResourceRef[] resources = createResourceRefs( dependenciesElement );
        return new Resource( home, key, info, resources, uri );
    }

   /**
    * Return TRUE is the element is an external resource.
    * @param element the DOM element
    * @return TRUE is the element is a resource
    */
    public static boolean isExternal( Element element )
    {
        return element.getTagName().equals( "resource" );
    }

   /**
    * Construct a resource from an element.
    * @param home the index
    * @param element the dom element
    * @param anchor the file anchor from which relative references will be resolved
    * @param uri the module uri
    * @return the resource
    */
    public static Resource createResource( AntFileIndex home, Element element, File anchor, String uri )
    {
        final String tag = element.getTagName();

        if( tag.equals( "resource" )  )
        {
            return createResource( home, element, uri );
        }
        else if( tag.equals( "project" ) || tag.equals( "index" ) )
        {
            //
            // its a project or module defintion
            //

            boolean external = isExternal( element );

            Element infoElement = ElementHelper.getChild( element, "info" );
            final Info info = createInfo( home, infoElement, external, uri );
            final String key = getDefinitionKey( element, info );

            final String path = ElementHelper.getAttribute( element, "basedir" );
            final File basedir = getBasedir( anchor, path );
            final String file = getBuildFile( element );

            final Element dependenciesElement = ElementHelper.getChild( element, "dependencies" );
            final ResourceRef[] resources = createResourceRefs( dependenciesElement );

            final Element pluginsElement = ElementHelper.getChild( element, "plugins" );
            final ResourceRef[] plugins = createPluginRefs( pluginsElement );

            return new Definition( home, key, basedir, file, path, info, resources, plugins, uri );
        }
        else
        {
            final String error =
              "Unrecognized element name \""
              + tag
              + "\" (recognized types include 'resource', 'project', 'index', and 'import').";
            throw new BuildException( error );
        }
    }

    private static File getBasedir( final File anchor, final String path )
    {
        if( null == path )
        {
            return anchor;
        }

        final File basedir = new File( anchor, path );
        if( !basedir.exists() )
        {
            final String error =
              "Declared basedir [" + basedir + "] does not exist.";
            throw new BuildException( error );
        }
        if( !basedir.isDirectory() )
        {
            final String error =
              "Declared basedir [" + basedir + "] is not a directory.";
            throw new BuildException( error );
        }
        return basedir;
    }

    private static String getBuildFile( Element element )
    {
        return ElementHelper.getAttribute( element, "file" );
    }

    private static String getDefinitionKey( final Element element, final Info info )
    {
        final String key = ElementHelper.getAttribute( element, "key" );
        if( null == key )
        {
            return info.getName();
        }
        else
        {
            return key;
        }
    }

   /**
    * Creation of a info descriptor.
    * @param home the index
    * @param info the dom element
    * @param external TRUE if this is an external element
    * @param uri the module uri
    * @return the info descriptor
    */
    public static Info createInfo( AntFileIndex home, final Element info, boolean external, String uri )
    {
        final Element nameElement = ElementHelper.getChild( info, "name" );
        final String name = ElementHelper.getValue( nameElement );
        final Element groupElement = ElementHelper.getChild( info, "group" );
        String group = ElementHelper.getValue( groupElement );
        if( null == group && !external )
        {
            group = getDefaultGroup( home, name, uri );
        }
        final Type[] types = createTypes( info );
        String version = null;
        if( external )
        {
            version = createExternalVersion( info );
        }
        else
        {
            String defaultVersion = getDefaultVersion( home, name, uri );
            version = createVersion( info, defaultVersion );
        } 
        return Info.create( group, name, version, types );
    }

   /**
    * Construct a default version value using the enclosing module uri reference.  The 
    * enclosing uri is in the form "key:[key]" where the value of [key] is the identifier of 
    * the project that defines the module.  If the uri value is null the implementation
    * ruturns the value of the property 'dpml.release.signature' or SNAPSHOT if undefined.
    * If not null, the version of the enclosing module is returned.
    * 
    * @param index the working index
    * @param name the name of the project we are evaluating
    * @param uri the uri pointer to the enclosing module project
    * @return the default version value
    */
    private static String getDefaultVersion( AntFileIndex index, String name, String uri )
    {
        if( null == uri )
        {
            return index.getReleaseSignature();
        }

        if( uri.startsWith( "key:" ) )
        {
            final String key = uri.substring( 4 );
            if( key.equals( name ) )
            {
                return index.getReleaseSignature();
            }
            else if( key.equals( "UNKNOWN" ) )
            {
                return index.getReleaseSignature();
            }
            else
            {
                Resource resource = index.getResource( key );
                return resource.getInfo().getVersion();
            }
        }
        else
        {
            final String error = 
              "The URI value declared as the enclosing module value does not declare a 'key:' prefix.";
            throw new BuildException( error ); 
        }
    }

   /**
    * Construct a default group value using the enclosing module uri reference.  The 
    * enclosing uri is in the form "key:[key]" where the value of [key] is the identifier of 
    * the project that defines the module.  If the uri value is null the implementation
    * ruturns null otherwise the implementation returns the enclosing module group id.
    * 
    * @param index the working index
    * @param name the name of the project we are evaluating
    * @param uri the uri pointer to the enclosing module project
    * @return the default group id
    */
    private static String getDefaultGroup( AntFileIndex index, String name, String uri )
    {
        if( null == uri )
        {
            return null;
        }

        if( uri.startsWith( "key:" ) )
        {
            final String key = uri.substring( 4 );
            if( key.equals( name ) )
            {
                return null;
            }
            else if( key.equals( "UNKNOWN" ) )
            {
                return null;
            }
            else
            {
                Resource resource = index.getResource( key );
                return resource.getInfo().getGroup();
            }
        }
        else
        {
            final String error = 
              "The URI value declared as the enclosing module value does not declare a 'key:' prefix.";
            throw new BuildException( error ); 
        }
    }

    private static String createExternalVersion( Element info )
    {
        final Element versionElement = ElementHelper.getChild( info, "version" );
        return ElementHelper.getValue( versionElement );
    }

    private static String createVersion( Element info, String defaultVersion )
    {
        final Element versionElement = ElementHelper.getChild( info, "version" );
        if( null == versionElement )
        {
            return defaultVersion;
        }
        else
        {
            return ElementHelper.getValue( versionElement );
        }
    }

    private static Type[] createTypes( Element info )
    {
        Element types = ElementHelper.getChild( info, "types" );
        if( null != types )
        {
            Element[] children = ElementHelper.getChildren( types, "type" );
            if( children.length == 0 )
            {
                return new Type[]{new Type()};
            }
            else
            {
                Type[] values = new Type[ children.length ];
                for( int i=0; i < children.length; i++ )
                {
                    Element child = children[i];
                    Type type = createType( child );
                    values[i] = type;
                }
                return values;
            }
        }
        else
        {
            Element typeElement = ElementHelper.getChild( info, "type" );
            final Type type = createType( typeElement );
            return new Type[]{type};
        }
    }

    private static Type createType( final Element element )
    {
        if( null == element )
        {
            return new Type();
        }
        String type = null;
        if( element.hasAttribute( "name" ) )
        {
            type = ElementHelper.getAttribute( element, "name" );
        }
        else
        {
            type = ElementHelper.getValue( element );
        }
        String alias = ElementHelper.getAttribute( element, "alias" );
        return new Type( type, alias );
    }

    private static ResourceRef[] createResourceRefs( final Element element )
      throws BuildException
    {
        final Element[] children = ElementHelper.getChildren( element, "include" );
        final ResourceRef[] refs = new ResourceRef[ children.length ];
        for( int i=0; i < children.length; i++ )
        {
            final Element child = children[i];
            final String key = ElementHelper.getAttribute( child, "key" );
            final String tagAttribute = ElementHelper.getAttribute( child, "tag" );
            final Category tag = resolveCategory( tagAttribute );
            final Policy policy = createPolicy( child );
            refs[i] = new ResourceRef( key, policy, tag );
        }
        return refs;
    }

    private static Category resolveCategory( String category )
    {
        if( ( null == category ) )
        {
            return Category.PRIVATE;
        }
        else
        {
            return ResourceRef.getCategory( category );
        }
    }

    private static ResourceRef[] createPluginRefs( final Element element )
      throws BuildException
    {
        final Element[] children = ElementHelper.getChildren( element, "include" );
        final ResourceRef[] refs = new ResourceRef[ children.length ];
        for( int i=0; i < children.length; i++ )
        {
            final Element child = children[i];
            final String key = ElementHelper.getAttribute( child, "key" );
            final String tagAttribute = ElementHelper.getAttribute( child, "tag" );
            final Category tag = resolveCategory( tagAttribute );
            final Policy policy = createPolicy( child, false );
            refs[i] = new ResourceRef( key, policy, tag );
        }
        return refs;
    }

    private static Policy createPolicy( final Element element )
    {
        return createPolicy( element, true );
    }

    private static Policy createPolicy( final Element element, final boolean defBuild )
    {
        boolean build = ElementHelper.getBooleanAttribute( element, "build", defBuild );
        boolean test = ElementHelper.getBooleanAttribute( element, "test", build );
        boolean runtime = ElementHelper.getBooleanAttribute( element, "runtime", test );
        return new Policy( build, test, runtime );
    }
}
