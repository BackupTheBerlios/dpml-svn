/*
 * Created on 08.08.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.metro.facility.presentationservice.impl;

import org.apache.metro.facility.presentationservice.api.ControllerEventService;
import org.apache.metro.facility.presentationservice.api.ViewEventService;

/**
 * @author Andreas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class PresentationServiceFactory
{
    public static ControllerEventService getControllerEventService() throws Exception
    {
        return PresentationService.getEventService(); 
    }

    public static ViewEventService getViewEventService() throws Exception
    {
        return PresentationService.getEventService(); 
    }

}