/*
 * EventServiceImpl.java
 *
 * Created on January 2, 2002, 8:45 PM
 */

package org.apache.metro.facility.presentationservice.impl;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.metro.facility.presentationservice.api.ChannelEvent;
import org.apache.metro.facility.presentationservice.api.ChannelException;
import org.apache.metro.facility.presentationservice.api.ChannelListener;
import org.apache.metro.facility.presentationservice.api.ControllerEventService;
import org.apache.metro.facility.presentationservice.api.ViewEventService;

/**
 * 
 * @author pmonday@stereobeacon.com
 * @version 1.0
 */
public class PresentationService implements ViewEventService,
        ControllerEventService
{

    /** Utility field used by event firing mechanism. */
    private javax.swing.event.EventListenerList listenerList = null;

    private Hashtable controlClickedListeners = new Hashtable(1);
    private Hashtable windowCreatedListeners = new Hashtable(1);
    private Hashtable modelChangedListeners = new Hashtable(1);

    private static PresentationService service = null;

    private static final boolean debug = true;

    private PresentationService() throws RemoteException
    {
    }

    public static PresentationService getEventService()
            throws RemoteException
    {
        if (service == null)
        {
            service = new PresentationService();
        }

        return service;
    }

    /**
     * Registers TemperatureChangeListener to receive events.
     * 
     * @param listener
     *            The listener to register.
     */
    public synchronized void addListener(String topic,
            ChannelListener listener, Hashtable listeners)
            throws ChannelException
    {
        if (debug)
            System.out.println("EventServiceImpl::addListener " + topic);
        Vector list = null;
        if (!listeners.containsKey(topic))
        {
            list = new Vector(1);
        } else
        {
            list = (Vector) listeners.get(topic);
            listeners.remove(topic);
        }
        list.add(listener);
        listeners.put(topic, list);
    }

    /**
     * Removes a listener from the list of listeners.
     * 
     * @param listener
     *            The listener to remove.
     */
    public synchronized void removeListener(String topic,
            ChannelListener listener, Hashtable listeners) throws ChannelException
    {
        Vector list = null;
        if (listeners.containsKey(topic))
        {
            list = (Vector) listeners.get(topic);
            listeners.remove(topic);
            list.remove(listener);
            if (list.size() > 0)
                listeners.put(topic, list);
        }
    }

    /**
     * Notifies all registered listeners about the event.
     * 
     * @param e
     *            The event to be fired
     */
    public ChannelEvent fireEvent(ChannelEvent event, Hashtable listeners)
            throws ChannelException
    {
        if (debug)
            System.out.println("EventServiceImpl::fireEvent "
                    + event.getTopic());
        Vector list = null;
        if (listeners.containsKey(event.getTopic()))
        {
            list = (Vector) listeners.get(event.getTopic());
            Enumeration e = list.elements();
            while (e.hasMoreElements())
            {
                ChannelListener tel = (ChannelListener) e
                        .nextElement();
                 return tel.notify(event);
            }
        }
        return event;
    }

    /** 
     *  window created Event Handling
     */
    public ChannelEvent windowCreated(ChannelEvent event) throws ChannelException
    {
        return fireEvent(event, windowCreatedListeners);
    }

    public void addWindowCreatedListener(String topic,
            ChannelListener listener) throws ChannelException
    {

        addListener(topic, listener, windowCreatedListeners);
    }
    
    public void removeWindowCreatedListener(String topic, 
            ChannelListener listener) 
		throws ChannelException {

        removeListener(topic, listener, windowCreatedListeners);
    }

    /** 
     *  Clicked Event Handling
     */
    public ChannelEvent controlClicked(ChannelEvent event) throws ChannelException
    {
        return fireEvent(event, controlClickedListeners);
    }

    public void addControlClickedListener(String topic,
            ChannelListener listener) throws ChannelException
    {

        addListener(topic, listener, controlClickedListeners);
    }
    
    public void removeControlClickedListener(String topic, 
            ChannelListener listener) 
		throws ChannelException {

        removeListener(topic, listener, controlClickedListeners);
    }

    /** 
     *  ModelChanged Event Handling
     */
    public void modelChanged(ChannelEvent event) throws ChannelException
    {

        fireEvent(event, modelChangedListeners);
    }

    public void addModelChangedListener(String topic,
            ChannelListener listener) throws ChannelException
    {

        addListener(topic, listener, modelChangedListeners);
    }
    
    public void removeModelChangedListener(String topic, 
            ChannelListener listener) 
		throws ChannelException {

        removeListener(topic, listener, modelChangedListeners);
    }

}
