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

package org.apache.metro.facilities.reflector.typehandlers;

import java.util.TreeSet;

import org.apache.avalon.composition.model.ComponentModel;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.metro.facilities.reflector.ReflectionException;

import org.apache.metro.facilities.reflector.spi.ReflectorProvider;
import org.apache.metro.facilities.reflector.spi.TypeHandler;

/**
 * @avalon.component name="type-handler-componentmodel" lifestyle="singleton"
 * @avalon.service type="org.apache.metro.facilities.reflector.spi.TypeHandler"
 */
public class ComponentModelTypeHandler extends AbstractObjectTypeHandler
    implements Serviceable
{
    /**
     * @avalon.dependency type="org.apache.metro.facilities.reflector.spi.ReflectorProvider"
     *                    key="provider"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        ReflectorProvider provider = (ReflectorProvider) man.lookup( "provider" );
        provider.addTypeHandler( this, ComponentModel.class );
    }
    
    public boolean isDefault()
    {
        return false;
    }

    public String[] getNames( Object container )
        throws ReflectionException
    {
        if( ! ( container instanceof ComponentModel ) )
            throw new ReflectionException( container + " is not a ComponentModel: " + container.getClass().getName() );
        TreeSet names = new TreeSet();
        names.add( "Implementation" );
        getNames( names, container );
        return packageNames( names );
    }
    
    public Object getMemberObject( Object container, String membername )
        throws ReflectionException
    {
        if( ! ( container instanceof ComponentModel ) )
            throw new ReflectionException( container + " is not a ComponentModel: " + container.getClass().getName() );
        if( membername.equals( "Implementation" ) )
        {
            try
            {
                ComponentModel model = (ComponentModel) container;
                Object instance = model.resolve( false );
                return instance;
            } catch( Exception e )
            {
                throw new ReflectionException( "Unable to obtain component instance.", e );
            }
        }
        return super.getMemberObject( container, membername );
    }
    
    public void setMemberObject( Object container, String membername, Object value )
        throws ReflectionException
    {
        if( ! ( container instanceof ComponentModel ) )
            throw new ReflectionException( container + " is not a ComponentModel: " + container.getClass().getName() );
        if( membername.equals( "Implementation" ) )
        {
            // Not supported.
            return;
        }
        super.setMemberObject( container, membername, value );
    }    

    public Class getClass( Object container, String membername )
        throws ReflectionException
    {
        if( ! ( container instanceof ComponentModel ) )
            throw new ReflectionException( container + " is not a ComponentModel: " + container.getClass().getName() );
        if( membername.equals( "Implementation" ) )
        {
            try
            {
                ComponentModel model = (ComponentModel) container;
                Object instance = model.resolve( false );
                return instance.getClass();
            } catch( Exception e )
            {
                throw new ReflectionException( "Unable to obtain component instance.", e );
            }
        }
        return super.getClass( container, membername );
    }

    public boolean isSettable( Object container, String membername )
        throws ReflectionException
    {
        if( ! ( container instanceof ComponentModel ) )
            throw new ReflectionException( container + " is not a ComponentModel: " + container.getClass().getName() );
        if( membername.equals( "Implementation" ) )
        {
            return false;
        }
        return super.isSettable( container, membername );
    }
} 
