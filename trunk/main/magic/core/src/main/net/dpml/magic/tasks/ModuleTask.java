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

package net.dpml.magic.tasks;

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.builder.AntFileIndexBuilder;
import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.model.Policy;
import net.dpml.magic.model.Info;
import net.dpml.magic.model.Type;
import net.dpml.magic.project.Context;
import net.dpml.magic.project.DeliverableHelper;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Create a module descriptor relative to the current project.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ModuleTask extends ProjectTask
{
    //-----------------------------------------------------------------------
    // static
    //-----------------------------------------------------------------------

   /**
    * Constant module type.
    */
    public static final String MODULE_EXT = "module";

   /**
    * Constant module repository key.
    */
    public static final String REPOSITORY_KEY = "project.module.repository";

   /**
    * Constant module svn key.
    */
    public static final String SVN_KEY = "project.module.svn";

   /**
    * Constant module home key.
    */
    public static final String HOME_KEY = "project.module.home";

   /**
    * Constant module license key.
    */
    public static final String LICENSE_KEY = "project.module.license";

   /**
    * Constant module license notice key.
    */
    public static final String NOTICE_KEY = "project.module.notice";

   /**
    * Constant module publisher key.
    */
    public static final String PUBLISHER_KEY = "project.module.publisher";

   /**
    * Constant module docs href key.
    */
    public static final String DOCS_KEY = "project.module.docs";

   /**
    * Constant module package key.
    */
    public static final String PACKAGE_KEY = "project.module.package";

    //-----------------------------------------------------------------------
    // state
    //-----------------------------------------------------------------------

    private AntFileIndex m_target;
    private Header m_header;

    //-----------------------------------------------------------------------
    // ModuleTask
    //-----------------------------------------------------------------------

   /**
    * Set the index file.
    * @param target the index file
    */
    public void setIndex( File target )
    {
        AntFileIndexBuilder builder = new AntFileIndexBuilder( getProject(), target );
        m_target = builder.getIndex();
    }

   /**
    * Return the working index file.
    * @return the index
    */
    private AntFileIndex getPrivateIndex()
    {
         if( null == m_target )
         {
             return getIndex();
         }
         else
         {
             return m_target;
         }
    }

   /**
    * Return the module group.
    * @return the group name
    */
    public String getGroup()
    {
        return getDefinition().getInfo().getGroup();
    }

   /**
    * Return the module version.
    * @return the module version
    */
    public String getVersion()
    {
        return getDefinition().getInfo().getVersion();
    }

   /**
    * Return the module name.
    * @return the module name
    */
    public String getName()
    {
        return getDefinition().getInfo().getName();
    }

   /**
    * Create and add a header.
    * @return the module header
    */
    public Header createHeader()
    {
        if( null == m_header )
        {
            m_header = new Header( getPrivateIndex() );
            return m_header;
        }
        else
        {
            throw new BuildException( "Multiple header entries not allowed." );
        }
    }

   /**
    * Get the module header.
    * @return the module header
    */
    private Header getHeader()
    {
        if( null == m_header )
        {
            return createHeader();
        }
        return m_header;
    }

   /**
    * Execute module creation.
    * @exception BuildException if a build errro occurs
    */
    public void execute() throws BuildException
    {
        Definition definition = getDefinition();
        final Info info = definition.getInfo();
  
        if( !info.isa( "module" ) )
        {
            final String error =
              "Illegal attempt to build a module from the project "
              + definition 
              + " as the project does not declare itself as producing a 'module' resource type.";
            throw new BuildException( error, getLocation() );
        }

        String type = "module";
        String filename = info.getFilename( type );
        File deliverables = getContext().getDeliverablesDirectory();
        File modules = new File( deliverables, type + "s" );
        File module = new File( modules, filename );

        try
        {
            module.getParentFile().mkdirs();
            module.createNewFile();
            log( "output: " + module );
            final OutputStream output = new FileOutputStream( module );
            final Writer writer = new OutputStreamWriter( output );

            try
            {
                writer.write( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );
                writer.write( "\n\n<module uri=\"" + definition.getInfo().getURI( "module" ) + "\">" );
                writeHeader( writer, getHeader() );
                writeModule( writer, definition );
                writer.write( "\n\n</module>\n" );
                writer.flush();
            }
            finally
            {
                closeStream( output );
            }

            DeliverableHelper.checksum( this, module );
            String gpg = getIndex().getProperty( Context.GPG_EXE_KEY );
            DeliverableHelper.asc( this, module, gpg );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

   /**
    * Write out the module defintion.
    * @param writer the print writer
    * @param definition the module definition 
    * @exception IOException if an IO related error occurs
    */
    protected void writeModule( final Writer writer, Definition definition )
        throws IOException
    {
        Definition[] definitions = getPrivateIndex().getSubsidiaryDefinitions( definition );

        List external = new ArrayList();
        List modules = new ArrayList();
        for( int i=0; i < definitions.length; i++ )
        {
            Definition def = definitions[i];
            expand( definition, def, external, modules );
        }

        writer.write( "\n  <resources>" );

        //
        // after scanning all of the contained projects within the scope of this
        // module, we scan all of the depedencies, we grab the list of module uris
        // implied by the dependencies.  This establishes the runtime imports
        // (module dependencies that this module has with other moudlues)
        //

        String[] uris = (String[]) modules.toArray( new String[0] );
        if( uris.length > 0 )
        {
            writer.write( "\n\n    <!-- imports -->\n" );
            for( int i=0; i < uris.length; i++ )
            {
               writer.write( "\n    <import uri=\"" + uris[i] + "\"/>" );
            }
        }

        //
        // write out the list of projects (in the form of <resource> statements
        // for all projects that are within the scope of the module
        //

        writer.write( "\n\n    <!-- module resources -->" );
        for( int i=0; i < definitions.length; i++ )
        {
            Definition def = definitions[i];
            writeResource( writer, def );
        }

        //
        // write out the list of any externally referenced resources that
        // are not part of the module but required by the module to fullfill
        // project runtime depedencies (this impl needs to be updated to
        // crosscheck with resources available under imported modules)
        //

        Resource[] resources = (Resource[]) external.toArray( new Resource[0] );
        if( resources.length > 0 )
        {
            writer.write( "\n\n    <!-- referenced resources -->" );
            for( int i=0; i < resources.length; i++ )
            {
                Resource resource = resources[i];
                writeResource( writer, resource );
            }
        }

        writer.write( "\n\n  </resources>" );
    }

   /**
    * Add the resource referenced by the ref to the list and for all of the
    * runtime dependencies of the resource, axpand them into the list.
    *
    * @param definition the current module definition
    * @param def the project defintion to be expanded
    * @param list the list of external resources to be populated
    * @param modules the list of module uri names to be populated
    */
    private void expand( final Definition definition, final Definition def, final List list, final List modules )
    {
        ResourceRef[] refs = def.getResourceRefs();
        for( int i=0; i < refs.length; i++ )
        {
            ResourceRef ref = refs[i];
            expand( definition, ref, list, modules );
        }
    }

   /**
    * Add the resource referenced by the ref to the list and for all of the
    * runtime dependencies of the resource, expand them into the list.
    *
    * @param definition the current module definition
    * @param reference the resource reference to expand
    * @param list the expansion list
    * @param modules
    */
    private void expand( Definition definition, ResourceRef reference, List list, List modules )
    {
        Resource resource = getPrivateIndex().getResource( reference );
        if( resource.getInfo().isa( "module" ) )
        {
            //
            // the resource is a module - add it to the list of modules to import
            //

            String uri = resource.getInfo().getURI( "module" );
            if( !uri.equals( definition.getInfo().getURI( "module" ) ) && !modules.contains( uri ) )
            {
                modules.add( uri );
            }
        }
        else
        {
            String module = resource.getModule();
            if( ( null != module ) && !"".equals( module ) )
            {
                if( !module.equals( definition.getInfo().getURI( "module" ) ) )
                {
                    if( !modules.contains( module ) )
                    {
                        modules.add( module );
                    }
                }
                else
                {
                    if( !( resource instanceof Definition ) && !list.contains( resource ) )
                    {
                        list.add( resource );
                        ResourceRef[] refs = resource.getResourceRefs();
                        for( int i=0; i < refs.length; i++ )
                        {
                            ResourceRef ref = refs[i];
                            if( ref.getPolicy().matches( Policy.RUNTIME ) )
                            {
                                 expand( definition, ref, list, modules );
                            }
                        }
                    }
                }
            }
        }
    }

   /**
    * Write the definition out the module as XML..
    * @param writer the writer
    * @param res Resource to be written.
    * @throws IOException if unable to write xml
    */
    private void writeResource( final Writer writer, Resource res )
        throws IOException
    {
        writer.write( "\n\n    <resource key=\"" + res.getKey() + "\">" );
        writer.write( "\n      <info>" );
        writer.write( "\n        <group>" + res.getInfo().getGroup() + "</group>" );
        writer.write( "\n        <name>" + res.getInfo().getName() + "</name>" );
        if( res.getInfo().getVersion() != null )
        {
            writer.write( "\n        <version>" + res.getInfo().getVersion() + "</version>" );
        }
        writer.write( "\n        <types>" );
        Type[] types = res.getInfo().getTypes();
        for( int i=0; i < types.length; i++ )
        {
            Type type = types[i];
            writer.write( "\n          <type name=\"" + type.getName() + "\"/>" );
        }
        writer.write( "\n        </types>" );
        writer.write( "\n      </info>" );

        ResourceRef[] refs = res.getResourceRefs();
        if( refs.length > 0 )
        {
            writer.write( "\n      <dependencies>" );
            for( int i=0; i < refs.length; i++ )
            {
                ResourceRef ref = refs[i];
                if( ref.getPolicy().matches( Policy.RUNTIME ) )
                {
                    writeDependency( writer, ref );
                }
            }
            writer.write( "\n      </dependencies>" );
        }
        writer.write( "\n    </resource>" );
    }

    private void writeDependency( final Writer writer, ResourceRef ref )
      throws IOException
    {
        writer.write( "\n        <include key=\"" + ref.getKey() + "\"/>" );
    }

   /**
    * Write the XML header.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    private void writeHeader( final Writer writer, Header header )
        throws IOException
    {
        writer.write( "\n\n  <header>" );
        writer.write( "\n    <publisher name=\"" + header.getPublisher().getName() + "\"/>" );

        Package[] packages = header.getPackages();
        if( packages.length > 0 )
        {
            writer.write( "\n    <packages>" );
            for( int i=0; i < packages.length; i++ )
            {
                writer.write( "\n      <package name=\"" + packages[i].getName() + "\"/>" );
            }
            writer.write( "\n    </packages>" );
        }

        writer.write( "\n    <home href=\"" + header.getHome().getHref() + "\"/>" );
        writer.write( "\n    <docs href=\"" + header.getDocs().getHref() + "\"/>" );
        writer.write( "\n    <legal>" );
        writer.write( "\n      <license href=\"" + header.getLegal().getLicense().getHref() + "\"/>" );
        writer.write( "\n      <notice href=\"" + header.getLegal().getNotice().getHref() + "\"/>" );
        writer.write( "\n    </legal>" );
        writer.write( "\n    <svn href=\"" + header.getSvn().getHref() + "\"/>" );
        writer.write( "\n    <repository href=\"" + header.getRepository().getHref() + "\"/>" );
        writer.write( "\n  </header>\n" );
    }

    private void closeStream( final OutputStream output )
    {
        if( null != output )
        {
            try
            {
                output.close();
            }
            catch( IOException e )
            {
                log( e.toString() );
            }
        }
    }

    //-----------------------------------------------------------------------
    // nested classes
    //-----------------------------------------------------------------------

   /**
    * Base class for href entries.
    */
    public static class Resolver
    {
        private AntFileIndex m_index;

       /**
        * Creation of a new href resolver.
        * @param index the working index
        */
        public Resolver( AntFileIndex index )
        {
            m_index = index;
        }

       /**
        * Return the working index.
        * @return the index
        */
        public AntFileIndex getIndex()
        {
            return m_index;
        }

       /**
        * Return a property.
        * @param key the property key
        * @param fallback the default value
        * @return the resolved property value
        */
        protected String getProperty( String key, String fallback )
        {
            String value = getIndex().getProperty( key );
            if( null == value )
            {
                return fallback;
            }
            else
            {
                return value;
            }
        }
    }

   /**
    * A defintion of a href.
    */
    public static class Href extends Resolver
    {
        private String m_href;
        private String m_key;
        private String m_default;

       /**
        * Creation of a new href.
        * @param index the working index
        * @param key a defintion key
        * @param fallback the default href value
        */
        public Href( AntFileIndex index, String key, String fallback )
        {
            super( index );
            m_key = key;
            m_default = fallback;
        }

       /**
        * Set the href value.
        * @param href the href value
        */
        public void setHref( String href )
        {
            m_href= href;
        }

       /**
        * Return the href value.
        * @return the href value
        */
        public String getHref()
        {
            if( null != m_href )
            {
                return m_href;
            }
            else
            {
                String value = getIndex().getProperty( m_key );
                if( null == value )
                {
                    return m_default;
                }
                else
                {
                    return value;
                }
            }
        }
    }

   /**
    * The publisher.
    */
    public static class Publisher extends Resolver
    {
        private String m_name;

       /**
        * Creation of a new publisher.
        * @param index the working index
        */
        public Publisher( AntFileIndex index )
        {
            super( index );
        }

       /**
        * Set the publisher name.
        * @param name the publisher name
        */
        public void setName( String name )
        {
            m_name = name;
        }

       /**
        * Return the publisher name.
        * @return the publisher name
        */
        public String getName()
        {
            if( null != m_name )
            {
                return m_name;
            }
            else
            {
                String value = getIndex().getProperty( PUBLISHER_KEY );
                if( null == value )
                {
                    return "";
                }
                else
                {
                    return value;
                }

            }
        }
    }

   /**
    * A new package description.
    */
    public static class Package extends Resolver
    {
        private String m_name;

       /**
        * Creation of a new package description.
        * @param index the working index
        */
        public Package( AntFileIndex index )
        {
            super( index );
        }

       /**
        * Set the package name.
        * @param name the pckage name
        */
        public void setName( String name )
        {
            m_name = name;
        }

       /**
        * Return the package name.
        * @return the package name
        */
        public String getName()
        {
            if( null != m_name )
            {
                return m_name;
            }
            else
            {
                String value = getIndex().getProperty( PACKAGE_KEY );
                if( null == value )
                {
                    return "";
                }
                else
                {
                    return value;
                }
            }
        }
    }

   /**
    * A collection of packages.
    */
    public static class Packages
    {
        private List m_packages = new ArrayList();
        private AntFileIndex m_index;

       /**
        * Creation of a new packages instance.
        * @param index the working index
        */
        public Packages( AntFileIndex index )
        {
            m_index = index;
        }

       /**
        * Add a new package to the colection of packages.
        * @return the new package
        */
        public Package createPackage()
        {
            Package p = new Package( m_index );
            m_packages.add( p );
            return p;
        }

       /**
        * Return the colleciton of packages.
        * @return the package array
        */
        public Package[] getPackages()
        {
            return (Package[]) m_packages.toArray( new Package[0] );
        }
    }

   /**
    * The license href.
    */
    public static class License extends Href
    {
       /**
        * Creation of a new licence href.
        * @param index the working index
        */
        public License( AntFileIndex index )
        {
            super( index, LICENSE_KEY, "" );
        }
    }

   /**
    * The notice href.
    */
    public static class Notice extends Href
    {
       /**
        * Creation of a new notice href.
        * @param index the working index
        */
        public Notice( AntFileIndex index )
        {
            super( index, NOTICE_KEY, ""  );
        }
    }


   /**
    * The svn href.
    */
    public static class Svn extends Href
    {
       /**
        * Creation of a new svn href.
        * @param index the working index
        */
        public Svn( AntFileIndex index )
        {
            super( index, SVN_KEY, "" );
        }
    }

   /**
    * The home page href.
    */
    public static class Home extends Href
    {
       /**
        * Creation of a new home href.
        * @param index the working index
        */
        public Home( AntFileIndex index )
        {
            super( index, HOME_KEY, "" );
        }
    }

   /**
    * The repository href.
    */
    public static class Repository extends Href
    {
       /**
        * Creation of a repository href.
        * @param index the working index
        */
        public Repository( AntFileIndex index )
        {
            super( index, REPOSITORY_KEY, "" );
        }
    }

   /**
    * The docs href.
    */
    public static class Docs extends Href
    {
       /**
        * Creation of a docs href.
        * @param index the working index
        */
        public Docs( AntFileIndex index )
        {
            super( index, DOCS_KEY, "" );
        }
    }

   /**
    * The legal datastructure.
    */
    public static class Legal extends Resolver
    {
        private License m_license;
        private Notice m_notice;

       /**
        * Creation of a legal datastructure.
        * @param index the working index
        */
        public Legal( AntFileIndex index )
        {
            super( index );
        }

       /**
        * Creation of a new license.
        * @return the license
        */
        public License createLicense()
        {
            if( null == m_license )
            {
                m_license = new License( getIndex() );
                return m_license;
            }
            else
            {
                throw new BuildException( "Multiple license entries not allowed." );
            }
        }

       /**
        * Creation of a new notice.
        * @return the notice
        */
        public Notice createNotice()
        {
            if( null == m_notice )
            {
                m_notice = new Notice( getIndex() );
                return m_notice;
            }
            else
            {
                throw new BuildException( "Multiple notice entries not allowed." );
            }
        }

       /**
        * Return the license.
        * @return the license
        */
        public License getLicense()
        {
            if( null == m_license )
            {
                return createLicense();
            }
            return m_license;
        }

       /**
        * Return the notice.
        * @return the notice
        */
        public Notice getNotice()
        {
            if( null == m_notice )
            {
                return createNotice();
            }
            return m_notice;
        }
    }

   /**
    * Header datatype definition.
    */
    public static class Header extends Resolver
    {
        private Publisher m_publisher;
        private Home m_home;
        private Legal m_legal;
        private Svn m_svn;
        private Packages m_packages;
        private Repository m_repository;
        private Docs m_docs;

       /**
        * Creation of a new header.
        * @param index the working index
        */
        public Header( AntFileIndex index )
        {
            super( index  );
            m_packages = new Packages( index );
        }

       /**
        * Creation and addition of a publisher entry.
        * @return the publisher entry
        */
        public Publisher createPublisher()
        {
            if( null == m_publisher )
            {
                m_publisher = new Publisher( getIndex() );
                return m_publisher;
            }
            else
            {
                throw new BuildException( "Multiple publisher entries not allowed." );
            }
        }

       /**
        * Return the publisher.
        * @return the publisher entry
        */
        public Publisher getPublisher()
        {
            if( null == m_publisher )
            {
                return createPublisher();
            }
            return m_publisher;
        }

       /**
        * Create and return a new package.
        * @return the package
        */
        public Packages createPackages()
        {
            return m_packages;
        }

       /**
        * Return the array of module packages.
        * @return the package array
        */
        public Package[] getPackages()
        {
            return m_packages.getPackages();
        }

       /**
        * Create and return the home declaration.
        * @return the home href
        */
        public Home createHome()
        {
            if( null == m_home )
            {
                m_home = new Home( getIndex() );
                return m_home;
            }
            else
            {
                throw new BuildException( "Multiple home entries not allowed." );
            }
        }

       /**
        * Return the home declaration.
        * @return the home href
        */
        public Home getHome()
        {
            if( null == m_home )
            {
                return createHome();
            }
            return m_home;
        }

       /**
        * Create and return the docs declaration.
        * @return the docs href
        */
        public Docs createDocs()
        {
            if( null == m_docs )
            {
                m_docs = new Docs( getIndex() );
                return m_docs;
            }
            else
            {
                throw new BuildException( "Multiple docs entries not allowed." );
            }
        }

       /**
        * Return the docs declaration.
        * @return the docs href
        */
        public Docs getDocs()
        {
            if( null == m_docs )
            {
                return createDocs();
            }
            return m_docs;
        }

       /**
        * Create and return the legal declaration.
        * @return the legal datastructure
        */
        public Legal createLegal()
        {
            if( null == m_legal )
            {
                m_legal = new Legal( getIndex() );
                return m_legal;
            }
            else
            {
                throw new BuildException( "Multiple legal entries not allowed." );
            }
        }

       /**
        * Return the legal declaration.
        * @return the legal datastructure
        */
        public Legal getLegal()
        {
            if( null == m_legal )
            {
                return createLegal();
            }
            return m_legal;
        }

       /**
        * Create and return the svn href.
        * @return the svn href
        */
        public Svn createSvn()
        {
            if( null == m_svn )
            {
                m_svn = new Svn( getIndex() );
                return m_svn;
            }
            else
            {
                throw new BuildException( "Multiple svn entries not allowed." );
            }
        }

       /**
        * Return the svn href.
        * @return the svn href
        */
        public Svn getSvn()
        {
            if( null == m_svn )
            {
                return createSvn();
            }
            return m_svn;
        }

       /**
        * Create and return a new repository href.
        * @return the repository href
        */
        public Repository createRepository()
        {
            if( null == m_repository )
            {
                m_repository = new Repository( getIndex() );
                return m_repository;
            }
            else
            {
                throw new BuildException( "Multiple repository entries not allowed." );
            }
        }

       /**
        * Return the repository href.
        * @return the repository href
        */
        public Repository getRepository()
        {
            if( null == m_repository )
            {
                return createRepository();
            }
            return m_repository;
        }
    }
}
