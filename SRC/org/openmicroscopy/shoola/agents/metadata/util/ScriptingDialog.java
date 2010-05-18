/*
 * org.openmicroscopy.shoola.agents.metadata.util.ScriptingDialog 
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
package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXTaskPane;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.model.ParamData;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import pojos.ExperimenterData;

/** 
 * Dialog to run the selected script. The UI is created on the fly.
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
public class ScriptingDialog 
	extends JDialog
	implements ActionListener, DocumentListener, PropertyChangeListener
{
	
	/** Bound property indicating to run the script. */
	public static final String RUN_SELECTED_SCRIPT_PROPERTY = 
		"runSelectedScript";
	
	/** Bound property indicating to download the script. */
	public static final String DOWNLOAD_SELECTED_SCRIPT_PROPERTY = 
		"downloadSelectedScript";
	
	/** Bound property indicating to download the script. */
	public static final String VIEW_SELECTED_SCRIPT_PROPERTY = 
		"viewSelectedScript";
	
	/** 
	 * The size of the invisible components used to separate buttons
	 * horizontally.
	 */
	private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
	
	/** The background of the description. */
	private static final Color		BG_COLOR = Color.LIGHT_GRAY;
	
	/** Title of the dialog. */
	private static final String		TITLE = "Run Script";
	
	/** The text displayed in the header. */
	private static final String		TEXT = "Set the parameters for the " +
			"selected script: ";
	
	/** The text displayed in the header. */
	private static final String		TEXT_END = ScriptComponent.REQUIRED +
			" indicates the required parameter.";
	
	/** Indicates to close the dialog. */
	private static final int CANCEL = 0;
	
	/** Indicates to run the script. */
	private static final int APPLY = 1;
	
	/** Indicates to download the script. */
	private static final int DOWNLOAD = 2;
	
	/** Indicates to view the script. */
	private static final int VIEW = 3;

	/** Close the dialog. */
	private JButton cancelButton;
	
	/** Run the script. */
	private JButton applyButton;
	
	/** Menu offering the ability to download or view the script. */
	private JButton menuButton;
	
	/** Component used to enter the author of the script. */
	private JTextField	author;
	
	/** Component used to enter the author's e-mail address. */
	private JTextField	eMail;
	
	/** Component used to enter the author's institution. */
	private JTextField	institution;
	
	/** Component used to enter the description of the script. */
	private JTextField	description;
	
	/** Component used to enter where the script was published if 
	 * published. */
	private JTextField	journalRef;
	
	/** The object to handle. */
	private ScriptObject script;
	
	/** The components to display. */
	private Map<String, ScriptComponent> components;
	
	/** Used to sort collections. */
	private ViewerSorter sorter;
	
	/** The menu offering various options to manipulate the script. */
	private JPopupMenu optionMenu;
	
	/** 
	 * Creates the option menu.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createOptionMenu()
	{
		if (optionMenu != null) return optionMenu;
		optionMenu = new JPopupMenu();
		optionMenu.add(createButton("Download", DOWNLOAD));
		optionMenu.add(createButton("View", VIEW));
		return optionMenu;
	}
	
	/**
	 * Creates a button.
	 * 
	 * @param text The text of the button.
	 * @param actionID The action command id.
	 * @param l The action listener.
	 * @return See above.
	 */
	private JButton createButton(String text, int actionID)
    {
    	JButton b = new JButton(text);
		b.setActionCommand(""+actionID);
		b.addActionListener(this);
		b.setOpaque(false);
		//b.setForeground(UIUtilities.HYPERLINK_COLOR);
		UIUtilities.unifiedButtonLookAndFeel(b);
		return b;
    }
	
	/** Closes the dialog. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Sets the enabled flag of the {@link #applyButton} to <code>true</code>
	 * if the script can run i.e. all required fields are filled, to 
	 * <code>false</code> otherwise.
	 */
	private void canRunScript()
	{
		Iterator i = components.entrySet().iterator();
		Entry entry;
		ScriptComponent c;
		int required = 0;
		int valueSet = 0;
		Object value;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = (ScriptComponent) entry.getValue();
			if (c.isRequired()) {
				required++;
				value = c.getValue();
				if (value != null) {
					if (value instanceof String) {
						if (((String) value).length() != 0)
							valueSet++;
					} else valueSet++;
				}
			}
		}
		applyButton.setEnabled(required == valueSet);
	}
	
	/** Collects the data and fires a property.*/
	private void runScript()
	{
		Entry entry;
		ScriptComponent c;
		Iterator i = components.entrySet().iterator();
		Map<String, ParamData> inputs = script.getInputs();
		ParamData param;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = (ScriptComponent) entry.getValue();
			param = inputs.get(entry.getKey());
			param.setValueToPass(c.getValue());
		}
		firePropertyChange(RUN_SELECTED_SCRIPT_PROPERTY, null, script);
		close();
	}
	
	/** 
	 * Creates a component displaying the various options.
	 * 
	 * @param values The values to display.
	 * @return See above.
	 */
	private JComboBox createValuesBox(List<Object> values)
	{
		if (values == null) return null;
		Object[] v = new Object[values.size()];
		Iterator<Object> i = values.iterator();
		int j = 0;
		Object value;
		while (i.hasNext()) {
			value = i.next();
			if (value instanceof String)
				v[j] = ((String) value).replace(
						ScriptObject.PARAMETER_SEPARATOR, 
						ScriptObject.PARAMETER_UI_SEPARATOR);
			else v[j] = value;
			j++;
		}
		return new JComboBox(v);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		sorter = new ViewerSorter();
		List<ExperimenterData> experimenters = script.getAuthors();
		Iterator<ExperimenterData> j;
		author = new JTextField();
		author.setEnabled(false);
		if (experimenters != null && experimenters.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			j = experimenters.iterator();
			ExperimenterData exp;
			int index = experimenters.size();
			while (j.hasNext()) {
				exp = j.next();
				buffer.append(exp.getLastName());
				if (index > 1) buffer.append(","); 
			}
			author.setText(buffer.toString());
		}
	    eMail = new JTextField();
	    eMail.setEnabled(false);
	    eMail.setText(script.getContact());
	    institution = new JTextField();
	    institution.setEnabled(false);
		//if (exp != null) {
			//institution.setText(exp.getInstitution());
		//}
	    journalRef = new JTextField(script.getJournalRef()); 
	    journalRef.setEnabled(false);
	    description = new JTextField(script.getDescription());
	    description.setEnabled(false);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close the dialog.");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		applyButton = new JButton("Run");
		applyButton.setToolTipText("Run the script.");
		applyButton.setActionCommand(""+APPLY);
		applyButton.addActionListener(this);
		IconManager icons = IconManager.getInstance();
		menuButton = new JButton(icons.getIcon(IconManager.BLACK_ARROW_DOWN));
		menuButton.setText("Script");
		menuButton.setHorizontalTextPosition(JButton.LEFT);
		menuButton.addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				Object src = e.getSource();
				if (src instanceof Component) {
					Point p = e.getPoint();
					createOptionMenu().show((Component) src, p.x, p.y);
				}
			}
		});
		
		components = new LinkedHashMap<String, ScriptComponent>(); 
		Map<String, ParamData> types = script.getInputs();
		if (types == null) return;
		List <ScriptComponent> results = new ArrayList<ScriptComponent>();
		Entry entry;
		ParamData param;
		JComponent comp;
		ScriptComponent c;
		String name;
		Class type;
		Object defValue ;
		Iterator i = types.entrySet().iterator();
		List<Object> values;
		Number n;
		String details = "";
		String text = "";
		String grouping;
		String parent;
		Map<String, List<ScriptComponent>> childrenMap = 
			new HashMap<String, List<ScriptComponent>>();
		Map<String, ScriptComponent> 
			parents = new HashMap<String, ScriptComponent>();
		List<ScriptComponent> l;
		while (i.hasNext()) {
			text = "";
			comp = null;
			entry = (Entry) i.next();
			param = (ParamData) entry.getValue();
			name = (String) entry.getKey();
			type = param.getPrototype();
			values = param.getValues();
			defValue = param.getDefaultValue();
			if (values != null && values.size() > 0) {
				comp = createValuesBox(values);
				if (defValue != null) {
					((JComboBox) comp).setSelectedItem(defValue);
				}
			}
			if (Long.class.equals(type) || Integer.class.equals(type) ||
					Float.class.equals(type) || Double.class.equals(type)) {
				if (comp == null) {
					comp = new NumericalTextField();
					((NumericalTextField) comp).setNumberType(type);
					n = param.getMinValue();
					if (n != null) {
						if (Long.class.equals(type)) {
							text += "Min: "+n.longValue()+" ";
							((NumericalTextField) comp).setMinimum(
									n.longValue());
						} else if (Integer.class.equals(type)) {
							text += "Min: "+n.intValue()+" ";
							((NumericalTextField) comp).setMinimum(
									n.intValue());
						} else if (Double.class.equals(type)) {
							text += "Min: "+n.doubleValue()+" ";
							((NumericalTextField) comp).setMinimum(
									n.doubleValue());
						} else if (Float.class.equals(type)) {
							text += "Min: "+n.floatValue()+" ";
							((NumericalTextField) comp).setMinimum(
									n.floatValue());
						} 
					}
					n = param.getMaxValue();
					if (n != null) {
						if (Long.class.equals(type)) {
							text += "Max: "+n.longValue()+" ";
							((NumericalTextField) comp).setMaximum(
									n.longValue());
						} else if (Integer.class.equals(type)) {
							text += "Max: "+n.intValue()+" ";
							((NumericalTextField) comp).setMaximum(
									n.intValue());
						} else if (Double.class.equals(type)) {
							text += "Max: "+n.doubleValue()+" ";
							((NumericalTextField) comp).setMaximum(
									n.doubleValue());
						} else if (Float.class.equals(type)) {
							text += "Max: "+n.floatValue()+" ";
							((NumericalTextField) comp).setMaximum(
									n.floatValue());
						} 
					}
					if (defValue != null)
						((NumericalTextField) comp).setText(
								""+defValue);
				}
			} else if (String.class.equals(type)) {
				if (comp == null) {
					comp = new JTextField();
					if (defValue != null)
						((JTextField) comp).setText(""+defValue);
				}
			} else if (Boolean.class.equals(type)) {
				if (comp == null) {
					comp = new JCheckBox();
					if (defValue != null)
						((JCheckBox) comp).setSelected((Boolean) defValue);
				}
			} else if (Map.class.equals(type)) {
				if (comp == null)
					comp = new ComplexParamPane(param.getKeyType(), 
							param.getValueType());
				else 
					comp = new ComplexParamPane(param.getKeyType(), 
							(JComboBox) comp);
			} else if (List.class.equals(type)) {
				if (comp == null)
					comp = new ComplexParamPane(param.getKeyType());
				else 
					comp = new ComplexParamPane((JComboBox) comp);
			}
			if (comp != null) {
				if (comp instanceof JTextField) {
					((JTextField) comp).setColumns(ScriptComponent.COLUMNS);
					((JTextField) comp).getDocument().addDocumentListener(this);
				}
				if (comp instanceof ComplexParamPane)
					comp.addPropertyChangeListener(this);
				comp.setToolTipText(param.getDescription());
				c = new ScriptComponent(comp, name);
				if (text.trim().length() > 0) c.setUnit(text);
				if (!(comp instanceof JComboBox || comp instanceof JCheckBox))
					c.setRequired(!param.isOptional());
				if (details != null && details.trim().length() > 0)
					c.setInfo(details);

				grouping = param.getGrouping();
				parent = param.getParent();
				c.setParentIndex(parent);
				if (parent.length() > 0) {
					l = childrenMap.get(parent);
					if (l == null) {
						l = new ArrayList<ScriptComponent>();
						childrenMap.put(parent, l);
					}
					l.add(c);
				}
					
				if (grouping.length() > 0) {
					c.setGrouping(grouping);
					c.setNameLabel(grouping);
				} else c.setNameLabel(name);
				results.add(c);
			}
		}
		ScriptComponent key;
		Iterator<ScriptComponent> k = results.iterator();
		while (k.hasNext()) {
			key = k.next();
			grouping = key.getGrouping();
			l = childrenMap.get(grouping);
			if (l != null)
				key.setChildren(l);
		}
		
		List<ScriptComponent> sortedKeys = sorter.sort(results);
		k = sortedKeys.iterator();
		
		while (k.hasNext()) {
			key = k.next();
			components.put(key.getParameterName(), key);
		}
		canRunScript();
	}
	
	/**
	 * Builds and lays out the details of the script.
	 * 
	 * @return See above.
	 */
	private JPanel buildScriptDetails()
	{
		double[][] size = {{TableLayout.PREFERRED, 5, TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
			TableLayout.PREFERRED, TableLayout.PREFERRED, 50}};
		JPanel details = new JPanel();
		details.setLayout(new TableLayout(size));
		int row = 0;
		JLabel l = UIUtilities.setTextFont("Author (First, Last):");
		details.add(l, "0, "+row+", LEFT, CENTER");
		details.add(author, "2, "+row);
		row++;
		l = UIUtilities.setTextFont("E-mail:");
		details.add(l, "0, "+row+", LEFT, CENTER");
		details.add(eMail, "2, "+row);
		row++;
		l = UIUtilities.setTextFont("Institution:");
		details.add(l, "0, "+row+", LEFT, CENTER");
		details.add(institution, "2, "+row);
		row++;
		l = UIUtilities.setTextFont("Journal Ref:");
		details.add(l, "0, "+row+", LEFT, CENTER");
		details.add(journalRef, "2, "+row);
		row++;
		l = UIUtilities.setTextFont("Script's Description:");
		details.add(l, "0, "+row+", LEFT, TOP");
		details.add(description, "2, "+row);
		
		return details;
	}
	
	/**
	 * Builds the panel hosting the components.
	 * 
	 * @return See above.
	 */
	private JPanel buildControlPanel()
	{
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(null);
		controlPanel.add(applyButton);
		controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		controlPanel.add(cancelButton);
		controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
		bar.add(controlPanel);
		bar.add(Box.createVerticalStrut(10));
	
		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.X_AXIS));
		all.add(UIUtilities.buildComponentPanel(menuButton));
		all.add(UIUtilities.buildComponentPanelRight(bar));
		return all;
	}
	
	/**
	 * Returns the component displaying the description of the script.
	 * 
	 * @return See above.
	 */
	private JComponent buildDescriptionPane()
	{
		String description = script.getDescription();
		if (description == null || description.trim().length() == 0) 
			return  null;
		OMEWikiComponent area = new OMEWikiComponent(false);
		area.setEnabled(false);
		area.setText(description);
		JPanel p = UIUtilities.buildComponentPanel(area);
		p.setBackground(BG_COLOR);
		area.setBackground(BG_COLOR);
		return p;
	}
	/** 
	 * Builds the component displaying the parameters.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		double[] columns = {TableLayout.PREFERRED, 5, TableLayout.FILL};
		TableLayout layout = new TableLayout();
		layout.setColumn(columns);
		p.setLayout(layout);
		int row = 0;
		JComponent area = buildDescriptionPane();
		if (area != null) {
			layout.insertRow(row, TableLayout.PREFERRED);
			p.add(area, "0,"+row+", 2, "+row);
			row++;
			layout.insertRow(row, TableLayout.PREFERRED);
			p.add(new JSeparator(), "0,"+row+", 2, "+row);
			row++;
		}
		
		Entry entry;
		Iterator i = components.entrySet().iterator();
		ScriptComponent comp;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			comp = (ScriptComponent) entry.getValue();
			layout.insertRow(row, TableLayout.PREFERRED);
			//p.add(comp.getLabel(), "0,"+row);
			comp.buildUI();
			p.add(comp, "0,"+row+", 2, "+row);
			row++;
		}
		
		JLabel label = new JLabel(TEXT_END);
		Font font = label.getFont();
		label.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
		label.setForeground(Color.RED);
		layout.insertRow(row, TableLayout.PREFERRED);
		p.add(UIUtilities.buildComponentPanel(label), "0,"+row+",2, "+row);
		JXTaskPane pane = null;
		if (script.hasDetails()) {
			pane = new JXTaskPane();
			pane.setCollapsed(true);
			pane.setTitle("Script details");
			pane.add(buildScriptDetails());
		}
		
		JPanel controls = new JPanel();
		controls.setLayout(new BorderLayout(0, 0));
		if (pane != null) controls.add(pane, BorderLayout.NORTH);
		controls.add(new JScrollPane(p), BorderLayout.CENTER);
		controls.add(buildControlPanel(), BorderLayout.SOUTH);
		return controls;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		String text = TEXT+script.getDisplayedName();
		TitlePanel tp = new TitlePanel(TITLE, text, script.getIconLarge());
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		//c.add(buildControlPanel(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the frame.
	 * @param script The script to run. Mustn't be <code>null</code>.
	 */
	public ScriptingDialog(JFrame parent, ScriptObject script)
	{
		super(parent);
		if (script == null)
			throw new IllegalArgumentException("No script specified");
		this.script = script;
		initComponents();
		buildGUI();
		pack();
	}
	
	/**
	 * Closes or runs the scripts.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				break;
			case APPLY:
				runScript();
				break;
			case DOWNLOAD:
				firePropertyChange(DOWNLOAD_SELECTED_SCRIPT_PROPERTY, null, 
						script);
				break;
			case VIEW:
				firePropertyChange(VIEW_SELECTED_SCRIPT_PROPERTY, null, script);
		}
	}

	/**
	 * Checks if the user can run the script.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (RowPane.MODIFIED_CONTENT_PROPERTY.equals(name))
			canRunScript();
	}
	
	/**
	 * Allows the user to run or not the script.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { canRunScript(); }

	/**
	 * Allows the user to run or not the script.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { canRunScript(); }
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation 
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
