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
import org.apache.avalon.meta.info.PermissionDescriptor;

import java.io.*;

/**
 * EntryDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: PermissionDescriptorTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class PermissionDescriptorTestCase extends TestCase
{
    private static final String m_classname = FilePermission.class.getName();
    private static final String m_name = "${avalon.dir}";
    private static final String[] m_actions = new String[]{"read","write"};

    public PermissionDescriptorTestCase( String name )
    {
        super( name );
    }

    public void testConstructor()
    {
        try
        {
            PermissionDescriptor d = new PermissionDescriptor( null, null, null );
            fail( "did not throw an npe" );
        }
        catch( NullPointerException npe )
        {
            // ok
        }
    }

    public void testConstructor2()
    {
        try
        {
            PermissionDescriptor d = new PermissionDescriptor( m_classname, null, null );
        }
        catch( Throwable e )
        {
            fail( "unexpected error:  " + e.toString() );
        }
    }

    public void testConstructor3()
    {
        try
        {
            PermissionDescriptor d = new PermissionDescriptor( m_classname, m_name, null );
        }
        catch( Throwable e )
        {
            fail( "unexpected error/2: " + e.toString()  );
        }
    }

    public void testPermission()
    {
        PermissionDescriptor d = new PermissionDescriptor( m_classname, m_name, m_actions );
        check( d, m_classname, m_name, m_actions );
    }


    public void testSerialization() throws IOException, ClassNotFoundException
    {
        PermissionDescriptor p = new PermissionDescriptor( m_classname, m_name, m_actions );

        File file = new File( "test.out" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( p );
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        PermissionDescriptor serialized = (PermissionDescriptor) ois.readObject();
        ois.close();
        file.delete();

        check( serialized, m_classname, m_name, m_actions );

        assertEquals( p, serialized );
        assertEquals( p.hashCode(), p.hashCode() );
    }

    public void check( PermissionDescriptor p, String classname, String name, String[] actions )
    {
        assertEquals( "classname", p.getClassname(), classname );
        if( null == p.getName() )
        {
            assertNull( "name", name );
        }
        else
        {
            assertEquals( "name", p.getName(), name );
        }
        assertEquals( "name", p.getName(), name );

        if( null == actions )
        {
            assertEquals( "actions-zero", p.getActions().length, 0 );
        }
        else
        {
            String[] array = p.getActions();
            int j = array.length;
            assertEquals( "actions-length", actions.length, j );
            for( int i=0; i<j; i++ )
            {
                assertEquals( "action", array[i], actions[i] );
            }
        }
    }
}
