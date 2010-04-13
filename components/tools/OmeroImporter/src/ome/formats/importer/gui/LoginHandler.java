/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.formats.importer.gui;

//TODO: some old code in this class that needs cleaning up

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmicroscopy.shoola.util.ui.login.LoginCredentials;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogin;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogo;

/**
 * @author Brian W. Loranger
 *
 */
public class LoginHandler implements IObservable, ActionListener, WindowListener, PropertyChangeListener, WindowStateListener, WindowFocusListener
{
	public final static String LOGIN = "LOGIN";

    public final static String  LOGIN_CANCELLED = "LOGIN_CANCELLED";

    /** Logger for this class */
	private static Log log = LogFactory.getLog(LoginHandler.class);
    
    private static boolean NEW_LOGIN = true;
    
    private final ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    public volatile JFrame      f;
    
    private boolean            center;

    private GuiImporter        viewer;

    private OMEROMetadataStoreClient store;
    
    public LoginDialog         dialog;
    
    public LoginFrame          frame;
    
    public ScreenLogin          view;
    public ScreenLogo           viewTop;

    private LoginCredentials    lc;
    
    private boolean modal, displayTop;
    
    private final HistoryTable historyTable;
    private final ImportConfig config;

    
    /**
     * Initialize the login handler
     * 
     * @param viewer - parent viewer
     * @param table - history table (for post login initialization) 
     */
    public LoginHandler(GuiImporter viewer, HistoryTable table) // TODO fix this history table shouldn't be here
    {
        this(viewer, table, false, false, true);
    }    
    
    /**
     * Initialize the login handler 
     * 
     * @param viewer - parent viewer
     * @param table - history table
     * @param modal - modal yes/no
     * @param center - center yes/no
     * @param displayTop - display the top 
     */
    public LoginHandler(GuiImporter viewer, HistoryTable table, boolean modal, boolean center, boolean displayTop)
    {
        this.viewer = viewer;
        this.center = center;
        this.modal = modal;
        this.config = viewer.config;
        
        historyTable = table;
        if (historyTable != null)
            addObserver(historyTable);
        
        viewer.enableMenus(false);
        
        displayLogin(displayTop);
    }
    
    /**
     * Display the login dialog
     * 
     * @param displayTop - display the top part of the login yes/no
     */
    public void displayLogin(boolean displayTop)
    {
        boolean cancelled;
        this.displayTop = displayTop;
        
        cancelled = displayLoginDialog(this, modal, displayTop);
        
        if (modal == true && cancelled == true)
        {
            loginCancelled();
        }
        
        if (modal == true && cancelled == false)
        {
            tryLogin();
        }
    }
    
