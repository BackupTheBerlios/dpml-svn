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

package org.apache.metro.facilities.reflector.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.framework.activity.Startable;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.avalon.http.HttpContextService;

import org.apache.metro.facilities.reflector.ReflectionException;
import org.apache.metro.facilities.reflector.ReflectorService;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 * @avalon.component name="reflector-http-handler" lifestyle="singleton"
 * @avalon.service   type="org.mortbay.http.HttpHandler"
 */
public class ReflectionHandler 
    implements Startable, Parameterizable, LogEnabled, 
               Serviceable, Contextualizable, HttpHandler
{
    private String              m_Name;
    private Logger              m_Logger;
    private HttpContextService  m_ContextService;
    private HttpContext         m_Context;
    private int                 m_Index;
    private boolean             m_Started;
    
    private ReflectorService    m_Reflector;
    private String              m_ContextPath;
    private String              m_Encoding;
    
    private Vector    m_Reserved;
        
    public ReflectionHandler()
    {
        m_Started = false;
        m_Reserved = new Vector();
        m_Reserved.addElement("debug");
        m_Reserved.addElement("app");
        m_Reserved.addElement("");
    }
    
    /**
     * Enable the logging system.
     *
     * @avalon.logger name="http"
     */
    public void enableLogging( Logger logger )
    {
        m_Logger = logger;
    }
    
    public Logger getLogger() 
    {
        return m_Logger;
    }
    
    /**
     * Contextulaization of the Handler.
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

    public void parameterize( Parameters params )
        throws ParameterException
    {
        m_Index = params.getParameterAsInteger( "handler-index", -1 );
        m_ContextPath = params.getParameter( "context-path" );
        m_Encoding = params.getParameter( "encoding", "ISO-8859-1" );
    }

    /**  
     * @avalon.dependency type="org.apache.avalon.http.HttpContextService"
     *                    key="http-context" 
     * @avalon.dependency type="org.apache.metro.facilities.reflector.ReflectorService"
     *                    key="reflector" 
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        m_ContextService = (HttpContextService) man.lookup( "http-context" );
        m_Reflector = (ReflectorService) man.lookup( "reflector" );
    }
 
    public void initialize( HttpContext context )
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Initializing ReflectorHandler: " + context );
        m_Context = context;
    }
    
    public String getName()
    {
        return m_Name;
    }
    
    public void setName( String name )
    {
        m_Name = name;
    }
    
    public HttpContext getHttpContext()
    {
        return m_Context;
    }
    
    public void start()
        throws Exception
    {
        m_Started = true;
        if( m_Index >= 0 )
            m_ContextService.addHandler( m_Index, this );
        else
            m_ContextService.addHandler( this );
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Starting ReflectorHandler: " + this );
    }
    
    public boolean isStarted()
    {
        return m_Started;
    }
    
    public void stop()
        throws InterruptedException
    {
        m_Started = false;
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Stopping ReflectorHandler: " + this );
        m_ContextService.removeHandler( this );
    }
    
    public void handle( String pathInContext, String pathParams, 
                        HttpRequest request, HttpResponse response ) 
        throws IOException
    {
        m_Logger.info( "Request: " + pathInContext );
        if( ! pathInContext.startsWith( m_ContextPath ) )
            return;
        pathInContext = pathInContext.substring( m_ContextPath.length() );
        
        response.setContentType("text/html");
        RequestContext ctx = new RequestContext( pathInContext, request, response, m_Encoding );
        try
        {        
            String method = request.getMethod();
            if( method.equals( "GET" ) )
                doGet( ctx );
            if( method.equals( "POST" ) )
                doPost( ctx );
        } catch( Exception e )
        {
            ctx.out.println( "<html><body><h1>Exception occurred in service.</h1><hr><pre>" );
            e.printStackTrace( ctx.out );
            ctx.out.println( "</pre></body></html>" );
        } finally
        {
            ctx.dispose();
        }
    }

    private void doGet( RequestContext ctx )
        throws IOException, ReflectionException
    {
        if( m_Logger.isDebugEnabled() )
        {
            String s1 = "Reflector get:[" + ctx.req.getRemoteAddr() + ":" + ctx.req.getUserPrincipal() + "] --> " + ctx.req.getRequestURL() + "?" + ctx.req.getQuery();
            m_Logger.debug(s1);
        }

        ctx.out.println( "<html>" );
        formatGet( ctx );
        formatDebug(ctx);
        ctx.out.println( "</body></html>" );
    }

    private void doPost( RequestContext ctx )
        throws IOException
    {
        if( m_Logger.isDebugEnabled() )
        {
            String s1 = "Reflector set:[" + ctx.req.getRemoteAddr() + ":" + ctx.req.getUserPrincipal() + "] --> " + ctx.req.getRequestURL() + "?" + ctx.req.getQuery();
            m_Logger.debug( s1 );
        }
            
        ctx.out.println( "<html><head>" );
        ctx.out.println( "</head><body>" );
        ctx.out.println( "<h3>Changes made:</h3><hr>" );
        ctx.out.println( "<table border=1 cellspacing=0 cellpadding=2 width=\"100%\">" );
        ctx.out.println( "<tr><td><b><u>Member</b></u></td><td><b><u>Changed to</b></u></td></tr>" );

        Iterator names = ctx.req.getParameterNames().iterator();
        while( names.hasNext() )
        {
            String name = (String) names.next();
            if( ! isReserved(name) ) 
            {
                List values = ctx.req.getParameterValues( name );
                for( int i=0 ; i < values.size() ; i++ )
                {
                    String value = (String) values.get(i);
                    if( value != null )
                    {
                        if( ! value.equals("") )try
                        {
                            ctx.out.print( "<tr><td>" );
                            ctx.out.print( name );
                            ctx.out.print( "</td><td>" );
                            m_Reflector.set( name, value );
                            ctx.out.print( value );
                        } catch( Exception e )
                        {
                            e.printStackTrace( ctx.out );
                        }
                        ctx.out.print( "</td></tr>" );
                    }
                }
            }
        }
        ctx.out.println( "</table>" );
        if( ctx.debug )
            ctx.out.println("<hr>Execution Time:" + (System.currentTimeMillis()-ctx.start) + " ms" );
        ctx.out.println( "</body></html>" );
    }
    
    private void formatGet( RequestContext ctx )
        throws IOException, ReflectionException
    {
        // then get the writer and write the response data
        String title = null;
        if( ctx.objectname == null || ctx.objectname.equals("") )
            title = "Application Root Names";
        else
            title = ctx.objectname;
        PrintWriter out = ctx.out;

        out.println( "<head><title>" );
        out.println( title );
        out.println( "</title></head><body>" );
        out.println( "<h1>" );
        out.println( title );
        out.println( "</h1>" );
        if( ctx.objectname == null || ctx.objectname.equals("") )
            out.println( "<hr>" );
        else
        {
            String container = m_Reflector.getContainer( ctx.objectname );
            String member = m_Reflector.getMember( ctx.objectname );
            if( container == null || "".equals(container) )
            {
                out.print( "<A href=" );
                out.print( "\"/bali/debug/" );
                out.print( "\">Application Root</A><hr>" );
            }
            else
            {
                out.print( "<A href=" );
                formatURL( ctx, container );
                out.println( "\">" );
                out.println( container );
                out.println( "</A><hr>" );
            }
        }
        out.print( "<FORM Method=\"POST\" Action=\"modify" );
        if( ctx.debug )
            out.print( "?debug=true" );
        out.print( "\">" );
        boolean error = false;
        if( ctx.objectname != null )
        {
            if( ! ctx.objectname.equals("") )
            {
                Class cls = m_Reflector.getClass(ctx.objectname);
                if( cls == null )
                {
                    formatNotFound(ctx);
                    error = true;
                }
                else if( cls.isArray() ||
                    Collection.class.isAssignableFrom(cls) ||
                    Map.class.isAssignableFrom(cls) ||
                    Dictionary.class.isAssignableFrom(cls)
                  )
                    formatArray( ctx );
                else
                    formatObject(ctx);
            }
            else
                formatObject(ctx);
        }
        else
            formatObject(ctx);
        if( ! error )
            formatButton( ctx );
        out.println( "</FORM>" );
    }

    private void formatNotFound( RequestContext ctx )
    {
        ctx.out.println( "<h2>Object not found.</h2>" );
    }
    
    private void formatObject( RequestContext ctx)
        throws ReflectionException
    {
        String[] names = m_Reflector.getNames( ctx.objectname );
        formatTableHeader(ctx);
        for( int i=0; i<names.length ; i++ )
        {
            ctx.out.print( "<tr>" );
              formatMember( ctx, names[i] );
            ctx.out.println( "</tr>" );
        }
        ctx.out.println( "</table>" ) ;
    }

    private void formatMember( RequestContext ctx, String member)
    {
        int columncount = 0;
        long then = System.currentTimeMillis();
        ctx.out.print( "<td><A href=" );
        columncount++;
        String str = "";
        if( ctx.objectname != null && ! "".equals(ctx.objectname) )
            str = ctx.objectname + ".";
        str = str + member;
        formatURL(ctx, str );
        ctx.out.print ( ">" );
        ctx.out.print( member );
        ctx.out.print( "</A></td><td>" );
        columncount++;
        try
        {
            String value;
            String membername;
            if( ctx.objectname == null || "".equals(ctx.objectname) )
                membername = member;
            else
                membername = ctx.objectname + "." + member;

            String obj = m_Reflector.get( membername );
            if( obj == null )
                value = "&lt;null&gt;";
            else
                value = obj;
            ctx.out.print( value );
            
            if( m_Reflector.isSettable(membername) )
            {
                ctx.out.print( "</td><td><INPUT Name=\"" );
                ctx.out.print( str );
                ctx.out.print( "\" TYPE=\"TEXT\" SIZE=\"15\" MAXLENGTH=\"300\" >" );
            }
            else
            {
                ctx.out.print( "</td><td><br>" );
            }
            columncount++;
            ctx.out.print( "</td><td>" );
            columncount++;
            String name = m_Reflector.getClassName( membername );
            if( name == null )
                ctx.out.print( "" );
            else
                ctx.out.print( name );
        }
        catch( Exception e )
        {
            m_Logger.error( e.toString() );
            ctx.out.print( "<pre>" );
            ctx.out.print( e.toString() );
            e.printStackTrace( ctx.out );
            ctx.out.print( "</pre>" );
            for( ; columncount < 4 ; columncount++ )
                ctx.out.print( "</td><td><br>" );
        }
        ctx.out.print( "</td>" );
        long now = System.currentTimeMillis();
        if( ctx.debug )
            ctx.out.print( "<td>" + (now-then) + " ms</td>" );
    }
    
    private void formatArrayMember( RequestContext ctx, String membername)
    {
        long then = System.currentTimeMillis();
        int columncount = 0;        
        
        ctx.out.print( "<td><A href=" );
        columncount++;
        formatURL( ctx, ctx.objectname + membername );
        ctx.out.print ( ">" );
        ctx.out.print( membername );
        ctx.out.print( "</A></td><td>" );
        columncount++;
        try
        {
            String value = m_Reflector.get(ctx.objectname + membername);
            Class cls = m_Reflector.getClass(ctx.objectname + membername);
            ctx.out.print( value );
            
            if( m_Reflector.isSettable(ctx.objectname + membername) )
            {
                ctx.out.print( "</td><td><INPUT Name=\"" );
                ctx.out.print( ctx.objectname + membername );
                ctx.out.print( "\" TYPE=\"TEXT\" SIZE=\"10\" MAXLENGTH=\"30\" >" );
            }
            else
            {
                ctx.out.print( "</td><td><br>" );
            }
            columncount++;
            ctx.out.print( "</td><td>" );
            columncount++;
            if( cls == null )
                ctx.out.print( "&lt;unknown&gt;" );
            else
                ctx.out.print( cls.getName() );
        }
        catch( Exception e )
        {
            m_Logger.error( e.getMessage(), e );
            ctx.out.print( "<pre>" );
            ctx.out.print( e.toString() );
            e.printStackTrace( ctx.out );
            ctx.out.print( "</pre>" );
            for( ; columncount < 4 ; columncount++ )
                ctx.out.print( "</td><td>" );
        }
        ctx.out.println( "</td>" );
        
        long now = System.currentTimeMillis();
        if( ctx.debug )
            ctx.out.print( "<td>" + (now-then) + " ms</td>" );
    }

    private void formatMapMember( RequestContext ctx, Object key, Object value)
    {
        long then = System.currentTimeMillis();
        int columncount = 0;
        
        ctx.out.print( "<td><A href=" );
        columncount++;
        formatURL( ctx, ctx.objectname + "['" + key + "']" );
        ctx.out.print ( ">" );
        ctx.out.print( "[\"" + key + "\"]" );
        ctx.out.print( "</A></td><td>" );
        columncount++;
        try
        {
            ctx.out.print( value );
            Class cls = key.getClass();
            ctx.out.print( "</td><td>" );
            if( isPrimitive( cls ) )
            {
                ctx.out.print( "<INPUT Name=\"" );
                ctx.out.print( ctx.objectname + "[\"" + key + "\"]" );
                ctx.out.print( "\" TYPE=\"TEXT\" SIZE=\"10\" MAXLENGTH=\"30\" >" );
            }
            columncount++;
            ctx.out.print( "</td><td>" );
            columncount++;
            ctx.out.print( cls.getName() );
        }
        catch( Exception e )
        {
            m_Logger.error( e.getMessage(), e );
            ctx.out.print( "<pre>" );
            ctx.out.print( e.toString() );
            e.printStackTrace( ctx.out );
            ctx.out.print( "</pre>" );
            for( ; columncount < 4 ; columncount++ )
                ctx.out.print( "</td><td>" );
        }
        ctx.out.println( "</td>" );
        
        long now = System.currentTimeMillis();
        if( ctx.debug )
            ctx.out.print( "<td>" + (now-then) + " ms</td>" );
    }

    private void formatArray( RequestContext ctx )
        throws ReflectionException
    {
        formatTableHeader(ctx);
        String[] names = m_Reflector.getNames(ctx.objectname);
        for( int i=0; i < names.length ; i++ )
        {
            ctx.out.print( "<tr>" );   
            formatArrayMember( ctx, "[\'" + names[i] + "\']" );
            ctx.out.println( "</tr>" );
        }
        ctx.out.println( "</table>" ) ;
    }
    
    private void formatButton( RequestContext ctx )
    {
        ctx.out.print( "<p><center><INPUT TYPE=\"SUBMIT\" VALUE=\"Change\"></center></p>" );
    }
    
    private void formatDebug( RequestContext ctx )
    {
        if( ctx.debug )
        {
            long i = System.currentTimeMillis() - ctx.start;
            ctx.out.println( "<hr>Execution time:" + i + " ms" );
        }
    }
    
    private boolean isPrimitive( Class cls )
    {
        if( cls.isPrimitive() )
            return true;
        
        if( cls.equals( Boolean.class ) )
            return true;
        if( cls.equals( Number.class ) )
            return true;
        else if( cls.equals(Character.class) )
            return true;
        else if( cls.equals(Void.class) )
            return true;
        else if( cls.equals(String.class) )
            return true;
        else
            return false;
    }

    private void formatURL( RequestContext ctx, String objectname )
    {
        ctx.out.print( "\"" );
        ctx.out.print( encode( objectname ) );
        if( ctx.debug )
            ctx.out.print( "?debug=true" );
        ctx.out.print( "\"" );
    }

    private String encode( String text )
    {
        StringBuffer buf = new StringBuffer();
        
        for( int i=0 ; i < text.length() ; i++ )
        {
            
            char ch = text.charAt(i);
            if( (ch >= 'A' && ch <= 'z') || (ch >= '(' && ch <= '9') &&
                ( ch != '/' )
              )
            {
                buf.append( ch );
            }
            else
            {
                buf.append( '{' );
                buf.append( "" + ((int) ch) );
                buf.append( '}' );
            }
        }
        return buf.toString();
    }
    
    private void formatTableHeader( RequestContext ctx )
    {
        ctx.out.println( "<table border=1 cellspacing=0 cellpadding=2 width=\"100%\">" );
        ctx.out.println( "<tr><td><b><u>Member</b></u></td><td><b><u>Value</b></u></td><td><b><u>Change</b></u></td><td><b><u>Class/Type</b></u></td></tr>" );
    }
    
    private boolean isReserved( String text )
    {
        return m_Reserved.contains(text);
    }
} 
