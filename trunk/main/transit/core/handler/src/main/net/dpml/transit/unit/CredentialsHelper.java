/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.transit.unit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.PasswordAuthentication;

import net.dpml.transit.TransitException;


/**
 * Helper class used to convert credentials to and from byte arrays.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
class CredentialsHelper
{
    private CredentialsHelper()
    {
        // static utility class
    }

    public static byte[] exportCredentials( PasswordAuthentication auth ) throws BuilderException
    {
        CrendentialsHolder holder = new CrendentialsHolder( auth );
        return toByteArray( holder );
    }

    public static PasswordAuthentication importCredentials( byte[] bytes ) throws BuilderException
    {
        try
        {
            ByteArrayInputStream input = new ByteArrayInputStream( bytes );
            ObjectInputStream stream = new ObjectInputStream( input );
            CrendentialsHolder holder = (CrendentialsHolder) stream.readObject();
            return holder.getPasswordAuthentication();
        }
        catch( Throwable e )
        {
            final String error = 
             "Error while attempting to load credentials input stream.";
            throw new BuilderException( error, e );
        }
    }

    public static byte[] toByteArray( Serializable object ) throws BuilderException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream output = null;
        try
        {
            output = new ObjectOutputStream( stream );
            output.writeObject( object );
            return stream.toByteArray();
        }
        catch( Throwable e )
        {
            final String error = 
              "Error while attempting to write object to a byte array."
              + "\nclass: " + object.getClass().getName()
              + "\nreason: " + e.toString();
            throw new BuilderException( error, e );
        }
        finally
        {
            closeStream( output );
        }
    }

    private static void closeStream( OutputStream out )
    {
        if( null != out )
        {
            try
            {
                out.close();
            }
            catch( IOException ioe )
            {
                boolean ignoreMe = true;
            }
        }
    }

    private static class CrendentialsHolder implements Serializable
    {
        static final long serialVersionUID = 1L;

        private final String m_username;
        private final char[] m_password;

        CrendentialsHolder( PasswordAuthentication auth )
        {
            m_username = auth.getUserName();
            m_password = auth.getPassword();
        }

        public PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication( m_username, m_password );
        }

        public boolean equals( Object other )
        {
            if( null == other ) 
            {
                return false;
            }
            if( false == ( other instanceof CrendentialsHolder ) )
            {
                return false;
            }
            CrendentialsHolder holder = (CrendentialsHolder) other;
            if( false == equals( m_username, holder.m_username ) )
            {
                return false;
            }
            if( false == equals( m_password , holder.m_password ) )
            {
                return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = m_username.hashCode();
            hash ^= new String( m_password ).hashCode();
            return hash;
        }

        private boolean equals( String s1, String s2 )
        {
            if( null == s1 )
            {
                return null == s2;
            }
            else
            {
                return s1.equals( s2 );
            }
        }
        
        private boolean equals( char[] s1, char[] s2 )
        {
            if( null == s1 )
            {
                return null == s2;
            }
            else if( null == s2 )
            {
                return null == s1;
            }
            else if( s1.length != s2.length )
            {
                return false;
            }
            else
            {
                for( int i=0; i<s1.length; i++ )
                {
                    char c = s1[i];
                    if( c != s2[i] )
                    {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
