/*
 * org.openmicroscopy.shoola.agents.hiviewer.util.TreeCellRenderer
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

package org.openmicroscopy.shoola.agents.hiviewer.util;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeViewNode;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Determines and sets the icon and text associated to a data object.
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
public class TreeCellRenderer
    extends DefaultTreeCellRenderer
{
    
    /** Flag to determine if the background color is modified. */
    private boolean             visibleColor;
    
    /** 
     * Value set to <code>true</code> to display a thumbnail of the image,
     * <code>false</code> otherwise.
     */
    private boolean				thumbnail;
    
    /** Reference to the {@link IconManager}. */
    private IconManager         icons;
    
    /**
     * Sets the icon and the text corresponding to the user's object.
     * If an icon is passed, the passed icon is set
     * 
     * @param usrObject The user's object.
     * @param icon  If <code>null</code>, a default icon is set according to the
     *              data type of the user's object.
     */
    private void setValues(Object usrObject, Icon icon)
    {
        if (usrObject instanceof ProjectData)  {
            if (icon == null) icon = icons.getIcon(IconManager.PROJECT);
            setIcon(icon);
        } else if (usrObject instanceof DatasetData) {
            DatasetData data = (DatasetData) usrObject;
            if (icon == null) {
                Long i = data.getAnnotationCount();
                if (i == null || i.longValue() == 0)
                    icon = icons.getIcon(IconManager.DATASET);
                else icon = icons.getIcon(IconManager.ANNOTATED_DATASET);
            }
            setIcon(icon);
        } else if (usrObject instanceof ImageData) {
            if (icon == null) {
                if (thumbnail) icon = icons.getIcon(IconManager.IMAGE_MEDIUM);
                else {
                    ImageData img = (ImageData) usrObject;
                    Long a = img.getAnnotationCount();
                    Long c = img.getClassificationCount();
                    long n = 0, m = 0;
                    if (a != null) n = a.longValue();
                    if (c != null) m = c.longValue();
                    if (n == 0 && m == 0) 
                        icon = icons.getIcon(IconManager.IMAGE);
                    else if (n != 0 && m == 0)
                        icon = icons.getIcon(IconManager.ANNOTATED_IMAGE);
                    else if (n == 0 && m != 0)
                        icon = icons.getIcon(IconManager.CLASSIFIED_IMAGE);
                    else if (n != 0 && m != 0)
                        icon = icons.getIcon(
                                IconManager.ANNOTATED_CLASSIFIED_IMAGE);
                }
            }
            setIcon(icon);
        } else if (usrObject instanceof CategoryGroupData) {
            if (icon == null) icon = icons.getIcon(IconManager.CATEGORY_GROUP);
            setIcon(icon);
        } else if (usrObject instanceof CategoryData) {
            if (icon == null) icon = icons.getIcon(IconManager.CATEGORY);
            setIcon(icon);
        } else if (usrObject instanceof String) setIcon(null);
    }

    /** Creates a new instance. */
    public TreeCellRenderer()
    {
        this(false, false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param visibleColor  Pass <code>true</code> to modify the backgroundColor
     *                      according to the highlight color of the node.,
     *                      <code>false</code> otherwise.
     * @param thumbnail		Pass <code>true</code> to display a thumbnail of the 
     * 						image, <code>false</code> otherwise.                     
     */
    public TreeCellRenderer(boolean visibleColor, boolean thumbnail)
    {
        this.visibleColor = visibleColor;
        this.thumbnail = thumbnail;
        icons = IconManager.getInstance();
    }
  
    /**
     * Overridden to set the icon and the text.
     * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
     * 								boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        
        DefaultMutableTreeNode  node = (DefaultMutableTreeNode) value;
        if (node.getLevel() == 0) {
            setIcon(icons.getIcon(IconManager.ROOT));
            return this;
        }
        
        Object usrObject = node.getUserObject();
        Color c = null;
        int w = 0;
        FontMetrics fm = getFontMetrics(getFont());
        if (usrObject instanceof ImageSet) {
            ImageSet set = (ImageSet) usrObject;
            if (node instanceof TreeViewNode) {
            	w += fm.stringWidth(((TreeViewNode) node).getNodeName());
            	setText(((TreeViewNode) node).toString());
            } else {
            	setText(set.toString());
            	w += fm.stringWidth(getText());
            }
            setValues(set.getHierarchyObject(), null);
            if (visibleColor) {
                c = ((ImageSet) usrObject).getHighlight();
                if (c == null) c = getForeground();
                setForeground(c);
            }
        } else if (usrObject instanceof ImageNode) {
            ImageNode imgNode = (ImageNode) usrObject;
            w += fm.stringWidth(imgNode.toString());
            if (node instanceof TreeViewNode) {
            	w += fm.stringWidth(((TreeViewNode) node).getNodeName());
            	setText(((TreeViewNode) node).toString());
            } else {
            	setText(imgNode.toString());
            	w += fm.stringWidth(getText());
            }
            if (thumbnail) setValues(imgNode.getHierarchyObject(),
                    					imgNode.getThumbnail().getIcon());
            else setValues(imgNode.getHierarchyObject(), null);
            if (visibleColor) {
                c = imgNode.getHighlight();
                if (c == null) c = getForeground();
                setForeground(c);
            }
        } else {
            setText(node.toString());
            setValues(usrObject, null);
            w += fm.stringWidth(getText());
        }
        if (getIcon() != null) w += getIcon().getIconWidth();
        w += getIconTextGap();
        setPreferredSize(new Dimension(w, fm.getHeight()));
        return this;
    }
    
}
