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

package org.apache.avalon.mutuals;

import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.logging.impl.SimpleLog;

import org.apache.avalon.mutuals.model.*;
import org.apache.avalon.mutuals.dao.*;
import org.apache.avalon.mutuals.dao.impl.*;

import java.util.List;

public class FortressMain {

  public SimpleLog m_log;
  public ServiceManager m_manager;

  public static void main(String[] args) throws Exception {
    SimpleLog log = new SimpleLog("fortress");
    FortressMain m = new FortressMain();

    // Set up all the preferences for Fortress
    final FortressConfig config = new FortressConfig();

    config.setContainerConfiguration("src/conf/system.xconf");
    config.setRoleManagerConfiguration("src/conf/roles.xconf");
    config.setLoggerManagerConfiguration("src/conf/logkit.xconf");

    // Get the root container initialized
    ContainerManager cm = new DefaultContainerManager(config.getContext());
    ContainerUtil.initialize(cm);
    DefaultContainer container = (DefaultContainer) cm.getContainer();
    ServiceManager serviceManager = container.getServiceManager();

    m.m_log = log;
    m.m_manager = serviceManager;
    m.performTransactions();

    org.apache.avalon.framework.container.ContainerUtil.dispose(cm);

  }

  public Object getComponent(String name) throws Exception {
    return m_manager.lookup(name);
  }

  public void performTransactions() {

    try {

      m_log.info("Logging in as users farra ");

      UserManager userManager = (UserManager) getComponent(UserManager.class.
          getName());
      User farra = userManager.login("farra", "farra");
      if (farra != null) {
        m_log.info("Successful login");
        m_log.info("User Information:");
        m_log.info("  User ID:   " + farra.getId());
        m_log.info("  User Name: " + farra.getName());
      }
      else {
        m_log.warn("Did not properly log in");
      }

      PortfolioManager portfolioManager = (PortfolioManager) getComponent(
          PortfolioManager.class.getName());
      double balance = portfolioManager.getAccountBalance(farra);
      m_log.info("  User Balance: $" + balance);

      m_log.info("Depositing $100.00 into account");
      portfolioManager.adjustAccountBalance(farra, 100.00);
      balance = portfolioManager.getAccountBalance(farra);
      m_log.info("Balance is now: $" + balance);

      m_log.info("User Portfolio:");
      List funds = portfolioManager.getPortfolio(farra);
      for (int i = 0; i < funds.size(); i++) {
        Fund fund = (Fund) funds.get(i);
        m_log.info("  " + fund.getSymbol() + "  " + fund.getName() + "  $" +
                   fund.getPrice() + "  " + fund.getQty());
      }

      m_log.info("Selling 10 shares of MOON");
      FundManager fundManager = (FundManager) getComponent(FundManager.class.
          getName());
      TransactionManager transactionManager = (TransactionManager) getComponent(
          TransactionManager.class.getName());
      Fund moon = fundManager.getFund("MOON");
      transactionManager.sell(farra, moon, 10);

      balance = portfolioManager.getAccountBalance(farra);
      m_log.info("Balance is now: $" + balance);
      m_log.info("User Portfolio:");
      funds = portfolioManager.getPortfolio(farra);
      for (int i = 0; i < funds.size(); i++) {
        Fund fund = (Fund) funds.get(i);
        m_log.info("  " + fund.getSymbol() + "  " + fund.getName() + "  $" +
                   fund.getPrice() + "  " + fund.getQty());
      }

    }
    catch (Exception ex) {
      m_log.error("Exception occurred during transaction test", ex);
    }

  }

}
