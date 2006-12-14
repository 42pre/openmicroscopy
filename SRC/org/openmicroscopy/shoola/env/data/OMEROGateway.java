/*
 * org.openmicroscopy.shoola.env.data.OMEROGateway
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;


//Java imports
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBAccessException;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IThumb;
import ome.api.IUpdate;
import ome.api.ThumbnailStore;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.CategoryGroupCategoryLink;
import ome.model.containers.CategoryImageLink;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.util.builders.PojoOptions;
import omeis.providers.re.RenderingEngine;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Unified access point to the various <i>OMERO</i> services.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class OMEROGateway
{

    /**
     * The entry point provided by the connection library to access the various
     * <i>OMERO</i> services.
     */
    private ServiceFactory          entry;
    
    /** The thumbnail service. */
    private ThumbnailStore          thumbnailService;
    
    /**
     * Tells whether we're currently connected and logged into <i>OMEDS</i>.
     */
    private boolean                 connected;
    
    /** 
     * Used whenever a broken link is detected to get the Login Service and
     * try reestabishing a valid link to <i>OMEDS</i>. 
     */
    private DataServicesFactory     dsFactory;
    
    /** Server instance to log in. */
    private Server                  server;
    
    /** The port to use in order to connect. */
    private int                     port;
    
    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Methods in this class are required to fill in a meaningful context
     * message.
     * This method is not supposed to be used in this class' constructor or in
     * the login/logout methods.
     *  
     * @param e     	The exception.
     * @param message	The context message.    
     * @throws DSOutOfServiceException  A connection problem.
     * @throws DSAccessException    A server-side error.
     */
    private void handleException(Exception e, String message) 
        throws DSOutOfServiceException, DSAccessException
    {
    	if (e instanceof SecurityException) {
    		String s = "Cannot access data for security reasons \n"; 
    		throw new DSAccessException(s+message+"\n\n"+printErrorText(e));
    	} else if (e instanceof EJBAccessException) {
    		String s = "Cannot access data for security reasons \n"; 
    		throw new DSAccessException(s+message+"\n\n"+printErrorText(e));
    	} else if (e instanceof ApiUsageException) {
    		String s = "Cannot access data, specified parameters not valid \n"; 
    		throw new DSAccessException(s+message+"\n\n"+printErrorText(e));
    	} else if (e instanceof ValidationException) {
    		String s = "Cannot access data, specified parameters not valid \n"; 
    		throw new DSAccessException(s+message+"\n\n"+printErrorText(e));
    	} else 
    		throw new DSOutOfServiceException(message+"\n\n"+printErrorText(e));
    }
    
    /**
     * Utility method to print the error message
     * 
     * @param e The exception to handle.
     * @return  See above.
     */
    private String printErrorText(Exception e) 
    {
    	StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Maps the constant defined by {@link OmeroDataService}
     * to the corresponding value defined by {@link IPojos}.
     * 
     * @param algorithm One of the constant defined by {@link OmeroDataService}.
     * @return See above.
     */
    private String mapAlgorithmToString(int algorithm)
    {
        switch (algorithm) {
            case OmeroDataService.CLASSIFICATION_ME:
                return IPojos.CLASSIFICATION_ME;
            case OmeroDataService.CLASSIFICATION_NME:
                return IPojos.CLASSIFICATION_NME;
            case OmeroDataService.DECLASSIFICATION:
                return IPojos.DECLASSIFICATION;
        }
        throw new IllegalArgumentException("Algorithm not valid.");
    }
    
    /**
     * Converts the specified POJO into a String corresponding model.
     *  
     * @param nodeType The POJO class.
     * @return The corresponding string.
     */
    private String convertPojosToString(Class nodeType)
    {
        if (nodeType.equals(ProjectData.class))
            return Project.class.getName();
        else if (nodeType.equals(DatasetData.class))
            return Dataset.class.getName();
        else if (nodeType.equals(ImageData.class))
            return Image.class.getName();
        else if (nodeType.equals(CategoryData.class)) 
            return Category.class.getName();
        else if (nodeType.equals(CategoryGroupData.class))
            return CategoryGroup.class.getName();
        throw new IllegalArgumentException("NodeType not supported");
    }
    
    /**
     * Transforms the specified <code>property</code> into the 
     * corresponding server value.
     * The transformation depends on the specified class.
     * 
     * @param nodeType The type of node this property corresponds to.
     * @param property The name of the property.
     * @return See above.
     */
    private String convertProperty(Class nodeType, String property)
    {
        if (nodeType.equals(DatasetData.class)) {
            if (property.equals(OmeroDataService.IMAGES_PROPERTY))
                return DatasetData.IMAGE_LINKS;
        } else if (nodeType.equals(CategoryData.class)) {
            if (property.equals(OmeroDataService.IMAGES_PROPERTY))
                return CategoryData.IMAGES;
        }
        else throw new IllegalArgumentException("NodeType or " +
        										"property not supported");
        return null;
    }
    
    /**
     * Converts the specified POJO into the corresponding model.
     *  
     * @param nodeType The POJO class.
     * @return The corresponding class.
     */
    private Class convertPojos(Class nodeType)
    {
        if (nodeType.equals(ProjectData.class)) return Project.class;
        else if (nodeType.equals(DatasetData.class)) return Dataset.class;
        else if (nodeType.equals(ImageData.class)) return Image.class;
        else if (nodeType.equals(CategoryData.class)) return Category.class;
        else if (nodeType.equals(CategoryGroupData.class))
            return CategoryGroup.class;
        else throw new IllegalArgumentException("NodeType not supported");
    }
    
    /**
     * Retrieves the details on the current user and maps the result calling
     * {@link PojoMapper#asDataObjects(Map)}.
     * 
     * @param name  The user's name.
     * @return      The {@link ExperimenterData} of the current user.
     * @throws DSOutOfServiceException If the connection is broken, or
     * logged in.
     */
    private ExperimenterData getUserDetails(String name)
        throws DSOutOfServiceException
    {
        try {
            IPojos service = getIPojosService();
            Set set = new HashSet(1);
            set.add(name);
            Map m = PojoMapper.asDataObjects(service.getUserDetails(set, 
                    (new PojoOptions()).map()));
            ExperimenterData data = (ExperimenterData) m.get(name);
            if (data == null) {
                throw new DSOutOfServiceException("Cannot retrieve user's " +
                "data");
            }
            return data;
        } catch (Exception e) {
        	e.printStackTrace();
            throw new DSOutOfServiceException("Cannot retrieve user's " +
                    						"data", e);
        }
    }
    
    /**
     * Returns the {@link IPojos} service.
     * 
     * @return See above.
     */
    private IPojos getIPojosService() { return entry.getPojosService(); }
    
    /**
     * Returns the {@link IQuery} service.
     *  
     * @return See above.
     */
    private IQuery getIQueryService() { return entry.getQueryService(); }
    
    /**
     * Returns the {@link IUpdate} service.
     *  
     * @return See above.
     */
    private IUpdate getIUpdateService() { return entry.getUpdateService(); }
    
    /**
     * Returns the {@link IThumb} service.
     *  
     * @return See above.
     */
    private ThumbnailStore getThumbService()
    { 
        if (thumbnailService == null) 
            thumbnailService = entry.createThumbnailService();
        return thumbnailService; 
    }

    /**
     * Returns the {@link RenderingEngine Rendering service}.
     * 
     * @return See above.
     */
    private RenderingEngine getRenderingService()
    {
        return entry.createRenderingEngine();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param port      The value of the port.
     * @param dsFactory A reference to the factory. Used whenever a broken link
     *                  is detected to get the Login Service and try 
     *                  reestabishing a valid link to <i>OMEDS</i>.
     *                  Mustn't be <code>null</code>.
     */
    OMEROGateway(int port, DataServicesFactory dsFactory)
    {
        if (dsFactory == null) 
            throw new IllegalArgumentException("No Data service factory.");
        this.dsFactory = dsFactory;
        this.port = port;
    }
    
    /**
     * Tells whether the communication channel to <i>OMERO</i> is currently
     * connected.
     * This means that we have established a connection and have sucessfully
     * logged in.
     * 
     * @return  <code>true</code> if connected, <code>false</code> otherwise.
     */
    boolean isConnected() { return connected; }
    
    /**
     * Tries to connect to <i>OMERO</i> and log in by using the supplied
     * credentials.
     * 
     * @param userName  The user name to be used for login.
     * @param password  The password to be used for login.
     * @param hostName  The name of the server.
     * @return The user's details.
     * @throws DSOutOfServiceException If the connection can't be established
     *                                  or the credentials are invalid.
     */
    ExperimenterData login(String userName, String password, String hostName)
        throws DSOutOfServiceException
    {
        try {
            server = new Server(hostName, port);
            entry = new ServiceFactory(server, new Login(userName, password)); 
            connected = true;
            return getUserDetails(userName);
        } catch (Exception e) {
            connected = false;
            String s = "Can't connect to OMERO. OMERO info not valid.";
            throw new DSOutOfServiceException(s, e);  
        } 
    }
    
    /** Log out. */
    void logout()
    {
        //TODO
        connected = false;
        if (thumbnailService != null) thumbnailService.close();
    }
    
    /**
     * Retrieves hierarchy trees rooted by a given node.
     * i.e. the requested node as root and all of its descendants.
     * The annotation for the current user is also linked to the object.
     * Annotations are currently possible only for Image and Dataset.
     * Wraps the call to the 
     * {@link IPojos#loadContainerHierarchy(Class, Set, Map)}
     * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param rootNodeType  The top-most type which will be searched for 
     *                      Can be <code>Project</code> or 
     *                      <code>CategoryGroup</code>. 
     *                      Mustn't be <code>null</code>.
     * @param rootNodeIDs   A set of the IDs of top-most containers. 
     *                      Passed <code>null</code> to retrieve all container
     *                      of the type specified by the rootNodetype parameter.
     * @param options       The Options to retrieve the data.
     * @return  A set of hierarchy trees.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return PojoMapper.asDataObjects(service.loadContainerHierarchy(
                    convertPojos(rootNodeType), rootNodeIDs, options));
        } catch (Exception e) {
            handleException(e, "Cannot load hierarchy for "+rootNodeType+".");
        }
        return new HashSet();
    }
    
    /**
     * Retrieves hierarchy trees in various hierarchies that
     * contain the specified Images.
     * The annotation for the current user is also linked to the object.
     * Annotations are currently possible only for Image and Dataset.
     * Wraps the call to the 
     * {@link IPojos#findContainerHierarchies(Class, Set, Map)}
     * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param rootNodeType  top-most type which will be searched for 
     *                      Can be <code>Project</code> or
     *                      <code>CategoryGroup</code>. 
     *                      Mustn't be <code>null</code>.
     * @param leavesIDs     Set of ids of the Images that sit at the bottom of
     *                      the trees. Mustn't be <code>null</code>.
     * @param options Options to retrieve the data.
     * @return A <code>Set</code> with all root nodes that were found.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Set findContainerHierarchy(Class rootNodeType, Set leavesIDs, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return PojoMapper.asDataObjects(service.findContainerHierarchies(
                            convertPojos(rootNodeType), leavesIDs, options));
        } catch (Exception e) {
            handleException(e, "Cannot find hierarchy for "+rootNodeType+".");
        }
        return new HashSet();
    }
    
    /**
     * Finds all the annotations that have been attached to the specified
     * <code>rootNodes</code>. This method looks for all the <i>valid</i>
     * annotations that have been attached to each of the specified objects. It
     * then maps each <code>rootNodeID</code> onto the set of all annotations
     * that were found for that node. If no annotations were found for that
     * node, then the entry will be <code>null</code>. Otherwise it will be a
     * <code>Set</code> containing <code>Annotation</code> objects.
     * Wraps the call to the 
     * {@link IPojos#findAnnotations(Class, Set, Set, Map)}
     * and maps the result calling {@link PojoMapper#asDataObjects(Map)}.
     * 
     * @param nodeType      The type of the rootNodes. It can either be
     *                      <code>Dataset</code> or <code>Image</code>.
     *                      Mustn't be <code>null</code>. 
     * @param nodeIDs       TheIds of the objects of type
     *                      <code>rootNodeType</code>. 
     *                      Mustn't be <code>null</code>.
     * @param annotatorIDs  The Ids of the users for whom annotations should be 
     *                      retrieved. If <code>null</code>, all annotations 
     *                      are returned.
     * @param options       Options to retrieve the data.
     * @return A map whose key is rootNodeID and value the <code>Set</code> of
     *         all annotations for that node or <code>null</code>.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Map findAnnotations(Class nodeType, Set nodeIDs, Set annotatorIDs, 
                        Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return PojoMapper.asDataObjects(
                    service.findAnnotations(convertPojos(nodeType), nodeIDs, 
                            annotatorIDs, options));
        } catch (Exception e) {
            handleException(e, "Cannot find annotations for "+nodeType+".");
        }
        return new HashMap();
    }
    
    /**
     * Retrieves paths in the Category Group/Category/Image (CG/C/I) hierarchy.
     * <p>
     * Because of the mutually exclusive rule of CG/C hierarchy, this method
     * is quite tricky.
     * We want to retrieve all Category Group/Category paths that end with
     * the specified leaves.
     * </p>
     * <p> 
     * We also want to retrieve the all Category Group/Category paths that
     * don’t end with the specified leaves, note that in that case because of 
     * the mutually exclusive constraint the categories which don’t contain a
     * specified leaf but which is itself contained in a group which already
     * has a category ending with the specified leaf is excluded.
     * </p>
     * <p>  
     * This is <u>more</u> restrictive than may be imagined. The goal is to 
     * find CGC paths to which an Image <B>MAY</b> be attached.
     * </p>
     * Wraps the call to the {@link IPojos#findCGCPaths(Set, String, Map)}
     * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param imgIDs    Set of ids of the images that sit at the bottom of the
     *                  CGC trees. Mustn't be <code>null</code>.
     * @param algorithm The search algorithm for finding paths. One of the 
     *                  following constants: 
     * @param options   Options to retrieve the data.
     * @return A <code>Set</code> of hierarchy trees with all root nodes 
     * that were found.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Set findCGCPaths(Set imgIDs, int algorithm, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return PojoMapper.asDataObjects(service.findCGCPaths(imgIDs, 
                                    mapAlgorithmToString(algorithm),
                                    options));
        } catch (Exception e) {
            handleException(e, "Cannot find CGC paths.");
        }
        return new HashSet();
    }
    
    /**
     * Retrieves the images contained in containers specified by the 
     * node type.
     * Wraps the call to the {@link IPojos#getImages(Class, Set, Map)}
     * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param nodeType  The type of container. Can be either Project, Dataset,
     *                  CategoryGroup, Category.
     * @param nodeIDs   Set of containers' IDS.
     * @param options   Options to retrieve the data.
     * @return A <code>Set</code> of retrieved images.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Set getContainerImages(Class nodeType, Set nodeIDs, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return PojoMapper.asDataObjects(
                   service.getImages(convertPojos(nodeType), nodeIDs, options));
        } catch (Exception e) {
            handleException(e, "Cannot find images for "+nodeType+".");
        }
        return new HashSet();
    }
    
    /**
     * Retrieves the images imported by the current user.
     * Wraps the call to the {@link IPojos#getUserImages(Map)}
     * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param options   Options to retrieve the data.
     * @return A <code>Set</code> of retrieved images.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Set getUserImages(Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return PojoMapper.asDataObjects(service.getUserImages(options));
        } catch (Exception e) {
            handleException(e, "Cannot find user images.");
        }
        return new HashSet();
    }
    
    /**
     * Counts the number of items in a collection for a given object.
     * Returns a map which key is the passed rootNodeID and the value is 
     * the number of items contained in this object and
     * maps the result calling {@link PojoMapper#asDataObjects(Map)}.
     * 
     * @param rootNodeType 	The type of container. Can either be Dataset 
     * 						and Category.
     * @param property		One of the properties defined by this class.
     * @param options		Options to retrieve the data.		
     * @param rootNodeIDs	Set of root node IDs.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Map getCollectionCount(Class rootNodeType, String property, Set rootNodeIDs,
            				Map options)
    	throws DSOutOfServiceException, DSAccessException
	{
        try {
            IPojos service = getIPojosService();
            String p = convertProperty(rootNodeType, property);
            if (p == null) return null;
            return PojoMapper.asDataObjects(service.getCollectionCount(
                    convertPojosToString(rootNodeType), p, rootNodeIDs, 
                    options));
        } catch (Exception e) {
            handleException(e, "Cannot count the collection.");
        }
        return new HashMap();
	}
    
    /**
     * Creates the speficied object.
     * 
     * @param object    The object to create.
     * @param options   Options to create the data.  
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    IObject createObject(IObject object, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return service.createDataObject(object, options);
        } catch (Exception e) {
            handleException(e, "Cannot update the object.");
        }
        return null;
    }
    
    /**
     * Creates the speficied objects.
     * 
     * @param objects   The object to create.
     * @param options   Options to create the data.  
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    IObject[] createObjects(IObject[] objects, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            IObject[] results = service.createDataObjects(objects, options);
            return results;
        } catch (Exception e) {
            handleException(e, "Cannot update the object.");
        }
        return null;
    }
    
    /**
     * Deletes the specified object.
     * 
     * @param object    The object to delete.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    void deleteObject(IObject object)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IUpdate service = getIUpdateService();
            service.deleteObject(object);
        } catch (Exception e) {
            handleException(e, "Cannot delete the object.");
        }
    }

    /**
     * Deletes the specified objects.
     * 
     * @param objects                  The objects to delete.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException       If an error occured while trying to 
     *                                 retrieve data from OMEDS service. 
     */
    void deleteObjects(IObject[] objects)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            //IPojos service = getIPojosService();
            IUpdate service = getIUpdateService();
            for (int i = 0; i < objects.length; i++) {
                service.deleteObject(objects[i]);
            }
        } catch (Exception e) {
            handleException(e, "Cannot delete the object.");
        }
    }
    
    /**
     * Updates the specified object.
     * 
     * @param object    The objet to update.
     * @param options   Options to update the data.   
     * @return          The updated object.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    IObject updateObject(IObject object, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return service.updateDataObject(object, options);
        } catch (Exception e) {
            handleException(e, "Cannot update the object.");
        }
        return null;
    }
    
    /**
     * Updates the specified <code>IObject</code>s and returned the 
     * updated <code>IObject</code>s.
     * 
     * @param objects   The array of objects to update.
     * @param options   Options to update the data.   
     * @return  See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    IObject[] updateObjects(IObject[] objects, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IPojos service = getIPojosService();
            return service.updateDataObjects(objects, options);
        } catch (Exception e) {
            handleException(e, "Cannot update the object.");
        }
        return null;
    }
    
    /**
     * Creates a new rendering service for the specified pixels set.
     * 
     * @param pixelsID  The pixels set ID.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    RenderingEngine createRenderingEngine(long pixelsID)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            RenderingEngine service = getRenderingService();
            service.lookupPixels(pixelsID);
            service.lookupRenderingDef(pixelsID);
            service.load();
            return service;
        } catch (Exception e) {
            handleException(e, "Cannot start the Rendering Engine.");
        }
        return null;
    }

    /**
     * Retrieves the dimensions in microns of the specified pixels set.
     * 
     * @param pixelsID  The pixels set ID.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    PixelsDimensions getPixelsDimensions(long pixelsID)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IQuery service = getIQueryService();
            Pixels pixs = (Pixels) service.get(Pixels.class, pixelsID);
            return (PixelsDimensions) service.get(PixelsDimensions.class,
                    pixs.getPixelsDimensions().getId().longValue());
        } catch (Exception e) {
            handleException(e, "Cannot retrieve the dimension of "+
                                "the pixels set.");
        }
        return null;
    }
    
    /**
     * Retrieves the channel information related to the given pixels set.
     * 
     * @param pixelsID  The id of the pixels set.
     * @return A list of <code>Channel</code> Objects.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    List getChannelsData(long pixelsID)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            IQuery service = getIQueryService();
            Pixels pixs = (Pixels) service.findByQuery(
                    "select p from Pixels as p " +
                    "left outer join fetch p.pixelsType as pt " +
                    "left outer join fetch p.channels as c " +
                    "left outer join fetch p.pixelsDimensions " +
                    "left outer join fetch c.logicalChannel as lc " +
                    "left outer join fetch c.statsInfo where p.id = :id",
                    new Parameters().addId(new Long(pixelsID)));
            return pixs.getChannels();
        } catch (Exception e) {
            handleException(e, "Cannot retrieve the channelsData for "+
                                "the pixels set "+pixelsID);
        }
        return null;
     }
    
    /**
     * Retrieves the thumbnail for the passed set of pixels.
     * 
     * @param pixelsID  The id of the pixels set the thumbnail is for.
     * @param sizeX     The size of the thumbnail along the X-axis.
     * @param sizeY     The size of the thumbnail along the Y-axis.
     * @return See above.
     * @throws RenderingServiceException If an error occured while trying to 
     *              retrieve data from the service. 
     */
    synchronized byte[] getThumbnail(long pixelsID, int sizeX, int sizeY)
        throws RenderingServiceException
    {
        try {
            ThumbnailStore service = getThumbService();
            service.setPixelsId(pixelsID);
            return service.getThumbnailDirect(new Integer(sizeX), 
                                                new Integer(sizeY));
        } catch (Exception e) {
            throw new RenderingServiceException("Cannot get thumbnail", e);
        }
    }
    
    /**
     * Finds the link if any between the specified parent and child.
     * 
     * @param parent    The parent.
     * @param child     The child.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    IObject findLink(IObject parent, IObject child)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            String table = null;
            Class klass = parent.getClass();
            if (klass.equals(Category.class)) table = "CategoryImageLink";
            else if (klass.equals(Dataset.class)) table = "DatasetImageLink";
            else if (klass.equals(Project.class)) table = "ProjectDatasetLink";
            else if (klass.equals(CategoryGroup.class)) 
                table = "CategoryGroupCategoryLink";
            if (table == null) return null;
            String sql = "select link from "+table+" as link where " +
                    "link.parent.id = :parentID and link.child.id = :childID";
            IQuery service = getIQueryService();
            Parameters param = new Parameters();
            param.addLong("parentID", parent.getId());
            param.addLong("childID", child.getId());
           return service.findByQuery(sql, param);
        } catch (Exception e) {
            handleException(e, "Cannot retrieve the requested link for "+
            "parent ID: "+parent.getId()+" and child ID: "+child.getId());
        }
        return null;
    }
    
    /**
     * Finds the links if any between the specified parent and children.
     * 
     * @param parent    The parent.
     * @param children  Collection of children as children ids.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    List findLinks(IObject parent, List children)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            String table = null;
            Class klass = parent.getClass();
            if (klass.equals(Category.class)) table = "CategoryImageLink";
            else if (klass.equals(Dataset.class)) table = "DatasetImageLink";
            else if (klass.equals(Project.class)) table = "ProjectDatasetLink";
            else if (klass.equals(CategoryGroup.class)) 
                table = "CategoryGroupCategoryLink";
            if (table == null) return null;
            String sql = "select link from "+table+" as link where " +
                    "link.parent.id = :parentID and link.child.id in " +
                    "(:childIDs)";
            IQuery service = getIQueryService();
            Parameters param = new Parameters();
            param.addLong("parentID", parent.getId());
            param.addList("childIDs", children);
            return service.findAllByQuery(sql, param);
        } catch (Exception e) {
            handleException(e, "Cannot retrieve the requested link for "+
            "parent ID: "+parent.getId());
        }
        return null;
    }
    
}
