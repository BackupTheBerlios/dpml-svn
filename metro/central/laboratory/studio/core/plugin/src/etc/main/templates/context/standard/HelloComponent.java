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

import java.io.File;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * Component demonstrating access to standard context entries.
 * @avalon.component name="demo" lifestyle="singleton"
 */
public class HelloComponent extends AbstractLogEnabled 
  implements Contextualizable
{

    private File m_home = null;
    private File m_temp = null;
    private String m_name = "unknown";
    private String m_partition = "unknown";

   /**
    * Contextualization of the component by the container.
    * The context supplied by the container holds the 
    * Merlin standard context entries for the home and 
    * working directories, component name and partition.
    *
    * @avalon.context
    * @avalon.entry key="urn:avalon:name" 
    * @avalon.entry key="urn:avalon:partition" 
    * @avalon.entry key="urn:avalon:home" type="java.io.File"
    * @avalon.entry key="urn:avalon:temp" type="java.io.File"
    */
    public void contextualize( Context context )
      throws ContextException
    {
        m_home = (File) context.get( "urn:avalon:home" );
        m_temp = (File) context.get( "urn:avalon:temp" );
        m_name = (String) context.get( "urn:avalon:name" );
        m_partition = (String) context.get( "urn:avalon:partition" );

        StringBuffer buffer = new StringBuffer( "standard context entries" );
        buffer.append( "\n  name: " + m_name );
        buffer.append( "\n  home: " + m_home );
        buffer.append( "\n  temp: " + m_temp );
        buffer.append( "\n  partition: " + m_partition );

        getLogger().info( buffer.toString() );

    }
}
