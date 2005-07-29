/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.depot.install;

import java.io.File;

import net.dpml.transit.Transit;
import net.dpml.transit.model.Logger;

/**
 */
public class DepotInstaller
{
   /**
    * Handles the setup of the depot and transit bootstrap jar files 
    * under the ${dpml.data}/bootstrap directory in preparation for 
    * a subprocess execution of the Install plugin.
    */
    public DepotInstaller( Logger logger, String[] args )
    {
        logger.info( "executing bootstrap setup" );

        for( int i=0; i<args.length; i++ )
        {
            logger.info( "  " + args[i] );
        }

        /*
        String path = System.getProperty( "dpml.depot.install.bootstrap", null );
        if( null != path )
        {
            //
            // create the ${dpml.data}/[path] directory 
            //

            File data = Transit.DPML_DATA;
            File boot = new File( data, path );
            boot.mkdirs();

        }
        */
    }
}
