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

import java.util.Map;
import org.apache.avalon.mutuals.dao.lifecycle.LoggingDao;
import org.apache.commons.logging.Log;

/**
 * @version $Id: PicoPortfolioManager.java,v 1.1 2004/02/29 08:41:42 farra Exp $
 */

public class PicoPortfolioManager
    extends AbstractPortfolioManager
    implements LoggingDao
{

  protected Log m_log;

  public PicoPortfolioManager(Map portfolios, Map balances) {
    m_portfolios = portfolios;
    m_balances = balances;
  }

  public void enableLogging(Log log) {
    m_log = log;
    log.info("Logging enabled for PicoPortfolioManager");
  }
}