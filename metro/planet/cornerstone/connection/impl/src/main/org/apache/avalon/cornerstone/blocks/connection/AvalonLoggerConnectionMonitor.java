package org.apache.avalon.cornerstone.blocks.connection;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision: 1.8 $
 */
public class AvalonLoggerConnectionMonitor extends AbstractLogEnabled implements ConnectionMonitor {

    public void acceptingConnectionException(Class clazz, String message, IOException ioe) {
        getLogger().error(message, ioe);
    }

    public void unexpectedException(Class clazz, String message, Exception e) {
        getLogger().error(message, e);

    }

    public void shutdownSocketWarning(Class clazz, String message, IOException ioe) {
        getLogger().warn(message, ioe);
    }

    public void debugMessage(Class clazz, String message) {
        getLogger().debug(message);
    }

    public boolean isDebugEnabled(Class clazz) {
        return getLogger().isDebugEnabled();
    }
}
