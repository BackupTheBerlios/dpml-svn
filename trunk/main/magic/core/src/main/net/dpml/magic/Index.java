package net.dpml.magic;

import java.io.File;

import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Module;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.transit.repository.Repository;

import org.apache.tools.ant.BuildException;

public interface Index {

	public abstract File getHomeDirectory();

	public abstract File getCacheDirectory();

	public abstract File getDocsDirectory();

	public abstract Repository getRepository();

	/**
	 * Return the index file used to establish this home.
	 * @return the index file
	 */
	public abstract File getIndexFile();

	/**
	 * Return the last modification time of the index file as a long.
	 * @return the last modification time
	 */
	public abstract long getIndexLastModified();

	/**
	 * Return a property declared under the project that established the root index.
	 * @param key the property key
	 * @return the value matching the supplied property key
	 */
	public abstract String getProperty(String key);

	/**
	 * Return a property declared undr the project that established the root index.
	 * @param key the property key
	 * @param fallback the default value if the property is undefined
	 * @return the value matching the supplied property key or the default
	 */
	public abstract String getProperty(String key, String fallback);

	/**
	 * Return TRUE if the supplied key is the name of a key of a resource
	 * declared within this home.
	 * @param key the key
	 */
	public abstract boolean isaResourceKey(String key);

	/**
	 * Return TRUE if this is a release build.
	 * @return TRUE if this is a release build sequence
	 */
	public abstract boolean isaRelease();

	/**
	 * Return the release signature.
	 * @return a string corresponding to the svn revision or null
	 *    if this is not a release build
	 */
	public abstract String getReleaseSignature();

	/**
	 * Return all of the resource declared within this home.
	 * @return the resource defintions
	 */
	public abstract Resource[] getResources();

	/**
	 * Return TRUE if the suppied resource ref references a project
	 * definition.
	 * @return TRUE is the resource is a definition
	 */
	public abstract boolean isaDefinition(final ResourceRef reference);

	/**
	 * Return all definitions with the home.
	 *
	 * @return array of know definitions
	 */
	public abstract Definition[] getDefinitions() throws BuildException;

	/**
	 * Return all resource refs within the index that are members of the
	 * group identified by the supplied resource and not equal to the
	 * supplied resource key.
	 *
	 * @return array of subsidiary resources
	 */
	public abstract ResourceRef[] getSubsidiaryRefs(Resource module)
			throws BuildException;

	/**
	 * Return all definitions with the index that are members of the
	 * group identified by the supplied resource and not equal to the
	 * supplied resoruce key.
	 *
	 * @return array of subsidiary definitions
	 */
	public abstract Definition[] getSubsidiaryDefinitions(Resource module)
			throws BuildException;

	/**
	 * Return a resource matching the supplied key.
	 * @return the resource mathing the key
	 * @exception BuildException if the resource is unknown
	 */
	public abstract Resource getResource(final String key)
			throws BuildException;

	/**
	 * Return a resource matching the supplied reference.
	 * @return the resource mathing the reference
	 * @exception BuildException if the resource is unknown
	 */
	public abstract Resource getResource(final ResourceRef reference)
			throws BuildException;

	/**
	 * Return a definition matching the supplied key.
	 * @return the definition mathing the key
	 * @exception BuildException if the definition is unknown
	 */
	public abstract Definition getDefinition(final String key)
			throws BuildException;

	/**
	 * Return a definition matching the supplied reference.
	 * @return the definition mathing the reference
	 * @exception BuildException if the definition is unknown
	 */
	public abstract Definition getDefinition(final ResourceRef reference)
			throws BuildException;

	public abstract Module buildModule(File source);

}