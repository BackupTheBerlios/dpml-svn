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

package org.apache.avalon.meta.info.ant;

import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 * This is an enumeration that gives the option of either
 * outputting as xml or as a serialized format.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FormatEnum.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class FormatEnum
    extends EnumeratedAttribute
{
   /**
    * Return the external form code.
    * @return an int value corresponding to a serial or XML type enumeration
    */
    public int getTypeCode()
    {
        final String value = super.getValue();
        if( value.equals( "serial" ) )
        {
            return MetaTask.SER_TYPE;
        }
        else
        {
            return MetaTask.XML_TYPE;
        }
    }

   /**
    * Return a string reparesentation of of the enumberation.
    * @return the enumeration as a string
    */
    public String[] getValues()
    {
        return new String[]{"xml", "serial"};
    }
}
