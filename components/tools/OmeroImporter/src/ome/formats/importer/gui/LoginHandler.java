/*
 * ome.formats.testclient.LoginHandler
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

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
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.util.Actions;
import ome.formats.importer.util.GuiCommonElements;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmicroscopy.shoola.util.ui.login.LoginCredentials;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogin;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogo;

/**
 * ImageExporter is master file format exporter for all supported formats and
 * exports the files to an OMERO database
 * 
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 * @basedOnCodeFrom Curtis Rueden ctrueden at wisc.edu
 */
public class LoginHandler implements IObservable, ActionListener, WindowListener, PropertyChangeListener, WindowStateListener, WindowFocusListener
{
	/** Logger for this class */
	private static Log log = LogFactory.getLog(LoginHandler.class);
    
    private static boolean NEW_LOGIN = true;
    
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    public volatile JFrame      f;
    
    private boolean            center;
    
    private String             username;

    private String             password;

    private int                port;

    private String             server;

    private Main               viewer;

    private Preferences        userPrefs = Preferences
                                                 .userNodeForPackage(LoginHandler.class);

    private OMEROMetadataStoreClient store;
    
    public LoginDialog         dialog;
    
    public LoginFrame          frame;

    private LoginCredentials    lc;
    
    private boolean modal, displayTop;
    
    private HistoryTable historyTable = null;
    
    private GuiCommonElements   gui;

    LoginHandler(Main viewer, boolean modal, boolean center)
    {
        this.viewer = viewer;
        this.center = center;
        this.modal = modal;
        
        gui = new GuiCommonElements();
        
        historyTable = HistoryTable.getHistoryTable();
        if (historyTable != null)
            addObserver(historyTable);
        
        viewer.enableMenus(false);
        
        displayLogin(true);
    }
    
    public static synchronized LoginHandler getLoginHandler(Main viewer, boolean modal, boolean center)
    {
        if (ref == null) 
        try
        {
            ref = new LoginHandler(viewer, modal, center);
        } catch (Exception e)
        {
        	log.error("Error constructing login handler.", e);
            JOptionPane.showMessageDialog(null,
                    "Unknown exception.\n\n" +
                    "We were not able to connect to the server.\n" +
                    "Click OK to exit.",
                    "Warning",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);

        }
        return ref;
    }
    
    public static synchronized LoginHandler getLoginHandler()
    {
        if (ref == null)
            throw new RuntimeException ("LoginHandler not created yet.");
        return ref;
    }
    
    private static LoginHandler ref;
    
    public void displayLogin(boolean displayTop)
    {
        boolean cancelled;
        this.displayTop = displayTop;
        
        if (NEW_LOGIN)
        {
            cancelled = viewer.displayLoginDialog(this, modal, displayTop);
        } else {
            cancelled = displayLoginDialog(viewer, modal);  
        }
        
        if (modal == true && cancelled == true)
        {
            loginCancelled();
        }
        
        if (modal == true && cancelled == false)
        {
            tryLogin();
        }
    }
    
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
                    
