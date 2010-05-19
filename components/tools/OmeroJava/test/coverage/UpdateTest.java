/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package coverage;

import static omero.rtypes.*;
import omero.api.IUpdatePrx;

import org.testng.annotations.Test;

public class UpdateTest 
	extends IceTest
{

    /**
     * Test the <code>saveAndReturnObject</code> method.
     * @throws Exception If an error occurred.
     */
    @Test
    public void testSaveAndReturnObject()
    	throws Exception
    {
        IUpdatePrx prx = factoryIce.getUpdateService();
        assertNotNull(prx);

        omero.model.ImageI obj = new omero.model.ImageI();
        obj.setName(rstring("foo"));
        obj.setAcquisitionDate(rtime(0));

        omero.model.DatasetImageLinkI link = new omero.model.DatasetImageLinkI();
        omero.model.DatasetI cat = new omero.model.DatasetI();
        cat.setName(rstring("bar"));
        link.setParent(cat);
        link.setChild(obj);
        obj.addDatasetImageLink(link);

        obj = (omero.model.ImageI) prx.saveAndReturnObject(obj);
        link = (omero.model.DatasetImageLinkI) obj.copyDatasetLinks().get(0);
        cat = (omero.model.DatasetI) link.getParent();

        assertTrue("foo".equals(obj.getName().getValue()));
        if (cat == null) {
            fail("Cat is null");
        } else {
            assertTrue("bar".equals(cat.getName().getValue()));
        }
    }

    /**
     * Deletes an Image.
     * @throws Exception If an error occurred.
     */
    @Test
    public void testDeleteObject()
    	throws Exception
    {
        IUpdatePrx prx = factoryIce.getUpdateService();
        assertNotNull(prx);

        String uuid = uuid();
        omero.model.ImageI obj = new omero.model.ImageI();
        obj.setName(rstring(uuid));
        obj.setAcquisitionDate(rtime(0));
        obj = (omero.model.ImageI) prx.saveAndReturnObject(obj);

        prx.deleteObject(obj);
        assertTrue(factoryIce.getQueryService().findAllByString(
                "Image", "name", uuid, false, null).size() == 0);
    }
}
