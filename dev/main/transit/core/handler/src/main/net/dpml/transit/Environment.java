/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004 Apache Software Foundation
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

package net.dpml.transit;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Properties;
import java.util.Enumeration;


/**
 * Encapsulates operating system and shell specific access to environment
 * variables.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Environment.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 */
public class Environment extends Properties
{
   /**
    * os.name System property
    */
    public static final String OSNAME = System.getProperty( "os.name" );

   /**
    * user.name System property
    */
    public static final String USERNAME = System.getProperty( "user.name" );

   /**
    * the user's platform specific shell executable
    */
    private static String m_SHELL = null;

   /**
    * the last Env instance created
    */
    private static Environment m_INSTANCE = null;

   /**
    * Creates a snapshot of the current shell environment variables for a user.
    *
    * @throws EnvironmentException if there is an error accessing the environment
    */
    public Environment() throws EnvironmentException
    {
        Properties properties = getEnvVariables();
        Enumeration list = properties.propertyNames();
        while ( list.hasMoreElements() )
        {
            String key = ( String ) list.nextElement();
            setProperty( key, properties.getProperty( key ) );
        }
        m_INSTANCE = this;
    }

    /**
     * Gets a copy of the last Environment instance without parsing the user's shell
     * environment.  Use this method if you do not want to reparse the
     * environment every time an environment variable is accessed.  If an
     * environment has not been created yet one is created then cloned
     * and a copy is returned instead of returning null.
     *
     * @return a copy of the last Environment object created
     * @throws EnvironmentException if there is an error accessing the environment
     */
    Environment getLastEnv() throws EnvironmentException
    {
        if( m_INSTANCE == null )
        {
            m_INSTANCE = new Environment();
        }

        // return cloned copy so there is no cross interference
        return ( Environment ) m_INSTANCE.clone();
    }


    /**
     * Gets the value of a shell environment variable.
     *
     * @param name the name of variable
     * @return the String representation of an environment variable value
     * @throws EnvironmentException if there is a problem accessing the environment
     */
    public static String getEnvVariable( String name )
        throws EnvironmentException
    {
        String osName = System.getProperty( "os.name" );

        if( isUnix() )
        {
            Properties properties = getUnixShellVariables();
            return properties.getProperty( name );
        }
        else if( isWindows() )
        {
            return getWindowsShellVariable( name );
        }

        throw new EnvironmentException( name,
            "Unrecognized operating system: " + osName );
    }

    /**
     * Checks to see if the operating system is a UNIX variant.
     *
     * @return true of the OS is a UNIX variant, false otherwise
     */
    public static boolean isUnix()
    {
        if( -1 != OSNAME.indexOf( "Linux" )
               || -1 != OSNAME.indexOf( "SunOS" )
               || -1 != OSNAME.indexOf( "Solaris" )
               || -1 != OSNAME.indexOf( "MPE/iX" )
               || -1 != OSNAME.indexOf( "AIX" )
               || -1 != OSNAME.indexOf( "FreeBSD" )
               || -1 != OSNAME.indexOf( "Irix" )
               || -1 != OSNAME.indexOf( "Digital Unix" )
               || -1 != OSNAME.indexOf( "HP-UX" )
               || -1 != OSNAME.indexOf( "Mac OS X" ) )
        {
            return true;
        }

        return false;
    }


    /**
     * Checks to see if the operating system is a Windows variant.
     *
     * @return true of the OS is a Windows variant, false otherwise
     */
    public static boolean isWindows()
    {
        return ( -1 != OSNAME.indexOf( "Windows" ) );
    }

    /**
     * Checks to see if the operating system is NetWare.
     *
     * @return true of the OS is NetWare, false otherwise
     */
    public static boolean isNetWare()
    {
        return ( -1 != OSNAME.indexOf( "netware" ) );
    }

    /**
     * Checks to see if the operating system is OpenVMS.
     *
     * @return true of the OS is a NetWare variant, false otherwise
     */
    public static boolean isOpenVMS()
    {
        return ( -1 != OSNAME.indexOf( "openvms" ) );
    }

