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

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.avalon.composition.model.ContainmentModel;

import org.apache.avalon.facilities.console.CommandInterpreter;
import org.apache.avalon.facilities.console.Console;
import org.apache.avalon.facilities.console.ConsoleCommand;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;


/** This is the main component for the Console facility.
 *
 * <p>Parameters, default values are shown below.</p>
 * <pre>
 *   &lt;parameters&gt;
 *     &lt;parameter name="port" value="3333" /&gt;
 *     &lt;parameter name="allow-deny" value="false" /&gt;
 *     &lt;parameter name="local-allow" value="true" /&gt;
 *     &lt;parameter name="welcome-text" value="type 'help' form more info." /&gt;
 *   &lt;/parameters&gt;
 * </pre>
 * <p> If <i>allow-deny</i> is set to true, then the connection is allowed
 * if the host does not exist in the deny list, otherwise it is not allowed
 * unless the host exist in the allow list.
 * </p>
 * <p>If <i>local-allow</i> is true, the localhost/127.0.0.1 is added to the
 * allow list. This is default, so one can connect without further parameterization
 * or configuration.</p>
 *
 * <p>Configuration</p>
 * <pre>
 *   &lt;configuration&gt;
 *     &lt;connections&gt;
 *       &lt;allow&gt;127.0.0.1&gt;
 *       &lt;allow&gt;10.0.0.23&gt;
 *       &lt;allow&gt;10.0.0.24&gt;
 *     &lt;/connections&gt;
 *   &lt;/configuration&gt;
 * </pre>
 * <p>or in some circumstances, one sets the <i>allow-deny</i> parameter to
 * true, and have a deny list in the configuration.</p>
 * <pre>
 *   &lt;configuration&gt;
 *     &lt;connections&gt;
 *       &lt;deny&gt;211.24.132.24&gt;
 *       &lt;deny&gt;211.24.132.29&gt;
 *     &lt;/connections&gt;
 *   &lt;/configuration&gt;
 * </pre>
 *
 * @avalon.component name="console-impl" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.facilities.console.Console"
 */
