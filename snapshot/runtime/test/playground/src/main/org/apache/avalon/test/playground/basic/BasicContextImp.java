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

package org.apache.avalon.test.playground.basic;

import java.io.File;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;

/**
 * This is example of a custom context class.  It is used in the demonsteation
 * of a context management fraework to show how a context class can be
 * supplied to a component declaring a context interface criteria.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class BasicContextImp extends DefaultContext implements BasicContext
{

    /**
     * Creation of a new custom context instance.
     * @param map the context name/value map
     * @param parent a possibly parent context
     */
    public BasicContextImp( Context context )
    {
        super( context );
    }

    /**
     * @return the location
     */
    public String getLocation()
    {
        try
        {
            return (String) super.get( "location" );
        } 
        catch( Throwable e )
        {
            return "Unknown";
        }
    }

    /**
     * @return the working directory
     */
    public File getWorkingDirectory()
    {
        try
        {
            return (File) super.get( "home" );
        } 
        catch( Throwable e )
        {
            throw new RuntimeException( "context object does not provide required home entry." );
        }
    }

}
