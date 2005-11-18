/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.data;

import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.part.ActivationPolicy;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;


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
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentDirective extends DeploymentDirective
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------
    
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------
    
   /**
    * The collection policy override.
    */
    private final CollectionPolicy m_collection;

   /**
    * The component lifestyle policy.
    */
    private final LifestylePolicy m_lifestyle;

   /**
    * The component classname.
    */
    private final String m_classname;

   /**
    * The parameters for component (if any).
    */
    private final Parameters m_parameters;

   /**
    * The configuration for component (if any).
    */
    private final Configuration m_configuration;

   /**
    * The components context directive.
    */
    private final ContextDirective m_context;

    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------

   /**
    * Creation of a new profile.
    *
    * @param name the name to assign to the component deployment scenario
    * @param classname the classname of the component type
    */
    public ComponentDirective( final String name, final String classname )
    {
        this(
          name, 
          ActivationPolicy.SYSTEM, 
          CollectionPolicy.SYSTEM, 
          LifestylePolicy.TRANSIENT,
          classname, 
          null, null, null, null, null );
    }

   /**
    * Creation of a new deployment profile using a supplied template profile.
    * @param name the name to assign to the created profile
    * @param template the template deployment profile
    */
    public ComponentDirective( String name, ComponentDirective template )
    {
        this(
          name,
          template.getActivationPolicy(),
          template.getCollectionPolicy(),
          template.getLifestylePolicy(),
          template.getClassname(),
          template.getCategoriesDirective(),
          template.getContextDirective(),
          template.getParameters(),
          template.getConfiguration(),
          template.getClassLoaderDirective() );
    }

   /**
    * Creation of a new deployment profile.
    * @param name the name to assign to the created profile
    * @param activation the component activation policy
    * @param collection the component garbage collection policy
    * @param lifestyle the component lifestyle policy
    * @param classname the component classname
    * @param categories logging categories
    * @param context context directive
    * @param parameters the default parameters
    * @param config the default configuration
    * @param classloader the component classloader directive
    */
    public ComponentDirective(
           final String name,
           final ActivationPolicy activation,
           final CollectionPolicy collection,
           final LifestylePolicy lifestyle,
           final String classname,
           final CategoriesDirective categories,
           final ContextDirective context,
           final Parameters parameters,
           final Configuration config,
           final ClassLoaderDirective classloader )
    {
        super( name, activation, categories, classloader );

        if( null == classname )
        {
            m_classname = Object.class.getName();
        }
        else
        {
            m_classname = classname;
        }

        if( null == context )
        {
            m_context = new ContextDirective( new PartReference[0] );
        }
        else
        {
            m_context = context;
        }

        m_lifestyle = lifestyle;
        m_collection = collection;
        m_parameters = parameters;
        m_configuration = config;
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
    * Return the component lifestyle policy.
    *
    * @return the lifestyle policy value
    */
    public LifestylePolicy getLifestylePolicy()
    {
        return m_lifestyle;
    }

   /**
    * Return the component collection policy.  If null, the component
    * type collection policy will apply.
    *
    * @return a HARD, WEAK, SOFT or SYSTEM
    */
    public CollectionPolicy getCollectionPolicy()
    {
        return m_collection;
    }

   /**
    * Return the context directive for the profile.
    *
    * @return the ContextDirective for the profile.
    */
    public ContextDirective getContextDirective()
    {
        return m_context;
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

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        if( !super.equals( other ) )
        {
            return false;
        }
        if( !( other instanceof ComponentDirective ) )
        {
            return false;
        }
        ComponentDirective profile = (ComponentDirective) other;
        if( !m_classname.equals( profile.getClassname() ) )
        {
            return false;
        }
        if( !m_context.equals( profile.getContextDirective() ) )
        {
            return false;
        }
        if( !equals( m_collection, profile.getCollectionPolicy() ) )
        {
            return false;
        }
        if( !equals( m_lifestyle, profile.getLifestylePolicy() ) )
        {
            return false;
        }
        if( !equals( m_parameters, profile.getParameters() ) )
        {
            return false;
        }
        if( !equals( m_configuration, profile.getConfiguration() ) )
        {
            return false;
        }
        return true;
    }

    private boolean equals( Object a, Object b )
    {
        if( null == a )
        {
            return ( null == b );
        }
        else
        {
            return a.equals( b );
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_classname.hashCode();
        hash ^= m_context.hashCode();
        if( null != m_collection )
        {
            hash ^= m_collection.hashCode();
        }
        if( null != m_lifestyle )
        {
            hash ^= m_lifestyle.hashCode();
        }
        if( !( null == m_parameters ) )
        {
            hash ^= m_parameters.hashCode();
        }
        if( !( null == m_configuration ) )
        {
            hash ^= m_configuration.hashCode();
        }
        return hash;
    }
}
