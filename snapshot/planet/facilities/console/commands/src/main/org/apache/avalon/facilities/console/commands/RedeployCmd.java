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

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.net.URL;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;

import org.apache.avalon.facilities.console.CommandException;
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
 * @avalon.component name="console-decommissionmodel" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.facilities.console.ConsoleCommand"
 */
public class RedeployCmd
    implements ConsoleCommand, Serviceable, Contextualizable
{
    private String m_Name;
    private File   m_WorkingDir;
        
    public String getName()
    {
        return m_Name;
    }
    
    public String getDescription()
    {
        String str = "usage: " + m_Name + " (url) (target-path)\n\nLoads and commissions the block at the URL into the model specified by (target-path)";
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
     * @avalon.entry key="urn:avalon:home" 
     *               type="java.io.File" 
     */
    public void contextualize( Context ctx ) 
        throws ContextException
    {
        m_Name = (String) ctx.get( "urn:avalon:name" );
        m_WorkingDir = (File) ctx.get( "urn:avalon:home" );
    }

    /**
     * @avalon.dependency type="org.apache.avalon.facilities.console.Console"
     *                    key="console"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        Console console = (Console) man.lookup( "console" );
        console.addCommand( this );
    }
    
    public void execute( CommandInterpreter intp, BufferedReader input, BufferedWriter output, String[] arguments )
        throws Exception
    {
        output.newLine();
        output.flush();
    }
}
 
