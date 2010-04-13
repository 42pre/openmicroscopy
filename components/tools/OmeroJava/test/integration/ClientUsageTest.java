/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.*;
import java.io.File;

import junit.framework.TestCase;
import omero.RString;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

public class ClientUsageTest extends TestCase {

    @Test
    public void testClientClosedAutomatically() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(f1);
        client.createSession();
        client.getSession().closeOnDestroy();
    }

    @Test
    public void testClientClosedManually() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(f1);
        client.createSession();
        client.getSession().closeOnDestroy();
        client.closeSession();
    }

    @Test
    public void testUseSharedMemory() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(f1);
        client.createSession();

        assertEquals(0, client.getInputKeys().size());
        client.setInput("a", rstring("b"));
        assertEquals(1, client.getInputKeys().size());
        assertTrue(client.getInputKeys().contains("a"));
        assertEquals("b", ((RString) client.getInput("a")).getValue());

        client.closeSession();
    }

    @Test
    public void testCreateInsecureClientTicket2099() throws Exception {
        omero.client secure = new omero.client();
        assertTrue(secure.isSecure());
        try {
            secure.createSession().getAdminService().getEventContext();
            omero.client insecure = secure.createClient(false);
            try {
                insecure.getSession().getAdminService().getEventContext();
                assertFalse(insecure.isSecure());
            } finally {
                insecure.closeSession();
            }
        } finally {
            secure.closeSession();
        }
    }

}