public class ConsoleImpl extends AbstractLogEnabled
    implements Console, Runnable, Startable, Parameterizable, 
               Initializable, Contextualizable
{
    private int         m_Port;
    private ServerSocket m_ServerSocket;
    private Thread      m_Thread;
    private Hashtable   m_Commands;
    private ArrayList   m_Interpreters;
    private String      m_Welcome;    
    private ContainmentModel m_RootModel;
    
    private boolean     m_Allow;
    private HashSet     m_Allows;
    private HashSet     m_Denies;
    
    public ConsoleImpl()
    {
        m_Allows = new HashSet();
        m_Denies = new HashSet();
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
     * @avalon.entry key="urn:composition:containment.model" 
     *               type="org.apache.avalon.composition.model.ContainmentModel" 
     *
     */
    public void contextualize( Context ctx ) 
        throws ContextException
    {
        m_RootModel = (ContainmentModel) ctx.get( "urn:composition:containment.model" );
    }

    /**
     * <p>Parameters, default values are shown below.</p>
     * <pre>
     *   &lt;parameters&gt;
     *     &lt;parameter name="port" value="3333" /&gt;
     *     &lt;parameter name="allow-deny" value="false" /&gt;
     *     &lt;parameter name="local-allow" value="true" /&gt;
     *     &lt;parameter name="welcome-text" value="type 'help' form more info." /&gt;
     *   &lt;/parameters&gt;
     * </pre>
     */     
    public void parameterize( Parameters params )
        throws ParameterException
    {
        m_Port = params.getParameterAsInteger( "port", 3333 );
        m_Welcome = params.getParameter( "welcome-text", "type 'help' form more info." );
        m_Allow = params.getParameterAsBoolean( "deny-allow", false );
        
        boolean localAllow = params.getParameterAsBoolean( "allow-local", true );
        if( localAllow )
        {
            try
            {
                InetAddress address = InetAddress.getByName( "127.0.0.1" );
                m_Allows.add( address );
            } catch( UnknownHostException e )
            {} // should not be possible, and if so, never mind.
        }
    }
    
    /**
     * <p>Configuration</p>
     * <pre>
     *   &lt;configuration&gt;
     *     &lt;connections&gt;
     *       &lt;allow&gt;127.0.0.1&gt;
     *       &lt;allow&gt;10.0.0.23&gt;
     *       &lt;allow&gt;10.0.0.24&gt;
     *     &lt;/connections&gt;
     *   &lt;/configuration&gt;
     * </pre>
     * <p>or in some circumstances, one sets the <i>allow-deny</i> parameter to
     * true, and have a deny list in the configuration.</p>
     * <pre>
     *   &lt;configuration&gt;
     *     &lt;connections&gt;
     *       &lt;deny&gt;211.24.132.24&gt;
     *       &lt;deny&gt;211.24.132.29&gt;
     *     &lt;/connections&gt;
     *   &lt;/configuration&gt;
     * </pre>
     *
     */
    public void configure( Configuration conf )
        throws ConfigurationException
    {
        Configuration child = conf.getChild( "connections" );
        configureConnections( child );
    }
    
    private void configureConnections( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] allows = conf.getChildren( "allow" );
        for( int i=0 ; i < allows.length ; i++ )
            configureAllow( allows[i] );        
            
        Configuration[] denies = conf.getChildren( "deny" );
        for( int i=0 ; i < denies.length ; i++ )
            configureDeny( denies[i] );        
    }
    
    private void configureAllow( Configuration allow )
        throws ConfigurationException
    {
        String address = allow.getValue();
        try
        {
            InetAddress allowAddr = InetAddress.getByName( address );
            m_Allows.add( allowAddr );
        } catch( UnknownHostException e )
        {
            throw new ConfigurationException( "Unknown host: " + address, e );
        }
    }
    
    private void configureDeny( Configuration deny )
        throws ConfigurationException
    {
        String address = deny.getValue();
        try
        {
            InetAddress denyAddr = InetAddress.getByName( address );
            m_Denies.add( denyAddr );
        } catch( UnknownHostException e )
        {
            throw new ConfigurationException( "Unknown host: " + address, e );
        }
    }
    
    public void initialize()
        throws Exception
    {
        m_Interpreters = new ArrayList();
        m_Commands = new Hashtable();
        m_ServerSocket = new ServerSocket( m_Port );
    }

    public void start()
    {
        /* start new thread for monitoring and updating. */
        getLogger().info( "Console started." );
        m_Thread = new Thread( this );
        m_Thread.start();
    }

    public void stop()
    {
        try
        {
            m_Thread.interrupt();
            m_ServerSocket.close();
        } catch( IOException e )
        {}
    }

    public void dispose()
    {
        getLogger().info("Console disposed.");
    }

    public void addCommand( ConsoleCommand cmd )
    {
        getLogger().info( "Added " + cmd.getName() + " command." );
        String name = cmd.getName();
        m_Commands.put( name, cmd );
        synchronized( m_Interpreters )
        {
            Iterator list = m_Interpreters.iterator();
            while( list.hasNext() )
            {
                CommandInterpreter intp = (CommandInterpreter) list.next();
                intp.addCommand( cmd );
            }
        }
    }
    
    public void removeCommand( ConsoleCommand cmd )
    {
        m_Commands.remove( cmd.getName() );
        synchronized( m_Interpreters )
        {
            Iterator list = m_Interpreters.iterator();
            while( list.hasNext() )
            {
                CommandInterpreter intp = (CommandInterpreter) list.next();
                intp.removeCommand( cmd );
            }
        }
    }
    
    public void removeCommand( String commandname )
    {
        m_Commands.remove( commandname );
        synchronized( m_Interpreters )
        {
            Iterator list = m_Interpreters.iterator();
            while( list.hasNext() )
            {
                CommandInterpreter intp = (CommandInterpreter) list.next();
                intp.removeCommand( commandname );
            }
        }
    }
    
    public ConsoleCommand getCommand( String name )
    {
        return (ConsoleCommand) m_Commands.get( name );
    }
    
    public Collection getCommandNames()
    {
        return m_Commands.keySet();
    }
    
    public void run()
    {
        boolean running = true;
        while( running )
        {
            Socket socket = null;
            try
            {
                socket = m_ServerSocket.accept();
                validateHost( socket.getInetAddress() );
                CommandInterpreterImpl intp = new CommandInterpreterImpl( socket, m_Welcome, m_Commands, m_RootModel );
                synchronized( m_Interpreters )
                {
                    m_Interpreters.add( intp );
                }
                intp.start();
            } catch( IOException e )
            {
                running = false;
            } catch( DeniedHostException e )
            {
                getLogger().warn( e.getMessage() );
                try
                {
                    socket.close();
                } catch( IOException ioe )
                {}
            } catch( Exception e )
            {
                getLogger().warn( "", e );
                try
                {
                    Thread.sleep( 60000 );
                } catch( InterruptedException f )
                {
                    running = false;
                }
            }
        }
        Iterator list = m_Interpreters.iterator();
        while( list.hasNext() )
        {
            CommandInterpreterImpl intp = (CommandInterpreterImpl) list.next();
            intp.interrupt();
        }
    }
    
    private void validateHost( InetAddress host )
        throws DeniedHostException
    {
        if( m_Allow )
        {
            if( m_Denies.contains( host ) )
                throw new DeniedHostException( host.toString() );
        }
        else
        {
            if( ! m_Allows.contains( host ) )
                throw new DeniedHostException( host.toString() );
        }
    }
}
