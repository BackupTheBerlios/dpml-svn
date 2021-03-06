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

package dpml.tools.checkstyle;

import java.io.File;
import java.net.URI;
import java.net.URL;

import com.puppycrawl.tools.checkstyle.CheckStyleTask;

import dpml.library.Resource;
import dpml.library.Module;

import dpml.tools.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
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
   /**
    * The format property key.
    */
    public static final String FORMAT_KEY = "project.checkstyle.format";
    
   /**
    * The default format value.
    */
    public static final String FORMAT_VALUE = "local:format:dpml/tools/dpml";
    
    private boolean m_init = false;
    private Context m_context;
    private boolean m_implicit = true;
    
   /**
    * Set the implicit inclusion of the current resource (and potentially
    * the resources children if the resource is a module).  If implicit
    * file inclusion is disabled then resources must be declared using an 
    * explicit fileset.
    *
    * @param flag false if implicit inclusion is to be disabled
    */
    public void setImplicit( boolean flag )
    {
        m_implicit = flag;
    }

   /**
    * Task initialization.
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

            String defaultFormat = "local:format:dpml/tools/dpml";
            String spec = m_context.getProperty( FORMAT_KEY, FORMAT_VALUE );
            if( !spec.startsWith( "local:" ) )
            {
                final String error = 
                  "Invalid checkstyle format uri ["
                  + spec
                  + ". The value must be a 'local:' artifact reference (e.g. "
                  + FORMAT_VALUE 
                  + ").";
                throw new BuildException( error, getLocation() );
            }
            
            try
            {
                URL url = new URI( spec ).toURL();
                File format = (File) url.getContent( new Class[]{File.class} );
                setConfig( format );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while attempting to resolve the checkstyle format property value ["
                  + spec
                  + "] to a local file.";
                throw new BuildException( error, e, getLocation() );
            }
        }
        m_init = true;
    }
    
   /**
    * Execute the checkstyle check.
    */
    public void execute() 
    {
        try
        {
            Resource resource = m_context.getResource();
            if( m_implicit )
            {
                addTargetToFileset( resource );
            }
            super.execute();
        }
        catch( Throwable e )
        {
            final String error = 
              "Checkstyle execution failure.";
            e.printStackTrace();
            //throw new BuildException( error, e, getLocation() );
        }
    }
    
    private void addTargetToFileset( Resource resource )
    {
        try
        {
            File file = resource.getBaseDir();
            Project project = getProject();
            //File main = new File( file, "target/build/main" );
            File main = new File( file, "src/main" );
            if( main.exists() )
            {
                FileSet fileset = new FileSet();
                fileset.setDir( main );
                fileset.setIncludes( "**/*.java" );
                super.addFileset( fileset );
            }
            //File test = new File( file, "target/build/test" );
            File test = new File( file, "src/test" );
            if( test.exists() )
            {
                FileSet fileset = new FileSet();
                fileset.setDir( test );
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
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to construct implicit fileset for the resource: " + resource;
            throw new BuildException( error, e, getLocation() );
        }
    }

    private Context getContext()
    {
        return m_context;
    }
}
