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
 * A HostStorage implementation is responsible for the maintainence of 
 * persistent information for an identifiable host model.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface HostStorage extends CodeBaseStorage
{
   /**
    * Return an immutable host identifier.  The host identifier shall be 
    * guranteed to be unique and constant for the life of the storage unit.
    */
    String getID();

   /**
    * Return the name of the resource host.
    * @return the host name
    */
    String getName();

   /**
    * Return the bootstrap status of the host.
    * @return TRUE if this is a bootstrap host
    */
    boolean getBootstrap();

   /**
    * Return the trusted status.
    * @return TRUE if trusted 
    */
    boolean getTrusted();

   /**
    * Return the enabled status of the host.
    * @return TRUE if enabled 
    */
    boolean getEnabled();

   /**
    * Return the host priority.
    * @return the host priority setting
    */
    int getPriority();

   /**
    * Return the layout strategy model key.
    * @return the layout model key7
    */
    String getLayoutModelKey();

   /**
    * Return the host base path.
    * @return the base path
    */
    String getBasePath();

   /**
    * Return index path.
    * @return the index path
    */
    String getIndexPath();

   /**
    * Return the host password authentication credentials.
    * @return the password authentication credentials
    */
    PasswordAuthentication getAuthentication();

   /**
    * Return a striong identify the authentication scheme employed by the host.
    * @return the scheme
    */
    String getScheme();

   /**
    * Return the authentication prompt.
    * Return the prompt value
    */
    String getPrompt();

   /**
    * Return the strategy using to establish a host model.
    * @return the strategy
    */
    Strategy getStrategy();

   /**
    * Update the storage unit with the supplied values.
    *
    * @param base the host base url path
    * @param index the host content index
    * @param enabled the enabled status of the host
    * @param trusted the trusted status of the host
    * @param layout the assigned host layout identifier
    * @param auth a possibly null host authentication username and password
    * @param scheme the host security scheme
    * @param prompt the security prompt raised by the host
    */
    void setHostSettings( 
      String base, String index, boolean enabled, boolean trusted, String layout, 
      PasswordAuthentication auth, String scheme, String prompt );

   /**
    * Set the host name.
    * @param the name
    */
    void setName( String name );

   /**
    * Set the host priority.
    * @param the priority value
    */
    void setPriority( int priority );

   /**
    * Set the host layout model identitfier.
    * @param the layout key
    */
    void setLayoutModelKey( String layout );

}
