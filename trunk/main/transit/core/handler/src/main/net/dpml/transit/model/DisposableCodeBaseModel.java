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

package net.dpml.transit.model;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.EventObject;
import java.util.EventListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.store.CodeBaseStorage;

/**
 * The DisposableCodeBaseModel is codebase model that is disposable.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public abstract class DisposableCodeBaseModel extends DefaultCodeBaseModel implements Disposable
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new disposable codebase model.
    * @param logger the assigned logging channel
    * @param uri the codebase uri
    */
    public DisposableCodeBaseModel( Logger logger, URI uri )
      throws RemoteException
    {
        super( logger, uri );
    }

   /**
    * Creation of a new disposable codebase model using a supplied storage unit.
    * @param logger the assigned logging channel
    * @param home the codebase storage unit
    */
    public DisposableCodeBaseModel( Logger logger, CodeBaseStorage home )
      throws RemoteException
    {
        super( logger, home );
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

   /**
    * Add a disposal listener to the model.
    * @param listener the listener to add
    */
    public void addDisposalListener( DisposalListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a disposal listener from the model.
    * @param listener the listener to remove
    */
    public void removeDisposalListener( DisposalListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

   /**
    * Dispose of the disposable codebase model
    */
    public void dispose() throws RemoteException
    {
        VetoableDisposalEvent veto = new VetoableDisposalEvent( this );
        enqueueEvent( veto, false  );
        DisposalEvent disposal = new DisposalEvent( this );
        enqueueEvent( disposal, false );
    }

    //----------------------------------------------------------------------
    // impl
    //----------------------------------------------------------------------

    protected void processEvent( EventObject event )
    {
        if( event instanceof DisposalEvent )
        {
            processDisposalEvent( (DisposalEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processDisposalEvent( DisposalEvent eventObject )
    {
        DisposalEvent event = (DisposalEvent) eventObject;
        EventListener[] listeners = listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof DisposalListener )
            {
                DisposalListener pl = (DisposalListener) listener;
                if( event instanceof VetoableDisposalEvent )
                {
                    try
                    {
                        pl.disposing( event );
                    }
                    catch( RemoteException e )
                    {
                        final String error =
                          "Disposal listener remote notification error."; 
                        getLogger().error( error, e );
                    }
                }
                else
                {
                    try
                    {
                        pl.disposed( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "Disposal listener notification error."; 
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

    private static class VetoableDisposalEvent extends DisposalEvent
    {
        public VetoableDisposalEvent( CodeBaseModel source )
        {
            super( source );
        }
    }
}

