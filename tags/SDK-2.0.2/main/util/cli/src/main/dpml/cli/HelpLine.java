/**
 * Copyright 2003-2004 The Apache Software Foundation
 * Copyright 2005 Stephen McConnell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dpml.cli;

import java.util.Comparator;
import java.util.Set;

/**
 * Represents a line of help for a particular Option.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface HelpLine
{
    /**
     * @return The description of the option
     */
    String getDescription();

    /**
     * @return The level of indentation for this line
     */
    int getIndent();

    /**
     * @return The Option that the help line relates to
     */
    Option getOption();

    /**
     * Builds a usage string for the option using the specified settings and
     * comparator.
     * 
     * @param helpSettings
     *            the settings to apply
     * @param comparator
     *            a comparator to sort options when applicable
     * @return the usage string
     */
    String usage( Set helpSettings, Comparator comparator );
}