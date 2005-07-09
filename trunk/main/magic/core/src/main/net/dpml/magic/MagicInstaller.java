/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.magic;

import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.UnknownKeyException;

/**
 */
public class MagicInstaller
{
    public MagicInstaller( TransitRegistryModel home, Boolean install ) throws Exception
    {
        String profile = "development";
        if( install.booleanValue() )
        {
            try
            {
                home.getTransitModel( profile );
                System.out.println( "  transit development profile exists (no action required)" );
            }
            catch( UnknownKeyException e )
            {
                System.out.println( "  adding transit development profile" );
                home.addTransitModel( profile );
                System.out.println( "  profile created" );
            }
        }
        else
        {
            System.out.println( "  removing transit development profile" );
        }
    }
}
