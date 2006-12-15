/*
 * org.openmicroscopy.shoola.env.data.views.calls.DataObjectSaver
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

package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Command to save a <code>DataObject</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DataObjectSaver
    extends BatchCallTree
{

    /** Indicates to create a <code>DataObject</code>. */
    public static final int CREATE = 0;
    
    /** Indicates to update the <code>DataObject</code>. */
    public static final int UPDATE = 1;
    
    /** Indicates to remove the <code>DataObject</code>. */
    public static final int REMOVE = 2;
    
    /** The save call. */
    private BatchCall       saveCall;
    
    /** The result of the call. */
    private Object          result;
    
    /**
     * Creates a {@link BatchCall} to create the specified {@link DataObject}.
     * 
     * @param object    The <code>DataObject</code> to create.
     * @param parent    The parent of the <code>DataObject</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall create(final DataObject object, final DataObject parent)
    {
        return new BatchCall("Create Data object.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.createDataObject(object, parent);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified {@link DataObject}.
     * 
     * @param object The <code>DataObject</code> to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall update(final DataObject object)
    {
        return new BatchCall("Update Data object.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.updateDataObject(object);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified {@link DataObject}.
     * 
     * @param objects	The <code>DataObject</code>s to remove.
     * @param parent    The parent of the <code>DataObject</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall remove(final Set objects, final DataObject parent)
    {
        return new BatchCall("Remove Data objects.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.removeDataObjects(objects, parent);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified {@link DataObject}.
     * 
     * @param objects   The <code>DataObject</code>s to remove.
     * @return The {@link BatchCall}.
     */
    private BatchCall remove(final Map objects)
    {
        return new BatchCall("Remove Data objects.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                Iterator i = objects.keySet().iterator();
                DataObject p;
                Set nodes;
                Map results = new HashMap(objects.size());
                while (i.hasNext()) {
                    p = (DataObject) i.next();
                    nodes = os.removeDataObjects((Set) objects.get((p)), p);
                    results.put(p, nodes);
                }
                result = results;
            }
        };
    }
    
    /**
     * Adds the {@link #saveCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(saveCall); }

    /**
     * Returns the saved <code>DataObject</code>.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }

    /**
     * Creates a new instance.
     * 
     * @param userObject    The {@link DataObject} to create or update.
     *                      Mustn't be <code>null</code>.
     * @param parent     	The parent of the <code>DataObject</code>. 
     * 						The value is <code>null</code> if there 
     * 						is no parent.
     * @param index         One of the constants defined by this class.
     */
    public DataObjectSaver(DataObject userObject, DataObject parent, int index)
    {
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject.");
        if (index == CREATE || index == REMOVE) {
            if (userObject instanceof DatasetData) {
                if (!(parent instanceof ProjectData))
                throw new IllegalArgumentException("Parent not valid.");
            } else if (userObject instanceof CategoryData) {
                if (!(parent instanceof CategoryGroupData))
                    throw new IllegalArgumentException("Parent not valid.");
            }
        }
        switch (index) {
            case CREATE:
                saveCall = create(userObject, parent);
                break;
            case UPDATE:
                saveCall = update(userObject);
                break;
            case REMOVE:
                Set l = new HashSet(1);
                l.add(userObject);
                saveCall = remove(l, parent);   
                break;
            default:
                throw new IllegalArgumentException("Operation not supported.");
        }
    }
  
    /**
     * Creates a new instance.
     * 
     * @param userObjects   The {@link DataObject}s to remove.
     *                      Mustn't be <code>null</code>.
     * @param parent        The parent of the <code>DataObject</code>. 
     *                      The value is <code>null</code> if there 
     *                      is no parent.
     * @param index         One of the following constants: {@link #REMOVE}.
     */
    public  DataObjectSaver(Set userObjects, DataObject parent, int index)
    {
        if (userObjects == null)
            throw new IllegalArgumentException("No DataObject.");
        if (userObjects.size() == 0)
            throw new IllegalArgumentException("No DataObject.");
        switch (index) {
            case REMOVE:
                saveCall = remove(userObjects, parent);   
                break;
            default:
                throw new IllegalArgumentException("Operation not supported.");
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param objects       The {@link DataObject} to remove.
     *                      Mustn't be <code>null</code>.
     * @param index         One of the following constants: {@link #REMOVE}.
     */
    public DataObjectSaver(Map objects, int index)
    {
        if (objects == null)
            throw new IllegalArgumentException("No DataObject.");
        if (objects.size() == 0)
            throw new IllegalArgumentException("No DataObject.");
        switch (index) {
            case REMOVE:
                saveCall = remove(objects);   
                break;
            default:
                throw new IllegalArgumentException("Operation not supported.");
        }
    }
    
}
