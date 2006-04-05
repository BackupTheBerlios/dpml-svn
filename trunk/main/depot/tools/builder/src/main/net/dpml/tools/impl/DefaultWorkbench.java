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

package net.dpml.tools.impl;

import java.util.List;
import java.util.ArrayList;

import net.dpml.lang.UnknownKeyException;

import net.dpml.library.Library;
import net.dpml.library.Resource;
import net.dpml.library.Type;

import net.dpml.tools.info.BuilderDirective;
import net.dpml.tools.info.BuilderDirectiveHelper;
import net.dpml.tools.info.ListenerDirective;
import net.dpml.tools.model.Workbench;
import net.dpml.tools.model.Context;
import net.dpml.tools.model.Processor;
import net.dpml.tools.model.ProcessorNotFoundException;

import org.apache.tools.ant.Project;

/**
 * The Profile interface exposes the working configuration of the build system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultWorkbench implements Workbench
{
    private final Library m_library;
    private final BuilderDirective m_directive;
    private final DefaultProcessor[] m_processors;
    
   /**
    * Creation of a new workbench instance.  The workbench loads the 
    * build system configuration and establishes the available processor
    * definitions.
    *
    * @param library the common project library
    * @exception Exception if an error occurs 
    */
    public DefaultWorkbench( Library library ) throws Exception
    {
        if( null == library )
        {
            throw new NullPointerException( "library" );
        }
        
        m_library = library;
        
        m_directive = BuilderDirectiveHelper.build();
        ListenerDirective[] directives = m_directive.getListenerDirectives();
        m_processors = new DefaultProcessor[ directives.length ];
        for( int i=0; i<directives.length; i++ )
        {
            ListenerDirective directive = directives[i];
            m_processors[i] = new DefaultProcessor( directive );
        }
    }

   /**
    * Create a project context.
    * @param resource the resource 
    * @param project the current project
    * @return the project context
    */
    public Context createContext( Resource resource, Project project )
    {
        return new DefaultContext( resource, m_library, project );
    }
    
   /**
    * Return the common resource library.
    * @return the library
    */
    public Library getLibrary()
    {
        return m_library;
    }

   /**
    * Return an array of all registered processor definitions.
    * @return the processor definition array
    */
    public Processor[] getProcessors()
    {
        return m_processors;
    }
    
   /**
    * Return the sequence of processor definitions supporting production of a 
    * supplied resource.  The implementation constructs a sequence of process
    * instances based on the types declared by the resource combined with 
    * dependencies declared by respective process defintions. Clients may
    * safely invoke processes sequentially relative to the returned process
    * sequence.
    * 
    * @param resource the resource to be produced
    * @return a sorted array of processor definitions supporting resource production
    * @exception ProcessorNotFoundException if a processor referenced by another 
    *   processor as a dependent cannot be resolved
    */ 
    public Processor[] getProcessorSequence( Resource resource )
      throws ProcessorNotFoundException
    {
        Type[] types = resource.getTypes();
        return getDefaultProcessorSequence( types );
    }
    
    DefaultProcessor[] getDefaultProcessorSequence( Type[] types )
      throws ProcessorNotFoundException
    {
        String[] names = getListenerNames( types );
        DefaultProcessor[] processors = new DefaultProcessor[ names.length ];
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            processors[i] = getDefaultProcessor( name );
        }
        return processors;
    }
    
    String[] getListenerNames( Type[] types )
    {
        String[] names = getTypeNames( types );
        ListenerDirective[] descriptors = getListenerDirectives( names );
        ListenerDirective[] sorted = sortListenerDirectives( descriptors );
        String[] processors = new String[ sorted.length ];
        for( int i=0; i<sorted.length; i++ )
        {
            ListenerDirective directive = sorted[i];
            processors[i] = directive.getName();
        }
        return processors;
    }
    
    String[] getTypeNames( Type[] types )
    {
        String[] names = new String[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            Type type = types[i];
            names[i] = type.getID();
        }
        return names;
    }

    private ListenerDirective[] getListenerDirectives( String[] names )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            try
            {
                ListenerDirective directive = getListenerDirective( name );
                list.add( directive );
            }
            catch( UnknownKeyException e )
            {
                // ok
            }
        }
        return (ListenerDirective[]) list.toArray( new ListenerDirective[0] );
    }

    private ListenerDirective getListenerDirective( String name ) throws UnknownKeyException
    {
        ListenerDirective[] directives = m_directive.getListenerDirectives();
        for( int i=0; i<directives.length; i++ )
        {
            ListenerDirective directive = directives[i];
            if( directive.getName().equals( name ) )
            {
                return directive;
            }
        }
        throw new UnknownKeyException( name );
    }

    private ListenerDirective[] sortListenerDirectives( ListenerDirective[] directives )
    {
        ArrayList visited = new ArrayList();
        ArrayList stack = new ArrayList();
        for( int i=0; i<directives.length; i++ )
        {
            ListenerDirective directive = directives[i];
            processListenerDirective( visited, stack, directive );
        }
        return (ListenerDirective[]) stack.toArray( new ListenerDirective[0] );
    }
    
    private void processListenerDirective( 
      List visited, List stack, ListenerDirective directive )
    {
        if( visited.contains( directive ) )
        {
            return;
        }
        else
        {
            visited.add( directive );
            String[] deps = directive.getDependencies();
            ListenerDirective[] providers = getListenerDirectives( deps );
            for( int i=0; i<providers.length; i++ )
            {
                processListenerDirective( visited, stack, providers[i] );
            }
            stack.add( directive );
        }
    }
    
    private DefaultProcessor getDefaultProcessor( String name )
      throws ProcessorNotFoundException
    {
        for( int i=0; i<m_processors.length; i++ )
        {
            DefaultProcessor processor = m_processors[i];
            if( name.equals( processor.getName() ) )
            {
                return processor;
            }
        }
        throw new ProcessorNotFoundException( name );
    }
}
