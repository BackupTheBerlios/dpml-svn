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

package net.dpml.magic.tasks;

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.builder.ElementHelper;
import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Policy;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.transit.artifact.Handler;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javadoc;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Element;

import net.dpml.transit.Plugin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Build the javadoc for a project.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class JavadocTask extends ProjectTask
{
    //-----------------------------------------------------------------------
    // static
    //-----------------------------------------------------------------------

    private static final String TARGET_BUILD_MAIN = "target/build/main";
    private static final String JAVADOC_TASK_NAME = "javadoc";

    //-----------------------------------------------------------------------
    // state
    //-----------------------------------------------------------------------

    private String m_id;
    private String m_title;
    private List m_links = new ArrayList();
    private List m_groups = new ArrayList();
    private File m_overview;

    //-----------------------------------------------------------------------
    // ModuleDocTask
    //-----------------------------------------------------------------------

   /**
    * Set the target project defintion identifier.
    * @param id the defintion id
    */
    public void setId( final String id )
    {
        m_id = id;
    }

   /**
    * Set the javadoc title.
    * @param title the title
    */
    public void setTitle( final String title )
    {
        m_title = title;
    }

   /**
    * Set the javadoc overview file.
    * @param overview the overview file
    */
    public void setOverview( final File overview )
    {
        m_overview = overview;
    }

   /**
    * Create and add a new link to the javadoc run.
    * @return the new link
    */
    public Link createLink()
    {
        final Link link = new Link();
        m_links.add( link );
        return link;
    }

   /**
    * Create and add a new group to the javadoc defintion.
    * @return the new group
    */
    public Group createGroup()
    {
        final Group group = new Group();
        m_groups.add( group );
        return group;
    }

    //-----------------------------------------------------------------------
    // Task
    //-----------------------------------------------------------------------

   /**
    * Task execution.
    * @exception BuildException if a build error occurs
    */
    public void execute() throws BuildException
    {
        final Definition def = getReferenceDefinition();

        log( "Generating javadoc for project: " + def, Project.MSG_VERBOSE );

        File api = new File( getContext().getTargetDirectory(), "api" );
        mkDir( api );

        //
        // construct the classpath by getting all of the classpaths for
        // all of the projects within the module
        //

        final Path classpath = def.getClassPath( getProject() );
        process( def, classpath, api );

        File docs = getIndex().getDocsDirectory();
        File destination = new File( docs, def.getInfo().getJavadocPath() );
        install( api, destination );
    }

    //-----------------------------------------------------------------------
    // internal
    //-----------------------------------------------------------------------

    private Group[] getGroups()
    {
        return (Group[]) m_groups.toArray( new Group[0] );
    }

    private void install( final File source, final File destination )
    {
        if( source.exists() )
        {
            log( "Installing api", Project.MSG_VERBOSE );
            final FileSet fileset = new FileSet();
            fileset.setDir( source );
            copy( destination, fileset, true );
        }
    }

    private void process(
       final Definition definition, final Path classpath, final File root )
    {
        log( "Preparing javadoc for output to: " + root );

        final Javadoc javadoc = (Javadoc) getProject().createTask( JAVADOC_TASK_NAME );
        javadoc.setTaskName( getTaskName() );
        javadoc.init();
        javadoc.setDestdir( root );
        javadoc.setUse( true );
        final Path source = javadoc.createSourcepath();

        addDefinitionSource( definition, javadoc, source );

        javadoc.createClasspath().add( classpath );

        Resource[] modules = getReferencedModules( definition );
        for( int i=0; i < modules.length; i++ )
        {
            Resource module = modules[i];
            Link link = getModuleLink( definition, module );
            addLink( definition, javadoc, link );
        }

        for( int i=0; i < m_links.size(); i++ )
        {
            final Link link = (Link) m_links.get( i );
            addLink( definition, javadoc, link );
        }

        if( null == m_title )
        {
            final String title = definition.getInfo().getName() + " API";
            javadoc.setDoctitle( title );
        }
        else
        {
            javadoc.setDoctitle( m_title );
        }

        if( null != m_overview )
        {
            javadoc.setOverview( m_overview );
        }

        Group[] groups = getGroups();
        for( int i=0; i < groups.length; i++ )
        {
            Group group = groups[i];
            Javadoc.GroupArgument jgroup = javadoc.createGroup();
            jgroup.setTitle( group.getTitle() );
            Group.Package[] packages = group.getPackages();
            for( int j=0; j < packages.length; j++ )
            {
                Javadoc.PackageName name = new Javadoc.PackageName();
                name.setName( packages[j].getName() );
                jgroup.addPackage( name );
            }
        }

        log( "Generating: " + definition.getInfo().getName() + " API" );
        javadoc.execute();
    }

    private void addDefinitionSource( Definition definition, Javadoc javadoc, Path source )
    {
        //
        // check for a classic src directory
        //

        final File base = definition.getBaseDir();
        final File src = new File( base, TARGET_BUILD_MAIN );
        if( src.exists() )
        {
            log( "Adding src path: " + src );
            source.createPathElement().setLocation( src );
            final DirSet packages = new DirSet();
            packages.setDir( src );
            packages.setIncludes( "**/**" );
            javadoc.addPackageset( packages );
        }
        else
        {
            log( "Skipping nonexistant src path: " + src, Project.MSG_VERBOSE );
        }

        //
        // if its a module then iterate over the projects within the scope
        // of the module and edd each source path
        //

        if( definition.getInfo().isa( "module" ) )
        {
            Definition[] defs = getIndex().getSubsidiaryDefinitions( definition );
            for( int i=0; i < defs.length; i++ )
            {
                Definition d = defs[i];
                addDefinitionSource( d, javadoc, source );
            }
        }
    }

    private void addLink( Definition definition, Javadoc javadoc, Link link )
    {
        Javadoc.LinkArgument arg = javadoc.createLink();
        String href = link.getHref( getIndex(), definition );
        arg.setHref( href );
        final File dir = link.getDir( getIndex() );
        if( null != dir )
        {
            if( dir.exists() )
            {
                log( "link: " + href + " (offline)" );
                arg.setOffline( true );
                arg.setPackagelistLoc( dir );
            }
        }
        else
        {
            log( "link: " + href + " [" + dir + "]" );
        }
    }

    private Link getModuleLink( Definition def, Resource resource )
    {
        if( !resource.getInfo().isa( "module" ) )
        {
            final String error =
              "Cannot construct a module link form a non-module resource: "
              + resource;
            throw new BuildException( error, def.getLocation() );
        }

        String uri = resource.getInfo().getURI( "module" );
        try
        {
            URL url = new URL( null, uri, new Handler() );
            InputStream input = url.openStream();
            Element root = ElementHelper.getRootElement( input );
            Element header = ElementHelper.getChild( root, "header" );
            Element docs = ElementHelper.getChild( header, "docs" );
            String host = ElementHelper.getAttribute( docs, "href" );
            return new Link( resource.getKey(), host );
        }
        catch( Throwable e )
        {
            final String error =
              "Unable to construct module link from resource: "
              + resource;
            throw new BuildException( error, def.getLocation() );
        }
    }

    private Resource[] getReferencedModules( Definition definition )
    {
        ResourceRef[] refs =
          definition.getResourceRefs( getProject(), Policy.ANY, Plugin.ANY, true );
        ArrayList list = new ArrayList();
        for( int i=0; i < refs.length; i++ )
        {
            Resource resource = getIndex().getResource( refs[i] );
            if( resource.getInfo().isa( "module" ) )
            {
                list.add( resource );
            }
        }
        return (Resource[]) list.toArray( new Resource[0] );
    }

    private Definition getReferenceDefinition()
    {
        if( null != m_id )
        {
            return getIndex().getDefinition( m_id );
        }
        else
        {
            return getIndex().getDefinition( getContext().getKey() );
        }
    }

    //-----------------------------------------------------------------------
    // static classes
    //-----------------------------------------------------------------------

   /**
    * Delcaration of a link.
    */
    public static class Link
    {
        private String m_href;
        private String m_host;
        private File m_dir;
        private String m_key;

       /**
        * Creation of a new link instance.
        */
        public Link()
        {
            m_href = null;
        }

       /**
        * Creation of a new link instance.
        * @param key the defintion key
        * @param host the remote host containing the references apis
        */
        public Link( final String key, final String host )
        {
            m_key = key;
            m_host = host;
        }

       /**
        * Set the href attrbute for the api packagelist base.
        * @param href the href to the packacke list base 
        */
        public void setHref( final String href )
        {
            m_href = href;
        }

       /**
        * Get the href attrbute.
        * @param index the magic index
        * @param def the project definition
        * @return the href attriute value
        */
        public String getHref( AntFileIndex index, Definition def )
        {
            if( null == m_key )
            {
                if( null != m_href )
                {
                    return m_href;
                }
                else
                {
                    final String error =
                      "Link element must contain either a 'key' and 'host' attribute or "
                      + "a 'href' attribute.";
                    throw new BuildException( error, def.getLocation() );
                }
            }
            else
            {
                if( null == m_host )
                {
                    final String error =
                      "The key attribute must be defined in conjunction with a remote host attrihbute.";
                    throw new BuildException( error, def.getLocation() );
                }
                final Resource resource = index.getResource( m_key );
                return m_host + resource.getInfo().getJavadocPath();
            }
        }

       /**
        * Set the project key.
        * @param key the key
        */
        public void setKey( final String key )
        {
            m_key = key;
        }

       /**
        * Set the link host.
        * @param host the host
        */
        public void setHost( final String host )
        {
            m_host = host;
        }

       /**
        * Set the local offline directory.
        * @param dir the directory to the local offline content
        */
        public void setDir( final File dir )
        {
            m_dir = dir;
        }

       /**
        * Return the local offline directory.
        * @param index the mgic index
        * @return the directory
        */
        public File getDir( AntFileIndex index )
        {
            if( null == m_key )
            {
                return m_dir;
            }
            else
            {
                final Resource resource = index.getResource( m_key );
                final File cache = index.getDocsDirectory();
                final String path = resource.getInfo().getJavadocPath();
                return new File( cache, path );
            }
        }

       /**
        * Return the link as a string.
        * @return the string
        */
        public String toString()
        {
            if( null == m_dir )
            {
                if( null == m_key )
                {
                    return "link: " + m_href;
                }
                else
                {
                    return "link: " + m_host + " from [" + m_key + "]";
                }
            }
            else
            {
                return "link: " + m_href + " at " + m_dir;
            }
        }
    }

   /**
    * Defintion of a package group.
    */
    public static class Group
    {
        private String m_title;
        private ArrayList m_packages = new ArrayList();

       /**
        * Set the package group title.
        * @param title the package group title
        */
        public void setTitle( String title )
        {
            m_title = title;
        }

       /**
        * Return the package group title.
        * @return the title
        */
        public String getTitle()
        {
            if( null == m_title )
            {
                final String error =
                  "Required 'title' attribute on group element undefined.";
                throw new BuildException( error );
            }
            else
            {
                return m_title;
            }
        }

       /**
        * Return the packages within the group.
        * @return the array of packages
        */
        public Group.Package[] getPackages()
        {
            return (Group.Package[]) m_packages.toArray( new Group.Package[0] );
        }

       /**
        * Create and add a new package.
        * @return the new package
        */
        public Group.Package createPackage()
        {
            final Package pkg = new Package();
            m_packages.add( pkg );
            return pkg;
        }

       /**
        * Defintionof a package.
        */
        public static class Package
        {
            private String m_name;

           /**
            * Set the package name.
            * @param name the package name
            */
            public void setName( String name )
            {
                m_name = name;
            }

           /**
            * Return the package name.
            * @return the name 
            */
            public String getName()
            {
                if( null == m_name )
                {
                    final String error =
                      "Required 'name' attribute on package element undefined.";
                    throw new BuildException( error );
                }
                else
                {
                    return m_name;
                }
            }
        }
    }
}
