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

package org.apache.avalon.composition.data.builder;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.logging.data.CategoriesDirective;
import org.apache.avalon.logging.data.CategoryDirective;

import org.apache.avalon.composition.data.DeploymentProfile;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.util.configuration.ConfigurationUtil;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLProfileCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class XMLProfileCreator
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( XMLProfileCreator.class );

   /**
    * Get the profile name.
    *
    * @param config a configuration fragment describing the profile.
    */
    protected String getName( 
      final String base, final Configuration config, final String defaultName )
    {
        final String name = config.getAttribute( "name", defaultName );
        if( base == null )
        {
            return name;
        }
        else
        {
            return base + "-" + name;
        }
    }

   /**
    * Get the activation policy from a configuration. If no activation attribute
    * is present the value return defaults to FALSE (i.e. activation is deferred).
    *
    * @param config a configuration fragment holding a activation attribute
    * @return TRUE is the value of the activation attribute is 'true' or 'startup'
    *   otherwise the return value is FALSE
    */
    protected int getActivationDirective( Configuration config )
    {
        return getActivationDirective( config, DeploymentProfile.DEFAULT );
    }

   /**
    * Get the activation policy from a configuration. 
    *
    * @param config a configuration fragment holding a activation attribute
    * @param fallback the default policy
    * @return activation policy
    */
    protected int getActivationDirective( Configuration config, int fallback )
    {
        final String value = config.getAttribute( "activation", null );
        if( value == null )
        {
            return fallback;
        }

        final String string = value.toLowerCase().trim();
        if( string.equals( "startup" ) || string.equals( "true" ) )
        {
            return DeploymentProfile.ENABLED;
        }
        else if( string.equals( "lazy" ) || string.equals( "false" ) )
        {
            return DeploymentProfile.DISABLED;
        }
        else
        {
            return DeploymentProfile.DEFAULT;
        }
    }

   /**
    * Creation of a single categories directive instance from a supplied
    * configuration fragment.
    * @param config the configuration fragment
    * @return the categories directive
    */
    public CategoriesDirective getCategoriesDirective( Configuration config )
      throws ConfigurationException
    {
        if( null == config )
        {
            return null;
        }

        final String name = config.getAttribute( "name", "." );
        final String priority = config.getAttribute( "priority", null );
        final String target = config.getAttribute( "target", null );
        final Configuration[] children = config.getChildren();
        final CategoryDirective[] categories = 
          getCategoryDirectives( children );

        return new CategoriesDirective( name, priority, target, categories );
    }

    public CategoryDirective getCategoryDirective( Configuration config )
      throws ConfigurationException
    {
        try
        {
            final String name = config.getAttribute( "name" );
            final String priority = config.getAttribute( "priority", null );
            final String target = config.getAttribute( "target", null );
            return new CategoryDirective( name, priority, target );
        }
        catch( ConfigurationException e )
        {
            final String error = 
              "Invalid category descriptor."
              + ConfigurationUtil.list( config );
            throw new ConfigurationException( error, e );
        }
    }

    private CategoryDirective[] getCategoryDirectives( Configuration[] children )
      throws ConfigurationException
    {
        final CategoryDirective[] categories = 
          new CategoryDirective[ children.length ];
        for( int i = 0; i<children.length; i++ )
        {
            Configuration config = children[i];
            final String name = config.getName();
            if( name.equals( "category" ) )
            {
                categories[i] = getCategoryDirective( config );
            }
            else if( name.equals( "categories" ) )
            {
                categories[i] = getCategoriesDirective( config );
            }
            else
            {
                final String error = 
                  "Nested element [" + name + "] not recognized.";
                throw new ConfigurationException( error );
            }
        }
        return categories;
    }
}
