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

package net.dpml.tools.ant.tasks;

import java.net.URI;
import java.util.ArrayList;

import net.dpml.tools.ant.StandardBuilder;
import net.dpml.tools.ant.Definition;
import net.dpml.tools.info.TypeDescriptor;
import net.dpml.tools.ant.process.JarProcess;
import net.dpml.tools.ant.process.PluginProcess;
import net.dpml.tools.model.TypeNotFoundException;
import net.dpml.tools.info.TypeDescriptor;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildException;

import net.dpml.transit.Transit;

/**
 * Execute the install phase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class InitializationTask extends GenericTask
{
    private ArrayList m_list = new ArrayList();
        
   /**
    * Initialize type to processor mapping.
    */
    public void execute()
    {
        Definition definition = getDefinition();
        TypeDescriptor[] types = getSequencedTypes( definition );
        for( int i=0; i<types.length; i++ )
        {
            TypeDescriptor type = types[i];
            String name = type.getName();
            try
            {
                //log( "type: " + type.getName() );
                //log( "uri: " + type.getURI() );
                //String[] deps = type.getDependencies();
                //for( int j=0; j<deps.length; j++ )
                //{
                //    log( "depends: " + deps[j] );
                //}
                
                if( type.getName().equals( "jar" ) )
                {
                    JarProcess process = new JarProcess();
                    getProject().addBuildListener( process );
                }
                else if( type.getName().equals( "plugin" ) )
                {
                    PluginProcess process = new PluginProcess();
                    getProject().addBuildListener( process );
                }
                else
                {
                    ClassLoader classloader = getClass().getClassLoader();
                    Project project = getProject();
                    URI uri = type.getURI();
                    Object[] params = new Object[]{project};
                    Object object = 
                      Transit.getInstance().getRepository().getPlugin( 
                        classloader, uri, params );
                    if( object instanceof BuildListener )
                    {
                        BuildListener listener = (BuildListener) object;
                        getProject().addBuildListener( listener );
                        log( "registered listener: " + listener.getClass().getName() );
                    }
                    else
                    {
                        final String error = 
                          "Build processor required for the type ["
                          + type
                          + "] with the uri ["
                          + uri
                          + "] is not a build listener.";
                        throw new BuildException( error, getLocation() );
                    }
                }
            }
            catch( Exception e )
            {
                final String error = 
                  "Failed to establish build listener for type [" + name + "].";
                throw new BuildException( error, getLocation() );
            }
        }
    }
    
    private TypeDescriptor[] getSequencedTypes( Definition definition )
    {
        ArrayList list = new ArrayList();
        String[] types = definition.getTypes();
        try
        {
            expand( list, definition, types );
            TypeDescriptor[] targets = (TypeDescriptor[]) list.toArray( new TypeDescriptor[0] );
            ArrayList sorted = new ArrayList();
            for( int i=0; i<targets.length; i++ )
            {
                TypeDescriptor type = targets[i];
                sort( sorted, type, targets );
            }
            return (TypeDescriptor[]) sorted.toArray( new TypeDescriptor[0] );
        }
        catch( TypeNotFoundException e )
        {
            final String error = 
              "Type reference unresolvable "
              + e.getMessage();
            throw new BuildException( error, e );
        }
    }
    
    private void expand( ArrayList list, Definition definition, String[] targets ) throws TypeNotFoundException
    {
        for( int i=0; i<targets.length; i++ )
        {
            String name = targets[i];
            TypeDescriptor type = definition.getTypeDescriptor( name );
            if( !list.contains( type ) )
            {
                list.add( type );
                String[] deps = type.getDependencies();
                expand( list, definition, deps );
            }
        }
    }
    
    private void sort( ArrayList sorted, TypeDescriptor type, TypeDescriptor[] targets ) throws TypeNotFoundException
    {
        if( sorted.contains( type ) )
        {
            return;
        }
        String[] deps = type.getDependencies();
        if( deps.length == 0 )
        {
            sorted.add( type );
        }
        else
        {
            for( int i=0; i<deps.length; i++ )
            {
                String dep = deps[i];
                TypeDescriptor t = select( dep, targets );
                sort( sorted, t, targets );
            }
            sorted.add( type );
        }
    }
    
    private TypeDescriptor select( String name, TypeDescriptor[] targets ) throws TypeNotFoundException
    {
        for( int i=0; i<targets.length; i++ )
        {
            TypeDescriptor target = targets[i];
            if( name.equals( target.getName() ) )
            {
                return target;
            }
        }
        throw new TypeNotFoundException( name );
    }
}
