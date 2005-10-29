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

import java.util.Arrays;
import java.util.ArrayList;
import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;
import java.util.Properties;

import net.dpml.transit.util.Enum;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ResourceDirective extends AbstractDirective
{
    public static final Classifier EXTERNAL = Classifier.EXTERNAL;
    public static final Classifier LOCAL = Classifier.LOCAL;
     
    private final String m_name;
    private final String m_version;
    private final String m_basedir;
    private final TypeDirective[] m_types;
    private final DependencyDirective[] m_dependencies;
    private final Classifier m_classifier;
    
    public ResourceDirective( 
      String name, String version, Classifier classifier, String basedir, TypeDirective[] types, 
      DependencyDirective[] dependencies, Properties properties )
    {
        super( properties );
        
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == types )
        {
            throw new NullPointerException( "types" );
        }
        if( null == dependencies )
        {
            throw new NullPointerException( "dependencies" );
        }
        if( null == classifier )
        {
            throw new NullPointerException( "classifier" );
        }
        m_name = name;
        m_version = version;
        m_types = types;
        m_dependencies = dependencies;
        m_basedir = basedir;
        m_classifier = classifier;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public String getVersion()
    {
        return m_version;
    }
    
    public String getBasedir()
    {
        return m_basedir;
    }
    
    public Classifier getClassifier()
    {
        return m_classifier;
    }
    
    public boolean isLocal()
    {
        return LOCAL.equals( m_classifier );
    }
    
    public TypeDirective[] getTypeDirectives()
    {
        return m_types;
    }
        
    public TypeDirective getTypeDirective( String name ) throws TypeUnknownException
    {
        for( int i=0; i<m_types.length; i++ )
        {
            TypeDirective type = m_types[i];
            if( name.equals( type.getName() ) )
            {
                return type;
            }
        }
        throw new TypeUnknownException( name );
    }
        
    public DependencyDirective[] getDependencyDirectives()
    {
        return m_dependencies;
    }
    
    public DependencyDirective getDependencyDirective( Scope scope )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<m_dependencies.length; i++ )
        {
            DependencyDirective directive = m_dependencies[i];
            if( scope.equals( directive.getScope() ) )
            {
                return directive;
            }
        }
        return new DependencyDirective( scope, new IncludeDirective[0] );
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ResourceDirective ) )
        {
            ResourceDirective object = (ResourceDirective) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( !equals( m_version, object.m_version ) )
            {
                return false;
            }
            else if( !equals( m_basedir, object.m_basedir ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_types, object.m_types ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_dependencies, object.m_dependencies ) )
            {
                return false;
            }
            else
            {
                return true;
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
        hash ^= super.hashValue( m_name );
        hash ^= super.hashValue( m_version );
        hash ^= super.hashValue( m_basedir );
        hash ^= super.hashArray( m_types );
        hash ^= super.hashArray( m_dependencies );
        return hash;
    }

   /**
    * Resource classifier enumeration.
    * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
    */
    public static final class Classifier extends Enum
    {
        static final long serialVersionUID = 1L;

        /**
        * External resource.
        */
        public static final Classifier EXTERNAL = new Classifier( "external" );

        /**
        * Local resources.
        */
        public static final Classifier LOCAL = new Classifier( "local" );
    
        /**
         * Internal constructor.
        * @param label the enumeration label.
        */
        private Classifier( String label )
        {
            super( label );
        }
    
        public static Classifier parse( String value )
        {
            if( value.equalsIgnoreCase( "external" ) )
            {
                return EXTERNAL;
            }
            else if( value.equalsIgnoreCase( "local" ))
            {
                return LOCAL;
            }
            else
            {
                final String error =
                  "Unrecognized module classifier argument [" + value + "]";
                throw new IllegalArgumentException( error );
            }
        }
    }

    public static final class ClassifierBeanInfo extends SimpleBeanInfo
    {
        private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();

        public BeanDescriptor getBeanDescriptor()
        {
            return BEAN_DESCRIPTOR;
        }
    
        private static BeanDescriptor setupBeanDescriptor()
        {
            BeanDescriptor descriptor = new BeanDescriptor( Classifier.class );
            descriptor.setValue( 
              "persistenceDelegate", 
            new ClassifierPersistenceDelegate() );
            return descriptor;
        }
    
        private static class ClassifierPersistenceDelegate extends DefaultPersistenceDelegate
        {
            public Expression instantiate( Object old, Encoder encoder )
            {
                Classifier classifier = (Classifier) old;
                return new Expression( Classifier.class, "parse", new Object[]{ classifier.getName() } );
            }
        }
    }
}
