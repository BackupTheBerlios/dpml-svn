/*
 * Copyright 1997-2004 Apache Software Foundation
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
package org.apache.avalon.facilities.console.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;

import java.util.TreeMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.avalon.composition.model.ContainmentModel;

import org.apache.avalon.facilities.console.LoginException;
import org.apache.avalon.facilities.console.CommandInterpreter;
import org.apache.avalon.facilities.console.Console;
import org.apache.avalon.facilities.console.ConsoleCommand;

class CommandInterpreterImpl extends Thread
    implements CommandInterpreter
{
    private Socket          m_Socket;
    private boolean         m_Login;
    private BufferedWriter  m_Output;
    private BufferedReader  m_Input;
    private Map             m_Commands;
    private ContainmentModel m_CurrentNode;
    
    CommandInterpreterImpl( Socket socket, String welcome, Map commands, ContainmentModel root )
        throws IOException
    {
        m_Socket = socket;
        m_Commands = commands;
        welcomeMessage( welcome );
        m_CurrentNode = root;
    }

    public void addCommand( ConsoleCommand cmd )
    {
        m_Commands.put( cmd.getName(), cmd );
    }
    
    public void removeCommand( ConsoleCommand cmd )
    {
        m_Commands.remove( cmd.getName() );
    }
    
    public void removeCommand( String commandname )
    {
        m_Commands.remove( commandname );
    }
    
    public ContainmentModel getCurrentContainer()
    {
        return m_CurrentNode;
    }
    
    public void setCurrentContainer( ContainmentModel node )
    {
        m_CurrentNode = node;
    }
    
    public void run()
    {
        boolean running = true;
        while( running )
        {
            try
            {
                if( ! m_Login )
                    execute( "login" );
                m_Login = true; //LoginCmd throws an exception if login fails.
                execute( waitForCommandLine() );
            } catch( LoginException e )
            {
                errorMessage( "wrong password" );
            } catch( InterruptedException e )
            {
                running = false;
            } catch( Exception e )
            {
                String mess = e.getMessage();
                if( mess == null )
                    mess = "<null>";
                errorMessage( mess );
                e.printStackTrace();
            }
        }
        try
        {
            m_Socket.close();
        } catch( IOException e )
        {
        }
    }

    private void execute( String cmdline )
        throws Exception
    {
        // TODO: Optimize parser.
        StringTokenizer st = new StringTokenizer( cmdline, " ", false );
        String cmd = "";
        while( st.hasMoreTokens() )
        {
            cmd = (cmd + " " + st.nextToken()).trim();
            ConsoleCommand cc = (ConsoleCommand) m_Commands.get( cmd );
            if( cc != null )
            {
                String[] args = parseArguments( st );
                cc.execute( this, getInput(), getOutput(), args );
                return;
            }
        }
        errorMessage( "Unknown command. 'help' for available commands." );
    }

    private String waitForCommandLine()
        throws IOException
    {
        String cmdline = "";
        while( cmdline.length() == 0 )
        {
            getOutput().write( "[" + m_Socket.getLocalAddress() + "  "  + m_CurrentNode.getQualifiedName() + "]$ " );
            getOutput().flush();
            cmdline = m_Input.readLine().trim();
        }
        return cmdline;
    }

    private String[] parseArguments( StringTokenizer st )
    {
        String[] args = new String[ st.countTokens() ];
        for( int i=0 ; st.hasMoreTokens() ; i++ )
            args[i] = st.nextToken();
        return args;
    }

    private void welcomeMessage( String message )
    {
        try
        {
            getOutput().newLine();
            getOutput().write( message );
            getOutput().newLine();
            getOutput().flush();
        } catch( IOException e )
        {
            e.printStackTrace( System.out );
        }
    }

    private void errorMessage( String message )
    {
        try
        {
            getOutput().write( message );
            getOutput().newLine();
            getOutput().flush();
        } catch( IOException e )
        {
            e.printStackTrace( System.out );
        }
    }

    private BufferedWriter getOutput()
        throws IOException
    {
        if( m_Output == null )
            openOut();
        return m_Output;
    }

    private BufferedReader getInput()
        throws IOException
    {
        if( m_Input == null )
            openIn();
        return m_Input;
    }

    private void openOut()
        throws IOException
    {
        m_Output = new BufferedWriter( new OutputStreamWriter( m_Socket.getOutputStream() ) );
    }

    private void openIn()
        throws IOException
    {
        m_Input = new BufferedReader( new InputStreamReader( m_Socket.getInputStream() ) );
    }

}

