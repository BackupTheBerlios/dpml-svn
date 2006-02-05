/* 
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.info.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Properties;

import net.dpml.metro.info.CategoryDescriptor;
import net.dpml.metro.info.ContextDescriptor;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.InfoDescriptor;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.ThreadSafePolicy;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.ServiceDescriptor;
import net.dpml.metro.info.Priority;

import net.dpml.state.State;

/**
 * TypeTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TypeTestCase extends AbstractEncodingTestCase
{
    private InfoDescriptor m_info;
    private CategoryDescriptor[] m_loggers;
    private ContextDescriptor m_context;
    private ServiceDescriptor[] m_services;
    private PartReference[] m_parts;
    private ServiceDescriptor m_reference;
    private String m_key;
    private State m_graph;

   /**
    * Testcase setup.
    */
    public void setUp()
    {
        m_reference = new ServiceDescriptor( TypeTestCase.class.getName() );
        m_key = TypeTestCase.class.getName();
        m_info = createSimpleInfo( TypeTestCase.class.getName() );
        m_loggers = 
          new CategoryDescriptor[]{
            new CategoryDescriptor( "name", Priority.INFO, new Properties() )
          };
        m_context = new ContextDescriptor( new EntryDescriptor[0] );
        m_services = new ServiceDescriptor[]{m_reference};
        m_parts = new PartReference[0];
        m_graph = State.NULL_STATE;
    }

    private void checkType( Type type )
    {
        assertNotNull( type );
        checkArray( m_loggers, type.getCategoryDescriptors() );
        assertEquals( m_context, type.getContextDescriptor() );
        assertEquals( m_info, type.getInfo() );
        assertEquals( m_graph, type.getStateGraph() );
        assertEquals( m_services[0], type.getServiceDescriptor( m_reference ) );
        assertEquals( m_services[0], type.getServiceDescriptor( m_services[0].getClassname() ) );
        checkArray( m_services, type.getServiceDescriptors() );
        checkArray( m_parts, type.getPartReferences() );
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

   /**
    * Validate the type.
    */
    public void testType()
    {
        Type type = 
          new Type(
            m_info, m_loggers, m_context, m_services, m_parts, m_graph );
        checkType( type );
    }

   /**
    * Test serialization.
    * @exception IOException if an IO error occurs
    * @exception ClassNotFoundException if a class is not found
    */
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        Type type = 
          new Type( 
            m_info, m_loggers, m_context, m_services, m_parts, m_graph );
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
        return new InfoDescriptor( null, classname, null, null, CollectionPolicy.WEAK, ThreadSafePolicy.FALSE, null );
    }
    
   /**
    * Test encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        Type type = 
          new Type(
            m_info, m_loggers, m_context, m_services, m_parts, m_graph );
        executeEncodingTest( type, "type.xml" );
    }
}
