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
import org.apache.tools.ant.taskdefs.Property;

/**
 * Build a set of projects taking into account dependencies within the
 * supplied fileset.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PropertyTask extends FeatureTask
{
    private String m_property;

   /**
    * Set the property name. The property task will assign a value to the 
    * property declared by the supplied name.
    * @param property the property name
    */
    public void setName( final String property )
    {
        m_property = property;
    }

   /**
    * Task execution.
    * @exception BuildException if an error occurs
    */
    public void execute() throws BuildException
    {
        if( null == m_property )
        {
            final String error =
              "Missing name attribute.";
            throw new BuildException( error );
        }

        final String value = super.resolve();
        if( null != value )
        {
            final Property property = (Property) getProject().createTask( "property" );
            property.init();
            property.setName( m_property );
            property.setValue( value );
            property.setTaskName( getTaskName() );
            property.execute();
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
