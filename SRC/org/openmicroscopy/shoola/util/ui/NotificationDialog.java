/*
 * org.openmicroscopy.shoola.util.ui.NotificationDialog
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

package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * A general-purpose modal dialog to display a notification message.
 * An icon can be specified to display by the message and an <i>OK</i>
 * button is provided to close the dialog.  The dialog is brought up by the
 * {@link #setVisible(boolean)} method and is automatically disposed after the
 * user closes it.
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
public class NotificationDialog
	extends JDialog
{

	/** 
	 * The preferred size of the widget that displays the notification message.
	 * Only the part of text that fits into this display area will be displayed.
	 */
	protected static final Dimension	MSG_AREA_SIZE = new Dimension(300, 50);
	
	/** 
	 * The size of the invisible components used to separate widgets
	 * horizontally.
	 */
	protected static final Dimension	H_SPACER_SIZE = new Dimension(20, 1);
	
	/** 
	 * The size of the invisible components used to separate widgets
	 * vertically.
	 */
	protected static final Dimension	V_SPACER_SIZE = new Dimension(1, 20);
	
    /** Sets the default color of the panel. */
    protected static final Color        DEFAULT_COLOR = Color.WHITE;
    
	/** 
	 * The outmost container.  
	 * All other widgets are added to this panel, which, in turn, is then 
	 * added to the dialog's content pane.
	 */
	protected JPanel	contentPanel;
	
	/** Contains the message and the message icon, if any. */
	protected JPanel	messagePanel;
	
	/** Contains the {@link #okButton}. */
	protected JPanel	buttonPanel;
	
	/** Hides and disposes of the dialog. */
	protected JButton	okButton;
		
	/** Creates the various UI components that make up the dialog. */
	private void createComponents()
	{
		contentPanel = new JPanel();
        contentPanel.setOpaque(true);
		messagePanel = new JPanel();
        messagePanel.setOpaque(true);
		buttonPanel = new JPanel();
        buttonPanel.setOpaque(true);
		okButton = new JButton("OK");
	}
	
	/**
	 * Binds the {@link #close() close} action to the exit event generated
	 * either by the close icon or by the {@link #okButton}.
	 */
	private void attachListeners()
	{
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { close(); }
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { close(); }
		});
	}
	
	/** Hides and disposes of the dialog. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Builds and lays out the {@link #messagePanel}.
	 * It will contain the notification message along with the message icon, 
	 * if any.
	 * 
	 * @param msg		The notification message.
	 * @param msgIcon	The icon to display by the message.
	 */
	private void buildMessagePanel(String msg, Icon msgIcon)
	{
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
		if (msgIcon != null) {
			JLabel iconLabel = new JLabel(msgIcon);
			iconLabel.setAlignmentY(TOP_ALIGNMENT);
			messagePanel.add(iconLabel);
			messagePanel.add(Box.createRigidArea(H_SPACER_SIZE));
		}
		MultilineLabel message = new MultilineLabel(msg);
		message.setPreferredSize(MSG_AREA_SIZE);
		message.setAlignmentY(TOP_ALIGNMENT);
		messagePanel.add(message);
	}
	
	/**
	 * Builds and lays out the {@link #buttonPanel}.
	 * The {@link #okButton} will be added to this panel.
	 */
	private void buildButtonPanel()
	{
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
	}
	
	/**
	 * Builds and lays out the {@link #contentPanel}, then adds it to the
	 * content pane.
	 * 
	 * @param message		The notification message.
	 * @param messageIcon	The icon to display by the message.
	 */
	private void buildGUI(String message, Icon messageIcon)
	{
		contentPanel.setBorder(BorderFactory.createCompoundBorder(
								BorderFactory.createEtchedBorder(),
								BorderFactory.createEmptyBorder(5, 5, 15, 10)));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		buildMessagePanel(message, messageIcon);
		contentPanel.add(messagePanel);
		JPanel vSpacer = new JPanel();
		vSpacer.add(Box.createRigidArea(V_SPACER_SIZE));
		contentPanel.add(vSpacer);
		buildButtonPanel();
		contentPanel.add(buttonPanel);
		getContentPane().add(contentPanel);
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 * @param messageIcon	An optional icon to display by the message.
	 */
	public NotificationDialog(JFrame owner, String title, String message, 
															Icon messageIcon) 
	{
		super(owner, title, true);
		
		//setResizable(false);  
		//Believe it or not the icon from owner won't be displayed if the
		//dialog is not resizable. 
		createComponents();
		attachListeners();
		buildGUI(message, messageIcon);
	}

}
