/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.annotator.AnnotationPaneUI
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.annotator;



//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;


//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.ExperimenterData;

/** 
 * The UI delegate for the {@link AnnotationPane}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class AnnotationPaneUI
    extends JPanel
{

    /** The default annotation text. */
    private static final String         DEFAULT_TEXT = "No annotation for ";
    
    /** The default message. */
    private static final String         DEFAULT_MSG = "Object not annotatable.";
    
    /** The title to define the context of the annotation. */
    private static final String         TITLE_MSG = "Annotate:";
    
    /** The label of the {@link #deleteBox}. */
    private static final String         DELETE_ANNOTATION = "Delete " +
                                                        "the annotation";
    
    /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    private static final Dimension      SMALL_V_SPACER_SIZE = 
                                                new Dimension(1, 6);
    
    /** The label presenting the annotation context. */
    private JLabel              titleLabel;
    
    /** Button to finish the operation. */
    private JButton             saveButton;
    
    /** Area where to annotate the <code>DataObject</code>. */
    private JTextArea           annotationArea;
    
    /** Hosts a list of users who annotated the selected object. */
    private JList               annotatedByList;
    
    /** Box to delete the annotation. */
    private JCheckBox           deleteBox;
    
    /** The model keeping track of the users who annotated a data object. */
    private DefaultListModel    listModel;
    
    /** Maps of users who annotated the data object. */
    private Map                 ownersMap;
    
    /** The index of the current user.*/
    private int                 userIndex;
    
    /** 
     * Flag indicating that the default text is displayed for the 
     * current user.
     */
    private boolean             defaultText;
    
    /** A {@link DocumentListener} for the {@link #annotationArea}. */
    private DocumentListener    annotationAreaListener;
    
    /** Reference to the Model. */
    private AnnotationPane      model;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        titleLabel = new JLabel(TITLE_MSG+DEFAULT_MSG);
        saveButton = new JButton("SAVE");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { model.save(); }
        });
        annotationArea = new MultilineLabel();
        annotationArea.setBackground(Color.WHITE);
        annotationArea.setBorder(new TitledBorder("Annotation"));
        deleteBox = new JCheckBox(DELETE_ANNOTATION);
        listModel = new DefaultListModel();
        annotatedByList = new JList(listModel);
        annotatedByList.setBackground(getBackground());
        annotatedByList.setBorder(new TitledBorder("Annotated by"));
        annotatedByList.setSelectionMode(
                ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        annotatedByList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    showSingleAnnotation();
                 }
            }
        });
        annotationAreaListener = new DocumentListener() {
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
            	defaultText = false;
                model.setAnnotated(true);
            }
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
            	defaultText = false;
                model.setAnnotated(true);
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        annotationArea.getDocument().addDocumentListener(
                            annotationAreaListener);
        annotationArea.addMouseListener(new MouseAdapter() {
            //Removes default message.
            public void mouseClicked(MouseEvent e)
            {
                if (isAnnotable() && defaultText) {
                	annotationArea.getDocument().removeDocumentListener(
                            annotationAreaListener);
                    annotationArea.setText("");
                    annotationArea.getDocument().addDocumentListener(
                            annotationAreaListener);
                }
            }
        
        });
        setComponentsEnabled(false);
        deleteBox.setSelected(false);
    }
    
    /**
     * Builds a panel hosting the {@link #annotationArea} and the list of users
     * who annotated the data object.
     * 
     * @return See above.
     */
    private JPanel buildAnnotationPanel()
    {
        JPanel p = new JPanel();
        double[][] tl = {{TableLayout.FILL, 5, TableLayout.FILL}, //columns
				{0, 150} }; //rows
		p.setLayout(new TableLayout(tl));
		p.add(new JScrollPane(annotationArea), "0, 0, 0, 1");
		JPanel empty = new JPanel();
		empty.setOpaque(true);
		p.add(empty, "1, 0, f, t");
		p.add(new JScrollPane(annotatedByList), "2, 0, 2, 1");       
		return p;
    }
    
    /**
     * Sets the specified text to the {@link #annotationArea}.
     * 
     * @param text  The text to set.
     */
    private void addAnnotationText(String text)
    {
        annotationArea.getDocument().removeDocumentListener(
                                        annotationAreaListener);
        annotationArea.setText(text);
        annotationArea.getDocument().addDocumentListener(
                                        annotationAreaListener);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(deleteBox);
        p.add(saveButton);
        add(UIUtilities.buildComponentPanel(titleLabel));
        add(new JSeparator());
        add(UIUtilities.buildComponentPanel(p));
        add(new JSeparator());
        add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
        add(buildAnnotationPanel());
        //add(Box.createVerticalGlue());
    }
    
    /**
     * Displays the users' name in a list box.
     * 
     * @param owners Array of users who annotated the selected item.
     */
    private void formatUsersList(String[] owners)
    {
        // remove all users from list before adding new
        listModel.removeAllElements();
        
        // add each user to list
        Timestamp date;
        DateFormat df = DateFormat.getDateInstance();
        AnnotationData data;
        List list;
        for (int i = 0; i < owners.length; i++) {
            list =  getOwnerAnnotation(i);
            data = ((AnnotationData) list.get(0));
            date = data.getLastModified();
            if (date == null)
                date = new Timestamp((new java.util.Date()).getTime()); 
            listModel.addElement(owners[i]+" ("+df.format(date)+")");  
        }
    }
    
    /**
     * Returns the list of annotations made by the selected user.
     * 
     * @param index The index of the selected user.
     * @return See below.
     */
    private List getOwnerAnnotation(int index)
    { 
        Map annotations = model.getAnnotations();
        Long ownerID = (Long) ownersMap.get(new Integer(index));
        if (ownerID == null) return new ArrayList();    //empty list
        return (List) annotations.get(ownerID);
    }
    
    /** Shows a single annotation. */
    private void showSingleAnnotation()
    {
        int index = annotatedByList.getSelectedIndex();
        saveButton.setEnabled(true);
        if (index == -1) {
            ExperimenterData details = model.getUserDetails();
            addAnnotationText(DEFAULT_TEXT+details.getFirstName()+" "+
                                details.getLastName());
            defaultText = true;
            setComponentsEnabled(true);
            deleteBox.setEnabled(false);
            return;
        }
        List list = getOwnerAnnotation(index);
        if (list.size() > 0) {
            AnnotationData data = (AnnotationData) list.get(0);
            addAnnotationText(data.getText());  
        }
        setComponentsEnabled(index == userIndex);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    AnnotationPaneUI(AnnotationPane model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        buildGUI();
    }
    
    /** 
     * Sets the UI components enabled.
     * 
     * @param b The enabled flag. 
     */
    void setComponentsEnabled(boolean b)
    {
        saveButton.setEnabled(b);
        deleteBox.setEnabled(b);
        annotationArea.setEditable(b);
        if (b) {
            annotationArea.requestFocus();
            annotationArea.selectAll();
        }
    }
    
    /** Shows the annotations. */
    void showAnnotations()
    {
        deleteBox.setSelected(false);
        ExperimenterData userDetails = model.getUserDetails();
        if (userDetails == null) return;
        Map annotations = model.getAnnotations();
        String[] owners = new String[annotations.size()];
        Iterator i = annotations.keySet().iterator();
        Long id;
        int index = 0;
        ownersMap = new HashMap();
        List list;
        ExperimenterData data;
        while (i.hasNext()) {
            id = (Long) i.next();
            list = (List) annotations.get(id);
            data = ((AnnotationData) list.get(0)).getOwner();
            if (userDetails.getId() == id.intValue()) userIndex = index;
            String n = "Name not available"; //TODO: REMOVE ASAP
            try {
                n = data.getLastName();
            } catch (Exception e) {}
            owners[index] = n;
            ownersMap.put(new Integer(index), id);
            index++;
        }
        //No annotation for the current user, so allow creation.
        
        setComponentsEnabled(true);
        formatUsersList(owners);
        //annotatedByList.clearSelection();
        if (userIndex != -1) annotatedByList.setSelectedIndex(userIndex);
        showSingleAnnotation();
    }
    
    /**
     * Reacts to a new selection in the browser.
     * 
     * @param b     Passed <code>true</code> to enable the controls,
     *              <code>true</code> otherwise.
     * @param title The context of the annotation.
     */
    void onSelectedDisplay(boolean b, String title)
    {
        setComponentsEnabled(b);
        userIndex = -1;
        if (title == null) title = DEFAULT_MSG;
        titleLabel.setText(TITLE_MSG+title);
        addAnnotationText("");
        listModel.clear();
        repaint();
    }
    
    /**
     * Returns <code>true</code> if the data object is annotated,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotable()
    { 
        if (userIndex == -1) return true;//no annotation for current user
        return (annotatedByList.getSelectedIndex() == userIndex); 
    }
    
    /**
     * Returns <code>true</code> if the data object has to be deleted,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotationDeleted() {  return deleteBox.isSelected(); }
    
    /** 
     * Returns the text of the annotation. 
     * 
     * @return See above. 
     */
    String getAnnotationText() { return annotationArea.getText(); }
    
}
