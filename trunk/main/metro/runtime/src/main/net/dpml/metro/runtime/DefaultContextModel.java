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

package net.dpml.metro.runtime;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.EventObject;
import java.util.Map;

import net.dpml.metro.data.Directive;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.data.ReferenceDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.EntryDescriptor;

import net.dpml.metro.model.ContextModel;
import net.dpml.metro.model.ValidationException;
import net.dpml.metro.model.ValidationException.Issue;

import net.dpml.logging.Logger;

import net.dpml.metro.part.Part;
import net.dpml.metro.part.PartException;
import net.dpml.metro.part.ContextException;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.Category;

/**
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
class DefaultContextModel extends UnicastEventSource implements ContextModel
{
    // ------------------------------------------------------------------------
    // immutable state
    // ------------------------------------------------------------------------

    private final Type m_type;
    private final ContextDirective m_directive;
    private final ClassLoader m_classloader;
    private final Map m_contextTable = 
      Collections.synchronizedMap( new HashMap() ); // (key,directive)
    
    // ------------------------------------------------------------------------
    // mutable state
    // ------------------------------------------------------------------------

    private boolean m_dirty = true;
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultContextModel( ClassLoader classloader, Type type, ContextDirective directive )
      throws ContextException, RemoteException
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
                throw new ContextException( error, e );
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
    * Validate the model.
    * @exception ValidationException if one or more issues exist within the model
    */
    public void validate() throws ValidationException
    {
        if( !m_dirty )
        {
            return;
        }
        synchronized( m_contextTable )
        {
            ArrayList list = new ArrayList();
            EntryDescriptor[] entries = getEntryDescriptors();
            for( int i=0; i<entries.length; i++ )
            {
                EntryDescriptor entry = entries[i];
                String key = entry.getKey();
                try
                {
                    Directive directive = getEntryDirective( key );
                    if( null == directive )
                    {
                        //
                        // check if the context entry is required
                        //
                    
                        if( entry.isRequired() )
                        {
                            final String message = 
                              "Required context entry ["
                              + key
                              + "] does not contain a solution directive.";
                            Issue issue = new Issue( key, message );
                            list.add( issue );
                        }
                    }
                }
                catch( UnknownKeyException e )
                {
                    // will not happen
                }
            }
            if( list.size() > 0 )
            {
                Issue[] issues = (Issue[]) list.toArray( new Issue[0] );
                throw new ValidationException( this, issues );
            }
            else
            {
                m_dirty = false;
            }
        }
    }

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
        invalidate();
    }

   /**
    * Apply an array of tagged directive as an atomic operation.  Application of 
    * directives to the context model is atomic such that changes all applied under a 
    * 'all-or-nothing' policy.
    *
    * @param references an array of part references
    * @exception UnknownKeyException if a key within the array does not match a key within
    *   the context model.
    * @exception IllegalArgumentException if a part is not castable to a Directive
    */
    public void setEntryDirectives( PartReference[] references ) throws UnknownKeyException
    {
        validatePartReferences( references );
        synchronized( getLock() )
        {
            for( int i=0; i<references.length; i++ )
            {
                PartReference ref = references[i];
                String key = ref.getKey();
                Directive directive = (Directive) ref.getPart();
                setEntryDirective( key, directive );
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    private void validatePartReferences( final PartReference[] references ) throws UnknownKeyException
    {
        int n = references.length;
        for( int i=0; i<n; i++ )
        {
            PartReference ref = references[i];
            String key = ref.getKey();
            m_type.getContextDescriptor().getEntryDescriptor( key );
            Part part = ref.getPart();
            if( ! ( part instanceof Directive ) )
            {
                final String error = 
                  "Cannot cast part reference ["
                  + ref
                  + "] to an instance of "
                  + Directive.class.getName();
                throw new IllegalArgumentException( error );
            }
        }
    }
    
    private void invalidate()
    {
        m_dirty = true;
    }
    
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

