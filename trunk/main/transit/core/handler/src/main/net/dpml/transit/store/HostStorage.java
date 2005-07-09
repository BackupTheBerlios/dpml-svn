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

package net.dpml.transit.store;

import java.net.URL;
import java.net.PasswordAuthentication; 

import net.dpml.transit.model.LayoutModel;

/**
 * 
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface HostStorage extends CodeBaseStorage
{
    String getID();

    String getName();

    boolean getBootstrap();

    boolean getTrusted();

    boolean getEnabled();

    int getPriority();

    String getLayoutModelKey();

    String getBasePath();

    String getIndexPath();

    PasswordAuthentication getAuthentication();

    String getScheme();

    String getPrompt();

    Strategy getStrategy();

    void setHostSettings( 
      String base, String index, boolean enabled, boolean trusted, String layout, 
      PasswordAuthentication auth, String scheme, String prompt );

    void setName( String name );

    void setPriority( int priority );

    void setLayoutModelKey( String layout );

}
