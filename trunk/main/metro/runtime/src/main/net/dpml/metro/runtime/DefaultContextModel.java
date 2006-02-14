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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Map;

import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Type;

import net.dpml.metro.ContextModelManager;
import net.dpml.metro.ValidationException;
import net.dpml.metro.ValidationException.Issue;
import net.dpml.metro.ContextModel;

import net.dpml.logging.Logger;

import net.dpml.part.Directive;
import net.dpml.part.ModelException;
import net.dpml.part.ModelListener;
import net.dpml.part.ModelEvent;

import net.dpml.lang.UnknownKeyException;

/**
 * Default implementation of <tt>ContextModel</tt>.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultContextModel extends UnicastEventSource implements ContextModelManager
{
    // ------------------------------------------------------------------------
    // immutable state
    // ------------------------------------------------------------------------

    private final Type m_type;
    private final ContextDirective m_directive;
    private final ClassLoader m_classloader;
    private final Map m_contextTable = 
      Collections.synchronizedMap( new HashMap() ); // (key,directive)
    private final DefaultComponentModel m_parent; 

    // ------------------------------------------------------------------------
    // mutable state
    // ------------------------------------------------------------------------

    private boolean m_dirty = true;
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new <tt>DefaultContextModel</tt>.
    * @param model the component model
    * @param classloader the classloader
    * @param type the component type
    * @param directive the context directive
    * @exception ContextException if an error occurs in context model construction
    * @exception RemoteException if a remote I/O error occurs
    */
    DefaultContextModel( 
      DefaultComponentModel parent, Logger logger, ClassLoader classloader, 
      Type type, ContextDirective directive )
      throws ModelException, RemoteException
    {
        super( logger );
        
        m_parent = parent;
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
                throw new ModelException( CompositionController.CONTROLLER_URI, error, e );
            }
        }
    }

   /**
    * Add a listener to the component model.
    * @param listener the model listener
    */
    public void addModelListener( ModelListener listener )
    {
        super.addListener( listener );
    }
    
   /**
    * Remove a listener from the component model.
    * @param listener the model listener
    */
    public void removeModelListener( ModelListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Process a context model event.
    * @param event the event to process
    */
    protected void processEvent( EventObject event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof ModelListener )
            {
                ModelListener l = (ModelListener) listener;
                if( event instanceof ModelEvent )
                {
                    try
                    {
                        ModelEvent e = (ModelEvent) event;
                        l.modelChanged( e );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "ModelListener change notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
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
                throw new ValidationException( CompositionController.CONTROLLER_URI, this, issues );
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
    * Return  a of context entry descriptor.
    *
    * @param key key
    * @return the entry descriptor 
    * @exception RemoteException if a remote exception occurs
    * @exception UnknownKeyException if the key is unknown
    */
    public EntryDescriptor getEntryDescriptor( String key ) throws UnknownKeyException
    {
        EntryDescriptor entry = m_type.getContextDescriptor().getEntryDescriptor( key );
        if( null != entry )
        {
            return entry;
        }
        else
        {
            throw new UnknownKeyException( key );
        }
    }
    
   /**
    * Return the current directive assigned to a context entry.
    * @param key the context entry key
    * @return the directive
    * @exception UnknownKeyException if the key is unknown
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
    * @exception UnknownKeyException if the key is unknown
    */
    public void setEntryDirective( String key, Directive directive ) throws UnknownKeyException
    {
        EntryDescriptor entry = m_type.getContextDescriptor().getEntryDescriptor( key );
        if( null == entry )
        {
            throw new UnknownKeyException( key );
        }
        
        Object old = m_contextTable.get( key );
        m_contextTable.put( key, directive );
        ModelEvent event = new ModelEvent( m_parent, "context.entry:" + key, old, directive );
        enqueueEvent( event );
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
                Directive directive = ref.getDirective();
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
        }
    }
    
    private void invalidate()
    {
        m_dirty = true;
    }
    
    private Directive resolveDirective( String key ) throws UnknownKeyException
    {
        return m_directive.getPartDirective( key );
    }
    
}

