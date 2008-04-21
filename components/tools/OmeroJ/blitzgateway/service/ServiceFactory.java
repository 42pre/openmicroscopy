/*
 * blitzgateway.service.ServiceFactory 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package blitzgateway.service;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.util.OMEROClass;
import omero.model.Dataset;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsTypeI;
import omero.model.Project;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ServiceFactory
{	
	/** The blitz gateway object. */
	private BlitzGateway 	gateway;
	
	/** The dataservice object. */
	private DataService 	dataService;
	
	/** The Image service object. */
	private ImageService	imageService;

	/**
	 * Create the service factory which creates the gateway and services
	 * and links the different services together.  
	 * 
	 * @param iceConfig path to the ice config file.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public ServiceFactory(String iceConfig) 
		throws DSOutOfServiceException, DSAccessException
	{
		gateway = new BlitzGateway(iceConfig);
		dataService = new DataServiceImpl(gateway);
		imageService = new ImageServiceImpl(gateway);
	}
	
	/**
	 * Is the session closed?
	 * @return true if closed.
	 */
	public boolean isClosed()
	{
		return gateway.isClosed();
	}
	
	/**
	 * Close the session with the server.
	 */
	public void close()
	{
		gateway.close();
		dataService = null;
		imageService = null;
	}
	
	/**
	 * Open a session to the server with username and password.
	 * @param username see above.
	 * @param password see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void createSession(String username, String password) 
				throws DSOutOfServiceException, DSAccessException
	{
		gateway.createSession(username, password);
	}
	
	/**
	 * Get the projects in the OMERO.Blitz server in the user account.
	 * @param withLeaves get the datasets too.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Project> getProjects(List<Long> ids, boolean withLeaves) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getProjects(ids, withLeaves);
	}

	/**
	 * Get the projects in the OMERO.Blitz server in the user account.
	 * @param withLeaves get the datasets too.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Dataset> getDatasets(List<Long> ids, boolean withLeaves) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getDatasets(ids, withLeaves);
	}

	/**
	 * Get the pixels associated with the image.
	 * @param imageId
	 * @return the list of pixels.
	 */
	public List<Pixels> getPixelsFromImage(long imageId) 
		throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelsFromImage(imageId);
	}

	
	/**
	 * Get the image with id
	 * @param id see above
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Image getImage(Long id) 
			throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getImage(id);
	}
	
	/**
	 * Get the projects in the OMERO.Blitz server in the user account.
	 * @param withLeaves get the datasets too.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImages(OMEROClass parentType, List<Long> ids ) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImages(parentType, ids);
	}

	/**
	 * Get the rawplane for the image id imageId.
	 * @param imageID id of the image to retrieve.
	 * @param c the channel of the image to retrieve.
	 * @param t the time point to retrieve.
	 * @param z the z section to retrieve.
	 * @return The rawplane in 2-d array of doubles. 
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public double[][] getPlane(long imageID, int c, int t, int z) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPlane(imageID, c, t, z);
	}
	
	
	/**
	 * Get the pixels information for an image.
	 * @param imageID image id relating to the pixels.
	 * @return see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels getPixels(long imageID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPixels(imageID);
	}
	
	/**
	 * Test method to make sure the client converts it's data to the server data
	 * correctly. 
	 * @param pixelsId pixels id to upload to .  
	 * @param c channel.
	 * @param t time point.
	 * @param z z section.
	 * @return the converted data. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException 
	 */
	public double[][] testVal(long pixelsId, int z, int c, int t) 
	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.testVal(pixelsId, z, c, t);
	}

	/**
	 * Get the name of the user.
	 * @return see above.
	 *
	 */
	public String getUserName()
	{
		return dataService.getUserName();
	}
	
	/**
	 * Copy the pixels set from pixels to a new set.
	 * @param pixelsID pixels id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t num timepoints
	 * @param z num zsections.
	 * @param channelList the list of channels to copy.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Long copyPixels(long pixelsID, int x, int y,
		int t, int z, List<Integer> channelList, String methodology) throws 
		DSOutOfServiceException, DSAccessException
	{
		return imageService.copyPixels(pixelsID, x, y, t, z, channelList, methodology);
	}

	/**
	 * Upload the plane to the server, on pixels id with channel and the 
	 * time, + z section. the data is the client 2d data values. This will
	 * be converted to the raw server bytes.
	 * @param pixelsId pixels id to upload to .  
	 * @param c channel.
	 * @param t time point.
	 * @param z z section. 
	 * @param data plane data. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void uploadPlane(long pixelsId, int c, int t, int z, 
			double [][] data) throws DSOutOfServiceException, DSAccessException
	{
		imageService.uploadPlane(pixelsId, c, t, z, data);
	}

	public Pixels updatePixels(Pixels object) 
	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.updatePixels(object);
	}

	public List<PixelsTypeI> getPixelTypes() 
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelTypes();
	}

	public PixelsTypeI getPixelsType(String type) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<PixelsTypeI> pixelsTypes = getPixelTypes();
		for(int i = 0 ; i < pixelsTypes.size(); i++)
			if(pixelsTypes.get(i).value.val.equals(type))
				return pixelsTypes.get(i);
		return null;
	}
}


