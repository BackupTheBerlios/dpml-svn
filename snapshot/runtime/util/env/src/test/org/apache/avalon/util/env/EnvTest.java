/*
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.avalon.util.env;

import junit.framework.TestCase;


/**
 * Env tests.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: EnvTest.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class EnvTest extends TestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(EnvTest.class);
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
     * Constructor for EnvTest.
     * @param arg0
     */
    public EnvTest(String arg0)
    {
        super(arg0);
    }

    
    final public void testEnvVariable()
        throws Exception
    {
        if ( Env.isUnix() )
        {    
            assertNotNull( Env.getEnvVariable( "PATH" ) ) ;
        }
        
        if ( Env.isWindows() )
        {
            assertNotNull( Env.getEnvVariable( "Path" ) ) ;
        }
        
        assertNull( Env.getEnvVariable( "PAT" ) ) ;
    }

    
    public void testEnv()
        throws EnvAccessException
    {
        Env l_env = new Env() ;
        assertNull( l_env.getProperty( "PAT" ) ) ;
        
        if ( Env.isUnix() )
        {    
            assertNotNull( l_env.getProperty( "PATH" ) ) ;
        }
        
        if ( Env.isWindows() )
        {
            assertNotNull( l_env.getProperty( "PROMPT" ) ) ;
        }
    }
}
