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
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

public class PublishTask extends SystemTask
{
    private String m_path;

    public void setPath( final String path )
    {
        m_path = path;
    }

    public void execute()
    {
        final File basedir = getProject().getBaseDir();
        final File target = new File( basedir, Context.TARGET );
        final File source = new File( target, Context.DOCS );

        if( source.exists() )
        {
            final File destination = getPath();
            final FileSet fileset = new FileSet();
            fileset.setDir( source );
            fileset.createInclude().setName( "**/*" );

            copy( destination, fileset );
        }
    }

   /**
    * Return the path relative to the document cache to which
    * documentation content will be published.  If no path has 
    * been declared, the default path return is equivalent to 
    * ${project.doc.cache}/[group]/[name]
    */
    private File getPath()
    {
        final String key = getKey();
        final Definition definition = getHome().getDefinition( key );
        final File cache = getHome().getDocsRepository().getCacheDirectory();
        if( null == m_path )
        {
            final String group = definition.getInfo().getGroup();
            final String name = definition.getInfo().getName();
            final File parent = new File( cache, group );
            return new File( parent, name );
        }
        else
        {
            return Context.getFile( cache, m_path );
        }
    }

    private void copy( final File destination, final FileSet fileset )
    {
        mkDir( destination );
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTaskName( getTaskName() );
        copy.setPreserveLastModified( true );
        copy.setTodir( destination );
        copy.addFileset( fileset );
        copy.init();
        copy.execute();
    }

}