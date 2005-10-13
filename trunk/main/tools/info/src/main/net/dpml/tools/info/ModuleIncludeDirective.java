/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.info;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;
import java.util.Properties;

import net.dpml.transit.util.Enum;

/**
 * The IncludeDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ModuleIncludeDirective extends IncludeDirective
{
    public static final Mode URI = Mode.URI;
    public static final Mode FILE = Mode.FILE;
    
    private Mode m_mode;
    
    public ModuleIncludeDirective( Mode mode, String value )
    {
        this( mode, value, null );
    }
    
    public ModuleIncludeDirective( Mode mode, String value, Properties properties )
    {
        super( mode.getName(), value, properties );
        m_mode = mode;
    }
    
    public Mode getMode()
    {
        return m_mode;
    }
    
   /**
    * Mode of inclusion.
    * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
    */
    public static final class Mode extends Enum
    {
        static final long serialVersionUID = 1L;

        /**
        * Include File.
        */
        public static final Mode FILE = new Mode( "file" );

        /**
        * Soft collection policy.
        */
        public static final Mode URI = new Mode( "uri" );
    
        /**
         * Internal constructor.
        * @param label the enumeration label.
        * @param index the enumeration index.
        * @param map the set of constructed enumerations.
        */
        private Mode( String label )
        {
            super( label );
        }
    
        public static Mode parse( String value )
        {
            if( value.equalsIgnoreCase( "file" ) )
            {
                return FILE;
            }
            else if( value.equalsIgnoreCase( "uri" ))
            {
                return URI;
            }
            else
            {
                final String error =
                  "Unrecognized module mode argument [" + value + "]";
                throw new IllegalArgumentException( error );
            }
        }
    }

    public static final class ModeBeanInfo extends SimpleBeanInfo
    {
        private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();

        public BeanDescriptor getBeanDescriptor()
        {
            return BEAN_DESCRIPTOR;
        }
    
        private static BeanDescriptor setupBeanDescriptor()
        {
            BeanDescriptor descriptor = new BeanDescriptor( Mode.class );
            descriptor.setValue( 
              "persistenceDelegate", 
            new ModePersistenceDelegate() );
            return descriptor;
        }
    
        private static class ModePersistenceDelegate extends DefaultPersistenceDelegate
        {
            public Expression instantiate( Object old, Encoder encoder )
            {
                Mode mode = (Mode) old;
                return new Expression( Mode.class, "parse", new Object[]{ mode.getName() } );
            }
        }
    }
}
