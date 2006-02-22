/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.library.util;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

import net.dpml.library.model.Type;
import net.dpml.library.model.Resource;

import net.dpml.lang.Plugin;
import net.dpml.lang.DefaultPlugin;
import net.dpml.lang.Strategy;
import net.dpml.lang.Category;
import net.dpml.lang.Classpath;
import net.dpml.lang.DefaultClasspath;
import net.dpml.lang.DefaultStrategy;

import net.dpml.transit.Artifact;

/**
 * Interface implemented by plugins that provide plugin building functionality.
 * Implementations that load plugin factoryies must supply the target Resource
 * as a plugin constructor argument.  Factory implementation shall construct 
 * plugin defintions using the supplied resource as the reference for the 
 * classpath dependencies.  Suppliementary properties may be aquired using 
 * the Type returned from the Resource.getType( "plugin" ) operation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultPluginFactory implements PluginFactory
{
   /**
    * Constant artifact type for a plugin.
    */
    public static final String TYPE = "plugin";

   /**
    * Property key used to identify the plugin title.
    */
    public static final String PLUGIN_TITLE_KEY = "project.plugin.title";
    
   /**
    * Property key used to identify the plugin description.
    */
    public static final String PLUGIN_DESCRIPTION_KEY = "project.plugin.description";
    
   /**
    * Property key used to identify a custom plugin handler classname.
    */
    public static final String PLUGIN_HANDLER_KEY = "project.plugin.handler";
    
   /**
    * Default runtime plugin handler classname.
    */
    public static final String STANDARD_PLUGIN_HANDLER = "net.dpml.transit.StandardHandler";
    
   /**
    * Build the plugin definition.
    * @exception exception if a build related error occurs
    */
    public Plugin build( File dir, Resource resource ) throws Exception
    {
        URI uri = getPluginURI( resource );
        String title = getTitle( resource );
        String description = getDescription( resource );
        Strategy strategy = getStrategy( dir, resource );
        Classpath classpath = getClasspath( resource );
        return new DefaultPlugin( 
            title, description, uri, strategy, classpath );
    }
    
    protected URI getPluginURI( Resource resource ) throws Exception
    {
        Artifact artifact = resource.getArtifact( TYPE );
        return artifact.toURI();
    }

    protected String getTitle( Resource resource )
    {
        Type type = resource.getType( TYPE );
        return type.getProperty( PLUGIN_TITLE_KEY );
    }

    protected String getDescription( Resource resource )
    {
        Type type = resource.getType( TYPE );
        return type.getProperty( PLUGIN_DESCRIPTION_KEY );
    }

    protected Classpath getClasspath( Resource resource ) throws IOException
    {
        URI[] sysUris = getURIs( resource, Category.SYSTEM );
        URI[] publicUris = getURIs( resource, Category.PUBLIC );
        URI[] protectedUris = getURIs( resource, Category.PROTECTED );
        URI[] privateUris = getURIs( resource, Category.PRIVATE, true );
        return new DefaultClasspath( sysUris, publicUris, protectedUris, privateUris );
    }

    protected Strategy getStrategy( File dir, Resource resource )
    {
        Type type = resource.getType( TYPE );
        Properties properties = getProperties( type );
        String handler = type.getProperty( 
            PLUGIN_HANDLER_KEY, 
            STANDARD_PLUGIN_HANDLER );
        return new DefaultStrategy( handler, properties );
    }
    
    private URI[] getURIs( Resource resource, Category category ) throws IOException
    {
        return getURIs( resource, category, false );
    }
    
    private URI[] getURIs( Resource resource, Category category, boolean self ) throws IOException
    {
        Resource[] resources = resource.getClasspathProviders( category );
        ArrayList list = new ArrayList();
        for( int i=0; i<resources.length; i++ )
        {
            Resource r = resources[i];
            addURI( list, r );
        }
        if( self )
        {
            addURI( list, resource );
        }
        URI[] uris = (URI[]) list.toArray( new URI[0] );
        return uris;
    }
    
    private void addURI( List list, Resource resource )  throws IOException
    {
        if( resource.isa( "jar" ) )
        {
            try
            {
                Artifact artifact = resource.getArtifact( "jar" );
                URI uri = artifact.toURI();
                list.add( uri );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error while attempting to resolve resource.";
                IOException ioe = new IOException( error );
                ioe.initCause( e );
                throw ioe;
            }
        }
    }
    
    private Properties getProperties( Type type )
    {
        Properties properties = new Properties();
        String[] keys = type.getLocalPropertyNames();
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            if( !PLUGIN_HANDLER_KEY.equals( key ) )
            {
                String value = type.getProperty( key );
                properties.setProperty( key, value );
            }
        }
        return properties;
    }
}
