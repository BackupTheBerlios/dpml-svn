package org.apache.avalon.mds.impl;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * @avalon.component name="test-listener"
 */
public class TestListener
    extends AbstractLogEnabled
    implements MessageListener
{
  public TestListener() {
  }

  public void onMessage(Message message) {
    try {
      getLogger().info( ( (TextMessage) message).getText());
    }
    catch (JMSException ex) {
      getLogger().error("error on message");
    }
  }
}