/*
 * org.openmicroscopy.shoola.agents.iviewer.ImViewerAgent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.measurement.MeasurementToolLoaded;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;

/** 
 * The ImViewer agent. This agent displays an <code>Image</code> and the 
 * controls to modify the rendering settings.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">
 *              donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ImViewerAgent
    implements Agent, AgentEventListener
{

    /** The default error message. */
    public static final String ERROR = " An error occured while modifying  " +
    		"the rendering settings.";
    
    /** Reference to the registry. */
    private static Registry         registry; 
    
    /**
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }
    
    /**
     * Handles the {@link ViewImage} event.
     * 
     * @param evt The event to handle.
     */
    private void handleViewImage(ViewImage evt)
    {
        if (evt == null) return;
        ImViewer view = ImViewerFactory.getImageViewer(evt.getPixelsID(), 
                                        evt.getImageID(), evt.getName(),
                                        evt.getRequesterBounds());
        if (view != null) view.activate();
    }
    
    /**
     * Handles the {@link MeasurementToolLoaded} event.
     * 
     * @param evt The event to handle.
     */
    private void handleMeasurementToolLoaded(MeasurementToolLoaded evt)
    {
    	if (evt == null) return;
    	MeasurementTool request = (MeasurementTool) evt.getACT();
    	long pixelsID = request.getPixelsID();
    	ImViewer view = ImViewerFactory.getImageViewer(pixelsID);
    	if (view != null) {
    		switch (evt.getIndex()) {
				case MeasurementToolLoaded.ADD:
					view.addView(evt.getView());
					break;
				case MeasurementToolLoaded.REMOVE:
					view.removeView(evt.getView());
			}
    	}
    }
    
    /** Creates a new instance. */
    public ImViewerAgent() {}
    
    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate()
     */
    public void activate() {}

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#terminate()
     */
    public void terminate() {}

    /** 
     * Implemented as specified by {@link Agent}. 
     * @see Agent#setContext(Registry)
     */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        EventBus bus = registry.getEventBus();
        bus.register(this, ViewImage.class);
        bus.register(this, MeasurementToolLoaded.class);
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate() { return true; }

    /**
     * Responds to an event fired trigger on the bus.
     * Listens to ViewImage event.
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof ViewImage) handleViewImage((ViewImage) e);
        else if (e instanceof MeasurementToolLoaded)
        	handleMeasurementToolLoaded((MeasurementToolLoaded) e);
    }

}
