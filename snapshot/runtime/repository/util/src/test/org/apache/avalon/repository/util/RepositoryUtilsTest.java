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

package org.apache.avalon.repository.util ;

import java.net.URL;
import java.util.Properties;

import javax.naming.directory.Attributes;

import junit.framework.TestCase;

import org.apache.avalon.repository.Artifact;


/**
 * @todo
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: RepositoryUtilsTest.java 43330 2004-09-04 10:26:58Z niclas $
 */
public class RepositoryUtilsTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RepositoryUtilsTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Constructor for RepositoryUtilsTest.
     * @param arg0
     */
    public RepositoryUtilsTest(String arg0)
    {
        super(arg0);
    }
    
    
    final public void testGetAsAttributes() throws Exception
    {
        /*
        Properties l_props = 
            RepositoryUtils.getProperties( 
              new URL( 
                "http://www.ibiblio.org/maven/avalon-repository/propertiess/repository.properties" ) ) ;
        Attributes l_attrs = RepositoryUtils.getAsAttributes( l_props ) ;
        assertEquals( ".repository", l_attrs.get( "cache.dir" ).get( 0 ) ) ; 

        assertEquals( "org.apache.avalon.repository.impl.DefaultFactory", 
                l_attrs.get( "factory" ).get( 0 ) ) ;
        assertEquals( "http://ibiblio.org/maven", 
                l_attrs.get( "remote.repository.url" ).get( 0 ) ) ;

        assertNotNull( l_attrs.get( "url" ).get( 0 ) ) ;
        assertNotNull( l_attrs.get( "url" ).get( 1 ) ) ;
        assertNotNull( l_attrs.get( "url" ).get( 2 ) ) ;
        */
    }

    final public void testGetProperties() throws Exception
    {
        /* Test for these properties
         * cache.dir=.repository
         * factory=org.apache.avalon.repository.impl.DefaultFactory
         * remote.repository.url.0=http://ibiblio.org/maven 
         */
        /*
        Properties l_props = 
            RepositoryUtils.getProperties( new URL( "http://www.ibiblio.org/maven/avalon-repository/propertiess/repository.properties" ) ) ;
        assertEquals( ".repository", l_props.getProperty( "cache.dir" ) ) ;
        assertEquals( "org.apache.avalon.repository.impl.DefaultFactory", 
                l_props.getProperty( "factory" ) ) ;
        assertEquals( "http://ibiblio.org/maven", 
                l_props.getProperty( "remote.repository.url.0" ) ) ;
        */
    }


    final public void testIsEnumerated()
    {
        assertFalse( "false for empty string \"\"", 
                RepositoryUtils.isEnumerated( "" ) ) ;

        assertFalse( "false for \".\"", 
                RepositoryUtils.isEnumerated( "." ) ) ;
        
        assertFalse( "false for \"nodot\"", 
                RepositoryUtils.isEnumerated( "nodot" ) ) ;
        
        assertFalse( "false for \"before.\"", 
                RepositoryUtils.isEnumerated( "before." ) ) ;
        
        assertFalse( "false for \".after\"", 
                RepositoryUtils.isEnumerated( ".after" ) ) ;
        
        assertFalse( "false for \"123.\"", 
                RepositoryUtils.isEnumerated( "123." ) ) ;
        
        assertFalse( "false for \".123\"", 
                RepositoryUtils.isEnumerated( ".123" ) ) ;
        
        assertFalse( "false for \"123.asdf\"", 
                RepositoryUtils.isEnumerated( "123.asdf" ) ) ;
        
        assertTrue( "true for \"asdf.123\"", 
                RepositoryUtils.isEnumerated( "asdf.123" ) ) ;
        
        assertTrue( "true for \"asdf.1\"", 
                RepositoryUtils.isEnumerated( "asdf.1" ) ) ;
    }

    
    final public void testGetEnumeratedBase()
    {
        assertEquals( "", 
                RepositoryUtils.getEnumeratedBase( "" ) ) ;

        assertEquals( ".", 
                RepositoryUtils.getEnumeratedBase( "." ) ) ;
        
        assertEquals( "nodot", 
                RepositoryUtils.getEnumeratedBase( "nodot" ) ) ;
        
        assertEquals( "before.", 
                RepositoryUtils.getEnumeratedBase( "before." ) ) ;
        
        assertEquals( ".after", 
                RepositoryUtils.getEnumeratedBase( ".after" ) ) ;
        
        assertEquals( "123.", 
                RepositoryUtils.getEnumeratedBase( "123." ) ) ;
        
        assertEquals( ".123", 
                RepositoryUtils.getEnumeratedBase( ".123" ) ) ;
        
        assertEquals( "123.asdf", 
                RepositoryUtils.getEnumeratedBase( "123.asdf" ) ) ;
        
        assertEquals( "asdf", 
                RepositoryUtils.getEnumeratedBase( "asdf.123" ) ) ;
        
        assertEquals( "asdf", 
                RepositoryUtils.getEnumeratedBase( "asdf.1" ) ) ;
    }
    
    
    public void testGetDelimited() throws Exception
    {
        String [] l_processed = null ;
        assertNull( RepositoryUtils.getDelimited( ',', null) ) ;
        assertNull( RepositoryUtils.getDelimited( ',', "") ) ;

        l_processed = RepositoryUtils.getDelimited( ',',
                "asdf" ) ;
        assertEquals( "asdf", l_processed[0] ) ;

        l_processed = RepositoryUtils.getDelimited( ',',
                "asdf,1234" ) ;
        assertEquals( "asdf", l_processed[0] ) ;
        assertEquals( "1234", l_processed[1] ) ;
        
        l_processed = RepositoryUtils.getDelimited( ',',
                "asdf,1234,abcd" ) ;
        assertEquals( "asdf", l_processed[0] ) ;
        assertEquals( "1234", l_processed[1] ) ;
        assertEquals( "abcd", l_processed[2] ) ;
    }
}