    /**
     * Attempt a login
     */
    public void tryLogin()
    {
        new Thread()
        {
            public void run()
            {
                if (!NEW_LOGIN)
                {
                    //SplashWindow.disposeSplash();
                    viewer.setVisible(true);
                    // Values should already be set on config.
                }
                else
                {
                	config.username.set(lc.getUserName());
                	config.password.set(lc.getPassword());
                	config.hostname.set(lc.getHostName());
                	config.port.set(lc.getPort());
                }
            
                viewer.statusBar.setStatusIcon("gfx/server_trying16.png",
                "Trying to connect to " + config.hostname.get());
                viewer.statusBar.setProgress(true, -1, "connecting....");
                
                try
                {
                    if (isValidLogin())
                    {
                        if (NEW_LOGIN)
                        {
                            view.close();
                            viewTop.close();
                            viewer.setVisible(true);
                        }

                        viewer.statusBar.setProgress(false, 0, "");
                        viewer.appendToOutput("> Login Successful.\n");
                        log.info("Login successful!");
                        viewer.enableMenus(true);
                        viewer.setImportEnabled(true);
                        viewer.loggedIn = true;
                        notifyObservers(new ImportEvent.LOGGED_IN());
                                                
                        // if this fails, using the old server without repositorySpace
                        try {
                            long freeSpace = store.getRepositorySpace();
                            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                            String freeMB = formatter.format(freeSpace/1000);                
                            viewer.statusBar.setStatusIcon("gfx/server_connect16.png",
                                    "Connected to " + config.hostname.get() + ". Free space: " + 
                                    freeMB + " MB.");
                            return;
                        } catch (Exception e) 
                        {
                        	log.error("Exception retrieving repository free space.", e);
                            viewer.statusBar.setStatusIcon("gfx/server_connect16.png", "Connected to " + config.hostname.get() + ".");
                            return;
                        }
                        
                    } else {   
                        if (NEW_LOGIN)
                            view.setAlwaysOnTop(false);
                        log.info("Login failed!");
                        viewer.statusBar.setProgress(false, 0, "");
                        viewer.statusBar.setStatusIcon("gfx/error_msg16.png",
                                "Incorrect username/password. Server login failed, please try to "
                                + "log in again.");

                        JOptionPane.showMessageDialog(viewer,
                                "Incorrect username/password. Server login \nfailed, please "
                                + "try to log in again.");
                        viewer.appendToOutput("> Login failed. Try to relog.\n");
                        viewer.enableMenus(true);
                        viewer.loggedIn = false;
                        
                        if (NEW_LOGIN)
                            refreshNewLogin();
                        return;
                    }  
                }
                catch (Exception e)
                {                   
                    log.error("Exception in LoginHandler.", e);
                    
                    viewer.statusBar.setProgress(false, 0, "");
                    viewer.statusBar.setStatusIcon("gfx/error_msg16.png",
                    "Server connection to " + config.hostname.get() +" failed. " +
                            "Please try again.");
                    
                    if (NEW_LOGIN)
                        view.setAlwaysOnTop(false);
                    
                    JOptionPane
                    .showMessageDialog(
                            viewer,
                            "\nThe application failed to log in. The hostname may be wrong or " +
                            "\nthe server may be offline." +
                            "\n\nPlease try again.");
                    viewer.appendToOutput("> Login failed. Try to relog.\n");
                    viewer.enableMenus(true);
                    viewer.loggedIn = false;
                    
                    if (NEW_LOGIN)
                        refreshNewLogin();
                    return;
                }
            }
        }.start();
    }

    /**
     *  refresh login
     */
    void refreshNewLogin()
    {
        view.setAlwaysOnTop(true);
        //viewTop.setAlwaysOnTop(true); 
        view.requestFocusOnField();
    }
    
    /**
     * cancel login
     */
    void loginCancelled() {
        viewer.loggedIn = false;
        viewer.enableMenus(true);
        //SplashWindow.disposeSplash();
        viewer.setVisible(true);
    }
    
    /**
     * Display the login dialog
     * 
     * @param viewer - parent
     * @param modal - modal yes/no
     * @return true if login successful
     */
    @SuppressWarnings("unused")
    private boolean displayLoginDialog(GuiImporter viewer, boolean modal)
    {
        if (modal == true)
        {
            dialog = new LoginDialog(config, viewer, viewer, "Login", modal, center);
            dialog.setAlwaysOnTop(true);
            if (dialog.cancelled == true) return true;
        } else {

            frame = new LoginFrame(config, viewer, viewer, "Login", modal, center);
            frame.addPropertyChangeListener(this);    
            
        }

        return false;
    }

