/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipBoard.ImageAnnotationLoader
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

package org.openmicroscopy.shoola.agents.hiviewer;

//Java imports
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ImageData;

/** 
 * Loads, asynchronously, the annotation linked to the specified image.
 * This class calls the <code>loadAnnotations</code> method in the
 * <code>HierarchyBrowsingView</code>.
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
public class ImageAnnotationLoader
    extends CBDataLoader
{

    /** The id of the image. */
    private long        imageID;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param clipBoard The {@link ClipBoard} this data loader is for.
     *                   Mustn't be <code>null</code>.
     * @param imageID The id of the image.
     */
    public ImageAnnotationLoader(ClipBoard clipBoard, long imageID)
    {
        super(clipBoard);
        this.imageID = imageID;
    }
    
    /**
     * Retrieves all the annotations linked to the specified image.
     * @see DataLoader#load()
     */
    public void load()
    {
        handle = hiBrwView.loadAnnotations(ImageData.class, imageID, this);
    }

    /** 
     * Cancels the data loading. 
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see #handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        clipBoard.setAnnotations((Map) result);
    }

    /**
     * Overridden so that we don't notify the user that the annotation
     * retrieval has been cancelled.
     * @see CBDataLoader#handleCancellation() 
     */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }
    
}
