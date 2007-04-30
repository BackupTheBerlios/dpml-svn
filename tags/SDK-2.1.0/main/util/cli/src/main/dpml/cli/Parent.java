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

import java.util.ListIterator;

/**
 * An Option that can have an argument and/or group of child Options in the form 
 * "-f &lt;arg&gt; [-a|-b|-c]".
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Parent extends Option
{

    /**
     * Processes the parent part of the Option.  The combination of parent,
     * argument and children is handled by the process method.
     * @see Option#process(WriteableCommandLine, ListIterator)
     * 
     * @param commandLine the CommandLine to write results to
     * @param args a ListIterator over argument strings positioned at the next
     *             argument to process
     * @throws OptionException if an error occurs while processing
     */
    void processParent(
      WriteableCommandLine commandLine, ListIterator args )
      throws OptionException;
}
