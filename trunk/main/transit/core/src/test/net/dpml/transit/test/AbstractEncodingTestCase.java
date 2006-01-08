/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.beans.Encoder;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.net.URI;

import junit.framework.TestCase;

/**
 * AbstractEncodingTestCase.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: EntryDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public abstract class AbstractEncodingTestCase extends TestCase
{
   /**
    * Utility operation to encode and decode an supplied object using an intermidiate file.
    * @param object the object to enciode
    * @param filename the intermidiate filename resolved relative to the target/test directory
    * @return the result of decoding the encoded structure
    * @exception Exception if an error occurs
    */
    public Object executeEncodingTest( Object object, String filename ) throws Exception
    {
        File test = new File( "target/test" );
        File destination = new File( test, filename );
        FileOutputStream output = new FileOutputStream( destination );
        BufferedOutputStream buffer = new BufferedOutputStream( output );
        XMLEncoder encoder = new XMLEncoder( buffer );
        encoder.setPersistenceDelegate( URI.class, new URIPersistenceDelegate() );
        encoder.setExceptionListener( 
          new ExceptionListener()
          {
            public void exceptionThrown( Exception e )
            {
                e.printStackTrace();
                fail( "encoding exception: " + e.toString() );
            }
          }
        );
        encoder.writeObject( object );
        encoder.close();
        
        FileInputStream input = new FileInputStream( destination );
        XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
        Object result = decoder.readObject();
        assertEquals( "encoding", object, result );
        return result;
    }

   /**
    * Internal persitance delage for the URI class.
    */
    public static class URIPersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Create an expression using an existing uri.
        * @param old the old uri
        * @param encoder the encoder
        * @return the expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            URI uri = (URI) old;
            String spec = uri.toString();
            Object[] args = new Object[]{ spec };
            return new Expression( old, old.getClass(), "new", args );
        }
    }

}
