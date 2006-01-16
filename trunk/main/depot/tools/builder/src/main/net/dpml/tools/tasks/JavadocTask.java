/*
 * Copyright 2004-2005 Stephen McConnell
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

package net.dpml.tools.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.dpml.library.info.Scope;
import net.dpml.library.model.Resource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javadoc;
import org.apache.tools.ant.taskdefs.Javadoc.AccessType;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;

/**
 * Build the javadoc for a project.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class JavadocTask extends GenericTask
{
    //-----------------------------------------------------------------------
    // static
    //-----------------------------------------------------------------------

    private static final String JAVADOC_TASK_NAME = "javadoc";
    
   /**
    * Property key for declaration of an overriding access level.
    */
    public static final String JAVADOC_ACCESS_KEY = "project.javadoc.access";

   /**
    * Property key for declaration of the lionksource option.
    */
    public static final String JAVADOC_LINK_SOURCE_KEY = "project.javadoc.linksource";

    //-----------------------------------------------------------------------
    // state
    //-----------------------------------------------------------------------

    private String m_title;
    private List m_links = new ArrayList();
    private List m_groups = new ArrayList();
    private File m_overview;
    private AccessType m_access;

    //-----------------------------------------------------------------------
    // JavadocTask
    //-----------------------------------------------------------------------

   /**
    * Task initialization.
    */
    public void init()
    {
        if( !isInitialized() )
        {
            super.init();
            getContext().getPath( Scope.RUNTIME );
        }
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
    
   /**
    * Set the documentation access level.
    * @param access the access level
    */
    public void setAccess( AccessType access )
    {
        m_access = access;
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
        final Resource resource = getResource();

        log( "Generating javadoc for project: " + resource, Project.MSG_VERBOSE );

        File api = getContext().getTargetReportsJavadocDirectory();
        mkDir( api );

        //
        // construct the classpath by getting all of the classpaths for
        // all of the projects within the module
        //

        Resource[] providers = resource.getClasspathProviders( Scope.RUNTIME );
        final Path classpath = getContext().createPath( providers, true, true );
        process( resource, classpath, api );
    }

    //-----------------------------------------------------------------------
    // internal
    //-----------------------------------------------------------------------

    private Group[] getGroups()
    {
        return (Group[]) m_groups.toArray( new Group[0] );
    }

    private void process(
       final Resource resource, final Path classpath, final File root )
    {
        log( "Preparing javadoc for output to: " + root );

        final Javadoc javadoc = (Javadoc) getProject().createTask( JAVADOC_TASK_NAME );
        javadoc.setTaskName( getTaskName() );
        javadoc.init();
        javadoc.setDestdir( root );
        javadoc.setUse( true );
        javadoc.createClasspath().add( classpath );
        
        final Path source = javadoc.createSourcepath();
        addSourcePath( resource, javadoc, source );
        
        if( null == m_access )
        {
            String access = getContext().getProperty( JAVADOC_ACCESS_KEY, "protected" );
            AccessType type = new AccessType();
            type.setValue( access );
            javadoc.setAccess( type );
        }
        else
        {
            javadoc.setAccess( m_access );
        }
        
        if( "true".equals( getContext().getProperty( JAVADOC_LINK_SOURCE_KEY, "false" ) ) )
        {
            javadoc.setLinksource( true );
        }
        
        aggregateLinks( javadoc, resource );
        Link[] links = (Link[]) m_links.toArray( new Link[0] );
        for( int i=0; i<links.length; i++ )
        {
            Link link = links[i];
            Javadoc.LinkArgument arg = javadoc.createLink();
            String href = link.getHref();
            log( "Adding link: " + href );
            arg.setHref( href );
        }
        
        if( null == m_title )
        {
            final String title = resource.getName() + " API";
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

        log( "Generating: " + resource.getName() + " API" );
        javadoc.execute();
    }
    
    private void aggregateLinks( Javadoc task, Resource resource )
    {
        String path = resource.getResourcePath();
        ArrayList list = new ArrayList();
        Resource[] providers = resource.getAggregatedProviders( Scope.RUNTIME, true, false );
        for( int i=0; i<providers.length; i++ )
        {
            Resource provider = providers[i];
            if( !provider.getResourcePath().startsWith( path ) )
            {
                String source = provider.getProperty( "project.api.root" );
                if( null != source )
                {
                    log( "Adding provider link: " + source );
                    Javadoc.LinkArgument arg = task.createLink();
                    arg.setHref( source );
                }
            }
        }
    }

    private void addSourcePath( Resource resource, Javadoc javadoc, Path source )
    {
        //
        // check for a classic src directory
        //

        if( null != resource.getBaseDir() )
        {
            File src = new File( resource.getBaseDir(), "target/build/main" );
            if( src.exists() )
            {
                log( "Adding src path: " + src );
                source.createPathElement().setLocation( src );
                final DirSet packages = new DirSet();
                packages.setDir( src );
                packages.setIncludes( "**/*" );
                javadoc.addPackageset( packages );
            }
        }
        
        String path = resource.getResourcePath();
        Resource[] providers = resource.getAggregatedProviders( Scope.RUNTIME, true, false );
        for( int i=0; i<providers.length; i++ )
        {
            Resource provider = providers[i];
            if( provider.getResourcePath().startsWith( path ) )
            {
                File basedir = provider.getBaseDir();
                if( null != basedir )
                {
                    File src = new File( basedir, "target/build/main" );
                    if( src.exists() )
                    {
                        log( "Adding src path: " + src );
                        source.createPathElement().setLocation( src );
                        final DirSet packages = new DirSet();
                        packages.setDir( src );
                        packages.setIncludes( "**/**" );
                        javadoc.addPackageset( packages );
                    }
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    // static classes
    //-----------------------------------------------------------------------

   /**
    * Delcaration of a link.
    */
    public class Link
    {
        private String m_href;

       /**
        * Creation of a new link instance.
        */
        public Link()
        {
            m_href = null;
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
        * Get the href attribute.
        * @return the href attriute value
        */
        public String getHref()
        {
            if( null != m_href )
            {
                return m_href;
            }
            else
            {
                final String error =
                      "Link element must contain an 'href' attribute.";
                throw new BuildException( error, getLocation() );
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
