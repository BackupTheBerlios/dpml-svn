/*
 * Copyright 2004-2007 Stephen McConnell
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

package dpml.tools.tasks;

import dpml.build.BuildError;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dpml.library.Scope;
import dpml.library.Resource;

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
public class JavadocTask extends ResourceTask
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
    * Property key for declaration of the linksource option.
    */
    public static final String JAVADOC_LINK_SOURCE_KEY = "project.javadoc.linksource";

   /**
    * Property key for declaration of excluded packages.
    */
    public static final String JAVADOC_EXCLUDES = "project.javadoc.excludes";

    //-----------------------------------------------------------------------
    // state
    //-----------------------------------------------------------------------

    private String m_title;
    private String m_windowTitle;
    private List<Link> m_links = new ArrayList<Link>();
    private List<Group> m_groups = new ArrayList<Group>();
    private File m_overview;
    private AccessType m_access;
    private File m_dest;
    private List<Javadoc.PackageName> m_excludes = new ArrayList<Javadoc.PackageName>();
    
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
    * Set the javadoc window title.
    * @param title the title
    */
    public void setWindowtitle( final String title )
    {
        m_windowTitle = title;
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
    * Override the default destination.
    * @param dir the destination directory
    */
    public void setDest( final File dir )
    {
        m_dest = dir;
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
    
    public Javadoc.PackageName createExclude()
    {
        Javadoc.PackageName packageName = new Javadoc.PackageName();
        m_excludes.add( packageName );
        return packageName;
    }
    
    //-----------------------------------------------------------------------
    // Task
    //-----------------------------------------------------------------------

   /**
    * Task execution.
    */
    public void execute()
    {
        final Resource resource = getResource();

        log( "Generating javadoc for project: " + resource );

        File api = getDestination();
        mkDir( api );

        //
        // construct the classpath by getting all of the classpaths for
        // all of the projects within the module
        //

        Resource[] providers = resource.getClasspathProviders( Scope.RUNTIME );
        try
        {
            final Path classpath = getContext().createPath( providers, true, true );
            process( resource, classpath, api );
        }
        catch( IOException ioe )
        {
            final String error = 
              "Unable to resolve the runtime classpath for the resource ["
              + resource.getResourcePath()
              + "] (" 
              + resource.getBaseDir()
              + ").";
            throw new BuildError( error, ioe );
        }
    }
    
    private File getDestination()
    {
        if( null == m_dest )
        {
            return getContext().getTargetReportsJavadocDirectory();
        }
        else
        {
            return m_dest;
        }
    }
    
    //-----------------------------------------------------------------------
    // internal
    //-----------------------------------------------------------------------

    private Group[] getGroups()
    {
        return m_groups.toArray( new Group[0] );
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
        
        String excludes = getPackageExcludes();
        if( null != excludes )
        {
            javadoc.setExcludePackageNames( excludes );
        }
        
        Javadoc.PackageName[] excludesList = m_excludes.toArray( new Javadoc.PackageName[0] );
        for( Javadoc.PackageName exclude : excludesList )
        {
            javadoc.addExcludePackage( exclude );
        }
        
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
        Link[] links = m_links.toArray( new Link[0] );
        for( int i=0; i<links.length; i++ )
        {
            Link link = links[i];
            Javadoc.LinkArgument arg = javadoc.createLink();
            String href = link.getHref();
            log( "Adding link: " + href );
            arg.setHref( href );
        }
        
        final String title = resource.getName() + " API";
        if( null != m_windowTitle )
        {
            javadoc.setWindowtitle( m_windowTitle );
        }
        else
        {
            javadoc.setWindowtitle( title );
        }
        
        if( null == m_title )
        {
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
                    log( "Adding provider link: " + source + " from " + provider );
                    Javadoc.LinkArgument arg = task.createLink();
                    arg.setHref( source );
                }
            }
        }
    }
    
    private String getPackageExcludes()
    {
        Resource resource = getResource();
        return getPackageExcludes( resource );
    }
    
    private String getPackageExcludes( Resource resource )
    {
        return resource.getProperty( JAVADOC_EXCLUDES, null );
    }

    private void addSourcePath( Resource resource, Javadoc javadoc, Path source )
    {
        //
        // handle the target resource
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
                String excludes = getPackageExcludes( resource );
                if( null != excludes )
                {
                    packages.setExcludes( excludes );
                }
                javadoc.addPackageset( packages );
            }
        }
        
        // add providers
        
        String path = resource.getResourcePath();
        Resource[] providers = resource.getAggregatedProviders( Scope.RUNTIME, true, false );
        for( int i=0; i<providers.length; i++ )
        {
            Resource provider = providers[i];
            if( provider.getResourcePath().startsWith( path ) )
            {
                String flag = provider.getProperty( "project.javadoc.exclude", "false" );
                if( "false".equals( flag ) )
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
                            String excludes = getPackageExcludes( provider );
                            if( null != excludes )
                            {
                                packages.setExcludes( excludes );
                            }
                            javadoc.addPackageset( packages );
                        }
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
        private ArrayList<Group.Package> m_packages = new ArrayList<Group.Package>();

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
            return m_packages.toArray( new Group.Package[0] );
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