                    if (!modal)
                    {
                        username = frame.username;
                        password = frame.password;
                        server = frame.currentServer;
                        port = frame.port;
                        frame.updateServerList(server);                    
                    }
                    else
                    {
                        username = dialog.username;
                        password = dialog.password;
                        server = dialog.currentServer;
                        port = dialog.port;
                        dialog.updateServerList(server);                    
                    }
                    userPrefs.put("username", username);
                    userPrefs.put("server", server);
                    userPrefs.putInt("port", port);
                }
                else
                {
                	username = lc.getUserName();
                	password = lc.getPassword();
                	server = lc.getHostName();
                	port = lc.getPort();
                }
            
                viewer.statusBar.setStatusIcon("gfx/server_trying16.png",
                "Trying to connect to " + server);
                viewer.statusBar.setProgress(true, -1, "connecting....");
                
                try
                {
                    if (isValidLogin())
                    {
                        if (NEW_LOGIN)
                        {
                            viewer.view.close();
                            viewer.viewTop.close();
                            viewer.setVisible(true);
                        }

                        viewer.statusBar.setProgress(false, 0, "");
                        viewer.appendToOutput("> Login Successful.\n");
                        log.info("Login successful!");
                        viewer.enableMenus(true);
                        viewer.setImportEnabled(true);
                        viewer.loggedIn = true;
                        notifyObservers("LOGGED_IN", null);
                                                
                        // if this fails, using the old server without repositorySpace
                        try {
                            long freeSpace = store.getRepositorySpace();
                            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                            String freeMB = formatter.format(freeSpace/1000);                
                            viewer.statusBar.setStatusIcon("gfx/server_connect16.png",
                                    "Connected to " + server + ". Free space: " + 
                                    freeMB + " MB.");
                            return;
                        } catch (Exception e) 
                        {
                        	log.error("Exception retrieving repository free space.", e);
                            viewer.statusBar.setStatusIcon("gfx/server_connect16.png", "Connected to " + server + ".");
                            return;
                        }
                    } else {   
                        if (NEW_LOGIN)
                            viewer.view.setAlwaysOnTop(false);
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
                    "Server connection to " + server +" failed. " +
                            "Please try again.");
                    
                    if (NEW_LOGIN)
                        viewer.view.setAlwaysOnTop(false);
                    
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

    void refreshNewLogin()
    {
        viewer.view.setAlwaysOnTop(true);
        //viewer.viewTop.setAlwaysOnTop(true); 
        viewer.view.requestFocusOnField();
    }
    
    void loginCancelled() {
        viewer.loggedIn = false;
        viewer.enableMenus(true);
        //SplashWindow.disposeSplash();
        viewer.setVisible(true);
    }
    
    private boolean displayLoginDialog(Main viewer, boolean modal)
    {
        if (modal == true)
        {
            dialog = new LoginDialog(viewer, viewer, "Login", modal, center);
            dialog.setAlwaysOnTop(true);
            if (dialog.cancelled == true) return true;
        } else {

            frame = new LoginFrame(viewer, viewer, "Login", modal, center);
            frame.addPropertyChangeListener(this);    
            
        }

        return false;
    }

    private boolean isValidLogin() throws Exception
    {
        try
        {
            store = new OMEROMetadataStoreClient();
            store.initialize(username, password, server, port);
        }
        catch (Exception e)
        {
            log.error("Login failure.", e);
            return false;
        }
        return true;
    }

    public void logout()
    {
    	store.logout();
    }
    
    public OMEROMetadataStoreClient getMetadataStore()
    {
        return store;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        
        if (!NEW_LOGIN)
        {
            if (prop.equals(Actions.LOGIN))
            {
                tryLogin();
            }
            if (prop.equals(Actions.LOGIN_CANCELLED))
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
                    if (gui.quitConfirmed(viewer, "Do you really want to quit?")
                            == true)
                    {
                        System.exit(0);
                    }    
                } else {
                    viewer.view.dispose();
                }
                
            } else if (ScreenLogin.TO_FRONT_PROPERTY.equals(prop) || 
                    ScreenLogo.MOVE_FRONT_PROPERTY.equals(prop)) {
            } 
        } 
    }

    // Observable methods
    
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(Object message, Object[] args)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, message, args);
        }
    }

    public void actionPerformed(ActionEvent e)
    {
    }

    public void windowActivated(WindowEvent e)
    {
    }

    public void windowClosed(WindowEvent e)
    {
    }

    public void windowClosing(WindowEvent e)
    {
    }

    public void windowDeactivated(WindowEvent e)
    {
    }

    public void windowDeiconified(WindowEvent e)
    {
    }

    public void windowIconified(WindowEvent e)
    {
    }

    public void windowOpened(WindowEvent e)
    {
    }

    public void windowStateChanged(WindowEvent e)
    {
    }

    public void windowGainedFocus(WindowEvent e)
    {
    }

    public void windowLostFocus(WindowEvent e)
    {
    }
}
