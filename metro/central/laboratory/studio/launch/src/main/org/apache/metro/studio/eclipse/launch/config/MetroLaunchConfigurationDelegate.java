/*
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metro.studio.eclipse.launch.config;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Vector;

import org.apache.metro.studio.eclipse.launch.MetroStudioLaunch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.osgi.util.ManifestElement;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team </a>
 *  
 */
public class MetroLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate 
    implements ILaunchConfigurationDelegate
{
    private static final String METRO_HOME_KEY = "merlin.home";

    final String CLASS_TO_LAUNCH = "org.apache.avalon.merlin.cli.Main";

    public void launch( ILaunchConfiguration config, String mode,
                        ILaunch launch, IProgressMonitor monitor ) 
        throws CoreException
    {
        if( monitor == null )
        {
            monitor = new NullProgressMonitor();
        }
        IVMInstall vmInstall = getVMInstall( config );
        IVMRunner vmRunner = getVMRunner( mode, config, vmInstall );

        // Program & VM args
        String programArguments = getProgramArguments( config );
        String vmArguments = getVMArguments(config);
        ExecutionArguments executionArguments = 
            new ExecutionArguments( vmArguments, programArguments );

        // Classpath
        String[] classpath = getClasspath( config );

        // Bootpath
        String[] bootClasspath = getBootClasspath( vmInstall );
        String[] vmArgs = new String[ 1 ];
        vmArgs[0] = executionArguments.getVMArguments();
        
        if (bootClasspath == null)
        {
            abort( "Boot classpath not resolved", null, 5 ); //$NON-NLS-1$
        }

        VMRunnerConfiguration runnerConfig = 
            new VMRunnerConfiguration( CLASS_TO_LAUNCH, classpath );
        runnerConfig.setVMArguments( vmArgs );
        runnerConfig.setProgramArguments(
            executionArguments.getProgramArgumentsArray() );
        runnerConfig.setBootClassPath( bootClasspath );

        vmRunner.run( runnerConfig, launch, monitor );
        // get return code
        // setExitCode( config, launch );

        monitor.done();
    }

    private void setExitCode( ILaunchConfiguration config, ILaunch launch )
    {
        try
        {
            IProcess[] process = launch.getProcesses();
            int exit = process[0].getExitValue();
            ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
            workingCopy.setAttribute( ILaunchConfigConstants.EXIT_CODE, exit );
            // dont need to save
        } catch( DebugException e )
        {
            MetroStudioLaunch.log( e, "Can't get exit code");
        } catch( CoreException e )
        {
            MetroStudioLaunch.log( e, "Can't get process exit code" );
        } 
    }
    
    private String[] getBootClasspath( IVMInstall vm )
    {
        LibraryLocation[] vmLibs = JavaRuntime.getLibraryLocations( vm );
        int classesType = IRuntimeClasspathEntry.STANDARD_CLASSES;
        return loadClasspath( vmLibs, classesType );
    }

    public String[] getClasspath( ILaunchConfiguration configuration )
    {
        LibraryLocation[] serverLibs = getLibraryLocations();
        int classesType = IRuntimeClasspathEntry.USER_CLASSES;
        return loadClasspath( serverLibs, classesType );
    }
      
    private String[] loadClasspath( LibraryLocation[] libs, int classesType )
    {
        ArrayList runtimeClasspaths = new ArrayList();
        for (int i = 0; i < libs.length; i++)
        {
            LibraryLocation libLocation = libs[i];
            
            final IPath sysLibPath = libLocation.getSystemLibraryPath();
            IRuntimeClasspathEntry runtimeEntry = 
                JavaRuntime.newArchiveRuntimeClasspathEntry( sysLibPath );
                
            final IPath sysLibSrcPath = libLocation.getSystemLibrarySourcePath();
            runtimeEntry.setSourceAttachmentPath( sysLibSrcPath );
            
            final IPath packageRootPath = libLocation.getPackageRootPath();
            runtimeEntry.setSourceAttachmentRootPath( packageRootPath );
            
            int classes = classesType;
            runtimeEntry.setClasspathProperty( classes );
            
            final String classpathEntry = runtimeEntry.getLocation();
            runtimeClasspaths.add( classpathEntry );
        }

        if( runtimeClasspaths.size() == 0 )
        {
            return null;
        } 
        else
        {
            int size = runtimeClasspaths.size();
            String[] classpaths = new String[ size ];
            runtimeClasspaths.toArray( classpaths );
            return classpaths ;
        }

    }

    protected LibraryLocation[] getLibraryLocations()
    {
        /*
         * String header = bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH);
         * ManifestElement[] elements = ManifestElement.parseHeader(
         * Constants.BUNDLE_CLASSPATH, header);
         */
        Vector libraryLocations = new Vector();
        try
        {
            String pluginID = MetroStudioLaunch.PLUGIN_ID;
            String bundleClasspath = Constants.BUNDLE_CLASSPATH;
            Bundle bundle = Platform.getBundle( pluginID );
            Dictionary headers = bundle.getHeaders();
            String header = (String) headers.get( bundleClasspath );
            
            ManifestElement[] elements = 
                ManifestElement.parseHeader( bundleClasspath, header);
                
            IPath pluginPath = MetroStudioLaunch.getDefault().getPluginLocation();

            for (int i = 0; i < elements.length; i++)
            {
                String mfValue = elements[i].getValue();
                LibraryLocation libLocation = new LibraryLocation( 
                    pluginPath.append( mfValue ), Path.EMPTY, Path.EMPTY
                );
                libraryLocations.add( libLocation );
            }

            // TODO:  Niclas Removed this hibernate reference.
            /*
            pluginPath = pluginPath.append("/lib/hibernate");
            String[] path = pluginPath.toFile().list();
            if (path != null)
            {
                for (int i = 0; i < path.length; i++)
                {
                    if (path[i].endsWith(".jar"))
                    { //$NON-NLS-1$
                        libraryLocations.add(new LibraryLocation(pluginPath
                                .append(path[i]), Path.EMPTY, Path.EMPTY));
                    }
                }
            }
            */
        } catch( BundleException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int size = libraryLocations.size();
        LibraryLocation[] result = new LibraryLocation[ size ];
        libraryLocations.toArray( result );
        return result; 
    }

    public String getVMArguments( ILaunchConfiguration configuration )
    {
        String fallback = System.getProperty( "user.home" ) + "/.merlin";
        String home = System.getProperty( METRO_HOME_KEY, fallback );

        return "-Djava.security.policy=" + home
                + "/bin/security.policy -Dmerlin.home=" + home;

    }

    public String getProgramArguments( ILaunchConfiguration config )
    {
        StringBuffer param = new StringBuffer();
        try
        {
            // create path to output location
            IWorkspace workspace = MetroStudioLaunch.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            String confName = config.getName();
            IProject project = root.getProject( confName );
            IJavaProject javaProject = JavaCore.create( project );
            param.append( '"' );
            
            IPath projLocation = project.getLocation();
            IPath outputLocation = javaProject.getOutputLocation();
            String lastSegment = outputLocation.lastSegment();
            IPath path = projLocation.append( lastSegment );
            param.append( path.toString() );
            param.append('"');
        } catch( JavaModelException e )
        {
            e.printStackTrace();
        }
        return param.toString() + " -execute -debug";
    }

    private IVMRunner getVMRunner( String mode, ILaunchConfiguration config,
                                   IVMInstall vmInstall ) 
        throws CoreException
    {
        // Virtual machine
        IVMRunner vmRunner = vmInstall.getVMRunner( mode );
        if( vmRunner == null )
        {
            String[] args = new String[] { vmInstall.getName() };
            if( mode == ILaunchManager.DEBUG_MODE )
            {
                String rawMess = "JRE {0} does not support debug mode."; //$NON-NLS-1$
                String mess = MessageFormat.format( rawMess, args ); 
                abort( mess, null, 3 );
            } else
            {
                String rawMess = "JRE {0} does not support run mode."; //$NON-NLS-1$
                String mess = MessageFormat.format( rawMess, args ); 
                abort( mess, null, 4 );
            }
        }
        return vmRunner;
    }

    protected void abort( String message, Throwable exception, int code )
        throws CoreException
    {
        ResourcesPlugin plugin = ResourcesPlugin.getPlugin();
        Bundle bundle = plugin.getBundle();
        String symName = bundle.getSymbolicName();
        int error = IStatus.ERROR;
        Status status = new Status( error, symName, code, message, exception );
        throw new CoreException( status );
    }
}

