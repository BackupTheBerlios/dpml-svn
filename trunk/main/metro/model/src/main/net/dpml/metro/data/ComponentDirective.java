/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

import java.io.IOException;
import java.net.URI;

import net.dpml.component.ActivationPolicy;
import net.dpml.component.Directive;

import net.dpml.metro.info.Composite;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;

//import net.dpml.metro.builder.ComponentDecoder;

import net.dpml.lang.Part;

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

&lt;/component&gt;
</pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentDirective extends Composite implements Comparable, Directive
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------
    
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private static final CategoriesDirective EMPTY_CATEGORIES = 
      new CategoriesDirective();
      
    //private static final ComponentDecoder DECODER = new ComponentDecoder();
    
    /*
    private static PartReference[] mergePartReferences( 
      PartReference[] parts, ComponentDirective directive )
    {
        if( null == directive )
        {
            return parts;
        }
        else if( null == parts )
        {
            return directive.getPartReferences();
        }
        else
        {
            Hashtable table = new Hashtable();
            PartReference[] references = directive.getPartReferences();
            for( int i=0; i<references.length; i++ )
            {
                PartReference ref = references[i];
                String key = ref.getKey();
                table.put( key, ref );
            }
            for( int i=0; i<parts.length; i++ )
            {
                PartReference ref = parts[i];
                String key = ref.getKey();
                table.put( key, ref );
            }
            PartReference[] refs = (PartReference[]) table.values().toArray( new PartReference[0] );
            Arrays.sort( refs );
            return refs;
        }
    }
    */
    
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
    * The components context directive.
    */
    private final ContextDirective m_context;

   /**
    * The name of the component profile. This is an
    * abstract name used during assembly.
    */
    private final String m_name;

   /**
    * The activation policy.
    */
    private final ActivationPolicy m_activation;

   /**
    * Logging category directives.
    */
    private final CategoriesDirective m_categories;

   /**
    * Base directive uri.
    */
    private final URI m_uri;

   /**
    * Base directive.
    */
    private final DefaultComposition m_base;

    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------

   /**
    * Creation of a new profile.
    *
    * @param name the name to assign to the component deployment scenario
    * @param classname the classname of the component type
    * @exception IOException if an IO exception occurs
    */
    public ComponentDirective( final String name, final String classname ) throws IOException
    {
        this(
          name, 
          ActivationPolicy.SYSTEM, 
          CollectionPolicy.SYSTEM, 
          LifestylePolicy.SYSTEM,
          classname, 
          null, null, null, null );
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
    * @param parts the component internal parts
    * @param uri URI of the component super-definition
    * @exception IOException if an IO exception occurs
    */
    public ComponentDirective(
           final String name,
           final ActivationPolicy activation,
           final CollectionPolicy collection,
           final LifestylePolicy lifestyle,
           final String classname,
           final CategoriesDirective categories,
           final ContextDirective context,
           final PartReference[] parts,
           final URI uri ) throws IOException
    {
        super( parts );
        
        if( null == activation )
        {
            if( null != uri )
            {
                m_activation = null;
            }
            else
            {
                m_activation = ActivationPolicy.SYSTEM;
            }
        }
        else
        {
            m_activation = activation;
        }
        
        if( null == categories )
        {
            if( null != uri )
            {
                m_categories = null;
            }
            else
            {
                m_categories = EMPTY_CATEGORIES;
            }
        }
        else
        {
            m_categories = categories;
        }
        
        if( null == classname )
        {
            if( null != uri )
            {
                m_classname = null;
            }
            else
            {
                m_classname = Object.class.getName();
            }
        }
        else
        {
            m_classname = classname;
        }
        
        if( null == context )
        {
            if( null != uri )
            {
                m_context = null;
            }
            else
            {
                m_context = new ContextDirective( new PartReference[0] );
            }
        }
        else
        {
            m_context = context;
        }
        
        if( null == lifestyle )
        {
            if( null != uri )
            {
                m_lifestyle = null;
            }
            else
            {
                m_lifestyle = LifestylePolicy.SYSTEM;
            }
        }
        else
        {
            m_lifestyle = lifestyle;
        }

        if( null == collection )
        {
            if( null != uri )
            {
                m_collection = null;
            }
            else
            {
                m_collection = CollectionPolicy.SYSTEM;
            }
        }
        else
        {
            m_collection = collection;
        }
        
        m_uri = uri;
        
        if( null != uri )
        {
            Part part = Part.load( uri );
            if( part instanceof DefaultComposition )
            {
                m_base = (DefaultComposition) part;
            }
            else
            {
                final String error = 
                  "Base part class is not recognized."
                  + "\nClass: " + part.getClass().getName();
                throw new IllegalStateException( error );
            }
        }
        else
        {
            m_base = null;
        }
        
        if( null == name )
        {
            if( null != uri )
            {
                m_name = null;
            }
            else
            {
                m_name = toName( m_classname );
            }
        }
        else
        {
            validateName( name );
            m_name = name;
        }
    }
    
    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    /**
     * Return the profile name.
     *
     * @return the name of the component.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the logging categories for the profile.
     *
     * @return the categories
     */
    public CategoriesDirective getCategoriesDirective()
    {
        return m_categories;
    }

   /**
    * Get the activation policy for the profile.
    *
    * @return the declared activation policy
    * @see ActivationPolicy#SYSTEM
    * @see ActivationPolicy#STARTUP
    * @see ActivationPolicy#DEMAND
    */
    public ActivationPolicy getActivationPolicy()
    {
        return m_activation;
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
    * Return the base directive uri.
    *
    * @return the uri of the base directive
    */
    public URI getBaseURI()
    {
        return m_uri;
    }

   /**
    * Return the base directive.
    *
    * @return the base directive
    */
    public ComponentDirective getBaseDirective()
    {
        if( null == m_base )
        {
            return null;
        }
        else
        {
            return m_base.getComponentDirective();
        }
    }

   /**
    * Return the base part.
    *
    * @return the base part
    */
    public DefaultComposition getBasePart()
    {
        return m_base;
    }

   /**
    * Returns a string representation of the profile.
    * @return a string representation
    */
    public String toString()
    {
        if( null != m_name )
        {
            return "[" + getName() + "]";
        }
        else
        {
            return "[unknown]";
        }
    }

   /**
    * Compare this object with the supplied object.
    * @param object the object to compare with
    * @return the result
    */
    public int compareTo( Object object )
    {
        String name = this.toString();
        String other = object.toString();
        return name.compareTo( other );
    }
    
   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        if( !( other instanceof ComponentDirective ) )
        {
            return false;
        }
        ComponentDirective profile = (ComponentDirective) other;
        if( !equals( m_name, profile.getName() ) )
        {
            return false;
        }
        else if( !equals( m_activation, profile.getActivationPolicy() ) )
        {
            return false;
        }
        else if( !equals( m_categories, profile.getCategoriesDirective() ) )
        {
            return false;
        }
        else if( !equals( m_classname, profile.getClassname() ) )
        {
            return false;
        }
        else if( !equals( m_context, profile.getContextDirective() ) )
        {
            return false;
        }
        else if( !equals( m_collection, profile.getCollectionPolicy() ) )
        {
            return false;
        }
        else if( !equals( m_lifestyle, profile.getLifestylePolicy() ) )
        {
            return false;
        }
        else
        {
            return equals( m_uri, profile.getBaseURI() );
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashValue( m_name );
        hash ^= hashValue( m_activation );
        hash ^= hashValue( m_categories );
        hash ^= hashValue( m_classname );
        hash ^= hashValue( m_context );
        hash ^= hashValue( m_collection );
        hash ^= hashValue( m_lifestyle );
        hash ^= hashValue( m_uri );
        return hash;
    }

   /**
    * Internal utility to get the name of the class without the package name. Used
    * when constructing a default component name.
    * @param classname the fully qualified classname
    * @return the short class name without the package name
    */
    private String toName( String classname )
    {
        int i = classname.lastIndexOf( "." );
        if( i == -1 )
        {
            return classname.toLowerCase();
        }
        else
        {
            return classname.substring( i + 1, classname.length() ).toLowerCase();
        }
    }
    
    private void validateName( final String name )
    {
        if( name.indexOf( " " ) > 0 || name.indexOf( "." ) > 0 || name.indexOf( "," ) > 0
          || name.indexOf( "/" ) > 0 )
        {
            final String error = 
              "Directive name ["
              + name
              + "] contains an illegal character (' ', ',', '/', or '.')";
            throw new IllegalArgumentException( error );
        }
        else if( name.length() == 0 )
        {
            final String error = 
              "Directive name [] is insufficient.";
            throw new IllegalArgumentException( error );
        }
    }
}
