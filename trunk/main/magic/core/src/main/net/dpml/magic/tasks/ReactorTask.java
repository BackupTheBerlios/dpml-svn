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
import net.dpml.magic.UnknownResourceException;
import net.dpml.magic.model.Definition;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.project.Context;
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
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ReactorTask extends Sequential
{

    public static final String REACTOR_HALT_ON_FAILURE_KEY = "reactor.test.halt-on-failure";
    public static final boolean REACTOR_HALT_ON_FAILURE_VALUE = true;
    public static final String REACTOR_TEST_FAILURES_KEY = "reactor.test.failures";

    public static final String REACTOR_HALT_ON_ERROR_KEY = "reactor.test.halt-on-error";
    public static final boolean REACTOR_HALT_ON_ERROR_VALUE = true;
    public static final String REACTOR_TEST_ERRORS_KEY = "reactor.test.errors";

    private String m_target;

    private Context m_context;

    public void setTarget( final String target )
    {
        m_target = target;
    }

    public void init()
    {
        if( null == m_context )
        {
            m_context = new Context( getProject() );
        }
    }

    private Context getContext()
    {
        return m_context;
    }

    private AntFileIndex getIndex()
    {
        return getContext().getIndex();
    }

    public void execute() throws BuildException
    {
        executeReactiveBuild( m_target );
    }

    private void executeReactiveBuild( String target )
    {
        final Project project = getProject();
        List definitions = getDefinitionList();
        final Definition[] defs = walkGraph( definitions );

        getProject().log( "Candidates: " + defs.length );

        project.log( AntFileIndex.BANNER );
        for( int i=0; i<defs.length; i++ )
        {
            final Definition def = defs[i];
            project.log( def.toString() );
        }
        project.log( AntFileIndex.BANNER );

        for( int i=0; i<defs.length; i++ )
        {
            final Definition def = defs[i];
            try
            {
                executeTarget( def, target );
            }
            catch( Throwable e )
            {

                throw new BuildException( e );

            }
        }
    }

    private List getDefinitionList()
    {
        final Project project = getProject();
        final File basedir = project.getBaseDir();
        try
        {
            final ArrayList list = new ArrayList();
            final String path = basedir.getCanonicalPath();
            final Definition[] defs = getIndex().getDefinitions();
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

    private Definition[] walkGraph( List definitions )
    {
        final ArrayList result = new ArrayList();
        final ArrayList done = new ArrayList();

        final int size = definitions.size();
        for( int i = 0; i < size; i++ )
        {
            final Definition def = (Definition) definitions.get( i );
            visit( definitions, def, done, result );
        }

        final Definition[] returnValue = new Definition[result.size()];
        return (Definition[]) result.toArray( returnValue );
    }

    private void visit( final List definitions, final Definition def, final ArrayList done,
            final ArrayList order )
    {
        if( done.contains( def ) ) return;
        done.add( def );
        visitProviders( definitions, def, done, order );
        order.add( def );
    }

    private void visitProviders(
      final List definitions, final Definition def, final ArrayList done, final ArrayList order )
    {
        final Definition[] providers = getProviders( definitions, def );

        for( int i = (providers.length - 1); i > -1; i-- )
        {
            visit( definitions, providers[i], done, order );
        }
    }

    private Definition[] getProviders( List definitions, final Definition def )
    {
        final ArrayList list = new ArrayList();
        final ResourceRef[] refs = def.getResourceRefs();
        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            try
            {
                if( getIndex().isaDefinition( ref ) )
                {
                    final Definition d = getIndex().getDefinition( ref );
                    if( definitions.contains( d ) )
                    {
                        list.add( d );
                    }
                }
            }
            catch( UnknownResourceException ure )
            {
                final String error =
                  "The definition "
                  + def
                  + " contains an unknown dependency reference ["
                  + ure.getKey()
                  + "].";
                throw new BuildException( error, getLocation() );
            }
        }

        final ResourceRef[] plugins = def.getPluginRefs();
        for( int i=0; i<plugins.length; i++ )
        {
            final ResourceRef ref = plugins[i];
            try
            {
                if( getIndex().isaDefinition( ref ) )
                {
                    final Definition plugin = getIndex().getDefinition( ref );
                    if( definitions.contains( plugin ) )
                    {
                        list.add( plugin );
                    }
                }
            }
            catch( UnknownResourceException ure )
            {
                final String error =
                  "The definition "
                  + def
                  + " contains an unknown plugin reference ["
                  + ure.getKey()
                  + "].";
                throw new BuildException( error, getLocation() );
            }
        }

        return (Definition[]) list.toArray( new Definition[0] );
    }

    private void executeTarget( final Definition definition, String target ) throws BuildException
    {
        final Ant ant = (Ant) getProject().createTask( "ant" );
        ant.setDir( definition.getBaseDir() );
        ant.setInheritRefs( true );
        ant.setInheritAll( false );
        if( null != definition.getBuildFile() )
        {
            ant.setAntfile( definition.getBuildFile() );
        }
        if( null != target )
        {
            if( ! "default".equals( target ) )
            {
                log( "building " + definition + " with target: " + target );
                ant.setTarget( target );
            }
            else
            {
                log( "building " + definition + " with default target" );
            }
        }
        else
        {
            log( "building " + definition + " with default target" );
        }
        ant.init();
        ant.execute();
    }
}
