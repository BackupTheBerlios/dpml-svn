/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.transit.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.prefs.Preferences;

import net.dpml.transit.store.LocalPreferences;

import org.apache.tools.ant.DynamicConfiguratorNS;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Task supports the creation of preference as readable xml files.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PreferencesTask extends NodeTask
{
    private File m_destination;
    private boolean m_system = false;
    private String m_base;
    
    public void setProject( Project project )
    {
        setTaskName( "prefs" );
        super.setProject( project );
    }
    
    public void setDest( File dest )
    {
        m_destination = dest;
    }
    
    public void setScope( String value )
    {
        if( "user".equals( value ) )
        {
            m_system = false;
        }
        else if( "system".equals( value ) )
        {
            m_system = true;
        }
        else
        {
            final String error = 
              "Scope attribute value [" + value + "] does not match 'user' or 'system'.";
            throw new BuildException( error, getLocation() );
        }
    }
    
    public void setBase( String base )
    {
        m_base = base;
    }
    
    public Preferences buildPreferences()
    {
        Preferences prefs = createPreferences();
        apply( prefs );
        return prefs;
    }

    public void execute()
    {
        Preferences prefs = buildPreferences();
        try
        {
            if( null == m_destination )
            {
                prefs.exportSubtree( System.out );
            }
            else
            {
                File parent = m_destination.getParentFile();
                if( null != parent )
                {
                    parent.mkdirs();
                }
                FileOutputStream output = new FileOutputStream( m_destination );
                BufferedOutputStream buffer = new BufferedOutputStream( output );
                try
                {
                    prefs.exportSubtree( buffer );
                }
                finally
                {
                    output.close();
                }
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Export failure.";
            throw new BuildException( error, e, getLocation() );
        }
    }
    
    private Preferences createPreferences()
    {
        if( null == m_base )
        {
            return new LocalPreferences( null, "", m_system );
        }
        else
        {
            Preferences root = new LocalPreferences( null, "", m_system );
            return root.node( m_base );
        }
    }
}
