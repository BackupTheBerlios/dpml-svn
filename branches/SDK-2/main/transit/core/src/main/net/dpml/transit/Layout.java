/*
 * Copyright 2006 Stephen McConnell
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

package net.dpml.transit;

import java.util.Map;
import java.util.Hashtable;
import java.util.ServiceLoader;

/**
 * Definition of a repository layout.  Specialized layouts must extend from
 * this base abstract class.  The class includes static methods supporting 
 * the resolution of available layouts.  Each layout instance is associated with
 * a unique identifier and represents the mapping between a layout strategy
 * name and the physical structural representation of a repository.  Operations
 * on the layout class support the translation of artifact addresses to their
 * corresponding physicaly layout on remote systems.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class Layout
{
    private static final Map<String, Layout> LAYOUTS = new Hashtable<String, Layout>();
    static
    {
        ServiceLoader<Layout> loaders = ServiceLoader.load( Layout.class );
        for( Layout layout : loaders )
        {
            String key = layout.getID();
            if( !LAYOUTS.containsKey( key ) )
            {
                LAYOUTS.put( key, layout );
            }
        }
    }
    
   /**
    * Return a location resolver capable for supporting the supplied id. If
    * a handler is available the handler is returned otherwise the returned
    * value is null.
    *
    * @param id the layout identifier
    * @return the location resolver or null if not available
    */
    public static Layout getLayout( final String id )
    {
        return LAYOUTS.get( id );
    }

    /**
     * Return the layout identifier.  The id value is used
     * to identify layout instances assigned to cache handlers and 
     * resource host handlers.
     *
     * @return the layout id
     */
    public abstract String getID();
    
   /**
    * Return the base path for an artifact.  The base path is the location
    * where the file will be found. The base + "/" filename is equal to the
    * full path.
    *
    * @param artifact the Artifact to resolve.
    * @return the base path
    */
    public abstract String resolveBase( Artifact artifact );

    /**
     * Returns the full path of the artifact relative to a logical root directory.
     * The base + "/" filename is equal to the full path.
     *
     * @see #resolveBase
     * @see #resolveFilename
     * @param artifact the Artifact to resolve.
     * @return the logical artifact path
     */
    public abstract String resolvePath( Artifact artifact );

    /**
     * Return the filename for an artifact.  The base + "/" filename is equal
     * to the full path.
     *
     * @see #resolveBase
     * @see #resolveFilename
     * @param artifact the Artifact to resolve.
     * @return the logical artifact path
     */
    public abstract String resolveFilename( Artifact artifact );
}
