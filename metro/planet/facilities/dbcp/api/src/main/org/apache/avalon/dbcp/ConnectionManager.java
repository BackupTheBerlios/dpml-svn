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
 
package org.apache.avalon.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service definition for a JDBC SQL connection manager for obtaining
 * JDBC connections to a datasource.
 *
 * @avalon.service version="1.0"
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/05/11 21:23:02 $
 */
public interface ConnectionManager {
	/**
	 * Returns a <code>java.sql.Connection</code> to the default data source.
	 * 
	 * @return a <code>java.sql.Connection</code> to the default data source
	 */
	Connection getConnection() throws SQLException;
	/**
	 * Returns a <code>java.sql.Connection</code> to the specified data source.
	 * 
	 * @param name the name of the data source to obtain a connection to
	 * @return a <code>java.sql.Connection</code> to the specified data source
	 */
	Connection getConnection(String name) throws SQLException;
}
