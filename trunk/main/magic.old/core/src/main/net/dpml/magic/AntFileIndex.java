/*
 * Copyright 2004 Stephen McConnell
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

package net.dpml.magic;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import net.dpml.magic.builder.AntFileIndexBuilder;
import net.dpml.magic.builder.XMLDefinitionBuilder;
import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Info;
import net.dpml.magic.model.Module;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;

import net.dpml.transit.NullArgumentException;
import net.dpml.transit.Transit;
import net.dpml.transit.TransitException;
import net.dpml.transit.Artifact;
import net.dpml.transit.Repository;
import net.dpml.transit.util.ElementHelper;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.DataType;

import org.w3c.dom.Element;

/**
 * A Index is an immutable data object that aggregates a suite of buildable
 * project defintions relative to local and external dependencies.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class AntFileIndex extends DataType implements Index
{
    //-------------------------------------------------------------
    // static
    //-------------------------------------------------------------

   /**
    * Banner.
    */
    public static final String BANNER =
      "------------------------------------------------------------------------";

   /**
    * Project home key.
    */
    public static final String HOME_KEY = "project.home";

   /**
    * Project index key.
    */
    public static final String INDEX_KEY = "project.index";

   /**
    * Project hosts key.
    */
    public static final String HOSTS_KEY = "project.hosts";

   /**
    * Project gpg key.
    */
    public static final String GPG_EXE_KEY = "project.gpg.exe";

    //-------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------

    private final File m_index;
    private final Hashtable m_resources = new Hashtable();
    private final ArrayList m_includes = new ArrayList();
    private final AntFileIndexBuilder m_builder;
    private Repository m_repository;

    //-------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------

   /**
    * Creation of a new home using a supplied index.
    * @param proj the ant project establishing the home
    * @param system the magic system instance
    * @param index the magic project index
    * @exception NullArgumentException if any of the supplied arguments are null.
    * @exception TransitException if a Transit initialization error occurs
    */
    public AntFileIndex( Project proj, AntFileIndexBuilder system, File index )
        throws TransitException, NullArgumentException
    {
        if( null == proj )
        {
            throw new NullArgumentException( "project" );
        }

        if( null == system )
        {
            throw new NullArgumentException( "system" );
        }

        if( null == index )
        {
            throw new NullArgumentException( "index" );
        }

        setProject( proj );

        if( !index.exists() )
        {
            throw new BuildException( new FileNotFoundException( index.toString() ) );
        }

        m_builder = system;
        m_index = resolveIndex( index );
        final File user = new File( System.getProperty( "user.home" ) );
        final File props = new File( user, "magic.properties" );
        loadProperties( proj, props );

        File base = m_index.getParentFile();
        processModuleProperties( proj, base );

        //
        // load the initial context
        //

        m_repository = Transit.getInstance().getRepository();
        File home = getHomeDirectory();
        if( null == proj.getProperty( HOME_KEY ) )
        {
            proj.setNewProperty( HOME_KEY, home.getAbsolutePath() );
        }
        File magic = new File( home, "magic.properties" );
        loadProperties( proj, magic );

        //
        // load the system wide properties
        //

        File siteUserProperties = new File( Transit.DPML_PREFS, "magic/user.properties" );
        loadProperties( proj, siteUserProperties );

        File siteProperties = new File( Transit.DPML_PREFS, "magic/magic.properties" );
        loadProperties( proj, siteProperties );

        //
        // build the index
        //

        buildList( m_index );
        int n = m_resources.size();
        log( "Resources: " + n, Project.MSG_VERBOSE );
    }

    //-------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------

   /**
    * Return the home directory.
    * @return the home directory
    */
    public File getHomeDirectory()
    {
        return new File( Transit.DPML_PREFS, "transit" );
    }

   /**
    * Return the cache directory.
    * @return the cache directory
    */
    public File getCacheDirectory()
    {
        String cache = getProject().getProperty( "dpml.cache" );
        return new File( cache );
    }

   /**
    * Return the docs directory.
    * @return the docs directory
    */
    public File getDocsDirectory()
    {
        return new File( Transit.DPML_DATA, "docs" );
    }

   /**
    * Return the repository service.
    * @return the repository service
    */
    public Repository getRepository()
    {
        return m_repository;
    }

   /**
    * Return the index file used to establish this home.
    * @return the index file
    */
    public File getIndexFile()
    {
        return m_index;
    }

   /**
    * Return the last modification time of the index file as a long.
    * @return the last modification time
    */
    public long getIndexLastModified()
    {
        return m_index.lastModified();
    }

   /**
    * Return a property declared under the project that established the root index.
    * @param key the property key
    * @return the value matching the supplied property key
    */
    public String getProperty( String key )
    {
        return getProperty( key, null );
    }

   /**
    * Return a property declared undr the project that established the root index.
    * @param key the property key
    * @param fallback the default value if the property is undefined
    * @return the value matching the supplied property key or the default
    */
    public String getProperty( String key, String fallback )
    {
        String value = getProject().getProperty( key );
        if( null == value )
        {
            return fallback;
        }
        else
        {
            return value;
        }
    }


   /**
    * Return TRUE if the supplied key is the name of a key of a resource
    * declared within this home.
    * @param key the key
    * @return TRUE if the key is a resource
    */
    public boolean isaResourceKey( String key )
    {
        return ( null != m_resources.get( key ) );
    }

   /**
    * Return TRUE if this is a release build.
    * @return TRUE if this is a release build sequence
    */
    public boolean isaRelease()
    {
        return m_builder.isaRelease();
    }

   /**
    * Return the release signature.
    * @return a string corresponding to the svn revision or null
    *    if this is not a release build
    */
    public String getReleaseSignature()
    {
        return m_builder.getReleaseSignature();
    }

   /**
    * Return all of the resource declared within this home.
    * @return the resource defintions
    */
    public Resource[] getResources()
    {
        return (Resource[]) m_resources.values().toArray( new Resource[0] );
    }

   /**
    * Return TRUE if the suppied resource ref references a project
    * definition.
    * @param reference the resource reference
    * @return TRUE is the resource is a definition
    */
    public boolean isaDefinition( final ResourceRef reference )
    {
        final Resource resource = getResource( reference );
        return ( resource instanceof Definition );
    }

   /**
    * Return all definitions with the home.
    *
    * @return array of know definitions
    * @exception BuildException if a build error occurs
    */
    public Definition[] getDefinitions()
        throws BuildException
    {
        final ArrayList list = new ArrayList();
        final Resource[] resources = getResources();
        for( int i=0; i < resources.length; i++ )
        {
            final Resource resource = resources[i];
            if( resource instanceof Definition )
            {
                list.add( resource );
            }
        }
        return (Definition[]) list.toArray( new Definition[0] );
    }

   /**
    * Return all resource refs within the index that are members of the
    * group identified by the supplied resource and not equal to the
    * supplied resource key.
    *
    * @param module the resource representing the module
    * @return array of subsidiary resources
    * @exception BuildException if a build error occurs
    */
    public ResourceRef[] getSubsidiaryRefs( Resource module )
        throws BuildException
    {
        if( !module.getInfo().isa( "module" ) )
        {
            final String error =
              "Resource argument [" + module + "] is not a module.";
            throw new BuildException( error );
        }

        final String group = module.getInfo().getGroup();
        final ArrayList list = new ArrayList();
        Resource[] resources = getResources();
        for( int i=0; i < resources.length; i++ )
        {
            Resource resource = resources[i];
            if( resource.getInfo().getGroup().startsWith( group + "/" ) )
            {
                list.add( new ResourceRef( resource.getKey() ) );
            }
            else if( resource.getInfo().getGroup().equals( group )
              && !resource.getKey().equals( module.getKey() ) )
            {
                list.add( new ResourceRef( resource.getKey() ) );
            }
        }
        return (ResourceRef[]) list.toArray( new ResourceRef[0] );
    }

   /**
    * Return all definitions with the index that are members of the
    * group identified by the supplied resource and not equal to the
    * supplied resource key.
    *
    * @param module the resource representing the module
    * @return array of subsidiary definitions
    * @exception BuildException if a build error occurs
    */
    public Definition[] getSubsidiaryDefinitions( Resource module )
        throws BuildException
    {
        final String group = module.getInfo().getGroup();
        final ArrayList list = new ArrayList();
        Definition[] defs = getDefinitions();
        for( int i=0; i < defs.length; i++ )
        {
            Definition d = defs[i];
            if( d.getInfo().getGroup().startsWith( group + "/" ) )
            {
                list.add( d );
            }
            else if( d.getInfo().getGroup().equals( group )
              && !d.getKey().equals( module.getKey() ) )
            {
                list.add( d );
            }
        }
        return (Definition[]) list.toArray( new Definition[0] );
    }

   /**
    * Return a resource matching the supplied key.
    * @param key the resource key
    * @return the resource mathing the key
    * @exception BuildException if the resource is unknown
    */
    public Resource getResource( final String key )
        throws BuildException
    {
        final ResourceRef reference = new ResourceRef( key );
        return getResource( reference );
    }

   /**
    * Return a resource matching the supplied reference.
    * @param reference the resource reference
    * @return the resource mathing the reference
    * @exception BuildException if the resource is unknown
    * @exception UnknownResourceException if the resource is unknown
    */
    public Resource getResource( final ResourceRef reference )
        throws BuildException, UnknownResourceException
    {
        final String key = reference.getKey();
        final Resource resource = (Resource) m_resources.get( key );
        if( null == resource )
        {
            throw new UnknownResourceException( key );
        }
        return resource;
    }

   /**
    * Return a definition matching the supplied key.
    * @param key the project defintion key
    * @return the definition mathing the key
    * @exception BuildException if the definition is unknown
    */
    public Definition getDefinition( final String key )
        throws BuildException
    {
        final ResourceRef reference = new ResourceRef( key );
        return getDefinition( reference );
    }

   /**
    * Return a definition matching the supplied reference.
    * @param reference the resource reference
    * @return the definition mathing the reference
    * @exception BuildException if the definition is unknown
    */
    public Definition getDefinition( final ResourceRef reference )
        throws BuildException
    {
        final Resource resource = getResource( reference );
        if( resource instanceof Definition )
        {
            return (Definition) resource;
        }
        else
        {
            final String error =
              "Reference [" + reference + "] does not refer to a projects.";
            throw new BuildException( error );
        }
    }

   /**
    * Build the module from a supplied source.
    * @param source the module source
    * @return the module
    */
    public Module buildModule( File source )
    {
        try
        {
            final Element root = ElementHelper.getRootElement( source );
            final String rootElementName = root.getTagName();
            if( "module".equals( rootElementName ) )
            {
                return buildModule( root, source );
            }
            else
            {
                final String error =
                  "Unexpected root module element name ["
                  + rootElementName
                  + "] while reading source ["
                  + source + "].";
                throw new BuildException( error );
            }
        }
        catch( Throwable e )
        {
            final String error =
              "Module construction failure using source ["
              + source
              + "]";
           throw new BuildException( error, e );
        }
    }

   /**
    * Load file based properties.
    * @param proj the project
    * @param file the property file
    * @exception BuildException if build error occurs
    */
    protected void loadProperties( final Project proj, final File file )
       throws BuildException
    {
        final Property props = (Property) proj.createTask( "property" );
        props.init();
        props.setFile( file );
        props.execute();
    }

    //-------------------------------------------------------------
    // internal
    //-------------------------------------------------------------

   /**
    * Populate the index from the contents of the supplied XML source.
    * @param source the index source file
    */
    private void buildList( final File source )
    {
        buildList( source, null );
    }

    private void buildList( final File source, String uri )
    {
        if( !source.exists() )
        {
            final String error = 
              "Imported index file [" + source + "] does not exist.";
            throw new BuildException( error, new Location( m_index.toString() ) );
        }

        final String filename = source.toString();
        final boolean exists = m_includes.contains( filename );
        if( exists )
        {
            return;
        }

        log( "import: " + source, Project.MSG_DEBUG );
        m_includes.add( filename );

        try
        {
            final Element root = ElementHelper.getRootElement( source );
            final String rootElementName = root.getTagName();
            if( "index".equals( rootElementName ) )
            {
                buildIndex( source, root, uri );
            }
            else if( "module".equals( rootElementName ) )
            {
                buildModule( root, source );
            }
            else
            {
                final String error =
                  "Unrecognized root element [" + rootElementName + "] in index ["
                  + m_index;
                throw new BuildException( error );
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while building index list."
              + "\nSource index: " + source
              + "\nBase index: " + m_index;
            throw new BuildException( error, e, new Location( m_index.toString() ) );
        }
    }

    private void buildIndex( File source, Element root, String uri )
    {
        //
        // its a native magic index
        //

        final File anchor = source.getParentFile();
        String key = ElementHelper.getAttribute( root, "key", null );
        final Element[] elements = ElementHelper.getChildren( root );

        if( ( null != key ) && ( key.length() > 0 ) )
        {
            buildLocalList( anchor, elements, "key:" + key );
        }
        else if( ( null != uri ) && uri.length() > 0 )
        {
            buildLocalList( anchor, elements, uri );
        }
        else
        {
            buildLocalList( anchor, elements, "key:UNKNOWN" );
        }
    }

    private Module buildModule( String uri )
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( uri );
            URL url = artifact.toURL();
            File local = (File) url.getContent( new Class[]{File.class} );
            final Element root = ElementHelper.getRootElement( local );
            return buildModule( root, local );
        }
        catch( Exception ioe )
        {
            final String error =
              "Failed to load module ["
              + uri
              + "]."
              + ioe.toString();
            throw new BuildException( error, ioe );
        }
    }

    private Module buildModule( Element root, File source )
    {
        //
        // its a module definition so we need to add the module as an available
        // resources and add all of the resources defined by the module
        //

        final String moduleUri = ElementHelper.getAttribute( root, "uri" );
        if( "".equals( moduleUri ) || ( null == moduleUri ) )
        {
            String mess =
              "Modules must contain a \"uri\" attribute in the root module element: "
              + source;
            throw new BuildException( mess );
        }

        Info info = createInfo( moduleUri );
        String key = info.getName();

        if( m_resources.containsKey( key ) )
        {
            Resource resource = getResource( key );
            if( resource instanceof Module )
            {
                return (Module) resource;
            }
            else
            {
                final String error =
                  "Source index or module ["
                  + source
                  + "] declares the key ["
                  + key
                  + "] that is already bound.";
                throw new BuildException( error );
            }
        }

        Module.Header header = Module.Header.create( root );
        String[] imports = Module.createImports( root );
        ResourceRef[] refs = new ResourceRef[ imports.length ];
        for( int i=0; i < imports.length; i++ )
        {
            String spec = imports[i];
            Module link = buildModule( spec );
            refs[i] = new ResourceRef( link.getKey() );
        }

        final Element resources = ElementHelper.getChild( root, "resources" );
        final ResourceRef[] children = buildResources( resources, source, moduleUri );

        Module module = new Module( this, key, info, refs, "key:NULL", header, children );
        m_resources.put( key, module );
        log( "module: " + module, Project.MSG_VERBOSE );

        return module;
    }

    private ResourceRef[] buildResources( final Element resources, File source, final String moduleUri )
    {
        final Element[] elements = ElementHelper.getChildren( resources, "resource" );
        final File anchor = source.getParentFile();
        final ResourceRef[] children = new ResourceRef[ elements.length ];
        for( int i=0; i < elements.length; i++ )
        {
            final Resource resource = createResource( elements[i], anchor, moduleUri );
            final String k = resource.getKey();
            if( !m_resources.containsKey( k ) )
            {
                m_resources.put( k, resource );
                log( "resource: " + resource, Project.MSG_VERBOSE );
            }
        }
        return children;
    }

    private Info createInfo( String uri )
    {
        try
        {
            return Info.create( uri );
        }
        catch( Exception e )
        {
            throw new BuildException( e );
        }
    }

    private File resolveIndex( File file )
    {
        if( file.isDirectory() )
        {
            return new File( file, "index.xml" );
        }
        else
        {
            return file;
        }
    }

    private void buildLocalList( final File anchor, final Element[] children, String uri )
    {
        log( "entries: " + children.length, Project.MSG_VERBOSE );
        for( int i = 0; i < children.length; i++ )
        {
            final Element element = children[i];
            final String tag = element.getTagName();
            if( "import".equals( element.getTagName() ) )
            {
                buildImport( element, anchor, uri );
            }
            else if( isaResource( tag ) )
            {
                buildResource( element, anchor, uri );
            }
            else
            {
                final String error =
                  "Unrecognized element type \"" + tag + "\" found in index.";
                throw new BuildException( error );
            }
        }
    }

    private void buildResource( final Element element, final File anchor, String uri )
    {
        final Resource resource = createResource( element, anchor, uri );
        final String key = resource.getKey();
        if( !m_resources.containsKey( key ) )
        {
            m_resources.put( key, resource );
            if( key.equals( resource.getInfo().getName() ) )
            {
                log( "resource: " + resource, Project.MSG_VERBOSE );
            }
            else
            {
                log( "resource: " + resource + " key=" + key, Project.MSG_VERBOSE );
            }
        }
        else
        {
            Resource r = (Resource) m_resources.get( key );
            if( !r.equals( resource ) )
            {
                final String error =
                  "WARNING: Ignoring duplicate key reference ["
                  + key
                  + "].";
                log( error, Project.MSG_WARN );
            }
        }
    }

    private void buildImport( final Element element, final File anchor, String uri )
    {
        final String filename = element.getAttribute( "index" );
        final String uriAttribute = element.getAttribute( "uri" );
        String urn = parse( uriAttribute );
        if( ( null != filename ) && ( !"".equals( filename ) ) )
        {
            final File index = AntFileIndexBuilder.getFile( anchor, filename );
            buildList( index, uri );
        }
        else if( null == urn || "".equals( urn ) )
        {
            final String error =
              "Invalid import statement. No uri, index attribute.";
            throw new BuildException( error );
        }
        else
        {
            // switch to artifact
            if( !m_includes.contains( urn ) )
            {
                m_includes.add( urn );
                try
                {
                    Artifact artifact = Artifact.createArtifact( urn );
                    URL url = artifact.toURL();
                    File local = (File) url.getContent( new Class[]{File.class} );
                    buildList( local, urn );
                }
                catch( BuildException e )
                {
                    throw e;
                }
                catch( Exception ioe )
                {
                    final String error =
                      "Failed to load module ["
                      + urn
                      + "]."
                      + ioe.toString();
                    throw new BuildException( error, ioe );
                }
            }
        }
    }

    private String parse( String value )
    {
        if( null == value )
        {
            return value;
        }
        String result = getProject().replaceProperties( value );
        if( !result.equals( value ) )
        {
            return parse( result );
        }
        else
        {
            return result;
        }
    }

    private Resource createResource( final Element element, final File anchor, String uri )
    {
        return XMLDefinitionBuilder.createResource( this, element, anchor, uri );
    }

    private boolean isaResource( final String tag )
    {
        return (
          "resource".equals( tag )
          || "project".equals( tag ) );
    }

    private void processModuleProperties( Project proj, File base )
    {
        if( null == base )
        {
            return;
        }
        File module = new File( base, "module.properties" );
        loadProperties( proj, module );
        File parent = base.getParentFile();
        processModuleProperties( proj, parent );
    }
}
