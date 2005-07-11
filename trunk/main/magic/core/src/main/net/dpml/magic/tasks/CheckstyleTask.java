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

import com.puppycrawl.tools.checkstyle.CheckStyleTask;

import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.project.Context;
import net.dpml.magic.AntFileIndex;

import net.dpml.transit.artifact.Handler;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.net.URL;

public class CheckstyleTask extends CheckStyleTask
{
    private boolean m_init = false;
    private Context m_context;

    public void init()
    {
        super.init();
        if( ! m_init )
        {
            m_init = true;
            m_context = new Context( getProject() );
            Resource resource = getResource();
            if( resource instanceof Definition )
            {
                Definition def = (Definition) resource;
                addTargetToFileset( def );
            }
        }
        m_init = true;
    }

    public void setUri( String uri )
    {
        try
        {
            URL url = new URL( null, uri, new Handler() );
            File local = (File) url.getContent( new Class[]{File.class} );
            super.setConfig( local );
        }
        catch( Throwable e )
        {
            final String error = 
              "Could not resolve the uri ["
              + uri 
              + "]";
            throw new BuildException( error, e );
        }
    }

    private void addTargetToFileset( Definition def )
    {
        File file = def.getBaseDir();
        File main = getSrcMainDirectory(def);
        if( main.exists() )
        {
            FileSet fileset = new FileSet();
            fileset.setDir( main );
            fileset.setIncludes( "**/*.java" );
            super.addFileset( fileset );
        }

        if( "module".equals( def.getInfo().getType() ) )
        {
            Definition[] defs = getIndex().getSubsidiaryDefinitions( def );
            for( int i=0; i<defs.length; i++ )
            {
                Definition d = defs[i];
                addTargetToFileset( d );
            }
        }
    }

    /**
     * 
     * @param def the project to analyse
     * @return the main source dir
     */
    private File getSrcMainDirectory(Definition def) 
    {
		
        File main = new File(def.getBaseDir(), "src/main");
        File java = new File(def.getBaseDir(), "src/java");
        if (main.exists())
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
