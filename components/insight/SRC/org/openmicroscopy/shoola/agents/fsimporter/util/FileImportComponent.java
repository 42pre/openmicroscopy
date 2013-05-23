/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.util;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import omero.cmd.CmdCallback;
import omero.cmd.CmdCallbackI;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FilesetData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Component hosting the file to import and displaying the status of the 
 * import process.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FileImportComponent 
	extends JPanel
	implements ActionListener, PropertyChangeListener
{
	/** Indicates that the container is of type <code>Project</code>. */
	public static final int PROJECT_TYPE = 0;
	
	/** Indicates that the container is of type <code>Screen</code>. */
	public static final int SCREEN_TYPE = 1;
	
	/** Indicates that the container is of type <code>Dataset</code>. */
	public static final int DATASET_TYPE = 2;
	
	/** Indicates that no container specified. */
	public static final int NO_CONTAINER = 3;
	
	/** Bound property indicating to retry an upload.*/
	public static final String RETRY_PROPERTY = "retry";
	
	/** 
	 * Bound property indicating that the error to submit is selected or not.
	 */
	public static final String SUBMIT_ERROR_PROPERTY = "submitError";
	
	/** Bound property indicating to display the error.*/
	public static final String DISPLAY_ERROR_PROPERTY = "displayError";
	
	/** Bound property indicating to cancel the import.*/
	public static final String CANCEL_IMPORT_PROPERTY = "cancelImport";
	
	/** Bound property indicating to browse the node. */
	public static final String BROWSE_PROPERTY = "browse";
	
	/** Bound property indicating to increase the number of files to import. */
	public static final String IMPORT_FILES_NUMBER_PROPERTY = "importFilesNumber";
	
	/**
	 * Bound property indicating to load the content of the log file.
	 */
	public static final String LOAD_LOGFILEPROPERTY = "loadLogfile";

	/**
	 * Bound property indicating to retrieve the log file.
	 */
	public static final String RETRIEVE_LOGFILEPROPERTY = "retrieveLogfile";
	
	/**
	 * Bound property indicating to show the checksums,
	 */
	public static final String CHECKSUM_DISPLAY_PROPERTY = "checksumDisplay";
	
	/** The default size of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);

	/** The number of extra labels for images to add. */
	public static final int MAX_THUMBNAILS = 3;

	/** Action id to delete the image. */
	private static final int DELETE_ID = 0;
	
	/** Action id to cancel the import before it starts. */
	private static final int CANCEL_ID = 1;

	/** One of the constants defined by this class. */
	private int type;
	
	/** The file to import. */
	private File file;
	
	/** The component indicating the progress of the import. */
	private JXBusyLabel busyLabel;
	
	/** The component displaying the file name. */
	private JPanel namePane;

	/** The component displaying the result. */
	private JLabel resultLabel;
	
	/** The component displaying the imported image. */
	private ThumbnailLabel imageLabel;
	
	/** Keeps track of the extra images if any. */
	private List<ThumbnailLabel> imageLabels;

	/** The imported image. */
	private Object image;

	/** Indicates the status of the on-going import. */
	private StatusLabel statusLabel;
	
	/** The component displaying the name of the file. */
	private JLabel fileNameLabel;
	
	/** Keep tracks of the components. */
	private Map<File, FileImportComponent> components;
	
	/** The mouse adapter to view the image. */
	private MouseAdapter adapter;

	/** Flag indicating to use the folder as container. */
	private boolean folderAsContainer;

	/** The data object corresponding to the folder. */
	private DataObject containerFromFolder;
	
	/** Button to cancel the import for that file. */
	private JLabel cancelButton;
	
	/** The node where to import the folder. */
	private DataObject data;
	
	/** The dataset if any. */
	private DatasetData dataset;
	
	/** The node of reference if any. */
	private Object refNode;
	
	/** The object where the data have been imported.*/
	private DataObject containerObject;
	
	/** The component used when importing a folder. */
	private JXTaskPane pane;
	
	/** The parent of the node. */
	private FileImportComponent parent;
	
	/** The total number of files to import. */
	private int totalFiles;
	
	/** The value indicating the number of imports in the folder. */
	private int importCount;

	/** 
	 * Flag indicating that the container hosting the imported image
	 * can be browsed or not depending on how the import is launched.
	 */
	private boolean browsable;
	
	/** Set to <code>true</code> if attempt to re-import.*/
	private boolean reimported;
	
	/** The group in which to import the file.*/
	private GroupData group;

	/** The user that will own the data being imported */
	private ExperimenterData user;
	
	/** Flag indicating the the user is member of one group only.*/
	private boolean singleGroup;

	/** The log file if any to load.*/
	private FileAnnotationData logFile;

	/** The button displayed the various options post if the import worked.*/
	private JButton actionMenuButton;
	
	/** The popup menu associated with the action button */
	private JPopupMenu menu;
	
	/** The state of the import */
	private ImportStatus resultIndex;
	
	/** The index associated to the main component.*/
	private int index;
	
	/** Reference to the callback.*/
	private CmdCallback callback;
	
	/** Retries to upload the file.*/
	private void retry()
	{
		Object o = statusLabel.getImportResult();
		if (o instanceof Exception || image instanceof Exception)
			firePropertyChange(RETRY_PROPERTY, null, this);
	}
	/**
	 * Creates or recycles the menu corresponding to the import status.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createActionMenu()
	{
		if (menu != null) return menu;
		menu = new JPopupMenu();
		JMenuItem item;
		String logText = "View Import Log";
		String checksumText = "View Checksum";
		switch (resultIndex) {
			case FAILURE:
				menu.add(new JMenuItem(new AbstractAction("Submit") {
					public void actionPerformed(ActionEvent e) {
						submitError();
					}
				}));
				break;
			case UPLOAD_FAILURE:
				menu.add(new JMenuItem(new AbstractAction("Retry") {
					public void actionPerformed(ActionEvent e) {
						retry();
					}
				}));
				break;
			case SUCCESS:
				logText = "Import Log";
				checksumText = "Checksum";
				item = new JMenuItem(new AbstractAction("In Full Viewer") {
					public void actionPerformed(ActionEvent e) {
						launchFullViewer();
					}
				});
				boolean b = false;
				if (image instanceof List) b = ((List) image).size() == 1;
				item.setEnabled(b);
				menu.add(item);
				item = new JMenuItem(new AbstractAction("In Data Browser") {
					public void actionPerformed(ActionEvent e) {
						browse();
					}
				});
				item.setEnabled(browsable);
				menu.add(item);
		}
		item = new JMenuItem(new AbstractAction(logText) {
            public void actionPerformed(ActionEvent e) {
            	displayLogFile();
            }
        });
		Object o = statusLabel.getImportResult();
		item.setEnabled(callback != null || o instanceof CmdCallback);
		menu.add(item);
        
		item = new JMenuItem(new AbstractAction(checksumText) {
            public void actionPerformed(ActionEvent e) {
            	showChecksumDetails();
            }
		});
		item.setEnabled(statusLabel.hasChecksum());
		menu.add(item);
		return menu;
	}
	
	/** Displays or loads the log file.*/
	private void displayLogFile()
	{
		if (logFile != null)
    		firePropertyChange(LOAD_LOGFILEPROPERTY, null, logFile);
    	else {
    		firePropertyChange(RETRIEVE_LOGFILEPROPERTY, null,
    				statusLabel.getFileset());
    	}
	}

	/**
	 * Displays the checksum details dialog for the file(s) in this entry
	 */
	private void showChecksumDetails()
	{
		firePropertyChange(CHECKSUM_DISPLAY_PROPERTY, null, statusLabel);
	}

	/**
	 * Formats the tool tip of a successful import.
	 * 
	 * @return See above.
	 */
	private void formatResultTooltip()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		buf.append("<b>Date Uploaded: </b>");
		buf.append(UIUtilities.formatShortDateTime(null));
		buf.append("<br>");
		if (image instanceof PlateData) {
			PlateData p = (PlateData) image;
			buf.append("<b>Plate ID: </b>");
			buf.append(p.getId());
			buf.append("<br>");
		}
		if (!statusLabel.isHCS()) {
			Object o = statusLabel.getImportResult();
			if (o instanceof Set) {
				Set<PixelsData> list = (Set<PixelsData>) o;
				int n = list.size();
				if (n == 1) {
					buf.append("<b>Image ID: </b>");
					Iterator<PixelsData> i = list.iterator();
					while (i.hasNext()) {
						buf.append(i.next().getImage().getId());
						buf.append("<br>");
					}
				} else if (n > 1) {
					buf.append("<b>Number of Images: </b>");
					buf.append(n);
					buf.append("<br>");
				}
			}
		}
		buf.append("<b>Size: </b>");
		buf.append(FileUtils.byteCountToDisplaySize(statusLabel.getFileSize()));
		buf.append("<br>");
		buf.append("<b>Group: </b>");
		buf.append(group.getName());
		buf.append("<br>");
		buf.append("<b>Owner: </b>");
		buf.append(EditorUtil.formatExperimenter(user));
		buf.append("<br>");
		if (containerObject instanceof ProjectData) {
			buf.append("<b>Project: </b>");
			buf.append(((ProjectData) containerObject).getName());
			buf.append("<br>");
		} else if (containerObject instanceof ScreenData) {
			buf.append("<b>Screen: </b>");
			buf.append(((ScreenData) containerObject).getName());
			buf.append("<br>");
		} else if (containerObject instanceof DatasetData) {
			buf.append("<b>Dataset: </b>");
			buf.append(((DatasetData) containerObject).getName());
			buf.append("<br>");
		} else if (dataset != null) {
			buf.append("<b>Dataset: </b>");
			buf.append(dataset.getName());
			buf.append("<br>");
		}
		buf.append("</body></html>");
		String tip = buf.toString();
		fileNameLabel.setToolTipText(tip);
		resultLabel.setToolTipText(tip);
	}
	
	/** Indicates that the import was successful or if it failed.*/
	private void formatResult()
	{
		if (callback != null) {
			try {
				((CmdCallbackI) callback).close(true);
			} catch (Exception e) {}
		}
		
		resultLabel.setVisible(true);
		busyLabel.setVisible(false);
		busyLabel.setBusy(false);
		IconManager icons = IconManager.getInstance();
		Object result = statusLabel.getImportResult();
		if (image instanceof ImportException) result = image;
		if (result instanceof ImportException) {
			ImportException e = (ImportException) result;
			resultLabel.setIcon(icons.getIcon(IconManager.DELETE));
			resultLabel.setToolTipText(
					UIUtilities.formatExceptionForToolTip(e));
			actionMenuButton.setVisible(true);
			actionMenuButton.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
			actionMenuButton.setText("Failed");
			if (e.getStatus() == ImportException.CHECKSUM_MISMATCH)
				resultIndex = ImportStatus.UPLOAD_FAILURE;
			else resultIndex = ImportStatus.FAILURE;
		} else if (result instanceof CmdCallback) {
			callback = (CmdCallback) result;
		} else {
			if (!statusLabel.isMarkedAsCancel() &&
				!statusLabel.isMarkedAsDuplicate()) {
				formatResultTooltip();
				resultLabel.setIcon(icons.getIcon(IconManager.APPLY));
				actionMenuButton.setVisible(true);
				actionMenuButton.setForeground(UIUtilities.HYPERLINK_COLOR);
				actionMenuButton.setText("View");
				resultIndex = ImportStatus.SUCCESS;
			}
		}
	}

	/** Submits the error.*/
	private void submitError()
	{
		Object o = statusLabel.getImportResult();
		if (o instanceof Exception)
			firePropertyChange(SUBMIT_ERROR_PROPERTY, null, this);
	}
	
	/** Sets the text indicating the number of import. */
	private void setNumberOfImport()
	{
		if (pane == null) return;
		String end = " file";
		if (totalFiles > 1) end +="s";
		String text = file.getName()+": "+importCount+" of "+totalFiles+end;
		pane.setTitle(text);
	}
	
	/** Browses the node or the data object. */
	private void browse()
	{
		EventBus bus = ImporterAgent.getRegistry().getEventBus();
		Object d = dataset;
		if (dataset == null || data instanceof ScreenData) d = data;
		if (d == null) return;
		bus.post(new BrowseContainer(d, null));
	}
	
	/** Indicates that the file will not be imported. 
	 * 
	 * @param fire	Pass <code>true</code> to fire a property,
	 * 				<code>false</code> otherwise.
	 */
	private void cancel(boolean fire)
	{
		if (busyLabel.isBusy() && !statusLabel.isCancellable())
			return;
		busyLabel.setBusy(false);
		busyLabel.setVisible(false);
		statusLabel.markedAsCancel();
		cancelButton.setEnabled(false);
		cancelButton.setVisible(false);
		if (fire)
			firePropertyChange(CANCEL_IMPORT_PROPERTY, null, this);
	}
	
	/** Deletes the image that was imported but cannot be viewed. */
	private void deleteImage()
	{
		List<DeletableObject> l = new ArrayList<DeletableObject>();
		
		if (image instanceof ThumbnailData) {
			l.add(new DeletableObject(((ThumbnailData) image).getImage()));
		} else if (image instanceof ImageData) {
			l.add(new DeletableObject((DataObject) image));
		}
		if (l.size() == 0) return;
		IconManager icons = IconManager.getInstance();
		DeleteActivityParam p = new DeleteActivityParam(
				icons.getIcon(IconManager.APPLY_22), l);
		p.setFailureIcon(icons.getIcon(IconManager.DELETE_22));
		fileNameLabel.setEnabled(false);
		resultLabel.setEnabled(false);
		imageLabel.setEnabled(false);
	}
	
	/**
	 * Launches the full viewer for the selected item.
	 */
	private void launchFullViewer()
	{
		ViewImage evt;
		int plugin = ImporterAgent.runAsPlugin();
		if (image == null) image = statusLabel.getImportResult();
		Object ho = image;
		if (image instanceof List) {
			List l = (List) image;
			if (CollectionUtils.isEmpty(l) || l.size() > 1) return;
			ho = l.get(0);
		}
		if (ho instanceof ThumbnailData) {
			ThumbnailData data = (ThumbnailData) ho;
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			evt = new ViewImage(new SecurityContext(group.getId()),
					new ViewImageObject(data.getImageID()), null);
			evt.setPlugin(plugin);
			bus.post(evt);
		} else if (ho instanceof PixelsData) {
			PixelsData data = (PixelsData) image;
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			evt = new ViewImage(
					new SecurityContext(group.getId()),
					new ViewImageObject(data.getImage().getId()), null);
			evt.setPlugin(plugin);
			bus.post(evt);
		} else if (image instanceof PlateData) {
			firePropertyChange(BROWSE_PROPERTY, null, image);
		}
	}
	/** Initializes the components. */
	private void initComponents()
	{
		actionMenuButton = new JButton();
		actionMenuButton.setVisible(false);
		actionMenuButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ev) {
				JPopupMenu popup = createActionMenu();
				popup.show(actionMenuButton, 0, actionMenuButton.getHeight());
			}
		});
		
		adapter = new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{ 
				if (e.getClickCount() == 1) {
					launchFullViewer();
				}
			}
		};
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		busyLabel = new JXBusyLabel(SIZE);
		busyLabel.setVisible(false);
		busyLabel.setBusy(false);
		
		cancelButton = new JLabel("Cancel");
		cancelButton.setForeground(UIUtilities.HYPERLINK_COLOR);
		cancelButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Browses the object the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				Object src = e.getSource();
				if (e.getClickCount() == 1 && src instanceof JLabel) {
					cancel(true);
				}
			}
		});
		cancelButton.setVisible(true);
		
		namePane = new JPanel();
		namePane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		IconManager icons = IconManager.getInstance();
		Icon icon;
		if (file.isFile()) icon = icons.getIcon(IconManager.IMAGE);
		else icon = icons.getIcon(IconManager.DIRECTORY);
		imageLabel = new ThumbnailLabel(icon);
		imageLabel.addPropertyChangeListener(this);
		imageLabels = new ArrayList<ThumbnailLabel>();
		ThumbnailLabel label;
		for (int i = 0; i < MAX_THUMBNAILS; i++) {
			label = new ThumbnailLabel();
			if (i == MAX_THUMBNAILS-1) {
				Font f = label.getFont();
				label.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
			}
			label.setVisible(false);
			imageLabels.add(label);
		}
		fileNameLabel = new JLabel(file.getName());
		namePane.add(imageLabel);
		Iterator<ThumbnailLabel> j = imageLabels.iterator();
		while (j.hasNext()) {
			namePane.add(j.next());
		}
		namePane.add(Box.createHorizontalStrut(4));
		namePane.add(fileNameLabel);
		namePane.add(Box.createHorizontalStrut(10));
		resultLabel = new JLabel();
		statusLabel = new StatusLabel();
		statusLabel.addPropertyChangeListener(this);
		image = null;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
		add(namePane);
		add(statusLabel);
		
		add(busyLabel);
		add(resultLabel);
		add(cancelButton);
		add(actionMenuButton);
	}

	/** 
	 * Attaches the listeners to the newly created component.
	 * 
	 * @param c The component to handle.
	 */
	private void attachListeners(FileImportComponent c)
	{
		PropertyChangeListener[] listeners = getPropertyChangeListeners();
		if (listeners != null && listeners.length > 0) {
			for (int j = 0; j < listeners.length; j++) {
				c.addPropertyChangeListener(listeners[j]);
			}
		}
	}
	
	/**
	 * Adds the specified files to the list of import data.
	 * 
	 * @param files The files to import.
	 */
	private void insertFiles(Map<File, StatusLabel> files)
	{
		if (files == null || files.size() == 0) return;
		components = new HashMap<File, FileImportComponent>();
		totalFiles = files.size();
		
		Entry<File, StatusLabel> entry;
		Iterator<Entry<File, StatusLabel>> i = files.entrySet().iterator();
		FileImportComponent c;
		File f;
		DatasetData d = dataset;
		Object node = refNode;
		if (folderAsContainer) {
			node = null;
			d = new DatasetData();
			d.setName(file.getName());
		}
		while (i.hasNext()) {
			entry = i.next();
			f = entry.getKey();
			c = new FileImportComponent(f, folderAsContainer, browsable, group,
					user, singleGroup, getIndex());
			if (f.isFile()) {
				c.setLocation(data, d, node);
				c.setParent(this);
			}
			c.setType(getType());
			attachListeners(c);
			c.setStatusLabel(entry.getValue());
			entry.getValue().addPropertyChangeListener(this);
			components.put((File) entry.getKey(), c);
		}
		
		removeAll();
		pane = EditorUtil.createTaskPane("");
		pane.setCollapsed(false);
		setNumberOfImport();

		IconManager icons = IconManager.getInstance();
		pane.setIcon(icons.getIcon(IconManager.DIRECTORY));
		Font font = pane.getFont();
		pane.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
		layoutEntries(false);
		double[][] size = {{TableLayout.FILL}, {TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		add(pane, new TableLayoutConstraints(0, 0));
		validate();
		repaint();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file The file to import.
	 * @param folderAsContainer Pass <code>true</code> if the passed file
	 * 							has to be used as a container, 
	 * 							<code>false</code> otherwise.
	 * @param browsable Flag indicating that the container can be browsed or not.
	 * @param group The group in which to import the file.
	 * @param user The user that will own the data being imported
	 * @param singleGroup Passes <code>true</code> if the user is member of
	 * only one group, <code>false</code> otherwise.
	 * @param index The index of the parent.
	 */
	public FileImportComponent(File file, boolean folderAsContainer, boolean
			browsable, GroupData group, ExperimenterData user,
			boolean singleGroup, int index)
	{
		if (file == null)
			throw new IllegalArgumentException("No file specified.");
		if (group == null)
			throw new IllegalArgumentException("No group specified.");
		this.index = index;
		this.file = file;
		this.group = group;
		this.user = user;
		this.singleGroup = singleGroup;
		importCount = 0;
		this.browsable = browsable;
		this.folderAsContainer = folderAsContainer;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Returns the file hosted by this component.
	 * 
	 * @return See above.
	 */
	public File getFile() { return file; }
	
	/**
	 * Sets the location where to import the files.
	 * 
	 * @param data The data where to import the folder or screening data.
	 * @param dataset The dataset if any.
	 * @param refNode The node of reference.
	 */
	public void setLocation(DataObject data, DatasetData dataset, 
			Object refNode)
	{
		this.data = data;
		this.dataset = dataset;
		this.refNode = refNode;
		if (refNode != null && refNode instanceof TreeImageDisplay) {
			TreeImageDisplay n = (TreeImageDisplay) refNode;
			Object ho = n.getUserObject();
			if (ho instanceof DatasetData || ho instanceof ProjectData ||
				ho instanceof ScreenData) {
				containerObject = (DataObject) ho;
			}
			return;
		}
		if (dataset != null) {
			containerObject = dataset;
			return;
		}
		if (data != null && data instanceof ScreenData) {
			containerObject = data;
		}
	}
	
	/**
	 * Sets the log file annotation.
	 * 
	 * @param data The annotation associated to the file set.
	 * @param id The id of the file set.
	 */
	public void setImportLogFile(Collection<FileAnnotationData> data, long id)
	{
		FilesetData fs = statusLabel.getFileset();
		if (fs == null) return;
		if (id != fs.getId() || data == null) return;
		Iterator<FileAnnotationData> i = data.iterator();
		FileAnnotationData fa;
		while (i.hasNext()) {
			fa = i.next();
			//Check name space
			if (FileAnnotationData.LOG_FILE_NS.equals(fa.getNameSpace())) {
				logFile = fa;
				break;
			}
		}
		if (logFile != null) {
			firePropertyChange(LOAD_LOGFILEPROPERTY, null, logFile);
		}
	}

	/**
	 * Returns the dataset or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public DatasetData getDataset() { return dataset; }
	
	/**
	 * Returns the object or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public DataObject getDataObject() { return data; }
	
	/**
	 * Replaces the initial status label.
	 * 
	 * @param label The value to replace.
	 */
	void setStatusLabel(StatusLabel label)
	{
		statusLabel = label;
		statusLabel.addPropertyChangeListener(this);
		buildGUI();
		revalidate();
		repaint();
	}
	
	/** Increases the number of imports. */
	void increaseNumberOfImport()
	{
		importCount++;
		setNumberOfImport();
	}
	
	/** 
	 * Sets the parent of the component.
	 * 
	 * @param parent The value to set.
	 */
	void setParent(FileImportComponent parent)
	{
		this.parent = parent;
	}
	
	/**
	 * Returns <code>true</code> if the parent is set.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasParent() { return parent != null; }
	
	/**
	 * Returns the components displaying the status of an on-going import.
	 * 
	 * @return See above.
	 */
	public StatusLabel getStatus() { return statusLabel; }
	
	/**
	 * Sets the result of the import.
	 * @param image The image.
	 */
	public void setStatus(Object image)
	{
		cancelButton.setVisible(false);
		importCount++;
		this.image = image;
		if (parent != null) parent.increaseNumberOfImport();
		if (image instanceof PlateData) {
			imageLabel.setData((PlateData) image);
			fileNameLabel.addMouseListener(adapter);
			formatResult();
			formatResultTooltip();
		} else if (image instanceof List) {
			List<ThumbnailData> list = new ArrayList<ThumbnailData>((List) image);
			int m = list.size();
			imageLabel.setData(list.get(0));
			list.remove(0);
			if (list.size() > 0) {
				ThumbnailLabel label = imageLabels.get(0);
				label.setVisible(true);
				label.setData(list.get(0));
				list.remove(0);
				if (list.size() > 0) {
					label = imageLabels.get(1);
					label.setVisible(true);
					label.setData(list.get(0));
					list.remove(0);
					int n = statusLabel.getSeriesCount()-m;
					if (n > 0) {
						label = imageLabels.get(2);
						label.setVisible(true);
						StringBuffer buf = new StringBuffer( "... ");
						buf.append(n);
						buf.append(" more");
						label.setText(buf.toString());
					}
				}
			}
			formatResult();
		} else if (image instanceof ImportException) {
			formatResult();
		} else if (image instanceof Boolean) {
			busyLabel.setBusy(false);
			busyLabel.setVisible(false);
			cancelButton.setVisible(false);
			if (statusLabel.isMarkedAsCancel() ||
					statusLabel.isMarkedAsDuplicate()) {
				resultIndex = ImportStatus.IGNORED;
			}
		}
		repaint();
	}

	/**
	 * Returns the files that failed to import.
	 * 
	 * @return See above.
	 */
	public List<FileImportComponent> getImportErrors()
	{
		List<FileImportComponent> l = null;
		if (file.isFile()) {
			Object r = statusLabel.getImportResult();
			if (r instanceof Exception || image instanceof Exception) {
				l = new ArrayList<FileImportComponent>();
				l.add(this);
				return l;
			}
		} else {
			if (components != null) {
				Iterator<FileImportComponent> i = components.values().iterator();
				FileImportComponent fc;
				l = new ArrayList<FileImportComponent>();
				List<FileImportComponent> list;
				while (i.hasNext()) {
					fc = i.next();
					list = fc.getImportErrors();
					if (!CollectionUtils.isEmpty(list))
						l.addAll(list);
				}
			}
		}
		return l;
	}
	
	/**
	 * Returns the import error object.
	 * 
	 * @return See above.
	 */
	public ImportErrorObject getImportErrorObject()
	{
		Object r = statusLabel.getImportResult();
		Exception e = null;
		if (r instanceof Exception) e = (Exception) r;
		else if (image instanceof Exception) e = (Exception) image;
		if (e == null) return null;
		ImportErrorObject object = new ImportErrorObject(file, e);
		object.setReaderType(statusLabel.getReaderType());
		object.setUsedFiles(statusLabel.getUsedFiles());
		return object;
	}
	
	/**
	 * Returns <code>true</code> if the import has failed, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasImportFailed()
	{
		return resultIndex == ImportStatus.FAILURE ||
				resultIndex == ImportStatus.UPLOAD_FAILURE;
	}
	
	/**
	 * Returns <code>true</code> if the import has been cancelled,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isCancelled()
	{
		return statusLabel.isMarkedAsCancel();
	}
	
	/**
	 * Returns <code>true</code> if the file can be re-imported,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFailuresToReimport()
	{
		if (file.isFile()) {
			return (resultIndex == ImportStatus.UPLOAD_FAILURE && !reimported);
		}
		if (components == null) return false;
		Iterator<FileImportComponent> i = components.values().iterator();
		while (i.hasNext()) {
			if (i.next().hasFailuresToReimport())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> the error can be submitted, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFailuresToSend()
	{
		if (file.isFile()) return resultIndex == ImportStatus.FAILURE;
		if (components == null) return false;
		Iterator<FileImportComponent> i = components.values().iterator();
		while (i.hasNext()) {
			if (i.next().hasFailuresToSend())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the folder has components added,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasComponents()
	{
		return components != null && components.size() > 0;
	}
	
	/** 
	 * Lays out the entries.
	 * 
	 * @param failure Pass <code>true</code> to display the failed import only,
	 * <code>false</code> to display all the entries.
	 */
	public void layoutEntries(boolean failure)
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		if (!hasComponents()) return;
		Entry<File, FileImportComponent> e;
		Iterator<Entry<File, FileImportComponent>> i =
				components.entrySet().iterator();
		int index = 0;
		FileImportComponent fc;
		if (failure) {
			while (i.hasNext()) {
				e = i.next();
				fc = e.getValue();
				if (fc.hasImportFailed()) {
					if (index%2 == 0)
						fc.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
					else 
						fc.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
					p.add(fc);
					index++;
				}
			}
		} else {
			while (i.hasNext()) {
				e = i.next();
				fc = e.getValue();
				if (index%2 == 0)
					fc.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
				else 
					fc.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
				p.add(fc);
				index++;
			}
		}
		
		pane.removeAll();
		pane.add(p);
		pane.revalidate();
		pane.repaint();
	}
	
	/**
	 * Returns the status of the import process one of the
	 * values defined in @see ImportStatus
	 * 
	 * @return See above.
	 */
	public ImportStatus getImportStatus()
	{
		if (file.isFile()) {
			if (hasImportFailed()) return ImportStatus.FAILURE;
			return resultIndex;
		}
		if (components == null || components.size() == 0) {
			if (image instanceof Boolean) {
				if (file.isDirectory()) {
					if  (isCancelled()) return ImportStatus.SUCCESS;
					return resultIndex;
				} else {
					if (!statusLabel.isMarkedAsCancel() &&
						!statusLabel.isMarkedAsDuplicate())
						return ImportStatus.FAILURE;
				}
			}
			return resultIndex;
		}
			
		Iterator<FileImportComponent> i = components.values().iterator();
		int n = components.size();
		int count = 0;
		while (i.hasNext()) {
			if (i.next().getImportStatus() == ImportStatus.FAILURE) 
				count++;
		}
		if (count == n) return ImportStatus.FAILURE;
		if (count > 0) return ImportStatus.PARTIAL;
		return ImportStatus.SUCCESS;
	}
	
	/**
	 * Returns <code>true</code> if refresh whole tree, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasToRefreshTree()
	{
		if (file.isFile()) {
			if (hasImportFailed()) return false;
			switch (type) {
				case PROJECT_TYPE:
				case NO_CONTAINER:
					return true;
				default:
					return false;
			}
		}
		if (components == null) return false;
		if (folderAsContainer && type != PROJECT_TYPE) {
			Iterator<FileImportComponent> i = components.values().iterator();
			while (i.hasNext()) {
				if (i.next().toRefresh()) 
					return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Returns <code>true</code> if some files were imported, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean toRefresh()
	{
		/*
		if (file.isFile()) {
			if (deleteButton.isVisible()) return false;
			else if (errorBox.isVisible())
				return !(errorBox.isEnabled() && errorBox.isSelected());
			return true;
		}
		if (components == null) return false;
		Iterator<FileImportComponent> i = components.values().iterator();
		int count = 0;
		while (i.hasNext()) {
			if (i.next().hasFailuresToSend()) 
				count++;
		}
		return components.size() != count;
		*/
		return true;
	}

	/** Indicates the import has been cancelled. */
	public void cancelLoading()
	{
		cancel(false);
		if (components == null) return;
		Iterator<FileImportComponent> i = components.values().iterator();
		while (i.hasNext()) {
			i.next().cancelLoading();
		}
	}
	
	/**
	 * Sets the type. 
	 * 
	 * @param type One of the constants defined by this class.
	 */
	public void setType(int type) { this.type = type; }
	
	/**
	 * Returns the supported type. One of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
	/**
	 * Returns <code>true</code> if the folder has been converted into a
	 * container, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFolderAsContainer() { return folderAsContainer; }
	
	/**
	 * Returns the object corresponding to the folder.
	 * 
	 * @return See above.
	 */
	public DataObject getContainerFromFolder() { return containerFromFolder; }
	
	/**
	 * Returns <code>true</code> if the extension of the specified file
	 * is a HCS files, <code>false</code> otherwise.
	 * 
	 * @param f The file to handle.
	 * @return See above.
	 */
	public boolean isHCSFile()
	{
		if (isFolderAsContainer()) return false;
		return ImportableObject.isHCSFile(file);
	}

	/**
	 * Returns <code>true</code> if the file has already been marked for
	 * re-import, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public List<FileImportComponent> getFilesToReupload()
	{
		List<FileImportComponent> l = null;
		if (file.isFile()) {
			if (resultIndex == ImportStatus.UPLOAD_FAILURE && !reimported) {
				return Arrays.asList(this);
			}
		} else {
			if (components != null) {
				Iterator<FileImportComponent> i = components.values().iterator();
				FileImportComponent fc;
				l = new ArrayList<FileImportComponent>();
				List<FileImportComponent> list;
				while (i.hasNext()) {
					fc = i.next();
					list = fc.getFilesToReupload();
					if (!CollectionUtils.isEmpty(list))
						l.addAll(list);
				}
			}
		}
		return l;
	}
	
	/**
	 * Sets to <code>true</code> to mark the file for reimport.
	 * <code>false</code> otherwise.
	 * 
	 * @param Pass <code>true</code> to mark the file for reimport.
	 * <code>false</code> otherwise.
	 */
	public void setReimported(boolean reimported)
	{ 
		this.reimported = reimported;
		repaint();
	}
	
	/**
	 * Sets the result of the import for the specified file.
	 * 
	 * @param result The result.
	 */
	public void uploadComplete(Object result)
	{
		if (result instanceof CmdCallback)
			callback = (CmdCallback) result;
	}

	/**
	 * Returns the index associated to the main component.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the result of the import either a collection of
	 * <code>PixelsData</code> or an exception.
	 * 
	 * @return See above.
	 */
	public Object getImportResult() { return statusLabel.getImportResult(); }
	
	/**
	 * Returns <code>true</code> if it is a HCS file, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isHCS() { return statusLabel.isHCS(); }
	
	/**
	 * Returns the size of the upload.
	 * 
	 * @return See above.
	 */
	public long getImportSize() { return statusLabel.getFileSize(); }
	
	/**
	 * Returns <code>true</code> if the result has already been set,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasResult() { return image != null; }
	
	/**
	 * Overridden to make sure that all the components have the correct 
	 * background.
	 * @see JPanel#setBackground(Color)
	 */
	public void setBackground(Color color)
	{
		if (busyLabel != null) busyLabel.setBackground(color);
		if (namePane != null) {
			namePane.setBackground(color);
			for (int i = 0; i < namePane.getComponentCount(); i++) 
				namePane.getComponent(i).setBackground(color);
		}
		super.setBackground(color);
	}

	/**
	 * Listens to property fired by the <code>StatusLabel</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (StatusLabel.FILES_SET_PROPERTY.equals(name)) {
			if (isCancelled()) {
				busyLabel.setBusy(false);
				busyLabel.setVisible(false);
				return;
			}
			Map<File, StatusLabel> files = (Map<File, StatusLabel>)
				evt.getNewValue();
			int n = files.size();
			insertFiles(files);
			firePropertyChange(IMPORT_FILES_NUMBER_PROPERTY, null,n);
		} else if (StatusLabel.FILE_IMPORT_STARTED_PROPERTY.equals(name)) {
			StatusLabel sl = (StatusLabel) evt.getNewValue();
			if (sl.equals(statusLabel) && busyLabel != null) {
				busyLabel.setBusy(false);
				busyLabel.setVisible(false);
				cancelButton.setVisible(sl.isCancellable());
			}
		} else if (StatusLabel.UPLOAD_DONE_PROPERTY.equals(name)) {
			StatusLabel sl = (StatusLabel) evt.getNewValue();
			if (sl.equals(statusLabel) && hasParent()) {
				importCount++;
				formatResult();
				firePropertyChange(StatusLabel.UPLOAD_DONE_PROPERTY, null, this);
			}
		} else if (StatusLabel.CANCELLABLE_IMPORT_PROPERTY.equals(name)) {
			StatusLabel sl = (StatusLabel) evt.getNewValue();
			if (sl.equals(statusLabel))
				cancelButton.setVisible(sl.isCancellable());
		} else if (StatusLabel.PROCESSING_ERROR_PROPERTY.equals(name)) {
			StatusLabel sl = (StatusLabel) evt.getNewValue();
			if (sl.equals(statusLabel)) {
				firePropertyChange(StatusLabel.IMPORT_DONE_PROPERTY, null, this);
			}
		} else if (StatusLabel.SCANNING_PROPERTY.equals(name)) {
			StatusLabel sl = (StatusLabel) evt.getNewValue();
			if (sl.equals(statusLabel)) {
				if (busyLabel != null) {
					busyLabel.setBusy(true);
					busyLabel.setVisible(true);
				}
				//cancelButton.setVisible(sl.isCancellable());
			}
		} else if (StatusLabel.FILE_RESET_PROPERTY.equals(name)) {
			file = (File) evt.getNewValue();
			fileNameLabel.setText(file.getName());
		} else if (ThumbnailLabel.BROWSE_PLATE_PROPERTY.equals(name)) {
			firePropertyChange(BROWSE_PROPERTY, evt.getOldValue(), 
					evt.getNewValue());
		} else if (StatusLabel.CONTAINER_FROM_FOLDER_PROPERTY.equals(name)) {
			containerFromFolder = (DataObject) evt.getNewValue();
			if (containerFromFolder instanceof DatasetData) {
				containerObject = containerFromFolder;
			} else if (containerFromFolder instanceof ScreenData) {
				containerObject = containerFromFolder;
			}
		} else if (StatusLabel.DEBUG_TEXT_PROPERTY.equals(name)) {
			firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
		} else if (ThumbnailLabel.VIEW_IMAGE_PROPERTY.equals(name)) {
			//use the group
			SecurityContext ctx = new SecurityContext(group.getId());
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			Long id = (Long) evt.getNewValue();
			bus.post(new ViewImage(ctx, new ViewImageObject(id), null));
		} else if (StatusLabel.IMPORT_DONE_PROPERTY.equals(name)) {
			firePropertyChange(StatusLabel.IMPORT_DONE_PROPERTY, null, this);
		}
	}

	/**
	 * Deletes the image if the image cannot be viewed.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{ 
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DELETE_ID:
				deleteImage();
				break;
			case CANCEL_ID:
				cancel(true);
		}
	}
	
	/**
	 * Returns the name of the file and group's id and user's id.
	 * @see #toString();
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(getFile().getAbsolutePath());
		if (group != null)
			buf.append("_"+group.getId());
		if (user != null)
			buf.append("_"+user.getId());
		return buf.toString();
	}
}
