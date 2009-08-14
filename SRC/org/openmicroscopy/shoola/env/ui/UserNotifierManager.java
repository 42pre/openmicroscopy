/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierManager 
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies
import omero.model.OriginalFile;

import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.svc.SvcRegistry;
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorDescriptor;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.util.ui.MessengerDetails;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;

/** 
 * Acts a controller. Listens to property changes fired by the 
 * <code>MessengerDialog</code>s.
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
class UserNotifierManager
	implements PropertyChangeListener
{
    
	/** The default message if an error occurred while transferring data. */
	private static final String	MESSAGE_START = "Sorry, but due to an error " +
								"we were not able to automatically \n";
	
	/** The default message if an error occurred while transferring data. */
	private static final String	MESSAGE_END = "\n\n"+
								"You can still send us the error message by " +
								"clicking on the \n" +
								"error message tab, copying the error " +
								"message to the clipboard, \n" +
								"and sending it to ";
	
	/** Message if the dialog's type is {@link MessengerDialog#ERROR_TYPE}. */
	private static final String	ERROR_MSG = "send your debug information.";
	
	/** Message if the dialog's type is {@link MessengerDialog#COMMENT_TYPE}. */
	private static final String	COMMENT_MSG = "send your comment.";
	
	/** Reply when sending the comments. */
	private static final String	COMMENT_REPLY = "Thanks, your comments have " +
											"been successfully posted.";
	
	/** Reply when sending the error message. */
	private static final String	ERROR_REPLY = "Thanks, the error message " +
										"has been successfully posted.";
	
	/** The tool invoking the service. */
	private static final String INVOKER_ERROR = "insight_bugs";
	
	/** The tool invoking the service. */
	private static final String INVOKER_COMMENT = "insight_comments";

	/** Default title for the comment dialog. */
    private static final String	DEFAULT_COMMENT_TITLE = "Comment";

    /** Reference to the container. */
	private Container						container;
	
	/** Back pointer to the component. */
	private UserNotifier					component;
	
	/** The dialog keeping track of the downloaded files. */
	private DownloadsDialog					download;
	
	/** Map keeping track of the ongoing data loading. */
	private Map<String, UserNotifierLoader> loaders;
	
	/** The Dialog used to send comments. */
	private MessengerDialog					commentDialog;
	
	/**
	 * Sends a message.
	 * 
	 * @param source	The source of the message.
	 * @param details 	The values to send.
	 */
	private void handleSendMessage(MessengerDialog source, 
								MessengerDetails details)
	{
		Registry reg = container.getRegistry();
		String url;
		boolean bug = true;
		String error = details.getError();
		if (error == null || error.length() == 0) bug = false;
		if (bug) url = (String) reg.lookup(LookupNames.DEBUG_URL_BUG);
		else url = (String) reg.lookup(LookupNames.DEBUG_URL_COMMENT);
		
		String teamAddress = (String) reg.lookup(LookupNames.DEBUG_EMAIL);
		CommunicatorDescriptor desc = new CommunicatorDescriptor
						(HttpChannel.CONNECTION_PER_REQUEST, url, -1);
		try {
			Communicator c = SvcRegistry.getCommunicator(desc);
			
			StringBuilder builder = new StringBuilder();
			String reply = "";
			if (!bug) c.submitComment(INVOKER_COMMENT,
								details.getEmail(), details.getComment(), 
								details.getExtra(), builder);
			else c.submitError(INVOKER_ERROR, 
							details.getEmail(), details.getComment(), 
					details.getExtra(), error, builder);
			if (!bug) reply += COMMENT_REPLY;
			else reply += ERROR_REPLY;
			JOptionPane.showMessageDialog(source, reply);
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
            msg.println("Failed to send message.");
            msg.println("Reason: "+e.getMessage());
            Logger logger = container.getRegistry().getLogger();
            logger.error(this, msg);
			String s = MESSAGE_START;
			if (source.getDialogType() == MessengerDialog.ERROR_TYPE)
				s += ERROR_MSG;
			else s += COMMENT_MSG;
			s += MESSAGE_END;
			JOptionPane.showMessageDialog(source, s+teamAddress+".");
		}
		source.setVisible(false);
		source.dispose();
	}

	/**
	 * Returns the name to give to the file.
	 * 
	 * @param files		Collection of files in the currently selected directory.
	 * @param f			The original file.
	 * @param original	The name of the file. 
	 * @param dirPath	Path to the directory.
	 * @param index		The index of the file.
	 * @return See above.
	 */
	private String getFileName(File[] files, OriginalFile f, String original, 
								String dirPath, int index)
	{
		String path = dirPath+original;
		boolean exist = false;
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
	        	 if ((files[i].getAbsolutePath()).equals(path)) {
	                 exist = true;
	                 break;
	             }
			}
		}
        if (!exist) return original;
        if (f == null) return original;
        String name = f.getName().getValue();
    	int lastDot = name.lastIndexOf(".");
    	if (lastDot != -1) {
    		String extension = name.substring(lastDot, name.length());
    		String v = name.substring(0, lastDot)+" ("+index+")"+extension;
    		index++;
    		return getFileName(files, f, v, dirPath, index);
    	} 
    	return original;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param component Back pointer to the component.
	 * @param c 		Reference to the container.
	 */
	UserNotifierManager(UserNotifier component, Container c)
	{
		container = c;
		this.component = component;
		loaders = new HashMap<String, UserNotifierLoader>();
	}
	
	/**
	 * Returns the details of the user currently logged in if any.
	 * 
	 * @return See above.
	 */
	ExperimenterData getExperimenter()
	{
		Object exp = 
			container.getRegistry().lookup(LookupNames.CURRENT_USER_DETAILS);
		if (exp == null) return null;
		return (ExperimenterData) exp;
	}
	
	/**
	 * Sets the loading status.
	 * 
	 * @param percent 	The value to set.
	 * @param fileID	The id of the file corresponding to the status.
	 * @param fileName	The name of the file.
	 */
	void setLoadingStatus(int percent, long fileID, String fileName)
	{
		if (download == null) return;
		download.setLoadingStatus(percent, fileName, fileID);
		loaders.remove(fileName);
	}
	
	/**
	 * Starts to download the file corresponding to the passed object.
	 * 
	 * @param file 		The file to handle.
	 * @param directory The directory where to save locally the file.
	 */
	void saveFileToDisk(OriginalFile file, File directory)
	{
		Logger log = container.getRegistry().getLogger();
		log.debug(this, "original: "+file);
		if (file == null) return;
		if (download == null) {
			Registry reg = container.getRegistry();
			JFrame f = reg.getTaskBar().getFrame();
			download = new DownloadsDialog(f, IconManager.getInstance(reg));
			download.addPropertyChangeListener(this);
		}
		if (directory == null) {
			//Need to make sure that file with same name does not exist.
			JFileChooser chooser = new JFileChooser();
	        //Get the current directory
			directory = chooser.getCurrentDirectory();
		}
		
		
        File[] files = directory.listFiles();
        String dirPath = directory+File.separator;
        log.debug(this, "dirPath: "+dirPath);
        String name = getFileName(files, file, file.getName().getValue(), 
        		dirPath, 1);
        
        log.debug(this, "name: "+name);
        
        String path = dirPath+name;
        log.debug(this, "name and path: "+path);
		FileLoader loader = new FileLoader(component, 
									container.getRegistry(), 
										path, file.getId().getValue(), 
										file.getSize().getValue());
		loader.load();
		download.addDowloadEntry(dirPath, name, file.getId().getValue());
		loaders.put(path, loader);
		if (!download.isVisible())
			UIUtilities.centerAndShow(download);
		
	}
	
	/**
	 * Starts to download the file corresponding to the passed object.
	 * 
	 * @param data 		The data to handle.
	 * @param directory The directory where to save locally the files.
	 */
	void saveFileToDisk(Collection data, File directory)
	{
		if (data == null) return;
		if (download == null) {
			Registry reg = container.getRegistry();
			JFrame f = reg.getTaskBar().getFrame();
			download = new DownloadsDialog(f, IconManager.getInstance(reg));
			download.addPropertyChangeListener(this);
		}
		if (directory == null) {
			JFileChooser chooser = new JFileChooser();
			//File dir = UIUtilities.getDefaultFolder();
	        //if (dir != null) chooser.setCurrentDirectory(dir);
			
	        //Get the current directory
			directory = chooser.getCurrentDirectory();
		}
		
        File[] files = directory.listFiles();
        String dirPath = directory+File.separator;
        Iterator i = data.iterator();
        String name;
        String path;
        OriginalFile file;
        FileLoader loader;
        while (i.hasNext()) {
        	file = (OriginalFile) i.next();
        	name = getFileName(files, file, file.getName().getValue(), 
        			dirPath, 1);
        	path = dirPath+name;
        	loader = new FileLoader(component, container.getRegistry(), 
						path, file.getId().getValue(), 
						file.getSize().getValue());
        	loader.load();
        	download.addDowloadEntry(dirPath, name, file.getId().getValue());
        	loaders.put(path, loader);
		}
		
		if (!download.isVisible())
			UIUtilities.centerAndShow(download);
	}
	
	/**
	 * Returns the version number.
	 * 
	 * @return See above.
	 */
	String getVersionNumber()
	{
		if (container == null) return "";
		Object version = container.getRegistry().lookup(LookupNames.VERSION);
		return (String) version;
	}
	
	/**
	 * Creates or recycles the messenger dialog.
	 * 
	 * @param frame The owner of the dialog.
	 * @param email The e-mail address.
	 * @return See above.
	 */
	MessengerDialog getCommentDialog(JFrame frame, String email)
	{
		if (commentDialog != null) return commentDialog;
		commentDialog = new MessengerDialog(frame, DEFAULT_COMMENT_TITLE, 
				email);   
		commentDialog.setVersion(getVersionNumber());
		commentDialog.addPropertyChangeListener(this);
		commentDialog.setModal(false);
		commentDialog.setAlwaysOnTop(false);
		return commentDialog;
	}
	
	/**
	 * Reacts to property changes fired by dialogs.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce)
	{
		String name = pce.getPropertyName();
		if (MessengerDialog.SEND_PROPERTY.equals(name)) {
			MessengerDialog source = (MessengerDialog) pce.getSource();
			handleSendMessage(source, (MessengerDetails) pce.getNewValue());
		} else if (MessengerDialog.CLOSE_MESSENGER_PROPERTY.equals(name)) {
			commentDialog = null;
		} else if (OpeningFileDialog.SAVE_TO_DISK_PROPERTY.equals(name)) {
			/*
			Object value = pce.getNewValue();
			if (value instanceof FileAnnotationData) 
				saveFileToDisk((FileAnnotationData) value);
				*/
		} else if (DownloadsDialog.CANCEL_LOADING_PROPERTY.equals(name)) {
			String fileName = (String) pce.getNewValue();
			UserNotifierLoader loader = loaders.get(fileName);
			if (loader != null) loader.cancel();
			
		}
	}
	
}
