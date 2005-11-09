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

package net.dpml.magic.checkstyle;

import java.io.File;

import com.puppycrawl.tools.checkstyle.CheckStyleTask;

import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Resource;
import net.dpml.magic.project.Context;
import net.dpml.magic.AntFileIndex;

import net.dpml.transit.Transit;

import org.apache.tools.ant.types.FileSet;



/**
 * The checkstyle task handes the establishment of a classic checkstyle task 
 * with automatic resolution of source directories.  Typical usage is within 
 * a build file that aggregates results for a module.
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
            m_context = new Context( getProject() );
            Resource resource = getResource();
            if( resource instanceof Definition )
            {
                Definition def = (Definition) resource;
                addTargetToFileset( def );
            }

            File prefs = Transit.DPML_PREFS;
            File magic = new File( prefs, "magic" );
            File formats = new File( magic, "formats" );
            File format = new File( formats, "dpml.format" );
            setConfig( format );
        }
        m_init = true;
    }

    private void addTargetToFileset( Definition def )
    {
        File file = def.getBaseDir();
        File main = getSrcMainDirectory( def );
        if( main.exists() )
        {
            FileSet fileset = new FileSet();
            fileset.setDir( main );
            fileset.setIncludes( "**/*.java" );
            super.addFileset( fileset );
        }
        if( def.getInfo().isa( "module" ) )    
        {
            Definition[] defs = getIndex().getSubsidiaryDefinitions( def );
            for( int i=0; i < defs.length; i++ )
            {
                Definition d = defs[i];
                addTargetToFileset( d );
            }
        }
    }

   /**
    * Resolve the source directory for a project definition.
    * @param def the defintion
    * @return the source dir
     */
    private File getSrcMainDirectory( Definition def ) 
    {
        File main = new File( def.getBaseDir(), "src/main" );
        File java = new File( def.getBaseDir(), "src/java" );
        if( main.exists() )
        {
            return main;
        }
        return java;
    }

    private String getKey()
    {
        return getContext().getKey();
    }

    private Resource getResource()
    {
        return getIndex().getResource( getKey() );
    }

    private AntFileIndex getIndex()
    {
        return getContext().getIndex();
    }

    private Context getContext()
    {
        return m_context;
    }
}