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

package org.apache.avalon.tools.model;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * A Home is an immutable data object that aggregates a suite of buildable 
 * project defintions relative to local and external dependencies.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Home extends DataType
{
    //-------------------------------------------------------------
    // static
    //-------------------------------------------------------------

    public static final String BANNER = 
      "------------------------------------------------------------------------";

    public static final String KEY = "project.home";
    public static final String HOME_KEY = "project.home";
    public static final String INDEX_KEY = "project.index";
    public static final String HOSTS_KEY = "project.hosts";
    public static final String GPG_EXE_KEY = "project.gpg.exe";

    //-------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------

    private final File m_index;
    private final Hashtable m_resources = new Hashtable();
    private final ArrayList m_includes = new ArrayList();
    private final Magic m_system;
    private final Project m_project;

    //-------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------

   /**
    * Creation of a new home using a supplied index.
    * @param project the ant project establishing the home
    * @param system the magic system instance
    * @param index the magic project index
    */
    protected Home( Project project, Magic system, File index )
    {
        setProject( project );
        m_index = index;
        m_system = system;
        m_project = project;
        buildList( index );
        int n = m_resources.size();
        project.log( "Resource count: " + n );

        if( null != system.getGumpSignature() )
        {
            project.log( "Gump signature: " + system.getGumpSignature() );
        }
    }

    //-------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------

   /**
    * Return the index file used to establish this home.
    * @return the index file
    */
    public File getIndex()
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
    * Return a property declared undr the project that established the root index.
    * @param key the property key
    * @return the value matching the supplied property key
    */
    public String getProperty( String key )
    {
        return m_project.getProperty( key );
    }

   /**
    * Return TRUE if the supplied key is the name of a key of a resource
    * declared within this home.
    * @param key the key
    */
    public boolean isaResourceKey( String key )
    {
        return ( null != m_resources.get( key ) );
    }

   /**
    * Return TRUE if the runtime is initiated by gump.
    * @return TRUE if gumpt is running the show
    */
    public boolean isGump()
    {
        return ( null != getGumpSignature() );
    }

   /**
    * Return the gump build signature.
    * @return a string corresponding to the gump @@DATE@@ value.
    */
    public String getGumpSignature()
    {
        return m_system.getGumpSignature();
    }

   /**
    * Return the immutable repository descriptor for the main cache.
    * @return the main cache repository descriptor
    */
    public Repository getRepository()
    {
        return m_system.getRepository();
    }

   /**
    * Return the immutable docs repository cache descriptor.
    * @return the docs repository descriptor
    */
    public Repository getDocsRepository()
    {
        return m_system.getDocsRepository();
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
    * @return TRUE is the resource is a definition
    */
    public boolean isaDefinition( final ResourceRef ref )
    {
        return ( getResource( ref ) instanceof Definition );
    }

   /**
    * Return all definitions with the home.
    *
    * @return array of know definitions
    */
    public Definition[] getDefinitions()
      throws BuildException
    {
        final ArrayList list = new ArrayList();
        final Resource[] resources = getResources();
        for( int i=0; i<resources.length; i++ )
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
    * Return a resource matching the supplied key.
    * @return the resource mathing the key
    * @exception BuildException if the resource is unknown
    */
    public Resource getResource( final String key )
      throws BuildException
    {
        final ResourceRef ref = new ResourceRef( key );
        return getResource( ref );
    }

   /**
    * Return a resource matching the supplied reference.
    * @return the resource mathing the reference
    * @exception BuildException if the resource is unknown
    */
    public Resource getResource( final ResourceRef ref )
      throws BuildException
    {
        final String key = ref.getKey();
        final Resource resource = (Resource) m_resources.get( key );
        if( null == resource )
        {
            throw new UnknownResourceException( key );
        }
        return resource;
    }

   /**
    * Return a definition matching the supplied key.
    * @return the definition mathing the key
    * @exception BuildException if the definition is unknown
    */
    public Definition getDefinition( final String key )
      throws BuildException
    {
        final ResourceRef ref = new ResourceRef( key );
        return getDefinition( ref );
    }

   /**
    * Return a definition matching the supplied reference.
    * @return the definition mathing the reference
    * @exception BuildException if the definition is unknown
    */
    public Definition getDefinition( final ResourceRef ref )
      throws BuildException
    {
        final Resource resource = getResource( ref );
        if( resource instanceof Definition )
        {
            return (Definition) resource;
        }
        else
        {
            final String error = 
              "Reference [" + ref + "] does not refer to a projects.";
            throw new BuildException( error );
        }
    }

   /**
    * Return a plugin matching the supplied reference.
    * @return the plugin mathing the reference
    * @exception BuildException if the plugin is unknown
    */
    public Plugin getPlugin( final ResourceRef ref )
      throws BuildException
    {
        final Resource resource = getResource( ref );
        if( resource instanceof Plugin )
        {
            return (Plugin) resource;
        }
        else
        {
            final String error = 
              "Reference [" + ref + "] does not refer to a plugin.";
            throw new BuildException( error );
        }
    }

    //-------------------------------------------------------------
    // internal
    //-------------------------------------------------------------

    private void buildList( final File index )
    {
        if( null == index ) 
        {
            throw new NullPointerException( "index" );
        }

        if( !index.exists() ) 
        {
            throw new BuildException( 
              new FileNotFoundException( index.toString() ) );
        }

        File source = resolveIndex( index );

        if( m_includes.contains( source.toString() ) )
        {
            return;
        }

        m_includes.add( source.toString() );
        log( "import: " + source );

        try
        {
            final Element root = ElementHelper.getRootElement( source );
            final String rootElementName = root.getTagName();
            if( "index".equals( rootElementName ) )
            {
                //
                // its a native magic index
                //

                final Element[] elements = ElementHelper.getChildren( root );
                final File anchor = source.getParentFile();
                buildLocalList( anchor, elements );
            }
            else
            {
                final String error = 
                  "Unrecognized rooot element [" + rootElementName + "].";
                throw new BuildException( error );
            }
        }
        catch( Throwable e )
        {
            throw new BuildException( e, new Location( index.toString() ) );
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

    private void buildLocalList( 
      final File anchor, final Element[] children )
    {
        log( "entries: " + children.length, Project.MSG_VERBOSE );
        for( int i=0; i<children.length; i++ )
        {
            final Element element = children[i];
            final String tag = element.getTagName();
            if( "import".equals( element.getTagName() ) )
            {
                final String filename = element.getAttribute( "index" );
                final String path = element.getAttribute( "href" );
                if(( null != filename ) && ( !"".equals( filename )))
                {
                    final File index = Context.getFile( anchor, filename );
                    buildList( index );
                }
                else if(( null != path ) && ( !"".equals( path )))
                {
                    // switch to remote
                    if( !m_includes.contains( path ) )
                    {
                        m_includes.add( path );
                        buildRemoteList( path );
                    }
                }
                else
                {
                    final String error = 
                      "Invalid import statement. No href or index attribute.";
                    throw new BuildException( error );
                }
            }
            else if( isaResource( tag ) )
            {
                final Resource resource = createResource( element, anchor );
                final String key = resource.getKey();
                m_resources.put( key, resource );
                log( 
                  "resource: " + resource 
                  + " key=" + key, Project.MSG_VERBOSE );
            }
            else
            {
                final String error =
                  "Unrecognized element type \"" + tag + "\" found in index.";
                throw new BuildException( error );
            }
        }
    }

    private void buildRemoteList( String path )
    {
        log( "import: " + path );
        final URL url = createURL( path );
        InputStream input = null;
        try
        {
            input = url.openStream();
            final Element root = ElementHelper.getRootElement( input );
            final Element[] elements = ElementHelper.getChildren( root );
            buildRemoteList( path, elements );
        }
        catch( SAXParseException e )
        {
            int line = e.getLineNumber();
            int column = e.getColumnNumber();
            throw new BuildException( e, new Location( path, line, column ) );
        }
        catch( SAXException e )
        {
            if( e.getException() != null) 
            {
                throw new BuildException( 
                  e.getException(), new Location( path ) );
            }
            throw new BuildException( e, new Location( path ) );
        }
        catch( Throwable e )
        {
            throw new BuildException( e, new Location( path ) );
        }
        finally
        {
            if( null != input )
            {
                try
                {
                    input.close();
                }
                catch( IOException ioe )
                {
                    // ignore
                }
            }
        }
    }

    private void buildRemoteList( final String source, final Element[] children )
    {
        log( "entries: " + children.length, Project.MSG_VERBOSE );
        for( int i=0; i<children.length; i++ )
        {
            final Element element = children[i];
            final String tag = element.getTagName();
            if( isaResource( tag ) )
            {
                final Resource resource = createResource( element );
                final String key = resource.getKey();
                m_resources.put( key, resource );
                log( 
                  "resource: " + resource 
                  + " key=" + key, Project.MSG_VERBOSE );
            }
            else if( "import".equals( element.getTagName() ) )
            {
                final String path = element.getAttribute( "href" );
                if(( null != path ) && ( !"".equals( path )))
                {
                    if( !m_includes.contains( path ) )
                    {
                        m_includes.add( path );
                        buildRemoteList( path );
                    }
                }
                else
                {
                    final String error = 
                      "Import statement in remote index does not contain an 'href' attribute.";
                    throw new BuildException( error, new Location( source ) );
                }
            }
            else
            {
                final String error =
                  "Unrecognized element type \"" + tag + "\" found in remote index.";
                throw new BuildException( error, new Location( source ) );
            }
        }
    }

    private URL createURL( String path )
    {
        try
        {
            return new URL( path );
        }
        catch( IOException ioe )
        {
            throw new BuildException( ioe );
        }
    }

    private Resource createResource( 
      final Element element, final File anchor )
    {
        return XMLDefinitionBuilder.createResource( this, element, anchor );
    }

    private Resource createResource( final Element element )
    {
        return XMLDefinitionBuilder.createResource( this, element );
    }

    private boolean isaResource( final String tag )
    {
        return ( 
          "resource".equals( tag ) 
          || "project".equals( tag )
          || "plugin".equals( tag ) );
    }

    /*

    public void build( Definition definition )
    {
        Ant ant = (Ant) getProject().createTask( "ant" );
        Property property = ant.createProperty();
        property.setName( "urn:avalon.definition.key" );
        property.setValue( definition.getKey() );
        ant.setDir( definition.getBasedir() );
        ant.setInheritRefs( true );
        ant.init();
        ant.execute();
    }

    public Definition[] getBuildSequence( Definition definition )
    {
        ArrayList visited = new ArrayList();
        ArrayList targets = new ArrayList();
        ProjectRef[] refs = definition.getProjectRefs();
        for( int i=0; i<refs.length; i++ )
        {
            Definition def = getDefinition( refs[i] );
            getBuildSequence( visited, targets, def );
        }
        return (Definition[]) targets.toArray( new Definition[0] );
    }

    private void getBuildSequence( List visited, List targets, Definition definition )
    {
        if( visited.contains( definition ) ) return;
        visited.add( definition );

        ProjectRef[] refs = definition.getProjectRefs();
        for( int i=0; i<refs.length; i++ )
        {
            Definition def = getDefinition( refs[i] );
            if( visited.contains( def ) )
            {
                final String error =
                  "Recursive reference identified in project: " 
                     + definition 
                     + " in dependency " + def + ".";
                throw new BuildException( error );
            }
            getBuildSequence( visited, targets, def );
        }

        if( !targets.contains( definition ) )
        {
            targets.add( definition );
        }
    }
    */

}
