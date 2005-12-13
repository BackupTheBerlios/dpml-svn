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

package net.dpml.metro.data.test;

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

import net.dpml.metro.info.PartReference;

import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.data.ComponentDirective;

/**
 * EntryDescriptorTestCase does XYZ
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractEncodingTestCase extends TestCase
{
    public Object executeEncodingTest( Object object, String filename ) throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File destination = new File( test, filename );
        FileOutputStream output = new FileOutputStream( destination );
        BufferedOutputStream buffer = new BufferedOutputStream( output );
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( ComponentDirective.class.getClassLoader() );
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
        try
        {
            encoder.writeObject( object );
        }
        finally
        {
            encoder.close();
        }
        
        FileInputStream input = new FileInputStream( destination );
        try
        {
            XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
            Object result = decoder.readObject();
            assertEquals( "encoding-equality", object, result );
            return result;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( current );
        }
    }

    public static class URIPersistenceDelegate extends DefaultPersistenceDelegate
    {
        public Expression instantiate( Object old, Encoder encoder )
        {
            URI uri = (URI) old;
            String spec = uri.toString();
            Object[] args = new Object[]{ spec };
            return new Expression( old, old.getClass(), "new", args );
        }
    }

}
