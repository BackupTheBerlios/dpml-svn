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

package org.apache.avalon.composition.data;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.meta.info.InfoDescriptor;

import org.apache.avalon.logging.data.CategoriesDirective;


/**
 * Definition of the criteria for an explicit component profile.  A profile, when
 * included within the scope of a container declaration will be instantiated in
 * the model as an EXPLICIT component profile resulting in the initiation of
 * dependency resolution relative to the component as the target deployment
 * objective.  Multiple supplementary profiles may be packaged in a .xprofiles
 * resources and will be assigned to the container automatically.  In the absence
 * of explicit or packaged profile directives, an implicit profile will be created
 * for any component types declared under a jar manifest.
 *
 * <p><b>XML</b></p>
 * <p>A component element declares the profile to be applied during the instantiation
 * of a component type.  It includes a name and class declaration, logging directives
 * (resolved relative to the component's container), context creation criteria,
 * together with configuration or parameters information.</p>
 *
 * <pre>
 <font color="gray"><i>&lt;!--
 Declaration of the services hosted by this container.  Service container here
 will be managed relative to other provider components at the same level and
 may be serviced by components declared in parent container.
 --&gt;</i></font>

&lt;component name="<font color="darkred">complex</font>" class="<font color="darkred">org.apache.avalon.playground.ComplexComponent</font>" activation="<font color="darkred">startup</font>"&gt;

  <font color="gray"><i>&lt;!--
  Priority and target assignments for component specific logging categrories.
  --&gt;</i></font>

  &lt;categories priority="<font color="darkred">DEBUG</font>"&gt;
    &lt;category name="<font color="darkred">init</font>" priority="<font color="darkred">DEBUG</font>" /&gt;
  &lt;/categories&gt;

  <font color="gray"><i>&lt;!--
  Context entry directives are normally only required in the case where the component
  type declares a required context type and entry values. Generally speaking, a component
  will normally qualify it's instantiation criteria through a configuration declaration.
  Any context values defined at this level will override context values supplied by the
  container.  The following two context directives for "location" and "home" demonstrate
  programatics creation of context values.  The first entry declares that the context
  value to be assigned to the key "location" shall be the String value "Paris".  The second
  context enty assignes the container's context value for "urn:avalon:home" to the component's
  context key of "home".
  --&gt;</i></font>

  &lt;context&gt;
    &lt;entry key="<font color="darkred">location</font>"&gt;<font color="darkred">Paris</font>&lt;/entry&gt;
    &lt;include name="<font color="darkred">urn:avalon:home</font>" key="<font color="darkred">home</font>"/&gt;
  &lt;/context&gt;

  <font color="gray"><i>&lt;!--
  Apply the following configuration when instantiating the component.  This configuration
  will be applied as the primary configuration in a cascading configuration chain.  A
  type may declare a default configuration under a "classname".xconfig file that will be
  used to dereference any configuration requests not resolvable by the configuration
  supplied here.
  --&gt;</i></font>

  &lt;configuration&gt;
    &lt;message value="<font color="darkred">Hello</font>"/&gt;
  &lt;/configuration&gt;

  <font color="gray"><i>&lt;!--
  The parameterization criteria from this instance of the component type.
  --&gt;</i></font>

  &lt;parameters/&gt;

&lt;/component&gt;
</pre>
 *
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ComponentProfile.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ComponentProfile extends DeploymentProfile
{
    /**
     * The assigned logging categories.
     */
    private CategoriesDirective m_categories;

    /**
     * The collection policy override.
     */
    private int m_collection;

    /**
     * The component classname.
     */
    private String m_classname;

    /**
     * The parameters for component (if any).
     */
    private Parameters m_parameters;

    /**
     * The configuration for component (if any).
     */
    private Configuration m_configuration;

    /**
     * The configuration for component (if any).
     */
    private ContextDirective m_context;

    /**
     * The dependency directives.
     */
    private DependencyDirective[] m_dependencies;

    /**
     * The stage directives.
     */
    private StageDirective[] m_stages;

    /**
     * The creation mode.
     */
    private Mode m_mode;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new profile using IMPLICT mode and LIBERAL collection
    * policies.
    *
    * @param name the name to assign to the component deployment scenario
    * @param classname the classname of the component type
    */
    public ComponentProfile( 
           final String name, 
           final String classname )
    {
        this( 
          name, DeploymentProfile.DEFAULT, InfoDescriptor.UNDEFINED, classname, null, null, null, null, 
          null, null, Mode.IMPLICIT );
    }

   /**
    * Creation of a new deployment profile using a supplied template profile.
    * @param name the name to assign to the created profile
    * @param template the template deployment profile
    */
    public ComponentProfile( String name, ComponentProfile template )
    {
        this( 
          name, 
          template.getActivationDirective(),
          template.getCollectionPolicy(),
          template.m_classname,
          template.m_categories,
          template.m_context,
          template.m_dependencies,
          template.m_stages,
          template.m_parameters,
          template.m_configuration,
          Mode.EXPLICIT );
    }

    public ComponentProfile( 
           final String name, 
           final int activation, 
           final int collection, 
           final String classname, 
           final CategoriesDirective categories, 
           final ContextDirective context, 
           final DependencyDirective[] dependencies,
           final StageDirective[] stages,
           final Parameters parameters, 
           final Configuration config,
           final Mode mode )
    {
        super( name, activation, mode, categories );

        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }

        m_collection = collection;
        m_classname = classname;
        m_categories = categories;
        m_context = context;
        m_parameters = parameters;
        m_configuration = config;

        if( null == dependencies )
        {
            m_dependencies = new DependencyDirective[0];
        }
        else
        {
            m_dependencies = dependencies;
        }

        if( null == stages )
        {
            m_stages = new StageDirective[0];
        }
        else
        {
            m_stages = stages;
        }
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    /**
     * Return the component type classname.
     *
     * @return classname of the component type
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the component collection policy.  If null, the component
     * type collection policy will apply.
     *
     * @return a HARD, WEAK, SOFT or UNDEFINED
     */
    public int getCollectionPolicy()
    {
        return m_collection;
    }

    /**
     * Return the context directive for the profile.
     *
     * @return the ContextDirective for the profile.
     */
    public ContextDirective getContext()
    {
        return m_context;
    }

    /**
     * Return the dependency directives.
     *
     * @return the set of DependencyDirective statements for the profile.
     */
    public DependencyDirective[] getDependencyDirectives()
    {
        return m_dependencies;
    }

    /**
     * Return the dependency directive for a supplied key.
     *
     * @return the matching DependencyDirective (possibly null if 
     *   no directive is declared for the given key)
     */
    public DependencyDirective getDependencyDirective( final String key )
    {
        DependencyDirective[] directives = getDependencyDirectives();
        for( int i=0; i<directives.length; i++ )
        {
            DependencyDirective directive = directives[i];
            if( directive.getKey().equals( key ) )
            {
                return directive;
            }
        }
        return null;
    }

    /**
     * Return the stage directives.
     *
     * @return the set of StageDirective statements for the profile.
     */
    public StageDirective[] getStageDirectives()
    {
        return m_stages;
    }

    /**
     * Return the dependency directive for a supplied key.
     *
     * @return the matching DependencyDirective (possibly null if 
     *   no directive is declared for the given key)
     */
    public StageDirective getStageDirective( final String key )
    {
        StageDirective[] directives = getStageDirectives();
        for( int i=0; i<directives.length; i++ )
        {
            StageDirective directive = directives[i];
            if( directive.getKey().equals( key ) )
            {
                return directive;
            }
        }
        return null;
    }

    /**
     * Return the Parameters for the profile.
     *
     * @return the Parameters for Component (if any).
     */
    public Parameters getParameters()
    {
        return m_parameters;
    }

    /**
     * Return the base Configuration for the profile.  The implementation
     * garantees that the supplied configuration is not null.
     *
     * @return the base Configuration for profile.
     */
    public Configuration getConfiguration()
    {
        return m_configuration;
    }

    /**
     * Returns a string representation of the profile.
     * @return a string representation
     */
    public String toString()
    {
        return "[" + getName() + "]";
    }
}
