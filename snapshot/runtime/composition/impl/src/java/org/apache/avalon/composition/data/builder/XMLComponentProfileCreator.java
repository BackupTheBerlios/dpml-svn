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

import java.util.ArrayList;

import org.apache.avalon.logging.data.CategoriesDirective;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.data.ComponentProfile;
import org.apache.avalon.composition.data.ConstructorDirective;
import org.apache.avalon.composition.data.ContextDirective;
import org.apache.avalon.composition.data.DependencyDirective;
import org.apache.avalon.composition.data.EntryDirective;
import org.apache.avalon.composition.data.ImportDirective;
import org.apache.avalon.composition.data.Mode;
import org.apache.avalon.composition.data.Parameter;
import org.apache.avalon.composition.data.SelectionDirective;
import org.apache.avalon.composition.data.StageDirective;

import org.apache.avalon.meta.info.InfoDescriptor;

import org.apache.avalon.util.configuration.ConfigurationUtil;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLComponentProfileCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class XMLComponentProfileCreator extends XMLProfileCreator
{
   /**
    * Creation of a {@link ComponentProfile} from an XML configuration.
    *
    * @param config the configuration instance describing the component deployment scenario 
    * @return the deployment profile
    */
    public ComponentProfile createComponentProfile( Configuration config )
      throws Exception
    {
        String classname = config.getAttribute( "class", null );
        if( null == classname )
        {
            String c = ConfigurationUtil.list( config );
            String error = 
              "Missing 'class' attribute in component declaration:\n" + c;
            throw new ConfigurationException( error );
        }
        return createComponentProfile( null, classname, config );
    }

   /**
    * Creation of a {@link ComponentProfile} from an XML configuration.
    *
    * @param base the default name
    * @param config the configuration describing the component deployment scenario 
    * @return the deployment profile
    */
    public ComponentProfile createComponentProfile( 
      String base, String classname, Configuration config )
      throws Exception
    {
        final String name = getName( base, config, "untitled" );
        return createComponentProfile( classname, config, name );
    }

   /**
    * Creation of a {@link ComponentProfile} from an XML configuration.
    *
    * @param classname the name of the class identifying the underlying component type
    * @param config the configuration describing the component deployment scenario 
    * @return the deployment profile
    */
    public ComponentProfile createComponentProfile( 
      String classname, Configuration config, String name )
      throws Exception
    {
        final int activation = 
          getActivationDirective( config, DeploymentProfile.DEFAULT );
        final int collection = getCollectionPolicy( config );

        final CategoriesDirective categories = 
          getCategoriesDirective( config.getChild( "categories", false ) );
        final ContextDirective context = 
          getContextDirective( config.getChild( "context", false ) );
        final DependencyDirective[] dependencies = 
          getDependencyDirectives( config.getChild( "dependencies" ) );
        final StageDirective[] stages = 
          getStageDirectives( config.getChild( "stages" ) );
        final Parameters params = 
          getParameters( config.getChild( "parameters", false ) );
        final Configuration configuration = 
          config.getChild( "configuration", true );

        return new ComponentProfile( 
          name, activation, collection, classname, categories, context, dependencies, 
          stages, params, configuration, Mode.EXPLICIT );
    }

   /**
    * Get the collection policy from a configuration.  If the collection
    * policy is not declared a null is returned indicating that the collection 
    * policy shall default to the component type collection policy. 
    *
    * @param config a configuration fragment holding a collection attribute
    * @return collection policy 
    */
    protected int getCollectionPolicy( Configuration config )
    {
        return InfoDescriptor.getCollectionPolicy( config.getAttribute( "collection", null ) );
    }

    protected DependencyDirective[] getDependencyDirectives( Configuration config )
      throws ConfigurationException
    {
        if( config != null )
        {
            ArrayList list = new ArrayList();
            Configuration[] deps = config.getChildren( "dependency" );
            for( int i=0; i<deps.length; i++ )
            {
                list.add( getDependencyDirective( deps[i] ) );
            }
            return (DependencyDirective[]) list.toArray( new DependencyDirective[0] );
        }
        return new DependencyDirective[0];
    }

    protected DependencyDirective getDependencyDirective( Configuration config )
      throws ConfigurationException
    {
        final String key = config.getAttribute( "key" );
        final String source = config.getAttribute( "source", null );
        if( source != null )
        {
            return new DependencyDirective( key, source );
        }
        else
        {
            Configuration[] children = config.getChildren( "select" );
            ArrayList list = new ArrayList();
            for( int i=0; i<children.length; i++ )
            {
                list.add( getSelectionDirective( children[i] ) );
            }
            SelectionDirective[] features = 
              (SelectionDirective[]) list.toArray( new SelectionDirective[0] );
            return new DependencyDirective( key, features );
        }
    }

    protected StageDirective[] getStageDirectives( Configuration config )
      throws ConfigurationException
    {
        if( config != null )
        {
            ArrayList list = new ArrayList();
            Configuration[] deps = config.getChildren( "stage" );
            for( int i=0; i<deps.length; i++ )
            {
                list.add( getStageDirective( deps[i] ) );
            }
            return (StageDirective[]) list.toArray( new StageDirective[0] );
        }
        return new StageDirective[0];
    }

    protected StageDirective getStageDirective( Configuration config )
      throws ConfigurationException
    {
        final String key = config.getAttribute( "key" );
        final String source = config.getAttribute( "source", null );
        if( source != null )
        {
            return new StageDirective( key, source );
        }
        else
        {
            Configuration[] children = config.getChildren( "select" );
            ArrayList list = new ArrayList();
            for( int i=0; i<children.length; i++ )
            {
                list.add( getSelectionDirective( children[i] ) );
            }
            SelectionDirective[] features = 
              (SelectionDirective[]) list.toArray( new SelectionDirective[0] );
            return new StageDirective( key, features );
        }
    }

    protected SelectionDirective getSelectionDirective( Configuration config )
      throws ConfigurationException
    {
        final String feature = config.getAttribute( "feature" );
        final String value = config.getAttribute( "value" );
        final String match = config.getAttribute( "match", "required" );
        final boolean optional = config.getAttributeAsBoolean( "optional", false );
        return new SelectionDirective( feature, value, match, optional );
    }

    protected Parameters getParameters( Configuration config )
      throws ConfigurationException
    {
        if( config != null )
        {
            return Parameters.fromConfiguration( config );
        }
        return null;
    }

    /**
     * Utility method to create a new context directive.
     *
     * @param config the context directive configuration
     * @return the context directive
     * @throws ConfigurationException if an error occurs
     */
    public ContextDirective getContextDirective( Configuration config )
        throws ConfigurationException
    {
        if( config == null )
        {
            return new ContextDirective( null );
        }

        if( config.getChildren( "import" ).length > 0 )
        {
            final String error = 
              "The 'context' tag format has changed."
              + " Please check Merlin home for details "
              + "http://avalon.apache.org/merlin";
            throw new ConfigurationException( error );
        }

        final String classname = config.getAttribute( "class", null );
        EntryDirective[] entries = getEntries( config.getChildren( "entry" ) );
        return new ContextDirective( classname, entries );
    }

    /**
     * Utility method to create a set of entry directives.
     *
     * @param configs the entry directive configurations
     * @return the entry directives
     * @throws ConfigurationException if an error occurs
     */
    protected EntryDirective[] getEntries( Configuration[] configs )
        throws ConfigurationException
    {
        ArrayList list = new ArrayList();
        for( int i = 0; i < configs.length; i++ )
        {
            Configuration conf = configs[ i ];
            EntryDirective entry = getEntry( conf );
            list.add( entry );
        }
        return (EntryDirective[])list.toArray( new EntryDirective[ 0 ] );
    }

    /**
     * Utility method to create a single entry directive.
     *
     * @param conf the entry directive configuration
     * @return the entry directive
     * @throws ConfigurationException if an error occurs
     */
    protected EntryDirective getEntry( Configuration conf )
        throws ConfigurationException
    {
        final String key = conf.getAttribute( "key" );
        final String classname = conf.getAttribute( "class", null );
        if( ( null != classname ) || ( null != conf.getValue( null ) ) )
        {
            String value = conf.getValue( null );
            if( null != value )
            {
                return new ConstructorDirective( 
                  key, classname, value );
            } 
            else
            {
                Configuration[] configs = conf.getChildren( "param" );
                Parameter[] params = getParameters( configs );
                return new ConstructorDirective( key, classname, params );
            }
        }
        else
        {
            //
            // its using the legacy constructor and import approach
            //

            final Configuration[] children = conf.getChildren();
            if( children.length != 1 )
            {
                final String error = 
                  "Entry '" + key + "' does not contain one child element.";
                throw new ConfigurationException( error );
            }

            Configuration child = children[0];
            final String name = child.getName();
            if( name.equals( "import" ) )
            {
                final String importKey = child.getAttribute( "key" );
                return new ImportDirective( key, importKey );
            }
            else if( name.equals( "constructor" ) )
            {
                final String constructorClassname = 
                  child.getAttribute( "class", "java.lang.String" );
                Configuration[] paramsConf = child.getChildren( "param" );
                if( paramsConf.length > 0 )
                {
                    Parameter[] params = getParameters( paramsConf );
                    return new ConstructorDirective( key, constructorClassname, params );
                }
                else
                {
                    return new ConstructorDirective( 
                      key, constructorClassname, (String) child.getValue( null ) );
                }
            }
            else
            {
                final String error = 
                  "Entry child unrecognized: " + name;
                throw new ConfigurationException( error );
            }
        }
    }


    /**
     * Utility method to create a set of parameter directive.
     *
     * @param configs the parameter directive configurations
     * @return the parameter directives
     * @throws ConfigurationException if an error occurs
     */
    protected Parameter[] getParameters( Configuration[] configs )
        throws ConfigurationException
    {
        ArrayList list = new ArrayList();
        for( int i = 0; i < configs.length; i++ )
        {
            Parameter parameter = getParameter( configs[ i ] );
            list.add( parameter );
        }
        return (Parameter[])list.toArray( new Parameter[ 0 ] );
    }

    /**
     * Utility method to create a new parameter directive.
     *
     * @param config the parameter directive configuration
     * @return the parameter directive
     * @throws ConfigurationException if an error occurs
     */
    protected Parameter getParameter( Configuration config )
        throws ConfigurationException
    {
        String classname = config.getAttribute( "class", "java.lang.String" );
        Configuration[] params = config.getChildren( "param" );
        if( params.length == 0 )
        {
            String value = config.getValue( null );
            return new Parameter( classname, value );
        }
        else
        {
            Parameter[] parameters = getParameters( params );
            return new Parameter( classname, parameters );
        }
    }
}