    /**
     * Display login dialog
     * 
     * @param viewer - parent
     * @param modal - modal yes/no
     * @param displayTop - display the top part of the login yes/no
     * @return
     */
    public boolean displayLoginDialog(Object viewer, boolean modal, boolean displayTop)
    {   
        Image img = Toolkit.getDefaultToolkit().createImage(GuiImporter.ICON);
        view = new ScreenLogin(config.getAppTitle(),
                GuiCommonElements.getImageIcon("gfx/login_background.png"),
                img,
                config.getIniVersionNumber(), 
                Integer.toString(config.port.get()));
        view.showConnectionSpeed(false);
        viewTop = new ScreenLogo(config.getAppTitle(), GuiCommonElements.getImageIcon(GuiImporter.splash), img);
        viewTop.setStatusVisible(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension d = viewTop.getExtendedSize();
        Dimension dlogin = view.getPreferredSize();
        Rectangle r;
        int totalHeight;
        if (displayTop)
        {
            totalHeight = d.height+dlogin.height;
            viewTop.setBounds((screenSize.width-d.width)/2, 
                    (screenSize.height-totalHeight)/2, 
                    d.width, viewTop.getSize().height);
            r = viewTop.getBounds();
            
            viewTop.addPropertyChangeListener((PropertyChangeListener) viewer);
            viewTop.addWindowStateListener((WindowStateListener) viewer);
            viewTop.addWindowFocusListener((WindowFocusListener) viewer); 
            view.setBounds(r.x, r.y+d.height, dlogin.width, dlogin.height);
       } else {
            totalHeight = dlogin.height;
            view.setBounds((screenSize.width-d.width)/2,
                    (screenSize.height-totalHeight)/2, 
                    dlogin.width, dlogin.height);
            view.setQuitButtonText("Canel");
        }
        view.addPropertyChangeListener((PropertyChangeListener) viewer);
        view.addWindowStateListener((WindowStateListener) viewer);
        view.addWindowFocusListener((WindowFocusListener) viewer);
        view.setAlwaysOnTop(!displayTop);
        
        
        viewTop.setVisible(displayTop);
        view.setVisible(true);
        
        return true;
    }
    

    
    /**
     * @return true if valid login
     * @throws Exception
     */
    protected boolean isValidLogin() throws Exception
    {
        try
        {
            store = config.createStore();
        }
        catch (Exception e)
        {
            log.error("Login failure.", e);
            return false;
        }
        
        new Thread()
        {
        	public void run()
        	{
        		try
        		{
        			viewer.historyTable.db.initialize(store);
        			viewer.historyTable.db.initializeDataSource();
                	if (viewer.historyTable.db.historyEnabled == false)
                	{
                        log.error("Could not start history DB.");
                        if (HistoryDB.alertOnce == false)
                        {
                            JOptionPane.showMessageDialog(null,
                                "For some reason we are not able to connect to the remote\n" +
                                "history service (this most likely means the server does\n" +
                                "not have this feature installed). In the meantime, you\n" +
                                "will still be able to use the importer, however the\n" +
                                "history tab's functionality will not be enabled.",
                                "Warning",
                                JOptionPane.ERROR_MESSAGE);
                            HistoryDB.alertOnce = true;
                        }        		
                	}
        		}
        		catch (Exception e)
        		{
        			log.error("Error initializing historytablestore", e);
        		}
        	}
        }.start();
        
        return true;
    }

    /**
     *  logout of the server
     */
    public void logout()
    {
    	store.logout();
    }
    
    /**
     * @return OMEROMetadataStore
     */
    public OMEROMetadataStoreClient getMetadataStore()
    {
        return store;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        
        if (!NEW_LOGIN)
        {
            if (prop.equals(LOGIN))
            {
                tryLogin();
            }
            if (prop.equals(LOGIN_CANCELLED))
            {
                loginCancelled();
            }

        } else {
            if (prop.equals(ScreenLogin.LOGIN_PROPERTY)) {
                lc = (LoginCredentials) evt.getNewValue();
                if (lc != null)
                {
                    tryLogin();
                }
            } else if (ScreenLogin.QUIT_PROPERTY.equals(prop)) {
                if (displayTop)
                {
                    if (GuiCommonElements.quitConfirmed(viewer, "Do you really want to quit?")
                            == true)
                    {
                        System.exit(0);
                    }    
                } else {
                    view.dispose();
                }
                
            } else if (ScreenLogin.TO_FRONT_PROPERTY.equals(prop) || 
                    ScreenLogo.MOVE_FRONT_PROPERTY.equals(prop)) {
            } 
        } 
    }

    // Observable methods
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#addObserver(ome.formats.importer.IObserver)
     */
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#deleteObserver(ome.formats.importer.IObserver)
     */
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            try {
                observer.update(this, event);
            } catch (Exception e) 
            {
                log.error(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    public void windowActivated(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    public void windowClosed(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    public void windowClosing(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
     */
    public void windowDeactivated(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
     */
    public void windowDeiconified(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    public void windowIconified(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
     */
    public void windowOpened(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowStateListener#windowStateChanged(java.awt.event.WindowEvent)
     */
    public void windowStateChanged(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowFocusListener#windowGainedFocus(java.awt.event.WindowEvent)
     */
    public void windowGainedFocus(WindowEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowFocusListener#windowLostFocus(java.awt.event.WindowEvent)
     */
    public void windowLostFocus(WindowEvent e)
    {
    }
}
