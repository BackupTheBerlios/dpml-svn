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

package org.apache.avalon.mutuals.dao.impl;

import org.apache.avalon.mutuals.dao.FundManager;
import org.apache.avalon.mutuals.model.Fund;
import org.apache.avalon.mutuals.dao.exception.DaoException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * @version $Id: AbstractFundManager.java,v 1.1 2004/02/29 08:41:41 farra Exp $
 */

public abstract class AbstractFundManager
    implements FundManager
{

  protected Map m_funds;

  public Fund getFund(String symbol) throws DaoException {
    Fund fund = null;
    if(m_funds.containsKey(symbol))
      fund = (Fund) m_funds.get(symbol);
    return fund;
  }
  public List getFunds() throws DaoException {
    ArrayList funds = new ArrayList(m_funds.entrySet());
    return funds;
  }

}