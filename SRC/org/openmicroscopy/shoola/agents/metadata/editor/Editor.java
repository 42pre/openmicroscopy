/*
 * org.openmicroscopy.shoola.agents.metadata.editor.Editor 
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
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.FileAnnotationData;
import pojos.ImageAcquisitionData;

/** 
 * Defines the interface provided by the viewer component. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface Editor 
	extends ObservableComponent
{

	/** Identified the <code>Immersion</code> enumeration. */
	public static final String IMMERSION = OmeroMetadataService.IMMERSION;
	
	/** Identified the <code>Correction</code> enumeration. */
	public static final String CORRECTION = OmeroMetadataService.CORRECTION;
	
	/** Identified the <code>Medium</code> enumeration. */
	public static final String MEDIUM = OmeroMetadataService.MEDIUM;
	
	/** Identified the <code>Detector type</code> enumeration. */
	public static final String DETECTOR_TYPE = 
					OmeroMetadataService.DETECTOR_TYPE;
	
	/** Identified the <code>Laser medium</code> enumeration. */
	public static final String LASER_MEDIUM = 
					OmeroMetadataService.LASER_MEDIUM;
	
	/** Identified the <code>Laser medium</code> enumeration. */
	public static final String LASER_PULSE = 
					OmeroMetadataService.LASER_PULSE;
	
	/** Identified the <code>Binning</code> enumeration. */
	public static final String BINNING = OmeroMetadataService.BINNING;
	
	/** Identified the <code>contrast method</code> enumeration. */
	public static final String CONTRAST_METHOD = 
				OmeroMetadataService.CONTRAST_METHOD;
	
	/** Identified the <code>illumination type</code> enumeration. */
	public static final String ILLUMINATION_TYPE = 
				OmeroMetadataService.ILLUMINATION_TYPE;
	
	/** Identified the <code>photometric Interpretation</code> enumeration. */
	public static final String PHOTOMETRIC_INTERPRETATION = 
		OmeroMetadataService.PHOTOMETRIC_INTERPRETATION;
	
	/** Identified the <code>arc type</code> enumeration. */
	public static final String ARC_TYPE = OmeroMetadataService.ARC_TYPE;
	
	/** Identified the <code>filament type</code> enumeration. */
	public static final String FILAMENT_TYPE = 
		OmeroMetadataService.FILAMENT_TYPE;
	
	/** Identified the <code>laser type</code> enumeration. */
	public static final String LASER_TYPE = 
		OmeroMetadataService.LASER_TYPE;
	
	/** Identified the <code>mode</code> enumeration. */
	public static final String MODE = OmeroMetadataService.ACQUISITION_MODE;

	/** Feeds the metadata back to the editor. */
	public void setStructuredDataResults();
	
	/**
	 * Returns the View.
	 * 
	 * @return See above.
	 */
	public JComponent getUI();
	
	/**
	 * Sets the root of the tree.
	 * 
	 * @param refObject The object hosted by the root node of the tree.
	 * 					Mustn't be <code>null</code>.
	 */
	public void setRootObject(Object refObject);
	
	/**
	 * Sets the thumbnails of the image currently selected.
	 * 
	 * @param thumbnails 	The thumbnails to set, one per user.
	 * @param imageID		The id of the image the thumbnails are for.
	 */
	public void setThumbnails(Map<Long, BufferedImage> thumbnails, 
							long imageID);

	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param collection The tags to set.
	 */
	public void setExistingTags(Collection collection);

	/**
	 * Sets the collection of channel data.
	 * 
	 * @param list 			The collection to set.
	 * @param updateView 	Pass <code>true</code> to update the view,
	 * 						<code>false</code> otherwise.
	 */
	public void setChannelsData(List list, boolean updateView);

	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasDataToSave();

	/**
	 * Sets the collection of archived files.
	 * 
	 * @param folder The folder where to save the files.
	 * @param files The collection of files to handle.
	 */
	public void setDownloadedFiles(File folder, Collection files);

	/**
	 * Sets the used and free disk space.
	 * 
	 * @param list The value to set.
	 */
	public void setDiskSpace(List list);

	/**
	 * Indicates that the password was successfully modified or not.
	 * 
	 * @param changed 	Pass <code>true</code> if the password was successfully
	 * 					changed, <code>false</code> otherwise.
	 */
	public void passwordChanged(boolean changed);

	/** Brings up on screen the image's information. */
	public void loadChannelData();

	/**
	 * Sets the collection of existing attachments, attachments added
	 * by the currently logged in user.
	 * 
	 * @param attachments The value to set.
	 */
	public void setExistingAttachements(Collection attachments);
	
	/**
	 * Sets either to single selection or to multi selection.
	 * 
	 * @param single	Pass <code>true</code> when single selection, 
	 * 					<code>false</code> otherwise.
	 */
	public void setSelectionMode(boolean single);

	/** Loads the container hosting the currently edited object. */
	public void loadParents();

    /**
     * Sets to <code>true</code> if loading data, to <code>false</code>
     * otherwise.
     * 
     * @param busy 	Pass <code>true</code> while loading data, 
     * 				<code>false</code> otherwise.
     */
	public void setStatus(boolean busy);

	/** Loads the existing tags. */
	public void loadExistingTags();
	
	/**
	 * Deletes the specified annotation.
	 * 
	 * @param data The annotation to delete.
	 */
	public void deleteAnnotation(AnnotationData data);

	/**
	 * Sets the image acquisition data.
	 * 
	 * @param data The value to set.
	 */
	public void setImageAcquisitionData(ImageAcquisitionData data);

	/** Loads the image acquisition data. */
	public void loadImageAcquisitionData();

	/**
	 * Sets the enumerations for the metadata related to an image.
	 * 
	 * @param map The value to set.
	 */
	public void setImageEnumerations(Map map);

	/**
	 * Sets the enumerations for the metadata related to a channel.
	 * 
	 * @param map The value to set.
	 */
	public void setChannelEnumerations(Map map);

	/**
	 * Indicates to bring up on screen the manufacturer details.
	 * 
	 * @param comp	The component to display.
	 * @param point	The onscreen location of the mouse pressed.
	 */
	public void showManufacturer(JComponent comp, Point point);

	/**
	 * Sets the acquisition data for the specified channel.
	 * 
	 * @param index The index of the channel.
	 * @param data	The acquisition data.
	 */
	public void setChannelAcquisitionData(int index, 
			ChannelAcquisitionData data);

	/**
	 * Loads the channel acquisition for the specified channel.
	 * 
	 * @param channel The channel to handle.
	 */
	public void loadChannelAcquisitionData(ChannelData channel);

	/** Loads the existing attachments. */
	public void loadExistingAttachments();

	/** 
	 * Downloads the archived images. 
	 * 
	 * @param folder The folder to save the file into.
	 */
	public void download(File folder);

	/**
	 * Sets the parent of the root object. This will be taken into account
	 * only if the root is a well sample.
	 * 
	 * @param parentRefObject The parent of the root object.
	 */
	public void setParentRootObject(Object parentRefObject);

	/**
	 * Sets the plane info corresponding to the channel and the pixels set.
	 * 
	 * @param result	The collection of plane.
	 * @param pixelsID	The id of the pixels set.
	 * @param channel	The selected channel.
	 */
	public void setPlaneInfo(Collection result, long pixelsID, int channel);

	/**
	 * Sets the rendering control
	 * 
	 * @param rndControl The value to set.
	 */
	public void setRenderingControl(RenderingControl rndControl);

	/** Loads the rendering control for the first selected image. */
	public void loadRenderingControl();
	
	/** Indicates to render a plane. */
	public void renderPlane();

	/** Updates the view when the settings have been applied. */
	public void onSettingsApplied();

	/**
	 * Sets the result back to the viewer.
	 * 
	 * @param data The annotation hosted the file to load.
	 * @param file The local copy.
	 * @param uiView The object handle the result.
	 */
	public void setLoadedFile(FileAnnotationData data, File file, 
			Object uiView);
	
}
