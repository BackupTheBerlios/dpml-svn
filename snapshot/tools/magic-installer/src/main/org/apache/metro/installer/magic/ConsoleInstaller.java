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

package org.apache.metro.installer.magic;

import java.io.File;

public class ConsoleInstaller
{
    private static final int BUFFER_SIZE = 10000;
    
    public ConsoleInstaller()
    {
    }
    
    public void start()
        throws Exception
    {
        File userHome = new File( System.getProperty( "user.home" ) );
        File magicHome = new File( userHome, ".magic" );
        File antLibDir = new File( userHome, ".ant/lib" );
        File cwDir = new File( System.getProperty( "user.dir" ) );
        
        ProgressIndicator indicator = new ConsoleProgress();
        Worker w = new Worker( indicator, magicHome, antLibDir, cwDir );
        w.start();
    }
    
    public class ConsoleProgress
        implements ProgressIndicator
    {
        public void message( String message )
        {
            System.out.println( message );
        }
        
        public void start()
        {
            System.out.print( "[                                                  ]  0%" );
        }        
        
        public void progress( int percentage )
        {
            int size = percentage / 2;
            System.out.print( "\r[" );
            for( int i = 0 ; i < size ; i++ )
                System.out.print( "*" );
            for( int i = 0 ; i < 50-size ; i++ )
                System.out.print( " " );
            System.out.print( "]  " + percentage + "%" );
        }
        
        public void finished()
        {
            System.out.print( "[**************************************************]  100%" );
            System.out.println();
        }
    }
}
 
