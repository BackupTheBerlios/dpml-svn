/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.component;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * The CompositionControllerContext class wraps a ContentModel and supplies convinience
 * operations that translate ContentModel properties and events to type-safe values used 
 * in the conposition controller.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class InitialContext extends LocalEventProducer 
  implements ControllerContext, Disposable
{
    //----------------------------------------------------------------------------
    // static
    //----------------------------------------------------------------------------
    
   /**
    * Create the default controller using the default initial context.
    * The default context and associated controller disposal will be triggered 
    * on JVM shutdown.
    *
    * @return the default controller
    */
    public static Controller createController()
    {
        return createController( null );
    }
    
   /**
    * Create the default controller.  Controller disposal is the responsiblity
    * of the client application.
    *
    * @param context the controller context
    * @return the controller
    */
    public static Controller createController( final InitialContext context )
    {
        ControllerInvocationHandler handler = 
          new ControllerInvocationHandler( context );
        return (Controller) Proxy.newProxyInstance( 
          Controller.class.getClassLoader(), new Class[]{Controller.class}, handler );
    }
    
   /**
    * Internal invocation handler that delays controller instantiation
    * until a request against the controller is made by a client.
    */
    private static final class ControllerInvocationHandler implements InvocationHandler
    {
        private InitialContext m_context;
        private Controller m_controller;
        
        private ControllerInvocationHandler( InitialContext context )
        {
            m_context = context;
        }
        
        /**
        * Invoke the specified method on underlying object.
        * This is called by the proxy object.
        *
        * @param proxy the proxy object
        * @param method the method invoked on proxy object
        * @param args the arguments supplied to method
        * @return the return value of method
        * @throws Throwable if an error occurs
        */
        public Object invoke( 
          final Object proxy, final Method method, final Object[] args ) throws Throwable
        {
            Controller controller = getController();
            return method.invoke( controller, args );
        }
        
        private synchronized Controller getController()
        {
            if( null != m_controller )
            {
                return m_controller;
            }
            else
            {
                m_controller = InitialContext.newController( m_context );
                return m_controller;
            }
        }
    }
    
    private static URI getControllerURI() throws Exception
    {
        String spec = 
          System.getProperty( 
            "dpml.part.controller.uri", 
            "@PART-HANDLER-URI@" );
        return new URI( spec );
    }
    
   /**
    * Construct a controller.
    * @param context the controller context
    * @return the controller
    */
    private static Controller newController( final InitialContext context )
    {
        InitialContext control = context;
        if( null == control )
        {
            control = new InitialContext();
            Runtime.getRuntime().addShutdownHook( new ContextShutdownHook( control ) );
        }
        
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( InitialContext.class.getClassLoader() );
            URI uri = getControllerURI();
            Repository repository = Transit.getInstance().getRepository();
            Class c = repository.getPluginClass( uri );
            Constructor constructor = c.getConstructor( new Class[]{ControllerContext.class} );
            Controller controller = (Controller) constructor.newInstance( new Object[]{control} );
            return controller;
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the standard controller.";
            throw new RuntimeException( error, e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( classloader );
        }
    }
    
    //----------------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------------
    
   /**
    * Working directory.
    */
    private File m_work;
  
   /**
    * Temp directory.
    */
    private File m_temp;

   /**
    * The assigned partition name.
    */
    private String m_partition;

    //----------------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------------
    
   /**
    * Creation of a new <tt>InitialContext</tt>.
    */
    public InitialContext()
    {
        this( "", null, null );
    }

   /**
    * Creation of a new <tt>InitialContext</tt>.
    * @param partition the assigned partition name
    */
    public InitialContext( String partition )
    {
        this( partition, null, null );
    }
    
   /**
    * Creation of a new <tt>InitialContext</tt>.
    * @param partition the partition name
    * @param work the working directory
    * @param temp the temporary directory
    */
    public InitialContext( String partition, File work, File temp )
    {
        super( new LoggingAdapter( partition ) );

        m_partition = partition;

        if( null == work )
        {
            String path = System.getProperty( "user.dir" );
            m_work = new File( path );
        }
        else
        {
            m_work = work;
        }
        
        if( null == temp )
        {
            String path = System.getProperty( "java.io.tmpdir" );
            m_temp = new File( path );
        }
        else
        {
            m_temp = temp;
        }
    }

    //----------------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------------
    
   /**
    * Initiate disposal.
    */
    public void dispose()
    {
        getInternalLogger().debug( "initiating context disposal" );
        ControllerDisposalEvent event = new ControllerDisposalEvent( this );
        enqueueEvent( event, false );
        super.dispose();
        getInternalLogger().debug( "disposed" );
    }

    //----------------------------------------------------------------------------
    // ControllerContext
    //----------------------------------------------------------------------------

   /**
    * Return the partition name
    * @return the partion name
    */
    public String getPartition()
    {
        return m_partition;
    }

   /**
    * Return the root working directory.
    *
    * @return directory representing the root of the working directory hierachy
    */
    public File getWorkingDirectory()
    {
        synchronized( getLock() )
        {
            return m_work;
        }
    }

   /**
    * Set the root working directory.
    *
    * @param dir the root of the working directory hierachy
    */
    public void setWorkingDirectory( File dir )
    {
        synchronized( getLock() )
        {
            m_work = dir;
        }
    }

   /**
    * Return the root temporary directory.
    *
    * @return directory representing the root of the temporary directory hierachy.
    */
    public File getTempDirectory()
    {
        synchronized( getLock() )
        {
            return m_temp;
        }
    }

   /**
    * Add the supplied controller context listener to the controller context.  A 
    * controller implementation should not maintain strong references to supplied 
    * listeners.
    *
    * @param listener the controller context listener to add
    */
    public void addControllerContextListener( ControllerContextListener listener ) 
    {
        addListener( listener );
    }

   /**
    * Remove the supplied controller context listener from the controller context.
    *
    * @param listener the controller context listener to remove
    */
    public void removeControllerContextListener( ControllerContextListener listener )
    {
        removeListener( listener );
    }

    //----------------------------------------------------------------------------
    // impl
    //----------------------------------------------------------------------------

   /**
    * Process a context related event.
    * @param event the event to process
    */
    protected void processEvent( EventObject event )
    {
        if( event instanceof WorkingDirectoryChangeEvent )
        {
            processWorkingDirectoryChangeEvent( (WorkingDirectoryChangeEvent) event );
        }
        else if( event instanceof TempDirectoryChangeEvent )
        {
            processTempDirectoryChangeEvent( (TempDirectoryChangeEvent) event );
        }
        else if( event instanceof ControllerDisposalEvent )
        {
            processControllerDisposalEvent( (ControllerDisposalEvent) event );
        }
    }

    private void processWorkingDirectoryChangeEvent( WorkingDirectoryChangeEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof ControllerContextListener )
            {
                ControllerContextListener listener = (ControllerContextListener) eventListener;
                try
                {
                    listener.workingDirectoryChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ControllerContextListener working dir change notification error.";
                    getInternalLogger().error( error, e );
                }
            }
        }
    }

    private void processTempDirectoryChangeEvent( TempDirectoryChangeEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof ControllerContextListener )
            {
                ControllerContextListener listener = (ControllerContextListener) eventListener;
                try
                {
                    listener.tempDirectoryChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ControllerContextListener temp dir change notification error.";
                    getInternalLogger().error( error, e );
                }
            }
        }
    }

    private void processControllerDisposalEvent( ControllerDisposalEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof ControllerContextListener )
            {
                ControllerContextListener listener = (ControllerContextListener) eventListener;
                try
                {
                    listener.controllerDisposal( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ControllerContextListener disposal notification error.";
                    getInternalLogger().error( error, e );
                }
            }
        }
    }

    //----------------------------------------------------------------------------
    // static (private)
    //----------------------------------------------------------------------------

   /**
    * Working directory change event impl.
    */
    private static class WorkingDirectoryChangeEvent extends ControllerContextEvent
    {
        public WorkingDirectoryChangeEvent( 
          ControllerContext source, File oldDir, File newDir )
        {
            super( source, oldDir, newDir );
        }
    }

   /**
    * Temp directory change event impl.
    */
    private static class TempDirectoryChangeEvent extends ControllerContextEvent
    {
        public TempDirectoryChangeEvent( 
          ControllerContext source, File oldDir, File newDir )
        {
            super( source, oldDir, newDir );
        }
    }

   /**
    * Disposal event.
    */
    private static class ControllerDisposalEvent extends ControllerContextEvent
    {
        public ControllerDisposalEvent( ControllerContext source )
        {
            super( source, null, null );
        }
    }

   /**
    * Internal utility class to handle context disposal on JVM shutdown.
    */
    private static class ContextShutdownHook extends Thread
    {
        private InitialContext m_context;
        
       /**
        * Creation of a new initial context shutdown hook.
        * @param context the initional context to be shutdown on JVM shutdown
        */
        ContextShutdownHook( InitialContext context )
        {
            m_context = context;
        }
        
       /**
        * Execute context disposal.
        */
        public void run()
        {
            try
            {
                m_context.dispose();
            }
            catch( Throwable e )
            {
                boolean ignorable = true;
            }
            finally
            {
                System.runFinalization();
            }
        }
    }
}
