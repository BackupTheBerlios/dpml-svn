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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.taskdefs.Delete;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Create a new project in the current directory.
 *
 * <p>This task is typically invoked from the global.xml build file, to
 * set up a new project in the current directory.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class NewTask extends Task
{
    private String m_Type;
    
    public void execute() throws BuildException 
    {
        File destFile = new File( "build.xml" );
        if( destFile.exists() )
            throw new BuildException( "build.xml file already exists in the current directory." );
        if( m_Type == null || "".equals( m_Type ) )
            throw new BuildException( "'type' attribute has not been set." );
            
        createBuildXml( destFile );
        
        createIndexEntry();
    }
    
    public String getType()
    {
        return m_Type;
    }
    
    public void setType( String type )
    {   
        m_Type = type;
    }
    
    private void createIndexEntry()
    {
        // TODO
        // Look for projects in the directory above, and see if those
        // projects belongs to the same group, if so use that group
        // for this project.
        // If not, see if any part of the group is common, then use that
        // part of the group and add the current working dir name to it.
        //
        // Finally, insert the <project> into the index.xml file.
        //
        
    }
    
    private void createBuildXml( File destFile )
    {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try
        {
            fos = new FileOutputStream( destFile );
            osw = new OutputStreamWriter( fos, "UTF-8" );
            writeContent( osw );
        } catch( IOException e )
        {
        } finally
        {
            if( osw != null )
            {
                try
                {
                    osw.close();
                } catch( IOException e )
                {
                    throw new BuildException( "Can't close stream.", e );
                }
            }
            if( fos != null )
            {
                try
                {
                    fos.close();
                } catch( IOException e )
                {
                    throw new BuildException( "Can't close stream.", e );
                }
            }
        }
    }
    
    private void writeContent( Writer out )
    {
        File f = new File( System.getProperty( "user.dir" ) );
        String dirname = f.getName();
        
        PrintWriter pw = new PrintWriter( out, true );
        pw.println( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" );
        pw.println();
        pw.println( "<project name=\"" + dirname + "\" default=\"default\" basedir=\".\" ");
        pw.println( "    xmlns:x=\"antlib:org.apache.avalon.tools\">" );
        pw.println();
        pw.println( "  <property file=\"build.properties\"/>" );
        pw.println( "  <x:home />" );
        pw.println( "  <import file=\"${magic.templates}/" + m_Type + ".xml\"/>" );
        pw.println();
        pw.println( "</project>" );
        pw.println();        
        pw.flush();
        pw.close();
    }
}
