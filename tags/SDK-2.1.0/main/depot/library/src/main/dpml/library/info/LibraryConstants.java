/*
 * Copyright 2005 Stephen J. McConnell
 *
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

package dpml.library.info;

/**
 * Abstract base class for encoder and decoder..
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class LibraryConstants 
{
    static final String PART_XSD_URI = "@PART-XSD-SPEC-URI@";
    static final String MODULE_XSD_URI = "dpml:library";
    
    static final String XML_HEADER = 
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
    static final String LIBRARY_ELEMENT_NAME = "index";
    static final String IMPORTS_ELEMENT_NAME = "imports";
    static final String IMPORT_ELEMENT_NAME = "import";
    static final String MODULE_ELEMENT_NAME = "module";
    static final String MODULES_ELEMENT_NAME = "modules";
    static final String DEPENDENCIES_ELEMENT_NAME = "dependencies";
    static final String INCLUDE_ELEMENT_NAME = "include";
    static final String RESOURCE_ELEMENT_NAME = "resource";
    static final String TYPES_ELEMENT_NAME = "types";
    static final String TYPE_ELEMENT_NAME = "type";
    static final String PROJECT_ELEMENT_NAME = "project";
    static final String PROPERTIES_ELEMENT_NAME = "properties";
    
   /**
    * Abstract base class for encoder and decoder.
    */
    LibraryConstants()
    {
    }
}
