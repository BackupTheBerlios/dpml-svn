/*
 * Copyright 2004 Niclas Hedman.
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

package net.dpml.http.spi;
 
import java.util.Map;

/**
 * Mime types registry.
 */
public interface MimeTypes
{
   /**
    * Get the MIME types extensions map.
    * @return the extensions map
    */
    Map getExtensionMap();
    
   /**
    * Get the mime type matching the supplied extension.
    * @param extension the extension
    * @return the mime type
    */
    String getMimeType( String extension );
    
   /**
    * Return the registered extensions as a String array.
    * @param mimetype the mimetype
    * @return the extensions
    */
    String[] getExtensions( String mimetype );
    
}