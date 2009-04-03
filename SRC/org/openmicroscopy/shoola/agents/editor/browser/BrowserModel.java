 /*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.openmicroscopy.shoola.agents.editor.model.ExperimentInfo;

//Third-party libraries

//Application-internal dependencies

/** 
 *	The Browser model. 
 * The data is stored in a treeModel.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class BrowserModel
{
    
	/** Holds one of the state flags defined by {@link Browser}. */
	private int					state;
	
	/**
	 * If the file is saved to the server, this ID will be the ID of the
	 * original file on the server (not the file annotation).
	 * The Editor manages file saving etc and this ID is purely for display
	 * purposes in the Browser. 
	 */
	private long 				id;
	
    /** Reference to the component that embeds this model. */
    protected Browser           component;

    /** The model is delegated to a treeModel. */
    private TreeModel 			treeModel;
    
    /** The type of browser. */
    private int 				type;
    
    /**
     * Creates an instance. 
     * 
     * @param state	The editing state of the browser. 
     * 				One of the flags defined by the {@link Browser} interface.
     * @param type 
     */
    BrowserModel(int state, int type) 
    {
    	this.state = state;
    	this.type = type;
    }
    
    /**
     * Called by the <code>Browser</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Browser component) { this.component = component; }
    
    /**
     * Returns a reference to the treeModel
     * 
     * @return	A reference to the treeModel.
     */
    TreeModel getTreeModel() { return treeModel; }
    
    /**
     * Sets the treeModel.
     * 
     * @param model		the new TreeModel.
     */
    void setTreeModel(TreeModel model) 
    {
    	this.treeModel = model;
    }
    
    /**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link Browser} interface.  
	 */
	int getState() { return state; }  
	
	/**
     * Sets the Edited state of the Browser.
     * 
     * @param edited Pass to <code>true</code> to indicate that the file is 
     * 				 edited, <code>false</code> otherwise. 
     */
    void setEdited(boolean edited)
    {
    	
    	if (edited)  state = Browser.TREE_EDITED;
    	else state = Browser.TREE_SAVED;
    	
    	// notify listeners of changes to the model 
    	// when file saved (eg Exp-Info last-saved date)
    	if (!edited) {
    		DefaultTreeModel d = ((DefaultTreeModel) treeModel);
    		d.nodeChanged((TreeNode) d.getRoot());
    	}
    }
    
    /**
     * Determines whether a TreeModel of a protocol
	 * contains an Experimental Info object at the root node, thereby 
	 * defining it as an Experiment. 
	 * 
     * @return	see above. 
     */
    boolean isModelExperiment() 
    {
    	return ExperimentInfo.isModelExperiment(treeModel);
    }
    
    /**
     * Sets the ID so that it can be displayed in the browser.
     * 
     * @param id The value to set.
     */
    void setId(long id) { this.id = id; }
    
    /**
     * Gets the ID for display. Not used for file saving etc (handled by Editor)
     * 
     * @return		see above. 
     */
    long getId() {	return id; }
}
