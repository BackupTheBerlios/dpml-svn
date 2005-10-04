/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.composition.controller;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.EventObject;

import net.dpml.composition.data.Directive;
import net.dpml.composition.data.ValueDirective;
import net.dpml.composition.data.ReferenceDirective;
import net.dpml.composition.data.ContextDirective;
import net.dpml.composition.info.LifestylePolicy;
import net.dpml.composition.info.CollectionPolicy;
import net.dpml.composition.info.Type;
import net.dpml.composition.info.EntryDescriptor;
import net.dpml.composition.info.PartReference;
import net.dpml.composition.info.EntryDescriptor;

import net.dpml.composition.model.ContextModel;

import net.dpml.composition.event.EventProducer;

import net.dpml.logging.Logger;

import net.dpml.part.Part;
import net.dpml.part.ControlException;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.Plugin.Category;

/**
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultContextModel extends EventProducer implements ContextModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Type m_type;
    private final ContextDirective m_directive;
    private final ClassLoader m_classloader;
    private final HashMap m_contextTable = new HashMap(); // key (String), value (Value)
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultContextModel( ClassLoader classloader, Logger logger, Type type, ContextDirective directive )
      throws ControlException, RemoteException
    {
        super();
        
        m_directive = directive;
        m_classloader = classloader;
        m_type = type;
        
        EntryDescriptor[] entries = getEntryDescriptors();
        for( int i=0; i < entries.length; i++ )
        {
            String key = entries[i].getKey();
            try
            {
                Directive d = resolveDirective( key );
                m_contextTable.put( key, d );
            }
            catch( UnknownKeyException e )
            {
                final String error = 
                  "Component directive does not declare a directive for the key [" + key + "].";
                throw new ControlException( error, e );
            }
        }
    }

    protected void processEvent( EventObject event )
    {
    }

    // ------------------------------------------------------------------------
    // ContextModel
    // ------------------------------------------------------------------------
    
   /**
    * Return the set of context entries descriptors.
    *
    * @return context entry descriptor array
    */
    public EntryDescriptor[] getEntryDescriptors() 
    {
        return m_type.getContextDescriptor().getEntryDescriptors();
    }
    
   /**
    * Return the current directive assigned to a context entry.
    * @param key the context entry key
    * @return the directive
    */
    public Directive getEntryDirective( String key ) throws UnknownKeyException
    {
        EntryDescriptor entry = m_type.getContextDescriptor().getEntryDescriptor( key );
        if( null == entry )
        {
            throw new UnknownKeyException( key );
        }
        return (Directive) m_contextTable.get( key );
    }

   /**
    * Set a context entry directive value.
    * @param directive the context entry directive
    */
    public void setEntryDirective( String key, Directive directive ) throws UnknownKeyException
    {
        EntryDescriptor entry = m_type.getContextDescriptor().getEntryDescriptor( key );
        if( null == entry )
        {
            throw new UnknownKeyException( key );
        }
        m_contextTable.put( key, directive );
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    private Directive resolveDirective( String key ) throws UnknownKeyException
    {
        Part part = getPartDirective( key );
        if( null == part )
        {
            return null;
        }
        else if( part instanceof Directive )
        {
            return (Directive) part;
        }
        else
        {
            throw new UnsupportedOperationException( "Unsupported directive: " + part.getClass().getName() );
        }
    }
    
    private Part getPartDirective( String key ) throws UnknownKeyException
    {
        return m_directive.getPartDirective( key );
    }

}

