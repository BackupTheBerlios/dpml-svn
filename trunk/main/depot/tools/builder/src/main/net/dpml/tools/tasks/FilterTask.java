/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Filter;

/**
 * Construct a pattern filter using a supplied feature and token.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FilterTask extends FeatureTask
{
    private String m_token;
    private String m_default;

   /**
    * Set the filter token value.
    * @param token the token value
    */
    public void setToken( final String token )
    {
        m_token = token;
    }

   /**
    * Set a default filter value.
    * @param value the default value
    */
    public void setDefault( final String value )
    {
        m_default = value;
    }

   /**
    * Execute the task.
    * @exception BuildException if a build error occurs
    */
    public void execute() throws BuildException
    {
        if( null == m_token )
        {
            final String error =
              "Missing token attribute.";
            throw new BuildException( error );
        }

        final String value = resolveValue();
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
    
    private String resolveValue()
    {
        String value = super.resolve();
        if( null == value )
        {
            return m_default;
        }
        else
        {
            return value;
        }
    }
}
