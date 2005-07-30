/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.depot.exec;

import java.util.prefs.Preferences;

import net.dpml.profile.DepotProfile;
import net.dpml.transit.model.Logger;


/**
 * Depot application deployment plugin.  This plugin handles the deployment of 
 * a target application based on commandline arguments supplied by the Depot 
 * Console.  It uses the Application Profile sub-system to retireve information 
 * about registered applications and criteria for JVM setup.
 */
public class ApplicationHandler
{
   /**
    * Plugin class used to handle the deployment of target application.
    * 
    * @param logger the assigned logging channel
    * @param profile the depot profile
    */
    public ApplicationHandler( Logger logger, DepotProfile profile ) 
    {
    }
}
