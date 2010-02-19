/*
 * org.openmicroscopy.shoola.env.data.AdminService 
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
package org.openmicroscopy.shoola.env.data;


//Java imports
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Provides methods to handle groups and users.
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
public interface AdminService
{

	/** Identifies the used space on the file system. */
	public static final int USED = 100;

	/** Identifies the free space on the file system. */
	public static final int FREE = 101;
	
	/**
	 * Changes the password of the user currently logged in.
	 * 
	 * @param oldPassword	The password used to log in.
	 * @param newPassword	The new password.
	 * @return 	<code>Boolean.TRUE</code> if successfully modified,
	 * 			<code>Boolean.FALSE</code> otherwise.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Boolean changePassword(String oldPassword, String newPassword)
		throws DSOutOfServiceException, DSAccessException;
	

	/**
	 * Updates the specified experimenter.
	 * 
	 * @param exp	The experimenter to update.
	 * @param group The group the user is member of.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public ExperimenterData updateExperimenter(ExperimenterData exp, GroupData
			group)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Updates the specified group.
	 * 
	 * @param group The group to update.
	 * @param permissions The desired permissions level or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public GroupData updateGroup(GroupData group, int permissions)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Changes the current group of the specified user.
	 * 
	 * @param exp The experimenter to handle.
	 * @param group The group the user is member of.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public void changeExperimenterGroup(ExperimenterData exp, GroupData 
			group)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Creates and returns the experimenters.
	 * 
	 * @param object The object hosting information about the experimenters
	 * to create. 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List<ExperimenterData> createExperimenters(AdminObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Creates and returns the new group
	 * 
	 * @param object The object hosting information about the group to create. 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public GroupData createGroup(AdminObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the address of the server the user is currently connected to.
	 * 
	 * @return See above.
	 */
	public String getServerName();
	
	/**
	 * Returns the version of the server if available.
	 * 
	 * @return See above.
	 */
	public String getServerVersion();
	
	/**
	 * Returns the name used to log in.
	 * 
	 * @return See above.
	 */
	public String getLoggingName();

	/**
	 * Returns the free or available space (in Kilobytes) if the passed
	 * parameter is <code>FREE</code>, returns the used space (in Kilobytes) 
	 * if the passed parameter is <code>USED</code> on the file system
	 * including nested sub-directories. Returns <code>-1</code> 
	 * otherwise.
	 * 
	 * @param index One of the following constants: {@link #USED} or 
	 * 				{@link #FREE}.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public long getSpace(int index, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the group specified by the passed identifier or all available
	 * groups if <code>-1</code>.
	 * 
	 * @param groupID The group identifier.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<GroupData> loadGroups(long groupID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the experimenters contained in the specified group.
	 * 
	 * @param groupID The group identifier.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ExperimenterData> loadExperimenters(long groupID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Deletes the specified experimenters. Returns the experimenters 
	 * that could not be deleted.
	 * 
	 * @param experimenters The experimenters to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ExperimenterData> deleteExperimenters(
			List<ExperimenterData> experimenters)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Deletes the specified groups. Returns the groups 
	 * that could not be deleted.
	 * 
	 * @param groups The groups to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<GroupData> deleteGroups(List<GroupData> groups)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the permissions associated to the current group.
	 * 
	 * @return See above.
	 */
	public int getPermissionLevel();
	
	/**
	 * Copies the passed experimenters to the specified group.
	 * 
	 * @param group The group to add the experimenters to.
	 * @param experimenters The experimenters to add.
	 */
	public List<ExperimenterData> copyExperimenters(GroupData group, 
			Set experimenters)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Cuts and paste the specified experimenters.
	 * 
	 * @param toPaste   The nodes to paste.
	 * @param toCut     The nodes to cut.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List<ExperimenterData> cutAndPasteExperimenters(Map toPaste, 
			Map toRemove)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Counts the number of experimenters within the specified groups.
	 * Returns a map whose keys are the group identifiers and the values the 
	 * number of experimenters in the group.
	 * 
	 * @param ids The group identifiers.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public Map<Long, Long> countExperimenters(List<Long> ids)
		throws DSOutOfServiceException, DSAccessException;
	
}