    /**
     * Gets all environment variables within a Properties instance where the
     * key is the environment variable name and value is the value of the
     * property.
     *
     * @return the environment variables and values as Properties
     * @throws EnvironmentException if os is not recognized
     */
    public static Properties getEnvVariables() throws EnvironmentException
    {
        if( isUnix() )
        {
            return getUnixShellVariables();
        }

        if( isWindows() )
        {
            return getWindowsShellVariables();
        }

        throw new EnvironmentException(
            new UnsupportedOperationException( "Environment operations not "
            + "supported on unrecognized operatings system" ) );
    }


    /**
     * Gets the user's shell executable.
     *
     * @return the shell executable for the user
     * @throws EnvironmentException the there is a problem accessing shell
     * information
     */
    public static String getUserShell() throws EnvironmentException
    {
        if( -1 != OSNAME.indexOf( "Mac OS X" ) )
        {
            return getMacUserShell();
        }

        if( isWindows() )
        {
            return getWindowsUserShell();
        }

        throw new EnvironmentException(
            new UnsupportedOperationException( "Environment operations not "
                + "supported on unrecognized operatings system" ) );
    }

    // ------------------------------------------------------------------------
    // Private UNIX Shell Operations
    // ------------------------------------------------------------------------

    /**
     * Gets the default login shell used by a mac user.
     *
     * @return the Mac user's default shell as referenced by cmd:
     *      'nidump passwd /'
     * @throws EnvironmentException if os information is not resolvable
     */
    private static String getMacUserShell()
        throws EnvironmentException
    {
        Process process = null;
        BufferedReader reader = null;

        if( null != m_SHELL )
        {
            return m_SHELL;
        }

        try
        {
            String entry = null;
            String [] args = {"nidump", "passwd", "/"};
            process = Runtime.getRuntime().exec( args );
            reader = new BufferedReader(
                    new InputStreamReader( process.getInputStream() ) );

            while ( null != ( entry = reader.readLine() ) )
            {
                // Skip entries other than the one for this username
                if( !entry.startsWith( USERNAME ) )
                {
                    continue;
                }

                // Get the shell part of the passwd entry
                int index = entry.lastIndexOf( ':' );

                if( index == -1 )
                {
                    throw new EnvironmentException(
                        "passwd database contains malformed user entry for "
                        + USERNAME );
                }

                m_SHELL = entry.substring( index + 1 );
                return m_SHELL;
            }

            process.waitFor();
            reader.close();
        }
        catch( Throwable t )
        {
            throw new EnvironmentException( t );
        }
        finally
        {
            if( process != null )
            {
                process.destroy();
            }

            try
            {
                if( null != reader )
                {
                    reader.close();
                }
            }
            catch( IOException e )
            {
                // do nothing
                final boolean ignorable = true;
            }
        }

        throw new EnvironmentException( "User " + USERNAME
            + " is not present in the passwd database" );
    }


   /**
    * Adds a set of Windows variables to a set of properties.
    * @return the environment properties
    * @exception EnvironmentException if an error occurs
    */
    private static Properties getUnixShellVariables()
        throws EnvironmentException
    {
        Process process = null;
        Properties properties = new Properties();

        // Read from process here
        BufferedReader reader = null;

        // fire up the shell and get echo'd results on stdout
        try
        {
            String [] args = {getUnixEnv()};
            process = Runtime.getRuntime().exec( args );
            reader = new BufferedReader(
                    new InputStreamReader( process.getInputStream() ) );

            String line = null;
            while ( null != ( line = reader.readLine() ) )
            {
                int index = line.indexOf( '=' );

                if( -1 == index )
                {
                    if( line.length() != 0 )
                    {
                        System.err.println(
                          "Skipping line - could not find '=' in"
                          + " line: '" + line + "'" );
                    }
                    continue;
                }

                String name = line.substring( 0, index );
                String value = line.substring( index + 1, line.length() );
                properties.setProperty( name, value );
            }

            process.waitFor();
            reader.close();
        }
        catch( Throwable t )
        {
            throw new EnvironmentException( "NA", t );
        }
        finally
        {
            process.destroy();

            try
            {
                if( null != reader )
                {
                    reader.close();
                }
            }
            catch( IOException e )
            {
                // ignore
                final boolean ignorable = true;
            }
        }

        // Check that we exited normally before returning an invalid output
        if( 0 != process.exitValue() )
        {
            throw new EnvironmentException(
              "Environment process failed "
              + " with non-zero exit code of "
              + process.exitValue() );
        }

        return properties;
    }


