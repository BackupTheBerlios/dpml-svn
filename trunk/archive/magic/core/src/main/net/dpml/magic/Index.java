package net.dpml.magic;

import java.io.File;

import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Module;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.transit.Repository;

import org.apache.tools.ant.BuildException;

/**
 * Index interface defining a collection of projects and related resources.
 */
public interface Index 
{

   /**
    * Return the home directory.
    * @return the home directory
    */
    File getHomeDirectory();

   /**
    * Return the cache directory.
    * @return the cache directory
    */
    File getCacheDirectory();

   /**
    * Return the docs directory.
    * @return the docs directory
    */
    File getDocsDirectory();

   /**
    * Return the repository service.
    * @return the repository service
    */
    Repository getRepository();

   /**
    * Return the index file used to establish this home.
    * @return the index file
    */
    File getIndexFile();

   /**
    * Return the last modification time of the index file as a long.
    * @return the last modification time
    */
    long getIndexLastModified();

   /**
    * Return a property declared under the project that established the root index.
    * @param key the property key
    * @return the value matching the supplied property key
    */
    String getProperty( String key );

   /**
    * Return a property declared undr the project that established the root index.
    * @param key the property key
    * @param fallback the default value if the property is undefined
    * @return the value matching the supplied property key or the default
    */
    String getProperty( String key, String fallback );

   /**
    * Return TRUE if the supplied key is the name of a key of a resource
    * declared within this home.
    * @param key the key
    * @return TRUE if the key references a resource
    */
    boolean isaResourceKey( String key );

   /**
    * Return TRUE if this is a release build.
    * @return TRUE if this is a release build sequence
    */
    boolean isaRelease();

   /**
    * Return the release signature.
    * @return a string corresponding to the svn revision or null
    *    if this is not a release build
    */
    String getReleaseSignature();

   /**
    * Return all of the resource declared within this home.
    * @return the resource defintions
    */
    Resource[] getResources();

   /**
    * Return TRUE if the suppied resource ref references a project
    * definition.
    * @param reference the resource reference
    * @return TRUE is the resource is a definition
    */
    boolean isaDefinition( ResourceRef reference );

   /**
    * Return all definitions with the home.
    *
    * @return array of know definitions
    * @exception BuildException if a build error occurs
    */
    Definition[] getDefinitions() throws BuildException;

   /**
    * Return all resource refs within the index that are members of the
    * group identified by the supplied resource and not equal to the
    * supplied resource key.
    *
    * @param module the resource used as the enclosing module
    * @return array of subsidiary resources
    * @exception BuildException if a build error occurs
    */
    ResourceRef[] getSubsidiaryRefs( Resource module ) throws BuildException;

   /**
    * Return all definitions with the index that are members of the
    * group identified by the supplied resource and not equal to the
    * supplied resoruce key.
    *
    * @param module the resource used as the enclosing module
    * @return array of subsidiary definitions
    * @exception BuildException if a build error occurs
    */
    Definition[] getSubsidiaryDefinitions( Resource module ) throws BuildException;

   /**
    * Return a resource matching the supplied key.
    * @param key the resource key
    * @return the resource mathing the key
    * @exception BuildException if the resource is unknown
    */
    Resource getResource( String key ) throws BuildException;

   /**
    * Return a resource matching the supplied reference.
    * @param reference the resource reference
    * @return the resource mathing the reference
    * @exception BuildException if the resource is unknown
    */
    Resource getResource( ResourceRef reference ) throws BuildException;

   /**
    * Return a definition matching the supplied key.
    * @param key the resource key
    * @return the definition mathing the key
    * @exception BuildException if the definition is unknown
    */
    Definition getDefinition( String key ) throws BuildException;

   /**
    * Return a definition matching the supplied reference.
    * @param reference the resource reference
    * @return the definition mathing the reference
    * @exception BuildException if the definition is unknown
    */
    Definition getDefinition( ResourceRef reference ) throws BuildException;

   /**
    * Build the module from a supplied source.
    * @param source the module source
    * @return the module
    */
    Module buildModule( File source );

}