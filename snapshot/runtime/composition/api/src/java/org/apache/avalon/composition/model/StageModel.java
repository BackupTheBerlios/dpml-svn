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

package org.apache.avalon.composition.model;

import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.ExtensionDescriptor;

/**
 * Stage model handles the establishment of an explicit source 
 * extension defintion or stage provider selection based on 
 * extension qualification.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: StageModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface StageModel extends Dependent
{
   /**
    * Return the stage descriptor for the model.
    *
    * @return the descriptor declaring the component 
    *    stage dependency
    */
    StageDescriptor getStage();

   /**
    * Return an explicit path to a component.  
    * If a stage directive has been declared
    * and the directive contains a source declaration, the value 
    * returned is the result of parsing the source value relative 
    * to the absolute address of the dependent component.
    *
    * @return the explicit path
    */
    String getPath();

   /**
    * Filter a set of candidate service descriptors and return the 
    * set of acceptable service as a ordered sequence.
    *
    * @param candidates the set of candidate extension providers
    *    for the stage dependency
    * @return the accepted candidates in ranked order
    */
    ExtensionDescriptor[] filter( ExtensionDescriptor[] candidates );
}
