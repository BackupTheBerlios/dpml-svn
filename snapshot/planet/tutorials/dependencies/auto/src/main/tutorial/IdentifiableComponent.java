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
package tutorial;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * The IdentifiableComponent implements Identifiable.
 *
 * @avalon.component version="1.0" name="simple" lifestyle="singleton"
 * @avalon.service type="tutorial.Identifiable"
 */
public class IdentifiableComponent extends AbstractLogEnabled 
  implements Identifiable, Contextualizable
{

    private String m_identity = null;

   /**
    * Contextualization of the component during which we 
    * establish the component identity.
    *
    * @param context the component context
    * @avalon.entry key="urn:avalon:name"
    * @avalon.entry key="urn:avalon:partition"
    */
    public void contextualize( Context context )
      throws ContextException
    {
        getLogger().info( "contextualize" );
        String name = (String) context.get( "urn:avalon:name" );
        String partition = (String) context.get( "urn:avalon:partition" );
        m_identity = partition + name;
    }

    public String getIdentity()
    {
        return m_identity;
    }

}
