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

package net.dpml.tools.checkstyle;

import java.io.File;

import com.puppycrawl.tools.checkstyle.CheckStyleTask;

import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Module;
import net.dpml.tools.ant.Context;

import net.dpml.transit.Transit;

import org.apache.tools.ant.types.FileSet;

/**
 * The checkstyle task handes the establishment of a classic checkstyle task 
 * with automatic resolution of source directories.  Typical usage is within 
 * a build file that aggregates results for a module.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CheckstyleTask extends CheckStyleTask
{
    private boolean m_init = false;
    private Context m_context;

   /**
    * Task initiaization.
    */
    public void init()
    {
        super.init();
        if( !m_init )
        {
            m_init = true;
            m_context = (Context) getProject().getReference( "project.context" );
            if( null == m_context )
            {
                final String error = 
                  "Missing 'project.context' reference.";
                throw new IllegalStateException( error );
            }
            Resource resource = m_context.getResource();
            addTargetToFileset( resource );
            File prefs = Transit.DPML_PREFS;
            File format = new File( prefs, "tools/formats/dpml.format" );
            setConfig( format );
        }
        m_init = true;
    }

    // TODO: remove hard reference to target/build/main

    private void addTargetToFileset( Resource resource )
    {
        File file = resource.getBaseDir();
        File main = new File( file, "target/build/main" ); // <----------- YUK!
        if( main.exists() )
        {
            FileSet fileset = new FileSet();
            fileset.setDir( main );
            fileset.setIncludes( "**/*.java" );
            super.addFileset( fileset );
        }
        if( resource instanceof Module )    
        {
            Module module = (Module) resource;
            Resource[] children = module.getResources();
            for( int i=0; i < children.length; i++ )
            {
                Resource child = children[i];
                addTargetToFileset( child );
            }
        }
    }

    private Context getContext()
    {
        return m_context;
    }
}
