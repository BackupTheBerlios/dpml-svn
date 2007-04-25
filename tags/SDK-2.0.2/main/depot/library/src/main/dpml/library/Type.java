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
    
   /**
    * Return the type name.
    * @return the type name
    */
    String getName();

   /**
    * Return the compound name.
    * @return the compound name
    */
    String getCompoundName();
    
   /**
    * Return the source for type (may be null).
    * @return the type source attribute value
    */
    String getSource();
    
   /**
    * Return TRUE if the type is exported.
    * @return the export policy
    */
    boolean getExport();
    
   /**
    * Return TRUE if the type is a test type.
    * @return the test policy
    */
    boolean getTest();

   /**
    * Return the alias production flag.
    * @return the alias flag
    */
    boolean getAliasProduction();

   /**
    * Return a file reference for the cache type.
    * @return the file representing the produced artifact type
    */
    File getFile();
    
   /**
    * Return a file reference for the type.
    * @param local if true return the local deliverable file otherwise return the cached file
    * @return the file representing the produced artifact type
    */
    File getFile( boolean local );

   /**
    * Return the artifact for the type ensuring that the artifact is fully resolved.
    * @return the resolved artifact
    * @exception IOException if an IO error occurs
    */
    Artifact getResolvedArtifact() throws IOException;
    
   /**
    * Return the artifact for the type.
    * @return the artifact
    */
    Artifact getArtifact();
    
   /**
    * Return the link artifact for the type.
    * @return the link artifact
    */
    Artifact getLinkArtifact();

   /**
    * Return a filename using the layout strategy employed by the cache.
    * @return the path
    */
    String getLayoutPath();
}