    /**
     * Gets the UNIX env executable path.
     *
     * @return the absolute path to the env program
     * @throws EnvironmentException if it cannot be found
     */
    private static String getUnixEnv() throws EnvironmentException
    {
        File env = new File( "/bin/env" );

        if( env.exists() && env.canRead() && env.isFile() )
        {
            return env.getAbsolutePath();
        }

        env = new File( "/usr/bin/env" );
        if( env.exists() && env.canRead() && env.isFile() )
        {
            return env.getAbsolutePath();
        }

        throw new EnvironmentException(
                "Could not find the UNIX env executable" );
    }


    // ------------------------------------------------------------------------
    // Private Windows Shell Operations
    // ------------------------------------------------------------------------


    /**
     * Gets the shell used by the Windows user.
     *
     * @return the shell: cmd.exe or command.com.
     */
    private static String getWindowsUserShell()
    {
        if( null != m_SHELL )
        {
            return m_SHELL;
        }

        if( -1 != OSNAME.indexOf( "98" )
          || -1 != OSNAME.indexOf( "95" )
          || -1 != OSNAME.indexOf( "Me" ) )
        {
            m_SHELL = "command.com";
            return m_SHELL;
        }

        m_SHELL = "cmd.exe";
        return m_SHELL;
    }


    /**
     * Adds a set of Windows variables to a set of properties.
     * @return the environment properties
     * @exception EnvironmentException if an error occurs
     */
    private static Properties getWindowsShellVariables()
        throws EnvironmentException
    {
        String line = null;
        Process process = null;
        BufferedReader reader = null;
        Properties properties = new Properties();

        // build the command based on the shell used: cmd.exe or command.com
        StringBuffer buffer = new StringBuffer( getWindowsUserShell() );
        buffer.append( " /C SET" );

        // fire up the shell and get echo'd results on stdout
        try
        {
            process = Runtime.getRuntime().exec( buffer.toString() );
            reader = new BufferedReader(
                    new InputStreamReader( process.getInputStream() ) );
            while ( null != ( line = reader.readLine() ) )
            {
                int index = line.indexOf( '=' );

                if( -1 == index )
                {
                    System.err.println( "Skipping line - could not find '=' in"
                            + " line: '" + line + "'" );
                    continue;
                }

                String name = line.substring( 0, index );
                String value = line.substring( index + 1, line.length() );
                properties.setProperty( name, value );
            }

            process.waitFor();
            reader.close();
        }
        catch( Throwable t )
        {
            throw new EnvironmentException( t );
        }
        finally
        {
            process.destroy();

            try
            {
                if( null != reader )
                {
                    reader.close();
                }
            }
            catch( IOException e )
            {
                // ignore
                final boolean ignorable = true;
            }
        }

        if( 0 != process.exitValue() )
        {
            throw new EnvironmentException( "Environment process failed"
                    + " with non-zero exit code of " + process.exitValue() );
        }

        return properties;
    }


    /**
     * Gets the value for a windows command shell environment variable.
     *
     * @param name the name of the variable
     * @return the value of the variable
     * @throws EnvironmentException if there is an error accessing the value
     */
    private static String getWindowsShellVariable( String name )
        throws EnvironmentException
    {
        String value = null;
        Process process = null;
        BufferedReader reader = null;

        StringBuffer buffer = new StringBuffer( getWindowsUserShell() );
        buffer.append( " /C echo %" );
        buffer.append( name );
        buffer.append( '%' );

        // fire up the shell and get echo'd results on stdout
        try
        {
            process = Runtime.getRuntime().exec( buffer.toString() );
            reader = new BufferedReader(
                    new InputStreamReader( process.getInputStream() ) );
            value = reader.readLine();
            process.waitFor();
            reader.close();
        }
        catch( Throwable t )
        {
            throw new EnvironmentException( name, t );
        }
        finally
        {
            process.destroy();

            try
            {
                if( null != reader )
                {
                    reader.close();
                }
            }
            catch( IOException e )
            {
                // ignore
                final boolean ignorable = true;
            }
        }

        if( 0 == process.exitValue() )
        {
            // Handle situations where the env property does not exist.
            if( value.startsWith( "%" ) && value.endsWith( "%" ) )
            {
                return null;
            }

            return value;
        }

        throw new EnvironmentException(
          name,
          "Environment process failed"
            + " with non-zero exit code of "
            + process.exitValue() );
    }
}
