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

package org.apache.avalon.activation.impl;

import org.apache.avalon.activation.ComponentFactory;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.LifestyleFactory;
import org.apache.avalon.composition.provider.LifestyleManager;

import org.apache.avalon.meta.info.InfoDescriptor;


/**
 * A factory enabling the establishment of runtime handlers.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultLifestyleFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultLifestyleFactory implements LifestyleFactory
{
    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final SystemContext m_system;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Creation of a new default lifestyle factory.
    * @param system the system context
    */
    public DefaultLifestyleFactory( SystemContext system )
    {
        m_system = system;
    }

    //-------------------------------------------------------------------
    // LifestyleFactory
    //-------------------------------------------------------------------

   /**
    * Create a new lifestyle manager.
    * @param model the component model
    * @return the lifestyle manager
    */
    public LifestyleManager createLifestyleManager( ComponentModel model )
    {
        ComponentFactory factory = 
          new DefaultComponentFactory( m_system, model );
        return createLifestyleManager( model, factory );
    }

   /**
    * Create a new lifestyle manager.
    * @param model the component model
    * @param factory the component factory
    * @return the lifestyle manager
    */
    protected LifestyleManager createLifestyleManager( 
      ComponentModel model, ComponentFactory factory )
    {
        final String lifestyle = 
          model.getType().getInfo().getLifestyle();

        if( lifestyle.equals( InfoDescriptor.SINGLETON ) )
        {
            return new SingletonLifestyleManager( model, factory );
        }
        else if( lifestyle.equals( InfoDescriptor.THREAD ) )
        {
            return new ThreadLifestyleManager( model, factory );
        }
        else if( lifestyle.equals( InfoDescriptor.TRANSIENT ) )
        {
            return new TransientLifestyleManager( model, factory );
        }
        else
        {
            //
            // TODO
            // check if the key is an artifact reference and if 
            // so, try to load up a lifestyle factory and delegate the 
            // request
            //

            final String error = 
              "Unsupported lifestyle [" + lifestyle + "].";
            throw new IllegalArgumentException( error );
        }
    }

    private ComponentFactory createComponentFactory( ComponentModel model )
    {
        //
        // TODO
        // check for a custom component factory artifact reference
        // and load via avalon-repository if non null
        //

        return new DefaultComponentFactory( m_system, model );
    }
}
