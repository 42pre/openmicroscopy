/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.Browser
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.browser;



//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;


/** 
 * Defines the interface provided by the browser component.
 * The Viewer provides a UI component to display the rendered image.
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
public interface Browser
    extends ObservableComponent
{
    
    /**
     * Returns the widget that displays the image.
     *  
     * @return The viewer widget.
     */
    public JComponent getUI();
    
    /**
     * Sets the original rendered image.
     * 
     * @param image The buffered image.
     */
    public void setRenderedImage(BufferedImage image);
    
    /**
     * Returns the image displayed on screen.
     * 
     * @return See above.
     */
    public BufferedImage getDisplayedImage();
    
    /**
     * Removes the specified component from the layered pane hosting 
     * the image.
     * 
     * @param c The component to remove.
     */
    public void removeComponent(JComponent c);
    
    /**
     * Adds the specified component to the layered pane hosting
     * the image.
     * 
     * @param c The component to add.
     */
    public void addComponent(JComponent c);
    
    /**
     * Sets the zoom factor.
     * 
     * @param factor    The zoom factor to set.
     */
    public void setZoomFactor(double factor);
    
    /**
     * Gets the zoom factor.
     * 
     * @return The zoom factor to get.
     */
    public double getZoomFactor();
    
    /**
     * Returns the name of the Browser.
     * 
     * @return See above.
     */
    public String getTitle();
    
    /**
     * Returns the newly created lens image. The magnification level of the
     * lens image depends on the specified factor.
     * 
     * @param lensFactor    The magnification of the lens image.
     * @return See above.
     */
    public BufferedImage getLensImage(double lensFactor);
    
    /**
     * Sets the size of the components composing the display.
     * 
     * @param w The width to set.
     * @param h The height to set.
     */
    public void setComponentsSize(int w, int h);

    /**
     * Displays if the passed value is <code>true</code>, doesn't display it
     * if if the passed value is <code>false</code>.
     * 
     * @param b Pass <code>true</code> to display the unit bar, 
     *          <code>false</code> otherwise.
     */
    public void setUnitBar(boolean b);

    /**
     * Sets the size of the unit bar in microns.
     * 
     * @param size The size of the unit bar in microns.
     */
    public void setUnitBarSize(double size);

    /**
     * Returns <code>true</code> if the unit bar is displayed, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isUnitBar();

    /**
     * Returns the value (with two decimals) of the unit bar or 
     * <code>null</code> if the actual value is <i>negative</i>.
     * 
     * @return See above.
     */
    public String getUnitBarValue();

    /**
     * Returns the size of the unit bar.
     * 
     * @return See above.
     */
    public double getUnitBarSize();
    
    /**
     * Returns the color of the unit bar.
     * 
     * @return See above.
     */
    public Color getUnitBarColor();

    /**
     * Sets the color of the unit bar.
     * 
     * @param color The value to set.
     */
	public void setUnitBarColor(Color color);
    
}
