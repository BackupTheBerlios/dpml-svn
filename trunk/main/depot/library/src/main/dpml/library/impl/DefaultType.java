/*
 * Copyright 2007 Stephen J. McConnell
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

package dpml.library.impl;

import dpml.library.info.TypeDirective;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Hashtable;

import dpml.library.Type;
import dpml.library.Resource;

import net.dpml.transit.Artifact;
import net.dpml.transit.Layout;
import net.dpml.transit.Transit;

import net.dpml.util.Logger;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultType extends DefaultDictionary implements Type
{
    private final TypeDirective m_directive;
    private final DefaultResource m_resource;
    
   /**
    * Creation of a new type.
    * @param resource the resource
    * @param type the type directive
    */
    public DefaultType( DefaultResource resource, TypeDirective type )
    {
        super( resource, type );
        
        if( null == resource )
        {
            throw new NullPointerException( "resource" );
        }
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        
        m_directive = type;
        m_resource = resource;
    }
    
    //----------------------------------------------------------------------------
    // Type
    //----------------------------------------------------------------------------
    
   /**
    * Return the resource holding the type.
    * @return the resource
    */
    public Resource getResource()
    {
        return m_resource;
    }
    
   /**
    * Return the type production id.
    * @return the type id
    */
    public String getID()
    {
        return m_directive.getID();
    }
        
   /**
    * Return the alias version.
    * @return the alias version value (may be null)
    */
    public String getVersion()
    {
        String version = m_directive.getVersion();
        if( null == version )
        {
            return m_resource.getVersion();
        }
        else
        {
            return version;
        }
    }
    
    public String getName()
    {
        String name = m_directive.getName();
        if( null == name )
        {
            return m_resource.getName();
        }
        else
        {
            return name;
        }
    }
    
    public String getCompoundName()
    {
        String name = m_directive.getName();
        if( null == name )
        {
            return getID();
        }
        else
        {
            return getID() + "#" + name;
        }
    }
    
    public String getSource()
    {
        return m_directive.getSource();
    }
    
    public boolean getTest()
    {
        return m_directive.getTest();
    }

   /**
    * Return the alias production flag.
    * @return the alias flag
    */
    public boolean getAliasProduction()
    {
        return m_directive.getAliasProduction();
    }
    
   /**
    * Return the type local filename.
    * @return the filename
    */
    public File getFile()
    {
        return getFile( false );
    }
    
   /**
    * Return the type filename.
    * @param local true if the requested filename is the local filename otherwise the 
    *   cached filename is returned
    * @return the filename
    */
    public File getFile( boolean local ) // <!-- TODO: policy concerning versions on test types
    {
        try
        {
            if( getTest() )
            {   
                String id = getID();
                String name = getName();
                String path = "target/test/" + name + "." + id;
                File basedir = m_resource.getBaseDir();
                return new File( basedir, path );
            }
            else
            {
                if( local )
                {
                    String id = getID();
                    String name = getName();
                    String version = getVersion();
                    String path = "target/deliverables/" + id +  "s/" + name;
                    if( null != version )
                    {
                        path = path + "-" + version;
                    }
                    path = path + "." + id;
                    File basedir = m_resource.getBaseDir();
                    return new File( basedir, path );
                }
                else
                {
                    Artifact artifact = getArtifact();
                    Layout layout = Transit.getInstance().getCacheLayout();
                    File cache = Transit.getInstance().getCacheDirectory();
                    String path = layout.resolvePath( artifact );
                    return new File( cache, path );
                }
            }
        }
        catch( Exception e )
        {
            final String error = 
              "Unable to resolve resource path.";
            throw new RuntimeException( error, e );
        }
    }
    
    public Artifact getArtifact()
    {
        String id = getID();
        String group = m_resource.getGroupName();
        String name = getName();
        String version = getVersion();
        String scheme = m_resource.getScheme();
        return Artifact.createArtifact( scheme, group, name, version, id );
    }
    
    public Artifact getResolvedArtifact() throws IOException
    {
        try
        {
            Artifact artifact = getArtifact();
            URL url = artifact.toURL();
            url.openConnection().connect();
            return artifact;
        }
        catch( IOException ioe )
        {
            final String error = 
              "Unable to resolve the artifact for the type ["
              + getID()
              + "] on the resource ["
              + m_resource.getResourcePath()
              + "].";
            IOException e = new IOException( error );
            e.initCause( ioe );
            throw e;
        }
    }
    
    public Artifact getLinkArtifact()
    {
        String id = getID();
        String group = m_resource.getGroupName();
        String name = getName();
        
        try
        {
            String spec = "link:" + id;
            if( null != group )
            {
                spec = spec + ":" + group + "/" + name;
            }
            else
            {
                spec = spec + ":" + name;
            }
            return Artifact.createArtifact( spec );
        }
        catch( Throwable e )
        {
            final String error = 
              "Failed to construct link artifact for resource ["
              + m_resource.getResourcePath()
              + "].";
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Return a filename using the layout strategy employed by the cache.
    * @return the path
    */
    public String getLayoutPath()
    {
        Artifact artifact = getArtifact();
        return Transit.getInstance().getCacheLayout().resolveFilename( artifact );
    }
}
