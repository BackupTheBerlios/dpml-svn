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

package net.dpml.composition.data;

import java.net.URI;

import net.dpml.part.PartReference;
import net.dpml.configuration.Configuration;
import net.dpml.composition.info.InfoDescriptor;
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
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ComponentProfile.java 2991 2005-07-07 00:00:04Z mcconnell@dpml.net $
 */
public class ComponentProfile extends DeploymentProfile
{
    /**
     * The collection policy override.
     */
    private int m_collection;

    /**
     * The component lifestyle policy.
     */
    private final String m_lifestyle;

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

    /**
     * The internal parts.
     */
    private final PartReference[] m_parts;

    /**
     * URI of the profile that this profile extends.
     */
    private final URI m_extends;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new profile.
    *
    * @param name the name to assign to the component deployment scenario
    * @param classname the classname of the component type
    */
    public ComponentProfile( final String name, final String classname )
        throws NullPointerException
    {
        this(
          name, 
          DeploymentProfile.DEFAULT, 
          InfoDescriptor.UNDEFINED_COLLECTION, 
          "request", 
          classname, 
          null, null, null, null, null, null, null );
    }

   /**
    * Creation of a new deployment profile using a supplied template profile.
    * @param name the name to assign to the created profile
    * @param template the template deployment profile
    */
    public ComponentProfile( String name, ComponentProfile template )
        throws NullPointerException
    {
        this(
          name,
          template.getActivationPolicy(),
          template.getCollectionPolicy(),
          template.getLifestylePolicy(),
          template.getClassname(),
          template.getCategoriesDirective(),
          template.getContextDirective(),
          template.getParts(),
          template.getParameters(),
          template.getConfiguration(),
          template.getClassLoaderDirective(),
          template.getExtends() );
    }

    public ComponentProfile(
           final String name,
           final int activation,
           final int collection,
           final String lifestyle,
           final String classname,
           final CategoriesDirective categories,
           final ContextDirective context,
           final PartReference[] parts,
           final Parameters parameters,
           final Configuration config,
           final ClassLoaderDirective classloader,
           final URI extendsURI )
        throws NullPointerException
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
            m_context = new ContextDirective();
        }
        else
        {
            m_context = context;
        }

        if( null == lifestyle )
        {
            m_lifestyle = "request";
        }
        else
        {
            m_lifestyle = lifestyle;
        }

        m_collection = collection;
        m_parameters = parameters;
        m_configuration = config;
        m_extends = extendsURI;

        if( null == parts )
        {
            m_parts = new PartReference[0];
        }
        else
        {
            m_parts = parts;
        }
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    /**
     * Return the uri of the profile that this profile extends or null if the
     * profile is standalone.
     *
     * @return the base profile URI
     */
    public URI getExtends()
    {
        return m_extends;
    }

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
    public String getLifestylePolicy()
    {
        return m_lifestyle;
    }

    /**
     * Return the component collection policy.  If null, the component
     * type collection policy will apply.
     *
     * @return a HARD_COLLECTION, WEAK_COLLECTION, SOFT_COLLECTION or UNDEFINED_COLLECTION
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
    public ContextDirective getContextDirective()
    {
        return m_context;
    }

    /**
     * Return the internal parts.
     *
     * @return the array of internal parts.
     */
    public PartReference[] getParts()
    {
        return m_parts;
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
        if( false == super.equals( other ) )
        {
            return false;
        }
        if( false == ( other instanceof ComponentProfile ) )
        {
            return false;
        }
        ComponentProfile profile = (ComponentProfile) other;
        if( false == m_classname.equals( profile.getClassname() ) )
        {
            return false;
        }
        if( false == m_context.equals( profile.getContextDirective() ) )
        {
            return false;
        }
        if( m_collection != profile.getCollectionPolicy() )
        {
            return false;
        }
        if( false == equals( m_parameters, profile.getParameters() ) )
        {
            return false;
        }
        if( false == equals( m_configuration, profile.getConfiguration() ) )
        {
            return false;
        }
        if( getParts().length != profile.getParts().length )
        {
            return false;
        }
        else
        {
            PartReference[] mine = getParts();
            PartReference[] yours = profile.getParts();
            for( int i=0; i<mine.length; i++ )
            {
                PartReference a = mine[i];
                PartReference b = yours[i];
                if( false == a.equals( b ) )
                {
                    return false;
                }
            }
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
        hash ^= m_collection;
        if( false == ( null == m_parameters ) )
        {
            hash ^= m_parameters.hashCode();
        }
        if( false == ( null == m_configuration ) )
        {
            hash ^= m_configuration.hashCode();
        }
        for( int i=0; i<m_parts.length; i++ )
        {
            hash ^= m_parts[i].hashCode();
        }
        return hash;
    }
}
