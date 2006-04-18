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
import net.dpml.tools.info.ProcessorDirective;

import net.dpml.tools.model.Context;
//import net.dpml.tools.model.Processor;
//import net.dpml.tools.model.ProcessorNotFoundException;

import org.apache.tools.ant.Project;

/**
 * The Profile interface exposes the working configuration of the build system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultWorkbench
{
    private final Library m_library;
    private final ProcessorDirective[] m_processors;
    
   /**
    * Creation of a new workbench instance.  The workbench loads the 
    * build system configuration and establishes the available processor
    * definitions.
    *
    * @param library the common project library
    * @exception Exception if an error occurs 
    */
    DefaultWorkbench( Library library ) throws Exception
    {
        if( null == library )
        {
            throw new NullPointerException( "library" );
        }
        
        m_library = library;
        
        m_processors = StandardBuilder.CONFIGURATION.getProcessorDirectives();
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
    public ProcessorDirective[] getProcessorDirectives()
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
    public ProcessorDirective[] getProcessorSequence( Resource resource )
      throws UnknownKeyException
    {
        Type[] types = resource.getTypes();
        return getDefaultProcessorSequence( types );
    }
    
    ProcessorDirective[] getDefaultProcessorSequence( Type[] types )
      throws UnknownKeyException
    {
        String[] names = getNames( types );
        ProcessorDirective[] processors = new ProcessorDirective[ names.length ];
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            processors[i] = getProcessorDirective( name );
        }
        return processors;
    }
    
    String[] getNames( Type[] types )
    {
        String[] names = getTypeNames( types );
        ProcessorDirective[] descriptors = getProcessorDirectives( names );
        ProcessorDirective[] sorted = sortProcessorDirectives( descriptors );
        String[] list = new String[ sorted.length ];
        for( int i=0; i<sorted.length; i++ )
        {
            ProcessorDirective directive = sorted[i];
            list[i] = directive.getName();
        }
        return list;
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

    private ProcessorDirective[] getProcessorDirectives( String[] names )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            try
            {
                ProcessorDirective directive = getProcessorDirective( name );
                list.add( directive );
            }
            catch( UnknownKeyException e )
            {
                // ok
            }
        }
        return (ProcessorDirective[]) list.toArray( new ProcessorDirective[0] );
    }

    private ProcessorDirective getProcessorDirective( String name ) throws UnknownKeyException
    {
        for( int i=0; i<m_processors.length; i++ )
        {
            ProcessorDirective directive = m_processors[i];
            if( directive.getName().equals( name ) )
            {
                return directive;
            }
        }
        throw new UnknownKeyException( name );
    }

    private ProcessorDirective[] sortProcessorDirectives( ProcessorDirective[] directives )
    {
        ArrayList visited = new ArrayList();
        ArrayList stack = new ArrayList();
        for( int i=0; i<directives.length; i++ )
        {
            ProcessorDirective directive = directives[i];
            processProcessorDirective( visited, stack, directive );
        }
        return (ProcessorDirective[]) stack.toArray( new ProcessorDirective[0] );
    }
    
    private void processProcessorDirective( 
      List visited, List stack, ProcessorDirective directive )
    {
        if( visited.contains( directive ) )
        {
            return;
        }
        else
        {
            visited.add( directive );
            String[] deps = directive.getDependencies();
            ProcessorDirective[] providers = getProcessorDirectives( deps );
            for( int i=0; i<providers.length; i++ )
            {
                processProcessorDirective( visited, stack, providers[i] );
            }
            stack.add( directive );
        }
    }
}
