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
import java.io.IOException;
import java.util.ArrayList;

import org.apache.avalon.composition.data.ExcludeDirective;
import org.apache.avalon.composition.data.IncludeDirective;

/**
 * <p>Specification of a fileset model from which a 
 * a set of included and excluded file directives can be resolved
 * into set of fully qualifed filenames for purposes of establishing
 * a classpath.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FilesetModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface FilesetModel {
    /**
     * Resolves the specified include and exclude directives from
     * the base directory anchor and produces an array of files
     * to include in the classpath.
     */
    void resolveFileset() throws IOException, IllegalStateException;
}
