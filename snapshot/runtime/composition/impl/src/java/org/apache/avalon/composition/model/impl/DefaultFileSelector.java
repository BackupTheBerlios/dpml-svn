/*
 * Copyright  2000-2004 The Apache Software Foundation
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

package org.apache.avalon.composition.model.impl;

import java.io.File;

import org.apache.avalon.composition.model.FileSelector;

public class DefaultFileSelector implements FileSelector
{
        /**
         * This file selector implementation will not filter any file
         * candidates, but instead allow all selections.
         *
         * @param basedir <code>File</code> object for the base directory
         * @param filename the name of the file to check
         * @param file <code>File</code> object for this filename
         * @return whether the file should be selected or not
         */
        public boolean isSelected(File basedir, String filename, File file) {
            return true;
        }
}
