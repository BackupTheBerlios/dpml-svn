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

package org.apache.avalon.meta.info.verifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Suspendable;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Reconfigurable;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Recontextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Reparameterizable;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Utility class to help verify that component respects the
 * rules of an Avalon component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TypeVerifier.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class TypeVerifier
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( TypeVerifier.class );

    private static final Class[] EMPTY_TYPES = new Class[ 0 ];

    /**
     * The interfaces representing lifecycle stages.
     */
    private static final Class[] FRAMEWORK_CLASSES = new Class[]
    {
        LogEnabled.class,
        Contextualizable.class,
        Recontextualizable.class,
        Composable.class,
        Recomposable.class,
        Serviceable.class,
        Configurable.class,
        Reconfigurable.class,
        Parameterizable.class,
        Reparameterizable.class,
        Initializable.class,
        Startable.class,
        Suspendable.class,
        Disposable.class
    };

    /**
     * Verify that the supplied implementation class
     * and service classes are valid for a component.
     *
     * @param name the name of component
     * @param implementation the implementation class of component
     * @param services the classes representing services
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyType( final String name,
                                 final Class implementation,
                                 final Class[] services )
        throws VerifyException
    {
        verifyClass( name, implementation );
        verifyLifecycles( name, implementation );
        verifyServices( name, services );
        verifyImplementsServices( name, implementation, services );
    }


    /**
     * Verify that the supplied implementation implements the specified
     * services.
     *
     * @param name the name of component
     * @param implementation the class representign component
     * @param services the services that the implementation must provide
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyImplementsServices( final String name,
                                          final Class implementation,
                                          final Class[] services )
        throws VerifyException
    {
        for( int i = 0; i < services.length; i++ )
        {
            if( !services[ i ].isAssignableFrom( implementation ) )
            {
                final String message =
                    REZ.getString( "verifier.noimpl-service.error",
                                   name,
                                   implementation.getName(),
                                   services[ i ].getName() );
                throw new VerifyException( message );
            }
        }
    }

    /**
     * Verify that the supplied class is a valid class for
     * a Component.
     *
     * @param name the name of component
     * @param clazz the class representing component
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyClass( final String name,
                             final Class clazz )
        throws VerifyException
    {
        verifyPublicConstructor( name, clazz );
        verifyNonAbstract( name, clazz );
        verifyNonArray( name, clazz );
        verifyNonInterface( name, clazz );
        verifyNonPrimitive( name, clazz );
        verifyPublic( name, clazz );
    }

    /**
     * Verify that the supplied classes are valid classes for
     * a service.
     *
     * @param name the name of component
     * @param classes the classes representign services
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyServices( final String name,
                                final Class[] classes )
        throws VerifyException
    {
        for( int i = 0; i < classes.length; i++ )
        {
            verifyService( name, classes[ i ] );
        }
    }

    /**
     * Verify that the supplied class is a valid class for
     * a service.
     *
     * @param name the name of component
     * @param clazz the class representign service
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyService( final String name,
                               final Class clazz )
        throws VerifyException
    {
        verifyServiceIsPublic( name, clazz );

        //
        // the following two validation points need more work
        // (a) it is valid to pass a class as a service because the class may be a proxy
        // (b) when (a) is a class, it may be implementing lifecycle interfaces which could
        // could be hidden under another proxy
        //

        //verifyServiceIsaInterface( name, clazz );
        //verifyServiceNotALifecycle( name, clazz );
    }

    /**
     * Verify that the implementation class does not
     * implement incompatible lifecycle interfaces.
     *
     * @param name the name of component
     * @param implementation the implementation class
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyLifecycles( final String name,
                                  final Class implementation )
        throws VerifyException
    {
        final boolean composable =
            Composable.class.isAssignableFrom( implementation ) 
            || Recomposable.class.isAssignableFrom( implementation );
        final boolean serviceable = Serviceable.class.isAssignableFrom( implementation );
        if( serviceable && composable )
        {
            final String message =
                REZ.getString( "verifier.incompat-serviceable.error",
                               name,
                               implementation.getName() );
            throw new VerifyException( message );
        }
    }

    /**
     * Verify that the service implemented by
     * specified component is an interface.
     *
     * @param name the name of component
     * @param clazz the class representign service
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyServiceIsaInterface( final String name,
                                           final Class clazz )
        throws VerifyException
    {
        if( !clazz.isInterface() )
        {
            final String message =
                REZ.getString( "verifier.non-interface-service.error",
                               name,
                               clazz.getName() );
            throw new VerifyException( message );
        }
    }

    /**
     * Verify that the service implemented by
     * specified component is public.
     *
     * @param name the name of component
     * @param clazz the class representign service
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyServiceIsPublic( final String name,
                                       final Class clazz )
        throws VerifyException
    {
        final boolean isPublic =
            Modifier.isPublic( clazz.getModifiers() );
        if( !isPublic )
        {
            final String message =
                REZ.getString( "verifier.non-public-service.error",
                               name,
                               clazz.getName() );
            throw new VerifyException( message );
        }
    }

    /**
     * Verify that the service implemented by
     * specified component does not extend any lifecycle interfaces.
     *
     * @param name the name of component
     * @param clazz the class representign service
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyServiceNotALifecycle( final String name,
                                            final Class clazz )
        throws VerifyException
    {
        for( int i = 0; i < FRAMEWORK_CLASSES.length; i++ )
        {
            final Class lifecycle = FRAMEWORK_CLASSES[ i ];
            if( lifecycle.isAssignableFrom( clazz ) )
            {
                final String message =
                    REZ.getString( "verifier.service-isa-lifecycle.error",
                                   name,
                                   clazz.getName(),
                                   lifecycle.getName() );
                throw new VerifyException( message );
            }
        }
    }

    /**
     * Verify that the component has a no-arg aka default
     * constructor.
     *
     * @param name the name of component
     * @param clazz the class representign component
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyPublicConstructor( final String name,
                                        final Class clazz )
        throws VerifyException
    {
        final Constructor[] ctors = clazz.getConstructors();
        if( ctors.length < 1 )
        {
            final String message =
              REZ.getString( "verifier.non-public-ctor.error",
                name,
                clazz.getName() );
            throw new VerifyException( message );
        }

        /*
        try
        {
            final Constructor ctor = clazz.getConstructor( EMPTY_TYPES );
            if( !Modifier.isPublic( ctor.getModifiers() ) )
            {
                final String message =
                    REZ.getString( "verifier.non-public-ctor.error",
                                   name,
                                   clazz.getName() );
                throw new VerifyException( message );
            }
        }
        catch( final NoSuchMethodException nsme )
        {
            final String message =
                REZ.getString( "verifier.missing-noargs-ctor.error",
                               name,
                               clazz.getName() );
            throw new VerifyException( message );
        }
        */
    }

    /**
     * Verify that the component is not represented by
     * abstract class.
     *
     * @param name the name of component
     * @param clazz the class representign component
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyNonAbstract( final String name,
                                   final Class clazz )
        throws VerifyException
    {
        final boolean isAbstract =
            Modifier.isAbstract( clazz.getModifiers() );
        if( isAbstract )
        {
            final String message =
                REZ.getString( "verifier.abstract-class.error",
                               name,
                               clazz.getName() );
            throw new VerifyException( message );
        }
    }

    /**
     * Verify that the component is not represented by
     * abstract class.
     *
     * @param name the name of component
     * @param clazz the class representign component
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyPublic( final String name,
                              final Class clazz )
        throws VerifyException
    {
        final boolean isPublic =
            Modifier.isPublic( clazz.getModifiers() );
        if( !isPublic )
        {
            final String message =
                REZ.getString( "verifier.nonpublic-class.error",
                               name,
                               clazz.getName() );
            throw new VerifyException( message );
        }
    }

    /**
     * Verify that the component is not represented by
     * primitive class.
     *
     * @param name the name of component
     * @param clazz the class representign component
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyNonPrimitive( final String name,
                                    final Class clazz )
        throws VerifyException
    {
        if( clazz.isPrimitive() )
        {
            final String message =
                REZ.getString( "verifier.primitive-class.error",
                               name,
                               clazz.getName() );
            throw new VerifyException( message );
        }
    }

    /**
     * Verify that the component is not represented by
     * interface class.
     *
     * @param name the name of component
     * @param clazz the class representign component
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyNonInterface( final String name,
                                    final Class clazz )
        throws VerifyException
    {
        if( clazz.isInterface() )
        {
            final String message =
                REZ.getString( "verifier.interface-class.error",
                               name,
                               clazz.getName() );
            throw new VerifyException( message );
        }
    }

    /**
     * Verify that the component is not represented by
     * an array class.
     *
     * @param name the name of component
     * @param clazz the class representign component
     * @throws VerifyException if error thrown on failure and
     *         component fails check
     */
    public void verifyNonArray( final String name,
                                final Class clazz )
        throws VerifyException
    {
        if( clazz.isArray() )
        {
            final String message =
                REZ.getString( "verifier.array-class.error",
                               name,
                               clazz.getName() );
            throw new VerifyException( message );
        }
    }
}
