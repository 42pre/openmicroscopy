/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;

//Java imports
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import ome.model.display.CodomainMapContext;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface provided by the renderer component. 
 * The Renderer provides a top-level window hosting the rendering controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public interface Renderer
    extends ObservableComponent
{
    
    /** 
     * Bound property name indicating to render the plane with the 
     * new rendering settings. 
     */
    public final static String  RENDER_PLANE_PROPERTY = "render_plane";
    
    /** 
     * Bound property name indicating that the contrast stretching selection
     * is available.
     */
    public final static String  CONTRAST_STRETCHING_PROPERTY = 
                                                    "contrastStretching";
    
    /** 
     * Bound property name indicating that the plane slicing selection
     * is available.
     */
    public final static String  PLANE_SLICING_PROPERTY =  "planeSlicing";
    
    /** Bound property name indicating that a new channel is selected. */
    public final static String  SELECTED_CHANNEL_PROPERTY = "selectedChannel";
    
    /** 
     * Bound property indicating that the pixels intensiy interval is 
     * modified.
     */
    public final static String  INPUT_INTERVAL_PROPERTY = "inputInterval";
    
   
    /**
     * Updates the codomain map corresponding to the specified 
     * {@link CodomainMapContext}.
     * 
     * @param ctx The codomain map context.
     */
    void updateCodomainMap(CodomainMapContext ctx);
    
    /**
     * Removes the codomain map identified by the class from the list of 
     * codomain transformations.
     * 
     * @param mapType The codomain map context type.
     */
    void removeCodomainMap(Class mapType);
    
    /**
     * Adds the codomain map identified by the class to the list of 
     * codomain transformations.
     * 
     * @param mapType The codomain map context type.
     */
    void addCodomainMap(Class mapType);
    
    /** 
     * Sets the pixels intensity interval for the
     * currently selected channel.
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     * @param released  Pass <code>true</code> to add an history item,
     * 					<code>false</code> otherwise.
     */
    void setInputInterval(double s, double e, boolean released);
    
    /** 
     * Sets the sub-interval of the device space. 
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     * @param released  Pass <code>true</code> to add an history item,
     * 					<code>false</code> otherwise.
     */
    void setCodomainInterval(int s, int e, boolean released);
    
    /**
     * Sets the bit resolution and updates the image.
     * 
     * @param v The new bit resolution.
     */
    void setBitResolution(int v);
    
    /**
     * Sets the selected channel.
     * 
     * @param c 			The new selected channel.
     * @param checkIfActive	Pass <code>true</code> to control if the selected
     * 						channel is active or not, <code>false</code>
     * 						otherwise.
     */
    void setSelectedChannel(int c, boolean checkIfActive);
    
    /**
     * Sets the family and updates the image.
     * 
     * @param family The new family value.
     */
    void setFamily(String family);
    
    /**
     * Sets the coefficient identifying a curve in the family
     * and updates the image.
     * 
     * @param k The new curve scoefficient.
     */
    void setCurveCoefficient(double k);
    
    /**
     * Sets the noise reduction flag to select the mapping algorithm
     * and updates the image.
     * 
     * @param b The noise reduction flag.
     */
    void setNoiseReduction(boolean b);
    
    /**
     * Returns the top model of this component.
     * 
     * @return See above.
     */
    ImViewer getParentModel();
    
    /**
     * Returns the <code>Codomain map context</code> corresponding to
     * the specifed class.
     * 
     * @param mapType       The class identifying the context.
     * @return See above.
     */
    CodomainMapContext getCodomainMapContext(Class mapType);

    /**
     * Set the colour of the channel button in the renderer.
     * 
     * @param changedChannel
     */
    void setChannelButtonColor(int changedChannel);
    
    /**
     * Fired if the colour model has been changed from RGB -> Greyscale or 
     * vise versa.
     */
    void setColorModelChanged();
    
    /**
     * Returns the current state.
     * 
     * @return See above
     */
    public int getState();
    
    /** Closes and disposes. */
    public void discard();

    /**
     * Returns the lower bound of the pixels intensity interval for the
     * currently selected channel.
     * 
     * @return See above.
     */
    public double getWindowStart();
    
    /**
     * Returns the upper bound of the pixels intensity interval for the
     * currently selected channel.
     * 
     * @return See above.
     */
    public double getWindowEnd();
    
    /**
     * Returns the global minimum for the currently selected channel.
     * 
     * @return See above.
     */
    public double getGlobalMin();
    
    /**
     * Returns the global maximum for the currently selected channel.
     * 
     * @return See above.
     */
    public double getGlobalMax();
    
    /**
     * Returns the global minimum for the currently selected channel.
     * 
     * @return See above.
     */
    public double getLowestValue();
    
    /**
     * Returns the global maximum for the currently selected channel.
     * 
     * @return See above.
     */
    public double getHighestValue();

    /**
     * Returns the {@link RendererUI View}.
     * 
     * @return See above.
     */
    public JComponent getUI();

    /**
     * Sets the specified rendering control.
     * 
     * @param rndControl The value to set.
     */
	public void setRenderingControl(RenderingControl rndControl);

	/** 
	 * Partially resets the rendering settings. Invoked when 
	 * selecting an image from the history.
	 */ 
	public void resetRndSettings();

}
