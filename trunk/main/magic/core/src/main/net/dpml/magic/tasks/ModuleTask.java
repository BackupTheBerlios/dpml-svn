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
import net.dpml.magic.model.*;
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

    public static final String MODULE_EXT = "module";

    public static final String REPOSITORY_KEY = "project.module.repository";
    public static final String SVN_KEY = "project.module.svn";
    public static final String HOME_KEY = "project.module.home";
    public static final String LICENSE_KEY = "project.module.license";
    public static final String NOTICE_KEY = "project.module.notice";
    public static final String PUBLISHER_KEY = "project.module.publisher";
    public static final String DOCS_KEY = "project.module.docs";
    public static final String PACKAGE_KEY = "project.module.package";

    //-----------------------------------------------------------------------
    // state
    //-----------------------------------------------------------------------
    private AntFileIndex m_target;
    private Header m_header;

    //-----------------------------------------------------------------------
    // ModuleTask
    //-----------------------------------------------------------------------

    public void setIndex( File target )
    {
        AntFileIndexBuilder builder = new AntFileIndexBuilder( getProject(), target );
        m_target = builder.getIndex();
    }

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

    public String getGroup()
    {
        return getDefinition().getInfo().getGroup();
    }

    public String getVersion()
    {
        return getDefinition().getInfo().getVersion();
    }

    public String getName()
    {
        return getDefinition().getInfo().getName();
    }

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

    private Header getHeader()
    {
        if( null == m_header )
        {
            return createHeader();
        }
        return m_header;
    }

    public void execute() throws BuildException
    {
        Definition definition = getDefinition();
        final Info info = definition.getInfo();
  
        if( false == info.isa( "module" ) )
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

    protected void writeModule( final Writer writer, Definition definition )
        throws IOException
    {
        Definition[] definitions = getPrivateIndex().getSubsidiaryDefinitions( definition );

        List external = new ArrayList();
        List modules = new ArrayList();
        for( int i=0; i<definitions.length; i++ )
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
            for( int i=0; i<uris.length; i++ )
            {
               writer.write( "\n    <import uri=\"" + uris[i] + "\"/>" );
            }
        }

        //
        // write out the list of projects (in the form of <resource> statements
        // for all projects that are within the scope of the module
        //

        writer.write( "\n\n    <!-- module resources -->" );
        for( int i=0; i<definitions.length; i++ )
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
            for( int i=0; i<resources.length; i++ )
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
        for( int i=0; i<refs.length; i++ )
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
            if(( null != module ) && !"".equals( module ) )
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
                    if( !(resource instanceof Definition) && !list.contains( resource ) )
                    {
                        list.add( resource );
                        ResourceRef[] refs = resource.getResourceRefs();
                        for( int i=0; i<refs.length; i++ )
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
        for( int i=0; i<types.length; i++ )
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
            for( int i=0; i<refs.length; i++ )
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
            for( int i=0; i<packages.length; i++ )
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
                // ignore
            }
        }
    }

    //-----------------------------------------------------------------------
    // nested classes
    //-----------------------------------------------------------------------

    public static class Resolver
    {
        private AntFileIndex m_index;

        public Resolver( AntFileIndex index )
        {
            m_index = index;
        }

        public AntFileIndex getIndex()
        {
            return m_index;
        }

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

    public static class Href extends Resolver
    {
        private String m_href;
        private String m_key;
        private String m_default;

        public Href( AntFileIndex index, String key, String fallback )
        {
            super( index );
            m_key = key;
            m_default = fallback;
        }

        public void setHref( String href )
        {
            m_href= href;
        }

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

    public static class Publisher extends Resolver
    {
        private String m_name;

        public Publisher( AntFileIndex index )
        {
            super( index );
        }

        public void setName( String name )
        {
            m_name = name ;
        }

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

    public static class Package extends Resolver
    {
        private String m_name;

        public Package( AntFileIndex index )
        {
            super( index );
        }

        public void setName( String name )
        {
            m_name = name ;
        }

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

    public static class Packages
    {
        private List m_packages = new ArrayList();
        private AntFileIndex m_index;

        public Packages( AntFileIndex index )
        {
            m_index = index;
        }

        public Package createPackage()
        {
            Package p = new Package( m_index );
            m_packages.add( p );
            return p;
        }

        public Package[] getPackages()
        {
            return (Package[]) m_packages.toArray( new Package[0] );
        }
    }


    public static class License extends Href
    {
        public License( AntFileIndex index )
        {
            super( index, LICENSE_KEY, "" );
        }
    }

    public static class Notice extends Href
    {
        public Notice( AntFileIndex index )
        {
            super( index, NOTICE_KEY, ""  );
        }
    }

    public static class Svn extends Href
    {
        public Svn( AntFileIndex index )
        {
            super( index, SVN_KEY, "" );
        }
    }

    public static class Home extends Href
    {
        public Home( AntFileIndex index )
        {
            super( index, HOME_KEY, "" );
        }
    }

    public static class Repository extends Href
    {
        public Repository( AntFileIndex index )
        {
            super( index, REPOSITORY_KEY, "" );
        }
    }

    public static class Docs extends Href
    {
        public Docs( AntFileIndex index )
        {
            super( index, DOCS_KEY, "" );
        }
    }

    public static class Legal extends Resolver
    {
        private License m_license;
        private Notice m_notice;

        public Legal( AntFileIndex index )
        {
            super( index );
        }

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

        public License getLicense()
        {
            if( null == m_license )
            {
                return createLicense();
            }
            return m_license;
        }

        public Notice getNotice()
        {
            if( null == m_notice )
            {
                return createNotice();
            }
            return m_notice;
        }
    }

    public static class Header extends Resolver
    {
        private Publisher m_publisher;
        private Home m_home;
        private Legal m_legal;
        private Svn m_svn;
        private Packages m_packages;
        private Repository m_repository;
        private Docs m_docs;

        public Header( AntFileIndex index )
        {
            super( index  );
            m_packages = new Packages( index );
        }

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

        public Publisher getPublisher()
        {
            if( null == m_publisher )
            {
                return createPublisher();
            }
            return m_publisher;
        }

        public Packages createPackages()
        {
            return m_packages;
        }

        public Package[] getPackages()
        {
            return m_packages.getPackages();
        }

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

        public Home getHome()
        {
            if( null == m_home )
            {
                return createHome();
            }
            return m_home;
        }

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

        public Docs getDocs()
        {
            if( null == m_docs )
            {
                return createDocs();
            }
            return m_docs;
        }

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

        public Legal getLegal()
        {
            if( null == m_legal )
            {
                return createLegal();
            }
            return m_legal;
        }

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

        public Svn getSvn()
        {
            if( null == m_svn )
            {
                return createSvn();
            }
            return m_svn;
        }

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
