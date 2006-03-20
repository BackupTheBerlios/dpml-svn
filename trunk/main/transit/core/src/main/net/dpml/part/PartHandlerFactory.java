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

package net.dpml.part;

import java.net.URI;
import java.util.Hashtable;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.lang.Value;
import net.dpml.lang.Construct;


/**
 * Factory used to locate part handlers.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class PartHandlerFactory
{
    private static final StandardPartHandler HANDLER = new StandardPartHandler();
    
    private Hashtable m_map = new Hashtable();
    
    private static final PartHandlerFactory FACTORY = new PartHandlerFactory();
    
   /**
    * Return the singleton part handler factory.
    * @return the factory instance
    */
    public static PartHandlerFactory getInstance()
    {
        return FACTORY;
    }
    
    private PartHandlerFactory()
    {
    }
    
   /**
    * Locate or resolve a part handler.
    * @param uri the part uri
    * @return the part handler
    * @exception Exception if an error occurs during handler resolution
    */
    public PartHandler getPartHandler( URI uri ) throws Exception
    {
        PartDirective directive = new PartDirective( uri, null );
        return getPartHandler( directive );
    }
    
   /**
    * Locate or resolve a part handler.
    * @param directive the part instantiation directive
    * @return the part handler
    * @exception Exception if an error occurs during handler resolution
    */
    public PartHandler getPartHandler( PartDirective directive ) throws Exception
    {
        URI uri = directive.getURI();
        Value[] params = directive.getValues();
        Object[] args = Construct.getArgs( null, params, new Object[0] );
        return getPartHandler( uri, args );
    }
    
   /**
    * Locate or resolve a part handler.
    * @param uri the part uri
    * @param args part instantiation arguments
    * @return the part handler
    * @exception Exception if an error occurs during handler resolution
    */
    public PartHandler getPartHandler( URI uri, Object[] args ) throws Exception
    {
        if( DecoderFactory.LOCAL_URI.equals( uri ) )
        {
            return HANDLER;
        }
        else
        {
            synchronized( m_map )
            {
                if( m_map.containsKey( uri ) )
                {
                    return (PartHandler) m_map.get( uri );
                }
                else
                {
                    PartHandler handler = loadPartHandler( uri, args );
                    m_map.put( uri, handler );
                    return handler;
                }
            }
        }
    }
    
    private PartHandler loadPartHandler( URI uri, Object[] args ) throws Exception
    {
        ClassLoader classloader = PartHandler.class.getClassLoader();
        Repository repository = Transit.getInstance().getRepository();
        Object instance = repository.getPlugin( classloader, uri, args );
        if( instance instanceof PartHandler )
        {
            return (PartHandler) instance;
        }
        else
        {
            final String error =
              "Plugin uri does not resolve to an instance of "
              + PartHandler.class.getName()
              + "."
              + "\nURI: " + uri 
              + "\nCLass: " + instance.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }
}
