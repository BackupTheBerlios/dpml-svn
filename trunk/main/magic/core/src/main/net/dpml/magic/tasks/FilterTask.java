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

package net.dpml.magic.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Filter;



/**
 * Construct a pattern filter using a supplied feature and token.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class FilterTask extends FeatureTask
{
    private String m_token;

   /**
    * Set the filter token value.
    * @param token the token value
    */
    public void setToken( final String token )
    {
        m_token = token;
    }

   /**
    * Execute the task.
    * @exception BuildExcetion if a build error occurs
    */
    public void execute() throws BuildException
    {
        if( null == m_token )
        {
            final String error =
              "Missing token attribute.";
            throw new BuildException( error );
        }

        final String value = super.resolve();
        if( null != value )
        {
            final Filter filter = (Filter) getProject().createTask( "filter" );
            filter.setTaskName( getTaskName() );
            filter.init();
            filter.setToken( m_token );
            filter.setValue( value );
            filter.execute();
        }
        else
        {
            final String error =
              "Unrecognized or unsupported feature ["
              + getFeature()
              + "].";
            throw new BuildException( error );
        }
    }
}
