/*
 * Copyright 2004 Cameron Taggart Copyright 2004 Dwango Wireless
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.magic.eclipse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Info;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.project.Context;
import net.dpml.magic.tasks.ContextualTask;

import net.dpml.transit.Artifact;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FilterSet;

/**
 * Creates Eclipse project files.
 * 
 * @author Andreas Ronge
 * @author Cameron Taggart
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library </a>
 */
public class EclipseTask extends ContextualTask
{

    /**
     * The ant filter token for the classpath.eclipse artifact
     */
    public static final String PROJECT_NAME = "PROJECT_NAME";

    /**
     * name of the generated eclipse project file
     */
    public static final String PROJECT_FILENAME = ".project";

    /**
     * name of the generated eclipse project classpath file
     */
    public static final String CLASSPATH_FILENAME = ".classpath";

    /**
     * the location of the created eclipse project files
     */
    public static final String TARGET_DIR = "target/eclipse";

    /**
     * The eclipse classpath variable pointing to the magic cache dir
     */
    public static final String ECLIPSE_MAGIC_VARIABLE = "MAGIC_CACHE";

    private File m_projectFile;

    private InputStream m_classpathStream;

    private String m_projectKeys;

    private String m_projectName;

    /**
     * Creates the two eclipse project files
     * 
     * @see org.apache.tools.ant.Task#execute()
     * @see #PROJECT_FILENAME
     * @see #CLASSPATH_FILENAME
     * @exception BuildException if a build error occurs
     */
    public void execute() throws BuildException
    {
        createProject();

        OutputStream classpathOutput = getOutputStream( CLASSPATH_FILENAME );

        try
        {
            createClasspath( classpathOutput );
        }
        catch( IOException e )
        {
            throw new BuildException( "Can't write to eclipse project files", e );
        }
        finally
        {
            closeStream( classpathOutput );
        }
    }

   /**
    * If this property is not set then the calling ant build.xml project key
    * will be used. A comma separated list of projects.
    * 
    * @param projectKeys the keys to the project we want to create an eclipse
    *            project for
    */
    public void setProjectKeys( String projectKeys )
    {
        m_projectKeys = projectKeys;
    }

   /**
    * Return the project keys.
    * @return a comma separated list of project keys that this eclipse project
    *         contains
    */
    public String getProjectKeys()
    {
        if( m_projectKeys == null || m_projectKeys.length() == 0 )
        {
            Context ctx = getContext();
            return ctx.getKey();
        }
        return m_projectKeys;
    }

   /**
    * The project name for the eclipse project. If this is not set then the
    * projectKeys will be used instead
    * 
    * @param projectName set the name of the eclipse project
    */
    public void setProjectName( String projectName )
    {
        m_projectName = projectName;
    }

   /**
    * Set the project name.
    * @return the name of the eclipse project
    */
    public String getProjectName()
    {
        if( m_projectName == null || m_projectName.length() == 0 )
        {
            return getProjectKeys();
        }
        return m_projectName;
    }

   /**
    * Set the project URI.
    * @param projectUri the uri for the eclipse project file
    */
    public void setProjectUri( String projectUri )
    {
        try
        {
            URL url = Artifact.toURL( new URI( projectUri ) );
            m_projectFile = (File) url.getContent( new Class[] {File.class} );
        }
        catch ( Throwable e )
        {
            final String error = "Could not resolve the project uri [" + projectUri + "]";
            throw new BuildException( error, e );
        }
    }

   /**
    * Set the classpath uri.
    * @param classpathUri the artifact uri for the eclipse classpath fragment
    */
    public void setClasspathUri( String classpathUri )
    {
        m_classpathStream = getInputStreamFromUrl( classpathUri );
    }

    // -------------------------------------------------------------------------
    // protected methods
    // -------------------------------------------------------------------------

   /**
    * Execute project creation.
    */
    protected void createProject()
    {
        if ( m_projectFile == null )
        {
            throw new BuildException( "Missing projectUri property for task" );
        }

        if ( !m_projectFile.canRead() )
        {
            throw new BuildException( "Can't read from resource '"
                    + m_projectFile.getAbsolutePath() );
        }

        Copy copy = new Copy();
        copy.setProject( getProject() );
        copy.setFile( m_projectFile );
        String targetLocation = TARGET_DIR + "/" + PROJECT_FILENAME;
        File targetFile = getProject().resolveFile( targetLocation );
        copy.setTofile( targetFile );
        copy.setOverwrite( true );
        copy.setFiltering( true );

        FilterSet filterSet = copy.createFilterSet();
        filterSet.addFilter( PROJECT_NAME, getProjectName() );

        copy.execute();
    }

    /**
     * Adds jar files and classpath header to the outputstream.
     * 
     * @param classpathOutput the classpath output stream
     * @exception IOException if an IO related error occurs
     */
    protected void createClasspath( OutputStream classpathOutput ) throws IOException
    {
        Writer writer = new OutputStreamWriter( classpathOutput );
        writer.write( "<?xml version=\"1.0\"?>\n" );
        writer.write( "<classpath>\n" );
        appendClasspathHeader( writer );
        String keys = getProjectKeys();
        Set excludeKeys = getExcludeKeysSet();        
        StringTokenizer tokenizer = new StringTokenizer( keys, ", ", false );
        while( tokenizer.hasMoreTokens() )
        {
            String key = tokenizer.nextToken();
            createProjectClasspath( writer, key, excludeKeys );
        }
        writer.write( "</classpath>\n" );
        writer.flush();
    }

