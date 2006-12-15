/*
 * org.openmicroscopy.shoola.env.data.login.UserCredentials
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

package org.openmicroscopy.shoola.env.data.login;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds the user's credentials for logging onto <i>OMERO</i>.
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
public class UserCredentials
{
    
    /**
     * The <i>OMERO</i> login name of the user.
     * This is the <code>OME Name</code> that was assinged to the user when
     * it was created in the DB.
     */
    private String  userName;
    
    /**
     * The <i>OMERO</i> login password of the user.
     * This is the password that was chosen for the user when it was created
     * in the DB.
     */
    private String  password;
    
    /** The name of the <i>OMERO</i> server. */
    private String  hostName;
    
    /**
     * Creates a new instance.
     * 
     * @param userName The <i>OMERO</i> login name of the user.
     *                 This is the <code>OME Name</code> that was assinged to 
     *                 the user when it was created in the DB.
     * @param password The <i>OMERO</i> login password of the user.
     *                 This is the password that was chosen for the user when
     *                 it was created in the DB.
     * @param hostName
     * @throws IllegalArgumentException If the user name and/or the password is
     *                 <code>null</code> or has <code>0</code>-length.
     */
    public UserCredentials(String userName, String password, String hostName)
    {
        if (userName == null || userName.length() == 0)
            throw new IllegalArgumentException("Please specify a user name.");
        if (password == null || password.length() == 0)
            throw new IllegalArgumentException("Please specify a password.");
        this.userName = userName;
        this.password = password;
        this.hostName = hostName;
    }
    
    /**
     * Returns the name of the <i>OMERO</i> server.
     * 
     * @return See above.
     */
    public String getHostName() { return hostName; }
    
    /**
     * Returns the <i>OMERO</i> login name of the user.
     * This is the <code>OME Name</code> that was assinged to the user when
     * it was created in the DB.
     * This field is always a non-<code>null</code> string with a positive
     * length.
     * 
     * @return See above.
     */
    public String getUserName() { return userName; }

    /**
     * Returns the <i>OMERO</i> login password of the user.
     * This is the password that was chosen for the user when it was created
     * in the DB.
     * This field is always a non-<code>null</code> string with a positive
     * length.
     * 
     * @return See above.
     */
    public String getPassword() { return password; }
    
    /**
     * Formats user name and password.
     * Each character of the password is replaced by a star.
     * 
     * @return The formatted string.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("User Name: ");
        buf.append(userName);
        buf.append(" -- Password: ");
        for (int i = 0; i < password.length(); ++i) buf.append('*');
        return buf.toString();
    }
    
}
