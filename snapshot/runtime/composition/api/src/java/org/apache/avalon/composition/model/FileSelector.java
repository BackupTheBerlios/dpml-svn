/*
 * Copyright  2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.avalon.composition.model;

import java.io.File;


/**
 * Interface used by implementations to filter files from candidate lists.
 * 
 * @author Apache Ant Development Team (Bruce Atherton)
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FileSelector.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface FileSelector {

    /**
     * Method that each selector will implement to create their
     * selection behaviour.
     *
     * @param basedir A java.io.File object for the base directory
     * @param filename The name of the file to check
     * @param file A File object for this filename
     * @return whether the file should be selected or not
     */
    boolean isSelected(File basedir, String filename, File file);

}