    // -------------------------------------------------------------------------
    // private methods
    // -------------------------------------------------------------------------

    private Set getExcludeKeysSet()
    {
        Set excludeKeys = new HashSet();
        String keys = getProjectKeys();
        StringTokenizer tokenizer = new StringTokenizer( keys, ", ", false );
        while( tokenizer.hasMoreTokens() )
        {
            String key = tokenizer.nextToken();
            excludeKeys.add( key );
        }

        return excludeKeys;
    }

    private void createProjectClasspath( Writer writer, String key, Set excludeKeys )
            throws IOException
    {
        Definition projectDef = getProjectDefinition( key );
        ResourceRef[] resourceRefs = projectDef.getResourceRefs();
        Collection resources = getResources( resourceRefs );
        Iterator iter = resources.iterator();
        while( iter.hasNext() )
        {
            Resource resource = (Resource) iter.next();
            String resourceKey = resource.getKey();
            if( excludeKeys.contains( resourceKey ) )
            {
                continue;
            }

            Info info = resource.getInfo();
            if( !info.isa( "jar" ) )
            {
                continue;
            }
            writer.write( "  " + getEclipseClasspath( resource ) + "\n" );
            
            // add key to exclude keys so that we don't add it again
            excludeKeys.add( resourceKey );
        }
        
    }

    private Definition getProjectDefinition( String key )
    {
        AntFileIndex index = getIndex();
        return index.getDefinition( key );
    }

    private void appendClasspathHeader( Writer writer )
            throws FileNotFoundException, IOException
    {
        String inString = readInputStream( m_classpathStream );
        writer.write( inString );
    }

    private String readInputStream( InputStream iInStream )
    {
        InputStreamReader tInputReader = new InputStreamReader( iInStream );
        BufferedReader tReader = new BufferedReader( tInputReader );
        StringBuffer readData = new StringBuffer();
        try
        {
            int tTemp = tReader.read();
            while( tTemp != -1 )
            {
                readData.append( (char) tTemp );
                tTemp = tReader.read();
            }
            String tFileData = readData.toString();
            return tFileData;
        }
        catch ( IOException e )
        {
            throw new BuildException( "Can't read file", e );
        }
        finally
        {
            closeStream( iInStream );
        }
    }

    /**
     * Recursiv search of all resources dependencies this resource ref has.
     * 
     * @param resourceRefs the resourceRef for which the dependencies we want to
     *            find
     * @return collection of Resource objects
     */
    private Collection getResources( ResourceRef[] resourceRefs )
    {
        Set resources = new HashSet();
        for ( int i=0; i < resourceRefs.length; i++ )
        {
            Resource r = getIndex().getResource( resourceRefs[i] );
            resources.add( r );
            resources.addAll( getResources( r.getResourceRefs() ) );
        }
        return resources;
    }

    private String getEclipseClasspath( Resource r )
    {
        Info info = r.getInfo();
        StringBuffer sb = new StringBuffer();
        sb.append( "<classpathentry kind=\"var\" path=\""
                + ECLIPSE_MAGIC_VARIABLE + "/" );
        sb.append( info.getGroup() );
        sb.append( "/jars/" );
        sb.append( info.getName() );
        if( info.getVersion() != null )
        {
            sb.append( "-" );
            sb.append( info.getVersion() );
        }
        sb.append( "." );
        sb.append( info.getType( "jar" ) );
        sb.append( "\"/>" );
        return sb.toString();
    }

    private OutputStream getOutputStream( String filename )
    {
        String targetFile = TARGET_DIR + "/" + filename;
        File projectFile = getProject().resolveFile( targetFile );
        OutputStream out;
        try
        {
            out = new FileOutputStream( projectFile );
        }
        catch( FileNotFoundException e )
        {
            String fileLocation = projectFile.getAbsolutePath();
            throw new BuildException( "Can't create file '" + fileLocation + "'", e );
        }
        return out;
    }

    private InputStream getInputStreamFromUrl( String urlString )
    {
        InputStream in;
        
        // try first without transit
        try
        {
            URL url = new URL( urlString );
            in = (InputStream) url.getContent( );
        }
        catch( Throwable e )
        {
            // now try with transit
            try
            {
                URL url = Artifact.toURL( new URI( urlString ) );
                File file = ( File ) url.getContent( new Class[] {File.class} );
                in = new FileInputStream( file );
            }
            catch( Throwable e2 )
            {
                final String error = "Could not resolve uri [" + urlString + "]";
                throw new BuildException( error, e2 );
            }
        }
        
        // check inputstream
        if( in == null )
        {
            throw new BuildException( "Can't open resource at '" + urlString + "'" );
        }
        return in;
    }
    
    private void closeStream( OutputStream output )
    {
        if( null != output )
        {
            try
            {
                output.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    private void closeStream( InputStream input )
    {
        if( null != input )
        {
            try
            {
                input.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

}