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

package org.apache.avalon.repository;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Repository URL protocol handler.  
 */
public class BlockHandler extends URLStreamHandler
{

    /**
     * Opens a connection to the specified URL.
     *
     * @param url A URL to open a connection to.
     * @return The established connection.
     * @throws IOException If a connection failure occurs.
     */
    protected URLConnection openConnection( final URL url )
      throws IOException
    {
        return new BlockURLConnection( url );
    }

    protected int getDefaultPort()
    {
        return 0;
    }

    protected String toExternalForm( URL url )
    {
	  StringBuffer result = new StringBuffer( "block:" );
        if (url.getFile() != null )
        {
            result.append(url.getFile());
        }
	  if (url.getRef() != null ) 
        {
	      result.append("#");
            result.append(url.getRef());
	  }
	  return result.toString();
    }
}
