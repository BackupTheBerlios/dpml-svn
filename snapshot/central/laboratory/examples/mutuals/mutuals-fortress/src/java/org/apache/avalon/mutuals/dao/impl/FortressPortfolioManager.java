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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import java.util.HashMap;
import org.apache.avalon.mutuals.model.Fund;
import org.apache.avalon.framework.configuration.ConfigurationException;
import java.util.ArrayList;

/**
 * @version $Id: FortressPortfolioManager.java,v 1.1 2004/02/29 08:41:42 farra Exp $
 */

public class FortressPortfolioManager
    extends AbstractPortfolioManager
    implements Configurable, LogEnabled
{

  protected Logger m_log;

  public FortressPortfolioManager() {
  }

  public void configure(Configuration configuration)
      throws ConfigurationException
  {
    HashMap balances = new HashMap();
    HashMap portfolios = new HashMap();

    Configuration[] userConfs = configuration.getChild("balances",true).getChildren("user");
    for(int i=0; i < userConfs.length; i++){
      Configuration c = userConfs[i];
      String user = c.getAttribute("id");
      String balance = c.getAttribute("amount");
      balances.put(user,new Double(balance));
    }

    Configuration[] portConfs = configuration.getChild("portfolios",true).getChildren("user");
    for(int i=0; i < portConfs.length; i++){
      Configuration user = portConfs[i];
      String id = user.getAttribute("id");
      Configuration[] fundsConf = user.getChildren("fund");
      ArrayList funds = new ArrayList();
      for(int j=0; j < fundsConf.length; j++){
        Configuration c = fundsConf[j];
        Fund fund = new Fund();
        fund.setSymbol(c.getAttribute("symbol"));
        fund.setName(c.getAttribute("name"));
        fund.setPrice((double)c.getAttributeAsFloat("price"));
        fund.setQty(c.getAttributeAsInteger("qty"));
        funds.add(fund);
      }
      portfolios.put(id,funds);
    }

    m_balances = balances;
    m_portfolios = portfolios;

  }

  public void enableLogging(Logger logger) {
    m_log = logger;
    m_log.info("Logging enabled for FortressPortfolioManager");
  }
}