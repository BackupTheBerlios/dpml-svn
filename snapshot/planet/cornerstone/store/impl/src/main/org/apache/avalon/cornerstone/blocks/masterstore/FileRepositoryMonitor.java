/*
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.avalon.cornerstone.blocks.masterstore;

import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision: 1.8 $
 */
public interface FileRepositoryMonitor {
    void repositoryCreated(Class aClass, String m_name, String m_destination, String childName);
    void keyRemoved(Class aClass, String key);
    void checkingKey(Class aClass, String key);
    void returningKey(Class aClass, String key);
    void returningObjectForKey(Class aClass, Object object, String key);
    void storingObjectForKey(Class xmlFilePersistentObjectRepository, Object value, String key);
    void initialized(Class aClass);
    void pathOpened(Class aClass, String m_path);
    void unExpectedIOException(Class aClass, String message, IOException ioe);
}
