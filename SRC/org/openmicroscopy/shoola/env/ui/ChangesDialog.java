/*
 * org.openmicroscopy.shoola.env.ui.ChangesDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class ChangesDialog 
	extends JDialog
{

	/** The title of the dialog. */
	private static final String TITLE = "Saving data";
	
	/** Displayed the progress. */
	private JLabel 	status;
	
	/** Displayed the progress. */
	private JProgressBar progressBar;
	
	/** The number of tasks. */
	private int 	totalTask;
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		status = new JLabel();
		progressBar = new JProgressBar(0, totalTask);
		progressBar.setValue(0);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(progressBar);
		p.add(UIUtilities.buildComponentPanel(status));
		getContentPane().add(p, BorderLayout.CENTER);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner		The owner of the dialog.
	 * @param totalTask The number of tasks to perform.
	 */
	public ChangesDialog(JFrame owner, int totalTask)
	{
		super(owner);
		this.totalTask = totalTask;
		setProperties();
		initComponents();
		buildGUI();
		setSize(400, 300);
	}
	
	/**
	 * Sets the progress status.
	 * 
	 * @param text The text to display.
	 * @param count
	 */
	public void setStatus(String text, int count)
	{
		if (count == totalTask) {
			setVisible(false);
			dispose();
		} else {
			status.setText(text);
			progressBar.setValue(count);
		}
	}
	
}
