/*
 * org.openmicroscopy.shoola.agents.metadata.editor.GeneralPaneUI 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries
import layout.TableLayout;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.util.ui.ScrollablePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellSampleData;

/** 
 * Component displaying the annotation.
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
class GeneralPaneUI 
	extends JScrollPane
{

	/** Reference to the controller. */
	private EditorControl				controller;
	
	/** Reference to the Model. */
	private EditorModel					model;
	
	/** Reference to the Model. */
	private EditorUI					view;
	
	/** The UI component displaying the object's properties. */
	private PropertiesUI				propertiesUI;
	
	/** The UI component displaying the textual annotations. */
	private TextualAnnotationsUI		textualAnnotationsUI;
	
	/** Component hosting the tags, rating, urls and attachments. */
	private AnnotationDataUI			annotationUI;
	
	/** The component hosting the {@link #browser}. */
	private JXTaskPane 					browserTaskPane;

	/** Collection of annotations UI components. */
	private List<AnnotationUI>			components;
	
	/** Main component. */
	private JPanel						content;
	
	/** The layout index of the {@link annotationUI}. */
	private int							annotationLayoutIndex;
	
	/** The layout index of the {@link textualAnnotationsUI}. */
	private int							textualAnnotationsLayoutIndex;
	
	/** The layout index of the {@link browserTaskPane}. */
	private int							browserIndex;
	
	/** The layout index of the protocols. */
	private int							protocolsIndex;
	
	/** The component hosting the various protocols. */
	private JPanel						protocolComponent;
	
	/** Collection of preview panels. */
	private List<PreviewPanel>			previews;
	
	/**
	 * Loads or cancels any on-going loading of containers hosting
	 * the edited object.
	 * 
	 * @param b Pass <code>true</code> to load, <code>false</code> to cancel.
	 */
	private void loadParents(boolean b)
	{
		if (b) controller.loadParents();
		else {
			view.setStatus(false);
			model.cancelParentsLoading();
		}
	}
	
    /** Initializes the UI components. */
	private void initComponents()
	{
		if (model.getBrowser() != null) {
			browserTaskPane = EditorUtil.createTaskPane(Browser.TITLE);
			browserTaskPane.add(model.getBrowser().getUI());
			browserTaskPane.addPropertyChangeListener(controller);
		}
		
		protocolComponent = new JPanel();
		protocolComponent.setBackground(UIUtilities.BACKGROUND);
		propertiesUI = new PropertiesUI(model, controller);
		textualAnnotationsUI = new TextualAnnotationsUI(model, controller);
		annotationUI = new AnnotationDataUI(model, controller);

		components = new ArrayList<AnnotationUI>();
		components.add(propertiesUI);
		components.add(textualAnnotationsUI);
		components.add(annotationUI);
		Iterator<AnnotationUI> i = components.iterator();
		while (i.hasNext()) {
			i.next().addPropertyChangeListener(EditorControl.SAVE_PROPERTY,
											controller);
		}
		previews = new ArrayList<PreviewPanel>();
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		content = new ScrollablePanel();
		content.setBackground(UIUtilities.BACKGROUND);
		double[][]	size = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, 
				TableLayout.PREFERRED, 0, 0}};
		int i = 0;
		content.setLayout(new TableLayout(size));

		content.add(propertiesUI, "0, "+i);
		i++;
		i++;
		annotationLayoutIndex = i;
		content.add(annotationUI, "0, "+i);
		i++;
		i++;
		textualAnnotationsLayoutIndex = i;
		content.add(textualAnnotationsUI, "0, "+i);
		i++;
		protocolsIndex = i;
		content.add(protocolComponent, "0, "+i);
		i++;
		browserIndex = i;
		content.add(browserTaskPane, "0, "+i);
		getViewport().add(content);
	}
    
	/**
	 * Returns <code>true</code> if the passed value corresponds to
	 * a name space for <code>Editor</code>.
	 * 
	 * @param nameSpace The value to handle.
	 * @return See above.
	 */
	private boolean isEditorFile(String nameSpace)
	{
		return (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(nameSpace) ||
				FileAnnotationData.EDITOR_PROTOCOL_NS.equals(nameSpace));
	}
	
	/** 
	 * Lays out the protocols files. Returns the number of protocol files.
	 * 
	 * @return See above.
	 */
	private int buildProtocolTaskPanes()
	{
		Collection list = model.getAttachments();
		protocolComponent.removeAll();
		TableLayout layout = new TableLayout();
		double[] size = {TableLayout.FILL};
		layout.setColumn(size);
		protocolComponent.setLayout(layout);
		if (list.size() == 0) return 0;
		Iterator i = list.iterator();
		FileAnnotationData fa;
		JXTaskPane pane;
		PreviewPanel preview;
		String description;
		String ns;
		int index = 0;
		previews.clear();
		while (i.hasNext()) {
			fa = (FileAnnotationData) i.next();
			ns = fa.getNameSpace();
			if (fa.getId() > 0 && isEditorFile(ns)) {
				description = fa.getDescription();
				if (description != null) {
					preview = new PreviewPanel(description, fa.getId());
					previews.add(preview);
					preview.addPropertyChangeListener(controller);
					pane = EditorUtil.createTaskPane(fa.getFileName());
					pane.add(preview);
					pane.setCollapsed(true);
					layout.insertRow(index, TableLayout.PREFERRED);
					protocolComponent.add(pane, "0, "+index);
					index++;
				}
			}
		}
		
		return index;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view			Reference to the View. Mustn't be <code>null</code>.
	 * @param model			Reference to the Model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the Control. 
	 * 						Mustn't be <code>null</code>.
	 */
	GeneralPaneUI(EditorUI view, EditorModel model, EditorControl controller)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.model = model;
		this.controller = controller;
		this.view = view;
		initComponents();
        buildGUI();
	}

	/** Lays out the UI when data are loaded. */
	void layoutUI()
	{
		propertiesUI.buildUI();
		annotationUI.buildUI();
		textualAnnotationsUI.buildUI();
		TableLayout layout = (TableLayout) content.getLayout();
		double h = 0;
		String s = "";
		boolean multi = model.isMultiSelection();
		Object refObject = model.getRefObject();
		if (refObject instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) refObject;
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
					tag.getNameSpace())) {
				browserTaskPane.setCollapsed(true);
			} else {
				if (!multi) {
					h = TableLayout.PREFERRED;
					s = "Contained in Tag Sets";
				}
			}
		} else if (refObject instanceof DatasetData) {
			if (!multi) {
				h = TableLayout.PREFERRED;
				s = "Contained in Projects";
			}
		} else if (refObject instanceof ImageData) {
			if (!multi) {
				h = TableLayout.PREFERRED;
				s = "Contained in Datasets";
				controller.loadChannelData();
			}
		} else if (refObject instanceof WellSampleData) {
			if (!multi) {
				controller.loadChannelData();
			}
		} 
		/*
		else if ((refObject instanceof ProjectData) || 
				(refObject instanceof ScreenData) ||
				(refObject instanceof WellSampleData)) {
			browserTaskPane.setCollapsed(true);
		}*/
		browserTaskPane.setTitle(s);
		content.remove(browserTaskPane);
		
		if (h != 0.0) {
			//layout.setRow(browserIndex, h);
			content.add(browserTaskPane, "0, "+browserIndex);
		}
		int n = buildProtocolTaskPanes();
		double hp = 0;
		if (n > 0) hp = TableLayout.PREFERRED;
		layout.setRow(protocolsIndex, hp);
		if (h != 0.0 && !browserTaskPane.isCollapsed()) {
			loadParents(true);
		}
	}
	
	/** 
	 * Returns an array of size 2 with the collection of 
	 * annotation to save. 
	 * 
	 * @return See above.
	 */
	List<AnnotationData>[] prepareDataToSave()
	{
		if (!model.isMultiSelection()) propertiesUI.updateDataObject();
		List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
		List<AnnotationData> toRemove = new ArrayList<AnnotationData>();
		List<AnnotationData> l = annotationUI.getAnnotationToSave();
		//To add
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		l = textualAnnotationsUI.getAnnotationToSave();
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		//To remove
		l = annotationUI.getAnnotationToRemove();
		if (l != null && l.size() > 0)
			toRemove.addAll(l);
		l = textualAnnotationsUI.getAnnotationToRemove();
		if (l != null && l.size() > 0)
			toRemove.addAll(l);
		List<AnnotationData>[] array = new List[2];
		array[0] = toAdd;
		array[1] = toRemove;
		return array;
		
	}
	
	/** Updates display when the new root node is set. */
	void setRootObject()
	{
		clearData();
		textualAnnotationsUI.clearDisplay();
		propertiesUI.clearDisplay();
		annotationUI.clearDisplay();
    	textualAnnotationsUI.clearDisplay();
    	propertiesUI.buildUI();
    	Object uo = model.getRefObject();
    	TableLayout layout = (TableLayout) content.getLayout();
    	if (uo instanceof AnnotationData) { //hide everything
    		layout.setRow(annotationLayoutIndex, 0);
    		layout.setRow(textualAnnotationsLayoutIndex, 0);
    		layout.setRow(browserIndex, 0);
    	} else {
    		layout.setRow(annotationLayoutIndex, TableLayout.PREFERRED);
    		layout.setRow(textualAnnotationsLayoutIndex, TableLayout.PREFERRED);
    		if (model.isMultiSelection()) layout.setRow(browserIndex, 0);
    		else layout.setRow(browserIndex, TableLayout.PREFERRED);
    	}
		revalidate();
    	repaint();
	}
	
	/** Lays out the thumbnails. */
	void setThumbnails() { annotationUI.setThumbnails(); }
	
	/**
	 * Returns the list of tags currently selected by the user.
	 * 
	 * @return See above.
	 */
	List<TagAnnotationData> getCurrentTagsSelection()
	{
		return annotationUI.getCurrentTagsSelection();
	}
	
	/**
	 * Returns the list of attachments currently selected by the user.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> getCurrentAttachmentsSelection() 
	{
		return annotationUI.getCurrentAttachmentsSelection();
	}
	
	/** Shows the image's info. */
    void setChannelData()
    { 
    	Object refObject = model.getRefObject();
    	if ((refObject instanceof ImageData) || 
    			(refObject instanceof WellSampleData))
    		propertiesUI.setChannelData(model.getChannelData());
    }
    
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		Iterator<AnnotationUI> i = components.iterator();
		AnnotationUI ui;
		while (i.hasNext()) {
			ui = i.next();
			if (ui.hasDataToSave())
				return true;
		}
		Iterator<PreviewPanel> p = previews.iterator();
		PreviewPanel pp;
		while (p.hasNext()) {
			pp = p.next();
			if (pp.hasDataToSave())
				return true;
		}
		return false;
	}
	
	/** Clears data to save. */
	void clearData()
	{
		Iterator<AnnotationUI> i = components.iterator();
		AnnotationUI ui;
		while (i.hasNext()) {
			ui = i.next();
			ui.clearData();
			ui.clearDisplay();
		}
		setCursor(Cursor.getDefaultCursor());
	}
	
	/**
	 * Handles the expansion or collapsing of the passed component.
	 * 
	 * @param source The component to handle.
	 */
	void handleTaskPaneCollapsed(JXTaskPane source)
	{
		if (source == null) return;
		if  (source.equals(browserTaskPane)) 
			loadParents(!browserTaskPane.isCollapsed());
	}

	/**
	 * Attaches the passed file.
	 * 
	 * @param file The file to attach.
	 */
	void attachFile(File file) { annotationUI.attachFile(file); }

	/**
	 * Removes the passed file from the display.
	 * 
	 * @param file The file to remove.
	 */
	void removeAttachedFile(Object file)
	{ 
		annotationUI.removeAttachedFile(file);
	}
	
	/**
	 * Removes a tag from the view.
	 * 
	 * @param tag The tag to remove.
	 */
	void removeTag(TagAnnotationData tag)
	{
		if (tag == null) return;
		annotationUI.removeTag(tag);
	}
	
	/**
	 * Handles the selection of objects via the selection wizard.
	 * 
	 * @param type		The type of objects to handle.
	 * @param objects   The objects to handle.
	 */
	void handleObjectsSelection(Class type, Collection objects)
	{
		if (objects == null) return;
		annotationUI.handleObjectsSelection(type, objects);
	}
	
}
