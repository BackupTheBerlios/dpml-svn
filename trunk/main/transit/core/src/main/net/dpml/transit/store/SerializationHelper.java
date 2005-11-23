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

package net.dpml.transit.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.PasswordAuthentication;

import net.dpml.transit.Value;

/**
 * Helper class used to convert credentials to and from byte arrays.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class SerializationHelper
{
   /**
    * Internal constructor.
    */
    private SerializationHelper()
    {
        // static utility class
    }

   /**
    * Utility operation to convert a password authentication instance to a byte array.
    * @param auth the password authentication
    * @return the byte array
    */
    public static byte[] exportCredentials( PasswordAuthentication auth ) throws StorageRuntimeException
    {
        CrendentialsHolder holder = new CrendentialsHolder( auth );
        return toByteArray( holder );
    }

   /**
    * Utility operation to convert a parameters array to a byte array.
    * @param params the parameters to export
    * @return the byte array
    */
    public static byte[] exportParameters( Value[] params ) throws StorageRuntimeException
    {
        ParametersHolder holder = new ParametersHolder( params );
        return toByteArray( holder );
    }

   /**
    * Utility operation to convert a byte array to a password authentication instance.
    * @param the byte array
    * @return auth the password authentication
    */
    public static PasswordAuthentication importCredentials( byte[] bytes ) throws StorageRuntimeException
    {
        CrendentialsHolder holder = (CrendentialsHolder) importObject( bytes );
        return holder.getPasswordAuthentication();
    }

   /**
    * Utility operation to convert a byte array to a parameter array.
    * @param the byte array
    * @return the parameters array
    */
    public static Value[] importParameters( byte[] bytes ) throws StorageRuntimeException
    {
        ParametersHolder holder = (ParametersHolder) importObject( bytes );
        return holder.getParameters();
    }

   /**
    * Utility operation to convert a byte array to a password authentication instance.
    * @param the byte array
    * @return auth the password authentication
    */
    public static Object importObject( byte[] bytes ) throws StorageRuntimeException
    {
        try
        {
            ByteArrayInputStream input = new ByteArrayInputStream( bytes );
            ObjectInputStream stream = new ObjectInputStream( input );
            return stream.readObject();
        }
        catch( Throwable e )
        {
            final String error = 
             "Error while attempting to load byte array input stream.";
            throw new StorageRuntimeException( error, e );
        }
    }

   /**
    * Utility operation to convert a serialized object to a a byte array
    * @param the object the serializable object
    * @return the byte array
    */
    public static byte[] export( Serializable object ) throws StorageRuntimeException
    {
        return toByteArray( object );
    }

   /**
    * Utility operation to convert a serialized object to a a byte array
    * @param the object the serializable object
    * @return the byte array
    */
    public static byte[] toByteArray( Serializable object ) throws StorageRuntimeException
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
            throw new StorageRuntimeException( error, e );
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
  
   /**
    * Serializable parameters holder class.
    */
    private static class ParametersHolder implements Serializable
    {
        static final long serialVersionUID = 1L;

        private final Value[] m_params;

        ParametersHolder( Value[] params )
        {
            m_params = params;
        }

        public Value[] getParameters()
        {
            return m_params;
        }

        public boolean equals( Object other )
        {
            if( null == other ) 
            {
                return false;
            }
            if( !( other instanceof ParametersHolder ) )
            {
                return false;
            }
            ParametersHolder holder = (ParametersHolder) other;
            if( m_params.length != holder.m_params.length )
            {
                return false;
            }
            for( int i=0; i < m_params.length; i++ )
            {
                if( !m_params[i].equals( holder.m_params[i] ) )
                {
                    return false;
                }
            }
            return true;
        }

        public int hashCode()
        {
            int hash = getClass().hashCode();
            for( int i=0; i < m_params.length; i++ )
            {
                hash ^= m_params[i].hashCode();
            }
            return hash;
        }
    }


   /**
    * Serializable credentials holder class.
    */
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
            if( !( other instanceof CrendentialsHolder ) )
            {
                return false;
            }
            CrendentialsHolder holder = (CrendentialsHolder) other;
            if( !equals( m_username, holder.m_username ) )
            {
                return false;
            }
            else
            {
                return equals( m_password , holder.m_password );
            }
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
                for( int i=0; i < s1.length; i++ )
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
