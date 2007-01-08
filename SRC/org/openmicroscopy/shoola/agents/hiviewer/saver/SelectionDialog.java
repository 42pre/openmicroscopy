/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.SelectionDialog
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

package org.openmicroscopy.shoola.agents.hiviewer.saver;




//Java imports
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.OptionsDialog;

/** 
 * Dialog window used to ask a Yes/No question to the user.
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
class SelectionDialog
    extends OptionsDialog
{
   
    /** Notification message. */
    private static final String MESSAGE = "A file with the same name and " +
                                            "extension already exists in " +
                                             "this directory. Do you " +
                                             "really want to save the image?";
    
    /** Reference to the model. */
    private ContainerSaver model;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param messageIcon The icon displayed in the window.
     */
    SelectionDialog(ContainerSaver model, Icon messageIcon) 
    {
        super(model, "Save images", MESSAGE, messageIcon);
        if (model == null) throw new IllegalArgumentException("No model");
        this.model = model;
    }
 
    /** Overrides the {@link #onYesSelection() onYesSelection} method. */
    protected void onYesSelection()
    {  
        setVisible(false);
        dispose();
        model.showPreview();
    }

}
