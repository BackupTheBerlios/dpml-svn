/*
 * Copyright 2007 Stephen J. McConnell.
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

package net.dpml.part.composition;

import java.io.File;

import junit.framework.TestCase;

import net.dpml.lang.Strategy;
import net.dpml.lang.TypeCastException;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class AbstractTestCase extends TestCase
{
    static
    {
        System.setProperty( 
          "handler:dpml:metro", 
          "net.dpml.runtime.ComponentStrategyHandler" );
    }
    
    protected <T>T load( Class<T> type, String path ) throws Exception
    {
        return load( type, path, null );
    }
    
    protected <T>T load( Class<T> type, String path, String name ) throws Exception
    {
        ClassLoader classloader = getClass().getClassLoader();
        File dir = new File( System.getProperty( "project.test.dir" ) );
        File file = new File( dir, path );
        try
        {
            Strategy strategy = Strategy.load( classloader, null, file.toURI(), name );
            return strategy.getContentForClass( type );
        }
        catch( TypeCastException e )
        {
            String report = e.getReport();
            System.out.println( report );
            throw e;
        }
    }
}
