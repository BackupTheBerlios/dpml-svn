
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

package org.apache.avalon.mds;

import javax.jms.JMSException;

import org.apache.commons.messenger.Messenger;
import org.apache.commons.messagelet.model.SubscriptionList;
import org.apache.commons.messagelet.model.Subscription;

/**
 * Message Driven Service Manager Definition.
 * Provides a facade for both the MessengerManager and SubscriptionManager from
 * the Jakarta commons-messenger project.
 * @avalon.service version="1.0"
 */
public interface MDSManager {

  // MessengerManager facade

  public String[] getMessengerNames() throws JMSException;

  public Messenger getMessenger(String name) throws JMSException;

  public void removeMessenger(Messenger messenger);


  // SubscriptionManager facade

  public void subscribe(Subscription subscription) throws Exception;

  public void unsubscribe(Subscription subscription) throws Exception;

  public SubscriptionList getSubscriptionList();

}
