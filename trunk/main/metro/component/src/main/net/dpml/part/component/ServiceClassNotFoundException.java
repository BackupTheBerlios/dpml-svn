/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.part.component;

/**
 * Unexpected runtime exception indicating an internal model error.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ServiceClassNotFoundException extends ComponentRuntimeException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private String m_serviceClassname;
    private String m_componentClassname;

    public ServiceClassNotFoundException( String componentClassname, String serviceClassname )
    {
        super( buildMessage( componentClassname, serviceClassname ) );
        m_serviceClassname = serviceClassname;
        m_componentClassname = componentClassname;
    }

    public String getComponentClassname()
    {
        return m_componentClassname;
    }

    public String getServiceClassname()
    {
        return m_serviceClassname;
    }

    private static String buildMessage( String componentClassname, String serviceClassname )
    {
        String error = 
          "Service class ["
          + serviceClassname 
          + "] declared by the component type ["
          + componentClassname
          + "] was not found.";
        return error;
    }

}

