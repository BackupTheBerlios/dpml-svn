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

import org.apache.avalon.tools.model.Context;
import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.ResourceRef;
import org.apache.avalon.tools.model.Home;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Sequential;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Build a set of projects taking into account cross-project dependencies.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ReactorTask extends Sequential
{
    private static final String BANNER = 
      "------------------------------------------------------------------------";

    private String m_target;
    private List m_defs;
    private Context m_context;
    private Home m_home;
    private boolean m_verbose = true;

    public void init()
    {
        if( null == m_context )
        {
            m_context = Context.getContext( getProject() );
        }

        if( null == m_home ) 
        {
            Home home = (Home) getProject().getReference( Home.KEY );
            if( null == home )
            {
                final String error = 
                  "Undefined home.";
                throw new BuildException( error );
            }
            else
            {
                m_home = home;
            }
        }
    }

    public void setTarget( final String target )
    {
        m_target = target;
    }

    public void setVerbose( final boolean flag )
    {
        m_verbose = flag;
    }

    public void execute() throws BuildException 
    {
        final Project project = getProject();
        m_defs = getDefinitionList();
        final Definition[] defs = walkGraph();

        log( "Candidates: " + defs.length );
        log( "Preparing build sequence." );
        if( m_verbose )
        {
            project.log( BANNER );
            for( int i=0; i<defs.length; i++ )
            {
                final Definition def = defs[i];
                project.log( def.toString() );
            }
            project.log( BANNER );
        }


        if( null != m_target )
        {
            for( int i=0; i<defs.length; i++ )
            {
                final Definition def = defs[i];
                try
                {
                    executeTarget( def, m_target );
                }
                catch( Throwable e )
                {
                    throw new BuildException( e );
                }
            }
        }
        else
        {
            for( int i=0; i<defs.length; i++ )
            {
                final Definition def = defs[i];
                project.setProperty( "reactor.key", def.getKey() );
                project.setProperty( "reactor.name", def.getInfo().getName() );
                project.setProperty( "reactor.group", def.getInfo().getGroup() );
                if( null == def.getInfo().getVersion() )
                {
                    project.setProperty( "reactor.version", "" );
                }
                else
                {
                    project.setProperty( "reactor.version", def.getInfo().getVersion() );
                }
                project.setProperty( "reactor.basedir", def.getBaseDir().toString() );
                project.setProperty( "reactor.path", def.getInfo().getPath() );
                project.setProperty( "reactor.uri", def.getInfo().getURI() );
                project.setProperty( "reactor.spec", def.getInfo().getSpec() );
                project.setProperty( "reactor.type", def.getInfo().getType() );
                project.setProperty( "reactor.filename", def.getInfo().getFilename() );
                project.setProperty( "reactor.short-filename", def.getInfo().getShortFilename() );
                super.execute();
            }
        }
    }

    private void executeTarget( final Definition definition, final String target )
    {
        final Ant ant = (Ant) getProject().createTask( "ant" );
        ant.setDir( definition.getBaseDir() );
        ant.setInheritRefs( false );
        ant.setInheritAll( false );
        if(( null != target ) && (!"default".equals( target )))
        {
            ant.setTarget( target );
        }
        ant.init();
        ant.execute();
    }

    private Definition[] walkGraph()
    {
        final ArrayList result = new ArrayList();
        final ArrayList done = new ArrayList();

        final int size = m_defs.size();
        for( int i = 0; i < size; i++ )
        {
            final Definition def = (Definition) m_defs.get( i );
            visit( def, done, result );
        }

        final Definition[] returnValue = new Definition[result.size()];
        return (Definition[]) result.toArray( returnValue );
    }

    private void visit( final Definition def, final ArrayList done,
            final ArrayList order )
    {
        if( done.contains( def ) ) return;
        done.add( def );
        visitProviders( def, done, order );
        order.add( def );
    }

    private void visitProviders( 
      final Definition def, final ArrayList done, final ArrayList order )
    {
        final Definition[] providers = getProviders( def );
        for( int i = (providers.length - 1); i > -1; i-- )
        {
            visit( providers[i], done, order );
        }
    }

    private Definition[] getProviders( final Definition def )
    {
        final ArrayList list = new ArrayList();
        final ResourceRef[] refs = def.getResourceRefs();
        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            if( getHome().isaDefinition( ref ) )
            {
                final Definition d = getHome().getDefinition( ref );
                if( m_defs.contains( d ) )
                {
                    list.add( d );
                }
            }
        }

        final ResourceRef[] plugins = def.getPluginRefs();
        for( int i=0; i<plugins.length; i++ )
        {
            final ResourceRef ref = plugins[i];
            if( getHome().isaDefinition( ref ) )
            {
                final Definition plugin = getHome().getPlugin( ref );
                if( m_defs.contains( plugin ) )
                {
                    list.add( plugin );
                }
            }
        }

        return (Definition[]) list.toArray( new Definition[0] );
    }

    private List getDefinitionList()
    {
        final Project project = getProject();
        final File basedir = project.getBaseDir(); 
        try
        {
            final ArrayList list = new ArrayList();
            final String path = basedir.getCanonicalPath();
            final Definition[] defs = getHome().getDefinitions();
            for( int i=0; i<defs.length; i++ )
            {
                final Definition def = defs[i];
                final String base = def.getBaseDir().getCanonicalPath();
                if( base.startsWith( path ) )
                {
                    if( base.length() == path.length() )
                    {
                        list.add( def );
                    }
                    else
                    {
                        String next = base.substring( path.length() );
                        if( next.startsWith( File.separator ) )
                        {
                            list.add( def );
                        }
                    }
                }
            }
            return list;
        }
        catch( IOException ioe )
        {
            throw new BuildException( ioe );
        }
    }

    private Home getHome()
    {
        return m_home;
    }
}
