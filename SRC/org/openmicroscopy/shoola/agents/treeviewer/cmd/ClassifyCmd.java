/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.ClassifyCmd
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;



//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.HashSet;
import java.util.Set;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
//import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.classifier.view.Classifier;
import pojos.ImageData;

/** 
* Classifies or declassifies the selected nodes depending on the specified
* mode.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/
public class ClassifyCmd
  implements ActionCmd
{

	/** Identifies the <code>Classify</code> mode. */
	public static final int CLASSIFY = Classifier.CLASSIFY_MODE;

	/** Identifies the <code>Declassify</code> mode. */
	public static final int DECLASSIFY = Classifier.DECLASSIFY_MODE;

	/** Reference to the Model. */
	private TreeViewer  viewer;

	/** One of the constants defined by this class. */
	private int         mode;

	/**
	 * Controls if the specified mode is supported.
	 * 
	 * @param m The value to control.
	 */
	private void checkMode(int m)
	{
		switch (m) {
		case CLASSIFY:
		case DECLASSIFY: 
			return;
		default:
			throw new IllegalArgumentException("Mode not supported.");
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer Reference to the model. Mustn't be <code>null</code>.
	 * @param mode  One of the constants defined by this class.
	 */
	public ClassifyCmd(TreeViewer viewer, int mode)
	{
		if (viewer == null) throw new IllegalArgumentException("No model.");
		checkMode(mode);
		this.viewer = viewer;
		this.mode = mode;
	}

	/** Brings up the classification component. */
	public void execute()
	{
		Browser browser = viewer.getSelectedBrowser();
		if (browser == null) return;
		TreeImageDisplay node = browser.getLastSelectedDisplay();
		if (node == null) return;
		Object data = node.getUserObject();
		if (data instanceof ImageData) {
			TreeImageDisplay[] nodes = browser.getSelectedDisplays();
			Set<ImageData> images = new HashSet<ImageData>(nodes.length);
			for (int i = 0; i < nodes.length; i++)
				images.add((ImageData) nodes[i].getUserObject());
			viewer.classify(images, mode);
		}
	}

}
