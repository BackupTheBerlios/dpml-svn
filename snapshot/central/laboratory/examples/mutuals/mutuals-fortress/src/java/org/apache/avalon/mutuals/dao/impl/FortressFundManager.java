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

/**
 * @version $Id: FortressFundManager.java,v 1.1 2004/02/29 08:41:42 farra Exp $
 */

public class FortressFundManager extends AbstractFundManager implements Configurable, LogEnabled  {

  protected Logger m_log;

  public FortressFundManager() {
  }

  public void configure(Configuration configuration)
      throws ConfigurationException
  {
    HashMap funds = new HashMap();

    Configuration[] fundsConf = configuration.getChildren("fund");
    for(int i=0; i < fundsConf.length; i++){
      Configuration c = fundsConf[i];
      Fund fund = new Fund();
      fund.setSymbol(c.getAttribute("symbol"));
      fund.setName(c.getAttribute("name"));
      fund.setPrice((double)c.getAttributeAsFloat("price"));
      funds.put(fund.getSymbol(),fund);
    }

    m_funds = funds;
  }

  public void enableLogging(Logger logger) {
    m_log = logger;
    m_log.info("Logging enabled for FortressFundManager");
  }
}