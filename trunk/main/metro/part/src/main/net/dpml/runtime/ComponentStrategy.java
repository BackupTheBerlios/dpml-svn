/*
 * Copyright 2004-2007 Stephen J. McConnell.
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

package net.dpml.runtime;

import dpml.appliance.StandardAppliance;
import dpml.lang.Disposable;
import dpml.state.StateDecoder;
import dpml.util.DefaultLogger;

import java.io.IOException;
import java.io.File;
import java.lang.ref.Reference;
import java.net.URI;
import java.net.URL;
import java.util.WeakHashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import net.dpml.annotation.CollectionPolicy;
import net.dpml.annotation.LifestylePolicy;
import net.dpml.annotation.ActivationPolicy;

import net.dpml.appliance.Appliance;

import net.dpml.lang.Buffer;
import net.dpml.lang.ServiceRegistry;
import net.dpml.lang.StandardServiceRegistry;
import net.dpml.lang.Strategy;

import net.dpml.state.State;

import net.dpml.transit.Artifact;

import net.dpml.util.Logger;

import static dpml.state.DefaultState.NULL_STATE;

/**
 * Component strategy.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentStrategy extends Strategy implements Component, ServiceRegistry
{
    private static final Logger LOGGER = new DefaultLogger( "dpml.lang.component" );
    private static final ComponentStrategyHandler HANDLER = new ComponentStrategyHandler();
    
    private final String m_name;
    private final int m_priority;
    private final Class<?> m_class;
    private final String m_path;
    private final LifestylePolicy m_lifestyle;
    private final CollectionPolicy m_collection;
    private final State m_graph;
    private final LifestyleHandler m_handler;
    private final ContextModel m_context;
    private final ActivationPolicy m_activation;
    private final PartsDirective m_parts;
    private final Logger m_logger;
    private final Map<String,Object> m_map = new Hashtable<String,Object>();
    
    private final Set<ComponentListener> m_listeners = new CopyOnWriteArraySet<ComponentListener>();
    private final ExecutorService m_queue = Executors.newSingleThreadExecutor();
    
    private ServiceRegistry m_registry;
    
   /**
    * Creation of a new component strategy.
    * @param partition the enclosing partition
    * @param name the component name relative to the enclosing partition
    * @param priority the component priority
    * @param type the component class
    * @param activation the activation policy
    * @param lifestyle the lifestyle policy
    * @param collection the collection policy
    * @param context the context model
    * @param parts the internal part structure
    * @exception IOException if an IO error occurs
    */
    ComponentStrategy( 
      final String partition, final String name, int priority, final Class type, 
      ActivationPolicy activation, LifestylePolicy lifestyle, CollectionPolicy collection, 
      ContextModel context, PartsDirective parts ) 
      throws IOException
    {
        super( type.getClassLoader() );
        
        m_class = type;
        m_priority = priority;
        m_activation = activation;
        m_lifestyle = lifestyle;
        m_collection = collection;
        m_context = context;
        
        m_name = getComponentName( name, m_class );
        m_path = getComponentPath( partition, m_name, m_class );
        m_logger = getComponentLogger( m_path );
        m_graph = getLifecycleGraph( m_class );
        m_parts = getPartsDirective( parts );
        
        m_parts.initialize( this );
        
        m_map.put( "name", m_name );
        m_map.put( "path", m_path );
        m_map.put( "work", new File( System.getProperty( "user.dir" ) ).getCanonicalFile() );
        m_map.put( "temp", new File( System.getProperty( "java.io.tmpdir" ) ).getCanonicalFile() );
        m_map.put( "uri", URI.create( "component:" + m_path ) ); 
        
        if( m_logger.isTraceEnabled() )
        {
            final String message = 
              "new "
              + m_collection.toString().toLowerCase()
              + " "
              + m_lifestyle.toString().toLowerCase()
              + " ["
              + m_class.getName()
              + "]";
            m_logger.trace( message );
        }
        
        m_handler = getLifestyleHandler( m_lifestyle );
    }
    
   /**
    * Get the component name.
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Get the component priority.
    * @return the priority
    */
    public int getPriority()
    {
        return m_priority;
    }
    
   /**
    * Add a listener to the component.
    * @param listener the component listener
    */
    public void addComponentListener( ComponentListener listener )
    {
        m_listeners.add( listener );
    }
    
   /**
    * Remove a listener from the component.
    * @param listener the component listener
    */
    public void removeComponentListener( ComponentListener listener )
    {
        m_listeners.remove( listener );
    }

    void processEvent( ComponentEvent event )
    {
        Logger logger= getLogger();
        for( ComponentListener listener : m_listeners )
        {
            m_queue.execute( new ComponentEventDistatcher( logger, listener, event ) );
        }
    }
    
    Map<String,Object> getContextMap()
    {
        return m_map;
    }
    
   /**
    * Get a runtime provider for this component.
    * @return the provider
    */
    public Provider getProvider()
    {
        synchronized( m_handler )
        {
            return m_handler.getProvider();
        }
    }
    
   /**
    * Release a provider back to the component.
    * @param provider the provider to release
    */
    public void release( Provider provider )
    {
        synchronized( m_handler )
        {
            m_handler.release( provider );
        }
    }
    
   /**
    * Initialize the component with a supplied service registry.
    * @param registry the service registry
    */
    public void initialize( ServiceRegistry registry ) // TODO: parts initialization should occur here?
    {
        if( m_logger.isTraceEnabled() )
        {
            m_logger.trace( "initialization" );
        }
        m_registry = registry;
    }

   /**
    * Test if this component model can handle the supplied service type.
    * @param type the service type
    * @return true if the component is type compatible
    */
    public boolean isaCandidate( Class<?> type )
    {
        return type.isAssignableFrom( m_class );
    }
    
   /**
    * Get a service reference for the supplied type.
    * @param service the service type
    * @return a service instance or null if unresolvable
    */
    public <T>T lookup( Class<T> service )
    {
        if( m_logger.isTraceEnabled() )
        {
            m_logger.trace( "lookup: " + service.getName() );
        }
        
        for( String key : m_parts.getKeys() )
        {
            Strategy strategy = m_parts.getStrategy( key );
            if( strategy.isaCandidate( service ) )
            {
                try
                {
                    return strategy.getInstance( service );
                }
                catch( Exception e )
                {
                    if( strategy instanceof ComponentStrategy )
                    {
                        ComponentStrategy s = (ComponentStrategy) strategy;
                        String path = s.getComponentPath();
                        
                        final String error = 
                          "Lookup aquisition in ["
                          + getComponentPath()
                          + "] failed while aquiring the service ["
                          + service.getName()
                          + "] from the provider ["
                          + path 
                          + "].";
                        throw new ComponentError( error, e );
                    }
                    else
                    {
                        final String error = 
                          "Lookup aquisition in ["
                          + getComponentPath()
                          + "] failed while aquiring the service ["
                          + service.getName()
                          + "].";
                        throw new ComponentError( error, e );
                    }
                }
            }
        }
        
        if( null != m_registry )
        {
            return m_registry.lookup( service );
        }
        else
        {
            ServiceRegistry registry = new StandardServiceRegistry();
            return registry.lookup( service );
        }
    }
    
   /**
    * Terminate the component model.
    */
    public void terminate()
    {
        terminate( 10, TimeUnit.SECONDS );
    }
    
   /**
    * Terminate the component model using a supplied timeout criteria.
    * @param timeout the timeout duration
    * @param units the measurement units
    */
    void terminate( long timeout, TimeUnit units )
    {
        synchronized( this )
        {
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "termination" );
            }
            m_handler.terminate();
            m_queue.shutdown();
            try
            {
                boolean ok = m_queue.awaitTermination( timeout, units );
                if( !ok )
                {
                    final String message = 
                      "Component termination timeout in ["
                      + getName()
                      + "] (some events may not have been processed).";
                    getLogger().warn( message );
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
    
   /**
    * Internal support for the resolution of a context service lookup request.
    * The service classname comes from a context entry in this component and 
    * is resolved by the parent component.  The parent evaluates off of its 
    * internal parts for a component implementing the service and if found, 
    * the instance is returned.
    * @param class the requested class
    * @param type the return type
    * @exception Exception if an error occurs
    */
    <T>T getService( Class<?> clazz, Class<T> type ) throws Exception // TODO: ensure we don't evaluate the requestor
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "invoking lookup in " + getComponentPath() + " for " + clazz.getName() );
        }
        if( null != m_registry )
        {
            try
            {
                Object value = m_registry.lookup( clazz );
                return type.cast( value );
            }
            catch( Exception e )
            {
                final String error = 
                  "Service lookup in component ["
                  + getComponentPath()
                  + "] failed.";
                throw new ComponentException( error, e );
            }
        }
        else
        {
            return null;
        }
    }
    
    Class getComponentClass()
    {
        return m_class;
    }
    
    String getComponentName()
    {
        return m_name;
    }
    
    String getComponentPath()
    {
        return m_path;
    }
    
    ContextModel getContextModel()
    {
        return m_context;
    }
    
    PartsDirective getPartsDirective()
    {
        return m_parts;
    }
    
    State getStateGraph()
    {
        return m_graph;
    }
    
    Logger getComponentLogger()
    {
        return m_logger;
    }
    
    Logger getLogger()
    {
        return m_logger;
    }
    
   /**
    * Return the assigned collection policy.
    * @return the collection policy
    */
    public CollectionPolicy getCollectionPolicy()
    {
        return m_collection;
    }
    
   /**
    * Return the assigned lifestyle policy.
    * @return the lifestyle policy
    */
    public LifestylePolicy getLifestylePolicy()
    {
        return m_lifestyle;
    }
    
   /**
    * Return the assigned activation policy.
    * @return the activation policy
    */
    public ActivationPolicy getActivationPolicy()
    {
        return m_activation;
    }
    
    private Logger getComponentLogger( String path )
    {
        return new DefaultLogger( path );
    }
    
   /**
    * Return an instance of the requested class.
    * @param clazz the class
    * @return the instance
    */
    public <T>T getInstance( Class<T> clazz )
    {
        if( clazz.equals( Component.class ) )
        {
            return clazz.cast( this );
        }
        synchronized( m_handler )
        {
            Provider provider = getProvider();
            if( clazz.equals( Provider.class ) )
            {
                return clazz.cast( provider );
            }
            Object instance = provider.getInstance( clazz );
            return clazz.cast( instance );
        }
    }
    
   /**
    * Return an instance of the requested class.
    * @param c the class
    * @return the instance
    * @exception IOException if an error occurs
    */
    public <T>T getContentForClass( Class<T> c ) throws IOException
    {
        if( c.isAssignableFrom( m_class ) )
        {
            return getInstance( c );
        }
        else if( c == Provider.class )
        {
            synchronized( m_handler )
            {
                Provider provider = m_handler.getProvider();
                return c.cast( provider );
            }
        }
        else if( c.isAssignableFrom( getClass() ) )
        {
            return c.cast( this );
        }
        else if( c == Appliance.class ) 
        {
            Logger logger = getComponentLogger();
            Appliance appliance = new StandardAppliance( logger, this );
            return c.cast( appliance );
        }
        else
        {
            return null;
        }
    }
    
   /**
    * Encode the component model to the supplied buffer.
    * @param buffer the endoding buffer
    * @param key the component key
    * @exception IOException if an error occurs
    */
    public void encode(  Buffer buffer, String key ) throws IOException
    {
        String name = getComponentName();
        String classname = getComponentClass().getName();
        ContextDirective context = getContextModel().getDirective();
        PartsDirective parts = getPartsDirective();
        
        boolean flag = buffer.isNamespace( NAMESPACE );
        if( flag )
        {
            buffer.nl( "<component" );
        }
        else
        {
            buffer.nl( "<component xmlns=\"" + NAMESPACE + "\"" );
            buffer.nl( "    " );
        }
        if( null != key )
        {
            buffer.write( " key=\"" + key + "\"" );
        }
        if( null != name )
        {
            buffer.write( " name=\"" + name + "\"" );
        }
        if( m_priority != 0 )
        {
            buffer.write( " priority=\"" + m_priority + "\"" );
        }
        buffer.write( " class=\"" + classname + "\"" );
        if( ( context.size() == 0 ) && ( parts.size() == 0 ) )
        {
            buffer.write( "/>" ); 
        }
        else
        {
            buffer.write( ">" ); 
            Buffer b = buffer.namespace( NAMESPACE );
            context.encode( b.indent(), null );
            parts.encode( b.indent() );
            buffer.nl( "</component>" );
        }
    }
    
    private PartsDirective getPartsDirective( PartsDirective directive )
    {
        if( null == directive )
        {
            return new PartsDirective();
        }
        else
        {
            return directive;
        }
    }
    
    private String getComponentPath( String partition, String name, Class c )
    {
        if( null == partition )
        {
            return "/" + name;
        }
        else
        {
            return "/" + partition.replace( ".", "/" ) + "/" + name;
        }
    }
    
    private String getComponentName( String name, Class<?> c )
    {
        if( null != name )
        {
            return name;
        }
        else
        {
            return getComponentName( c );
        }
    }
    
    //-----------------------------------------------------------------------
    // Lifestyle handlers
    //-----------------------------------------------------------------------
    
    private LifestyleHandler getLifestyleHandler( LifestylePolicy policy )
    {
        if( policy.equals( LifestylePolicy.SINGLETON ) )
        {
            return new SingletonLifestyleHandler( this );
        }
        else if( policy.equals( LifestylePolicy.THREAD ) )
        {
            return new ThreadLifestyleHandler( this );
        }
        else
        {
            return new TransientLifestyleHandler( this );
        }
    }
    
   /**
    * Singleton holder class.  The singleton holder mains a single 
    * <tt>LifestyleHandler</tt> of a component relative to the component model 
    * identity within the scope of the controller.  References to the 
    * singleton instance will be shared across mutliple threads.
    */
    private class SingletonLifestyleHandler extends LifestyleHandler
    {
        private Reference<Provider> m_reference;
        
        SingletonLifestyleHandler( ComponentStrategy strategy )
        {
            super( strategy );
            Provider provider = new StandardProvider( strategy );
            m_reference = createReference( null );
        }
        
        Provider getProvider()
        {
            Provider provider = m_reference.get();
            if( null == provider )
            {
                ComponentStrategy strategy = getComponentStrategy();
                provider = new StandardProvider( strategy );
                m_reference = createReference( provider );
            }
            return provider;
        }
        
        void release( Provider provider )
        {
        }
        
        void terminate()
        {
            synchronized( this )
            {
                Provider provider = m_reference.get();
                if( null != provider )
                {
                    if( provider instanceof Disposable )
                    {
                        Disposable disposable = (Disposable) provider;
                        disposable.dispose();
                    }
                    m_reference = createReference( null ); 
                }
            }
        }
    }
    
   /**
    * Transient holder class.  The transient holder provides support for 
    * the transient lifestyle ensuing the creation of a new <tt>LifestyleHandler</tt>
    * per request.
    */
    private class TransientLifestyleHandler extends LifestyleHandler
    {
        private final WeakHashMap<Provider,Void> m_providers = new WeakHashMap<Provider,Void>(); // transients
        
        TransientLifestyleHandler( ComponentStrategy strategy )
        {
            super( strategy );
        }
        
        Provider getProvider()
        {
            ComponentStrategy strategy = getComponentStrategy();
            Provider provider = new StandardProvider( strategy );
            m_providers.put( provider, null );
            return provider;
        }
        
        void release( Provider provider )
        {
            if( null == provider )
            {
                return;
            }
            if( m_providers.containsKey( provider ) )
            {
                m_providers.remove( provider );
                if( provider instanceof Disposable )
                {
                    Disposable disposable = (Disposable) provider;
                    disposable.dispose();
                }
            }
        }
        
        void terminate()
        {
            Provider[] providers = m_providers.keySet().toArray( new Provider[0] );
            for( Provider provider : providers )
            {
                release( provider );
            }
        }
    }

   /**
    * The ThreadHolder class provides support for the per-thread lifestyle
    * policy within which new <tt>LifestyleHandler</tt> creation is based on a single
    * <tt>LifestyleHandler</tt> per thread.
    */
    private class ThreadLifestyleHandler extends LifestyleHandler
    {
        private final ThreadLocalHolder m_threadLocalHolder = new ThreadLocalHolder();
        
        ThreadLifestyleHandler( ComponentStrategy strategy )
        {
            super( strategy );
        }
        
        Provider getProvider()
        {
            return (Provider) m_threadLocalHolder.get();
        }
        
        void release( Provider provider )
        {
            m_threadLocalHolder.release( provider );
        }

        void terminate()
        {
            m_threadLocalHolder.terminate();
        }
        
       /**
        * Internal thread local holder for the per-thread lifestyle holder.
        */
        private class ThreadLocalHolder extends ThreadLocal
        {
            private final WeakHashMap<Provider,Void> m_providers = 
              new WeakHashMap<Provider,Void>(); // per thread instances
            
            protected synchronized Provider initialValue()
            {
                ComponentStrategy strategy = getComponentStrategy();
                Provider provider = new StandardProvider( strategy );
                m_providers.put( provider, null );
                return provider;
            }
            
            synchronized void release( Provider provider )
            {
                if( m_providers.containsKey( provider ) )
                {
                    m_providers.remove( provider );
                    if( provider instanceof Disposable )
                    {
                        Disposable disposable = (Disposable) provider;
                        disposable.dispose();
                    }
                }
            }
            
            synchronized void terminate()
            {
                Provider[] providers = m_providers.keySet().toArray( new Provider[0] );
                for( Provider provider : providers )
                {
                    release( provider );
                }
            }
        }
    }
    
    //-----------------------------------------------------------------------
    // utilities
    //-----------------------------------------------------------------------

    private static String getComponentName( Class<?> c )
    {
        if( c.isAnnotationPresent( net.dpml.annotation.Component.class ) )
        {
            net.dpml.annotation.Component annotation = 
              c.getAnnotation( net.dpml.annotation.Component.class );
            String name = annotation.name();
            if( !"".equals( name ) )
            {
                return name;
            }
        }
        return c.getName();
    }
    
    private static String getComponentNameFromClass( Class<?> c )
    {
        String classname = c.getName();
        int n = classname.lastIndexOf( "." );
        if( n > -1 )
        {
            return classname.substring( n+1 );
        }
        else
        {
            return classname;
        }
    }
    
    private static CollectionPolicy getCollectionPolicy( Class<?> c )
    {
        if( c.isAnnotationPresent( net.dpml.annotation.Component.class ) )
        {
            net.dpml.annotation.Component annotation = 
              c.getAnnotation( net.dpml.annotation.Component.class );
            return annotation.collection();
        }
        return CollectionPolicy.HARD;
    }
    
    private static State getLifecycleGraph( Class<?> c ) throws IOException
    {
        String name = getComponentNameFromClass( c );
        String spec = name + ".xgraph";
        URL url = getLifecycleURL( c, spec );
        if( null != url )
        {
            StateDecoder decoder = new StateDecoder();
            return decoder.loadState( url );
        }
        if( c.isAnnotationPresent( net.dpml.annotation.Component.class ) )
        {
            net.dpml.annotation.Component annotation = 
              c.getAnnotation( net.dpml.annotation.Component.class );
            String path = annotation.lifecycle();
            return getLifecycleGraph( c, path );
        }
        else
        {
            return NULL_STATE;
        }
    }
    
    private static State getLifecycleGraph( Class<?> c, String path ) throws IOException
    {
        if( ( null == path ) || "".equals( path ) )
        {
            return NULL_STATE;
        }
        else
        {
            URL url = getLifecycleURL( c, path );
            StateDecoder decoder = new StateDecoder();
            return decoder.loadState( url );
        }
    }
    
    private static URL getLifecycleURL( Class<?> c, String path ) throws IOException
    {
        int n = path.indexOf( ":" );
        if( n > -1 )
        {
            try
            {
                return Artifact.toURL( new URI( path ) );
            }
            catch( Exception e )
            {
                final String error = 
                  "Bad url: " + path;
                IOException ioe = new IOException( error );
                ioe.initCause( e );
                throw ioe;
            }
        }
        else
        {
            return c.getResource( path );
        }
    }

    private static final String NAMESPACE = ComponentStrategyHandler.NAMESPACE;
    
   /**
    * Returns the string representing of the component.
    * @return the component as a string
    */
    public String toString()
    {
        return getComponentPath();
    }

   /**
    * Component event dispatcher.
    */
    private static class ComponentEventDistatcher implements Runnable
    {
        private Logger m_logger;
        private ComponentListener m_listener;
        private ComponentEvent m_event;
        
        ComponentEventDistatcher( Logger logger, ComponentListener listener, ComponentEvent event )
        {
            m_logger = logger;
            m_listener = listener;
            m_event = event;
        }
        
       /**
        * Run the dispatch thread.
        */
        public void run()
        {
            try
            {
                m_listener.componentChanged( m_event );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Event distatch error.";
                m_logger.error( error, e );
            }
        }
    }
}
