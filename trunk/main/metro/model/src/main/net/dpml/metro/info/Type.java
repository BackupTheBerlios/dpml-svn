/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.metro.info;

import java.io.Serializable;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.lang.reflect.Method;

import net.dpml.state.State;

/**
 * This class contains the meta information about a particular
 * component type. It describes;
 *
 * <ul>
 *   <li>Human presentable meta data such as name, version, description etc
 *   useful when assembling a system.</li>
 *   <li>the context that this component requires</li>
 *   <li>the services that this component type is capable of providing</li>
 *   <li>the services that this component type requires to operate (and the
 *   names via which services are accessed)</li>
 *   <li>information about the component lifestyle</li>
 *   <li>component collection preferences</li>
 * </ul>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Type extends Composite implements Serializable
{
    static final long serialVersionUID = 1L;
    
    private final InfoDescriptor m_info;
    private final CategoryDescriptor[] m_categories;
    private final ContextDescriptor m_context;
    private final ServiceDescriptor[] m_services;
    private final State m_graph;

   /**
    * Creation of a new Type instance using a supplied component descriptor,
    * logging, context, services, and part references.
    *
    * @param info information about the component type
    * @param loggers a set of logger descriptors the declare the logging channels
    *   required by the type
    * @param context a component context descriptor that declares the context type
    *   and context entry key and value classnames
    * @param services a set of service descriptors that detail the service that
    *   this component type is capable of supplying
    * @param parts an array of part descriptors
    * @param graph the state graph
    * @exception NullPointerException if the info, loggers, state, or context is null
    */
    public Type( 
      final InfoDescriptor info, final CategoryDescriptor[] loggers,
      final ContextDescriptor context, final ServiceDescriptor[] services,
      final PartReference[] parts, State graph )
      throws NullPointerException 
    {
        super( parts );
        
        if( null == info )
        {
            throw new NullPointerException( "info" );
        }
        if( null == loggers )
        {
            throw new NullPointerException( "loggers" );
        }
        if( null == context )
        {
            throw new NullPointerException( "context" );
        }
        if( null == graph )
        {
            throw new NullPointerException( "graph" );
        }
        if( null == services )
        {
            m_services = new ServiceDescriptor[0];
        }
        else
        {
            m_services = services;
        }

        m_info = info;
        m_categories = loggers;
        m_context = context;
        m_graph = graph;
    }
    
   /**
    * Return the state graph for the component type.
    * @return the state graph
    */
    public State getStateGraph()
    {
        return m_graph;
    }

   /**
    * Return the info descriptor.
    *
    * @return the component info descriptor.
    */
    public InfoDescriptor getInfo()
    {
        return m_info;
    }

    /**
     * Return the set of Logger that this Component will use.
     *
     * @return the set of Logger that this Component will use.
     */
    public CategoryDescriptor[] getCategoryDescriptors()
    {
        return m_categories;
    }

    /**
     * Return TRUE if the logging categories includes a category with
     * a matching name.
     *
     * @param name the logging category name
     * @return TRUE if the logging category is declared.
     */
    public boolean isaCategory( String name )
    {
        CategoryDescriptor[] loggers = getCategoryDescriptors();
        for( int i = 0; i < loggers.length; i++ )
        {
            CategoryDescriptor logger = loggers[ i ];
            if( logger.getName().equals( name ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the ContextDescriptor for component.
     *
     * @return the ContextDescriptor for component.
     */
    public ContextDescriptor getContextDescriptor()
    {
        return m_context;
    }

    /**
     * Get the set of service descriptors defining the set of services that
     * the component type exports.
     *
     * @return the array of service descriptors
     */
    public ServiceDescriptor[] getServiceDescriptors()
    {
        return m_services;
    }

    /**
     * Retrieve a service descriptor matching the supplied reference.
     *
     * @param reference a service descriptor to match against
     * @return a matching service descriptor or null if no match found
     */
    public ServiceDescriptor getServiceDescriptor( final ServiceDescriptor reference )
    {
        for ( int i = 0; i < m_services.length; i++ )
        {
            final ServiceDescriptor service = m_services[i];
            if ( service.matches( reference ) )
            {
                return service;
            }
        }
        return null;
    }

    /**
     * Retrieve a service descriptor matching the supplied classname.
     *
     * @param classname the service classname
     * @return the matching service descriptor or null if it does not exist
     */
    public ServiceDescriptor getServiceDescriptor( final String classname )
    {
        for ( int i = 0; i < m_services.length; i++ )
        {
            final ServiceDescriptor service = m_services[i];
            if ( service.getClassname().equals( classname ) )
            {
                return service;
            }
        }
        return null;
    }

    /**
     * Return a string representation of the type.
     * @return the stringified type
     */
    public String toString()
    {
        return getInfo().toString();
    }

   /**
    * Test is the supplied object is equal to this object.
    * @param other the other object
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        if( !( other instanceof Type ) )
        {
            return false;
        }
        Type t = (Type) other;
        if( !m_info.equals( t.m_info ) )
        {
            return false;
        }
        if( !m_context.equals( t.m_context ) )
        {
            return false;
        }
        if( !m_graph.equals( t.m_graph ) )
        {
            return false;
        }
        for( int i=0; i<m_categories.length; i++ )
        {
            if( !m_categories[i].equals( t.m_categories[i] ) )
            {
                return false;
            }
        }
        for( int i=0; i<m_services.length; i++ )
        {
            if( !m_services[i].equals( t.m_services[i] ) )
            {
                return false;
            }
        }
        return true;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_info.hashCode();
        hash ^= m_context.hashCode();
        hash ^= m_graph.hashCode();
        for( int i = 0; i < m_services.length; i++ )
        {
            hash ^= m_services[i].hashCode();
            hash = hash - 163611323;
        }
        for( int i = 0; i < m_categories.length; i++ )
        {
            hash ^= m_categories[i].hashCode();
            hash = hash + 471312761;
        }
        return hash;
    }
    
   /**
    * Utility operation to construct a default type given a supplied class.
    * @param subject the component implementation class
    * @return the defalt type descriptor for the class
    */
    public static Type createType( Class subject )
       throws IntrospectionException
    {
        final InfoDescriptor info = new InfoDescriptor( null, subject.getName() );
        final CategoryDescriptor[] loggers = new CategoryDescriptor[0];
        final ContextDescriptor context = createContextDescriptor( subject );
        final ServiceDescriptor[] services = 
          new ServiceDescriptor[]{
            new ServiceDescriptor( subject.getName() )
          };
        final PartReference[] parts = new PartReference[0];
        return new Type( info, loggers, context, services, parts, State.NULL_STATE );
    }
    
    private static ContextDescriptor createContextDescriptor( Class subject ) 
      throws IntrospectionException
    {
        EntryDescriptor[] entries = createEntryDescriptors( subject );
        return new ContextDescriptor( entries );
    }

    private static EntryDescriptor[] createEntryDescriptors( Class subject ) 
       throws IntrospectionException
    {
        String classname = subject.getName();
        Class[] classes = subject.getClasses();
        Class param = locateClass( "$Context", classes );
        if( null == param )
        {
            return new EntryDescriptor[0];
        }
        else
        {
            //
            // For each method in the Context inner-interface we construct a 
            // descriptor that establishes the key, type, and required status.
            //

            Method[] methods = param.getMethods();
            ArrayList list = new ArrayList();
            for( int i=0; i<methods.length; i++ )
            {
                Method method = methods[i];
                String name = method.getName();
                if( name.startsWith( "get" ) )
                {
                    EntryDescriptor descriptor = 
                      createEntryDescriptor( method );
                    list.add( descriptor );
                }
            }
            return (EntryDescriptor[]) list.toArray( new EntryDescriptor[0] );
        }
    }

   /**
    * Creation of a new parameter descriptor using a supplied method.
    * The method is the method used by the component implementation to get the parameter 
    * instance. 
    */
    private static EntryDescriptor createEntryDescriptor( Method method ) 
      throws IntrospectionException
    {
        validateMethodName( method );
        validateNoExceptions( method );

        String key = EntryDescriptor.getEntryKey( method );

        Class returnType = method.getReturnType();
        if( method.getParameterTypes().length == 0 )
        {
            //
            // required context entry
            //

            validateNonNullReturnType( method );
            String type = returnType.getName();
            return new EntryDescriptor( key, type, EntryDescriptor.REQUIRED );
        }
        else if( method.getParameterTypes().length == 1 )
        {
            Class[] params = method.getParameterTypes();
            Class param = params[0];
            if( returnType.isAssignableFrom( param ) )
            {
                String type = param.getName();
                return new EntryDescriptor( key, type, EntryDescriptor.OPTIONAL );
            }
            else
            {
                final String error = 
                  "Context entry assessor declares an optional default parameter class ["
                  + param.getName()
                  + "] which is not assignable to the return type ["
                  + returnType.getName()
                  + "]";
                throw new IntrospectionException( error );
            }
        }
        else
        {
            final String error =
              "Unable to establish a required or optional context entry method pattern on ["
              + method.getName()
              + "]";
            throw new IntrospectionException( error );
        }
    }
    
    private static void validateMethodName( Method method ) 
      throws IntrospectionException
    {
        if( !method.getName().startsWith( "get" ) )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] does not start with 'get'.";
            throw new IntrospectionException( error );
        }
    }

    private static void validateNoExceptions( Method method ) 
      throws IntrospectionException
    {
        Class[] exceptionTypes = method.getExceptionTypes();
        if( exceptionTypes.length > 0 )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares one or more exceptions.";
            throw new IntrospectionException( error );
        }
    }

    private static void validateNonNullReturnType( Method method ) 
       throws IntrospectionException
    {
        Class returnType = method.getReturnType();
        if( Void.TYPE.equals( returnType ) ) 
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] does not declare a return type.";
            throw new IntrospectionException( error );
        }
    }

    private static Class locateClass( String postfix, Class[] classes )
    {
        for( int i=0; i<classes.length; i++ )
        {
            Class inner = classes[i];
            String name = inner.getName();
            if( name.endsWith( postfix ) )
            {
                return inner;
            }
        }
        return null;
    }
}
