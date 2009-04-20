/*
 * org.openmicroscopy.shoola.env.data.DataServicesFactory
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

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.views.DataViewsFactory;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * A factory for the {@link OmeroDataService} and the {@link OmeroImageService}.
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
public class DataServicesFactory
{
	
	/** The name of the fs configuration file in the config directory. */
	private static final String		FS_CONFIG_FILE = "fs.config";

    /** The sole instance. */
	private static DataServicesFactory		singleton;
	
	/**
	 * Creates a new instance. This can't be called outside of container 
	 * b/c agents have no refs to the singleton container.
	 * So we can be sure this method is going to create services just once.
	 * 
	 * @param c Reference to the singleton container. Mustn't be 
	 * 			<code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 */
	public static DataServicesFactory getInstance(Container c)
		throws DSOutOfServiceException
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null)	singleton = new DataServicesFactory(c);
		return singleton;
	}
	
	/** 
	 * Reference to the container, to exit the application when the session
	 * has expired.
	 */
	private Container					container;

	/** A reference to the container's registry. */
	private static Registry         	registry;

	/** Unified access point to the various OMERO services. */
	private static OMEROGateway			omeroGateway;

	/** The omero service adapter. */
	private OmeroDataService			ds;

	/** The image service adapter. */
	private OmeroImageService			is;

	/** The metadata service adapter. */
	private OmeroMetadataService 		ms;
 
    /** Keeps the client's session alive. */
	private ScheduledThreadPoolExecutor	executor;
	
    /** The fs properties. */
    private Properties 					fsConfig;
    
    /**
	 * Reads in the specified file as a property object.
	 * 
	 * @param file	Absolute pathname to the file.
	 * @return	The content of the file as a property object or
	 * 			<code>null</code> if an error occured.
	 */
	private static Properties loadConfig(String file)
	{
		Properties config = new Properties();
		FileInputStream fis = null;
		try { 
			fis = new FileInputStream(file);
			config.load(fis);
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (fis != null) fis.close();
			} catch (Exception ex) {}
		}
		return config;
	}
    
	/**
	 * Attempts to create a new instance.
     * 
	 * @param c	Reference to the container.
	 * @throws DSOutOfServiceException If the connection can't be established
	 * 									or the credentials are invalid.	
	 */
	private DataServicesFactory(Container c)
		throws DSOutOfServiceException
	{
		registry = c.getRegistry();
		container = c;
		
        OMEROInfo omeroInfo = (OMEROInfo) registry.lookup(LookupNames.OMERODS);
        
		//Try and read the Ice config file.
		//Properties config = loadConfig(c.resolveConfigFile(ICE_CONFIG_FILE));
		
		//Check what to do if null.
        omeroGateway = new OMEROGateway(omeroInfo.getPort(), this);
        
        //omeroGateway = new OMEROGateway(omeroInfo.getPort(), this);
		//Create the adapters.
        ds = new OmeroDataServiceImpl(omeroGateway, registry);
        is = new OmeroImageServiceImpl(omeroGateway, registry);
        ms = new OmeroMetadataServiceImpl(omeroGateway, registry);
        
        
        
        //fs stuff
        fsConfig = loadConfig(c.resolveConfigFile(FS_CONFIG_FILE));
        //Initialize the Views Factory.
        DataViewsFactory.initialize(c);
        if (omeroGateway.isUpgradeRequired()) {
        	
        }
	}
	
	/**
	 * Determines the quality of the compression depending on the
	 * connection speed.
	 * 
	 * @param connectionSpeed The connection speed.
	 * @return See above.
	 */
	private float determineCompression(int connectionSpeed)
	{
		Float value;
		switch (connectionSpeed) {
			case UserCredentials.MEDIUM:
			case UserCredentials.HIGH:
				value = (Float) registry.lookup(
						LookupNames.COMPRESSIOM_MEDIUM_QUALITY);
				return value.floatValue();
			case UserCredentials.LOW:
			default:
				value = (Float) registry.lookup(
						LookupNames.COMPRESSIOM_LOW_QUALITY);
				return value.floatValue();
		}
	}
	
	/**
	 * Returns <code>true</code> if the connection is fast,
	 * <code>false</code> otherwise.
	 * 
	 * @param connectionSpeed The connection speed.
	 * @return See above.
	 */
	private boolean isFastConnection(int connectionSpeed)
	{
		switch (connectionSpeed) {
			case UserCredentials.HIGH:
				return true;
			case UserCredentials.MEDIUM:
			case UserCredentials.LOW:
			default:
				return false;
		}
	}
	
	/** 
	 * Brings up a dialog indicating that the session has expired and
	 * quits the application.
	 * 
	 * @param index One of the connection constants defined by the gateway.
	 */
	void sessionExpiredExit(int index)
	{
		UserNotifier un = registry.getUserNotifier();
		String message = "The server is no longer " +
			"running. \nPlease contact your system administrator.";
		if (index == OMEROGateway.LOST_CONNECTION) {
			message = "The connection has been lost. \nThe application will " +
					"exit.";
			//Need to reconnect.
		}
		un.notifyInfo("Connection Refused", message);
		exitApplication();
	}
	
	/**
	 * Checks if the session is still alive.
	 * 
	 * @param reg Reference to the container registry.
	 */
	public static void isSessionAlive(Registry reg)
	{
		if (!(reg.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		omeroGateway.isSessionAlive();
	}
	
    /**
     * Returns the {@link OmeroDataService}.
     * 
     * @return See above.
     */
    public OmeroDataService getOS() { return ds; }
    
    /**
     * Returns the {@link OmeroImageService}.
     * 
     * @return See above.
     */
    public OmeroImageService getIS() { return is; }
    
    /**
     * Returns the {@link OmeroMetadataService}.
     * 
     * @return See above.
     */
    public OmeroMetadataService getMS() { return ms; }
    
    /**
     * Returns the {@link LoginService}. 
     * 
     * @return See above.
     */
    public LoginService getLoginService()
    {
        return (LoginService) registry.lookup(LookupNames.LOGIN);
    }

	/**
	 * Attempts to connect to <i>OMERO</i> server.
	 * 
     * @param uc The user's credentials for logging onto <i>OMERO</i> server.
	 * @throws DSOutOfServiceException If the connection can't be established
     *                                 or the credentials are invalid.							
	 */
	public void connect(UserCredentials uc)
		throws DSOutOfServiceException
	{
		if (uc == null)
            throw new NullPointerException("No user credentials.");
		omeroGateway.setPort(uc.getPort());
        ExperimenterData exp = omeroGateway.login(uc.getUserName(), 
                									uc.getPassword(),
                                                    uc.getHostName(),
                                                    determineCompression(
                                                    	uc.getSpeedLevel()));
        
        KeepClientAlive kca = new KeepClientAlive(container, omeroGateway);
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(kca, 60, 60, TimeUnit.SECONDS);
        
        String ldap = omeroGateway.lookupLdapAuthExperimenter(exp.getId());
        //replace Server string in fs config
        /*
        Iterator k = fsConfig.keySet().iterator();
        String value, key;
        String regex = LookupNames.FS_HOSTNAME;
        while (k.hasNext()) {
        	key = (String) k.next();
			value = fsConfig.getProperty(key);
			value = value.replaceAll(regex, uc.getHostName());
			fsConfig.setProperty(key, value);
		}
        omeroGateway.startFS(fsConfig);
        */
        registry.bind(LookupNames.USER_AUTHENTICATION, ldap);
        boolean fastConnection = isFastConnection(uc.getSpeedLevel());
        registry.bind(LookupNames.CURRENT_USER_DETAILS, exp);
        registry.bind(LookupNames.CONNECTION_SPEED, fastConnection);
        Map<GroupData, Set> groups;
        List<ExperimenterData> exps = new ArrayList<ExperimenterData>();
        try {
        	 groups = omeroGateway.getAvailableGroups(exp);
        	 registry.bind(LookupNames.USER_GROUP_DETAILS, groups);
        	 
        	 List<Long> ids = new ArrayList<Long>();
        	 Iterator i = groups.keySet().iterator();
        	 Set set;
        	 Iterator j;
        	 ExperimenterData e;
        	 while (i.hasNext()) {
				set = groups.get(i.next());
				j = set.iterator();
				while (j.hasNext()) {
					e = (ExperimenterData) j.next();
					if (!ids.contains(e.getId())) {
						ids.add(e.getId());
						exps.add(e);
					}
				}
			}
        	registry.bind(LookupNames.USERS_DETAILS, exps);	 
		} catch (DSAccessException e) {
			throw new DSOutOfServiceException("Cannot retrieve groups", e);
		}
       
        //Bind user details to all agents' registry.
        List agents = (List) registry.lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		Registry reg;
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			reg = agentInfo.getRegistry();
			reg.bind(LookupNames.USER_AUTHENTICATION, ldap);
			reg.bind(LookupNames.CURRENT_USER_DETAILS, exp);
			reg.bind(LookupNames.USER_GROUP_DETAILS, groups);
			reg.bind(LookupNames.USERS_DETAILS, exps);
			reg.bind(LookupNames.CONNECTION_SPEED, fastConnection);
		}
	}
	
	/**
	 * Tells whether the communication channel to <i>OMEDS</i> is currently
	 * connected.
	 * This means that we have established a connection and have sucessfully
	 * logged in.
	 * 
	 * @return	<code>true</code> if connected, <code>false</code> otherwise.
	 */
	public boolean isConnected() { return omeroGateway.isConnected(); }
	
    /** Shuts down the connection. */
	public void shutdown()
    { 
        ((OmeroImageServiceImpl) is).shutDown();
        omeroGateway.logout(); 
        if (executor != null) executor.shutdown();
        executor = null;
    }
	
	/** Shuts the services down and exits the application. */
	public void exitApplication()
	{
		//initTimer();
		shutdown();
		container.exit();
	}
	
	 /** Helper inner class. */
	class ExitTask 
		extends TimerTask {
    	
		/** 
		 * Sets the {@link #connAttempt} flag.
		 * @see TimerTask#run()
		 */
    	public void run() {
    		System.exit(0);
    	}
    }
	
}
