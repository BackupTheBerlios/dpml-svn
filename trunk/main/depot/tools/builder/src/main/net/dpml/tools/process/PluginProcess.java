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

package net.dpml.tools.process;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import net.dpml.lang.Classpath;
import net.dpml.lang.Category;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.lang.Logger;
import net.dpml.transit.util.ExceptionHelper;

import net.dpml.part.Part;
import net.dpml.part.Strategy;
import net.dpml.part.Info;
import net.dpml.part.PartBuilder;

import net.dpml.lang.Type;
import net.dpml.library.info.Scope;
import net.dpml.library.Resource;

import net.dpml.tools.tasks.PartTask;
import net.dpml.tools.model.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PluginProcess extends AbstractBuildListener
{    
    /**
     * Signals that a target is finished.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getTarget()
     */
    public void targetStarted( BuildEvent event )
    {
        Target target = event.getTarget();
        String name = target.getName();
        if( "package".equals( name ) )
        {
            try
            {
                PartTask task = new PartTask();
                Project project = event.getProject();
                task.setProject( project );
                task.init();
                task.execute();
            }
            catch( BuildException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                final String message = 
                  "Unexpected failure during plugin generation.";
                final String error =
                  ExceptionHelper.packException( message, e, true );
                throw new BuildException( error, e );
            }
        }
    }
}
