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
package org.apache.avalon.facilities.console.commands;


import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.util.Iterator;

import org.apache.avalon.facilities.console.CommandInterpreter;
import org.apache.avalon.facilities.console.Console;
import org.apache.avalon.facilities.console.ConsoleCommand;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * @avalon.component name="console-help" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.facilities.console.ConsoleCommand"
 */
public class HelpCmd
    implements ConsoleCommand, Serviceable, Contextualizable
{
    private Console m_Console;
    private String m_Name;
    
    public String getName()
    {
        return m_Name;
    }
    
    public String getDescription()
    {
        String str = "usage: " + m_Name + " (command)*\n\nGives a short description of each command.\nIf no command is given, then a list of available commands are printed.";
        return str;
    }
    
    /**
     * Contextulaization of the listener by the container during 
     * which we are supplied with the root composition model for 
     * the application.
     *
     * @param ctx the supplied listener context
     *
     * @exception ContextException if a contextualization error occurs
     *
     * @avalon.entry key="urn:avalon:name" 
     *               type="java.lang.String" 
     */
    public void contextualize( Context ctx ) 
        throws ContextException
    {
        m_Name = (String) ctx.get( "urn:avalon:name" );
    }

    /**
     * @avalon.dependency type="org.apache.avalon.facilities.console.Console"
     *                    key="console"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        m_Console = (Console) man.lookup( "console" );
        m_Console.addCommand( this );
    }
    
    public void execute( CommandInterpreter intp, BufferedReader input, BufferedWriter output, String[] arguments )
        throws Exception
    {
        output.newLine();
        if( arguments.length == 0 )
        {
            output.write( "Available commands:" );
            output.newLine();
            output.write( "===================" );
            output.newLine();
            Iterator list = m_Console.getCommandNames().iterator();
            while( list.hasNext() )
            {
                output.write( list.next().toString() );
                output.newLine();
            }
        } else
        {
            for( int i=0 ; i < arguments.length ; i++ )
            {
                ConsoleCommand cmd = m_Console.getCommand( arguments[i] );
                if( cmd != null )
                {
                    String descr = cmd.getDescription();
                    output.write( descr );
                    output.newLine();
                }
            }
        }
        output.newLine();
        output.flush();
    }
}
