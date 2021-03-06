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

package net.dpml.library;

import java.io.File;

import net.dpml.library.info.Scope;
import net.dpml.library.info.ResourceDirective.Classifier;

import net.dpml.transit.Artifact;

import net.dpml.lang.Category;
import net.dpml.lang.Version;

import net.dpml.util.Resolver;

/**
 * The Resource interface describes infomation about a published resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Resource extends Dictionary, Resolver
{
   /**
    * Return the singleton library.
    * @return the library
    */
    Library getLibrary();

   /**
    * Return the name of the resource.
    * @return the resource name
    */
    String getName();
    
   /**
    * Return the resource version.
    * @return the version
    */
    String getVersion();
    
   /**
    * Return the statutory resource version.
    * @return the version
    */
    String getStatutoryVersion();
    
   /**
    * Return the decimal version.
    * @return the version
    */
    Version getDecimalVersion();
    
   /**
    * Return the fully qualified path to the resource.
    * @return the path
    */
    String getResourcePath();

   /**
    * Return the basedir for this resource.
    * @return the base directory (possibly null)
    */
    File getBaseDir();

   /**
    * Return the info block.
    * @return the info block
    */
    Info getInfo();
    
   /**
    * Return the resource classifier.
    * @return the classifier (LOCAL, EXTERNAL or ANONYMOUS)
    */
    Classifier getClassifier();
    
   /**
    * Return the expanded array of types associated with the resource.
    * The returned array is a function of the types declared by a resource
    * expanded relative to any types implied by processor dependencies.
    * @return the type array
    */
    Type[] getTypes();
    
   /**
    * Test if this resource is associated with a type of the supplied name.
    * @param type the type id
    * @return TRUE if this resource produces an artifact of the supplied type
    */
    boolean isa( String type );
    
   /**
    * Return a resource type relative to a supplied type id.
    * @param id the type name to retrieve
    * @return the type instance
    */
    Type getType( String id );
    
   /**
    * Construct an artifact for the supplied type.
    * @param type the resource type id
    * @return the artifact
    */
    Artifact getArtifact( String type );
    
   /**
    * Construct an unversion link artifact for the supplied type.
    * @param type the resource type id
    * @return the link artifact
    */
    Artifact getLinkArtifact( String type );
    
   /**
    * Return the enclosing parent module.
    * @return the enclosing module of null if this a top-level module.
    */
    Module getParent();
    
   /**
    * Return an array of resource that are providers to this resource.
    * @param scope the operational scope
    * @param expand if true include transitive dependencies
    * @param sort if true the array will sorted relative to dependencies
    * @return the resource providers
    */
    Resource[] getProviders( Scope scope, boolean expand, boolean sort );
    
   /**
    * Return an array of resource that are providers to this resource. If
    * the supplied scope is BUILD the returned resource array is equivalent
    * <src>getProviders( Scope.BUILD, .. )</src>.  If the scope is RUNTIME
    * the returned resource array includes BUILD and RUNTIME resources. If 
    * the scope is TEST the returned array includes BUILD, RUNTIME and TEST
    * resources.
    * @param scope the scope of aggregation to be applied to the selection
    * @param expand if TRUE include transitive dependencies
    * @param sort if true the array will sorted relative to dependencies
    * @return the resource providers
    */
    Resource[] getAggregatedProviders( Scope scope, boolean expand, boolean sort );
    
   /**
    * Return a sorted and filtered array of providers. Resources not declaring
    * the "jar" type as a produced type are excluded from selection.  The 
    * resource array will include transitive dependencies.  The method is 
    * suitable for the construction of build and test phase classloaders.
    *
    * @param scope the aggregation scope
    * @return the scoped resource chain
    */
    Resource[] getClasspathProviders( Scope scope );

   /**
    * Return an array of runtime providers filtered relative to a supplied
    * classloading category.  Resources not declaring the "jar" type as a 
    * produced type are excluded from selection.  The resource array returned 
    * from this operation is a sorted transitive sequence excluding all 
    * resource references by any category higher than the supplied category.
    * This method is typically used to construct information suitable for 
    * the gerneration of plugin metadata.
    *
    * @param category the classloader category
    * @return the category scoped resource chain
    */
    Resource[] getClasspathProviders( Category category );

   /**
    * Return an array of resources that are consumers of this resource.
    * @param expand if true the returned array includes consumers associated
    *   through transitive dependency relationships, otherwise the array is 
    *   limited to direct consumers
    * @param sort if true the array is sorted relative to depenency relationships
    * @return the array of consumer projects
    */
    Resource[] getConsumers( boolean expand, boolean sort );
    
   /**
    * Return an array of filters associated with the resource.
    * @return the array of filters
    */
    Filter[] getFilters();
    
   /**
    * Return a filename using the layout strategy employed by the cache.
    * @param id the artifact type
    * @return the filename
    */
    String getLayoutPath( String id );

   /**
    * Return the array of production data.
    * @return the associated production data
    */
    Data[] getData();
}
