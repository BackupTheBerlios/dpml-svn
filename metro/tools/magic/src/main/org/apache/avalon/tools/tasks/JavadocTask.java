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

package org.apache.avalon.tools.tasks;


import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.ResourceRef;
import org.apache.avalon.tools.model.Resource;
import org.apache.tools.ant.BuildException;
import org.apache.avalon.tools.model.Policy;
import org.apache.avalon.tools.model.Home;
import org.apache.avalon.tools.model.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javadoc;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Build the javadoc for a project. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class JavadocTask extends SystemTask
{
    public static class Link
    {
        private String m_href;
        private File m_dir;
        private int m_tag = ResourceRef.ANY;
        private String m_key;

        public Link()
        {
            m_href = null;
        }

        public Link( final String href )
        {
            this( href, null );
        }

        public Link( final String href, final File dir )
        {
            m_href = href;
            m_dir = dir;
        }

        public void setHref( final String href )
        {
            m_href = href;
        }

        public void setTag( final String tag )
        {
            m_tag = ResourceRef.getCategory( tag );
        }

        public String getHref()
        {
            return m_href;
        }

        public void setKey( final String key )
        {
            m_key = key;
        }

        public void setDir( final File dir )
        {
            m_dir = dir;
        }

        public File getDir( final Home home )
        {
            if( null == m_key )
            {
                return m_dir;
            }
            else
            {
                final Resource resource = home.getResource( m_key );
                final File cache = home.getDocsRepository().getCacheDirectory();
                final String category = ResourceRef.getCategoryName( m_tag );
                final String spec = 
                  resource.getInfo().getSpecification( "/", "/" );
                return new File( cache, spec + "/" + category );
            }
        }

        public boolean matches( final int category )
        {
            if( ResourceRef.ANY == category ) return true;
            if( ResourceRef.ANY == m_tag ) return true;
            return ( m_tag == category );
        }

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
                    return "link: " + m_href + " from [" + m_key + "]";
                }
            }
            else
            {
                return "link: " + m_href + " at " + m_dir;
            }
        }
    }

    public static final String JAVADOC = "javadoc";
    public static final String API = "api";
    public static final String SPI = "spi";
    public static final String IMPL = "impl";

    private String m_id;
    private String m_title;
    private List m_links = new ArrayList();
    private boolean m_staged = false;

    public void setId( final String id )
    {
        m_id = id;
    }

    public void setTitle( final String title )
    {
        m_title = title;
    }

    public void setStaged( final boolean staged )
    {
        m_staged = staged;
    }

    public Link createLink()
    {
        final Link link = new Link();
        m_links.add( link );
        return link;
    }

    public void execute() throws BuildException
    {
        final Definition def = getReferenceDefinition();

        log( "Generating javadoc for project: " + def, Project.MSG_VERBOSE );

        final File root = def.getDocDirectory();
        final Path classpath = def.getPath( getProject(), Policy.BUILD );

        if( m_staged )
        {
            final File api = new File( root, "api" );
            final File spi = new File( root, "spi" );
            final File imp = new File( root, "impl" );

            setup( def, classpath, ResourceRef.API, api, false );
            setup( def, classpath, ResourceRef.SPI, spi, false );
            setup( def, classpath, ResourceRef.IMPL, imp,  true );
        }
        else
        {
            setup( def, classpath, ResourceRef.ANY, root,  true );
        }
    }

    private void setup( 
      final Definition def, final Path classpath, final int category, 
      final File root, final boolean flag )
    {
        final ResourceRef[] refs =
          def.getResourceRefs( getProject(), Policy.RUNTIME, category, true );
        if( flag || ( refs.length > 0 ))
        {
            generate( def, classpath, refs, category, root, flag );
        }
    }

    private void generate( 
       final Definition definition, final Path classpath, final ResourceRef[] refs,
       final int category, final File root, final boolean flag )
    {
        final Javadoc javadoc = (Javadoc) getProject().createTask( "javadoc" );
        javadoc.setTaskName( getTaskName() );
        createAvalonTags( javadoc );
        
        javadoc.init();
        javadoc.setDestdir( root );
        final Path source = javadoc.createSourcepath();
        javadoc.createClasspath().add( classpath );
        final String title = getTitle( definition, category );
        javadoc.setDoctitle( title );

        log( "Generating: " + title );

        for( int i=0; i<m_links.size(); i++ )
        {
            final Link link = (Link) m_links.get( i );
            if( link.matches( category ) )
            {
                final Javadoc.LinkArgument arg = javadoc.createLink();
                arg.setHref( link.getHref() );
                final File dir = link.getDir( getHome() );
                if( null != dir )
                {
                    if( dir.exists() )
                    {
                        log( link.toString() );
                        arg.setOffline( true );
                        arg.setPackagelistLoc( dir );
                    }
                    else
                    {
                        final String warning = 
                          link + ": warning - unresolved directory";
                        log( warning, Project.MSG_WARN );
                    }
                }
                else
                {
                    log( link.toString() );
                }
            }
        }

        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            final Resource resource = getHome().getResource( ref );
            if( resource instanceof Definition )
            {
                final Definition def = (Definition) resource;
                final File base = def.getBaseDir();
                final File src = Context.getFile( base, "target/build/main" );
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
                    log( "Ignoring src path: " + src, Project.MSG_WARN );
                }
            }
        }

        if( flag )
        {
            final File basedir = definition.getBaseDir();
            final File local = new File( basedir, "target/build/main" );
            if( local.exists() )
            {
                source.createPathElement().setLocation( local );
                final DirSet packages = new DirSet();
                packages.setDir( local );
                packages.setIncludes( "**/**" );
                javadoc.addPackageset( packages );
            }
        }

        javadoc.execute();
    }

    private void createAvalonTags( Javadoc javadoc )
    {
        for( int i=0 ; i < TAG_LIST.length ; i++ )
        {
            Javadoc.TagArgument tag = javadoc.createTag();
            tag.setName( TAG_LIST[i][0] );
            tag.setDescription( TAG_LIST[i][1] );
            tag.setScope( TAG_LIST[i][2] );
            tag.setEnabled( true );
        }
    }
    
    
    private String getTitle( final Definition def, final int category )
    {
        final String extra = getTitleSuppliment( def, category );
        if( null == m_title )
        {
            return def.getInfo().getName() + extra;
        }
        return m_title + extra;
    }

    private String getTitleSuppliment( final Definition def, final int cat )
    {
        final String category = ResourceRef.getCategoryName( cat ).toUpperCase();
        final String version = def.getInfo().getVersion();
        if( null == version )
        {
            return " : " + category;
        }
        else
        {
            return ", Version " + version + " : " + category;
        }
    }

    private Definition getReferenceDefinition()
    {
        if( null != m_id )
        {
            return getHome().getDefinition( m_id );
        }
        else
        {
            return getHome().getDefinition( getKey() );
        }
    }
    
    private String[][] TAG_LIST = 
    {
        { "avalon.component", "Avalon Component Type", "types" },
        { "avalon.service", "Avalon Service", "types" },
        { "avalon.attribute", "Avalon Attribute", "types" },
        { "avalon.stage", "Avalon Lifecycle Stage", "types" },
        { "avalon.extension", "Avalon Extension", "types" },
    
        { "avalon.entry", "Avalon Context Entry", "methods,constructors" },
        { "avalon.dependency", "Avalon Dependency Declaration", "methods,constructors" },
        { "avalon.logger", "Avalon Logger", "methods,constructors" },
        { "avalon.configuration", "Avalon Configuration", "methods,constructors" },
        { "avalon.context", "Avalon Context Interface", "methods,constructors" }
    };
}
