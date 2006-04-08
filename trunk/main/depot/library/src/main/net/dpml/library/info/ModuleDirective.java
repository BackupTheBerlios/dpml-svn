/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.library.info;

import java.util.Arrays;
import java.util.Properties;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ModuleDirective extends ResourceDirective
{
    private final ResourceDirective[] m_resources;
    
   /**
    * Creation of a new module directive supporting the establishment
    * of an anonymous reesource.
    *
    * @param name the module name
    * @param resource resource contained within the module
    */
    public ModuleDirective( String name, ResourceDirective resource )
    {
        this(
          name, null, Classifier.ANONYMOUS, null,
          new TypeDirective[0], new DependencyDirective[0],
          new ResourceDirective[]{resource}, null, null );
    }
    
   /**
    * Creation of a new module directive.
    * @param resource the abstract resource definition
    * @param resources resource contained within the module
    */
    public ModuleDirective(
      ResourceDirective resource, ResourceDirective[] resources )
    {
        this( 
          resource.getName(), 
          resource.getVersion(), 
          resource.getClassifier(), 
          resource.getBasedir(), 
          resource.getTypeDirectives(), 
          resource.getDependencyDirectives(), 
          resources, 
          resource.getProperties(),
          resource.getFilterDirectives() );
    }
    
   /**
    * Creation of a new module directive.
    * @param name the resource name
    * @param version the resource version
    * @param classifier LOCAL or EXTERNAL classifier
    * @param basedir the project basedir
    * @param types types produced by the resource
    * @param dependencies resource dependencies
    * @param resources resource included within the module
    * @param properties suppliementary properties
    */
    public ModuleDirective(
      String name, String version, Classifier classifier, String basedir, TypeDirective[] types,
      DependencyDirective[] dependencies, ResourceDirective[] resources, Properties properties, FilterDirective[] filters )
    {
        super( name, version, classifier, basedir, types, dependencies, properties, filters );
        if( null == resources )
        {
            throw new NullPointerException( "resources" );
        }
        for( int i=0; i<resources.length; i++ )
        {
            if( null == resources[i] )
            {
                throw new NullPointerException( "resource [" + i + "]" );
            }
        }
        m_resources = resources;
    }
    
   /**
    * Return an array of resource directives representing the resources within 
    * the module.
    * @return the nested resource directives
    */
    public ResourceDirective[] getResourceDirectives()
    {
        return m_resources;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ModuleDirective ) )
        {
            ModuleDirective object = (ModuleDirective) other;
            return Arrays.equals( m_resources, object.m_resources );
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashArray( m_resources );
        return hash;
    }
}
