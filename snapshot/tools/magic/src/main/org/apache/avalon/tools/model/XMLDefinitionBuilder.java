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

package org.apache.avalon.tools.model;

import org.apache.avalon.tools.model.Plugin.ListenerDef;
import org.apache.avalon.tools.model.Plugin.TaskDef;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Element;

import java.io.File;

/**
 * Definition of a project. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class XMLDefinitionBuilder 
{
    public static Resource createResource( final Home home, final Element element )
    {
        final Info info =
          createInfo( home, ElementHelper.getChild( element, "info" ) );
        final Gump gump =
          createGump( ElementHelper.getChild( element, "gump" ) );
        final String key = getDefinitionKey( element, info );

        final ResourceRef[] resources =
          createResourceRefs( 
            ElementHelper.getChild( element, "dependencies" ) );
        
        return new Resource( home, key, info, gump, resources );
    }

    public static Resource createResource( final Home home, final Element element, final File anchor )
    {
        final String tag = element.getTagName();

        if( tag.equals( "resource" ) )
          return createResource( home, element );
          
        //
        // otherwise its a project or plugin defintion
        // 

        final Info info =
          createInfo( home, ElementHelper.getChild( element, "info" ) );
        final Gump gump =
          createGump( ElementHelper.getChild( element, "gump" ) );
        final String key = getDefinitionKey( element, info );

        final String path = element.getAttribute( "basedir" );
        final File basedir = getBasedir( anchor, path );
       
        final ResourceRef[] resources =
          createResourceRefs( 
            ElementHelper.getChild( element, "dependencies" ) );
        
        final ResourceRef[] plugins =
          createPluginRefs( 
            ElementHelper.getChild( element, "plugins" ) );

        if( tag.equals( "project" ) )
        {
            return new Definition( 
              home, key, basedir, path, info, gump, resources, plugins );
        }
        else if( tag.equals( "plugin" ) )
        {
            final TaskDef[] tasks =
              getTaskDefs( ElementHelper.getChild( element, "tasks" ) );
            final ListenerDef[] listeners =
              getListenerDefs( 
                ElementHelper.getChild( element, "listeners" ) );
            return new Plugin( 
              home, key, basedir, path, info, gump, resources, plugins, 
              tasks, listeners );
        }
        else
        {
            final String error =
              "Unrecognized project type \"" + tag + "\".";
            throw new BuildException( error );
        }
    }

    public static ListenerDef[] getListenerDefs( final Element element )
    {
        final Element[] children = ElementHelper.getChildren( element, "listener" );
        final ListenerDef[] listeners = new ListenerDef[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            final Element child = children[i];
            final ListenerDef listener = new ListenerDef( child.getAttribute( "class" ) );
            listeners[i] = listener;
        }
        return listeners;
    }

    public static TaskDef[] getTaskDefs( final Element element )
    {
        final Element[] children = ElementHelper.getChildren( element, "taskdef" );
        final TaskDef[] refs = new TaskDef[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            final Element child = children[i];
            final String name = child.getAttribute( "name" );
            final String classname = child.getAttribute( "class" );
            refs[i] = new TaskDef( name, classname );
        }
        return refs;
    }

    private static File getBasedir( final File anchor, final String path )
    {
        if( null == path )
        {
            final String error = 
              "Missing 'basedir' attribute.";
            throw new BuildException( error );
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

    private static String getDefinitionKey( final Element element, final Info info )
    {
        final String key = element.getAttribute( "key" );
        if( null == key ) 
        {
            return info.getName();
        }
        if( key.equals( "" ) )
        {
            return info.getName();
        }
        return key;
    }

    public static Info createInfo( Home home, final Element info )
    {
        final String group =
          ElementHelper.getValue( 
            ElementHelper.getChild( info, "group" ) );
        final String name =
          ElementHelper.getValue( 
            ElementHelper.getChild( info, "name" ) );
        final String type =
          ElementHelper.getValue( 
            ElementHelper.getChild( info, "type" ) );
        final String version =
          ElementHelper.getValue( 
            ElementHelper.getChild( info, "version" ) );
        final boolean status = createSnapshotPolicy( info );
        return Info.create( home, group, name, version, type, status );
    }

    private static boolean createSnapshotPolicy( final Element info )
    {
        final String status =
          ElementHelper.getValue( 
            ElementHelper.getChild( info, "status" ) );
        return Info.SNAPSHOT.equalsIgnoreCase( status );
    }

    public static Gump createGump( final Element info )
    {
        final String alias =
          ElementHelper.getValue( 
            ElementHelper.getChild( info, "alias" ) );
        final String id =
          ElementHelper.getValue( 
            ElementHelper.getChild( info, "id" ) );
        final boolean classpath = (null != ElementHelper.getChild( info, "classpath" ));
        final boolean ignore = (null != ElementHelper.getChild( info, "ignore" ));
        return new Gump( alias, id, classpath, ignore );
    }

    private static ResourceRef[] createResourceRefs( final Element element )
      throws BuildException
    {
        final Element[] children = ElementHelper.getChildren( element, "include" );
        final ResourceRef[] refs = new ResourceRef[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            final Element child = children[i];
            final String key = child.getAttribute( "key" );
            final int tag = ResourceRef.getCategory( child.getAttribute( "tag" ) );
            final Policy policy = createPolicy( child );
            refs[i] = new ResourceRef( key, policy, tag );
        }
        return refs;
    }

    private static ResourceRef[] createPluginRefs( final Element element )
      throws BuildException
    {
        final Element[] children = ElementHelper.getChildren( element, "include" );
        final ResourceRef[] refs = new ResourceRef[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            final Element child = children[i];
            final String key = child.getAttribute( "key" );
            final int tag = ResourceRef.getCategory( child.getAttribute( "tag" ) );
            final Policy policy = createPolicy( child, false, false );
            refs[i] = new ResourceRef( key, policy, tag );
        }
        return refs;
    }

    private static Policy createPolicy( final Element element )
    {
        return createPolicy( element, true, true );
    }

    private static Policy createPolicy( 
      final Element element, final boolean defBuild, final boolean defTest )
    {
        final boolean build =
          ElementHelper.getBooleanAttribute( element, "build", defBuild );
        final boolean test =
          ElementHelper.getBooleanAttribute( element, "test", defTest );
        final boolean runtime =
          ElementHelper.getBooleanAttribute( element, "runtime", build );
        return new Policy( build, test, runtime );
    }
}
