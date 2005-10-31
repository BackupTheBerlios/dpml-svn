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

import net.dpml.transit.Category;
import net.dpml.transit.util.Enum;

/**
 * The IncludeDirective class describes a dependency on a named resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class IncludeDirective extends AbstractDirective
{
    public static final Mode KEY = Mode.KEY;
    public static final Mode REF = Mode.REF;
    
    private final Mode m_mode;
    private final String m_value;
    private final Category m_category;
    
    public IncludeDirective( Mode mode, Category category, String value, Properties properties )
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
        if( null == category )
        {
            m_category = Category.PUBLIC;
        }
        else
        {
            m_category = category;
        }
        m_mode = mode;
        m_value = value;
    }
    
    public Mode getMode()
    {
        return m_mode;
    }
    
    public Category getCategory()
    {
        return m_category;
    }

    public String getValue()
    {
        return m_value;
    }

    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof IncludeDirective ) )
        {
            IncludeDirective include = (IncludeDirective) other;
            if( !equals( m_mode, include.m_mode ) )
            {
                return false;
            }
            if( !equals( m_category, include.m_category ) )
            {
                return false;
            }
            else
            {
                return equals( m_value, include.m_value );
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
        hash ^= super.hashValue( m_category );
        hash ^= super.hashValue( m_value );
        return hash;
    }
    
    public String toString()
    {
        return "include:" + m_mode + ":" + m_category + ":" + m_value;
    }
    
   /**
    * Mode of inclusion.
    * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
    */
    public static final class Mode extends Enum
    {
        static final long serialVersionUID = 1L;

       /**
        * Include by reference to a local key.
        */
        public static final Mode KEY = new Mode( "key" );

       /**
        * Include by reference to an absolute resource address.
        */
        public static final Mode REF = new Mode( "ref" );
    
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
            if( value.equalsIgnoreCase( "key" ) )
            {
                return KEY;
            }
            else if( value.equalsIgnoreCase( "ref" ))
            {
                return REF;
            }
            else
            {
                final String error =
                  "Unrecognized resource mode argument [" + value + "]";
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
