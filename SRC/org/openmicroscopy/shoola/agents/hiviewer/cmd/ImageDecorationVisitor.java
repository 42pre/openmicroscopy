/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ImageDecorationVisitor
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;



//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import pojos.ImageData;

/** 
 * Visits the nodes to set the decoration depending on the status of 
 * the hierarchy object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ImageDecorationVisitor
    implements ImageDisplayVisitor
{

    /** Collection of {@link ImageNode} ids. */
    private List nodeIDs;
    
    /**
     * Creates a new instance.
     * 
     * @param nodeIDs   Collection of {@link ImageNode} ids. 
     *                  Mustn't be <code>null</code> or of length zero.
     */
    public ImageDecorationVisitor(List nodeIDs)
    {
        if (nodeIDs == null || nodeIDs.size() == 0)
            throw new IllegalArgumentException("No nodes");
        this.nodeIDs = nodeIDs;
    }
    
    /**
     * Sets the decoration of the node.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        long id = ((ImageData) node.getHierarchyObject()).getId();
        if (nodeIDs.contains(new Long(id))) {
            node.setNodeDecoration();
        }     
    }

    /**
     * No implementation because a {@link ImageSet} is not decorated.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node) {}

}
