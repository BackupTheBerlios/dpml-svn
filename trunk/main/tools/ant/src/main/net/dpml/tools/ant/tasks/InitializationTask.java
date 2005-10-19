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

import org.apache.tools.ant.BuildException;

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
        String[] types = definition.getTypes();
        for( int i=0; i<types.length; i++ )
        {
            String name = types[i];
            try
            {
                TypeDescriptor type = definition.getTypeDescriptor( name );
                log( "type: " + type.getName() );
                log( "uri: " + type.getURI() );
                String[] deps = type.getDependencies();
                for( int j=0; j<deps.length; j++ )
                {
                    log( "depends: " + deps[j] );
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
}
