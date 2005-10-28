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
 * The ImportDirective class describes a the import of resource via a file or uri reference.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ImportDirective extends AbstractDirective
{
    public static final Mode URI = Mode.URI;
    public static final Mode FILE = Mode.FILE;
    
    private Mode m_mode;
    private final String m_value;
    
    public ImportDirective( Mode mode, String value )
    {
        this( mode, value, null );
    }
    
    public ImportDirective( Mode mode, String value, Properties properties )
    {
        super( properties );
        
        if( null == mode )
        {
            throw new NullPointerException( "mode" );
        }
        if( null == value )
        {
            throw new NullPointerException( "value" );
        }

        m_mode = mode;
        m_value = value;
    }
    
    public Mode getMode()
    {
        return m_mode;
    }
    
    public String getValue()
    {
        return m_value;
    }

    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ImportDirective ) )
        {
            ImportDirective object = (ImportDirective) other;
            if( !equals( m_mode, object.m_mode ) )
            {
                return false;
            }
            else
            {
                return equals( m_value, object.m_value );
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_mode );
        hash ^= super.hashValue( m_value );
        return hash;
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
