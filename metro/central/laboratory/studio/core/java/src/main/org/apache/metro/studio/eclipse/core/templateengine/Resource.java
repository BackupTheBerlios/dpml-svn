/*
 *     Copyright 2004. The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 */
package org.apache.metro.studio.eclipse.core.templateengine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.Iterator;

import org.apache.metro.studio.eclipse.core.tools.ClassNameAnalyzer;
import org.apache.metro.studio.eclipse.core.tools.DynProjectParam;
import org.apache.metro.studio.eclipse.core.tools.SystemTool;

import org.eclipse.core.resources.IFolder;

import org.eclipse.core.runtime.IPath;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team </a>
 */
public class Resource
{
    /**
     * This is the name of the root segment (one of the folders directly under
     * the projects name)
     */
    private String rootSegment;

    /**
     * This is the pointer to the templates source
     */
    private String sourceFilePathName;

    /**
     * The packagename, where the output should be stored
     */
    private String packageName;

    /**
     * the parameter object which hold all dynamic parameters
     */
    private transient DynProjectParam dynParam;

    /**
     * @return Returns the rootSegment.
     */
    public String getRootSegment()
    {
        return rootSegment;
    }

    /**
     * @param rootSegment
     *            The rootSegment to set.
     */
    public void setRootSegment( String rootSegment )
    {
        this.rootSegment = rootSegment;
    }

    /**
     * @return Returns the sourceFilePathName.
     */
    public String getSourceFilePathName()
    {
        return sourceFilePathName;
    }

    /**
     * @param sourceFilePathName
     *            The sourceFilePathName to set.
     */
    public void setSourceFilePathName( String sourceFilePathName )
    {
        this.sourceFilePathName = sourceFilePathName;
    }

    /**
     * opens the templatefile and replaces all occurencies of the parameters
     * contained in the map. Output is writen to <folder>
     * 
     * @param templateName
     * @param map
     * @param folder
     */
    private void createFromTemplate( 
        String templateName, DynProjectParam map, String destinationPath )
    {
        InputStream input = null;
        InputStreamReader file = null;
        BufferedReader reader = null;
        FileOutputStream ostream = null;
        OutputStreamWriter out = null;
        
        try
        {
            if( map == null )
            {
                map = new DynProjectParam();
            }
            File inFile = new File( templateName );
            input = new FileInputStream( inFile );
            file = new InputStreamReader( input );
            reader = new BufferedReader( file );

            File outFile = new File( destinationPath );
            ostream = new FileOutputStream( outFile );
            out = new OutputStreamWriter( ostream );

            String line = reader.readLine();
            while( line != null )
            {
                line = replaceParam( line, map );
                out.write( line );
                out.write( "\n" );
                
                line = reader.readLine();
            }
            out.flush();
        } catch( Exception e )
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if( reader != null )
                    reader.close();

                if( file != null )
                    file.close();

                if( input != null )
                    input.close();

                if( out != null )
                    out.close();
                
                if( ostream != null )
                    ostream.close();
            } catch( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    private String replaceParam( String line, DynProjectParam map )
    {
        Iterator it = map.keySet().iterator();
        while( it.hasNext() )
        {
            String key = (String) it.next();
            if( key.startsWith( "%" ) )
            {                
                /*
                * to retain 1.3.1 compatibiliy (WSAD) dont use
                * "replace" line = line.replaceAll(key, (String)
                * map.get(key));
                */
                String value = (String) map.get( key );
                line = SystemTool.replaceAll( line, key, value );
            }
        }
        return line;
    }

    private String getFileName()
    {
        ClassNameAnalyzer analyzer = new ClassNameAnalyzer();
        analyzer.setPath( sourceFilePathName );
        return replaceParam( analyzer.getFileName(), getDynParam() );
    }

    /**
     * @param directory.
     *            Destination directory template
     * @param templateName
     */
    public void create( DirectoryTemplate directoryTemplate, DynProjectParam param )
    {
        setDynParam( param );
        String destinationFilePathName = null;
        Directory dir = directoryTemplate.getDirectory( rootSegment );
        if( dir == null )
            return;
        dir = dir.appendPackage( getPackageName() );

        IFolder folder = dir.getEclipseFolder();
        IPath location = folder.getLocation();
        destinationFilePathName = location.toString();
        
        destinationFilePathName = destinationFilePathName + "/" + getFileName();
        createFromTemplate( sourceFilePathName, param, destinationFilePathName );
    }

    /**
     * @return Returns the packageName.
     */
    public String getPackageName()
    {
        return replaceParam( packageName, getDynParam() );
    }

    /**
     * @param packageName
     *            The packageName to set.
     */
    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    /**
     * @return Returns the dynParam.
     */
    public DynProjectParam getDynParam()
    {
        return dynParam;
    }

    /**
     * @param dynParam
     *            The dynParam to set.
     */
    public void setDynParam( DynProjectParam dynParam )
    {
        this.dynParam = dynParam;
    }
}
