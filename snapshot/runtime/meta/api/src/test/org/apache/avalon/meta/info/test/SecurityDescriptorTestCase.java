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

import org.apache.avalon.meta.info.PermissionDescriptor;
import org.apache.avalon.meta.info.SecurityDescriptor;
import org.apache.avalon.meta.info.Descriptor;

import java.io.*;

/**
 * EntryDescriptorTestCase does XYZ
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version $Id: SecurityDescriptorTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class SecurityDescriptorTestCase extends AbstractDescriptorTestCase
{
    private static final PermissionDescriptor m_permission = 
      new PermissionDescriptor( "java.io.FilePermission", "${avalon.dir}", new String[]{"read,write"} );
    private static final PermissionDescriptor[] m_permissions = 
      new PermissionDescriptor[]{ m_permission };

    public SecurityDescriptorTestCase( String name )
    {
        super( name );
    }

    protected Descriptor getDescriptor()
    {
        return new SecurityDescriptor( m_permissions, super.getProperties() );
    }


    public void testConstructor()
    {
        try
        {
            SecurityDescriptor d = new SecurityDescriptor( null, null );
        }
        catch( Throwable npe )
        {
            fail( "unexpected error/1" );
        }
    }

    public void testConstructor2()
    {
        try
        {
            SecurityDescriptor d = new SecurityDescriptor( m_permissions, null );
        }
        catch( Throwable npe )
        {
            fail( "unexpected error/2" );
        }
    }

    public void testConstructor3()
    {
        try
        {
            SecurityDescriptor d = new SecurityDescriptor( null, super.getProperties() );
        }
        catch( Throwable npe )
        {
            fail( "unexpected error/3" );
        }
    }

    public void testSecurity()
    {
        SecurityDescriptor security = new SecurityDescriptor( m_permissions, super.getProperties() );
        checkDescriptor( security );
    }


    public void testSerialization() throws IOException, ClassNotFoundException
    {
        SecurityDescriptor s = new SecurityDescriptor ( m_permissions, super.getProperties() );

        File file = new File( "test.out" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( s );
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        SecurityDescriptor serialized = (SecurityDescriptor) ois.readObject();
        ois.close();
        file.delete();

        checkDescriptor( serialized );
        assertEquals( "serialization-equivalent", s, serialized );
        assertEquals( "hash-code-equivalent", s.hashCode(), serialized.hashCode() );
    }

    protected void checkDescriptor( SecurityDescriptor desc )
    {
        super.checkDescriptor( desc );
        PermissionDescriptor[] perms = desc.getPermissions();
        assertEquals( "equal-permissison-length", perms.length, m_permissions.length );
        for( int i=0; i<perms.length; i++ )
        {   
            PermissionDescriptor p = perms[i];
            assertEquals( "permission-" + i, p, m_permissions[i] );
        }
    }
}
