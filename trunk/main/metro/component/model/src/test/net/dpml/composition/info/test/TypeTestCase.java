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

package net.dpml.composition.info.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.composition.info.CategoryDescriptor;
import net.dpml.composition.info.ContextDescriptor;
import net.dpml.composition.info.EntryDescriptor;
import net.dpml.composition.info.InfoDescriptor;
import net.dpml.composition.info.CollectionPolicy;
import net.dpml.composition.info.Type;
import net.dpml.composition.info.PartReference;
import net.dpml.composition.info.ServiceDescriptor;

import net.dpml.configuration.Configuration;

import net.dpml.composition.AbstractEncodingTestCase;

/**
 * TypeTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: TypeTestCase.java 2958 2005-07-03 08:11:07Z mcconnell@dpml.net $
 */
public class TypeTestCase extends AbstractEncodingTestCase
{
    private InfoDescriptor m_descriptor;
    private CategoryDescriptor[] m_loggers;
    private ContextDescriptor m_context;
    private ServiceDescriptor[] m_services;
    private PartReference[] m_parts;
    private ServiceDescriptor m_reference;
    private String m_key;

    public void setUp()
    {
        m_reference = new ServiceDescriptor( TypeTestCase.class.getName() );
        m_key = TypeTestCase.class.getName();
        m_descriptor = createSimpleInfo( TypeTestCase.class.getName() );
        m_loggers = new CategoryDescriptor[] {
            new CategoryDescriptor("name", new Properties())
        };
        m_context = new ContextDescriptor( new EntryDescriptor[0] );
        m_services = new ServiceDescriptor[] { m_reference };
        m_parts = new PartReference[0];
    }

    private void checkType( Type type )
    {
        assertNotNull( type );
        checkArray( m_loggers, type.getCategoryDescriptors() );
        assertEquals( m_context, type.getContextDescriptor() );
        assertEquals( m_descriptor, type.getInfo() );
        assertEquals( m_services[0], type.getServiceDescriptor( m_reference ) );
        assertEquals( m_services[0], type.getServiceDescriptor( m_services[0].getClassname() ) );
        checkArray( m_services, type.getServiceDescriptors());
        checkArray( m_parts, type.getPartReferences());
        assertTrue( type.isaCategory( m_loggers[0].getName() ) );
        assertTrue( !type.isaCategory( "fake name" ) );
    }

    private void checkArray( Object[] orig, Object[] other )
    {
        assertEquals( orig.length, other.length );
        for( int i = 0; i < orig.length; i++ )
        {
            assertEquals( orig[i], other[i] );
        }
    }

    public void testType()
    {
        Type type = 
          new Type(
            m_descriptor, m_loggers, m_context, m_services, 
            null, m_parts );
        checkType( type );
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        Type type = 
          new Type( 
            m_descriptor, m_loggers, m_context, m_services, null, m_parts );

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
        return new InfoDescriptor( null, classname, null, null, CollectionPolicy.WEAK, null, false, null );
    }
    
    public void testEncoding() throws Exception
    {
        Type type = 
          new Type(
            m_descriptor, m_loggers, m_context, m_services, 
            null, m_parts );
        executeEncodingTest( type, "type.xml" );
    }
}