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

/**
 * Interface implemented by objects that provide layout model data storage.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface LayoutStorage extends CodeBaseStorage
{
   /**
    * The layout model identifier.
    * @return the layout id
    */
    String getID();

   /**
    * The layout model title.
    * @return the layout title
    */
    String getTitle();

   /**
    * Set the layout model title.
    * @param title the new layout title
    */
    void setTitle( String title );

   /**
    * Return the strategy object for the layout model.
    * @return the strategy
    */
    Strategy getStrategy();

   /**
    * Set the model construction strategy.
    * @param strategy the construction strategy
    */
    void setStrategy( Strategy strategy );
}
