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

package tutorial;

import java.io.File;
import java.util.Map;

import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.Context;

/**
 * This is example of a custom context class.  It is used in the demonsteation
 * of a context management fraework to show how a context class can be
 * supplied to a component declaring a context interface criteria.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class StandardContextImp extends DefaultContext 
    implements StandardContext
{

    //-----------------------------------------------------------------
    // constructor
    //-----------------------------------------------------------------

    /**
     * Creation of a new custom context instance.
     * @param map the context name/value map
     * @param parent a possibly parent context
     */
    public StandardContextImp( Context context ) throws ContextException
    {
        super( context );
    }

    //-----------------------------------------------------------------
    // StandardContext
    //-----------------------------------------------------------------

    /**
     * Return the name assigned to the component
     * @return the name
     */
    public String getName()
    {
        try
        {
            return (String) super.get( StandardContext.NAME_KEY );
        }
        catch( Throwable e )
        {
            throw new IllegalStateException( StandardContext.NAME_KEY );
        }
    }

    /**
     * Return the partition name assigned to the component
     * @return the partition name
     * @exception IllegalStateException if the partition name is undefined
     */
    public String getPartitionName()
    {
        try
        {
            return (String) super.get( StandardContext.PARTITION_KEY );
        }
        catch( Throwable e )
        {
            throw new IllegalStateException( StandardContext.PARTITION_KEY );
        }
    }

    /**
     * Returns the home directory for this component.
     * @return the home directory
     */
    public File getHomeDirectory()
    {
        try
        {
            return (File) super.get( StandardContext.HOME_KEY );
        }
        catch( Throwable e )
        {
            throw new IllegalStateException( StandardContext.HOME_KEY );
        }
    }

    /**
     * @return the working directory
     */
    public File getWorkingDirectory()
    {
        try
        {
            return (File) super.get( StandardContext.WORKING_KEY );
        }
        catch( Throwable e )
        {
            throw new IllegalStateException( StandardContext.WORKING_KEY );
        }
    }
}
