/*
 * org.openmicroscopy.shoola.env.data.util.PojoMapper
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

package org.openmicroscopy.shoola.env.data.util;




//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.annotations.UrlAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import pojos.ArchivedAnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.URLAnnotationData;

/** 
 * Helper methods to convert {@link IObject}s into their corresponding
 * {@link DataObject}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class PojoMapper
{

	
    /**
     * Helper method to convert the specified object into its corresponding
     * {@link DataObject} or collection of {@link DataObject}s.
     * 
     * @param value The object to convert.
     * @return      See above.
     */
    private static Object convert(Object value)
    {
        if (value instanceof IObject) return asDataObject((IObject) value);
        else if (value instanceof Set) return asDataObjects((Set) value);
        else if (value instanceof Map) return asDataObjects((Map) value);
        else return null;
    }
    
    /**
     * Converts the speficied {@link IObject} into its corresponding 
     * {@link DataObject}.
     * 
     * @param object    The object to convert.
     * @return          See above.
     * @throws IllegalArgumentException If the object is null or 
     * if the type {@link IObject} is unknown.
     */
    public static DataObject asDataObject(IObject object)
    {
        if (object == null) 
            throw new IllegalArgumentException("IObject cannot be null.");
        if (object instanceof Project) 
            return new ProjectData((Project) object);
        else if (object instanceof Dataset) 
            return new DatasetData((Dataset) object);
        else if (object instanceof CategoryGroup) 
            return new CategoryGroupData((CategoryGroup) object);
        else if (object instanceof Category) 
            return new CategoryData((Category) object);
        else if (object instanceof Image) 
        	return new ImageData((Image) object);
        else if (object instanceof UrlAnnotation)
        	return new URLAnnotationData((UrlAnnotation) object);
        else if (object instanceof TagAnnotation)
        	return new TagAnnotationData((TagAnnotation) object);
        else if (object instanceof TextAnnotation) 
        	return new TextualAnnotationData((TextAnnotation) object);
        else if (object instanceof LongAnnotation) 
        	return new RatingAnnotationData((LongAnnotation) object);	
        else if (object instanceof FileAnnotation) 
        	return new FileAnnotationData((FileAnnotation) object);
        else if (object instanceof BooleanAnnotation) {
        	BooleanAnnotation ann = (BooleanAnnotation) object;
        	if (ArchivedAnnotationData.IMPORTER_ARCHIVED_NS.equals(ann.getNs()))
        		return new ArchivedAnnotationData(ann);
        } else if (object instanceof Pixels) 
            return new PixelsData((Pixels) object);
        else if (object instanceof Experimenter) {
        	ExperimenterData exp = new ExperimenterData((Experimenter) object); 
        	//System.err.println(exp.getGroups());
        	return exp;
        }
        else if (object instanceof ExperimenterGroup) 
            return new GroupData((ExperimenterGroup) object); 
        throw new IllegalArgumentException("Unknown IObject type: "+
                object.getClass().getName());
    }
    
    /**
     * Converts each {@link IObject element} of the collection into its 
     * corresponding {@link DataObject}.
     * 
     * @param objects   The set of objects to convert.
     * @return          A set of {@link DataObject}s.
     * @throws IllegalArgumentException If the set is <code>null</code>, doesn't
     * contain {@link IObject} or if the type {@link IObject} is unknown.
     */
    public static Set asDataObjects(Set objects)
    {
        if (objects == null) 
            throw new IllegalArgumentException("The set cannot be null.");
        HashSet<DataObject> set = new HashSet<DataObject>(objects.size());
        Iterator i = objects.iterator();
        while (i.hasNext())
            set.add(asDataObject((IObject) i.next()));
        return set;
    }
    
    /**
     * Converts each {@link IObject element} of the collection into its 
     * corresponding {@link DataObject}.
     * 
     * @param objects   The set of objects to convert.
     * @return          A set of {@link DataObject}s.
     * @throws IllegalArgumentException If the set is <code>null</code>, doesn't
     * contain {@link IObject} or if the type {@link IObject} is unknown.
     */
    public static Set asDataObjects(List objects)
    {
        if (objects == null) 
            throw new IllegalArgumentException("The set cannot be null.");
        HashSet<DataObject> set = new HashSet<DataObject>(objects.size());
        Iterator i = objects.iterator();
        while (i.hasNext())
            set.add(asDataObject((IObject) i.next()));
        return set;
    }
    
    /**
     * Converts each {@link IObject element} of the array into its 
     * corresponding {@link DataObject}.
     * 
     * @param objects   The set of objects to convert.
     * @return          A set of {@link DataObject}s.
     * @throws IllegalArgumentException If the set is <code>null</code>, doesn't
     * contain {@link IObject} or if the type {@link IObject} is unknown.
     */
    public static Set asDataObjects(IObject[] objects)
    {
    	if (objects == null) 
            throw new IllegalArgumentException("The array cannot be null.");
    	HashSet<DataObject> set = new HashSet<DataObject>(objects.length);
        for (int i = 0; i < objects.length; i++)
        	set.add(asDataObject(objects[i]));
        return set;
    }
    
    /**
     * Converts each pair (key, value) of the map. If the key (resp. value) is
     * an {@link IObject}, the element is converted into its corresponding
     * {@link DataObject}.
     * 
     * @param objects   The map of objects to convert.
     * @return          A map of converted objects.
     * @throws IllegalArgumentException If the map is <code>null</code> 
     * or if the type {@link IObject} is unknown.
     */
    public static Map asDataObjects(Map objects)
    {
        if (objects == null) 
            throw new IllegalArgumentException("The map cannot be null.");
        HashMap<Object, Object> 
        	map = new HashMap<Object, Object>(objects.size());
        Iterator i = objects.keySet().iterator();
        Object key, value;
        Object convertedKey = null;
        Object convertedValue = null;
        while (i.hasNext()) {
            key = i.next();
            value = objects.get(key);
            convertedKey = convert(key);
            convertedValue = convert(value);
            map.put(convertedKey == null ? key : convertedKey, 
                    convertedValue == null ? value : convertedValue);
        }
        return map;
    }
    
}
