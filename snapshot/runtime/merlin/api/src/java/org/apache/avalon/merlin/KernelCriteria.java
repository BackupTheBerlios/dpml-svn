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

package org.apache.avalon.merlin;

import java.net.URL;
import java.io.File;
import java.util.Map;

import org.apache.avalon.repository.Artifact;

/**
 * A service that provides access to versioned resources.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: KernelCriteria.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface KernelCriteria extends Map
{
   /**
    * Shared application repository root directory.
    */
    String MERLIN_REPOSITORY = 
      "merlin.repository";

   /**
    * Merlin system home.
    */
    String MERLIN_HOME = 
      "merlin.home";

   /**
    * Merlin system repository cache directory.
    */
    String MERLIN_SYSTEM = 
      "merlin.system";

   /**
    * System configuration directory.
    */
    String MERLIN_CONFIG = 
      "merlin.config";

   /**
    * Install directive path sequence.
    */
    String MERLIN_INSTALL = 
      "merlin.install";

   /**
    * A comma seperated sequence of block urls.
    */
    String MERLIN_DEPLOYMENT = 
      "merlin.deployment";

   /**
    * Merlin kernel url key.
    */
    String MERLIN_KERNEL = 
      "merlin.kernel";

   /**
    * Merlin external logging configuration file key.
    */
    String MERLIN_LOGGING_CONFIG = 
      "merlin.logging.config";

   /**
    * Merlin logging configuration key.
    */
    String MERLIN_LOGGING_IMPLEMENTATION = 
      "merlin.logging.implementation";

   /**
    * The default merlin runtime artifact spec.
    */
    String MERLIN_RUNTIME_IMPLEMENTATION = 
      "merlin.runtime.implementation";

   /**
    * The preferred runtime.
    */
    String MERLIN_RUNTIME = 
      "merlin.runtime";

   /**
    * Merlin target configuration override path.
    */
    String MERLIN_OVERRIDE = 
      "merlin.override";

   /**
    * Maerlin working directory.
    */
    String MERLIN_DIR = 
      "merlin.dir";

   /**
    * The temp directory parameter.
    */
    String MERLIN_TEMP = 
      "merlin.temp";

   /**
    * Base directory parameter.
    */
    String MERLIN_CONTEXT = 
      "merlin.context";

   /**
    * Anchor directory for extension and classpath 
    * relative references.
    */
    String MERLIN_ANCHOR = 
      "merlin.anchor";

   /**
    * Info policy parameter.
    */
    String MERLIN_INFO = 
      "merlin.info";

   /**
    * Debug policy parameter.
    */
    String MERLIN_DEBUG = 
      "merlin.debug";

   /**
    * Audit policy parameter.
    */
    String MERLIN_AUDIT = 
      "merlin.audit";

   /**
    * Proxy policy parameter.
    */
    String MERLIN_PROXY = 
      "merlin.proxy";

   /**
    * Server model parameter.
    */
    String MERLIN_SERVER = 
      "merlin.server";

   /**
    * Auto start mode.
    */
    String MERLIN_AUTOSTART = 
      "merlin.autostart";

   /**
    * Language override.
    */
    String MERLIN_LANG = 
      "merlin.lang";

   /**
    * Default component deployment timeout.
    */
    String MERLIN_DEPLOYMENT_TIMEOUT = 
      "merlin.deployment.timeout";

   /**
    * Default component deployment timeout.
    */
    String MERLIN_CODE_SECURITY_ENABLED = 
      "merlin.code.security.enabled";

   /**
    * Return the lang code.  A null value indicates that the 
    * default language applies.
    * @return the language code
    */
    String getLanguageCode();

   /**
    * Return the root directory to the shared repository.
    * @return the root common repository directory
    */
    File getRepositoryDirectory();

   /**
    * Return the root directory to the merlin installation
    * @return the merlin home directory
    */
    File getHomeDirectory();

   /**
    * Return the root directory to the merlin system repository
    * @return the merlin system repository directory
    */
    File getSystemDirectory();

   /**
    * Return the set of block URLs to be included in the root application
    * @return the block deployment urls
    */
    URL[] getDeploymentURLs();

   /**
    * Return the root directory to the merlin configurations
    * @return the merlin configuration directory
    */
    File getConfigDirectory();

   /**
    * Return the url to the kernel configuration
    * @return the kernel configuration url
    */
    URL getKernelURL();

   /**
    * Return a external logging system configuration file
    * @return the logging configuration file (possibly null)
    */
    URL getLoggingConfiguration();

   /**
    * Return the logging system implementation artifact.
    * @return the logging implementation artifact
    */
    Artifact getLoggingImplementation();

   /**
    * Return the runtime implementation artifact.
    * @return the runtime implementation artifact
    */
    Artifact getRuntimeImplementation();

   /**
    * Return the url to the configuration override targets.
    * @return the override url
    */
    String getOverridePath();

   /**
    * Return the working client directory.
    * @return the working directory
    */
    File getWorkingDirectory();

   /**
    * Return the temporary client directory.
    * @return the temp directory
    */
    File getTempDirectory();

   /**
    * Return the context directory from which relative 
    * runtrime home directories will be established for 
    * components referencing urn:avalon:home
    *
    * @return the working directory
    */
    File getContextDirectory();

   /**
    * Return the anchor directory to be used when resolving 
    * library declarations in classload specifications.
    *
    * @return the anchor directory
    */
    File getAnchorDirectory();

   /**
    * Return info generation policy.  If TRUE the parameters 
    * related to deployment will be listed on startup. 
    *
    * @return the info policy
    */
    boolean isInfoEnabled();

   /**
    * Return debug policy.  If TRUE all logging channels will be 
    * set to debug level (useful for debugging).
    *
    * @return the debug policy
    */
    boolean isDebugEnabled();

   /**
    * Return audit policy.  If TRUE a model listing will be generated.
    *
    * @return the audit policy
    */
    boolean isAuditEnabled();

   /**
    * Return proxy policy. 
    *
    * @return the proxy policy
    */
    boolean isProxyEnabled();

   /**
    * Return server execution policy.  If TRUE the kernel will 
    * continue until explicitly terminated.  If FALSE the kernel
    * will initiate decommissioning on completion of deployment.
    *
    * @return the server execution mode
    */
    boolean isServerEnabled();

   /**
    * Return the autostart policy.  If TRUE (the default) the 
    * deployment of the application container will be initiated
    * following kernel initialization.
    *
    * @return the autostart policy
    */
    boolean isAutostartEnabled();

   /**
    * Return the default deployment timeout value.
    *
    * @return the default timeout for the component deployment sequence
    */
    long getDeploymentTimeout();

   /**
    * Return the code security enabled status.
    *
    * @return TRUE if code security is enabled
    */
    boolean isSecurityEnabled();

}
