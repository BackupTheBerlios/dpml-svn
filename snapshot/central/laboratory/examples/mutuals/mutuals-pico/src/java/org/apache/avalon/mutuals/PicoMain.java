package org.apache.avalon.mutuals;

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


import java.util.*;

import org.apache.avalon.mutuals.model.*;
import org.apache.avalon.mutuals.dao.*;
import org.apache.avalon.mutuals.dao.impl.*;
import org.apache.avalon.mutuals.dao.lifecycle.LoggingDao;

import org.picocontainer.defaults.DefaultPicoContainer;

import org.apache.commons.logging.impl.SimpleLog;
import org.picocontainer.PicoContainer;
import org.apache.commons.logging.Log;

public class PicoMain
{

  public PicoContainer m_container;
  public Log m_log;

    public static void main( String[] args )
    {
      SimpleLog log = new SimpleLog("pico");
      PicoMain m = new PicoMain();

      Map users = m.createUsers();
      Map funds = m.createFunds();
      Map portfolios = m.createPortfolios();
      Map balances = m.createBalances();

      PicoUserManager userManager = new PicoUserManager(users);
      PicoFundManager fundManager = new PicoFundManager(funds);
      PicoPortfolioManager portfolioManager = new PicoPortfolioManager(portfolios,balances);

      DefaultPicoContainer container = new DefaultPicoContainer();
      container.registerComponentInstance("userManager",userManager);
      container.registerComponentInstance("fundManager",fundManager);
      container.registerComponentInstance("portfolioManager",portfolioManager);
      container.registerComponentImplementation("transactionManager",PicoTransactionManager.class);

      LoggingDao dao = (LoggingDao) container.getComponentMulticaster();
      dao.enableLogging(log);

      m.m_container = container;
      m.m_log = log;

      m.performTransactions();

    }

    public void performTransactions() {

      try {

        m_log.info("Logging in as users farra ");

        UserManager userManager = (UserManager) getComponent("userManager");
        User farra = userManager.login("farra","farra");
        if(farra != null){
          m_log.info("Successful login");
          m_log.info("User Information:");
          m_log.info("  User ID:   "+farra.getId());
          m_log.info("  User Name: "+farra.getName());
        }
        else{
          m_log.warn("Did not properly log in");
        }

        PortfolioManager portfolioManager = (PortfolioManager) getComponent("portfolioManager");
        double balance = portfolioManager.getAccountBalance(farra);
        m_log.info("  User Balance: $"+balance);

        m_log.info("Depositing $100.00 into account");
        portfolioManager.adjustAccountBalance(farra,100.00);
        balance = portfolioManager.getAccountBalance(farra);
        m_log.info("Balance is now: $"+balance);

        m_log.info("User Portfolio:");
        List funds = portfolioManager.getPortfolio(farra);
        for(int i=0; i < funds.size(); i++){
          Fund fund = (Fund) funds.get(i);
          m_log.info("  "+fund.getSymbol()+"  "+fund.getName()+"  $"+fund.getPrice()+"  "+fund.getQty());
        }

        m_log.info("Selling 10 shares of MOON");
        FundManager fundManager = (FundManager) getComponent("fundManager");
        TransactionManager transactionManager = (TransactionManager) getComponent("transactionManager");
        Fund moon = fundManager.getFund("MOON");
        transactionManager.sell(farra,moon,10);

        balance = portfolioManager.getAccountBalance(farra);
        m_log.info("Balance is now: $"+balance);
        m_log.info("User Portfolio:");
        funds = portfolioManager.getPortfolio(farra);
        for(int i=0; i < funds.size(); i++){
          Fund fund = (Fund) funds.get(i);
          m_log.info("  "+fund.getSymbol()+"  "+fund.getName()+"  $"+fund.getPrice()+"  "+fund.getQty());
        }

      }
      catch (Exception ex) {
        m_log.error("Exception occurred during transaction test",ex);
      }

    }

    public Object getComponent(String name) throws Exception{
      return m_container.getComponentInstance(name);
    }


    public Map createUsers(){
      HashMap users = new HashMap();

      User me = new User();
      me.setId("farra");
      me.setName("Aaron Farr");
      me.setPassword("farra");

      User you = new User();
      you.setId("lsd");
      you.setName("Leo Simons");
      you.setPassword("lsd");

      users.put(me.getId(),me);
      users.put(you.getId(),you);

      return users;
    }

    public Map createFunds(){
      HashMap funds = new HashMap();

      Fund moon = new Fund();
      moon.setSymbol("MOON");
      moon.setName("Moon Microsystems");
      moon.setPrice(5.39);

      Fund ibn = new Fund();
      ibn.setSymbol("IBN");
      ibn.setName("International Business gNomes");
      ibn.setPrice(95.32);

      Fund ms = new Fund();
      ms.setSymbol("MNFT");
      ms.setName("Minisoft");
      ms.setPrice(27.81);

      funds.put(moon.getSymbol(),moon);
      funds.put(ibn.getSymbol(),ibn);
      funds.put(ms.getSymbol(),ms);

      return funds;
    }

    public Map createBalances(){
      HashMap balances = new HashMap();
      balances.put("farra",new Double(1000.00));
      balances.put("leo",new Double(5000.00));
      return balances;
    }

    public Map createPortfolios(){
      HashMap portfolios = new HashMap();

      Fund moon = new Fund();
      moon.setSymbol("MOON");
      moon.setName("Moon Microsystems");
      moon.setPrice(5.39);
      moon.setQty(100);

      Fund ibn1 = new Fund();
      ibn1.setSymbol("IBN");
      ibn1.setName("International Business gNomes");
      ibn1.setPrice(95.32);
      ibn1.setQty(10);

      Fund ibn2 = new Fund();
      ibn2.setSymbol("IBN");
      ibn2.setName("International Business gNomes");
      ibn2.setPrice(95.32);
      ibn2.setQty(30);

      Fund ms = new Fund();
      ms.setSymbol("MNFT");
      ms.setName("Minisoft");
      ms.setPrice(27.81);
      ms.setQty(25);

      ArrayList port1 = new ArrayList();
      port1.add(moon);
      port1.add(ibn1);

      ArrayList port2 = new ArrayList();
      port2.add(ibn2);
      port2.add(ms);

      portfolios.put("farra",port1);
      portfolios.put("leo",port2);

      return portfolios;
    }


}
