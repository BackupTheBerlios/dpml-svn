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

package org.apache.avalon.meta.info.test;

import junit.framework.TestCase;
import org.apache.avalon.meta.info.*;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.parameters.Parameters;

import java.util.Properties;
import java.io.*;

/**
 * TypeTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TypeTestCase.java 31046 2004-07-31 17:20:26Z niclas $
 */
public class TypeTestCase extends TestCase
{
    private InfoDescriptor m_descriptor;
    private SecurityDescriptor m_security;
    private CategoryDescriptor[] m_loggers;
    private ContextDescriptor m_context;
    private ServiceDescriptor[] m_services;
    private DependencyDescriptor[] m_dependencies;
    private StageDescriptor[] m_stages;
    private ExtensionDescriptor[] m_extensions;
    private Configuration m_defaults;
    private Parameters m_parameters;
    private ReferenceDescriptor m_reference;
    private String m_key;

    public TypeTestCase( String name )
    {
        super( name );
    }

    public void setUp()
    {
        m_reference = new ReferenceDescriptor(TypeTestCase.class.getName());
        m_key = TypeTestCase.class.getName();
        m_descriptor = createSimpleInfo(TypeTestCase.class.getName());
        m_security = new SecurityDescriptor( null, null );
        m_loggers = new CategoryDescriptor[] {
            new CategoryDescriptor("name", new Properties())
        };
        m_context = new ContextDescriptor( 
          TypeTestCase.class.getName(), new EntryDescriptor[0]);
        m_services = new ServiceDescriptor[] {
            new ServiceDescriptor(m_reference)
        };
        m_dependencies = new DependencyDescriptor[] {
            new DependencyDescriptor("role", m_reference)
        };
        m_stages = new StageDescriptor[] {
            new StageDescriptor( m_key )
        };
        m_extensions = new ExtensionDescriptor[] {
            new ExtensionDescriptor( m_key )
        };
        m_defaults = new DefaultConfiguration("default");
        m_parameters = Parameters.EMPTY_PARAMETERS;
    }

    private void checkType(Type type)
    {
        assertNotNull(type);
        checkArray(m_loggers, type.getCategories());
        assertEquals( m_security, type.getSecurity() );
        assertEquals( m_defaults, type.getConfiguration() );
        assertEquals( m_parameters , type.getParameters() );
        assertEquals( m_context, type.getContext());
        checkArray(m_dependencies, type.getDependencies());
        assertEquals(m_dependencies[0], type.getDependency(m_dependencies[0].getKey()));
        assertEquals(m_extensions[0], type.getExtension( m_stages[0].getKey() ) );
        checkArray(m_extensions, type.getExtensions());
        assertEquals( m_descriptor, type.getInfo() );
        assertEquals( m_services[0], type.getService(m_reference));
        assertEquals( m_services[0], type.getService( m_services[0].getReference().getClassname()));
        checkArray(m_services, type.getServices());
        checkArray(m_stages, type.getStages());
        assertTrue(type.isaCategory(m_loggers[0].getName()));
        assertTrue( !type.isaCategory( "fake name" ) );
    }

    private void checkArray( Object[] orig, Object[] other )
    {
        assertEquals(orig.length, other.length);
        for (int i = 0; i < orig.length; i++)
        {
            assertEquals( orig[i], other[i] );
        }
    }

    public void testType()
    {
        Type type = 
          new Type(
            m_descriptor, m_security, m_loggers, m_context, m_services, m_dependencies, 
            m_stages, m_extensions, m_defaults, m_parameters );
        checkType(type);
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        Type type = 
          new Type( 
            m_descriptor, m_security, m_loggers, m_context, m_services, m_dependencies, 
            m_stages, m_extensions, m_defaults, m_parameters );

        checkType( type );

        File file = new File( "test.out" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( type );
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        Type serialized = (Type) ois.readObject();
        ois.close();
        file.delete();

        checkType( serialized );

        assertEquals( "equality", type, serialized );
        assertEquals( "hashcode", type.hashCode(), serialized.hashCode() );

    }

    private static InfoDescriptor createSimpleInfo( String classname )
    {
        return new InfoDescriptor( null, classname, null, null, null, null, null );
    }
 }
