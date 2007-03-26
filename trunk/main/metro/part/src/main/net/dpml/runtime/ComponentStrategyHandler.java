/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

import dpml.lang.Classpath;
import dpml.lang.DOM3DocumentBuilder;
import dpml.lang.Value;
import dpml.util.StandardClassLoader;
import dpml.util.ElementHelper;
import dpml.util.DefaultLogger;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.lang.reflect.Constructor;

import net.dpml.lang.DecodingException;
import net.dpml.lang.Strategy;
import net.dpml.lang.StrategyHandler;
import net.dpml.lang.Buffer;

import net.dpml.annotation.Activation;
import net.dpml.annotation.ActivationPolicy;
import net.dpml.annotation.LifestylePolicy;
import net.dpml.annotation.CollectionPolicy;

import net.dpml.util.Resolver;
import net.dpml.util.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Strategy handler for the component model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentStrategyHandler implements StrategyHandler
{
    public static final String NAMESPACE = "dpml:metro";
    
    private static final DOM3DocumentBuilder BUILDER = new DOM3DocumentBuilder();
    
    public Strategy newStrategy( Class<?> c ) throws IOException
    {
        return newStrategy( c, null );
    }
    
    public Strategy newStrategy( Class<?> c, String name ) throws IOException
    {
        String spec = getComponentName( c, name );
        Profile profile = new Profile( c, spec, null );
        ContextModel context = getContextModel( null, c, spec, profile, null, null, null, true );
        PartsDirective parts = profile.getPartsDirective();
        return new ComponentStrategy( 
          null, spec, 0, c, ActivationPolicy.SYSTEM, LifestylePolicy.THREAD, 
          CollectionPolicy.HARD, context, parts );
    }
    
    public Component newComponent( Class<?> c, String name ) throws IOException
    {
        return (Component) newStrategy( c, name );
    }
    
   /**
    * Construct a strategy definition using a supplied element and value resolver.
    * @param classloader the operational classloader
    * @param element the DOM element definining the deployment strategy
    * @param resolver symbolic property resolver
    * @param partition the enclosing partition
    * @param query the query fragment to applied to the component context definition
    * @return the strategy definition
    * @exception IOException if an I/O error occurs
    */
    public Strategy build( 
      ClassLoader classloader, Element element, Resolver resolver, String partition, 
      String query, boolean validate ) throws IOException
    {
        Class c = loadComponentClass( classloader, element, resolver );
        String name = getComponentName( c, element, resolver );
        int priority = getPriority( element, resolver );
        String path = getComponentPath( partition, name );
        Profile profile = new Profile( c, path, resolver );
        
        Query q = new Query( query );
        Element contextElement = ElementHelper.getChild( element, "context" );
        ContextModel context = 
          getContextModel( 
            classloader, c, path, profile, contextElement, resolver, q, validate );
        ActivationPolicy activation = getActivationPolicy( element, profile, c );
        LifestylePolicy lifestyle = getLifestylePolicy( element, profile, c );
        CollectionPolicy collection = getCollectionPolicy( element, profile, c );
        
        try
        {
            Element partsElement = ElementHelper.getChild( element, "parts" );
            PartsDirective parts = 
              new PartsDirective( 
                profile.getPartsDirective(), 
                classloader, 
                partsElement, 
                resolver, 
                path );

            ComponentStrategy strategy = 
              new ComponentStrategy( 
                partition, 
                name, 
                priority, 
                c, 
                activation,
                lifestyle,
                collection,
                context, 
                parts );
            
            return strategy;
        }
        catch( IOException ioe )
        {
            throw ioe;
        }
        catch( Exception e )
        {
            final String error = 
              "Unable to construct component strategy for the class [" 
              + c.getName() 
              + "] under the partition ["
              + partition 
              + "]";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }
    
   /**
    * Return the activation policy.
    * 
    * @return the resolved activation policy
    */
    private static ActivationPolicy getActivationPolicy( Element element, Profile profile, Class<?> c )
    {
        if( null != element )
        {
            String value = ElementHelper.getAttribute( element, "activation" );
            if( null != value )
            {
                return ActivationPolicy.valueOf( value.toUpperCase() );
            }
        }
        if( null != profile )
        {
            return profile.getActivationPolicy();
        }
        if( c.isAnnotationPresent( Activation.class ) )
        {
            Activation annotation = 
              c.getAnnotation( Activation.class );
            return annotation.value();
        }
        return ActivationPolicy.SYSTEM;
    }
    
   /**
    * Return the lifestyle policy.
    * 
    * @return the resolved lifestyle policy
    */
    private static LifestylePolicy getLifestylePolicy( Element element, Profile profile, Class<?> c )
    {
        if( null != element )
        {
            String value = ElementHelper.getAttribute( element, "lifestyle" );
            if( null != value )
            {
                return LifestylePolicy.valueOf( value.toUpperCase() );
            }
        }
        if( null != profile )
        {
            return profile.getLifestylePolicy();
        }
        if( c.isAnnotationPresent( net.dpml.annotation.Component.class ) )
        {
            net.dpml.annotation.Component annotation = 
              c.getAnnotation( net.dpml.annotation.Component.class );
            return annotation.lifestyle();
        }
        return LifestylePolicy.THREAD;
    }
    
   /**
    * Return the collection policy.
    * 
    * @return the resolved collection policy
    */
    private static CollectionPolicy getCollectionPolicy( Element element, Profile profile, Class<?> c )
    {
        if( null != element )
        {
            String value = ElementHelper.getAttribute( element, "collection" );
            if( null != value )
            {
                return CollectionPolicy.valueOf( value.toUpperCase() );
            }
        }
        if( null != profile )
        {
            return profile.getCollectionPolicy();
        }
        if( c.isAnnotationPresent( net.dpml.annotation.Component.class ) )
        {
            net.dpml.annotation.Component annotation = 
              c.getAnnotation( net.dpml.annotation.Component.class );
            return annotation.collection();
        }
        return CollectionPolicy.HARD;
    }
    
    private int getPriority( Element element, Resolver resolver )
    {
        String value = ElementHelper.getAttribute( element, "priority", null, resolver );
        if( null == value )
        {
            return 0;
        }
        else
        {
            return Integer.parseInt( value );
        }
    }
    
    private Class loadComponentClass( ClassLoader classloader, Element element, Resolver resolver ) throws DecodingException
    {
        String classname = buildComponentClassname( element, resolver );
        try
        {
            return classloader.loadClass( classname );
        }
        catch( ClassNotFoundException e )
        {
            final String error =
              "Class not found [" + classname + "].";
            Logger logger = new DefaultLogger( "dpml.lang" );
            String stack = StandardClassLoader.toString( classloader, null );
            logger.error( error + stack );
            throw new DecodingException( error, e, element );
        }
    }
    
    private String buildComponentClassname( Element element, Resolver resolver )
    {
        String classname = ElementHelper.getAttribute( element, "class", null, resolver );
        if( null != classname )
        {
            return classname;
        }
        else
        {
            return ElementHelper.getAttribute( element, "type", null, resolver );
        }
    }

    private static String getComponentName( Class c, Element element, Resolver resolver )
    {
        String name = getComponentName( element, resolver );
        return getComponentName( c, name );
    }
    
    private static String getComponentName( Element element, Resolver resolver )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            String key = ElementHelper.getAttribute( element, "key", null, resolver );
            if( null != key )
            {
                return key;
            }
            else
            {
                return ElementHelper.getAttribute( element, "name", null, resolver );
            }
        }
    }

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
    
    private static String getComponentPath( String partition, String name )
    {
        if( null == partition )
        {
            return name;
        }
        else
        {
            return partition + "." + name;
        }
    }
    
    private static String getComponentName( Class c, String name )
    {
        if( null != name )
        {
            return name;
        }
        else
        {
            return  getComponentName( c );
        }
    }

    static Constructor getSingleConstructor( Class c )
    {
        Constructor[] constructors = c.getConstructors();
        if( constructors.length < 1 )
        {
            final String error =
              "The component class ["
              + c.getName()
              + "] does not declare a public constructor.";
            throw new ComponentError( error );
        }
        else if( constructors.length > 1 )
        {
            final String error =
              "The component class ["
              + c.getName()
              + "] declares more than one public constructor.";
            throw new ComponentError( error );
        }
        else
        {
            return constructors[0];
        }
    }

    private static Class getContextClass( Constructor constructor, boolean policy )
    {
        final Class<?>[] types = constructor.getParameterTypes();
        for( Class c : types )
        {
            if( ContextInvocationHandler.isaContext( c, policy ) )
            {
                return c;
            }
        }
        return null;
    }

    private static ContextModel getContextModel( 
      ClassLoader classloader, Class clazz, String path, 
      Profile profile, Element element, Resolver resolver, Query query,
      boolean validate  ) throws IOException
    {
        boolean policy = getContextHandlingPolicy( clazz );
        Constructor constructor = getSingleConstructor( clazz );
        Class subject = getContextClass( constructor, policy );
        ContextDirective bundled = profile.getContextDirective();
        ContextDirective declared = new ContextDirective( classloader, element, resolver );
        return new ContextModel( clazz, path, subject, policy, bundled, declared, query, validate );
    }
    
    private static boolean getContextHandlingPolicy( Class c )
    {
        // TODO: update to get policy as a class annotation
        return false; // return classic evaluation policy
    }

}
