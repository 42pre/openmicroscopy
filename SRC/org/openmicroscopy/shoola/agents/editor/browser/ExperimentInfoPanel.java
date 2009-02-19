 /*
 * org.openmicroscopy.shoola.agents.editor.browser.ExperimentInfoPanel 
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.jdesktop.swingx.JXDatePicker;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.model.CPEimport;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.TreeIterator;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomFont;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A Panel to display experimental info (IF we're editing an experiment). 
 * Otherwise this panel is hidden. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ExperimentInfoPanel 
	extends JPanel
	implements TreeModelListener,
	ActionListener, 
	TreeSelectionListener {
	
	/**
	 * A reference to the tree UI used for selection of root.
	 */
	private JTree 				navTree;
	
	/**
	 * Controller for edits etc.
	 */
	private BrowserControl 		controller;
	
	/**
	 * The tree Model. Get the root for experimental info, and listen for
	 * changes to update 'unfilled' fields. 
	 */
	private TreeModel			treeModel;
	
	/** The root node of the Tree Model, contains the field with exp info */
	TreeNode 					root;
	
	/** The root field/step of the protocol. Holds exp info. */
	IField 						field;
	
	/** Label to display investigator */
	private JLabel				investigatorLabel;
	
	/** Label to display number of unfilled parameters in the experiment */
	private JLabel 				unfilledParamsLabel;
	
	/** Label to display number of unfilled steps in the experiment */
	private JLabel 				unfilledStepsLabel;
	
	/** A Date-picker to display and pick date of experiment. */
	private JXDatePicker 		datePicker;
	
	/**
	 *  A list of the unfilled steps in the experiment. Allows user to 
	 * search through the experiment. 
	 */
	private List<TreePath>		unfilledSteps;
	
	/** The currently selected step of the unfilled steps list */
	private int					currentStepIndex;
	
	/** Button for moving to the next unfilled step */
	private JButton				nextStep;
	
	/** Button for moving to the previous unfilled step */
	private JButton				prevStep;
	
	/** Button for moving to the first unfilled step */
	private JButton				goToFirstStep;
	
	/** Action command for the Next Step button */
	public static final String	NEXT_STEP = "nextStep";
	
	/** Action command for the Previous Step button */
	public static final String	PREV_STEP = "prevStep";
	
	/** Action command for the First-Step button */
	public static final String	FIRST_STEP = "firstStep";
	
	
	/**
	 * Initialises the various UI components
	 */
	private void initialise() 
	{
		investigatorLabel = new CustomLabel();
		datePicker = UIUtilities.createDatePicker();
		datePicker.setFont(new CustomFont());
		datePicker.addActionListener(this);
		
		unfilledParamsLabel = new CustomLabel();
		unfilledStepsLabel = new CustomLabel();
		unfilledStepsLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		IconManager iM = IconManager.getInstance();
		Icon rightIcon = iM.getIcon(IconManager.ARROW_RIGHT_ICON_12);
		Icon leftIcon = iM.getIcon(IconManager.ARROW_LEFT_ICON_12);
		Icon goIcon = iM.getIcon(IconManager.GO_ICON_12_20);
		
		goToFirstStep = new CustomButton(goIcon);
		goToFirstStep.setActionCommand(FIRST_STEP);
		goToFirstStep.addActionListener(this);
		goToFirstStep.setFocusable(false);		
		goToFirstStep.setToolTipText("Go to the first un-filled step");
		goToFirstStep.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		nextStep = new CustomButton(rightIcon);
		nextStep.setActionCommand(NEXT_STEP);
		nextStep.addActionListener(this);
		nextStep.setFocusable(false);  // long focus-bug-fix story! 
		nextStep.setToolTipText("Go to the next un-filled step");
		nextStep.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		prevStep = new CustomButton(leftIcon);
		prevStep.setActionCommand(PREV_STEP);
		prevStep.addActionListener(this);
		prevStep.setFocusable(false);
		prevStep.setToolTipText("Go to the previous un-filled step");
		prevStep.setAlignmentY(Component.CENTER_ALIGNMENT);
	}

	/**
	 * Builds the UI. 
	 */
	private void buildUI() 
	{
		
		setLayout(new BorderLayout());
		setBackground(new Color(254,244,156));
		Border lineBorder = BorderFactory.createMatteBorder(1, 1, 0, 1,
	             UIUtilities.LIGHT_GREY.darker());
		setBorder(BorderFactory.createCompoundBorder(lineBorder, 
						new EmptyBorder(5,5,5,5)));
		
		// add header
		JLabel experiment = new CustomLabel("Experiment Info:");
		experiment.setFont(CustomFont.getFontBySize(14));
		add(experiment, BorderLayout.NORTH);
		
		
		// left Panel
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(null);
		
		// add labels to left
		investigatorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(investigatorLabel);
		Box dateBox = Box.createHorizontalBox();
		JLabel dateLabel = new CustomLabel("Date: ");
		dateBox.add(dateLabel);
		dateBox.add(datePicker);
		dateBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(dateBox);
		
		
		// right Panel
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBackground(null);
		
		// buttons for finding unfilled steps
		Box	stepButtonsBox = Box.createHorizontalBox();
		stepButtonsBox.add(unfilledStepsLabel);
		stepButtonsBox.add(prevStep);
		stepButtonsBox.add(goToFirstStep);
		stepButtonsBox.add(nextStep);
		stepButtonsBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		unfilledParamsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		rightPanel.add(unfilledParamsLabel);
		rightPanel.add(stepButtonsBox);
		
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);
	}

	/**
	 * Sets the text and visibility of this panel, according to whether the
	 * protocol has any experimental info.
	 */
	private void refreshPanel() 
	{
		
		TreeNode tn = (TreeNode)treeModel.getRoot();
		
		if (!(tn instanceof DefaultMutableTreeNode)) return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tn;
		Object userOb = node.getUserObject();
		if (!(userOb instanceof IField)) return;
		field = (IField)userOb;
	
		// add details (name and date)
		String expDate = field.getAttribute(CPEimport.EXP_DATE);
		String investigName = field.getAttribute(CPEimport.INVESTIG_NAME);
		if (expDate != null || investigName != null) {
		
			String date = "no date";
			
			SimpleDateFormat f = new SimpleDateFormat("yyyy, MMM d");
			try {
				long millis = new Long(expDate);
				Date d = new Date(millis);
				datePicker.setDate(d);
				date = f.format(d);
				datePicker.setToolTipText(date);
			} catch (NumberFormatException ex) {}
			
			investigatorLabel.setText("Investigator: " + investigName);
			
			searchUnfilledParams();
			unfilledParamsLabel.setText("<html>Unfilled Parameters: <b>" + 
					unfilledSteps.size() + "</b></html>");
			unfilledStepsLabel.setText("<html>in <b>" + unfilledSteps.size() + 
					"</b> steps.</html>");
			
			selectCurrentStep();
			
			setVisible(true);
		}
		else 
			setVisible(false);
		
		revalidate();
		repaint();
	}
	
	/**
	 * This method iterates through the Tree Model, counting the number of 
	 * un-filled parameters in the experiment and making a list of the steps
	 * that contain them. 
	 */
	private void searchUnfilledParams()
	{
		if (unfilledSteps == null) {
			unfilledSteps = new ArrayList<TreePath>();
		} else {
			unfilledSteps.clear();
		}
		currentStepIndex = -1;
		
		TreeNode tn;
		IField f;
		Object userOb;
		DefaultMutableTreeNode node;
		TreePath path;
		int paramCount;
		
		Object r = treeModel.getRoot();
		if (! (r instanceof TreeNode)) 		return;
		root = (TreeNode)r;
		
		Iterator<TreeNode> iterator = new TreeIterator(root);
		
		while (iterator.hasNext()) {
			tn = iterator.next();
			if (!(tn instanceof DefaultMutableTreeNode)) continue;
			node = (DefaultMutableTreeNode)tn;
			userOb = node.getUserObject();
			if (!(userOb instanceof IField)) continue;
			f = (IField)userOb;
			path = new TreePath(node.getPath());
			if (f != null) {
				paramCount = f.getUnfilledCount();
				if (paramCount > 0) {
					unfilledSteps.add(path);
				}
			}
		}
	}
	
	/**
	 * This selects the unfilled step in the {@link #navTree} according to 
	 * the {@link #currentStepIndex}.
	 * Then calls {@link #refreshButtons()} to update their enabled status. 
	 */
	private void selectCurrentStep()
	{
		// if index is valid within un-filled steps, select the step
		if (unfilledSteps != null) {
			if (currentStepIndex > -1 && 
								currentStepIndex < unfilledSteps.size()) {
				TreePath currentStep = unfilledSteps.get(currentStepIndex);
				// select path (don't want feedback!)
				
				navTree.removeTreeSelectionListener(this);
				navTree.setSelectionPath(currentStep);
				navTree.addTreeSelectionListener(this);
			}
		}
		refreshButtons();
	}
	
	/**
	 * Refreshes the enabled state of the buttons depending on the current
	 * step index 
	 */
	private void refreshButtons()
	{
		// if no steps selected (before user clicks through)
		if (currentStepIndex == -1) {
			goToFirstStep.setEnabled(true);
			nextStep.setEnabled(false);
			prevStep.setEnabled(false);
		// otherwise, set buttons depending on index 
		} else {			
			goToFirstStep.setEnabled(currentStepIndex != 0);
			prevStep.setEnabled(currentStepIndex > 0);
			nextStep.setEnabled(currentStepIndex < unfilledSteps.size()- 1);
		}
	}
	
	/**
	 * Method to handle the editing of experiment info attributes.
	 * Delegates to the controller to handle undo/redo etc. 
	 * 
	 * @param attributeName
	 * @param newValue
	 */
	private void editAttribute(String attributeName, String newValue)
	{
		controller.editAttribute(field, attributeName, newValue, 
	 			"Experiment", navTree, root);
	}

	/**
	 * Creates an instance of this class, and builds UI. 
	 * 
	 * @param tree				The JTree used for selection management.
	 * @param controller		The controller for editing, undo/redo etc. 
	 */
	ExperimentInfoPanel(JTree tree, BrowserControl controller)
	{
		this.navTree = tree;
		this.controller = controller;
		
		if (navTree != null)
			navTree.addTreeSelectionListener(this);
		
		initialise();
		buildUI();
	}
	
	/**
	 * Sets the Tree Model for this panel to display experimental info. 
	 * 
	 * @param tm		The Tree Model
	 */
	void setTreeModel(TreeModel tm) {
		
		treeModel = tm;
		
		if (treeModel != null)
			treeModel.addTreeModelListener(this);
		
		refreshPanel();
	}
	
	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshPanel()} when the tree model changes. 
	 * 
	 * @see TreeModelListener#treeNodesChanged(TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e) {
		refreshPanel();
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshPanel()} when the tree model changes. 
	 * 
	 * @see TreeModelListener#treeNodesInserted(TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e) {
		refreshPanel();
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshPanel()} when the tree model changes. 
	 * 
	 * @see TreeModelListener#treeNodesRemoved(TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e) {
		refreshPanel();
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshPanel()} when the tree model changes. 
	 * 
	 * @see TreeModelListener#treeStructureChanged(TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e) {
		refreshPanel();
	}

	/**
	 * Implemented as specified by the {@link ActionListener} interface.
	 * Handles actions from Step buttons, and from date-picker. 
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		String cmd = e.getActionCommand();
		int stepCount = unfilledSteps.size();
		
		if (NEXT_STEP.equals(cmd)) {
			if (currentStepIndex < stepCount-1) {
				currentStepIndex++;
				selectCurrentStep();
			}
		}
		
		else if (PREV_STEP.equals(cmd)) {
			if (currentStepIndex > 0) {
				currentStepIndex--;
				selectCurrentStep();
			}
		}
		else if (FIRST_STEP.equals(cmd)) {
			if (stepCount >0) {
				currentStepIndex = 0;
				selectCurrentStep();
			}
		}
		else if (e.getSource().equals(datePicker)) {
			String date = datePicker.getDate().getTime() + "";
			
			editAttribute(CPEimport.EXP_DATE, date);
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		currentStepIndex = -1;
		// sets enabled status of buttons. 
		refreshButtons(); 
	}

}
