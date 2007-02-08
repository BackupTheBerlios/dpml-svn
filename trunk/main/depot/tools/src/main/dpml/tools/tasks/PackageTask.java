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

package dpml.tools.tasks;

import dpml.lang.Classpath;
import dpml.lang.Info;
import dpml.lang.Part;
import dpml.lang.DOM3DocumentBuilder;

import dpml.tools.Context;
import dpml.tools.BuilderError;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;

import dpml.util.Category;
import net.dpml.lang.Strategy;
import net.dpml.lang.StrategyHandler;
import net.dpml.lang.PartContentHandler;

import dpml.library.Resource;
import dpml.library.Type;
import dpml.library.Scope;

import net.dpml.transit.Artifact;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Copy;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Cleanup of generated target directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PackageTask extends GenericTask
{
    private static final DOM3DocumentBuilder BUILDER = new DOM3DocumentBuilder();

   /**
    * Compiles main and test classes resulting in the creation of the 
    * target/classes/main and target/classes/test directories.
    */
    public void execute()
    {
        Context context = getContext();
        Project project = context.getProject();
        Resource resource = context.getResource();
        
        Type[] types = resource.getTypes();
        for( Type type : types )
        {
            build( project, context, resource, type );
        }
        
        /*
        // handle jar file packaging
        
        if( resource.isa( "jar" ) )
        {
            File base = context.getTargetClassesMainDirectory();
            final File jar = getJarFile( context );
            if( base.exists() )
            {
                final JarTask task = new JarTask();
                task.setProject( project );
                task.setTaskName( "jar" );
                task.setSrc( base );
                task.setDest( jar );
                task.init();
                task.execute();
            }
        }
        
        // handle part production
        
        if( resource.isa( "part" ) )
        {
            try
            {
                PartTask task = new PartTask();
                task.setProject( project );
                task.setTaskName( "part" );
                task.init();
                task.execute();
            }
            catch( BuildException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                final String error = 
                  "Unexpected failure during part externalization in ["
                    + resource.getName()
                    + "]";
                throw new BuilderError( error, e, getLocation() );
            }
        }
        
        // handle module export
        
        if( resource.isa( "module" ) )
        {
            final ModuleTask task = new ModuleTask();
            task.setProject( project );
            task.setTaskName( "module" );
            task.init();
            task.execute();
        }
        */
    }
    
    private void build( Project project, Context context, Resource resource, Type type )
    {
        String id = type.getID();
        if( "jar".equals( id ) )
        {
            File base = context.getTargetClassesMainDirectory();
            if( base.exists() )
            {
                final File jar = type.getFile( true );
                final JarTask task = new JarTask();
                task.setProject( project );
                task.setTaskName( "jar" );
                task.setSrc( base );
                task.setDest( jar );
                task.init();
                task.execute();
            }
        }
        else if( "part".equals( id ) )
        {
            buildPart( project, context, resource, type );
        }
        else if( "module".equals( id ) )
        {
            final ModuleTask task = new ModuleTask();
            task.setProject( project );
            task.setTaskName( "module" );
            task.init();
            task.execute();
        }
        else if( null != type.getSource() )
        {
            buildByCopy( type );
        }
    }
    
    private void buildByCopy( Type type )
    {
        log( "Copying [" + type.getName() + "#" + type.getID() + "] to deliverables" );
        
        String path = type.getSource();
        Resource resource = type.getResource();
        File base = resource.getBaseDir();
        File source = new File( base, path );
        File file = type.getFile( true );
        File dir = file.getParentFile();
        if( null != dir )
        {
            dir.mkdirs();
        }
        
        boolean flag = type.getBooleanProperty( "project.package.include-in-classes", false );
        if( flag )
        {
            String group = getGroup( resource );
            String filename = type.getName() + "." + type.getID();
            File ddir = new File( base, "target/classes/main" );
            File root = new File( ddir, group );
            root.mkdirs();
            File destination = new File( root, filename );
            final Copy ccopy = (Copy) getProject().createTask( "copy" );
            ccopy.setTaskName( getTaskName() );
            ccopy.setFile( source );
            ccopy.setTofile( destination );
            ccopy.init();
            ccopy.execute();
        }
        
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTaskName( getTaskName() );
        copy.setFile( source );
        copy.setTofile( file );
        copy.init();
        copy.execute();
    }
    
    private String getGroup( Resource resource )
    {
        Resource parent = resource.getParent();
        if( null == parent )
        {
            return null;
        }
        else
        {
            return parent.getResourcePath();
        }
    }
    
    private void buildPart( Project project, Context context, Resource resource, Type type )
    {
        String source = type.getSource();
        if( null == source )
        {
            final String error = 
              "Missing source declaration in part type production statement. ["
                + resource.getName()
                + "]";
            throw new BuilderError( error, null, getLocation() );
        }
        else
        {
            String resolved = resource.resolve( source );
            log( "Creating part: " + resolved );
            File basedir = resource.getBaseDir();
            File input = new File( basedir, resolved );
            if( !input.exists() )
            {
                final String error = 
                  "Part input not found."
                  + "\nPath: " + source
                  + "\nExpanded: " + resolved
                  + "\nFile: " + input.getAbsolutePath();
                throw new BuilderError( error, null, getLocation() );
            }
            else
            {
                OutputStream output = null;
                try
                {
                
                    Info info = getInfo( resource );
                    Classpath classpath = getClasspath( resource, type.getTest() );
                    URI uri = input.toURI();
                    final Document document = BUILDER.parse( uri );
                    final Element root = document.getDocumentElement();
                    StrategyHandler handler = PartContentHandler.getStrategyHandler( root );
                    
                    ClassLoader parent = ClassLoader.getSystemClassLoader();
                    ClassLoader classloader = getRuntimeClassLoader( parent, resource );
                    Strategy strategy = handler.build( classloader, root, resource, null, null );
                    Part part = new Part( info, classpath, strategy );
                    File file = type.getFile( true );
                    File dir = file.getParentFile();
                    if( null != dir )
                    {
                        dir.mkdirs();
                    }
                    file.createNewFile();
                    output = new FileOutputStream( file );
                    part.save( output );
                    if( !type.getTest() )
                    {
                        checksum( file );
                        asc( file );
                    }
                }
                catch( Exception e )
                {
                    final String error = 
                      "Unable to construct part deliverable: " 
                      + "\nError: " + e.getClass().getName()
                      + "\nCause: " + e.getCause()
                      + "\nMessage: " + e.getMessage();
                    throw new BuilderError( error, e, getLocation() );
                }
                finally
                {
                    if( null != output )
                    {
                        try
                        {
                            output.close();
                        }
                        catch( IOException e )
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    private Info getInfo( Resource resource )
    {
        Type type = resource.getType( "part" );
        Artifact artifact = type.getArtifact();
        URI uri = artifact.toURI();
        String title = resource.getInfo().getTitle();
        String description = resource.getInfo().getDescription();
        return new Info( uri, title, description );
    }
    
    protected Classpath getClasspath( Resource resource, boolean test ) throws IOException
    {
        URI[] sysUris = getURIs( resource, Category.SYSTEM );
        URI[] publicUris = getURIs( resource, Category.PUBLIC );
        URI[] protectedUris = getURIs( resource, Category.PROTECTED );
        URI[] privateUris = getURIs( resource, Category.PRIVATE, true, test );
        return new Classpath( sysUris, publicUris, protectedUris, privateUris );
    }

    private URI[] getURIs( Resource resource, Category category ) throws IOException
    {
        return getURIs( resource, category, false, false );
    }
    
    private URI[] getURIs( Resource resource, Category category, boolean self, boolean test ) throws IOException
    {
        Resource[] resources = resource.getClasspathProviders( category );
        ArrayList<URI> list = new ArrayList<URI>();
        for( int i=0; i<resources.length; i++ )
        {
            Resource r = resources[i];
            addURI( list, r );
        }
        if( self && resource.isa( "jar" ) )
        {
            addSelf( list, resource, test );
        }
        URI[] uris = list.toArray( new URI[0] );
        return uris;
    }
    
    private void addURI( List<URI> list, Resource resource )  throws IOException
    {
        addURI( list, resource, true );
    }
    
    private void addURI( List<URI> list, Resource resource, boolean resolve )  throws IOException
    {
        if( resource.isa( "jar" ) )
        {
            Type type = resource.getType( "jar" );
            Artifact artifact = getArtifact( type, resolve );
            URI uri = artifact.toURI();
            list.add( uri );
        }
    }
    
    private Artifact getArtifact( Type type, boolean resolve ) throws IOException
    {
        if( resolve )
        {
            return type.getResolvedArtifact();
        }
        else
        {
            return type.getArtifact();
        }
    }
    
    private void addSelf( List<URI> list, Resource resource, boolean test )  throws IOException
    {
        if( test )
        {
            try
            {
                Type type = resource.getType( "jar" );
                File local = type.getFile( true );
                list.add( local.toURI() );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error while attempting to resolve resource.";
                IOException ioe = new IOException( error );
                ioe.initCause( e );
                throw ioe;
            }
        }
        else
        {
            addURI( list, resource, false );
        }
    }
    
    ClassLoader getRuntimeClassLoader( ClassLoader parent, Resource resource ) throws Exception
    {
        boolean flag = resource.isa( "jar" );
        ArrayList<URL> list = new ArrayList<URL>();
        Resource[] providers = resource.getClasspathProviders( Scope.RUNTIME );
        for( Resource provider : providers )
        {
            Type type = provider.getType( "jar" );
            URI uri = type.getResolvedArtifact().toURI();
            list.add( Artifact.toURL( uri ) );
        }
        if( flag )
        {
            Type type = resource.getType( "jar" );
            File local = type.getFile( true );
            list.add( local.toURI().toURL() );
        }
        URL[] urls = list.toArray( new URL[0] );
        return new URLClassLoader( urls, parent );
    }
}
