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

package test.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A singleton component that increments a counter.
 * 
 * @avalon.component name="test" lifestyle="singleton"
 * @avalon.service type="test.http.Counter"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultCounter implements Counter
{
    //----------------------------------------------------------
    // state
    //----------------------------------------------------------

    private int m_count = 0;

    //----------------------------------------------------------
    // Counter
    //----------------------------------------------------------

    public int increment()
    {
        m_count++;
        return m_count;
    }
}
