/*
 * Copyright 2004 Stephen McConnell.
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

package net.dpml.magic.project;

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.model.Policy;
import net.dpml.magic.model.Resource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import java.io.File;

/**
 * An ant datatype that represent a typical ant path created using
 * transitive magic dependencies.  If the path datatype declaration includes
 * the 'key' attribute the result path will include the artifact identified
 * by the key if the resource type is a jar together with all dependent
 * artifacts.  If the key attribute is not declared the path returned will
 * be composed of the dependent artifacts.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ProjectPath extends Path
{
    //-----------------------------------------------------------------------
    // state
    //-----------------------------------------------------------------------

    private Context m_context;
    private String m_key;
    private String m_type = "jar";
    private boolean m_module = false;
    private int m_mode = Policy.RUNTIME;
    private boolean m_initialized = false;
    private boolean m_resolve = true;

    //-----------------------------------------------------------------------
    // ProjectPath
    //-----------------------------------------------------------------------

   /**
    * Creation of a new path relative to a supplied project.
    *
    * @param proj the current ant project
    */
    public ProjectPath( Project proj )
    {
        super( proj );
    }

   /**
    * Set the key identifying the magic resource that will be
    * used for path construction.  If not declared the key defaults
    * to the key of the current project.
    *
    * @param key the resource key
    */
    public void setKey( final String key )
    {
        m_key = key;
    }

   /**
    * Set the type of resource to include in the path.  The default is "jar".
    *
    * @param type the resource type
    */
    public void setType( final String type )
    {
        m_type = type;
    }

   /**
    * Set module filtering. The default value is "*".
    *
    * @param module the module name to restrict path elements to.
    */
    public void setModule( final boolean module )
    {
        m_module = module;
    }

   /**
    * Set the policy concerning path resolution.  By default a path
    * structure will only be returned if it can be fully resolved.  Setting
    * the resolve flag to false disables repository resolution of path entries.
    *
    * @param flag the resolve flag
    */
    public void setResolve( final boolean flag )
    {
        m_resolve = flag;
    }

   /**
    * Set the path creation mode. A mode value may be one of
    * ANY, BUILD, TEST or RUNTIME.
    *
    * @param mode the mode value
    */
    public void setMode( final String mode )
    {
        if( "ANY".equalsIgnoreCase( mode ) )
        {
            m_mode = Policy.ANY;
        }
        else if( "BUILD".equalsIgnoreCase( mode ) )
        {
            m_mode = Policy.BUILD;
        }
        else if( "TEST".equalsIgnoreCase( mode ) )
        {
            m_mode = Policy.TEST;
        }
        else if( "RUNTIME".equalsIgnoreCase( mode ) )
        {
            m_mode = Policy.RUNTIME;
        }
        else
        {
            final String error =
              "Invalid mode argument [" + mode
              + "] - use ANY, BUILD, TEST or RUNTIME.";
            throw new BuildException( error );
        }
    }

    //------------------------------------------------------------
    // private
    //------------------------------------------------------------

    public Object clone()
    {
        setup();
        return super.clone();
    }

    public String[] list()
    {
        setup();
        return super.list();
    }

    public int size()
    {
        setup();
        return super.size();
    }

    public String toString()
    {
        setup();
        return super.toString();
    }

    private int getMode()
    {
        return m_mode;
    }

    private Resource getResource()
    {
        if( null != m_key )
        {
            return getIndex().getResource( m_key );
        }
        else
        {
            return getIndex().getResource( getKey() );
        }
    }

    private void setup()
    {
        if( m_initialized )
        {
            return;
        }

        if( null == m_context )
        {
            m_context = new Context( getProject() );
        }

        Resource def = getResource();

        Path path = def.getPath( getProject(), getMode(), m_type, m_module );

        if( null != m_key )
        {
            if( def.getInfo().isa( m_type ) )
            {
                File file = getMainFile( def, m_resolve );
                path.createPathElement().setLocation( file );
            }
        }
        super.add( path );
        m_initialized = true;
    }

    private File getMainFile( Resource resource, boolean resolve )
    {
        if( resolve )
        {
            return resource.getArtifact( getProject(), m_type );
        }
        else
        {
            return new File( getIndex().getCacheDirectory(), resource.getInfo().getPath( m_type ) );
        }
    }

    private Context getContext()
    {
        return m_context;
    }

    private String getKey()
    {
        return getContext().getKey();
    }

    private AntFileIndex getIndex()
    {
        return getContext().getIndex();
    }

}
