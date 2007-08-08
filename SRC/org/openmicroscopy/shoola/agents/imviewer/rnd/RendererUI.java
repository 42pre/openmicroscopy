/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.RendererUI
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;


//Java imports
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * The {@link Renderer} view. Provides a menu bar, a status bar and a 
 * panel hosting various controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class RendererUI
    extends JPanel//TopWindow
{
    
    /** Identifies the {@link DomainPane}. */
    static final Integer        DOMAIN = new Integer(0);
    
    /** Identifies the {@link CodomainPane}. */
    static final Integer        CODOMAIN = new Integer(1);
    
    /** Reference to the control. */
    private RendererControl     			controller;
    
    /** Reference to the model. */
    private RendererModel       			model;
    
    /** The map hosting the controls pane. */
    private HashMap<Integer, ControlPane>	controlPanes;
    
    /** Button to copy the rendering settings. */
    private JButton							copyButton;
    
    /** Initializes the components. */
    private void initComponents()
    {
    	copyButton = new JButton("Copy");
    	copyButton.setToolTipText(
    		UIUtilities.formatToolTipText("Copies the rendering settings."));
    	copyButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				model.getParentModel().copyRenderingSettings();
			}
		
		});
    }
    
    /**
     * Creates the menu bar.
     * 
     * @return See above
     */
    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar(); 
        menuBar.add(createControlsMenu());
        return menuBar;
    }
    
    /**
     * Helper method to create the <code>Controls</code> menu.
     * 
     * @return See above.
     */
    private JMenu createControlsMenu()
    {
        IconManager icons = IconManager.getInstance();
        JMenu menu = new JMenu("Controls");
        JMenuItem item = new JMenuItem(
                controller.getAction(RendererControl.SAVE_SETTINGS));
        item.setIcon(icons.getIcon(IconManager.SAVE_SETTINGS));
        menu.add(item);
        item = new JMenuItem(
                controller.getAction(RendererControl.RESET_SETTINGS));
        item.setIcon(icons.getIcon(IconManager.RESET_SETTINGS));
        menu.add(item);
        return menu;
    }
    
    /** Creates the panels hosting the rendering controls. */
    private void createControlPanes()
    {
        ControlPane p = new DomainPane(model, controller);
        p.addPropertyChangeListener(controller);
        controlPanes.put(DOMAIN, p);
        p = new CodomainPane(model, controller);
        p.addPropertyChangeListener(controller);
        controlPanes.put(CODOMAIN, p);
    }
    
    /**
     * Creates the accept, revert buttons on the bottom on the panel.
     * 
     * @return See above.
     */
    private JPanel createButtonsPanel()
    {
    	/*
    	JButton acceptButton, revertButton;
    	JPanel p = new JPanel();
    	GridBagConstraints gbc = new GridBagConstraints();
   	   	
    	revertButton = new JButton(
                controller.getAction(RendererControl.RESET_SETTINGS));
        
        acceptButton = new JButton(
                controller.getAction(RendererControl.SAVE_SETTINGS));
        
      	p.setLayout(new GridBagLayout());
    	gbc.gridx = 0;
    	gbc.anchor = GridBagConstraints.EAST;
        p.add(acceptButton, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 14);
        p.add(revertButton, gbc);
        return p;
        */
    	JPanel bar = new JPanel();
    	bar.setBorder(null);
    	bar.add(copyButton);
    	JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	/*
        Container c = getContentPane();
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP,
                                JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        ControlPane pane = controlPanes.get(DOMAIN);
        tabs.insertTab(pane.getPaneName(), pane.getPaneIcon(), pane,
                        pane.getPaneDescription(), pane.getPaneIndex());
        pane = controlPanes.get(CODOMAIN);
        tabs.insertTab(pane.getPaneName(), pane.getPaneIcon(), pane,
                        pane.getPaneDescription(), pane.getPaneIndex());
        c.setLayout(new BorderLayout());
        c.add(tabs,BorderLayout.NORTH);
        c.add(createButtonPanel(),BorderLayout.SOUTH);
        */
    	JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP,
                JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		ControlPane pane = controlPanes.get(DOMAIN);
		tabs.insertTab(pane.getPaneName(), pane.getPaneIcon(), 
				new JScrollPane(pane), pane.getPaneDescription(), 
				pane.getPaneIndex());
		pane = controlPanes.get(CODOMAIN);
		tabs.insertTab(pane.getPaneName(), pane.getPaneIcon(), 
					new JScrollPane(pane), pane.getPaneDescription(), 
					pane.getPaneIndex());
		setLayout(new BorderLayout());
    	add(tabs, BorderLayout.CENTER);
    	add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates a new instance. The method 
     * {@link #initialize(RendererControl, RendererModel) initialize}
     * should be called straight after.
     * 
     * @param title The name of the image.
     */
    RendererUI(String title)
    {
        //super("Display Settings:  "+title);
        controlPanes = new HashMap<Integer, ControlPane>(2);
    }
    
    /**
     * Links the MVC triad.
     * 
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     */
    void initialize(RendererControl controller, RendererModel model)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.controller = controller;
        this.model = model;
        initComponents();
        //setJMenuBar(createMenuBar());
        createControlPanes();
        buildGUI();
        //pack();
    }

    /**
     * Updates the corresponding controls when a codomain transformation
     * is added.
     * 
     * @param mapType The type of codomain transformation. 
     */
    void addCodomainMap(Class mapType)
    {
        CodomainPane pane = (CodomainPane) controlPanes.get(CODOMAIN);
        pane.addCodomainMap(mapType);
    }
    
    /**
     * Updates the corresponding controls when a codomain transformation
     * is added.
     * 
     * @param mapType The type of codomain transformation. 
     */
    void removeCodomainMap(Class mapType)
    {
        CodomainPane pane = (CodomainPane) controlPanes.get(CODOMAIN);
        pane.removeCodomainMap(mapType);
    }
    
    /**
     * Sets the specified channel as current.
     * 
     * @param c The channel's index.
     */
    void setSelectedChannel(int c)
    {
        DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
        pane.setSelectedChannel(c);
    }

    /** 
     * Sets the color of the specified channel
     * 
     * @param c The channel's index.
     */
    void setChannelButtonColor(int c)
    {
        DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
        pane.setChannelButtonColor(c);
    }
    
    /** Sets the pixels intensity interval. */
    void setInputInterval()
    {
        DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
        pane.setInputInterval();
    }

    /** Resets the UI controls. */
    void resetDefaultRndSettings()
    {
        Iterator i = controlPanes.keySet().iterator();
        ControlPane pane;
        while (i.hasNext()) {
            pane = controlPanes.get(i.next());
            pane.resetDefaultRndSettings();
        }
    }

	/**
	 * This is a method which is triggered from the {@link RendererControl} 
     * if the colour model has changed.
	 */
	void setColorModelChanged() 
	{
	     DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
	     pane.setColorModelChanged();
	 
	}

    /** 
     * Updates the UI when a new curve is selected i.e. when a new family
     * is selected or when a new gamma value is selected.
     */
    void onCurveChange()
    {
        DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
        pane.onCurveChange();
    }
    
}
