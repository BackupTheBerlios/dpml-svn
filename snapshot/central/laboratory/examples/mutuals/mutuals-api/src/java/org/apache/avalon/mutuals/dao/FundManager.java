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
package org.apache.avalon.mutuals.dao;

import org.apache.avalon.mutuals.model.Fund;
import org.apache.avalon.mutuals.dao.exception.DaoException;
import java.util.List;

/**
 * @version $Id: FundManager.java,v 1.1 2004/02/29 08:41:41 farra Exp $
 */

public interface FundManager {

  public Fund getFund(String symbol) throws DaoException;
  public List getFunds() throws DaoException;

}