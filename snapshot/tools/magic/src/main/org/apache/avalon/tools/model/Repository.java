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

package org.apache.avalon.tools.model;

import java.io.File;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Repository 
{
    private final File m_cache;
    private final String[] m_hosts;

    public Repository( final File cache, final String[] hosts )
    {
        if( null == cache ) 
        {
            throw new NullPointerException( "cache" );
        }
        m_cache = cache;
        m_hosts = hosts;
    }

    public File getCacheDirectory()
    {
        return m_cache;
    }

    public String[] getHosts()
    {
        return m_hosts;
    }
}
