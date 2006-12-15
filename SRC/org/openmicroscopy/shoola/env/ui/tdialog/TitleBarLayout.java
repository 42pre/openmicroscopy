/*
 * org.openmicroscopy.shoola.env.ui.tdialog.TitleBarLayout
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

package org.openmicroscopy.shoola.env.ui.tdialog;




//Java imports
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * The {@link TitleBar}'s layout manager.
 * This class makes sure the minimum dimension of the title bar is always
 * {@link TitleBar#MIN_WIDTH}x{@link TitleBar#HEIGHT}.
 * This is possible because the title bar has a <code>null</code> UI delegate
 * and its dimensions are never set. So every call to a <code>getXXXSize</code>
 * method will eventually be answered by this class' 
 * {@link #minimumLayoutSize(Container) minimumLayoutSize} method.
 * The layout assumes the title bar has no borders.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TitleBarLayout
    implements LayoutManager
{
    
    /**
     * Returns the {@link #minimumLayoutSize(Container) minimumLayoutSize}.
     * @see LayoutManager#preferredLayoutSize(Container)
     */
    public Dimension preferredLayoutSize(Container c)  
    {
        return minimumLayoutSize(c);
    }

    /**
     * Returns the {@link TitleBar}'s minimum layout size according to the
     * {@link TitleBar#MIN_WIDTH} and {@link TitleBar#HEIGHT} constants.
     * @see LayoutManager#minimumLayoutSize(Container)
     */
    public Dimension minimumLayoutSize(Container c) 
    {
        return new Dimension(TitleBar.MIN_WIDTH, TitleBar.HEIGHT);
    }

    /**
     * Lays out the {@link TitleBar}'s components.
     * 
     * @param c The container to lay out.
     */
    public void layoutContainer(Container c) 
    {
        TitleBar titleBar = (TitleBar) c;
        titleBar.sizeButton.setBounds(
                TitleBar.H_SPACING,  //x, space from the left edge. 
                (TitleBar.HEIGHT-TitleBar.SIZE_BUTTON_DIM)/2, //y, centered.
                TitleBar.SIZE_BUTTON_DIM, //w=h, it must be a square.
                TitleBar.SIZE_BUTTON_DIM);
        titleBar.closeButton.setBounds(
                //x, next to the sizeButton. 
                2*TitleBar.H_SPACING+TitleBar.SIZE_BUTTON_DIM,  
                (TitleBar.HEIGHT-TitleBar.SIZE_BUTTON_DIM)/2, //y, centered.
                TitleBar.SIZE_BUTTON_DIM, //w=h, it must be a square.
                TitleBar.SIZE_BUTTON_DIM);
    }
    
    /**
     * No-op implementation.
     * Required by {@link LayoutManager}, but not needed here.
     * @see LayoutManager#addLayoutComponent(String, Component)
     */
    public void addLayoutComponent(String name, Component c) {}
    
    /**
     * No-op implementation.
     * Required by {@link LayoutManager}, but not needed here.
     * @see LayoutManager#removeLayoutComponent(Component)
     */
    public void removeLayoutComponent(Component c) {} 
    
}
