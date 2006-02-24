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

package net.dpml.metro.builder;

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

import net.dpml.transit.Artifact;

import net.dpml.library.util.DefaultPluginFactory;

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
public class ComponentPluginFactory extends DefaultPluginFactory
{
   /**
    * Build the plugin definition for the supplied resource.
    * @exception exception if a build related error occurs
    */
    public Plugin build( File dir, Resource resource ) throws Exception
    {
        URI uri = getPluginURI( resource );
        String title = getTitle( resource );
        String description = getDescription( resource );
        Strategy strategy = getStrategy( dir, resource );
        Classpath classpath = getClasspath( resource );
        return new ComponentPlugin( 
            title, description, uri, strategy, classpath );
    }
    
    protected Strategy getStrategy( File dir, Resource resource )
    {
        Type type = resource.getType( "plugin" );
        String source = type.getProperty( "project.plugin.srcfile", "plugin.xml" );
        File src = new File( dir, source );
        if( src.exists() )
        {
            System.out.println( "## WE HAVE A DEFINITION: " + src );
            return super.getStrategy( dir, resource );
        }
        else
        {
            System.out.println( "## NO DEFINITION: " + src );
            return super.getStrategy( dir, resource );
        }
    }
}
