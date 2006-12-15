/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.ImageFinder
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * Initializes two sets: one containing the imageNodes displayed
 * and a second containing the corresponding <code>DataObject</code>s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImageFinder
    implements ImageDisplayVisitor
{

    /** Set of <code>ImageNode</code>s */
    private Set imageNodes;
    
    /** Set of corresponding <code>DataObject</code>s */
    private Set images;
    
    /** Creates a new instance. */
    public ImageFinder()
    {
        images = new HashSet();
        imageNodes = new HashSet();
    }
    
    /** 
     * Returns the set of {@link ImageNode}s displayed. 
     * 
     * @return See above.
     */
    public Set getImageNodes() { return imageNodes; }
    
    /** 
     * Returns the set of corresponding <code>DataObject</code>s. 
     * 
     * @return See above.
     */
    public Set getImages() { return images; }
    
    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        imageNodes.add(node);
        images.add(node.getHierarchyObject());
    }

    /** 
     * Required by the {@link ImageDisplayVisitor} I/F but no-op in our case. 
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node) {}

}
