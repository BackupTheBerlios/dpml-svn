/*
 * Copyright 1999-2004 Apache Software Foundation
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

package org.apache.avalon.mds.impl;

import org.apache.avalon.mds.MDSManager;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;

import org.apache.avalon.composition.event.CompositionEvent;
import org.apache.avalon.composition.event.CompositionListener;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.DeploymentModel;

import org.apache.commons.messenger.Messenger;
import org.apache.commons.messenger.MessengerManager;
import org.apache.commons.messenger.MessengerDigester;
import org.apache.commons.messagelet.model.Subscription;
import org.apache.commons.messagelet.model.SubscriptionList;

import java.io.StringReader;
import java.io.*;
import org.apache.avalon.framework.configuration.*;
import org.xml.sax.*;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

import javax.jms.MessageListener;
import org.apache.commons.messagelet.SubscriptionManager;
import javax.jms.JMSException;
import org.apache.commons.messagelet.ConsumerThread;
import java.util.HashMap;
import org.apache.commons.messagelet.model.SubscriptionDigester;


/**
 * Default MDS Manager
 * @avalon.component name="mdsManager" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.composition.event.CompositionListener"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultMDSManager.java,v 1.1 2004/04/04 15:00:59 niclas Exp $
 */
public class DefaultMDSManager
    extends AbstractLogEnabled
    implements MDSManager, CompositionListener, Contextualizable,
    Configurable, Initializable

{

  /**
   * holds Subscriptions object.  Key = ComponentModel.getQualifiedName()
   */
  private HashMap m_subscriptions = new HashMap();

  /**
   * commons-messenger SubscriptionManager
   */
  private SubscriptionManager m_subManager = null;

  /**
   * commons-messenger MessengerManager
   */
  private MessengerManager m_msgManager = null;

  /**
   * The root application model supplied during the
   * contextualization phase.
   */
  private ContainmentModel m_model;

  //---------------------------------------------------------
  // Lifecycle Methods
  //---------------------------------------------------------

  /**
   * Contextulaization of the listener by the container during
   * which we are supplied with the root composition model for
   * the application.
   *
   * @param context the supplied listener context
   * @avalon.entry key="urn:composition:containment.model"
   *    type="org.apache.avalon.composition.model.ContainmentModel"
   * @exception ContextException if a contextualization error occurs
   */
  public void contextualize(Context context) throws ContextException {
    m_model =
        (ContainmentModel) context.get(
        "urn:composition:containment.model");
  }

  /**
   * configures the MessengerManager and SubscriptionManager.  Format:
   * <pre>
   *    &lg;messenger-manager&gt;
   *      {standard messenger.xml format}
   *    &lg;/messenger-manager&gt;
   *    &lg;subscription-manager&gt;
   *      {standard subscriptions.xml format}
   *    &lg;/subscription-manager&gt;
   * </pre>
   * @param configuration
   * @throws ConfigurationException
   */
  public void configure(Configuration configuration) throws
      ConfigurationException {
    try {
      DefaultConfigurationSerializer serializer = new
          DefaultConfigurationSerializer();

      // configure messenger-manager
      Configuration msgConf = configuration.getChild("messenger-manager",true);
      StringReader reader = new StringReader(serializer.serialize(msgConf));
      MessengerDigester msgDigester = new MessengerDigester();
      m_msgManager = (MessengerManager) msgDigester.parse(reader);

      // configure subscription-manager
      Configuration subConf = configuration.getChild("subscription-manager",true);
      StringReader strReader = new StringReader(serializer.serialize(subConf));
      SubscriptionDigester subDigester = new SubscriptionDigester();
      m_subManager = (SubscriptionManager) subDigester.parse(strReader);

    }
    catch (Exception ex) {
      throw new ConfigurationException("Error configuring MDSManager.", ex);
    }
  }

  /**
   * starts the subcriber manager and processes the ContainmentModel
   */
  public void initialize() {
    m_subManager.setMessengerManager(m_msgManager);
    processModel(m_model,true);
  }


  //---------------------------------------------------------
  // Model Processing
  //---------------------------------------------------------

  private void processModel(DeploymentModel model, boolean flag) {
    if (model instanceof ContainmentModel) {
      ContainmentModel containment =
          (ContainmentModel) model;
      if (flag) {
        containment.addCompositionListener(this);
      }
      else {
        containment.removeCompositionListener(this);
      }
      DeploymentModel[] models = containment.getModels();
      for (int i = 0; i < models.length; i++) {
        processModel(models[i], flag);
      }
    }
    else if (model instanceof ComponentModel) {
      ComponentModel component = (ComponentModel) model;
      Class clazz = component.getDeploymentClass();
      if (MessageListener.class.isAssignableFrom(clazz)) {
        if (flag) {
          if (getLogger().isInfoEnabled()) {
            getLogger().info(
                "component: " + component + " is a JMS MessageListener");
          }
          subscribe(component);
        }
        else {
          if (getLogger().isInfoEnabled()) {
            getLogger().info(
                "component: " + component + " unsubscribing");
          }
          Subscription subscription = (Subscription) m_subscriptions.get(
              component.getQualifiedName());
          if (subscription != null) {
            try {
              m_subscriptions.remove(component.getQualifiedName());
              unsubscribe(subscription);
            }
            catch (Exception ex) {
              if (getLogger().isErrorEnabled()) {
                getLogger().error("component: " + component +
                                  " Error unsubscribing");
              }
            }
          }
        }
      }
    }
  }

  /**
   * create a Subscription from a ComponentModel by using the component's Configuration
   * @param model
   */
  private void subscribe(ComponentModel model) {

    try {
      Configuration conf = model.getConfiguration().getChild("subscription", true);
      String messenger = conf.getAttribute("connection", null);
      String subject = conf.getAttribute("subject", null);
      String selector = conf.getAttribute("selector", null);
      if (messenger != null && subject != null) {
        Subscription sub = new Subscription();
        sub.setConnection(messenger);
        sub.setSubject(subject);
        sub.setSelector(selector);
        sub.setConsumerThread(new ConsumerThread());
        sub.setMessageListener( (MessageListener) model.resolve());

        m_subscriptions.put(model.getQualifiedName(), sub);
        subscribe(sub);
      }
      else {
        if (getLogger().isErrorEnabled()) {
          getLogger().error("component: " + model +
              " unable to subscribe to messenger service due to bad configuration");
        }
      }
    }
    catch (Exception ex) {
    }
  }


  //---------------------------------------------------------
  // MDSManager
  //---------------------------------------------------------

  public String[] getMessengerNames() {
    return null;
  }

  public Messenger getMessenger(String name) {
    return m_msgManager.getMessenger(name);
  }

  public void removeMessenger(Messenger messenger) {
    m_msgManager.removeMessenger(messenger);
  }

  public void subscribe(Subscription subscription) throws Exception {
    m_subManager.subscribe(subscription);
  }

  public void unsubscribe(Subscription subscription) throws Exception {
    m_subManager.unsubscribe(subscription);
  }

  public SubscriptionList getSubscriptionList() {
    return m_subManager.getSubscriptionList();
  }

  //---------------------------------------------------------
  // CompositionListener
  //---------------------------------------------------------

  /**
   * Model addition.
   */
  public void modelAdded(CompositionEvent event) {
    DeploymentModel model = event.getChild();
    processModel(model, true);
  }

  /**
   * Model removal.
   */
  public void modelRemoved(CompositionEvent event) {
    DeploymentModel model = event.getChild();
    processModel(model, false);
  }

}