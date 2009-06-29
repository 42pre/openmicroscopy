/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.RendererModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.rnd;


//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

import pojos.ChannelData;
import pojos.PixelsData;

/** 
 * The Model component in the <code>Renderer</code> MVC triad.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class RendererModel 
{

	/** Identifies the minimum value of the device space. */
	static final int    CD_START = 0;

	/** Identifies the maximum value of the device space. */
	static final int    CD_END = 255;

	/** Flag to select a 1-bit depth (<i>=2^1-1</i>) output interval. */
	static final int   DEPTH_1BIT = RenderingControl.DEPTH_1BIT;

	/** Flag to select a 2-bit depth (<i>=2^2-1</i>) output interval. */
	static final int   DEPTH_2BIT = RenderingControl.DEPTH_2BIT;

	/** Flag to select a 3-bit depth (<i>=2^3-1</i>) output interval. */
	static final int   DEPTH_3BIT = RenderingControl.DEPTH_3BIT;

	/** Flag to select a 4-bit depth (<i>=2^4-1</i>) output interval. */
	static final int   DEPTH_4BIT = RenderingControl.DEPTH_4BIT;

	/** Flag to select a 5-bit depth (<i>=2^5-1</i>) output interval. */
	static final int   DEPTH_5BIT = RenderingControl.DEPTH_5BIT;

	/** Flag to select a 6-bit depth (<i>=2^6-1</i>) output interval. */
	static final int   DEPTH_6BIT = RenderingControl.DEPTH_6BIT;

	/** Flag to select a 7-bit depth (<i>=2^7-1</i>) output interval. */
	static final int   DEPTH_7BIT = RenderingControl.DEPTH_7BIT;

	/** Flag to select a 8-bit depth (<i>=2^8-1</i>) output interval. */
	static final int   DEPTH_8BIT = RenderingControl.DEPTH_8BIT;

	/** Identifies the <code>Linear</code> family. */
	static final String LINEAR = RenderingControl.LINEAR;

	/** Identifies the <code>Exponential</code> family. */
	static final String LOGARITHMIC = RenderingControl.LOGARITHMIC;

	/** Identifies the <code>Exponential</code> family. */
	static final String EXPONENTIAL = RenderingControl.EXPONENTIAL;

	/** Identifies the <code>Exponential</code> family. */
	static final String POLYNOMIAL = RenderingControl.POLYNOMIAL;

	/** Reference to the component that embeds this model. */
	private Renderer            component;

	/** Reference to the rendering control. */
	private RenderingControl    rndControl;

	/** The current state of the component. */
	private int                 state;

	/** The index of the selected channel. */
	private int                 selectedChannelIndex;

	/** Flag to denote if the widget is visible or not. */
	private boolean             visible;
     
    /** The index of the rendering. */
    private int					rndIndex;
    
    private List<ChannelData>	sortedChannel;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param rndControl    Reference to the component that controls the
	 *                      rendering settings. Mustn't be <code>null</code>.
	 * @param rndIndex		The index associated to the renderer.
	 */
	RendererModel(RenderingControl rndControl, int rndIndex)
	{
		if (rndControl == null)
			throw new NullPointerException("No rendering control.");
		this.rndControl = rndControl;
		this.rndIndex = rndIndex;
		visible = false;
	}
	
	/**
	 * Returns the index of a channel or <code>-1</code>.
	 * 
	 * @return See above.
	 */
	int createSelectedChannel()
	{
		//Set the selected channel
		List<Integer> active = getActiveChannels();
		List<ChannelData> list = getChannelData();
		Iterator<ChannelData> i = list.iterator();
		ChannelData channel;
		int index;
		int setIndex = -1;
		while (i.hasNext()) {
			channel = i.next();
			index = channel.getIndex();
			if (active.contains(index) && setIndex < 0) {
				setIndex = index;
				break;
			}
		}
		return setIndex;
	}
	
	/**
	 * Gets the colour of the channel from the model. 
	 * 
	 * @param index Channel.
	 *  
	 * @return Color of the channel specified by index.
	 */
	Color getChannelColor(int index)
	{
		return rndControl.getRGBA(index);
	}

	/**
	 * Returns the status of the window.
	 * 
	 * @return See above.
	 */
	boolean isVisible() { return visible; }

	/**
	 * Called by the <code>Renderer</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(Renderer component) { this.component = component; }

	/** Discard component. */
	void discard() 
	{
		//state = Renderer.DISCARDED;
	}

	/**
	 * Returns the state of the component.
	 * 
	 * @return See above.
	 */
	int getState() { return state; }

	/**
	 * Sets the pixels intensity interval for the specified channel.
	 * 
	 * @param index The index of the channel.
	 * @param start	The lower bound of the interval.
	 * @param end	The upper bound of the interval.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setInputInterval(int index, double start, double end)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setChannelWindow(index, start, end); 
	}

	/**
	 * Returns the upper bound of the sub-interval of the device space.
	 * 
	 * @return See above.
	 */
	int getCodomainEnd()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getCodomainEnd();
	}

	/**
	 * Returns the lower bound of the sub-interval of the device space.
	 * 
	 * @return See above.
	 */
	int getCodomainStart()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getCodomainStart();
	}

	/**
	 * Sets the sub-interval of the device space. 
	 * 
	 * @param s The lower bound of the interval.
	 * @param e The upper bound of the interval.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setCodomainInterval(int s, int e)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setCodomainInterval(s, e);
	}

	/**
	 * Sets the quantum strategy.
	 * 
	 * @param v The bit resolution defining the strategy.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setBitResolution(int v)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setQuantumStrategy(v);
	}

	/**
	 * Sets the selected channel.
	 * 
	 * @param index The index of the selected channel.
	 */
	void setSelectedChannel(int index) { selectedChannelIndex = index; }

	/**
	 * Sets, for the currently selected channel, the family used during 
	 * the mapping process.
	 * 
	 * @param family The family to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setFamily(String family)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		boolean b = rndControl.getChannelNoiseReduction(selectedChannelIndex);
		double k = rndControl.getChannelCurveCoefficient(selectedChannelIndex);
		rndControl.setQuantizationMap(selectedChannelIndex, family, k, b);
	}

	/**
	 * Selects one curve in the family.
	 * 
	 * @param k The coefficient identifying a curve within a family.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setCurveCoefficient(double k)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		boolean b = rndControl.getChannelNoiseReduction(selectedChannelIndex);
		String family = rndControl.getChannelFamily(selectedChannelIndex);
		rndControl.setQuantizationMap(selectedChannelIndex, family, k, b);
	} 

	/**
	 * Turns on and off the noise reduction algortihm mapping.
	 * 
	 * @param b Pass <code>true</code>  to turn it on,
	 *          <code>false</code> otherwise.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setNoiseReduction(boolean b)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		String family = rndControl.getChannelFamily(selectedChannelIndex);
		double k = rndControl.getChannelCurveCoefficient(selectedChannelIndex);
		rndControl.setQuantizationMap(selectedChannelIndex, family, k, b);
	}

	/**
	 * Upates the specified {@link CodomainMapContext context}.
	 * 
	 * @param ctx The context to update.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	/*
	void updateCodomainMap(CodomainMapContext ctx)
	throws RenderingServiceException, DSOutOfServiceException
	{
		rndControl.updateCodomainMap(ctx);
	}
	*/

	/**
	 * Returns the codomain map context corresponding to the specified 
	 * <code>codomain</code> class. Returns <code>null</code> if there is no
	 * context matching the class.
	 * 
	 * @param mapType The class corresponding to the context to retrieve.
	 * @return See above.
	 */
	/*
	CodomainMapContext getCodomainMap(Class mapType)
	{
		List maps = getCodomainMaps();
		Iterator i = maps.iterator();
		CodomainMapContext ctx;
		while (i.hasNext()) {
			ctx = (CodomainMapContext) i.next();
			if (ctx.getClass().equals(mapType)) return ctx;
		}
		return null;
	}
	*/

	/**
	 * Returns a read-only list of {@link CodomainMapContext}s using during
	 * the mapping process in the device space.
	 * 
	 * @return See above.
	 */
	List getCodomainMaps()
	{ 
		if (rndControl == null) return null;
		return rndControl.getCodomainMaps();
	}

	/**
	 * Removes the codomain map identified by the class from the chain of 
	 * codomain transformations.
	 * 
	 * @param mapType   The type to identify the codomain map.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	/*
	void removeCodomainMap(Class mapType)
	throws RenderingServiceException, DSOutOfServiceException
	{
		CodomainMapContext ctx = getCodomainMap(mapType);
		if (ctx != null) rndControl.removeCodomainMap(ctx);
	}
	*/

	/**
	 * Adds the codomain map identified by the class to the chain of 
	 * codomain transformations.
	 * 
	 * @param mapType   The type to identify the codomain map.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	/*
	void addCodomainMap(Class mapType)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (mapType.equals(ReverseIntensityContext.class)) {
			ReverseIntensityContext riCtx = new ReverseIntensityContext();
			riCtx.setReverse(Boolean.TRUE);
			rndControl.addCodomainMap(riCtx);
		} else if (mapType.equals(PlaneSlicingContext.class)) {

		} else if (mapType.equals(ContrastStretchingContext.class)) {

		}
	}
	*/

	/** 
	 * Returns the index of the currently selected channel. 
	 * 
	 * @return See above.
	 */
	int getSelectedChannel() { return selectedChannelIndex; }

	/**
	 * Returns a list of available mapping families.
	 * 
	 * @return See above.
	 */
	List getFamilies()
	{ 
		if (rndControl == null) return null;
		return rndControl.getFamilies();
	}

	/**
	 * Returns the mapping family used for to map the selected channel.
	 * 
	 * @return See above.
	 */
	String getFamily()
	{
		if (rndControl == null) return null;
		return rndControl.getChannelFamily(selectedChannelIndex);
	}

	/**
	 * Returns the map selected in the family for the selected channel.
	 * 
	 * @return See above.
	 */
	double getCurveCoefficient()
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelCurveCoefficient(selectedChannelIndex);
	}

	/**
	 * Returns the bit resolution value.
	 * 
	 * @return See above.
	 */
	int getBitResolution()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getBitResolution();
	}

	/**
	 * Returns <code>true</code> if the noise reduction flag is turned on 
	 * for the selected channel, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isNoiseReduction()
	{
		if (rndControl == null) return false;
		return rndControl.getChannelNoiseReduction(selectedChannelIndex);
	}

	/**
	 * Returns a list of <code>Channel Data</code> objects.
	 * 
	 * @return See above.
	 */
	List<ChannelData> getChannelData()
	{
		if (rndControl == null) return null;
		if (sortedChannel == null) {
			ChannelData[] data = rndControl.getChannelData();
			ViewerSorter sorter = new ViewerSorter();
			List l = sorter.sort(data);
			sortedChannel = Collections.unmodifiableList(l);
		}
		return sortedChannel;
	}

	/**
	 * Returns the global minimum of the currently selected channel.
	 * 
	 * @return See above.
	 */
	double getGlobalMin()
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelData(selectedChannelIndex).getGlobalMin();
	}

	/**
	 * Returns the global maximum of the currently selected channel.
	 * 
	 * @return See above.
	 */
	double getGlobalMax()
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelData(selectedChannelIndex).getGlobalMax();
	}

	/**
	 * Returns the lowest possible value.
	 * 
	 * @return See above.
	 */
	double getLowestValue()
	{
		if (rndControl == null) return -1;
		return rndControl.getPixelsTypeLowerBound(selectedChannelIndex);
	}

	/**
	 * Returns the highest possible value.
	 * 
	 * @return See above.
	 */
	double getHighestValue()
	{
		if (rndControl == null) return -1;
		return rndControl.getPixelsTypeUpperBound(selectedChannelIndex);
	}

	/**
	 * Returns the lower bound of the pixels intensity interval of the 
	 * currently selected channel.
	 * 
	 * @return See above.
	 */
	double getWindowStart()
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelWindowStart(selectedChannelIndex);
	}

	/**
	 * Returns the upper bound of the pixels intensity interval of the 
	 * currently selected channel.
	 * 
	 * @return See above.
	 */
	double getWindowEnd()
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelWindowEnd(selectedChannelIndex);
	}

	/**
	 * Returns <code>true</code> if the grey scale is selected,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isGreyScale()
	{
		if (rndControl == null) return false;
		return rndControl.getModel().equals(RenderingControl.GREY_SCALE);
	}
	
	/** 
	 * Saves the rendering settings. 
	 * 
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void saveRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.saveCurrentSettings(); 
	}

	/**
	 * Returns <code>true</code> if the channel is mapped, <code>false</code>
	 * otherwise.
	 * 
	 * @param w	The channel's index.
	 * @return See above.
	 */
	boolean isChannelActive(int w)
	{ 
		if (rndControl == null) return false;
		return rndControl.isActive(w);
	}

	/**
	 * Returns a list of active channels.
	 * 
	 * @return See above.
	 */
	List<Integer> getActiveChannels()
	{
		if (rndControl == null) return null;
		List<Integer> active = new ArrayList<Integer>();
		for (int i = 0; i < getMaxC(); i++) {
			if (rndControl.isActive(i)) active.add(Integer.valueOf(i));
		}
		return active;
	}

	/**
	 * Returns the number of channels.
	 * 
	 * @return See above.
	 */
	int getMaxC() { return rndControl.getPixelsDimensionsC(); }
	
	/**
	 * Returns the index associated to the renderer.
	 * 
	 * @return See above.
	 */
	int getRndIndex() { return rndIndex; }

	/**
	 * Returns <code>true</code> if the renderer is for a general perspective
	 * or for a specific view.
	 * 
	 * @return See above.
	 */
	boolean isGeneralIndex()
	{ 
		return getRndIndex() == MetadataViewer.RND_GENERAL;
	}
	
	/**
	 * Sets the color for the specified channel.
	 * 
	 * @param index The channel's index.
	 * @param color The color to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setChannelColor(int index, Color color)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setRGBA(index, color);
	}

	/**
	 * Returns the color model.
	 * 
	 * @return See above.
	 */
	String getColorModel()
	{
		if (rndControl == null) return null;
		return rndControl.getModel();
	}
	
	/**
	 * Sets the color model.
	 * 
	 * @param colorModel	The color model to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setColorModel(String colorModel)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setModel(colorModel);
	}
	
	/**
	 * Returns the number of pixels along the X-axis.
	 * 
	 * @return See above.
	 */
	int getMaxX()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsX(); 
	}
	
	/**
	 * Returns the number of pixels along the Y-axis.
	 * 
	 * @return See above.
	 */
	int getMaxY()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsY(); 
	}
	
	/**
	 * Returns the maximum number of z-sections.
	 * 
	 * @return See above.
	 */
	int getMaxZ()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsZ();
	}

	/**
	 * Returns the maximum number of timepoints.
	 * 
	 * @return See above.
	 */
	int getMaxT()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsT(); 
	}
	
	/**
	 * Returns the currently selected z-section.
	 * 
	 * @return See above.
	 */
	int getDefaultZ()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getDefaultZ(); 
	}

	/**
	 * Returns the currently selected timepoint.
	 * 
	 * @return See above.
	 */
	int getDefaultT()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getDefaultT();
	}
	
	/**
	 * Sets the selected plane.
	 * 
	 * @param z The z-section to set.
	 * @param t The timepoint to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setSelectedXYPlane(int z, int t)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		if (t >= 0 && t != getDefaultT()) rndControl.setDefaultT(t);
		if (z >= 0 && z != getDefaultZ()) rndControl.setDefaultZ(z);
	}

	/**
	 * Turns on or off the specified channel.
	 * 
	 * @param index 	The index of the channel.
	 * @param active	Pass <code>true</code> to turn the channel on,
	 * 					<code>false</code> to turn in off.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setChannelActive(int index, boolean active)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setActive(index, active);
	}

	/**
	 * Returns the compression level.
	 * 
	 * @return See above.
	 */
	int getCompressionLevel()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getCompressionLevel();
	}

	/**
	 * Returns the physical size of a pixels along the Y-axis.
	 * 
	 * @return See above.
	 */
	double getPixelsSizeY()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsPhysicalSizeY(); 
	}
	
	/**
	 * Returns the physical size of a pixels along the X-axis.
	 * 
	 * @return See above.
	 */
	double getPixelsSizeX()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsPhysicalSizeX(); 
	}
	
	/**
	 * Returns the physical size of a pixels along the Z-axis.
	 * 
	 * @return See above.
	 */
	double getPixelsSizeZ()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsPhysicalSizeZ(); 
	}

    /**
     * Returns a copy of the current rendering settings.
     * 
     * @return See above.
     */
	RndProxyDef getRndSettingsCopy()
	{ 
		if (rndControl == null) return null;
		return rndControl.getRndSettingsCopy();
	}

	/**
	 * Returns <code>true</code> if an active channel 
	 * is mapped to <code>Red</code> if the band is <code>0</code>,
	 * <code>Red</code> if the band is <code>0</code>, 
	 * <code>Red</code> if the band is <code>0</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @param band Pass <code>0</code> for <code>Red</code>, 
	 * 			   <code>1</code> for <code>Green</code>,
	 * 			   <code>2</code> for <code>Blue</code>.
	 * @return See above
	 */
	boolean hasActiveChannel(int band)
	{
		if (rndControl == null) return false;
		switch (band) {
			case 0: rndControl.hasActiveChannelRed();
			case 1: rndControl.hasActiveChannelGreen();
			case 2: rndControl.hasActiveChannelBlue();
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the compression is turned on,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isCompressed()
	{
		if (rndControl == null) return false;
		return rndControl.isCompressed();
	}

	/**
	 * Returns <code>true</code> if the specified channel 
	 * is mapped to <code>Red</code> if the band is <code>0</code>,
	 * <code>Red</code> if the band is <code>0</code>, 
	 * <code>Red</code> if the band is <code>0</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @param band Pass <code>0</code> for <code>Red</code>, 
	 * 			   <code>1</code> for <code>Green</code>,
	 * 			   <code>2</code> for <code>Blue</code>.
	 * @param index The index of the channel.
	 * @return See above
	 */
	boolean isColorComponent(int band, int index)
	{
		if (rndControl == null) return false;
		switch (band) {
			case 0: rndControl.isChannelRed(index);
			case 1: rndControl.isChannelGreen(index);
			case 2: rndControl.isChannelBlue(index);
		}
		return false;
	}

	/**
	 * Resets the default settings.
	 * 
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void resetDefaults()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.resetDefaults();
		
	}

	/**
	 * Resets the passed rendering settings.
	 * 
	 * @param settings The rendering settings to reset.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void resetSettings(RndProxyDef settings)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.resetSettings(settings);
	}

	/**
	 * Saves the current settings.
	 *  
	 * @return See above.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	RndProxyDef saveCurrentSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return null;
		return rndControl.saveCurrentSettings();
	}

	/**
	 * Turns on or off the specified channel.
	 * 
	 * @param index  The index of the channel
	 * @param active Pass <code>true</code> to turn the channel on, 
	 * 				 <code>false</code> to turn it off.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setActive(int index, boolean active)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setActive(index, active);
	}

	/**
	 * Sets the compression level.
	 * 
	 * @param compression  The compression level.
	 */
	void setCompression(int compression)
	{
		if (rndControl == null) return;
		rndControl.setCompression(compression);
	}

	/**
	 * Sets the original settings.
	 * 
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setOriginalRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setOriginalRndSettings();
	}

	/**
	 * Checks if the passed set of pixels is compatible.
	 * Returns <code>true</code> if the pixels set is compatible,
	 * <code>false</code> otherwise.
	 * 
	 * @param pixels The pixels to check.
	 * @return See above.
	 */
	boolean validatePixels(PixelsData pixels)
	{
		return rndControl.validatePixels(pixels);
	}

	/**
	 * Renders the specified plane.
	 * 
	 * @param pDef The plane to render.
	 * @return See above.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	BufferedImage renderPlane(PlaneDef pDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		return rndControl.renderPlane(pDef);
	}
	
}
