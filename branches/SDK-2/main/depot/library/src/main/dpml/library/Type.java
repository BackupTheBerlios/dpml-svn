/*
 * Copyright 2006 Stephen J. McConnell.
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

package dpml.library;

import java.io.File;
import java.io.IOException;

import net.dpml.transit.Artifact;

/**
 * Declaration of a type production.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Type extends Dictionary
{
   /**
    * Return the resource holding the type.
    * @return the resource
    */
    Resource getResource();
    
   /**
    * Return the type production id.
    * @return the type id
    */
    String getID();
    
   /**
    * Return the version.
    * @return the type version
    */
    String getVersion();
    
    String getName();

    String getCompoundName();
    
    String getSource();
    
    boolean getExport();
    
    boolean getTest();

   /**
    * Return the alias production flag.
    * @return the alias flag
    */
    boolean getAliasProduction();

    File getFile();
    
    File getFile( boolean local );

    Artifact getResolvedArtifact() throws IOException;
    
    Artifact getArtifact();
    
    Artifact getLinkArtifact(); // resolve option?

   /**
    * Return a filename using the layout strategy employed by the cache.
    * @return the path
    */
    String getLayoutPath();
}
