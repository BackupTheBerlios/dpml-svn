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
package org.apache.avalon.jmx.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.composition.model.ComponentModel;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.jmx.ComponentRegistrationManager;
import org.apache.avalon.jmx.ComponentRegistrationException;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * This is an abstract implementation of a component to provide a lifecycle 
 * extension to export management interfaces for other components.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.3 $
 */
public abstract class AbstractComponentRegistrationManager 
    implements ComponentRegistrationManager
{

    private static final Resources REZ = ResourceManager.getPackageResources(
        AbstractComponentRegistrationManager.class );

    private Map m_entries = Collections.synchronizedMap( new HashMap() );

    private final Logger m_logger;

    public AbstractComponentRegistrationManager( final Logger logger )
      throws Exception
    {
        m_logger = logger;
    }

    public void register( ComponentModel componentModel ) 
        throws ComponentRegistrationException
    {
        final Class[] interfaces = getManagementInterfaces( componentModel );

        if ( null != interfaces && interfaces.length > 0 )
        {
            final String name = getName( componentModel );
            final Object component;
            try
            {
                component = componentModel.resolve( false );
            }
            catch ( Exception e )
            {
                final String message = REZ.getString( "manager.error.resolve.failure",
                                                      componentModel.getQualifiedName() );
                throw new ComponentRegistrationException( message, e );
            }
            verifyInterfaces( component, interfaces );
            doRegister( name, component, interfaces );
        }
    }

    public void unregister( ComponentModel componentModel ) 
        throws ComponentRegistrationException
    {
        final String name = getName( componentModel );
        final ManagedEntry entry = ( ManagedEntry ) m_entries.remove( name );

        if ( entry != null )
        {
            unexport( name, entry.getExportedObject() );
        }
    }

    protected abstract String getName( ComponentModel deploymentModel ) throws
        ComponentRegistrationException;

    protected abstract Class[] getManagementInterfaces( ComponentModel componentModel ) throws
        ComponentRegistrationException;

    /**
     * Export the object to the particular management medium using
     * the supplied object and interfaces.
     * This needs to be implemented by subclasses.
     *
     * @param name the name of object
     * @param object the object
     * @param interfaces the interfaces
     * @return the exported object
     * @throws ComponentRegistrationException if an error occurs
     */
    protected abstract Object export( String name, Object object, Class[] interfaces ) throws
        ComponentRegistrationException;

    /**
     * Stop the exported object from being managed.
     *
     * @param name the name of object
     * @param exportedObject the object return by export
     * @throws ComponentRegistrationException if an error occurs
     */
    protected abstract void unexport( String name, Object exportedObject ) throws
        ComponentRegistrationException;

    /**
     * Verify that an interface conforms to the requirements of management medium.
     *
     * @param clazz the interface class
     * @throws ComponentRegistrationException if verification fails
     */
    protected abstract void verifyInterface( Class clazz ) throws ComponentRegistrationException;

    /**
     * Verfify that name is well formed.
     *
     * @param name the name
     * @param object the object so named
     * @exception ComponentRegistrationException if the name is invalid
     */
    protected void verifyName( final String name, final Object object ) throws
        ComponentRegistrationException
    {
    }

    /**
     * Helper method to help check before an objects registration.
     * Verifies name and object are not null and verifies no entry exists using name.
     *
     * @param name the name of object
     * @param object the object to be registered
     * @throws ComponentRegistrationException if name already exists
     * @throws IllegalArgumentException if name or object is null
     */
    private void checkRegister( final String name, final Object object ) throws
        ComponentRegistrationException, IllegalArgumentException, NullPointerException
    {
        if ( null == object )
        {
            throw new NullPointerException( "object" );
        }

        if ( null == name )
        {
            throw new NullPointerException( "name" );
        }

        verifyName( name, object );

        if ( null != m_entries.get( name ) )
        {
            final String message = REZ.getString( "manager.error.register.exists", name );
            throw new ComponentRegistrationException( message );
        }
    }

    /**
     * Utility method that actually does the registration.
     *
     * @param name the name to register under
     * @param object the object
     * @param interfaces the interfaces (may be null)
     * @throws ComponentRegistrationException if error occurs
     */
    private void doRegister( final String name, final Object object, final Class[] interfaces ) throws
        ComponentRegistrationException
    {
        try
        {
            checkRegister( name, object );
        }
        catch ( IllegalArgumentException iae )
        {
            final String message = REZ.getString( "manager.error.register.failure", name );
            throw new ComponentRegistrationException( message, iae );
        }
        catch ( NullPointerException npe )
        {
            final String message = REZ.getString( "manager.error.register.failure", name );
            throw new ComponentRegistrationException( message, npe );
        }

        final Object exportedObject = export( name, object, interfaces );
        final ManagedEntry entry = new ManagedEntry( object, interfaces, exportedObject );
        m_entries.put( name, entry );
    }

    private void verifyInterfaces( final Object object, final Class[] interfaces ) throws
        ComponentRegistrationException
    {
        for ( int i = 0; i < interfaces.length; i++ )
        {
            final Class clazz = interfaces[i];

            if ( !clazz.isInterface() )
            {
                final String message = REZ.getString( "manager.error.verify.notinterface",
                                                      clazz.getName() );
                throw new ComponentRegistrationException( message );
            }

            if ( !clazz.isInstance( object ) )
            {
                final String message = REZ.getString( "manager.error.verify.notinstance",
                                                      clazz.getName() );
                throw new ComponentRegistrationException( message );
            }

            verifyInterface( clazz );
        }
    }

    protected Logger getLogger()
    {
        return m_logger;
    }

}
