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

package org.apache.avalon.composition.provider;

import java.net.URL;

import org.apache.avalon.composition.data.ContainmentProfile;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.ModelException;

/**
 * A factory enabling the establishment of new containment model instances.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ModelFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ModelFactory 
{
   /**
    * Creation of a new root containment model.
    *
    * @param url a url of a containment profile 
    * @return the containment model
    * @exception ModelException if an error occurs during model establishment
    */
    ContainmentModel createRootContainmentModel( URL url ) 
      throws ModelException;

   /**
    * Creation of a new root containment model.
    *
    * @param profile a containment profile 
    * @return the containment model
    * @exception ModelException if an error occurs during model establishment
    */
    ContainmentModel createRootContainmentModel( ContainmentProfile profile ) 
      throws ModelException;

   /**
    * Creation of a new nested component model using a supplied component
    * context.
    *
    * @param context a potentially foreign component context
    * @return the compoent model
    */
    ComponentModel createComponentModel( ComponentContext context )
      throws ModelException;


   /**
    * Creation of a new nested containment model using a supplied 
    * containment context.
    *
    * @param context a potentially foreign containment context
    * @return the containment model
    */
    ContainmentModel createContainmentModel( ContainmentContext context )
      throws ModelException;

}
