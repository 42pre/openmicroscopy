/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.CodomainPane
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;



//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import ome.model.display.CodomainMapContext;
import ome.model.display.ContrastStretchingContext;
import ome.model.display.PlaneSlicingContext;
import ome.model.display.ReverseIntensityContext;
import org.openmicroscopy.shoola.agents.imviewer.IconManager;

/** 
 * Pane displaying the controls used to define the transformations happening 
 * in the device space or codomain i.e. sub-interval of [0, 255].
 * 
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
class CodomainPane
    extends ControlPane
{

    /** Button to bring up the contrast stretching modal dialog. */
    private JButton         contrastStretchingButton;
    
    /** Button to bring up the plane slicing modal dialog. */
    private JButton         planeSlicingButton;
    
    /** Box to select the {@link ReverseIntensityContext}. */
    private JCheckBox       reverseIntensity;
    
    /** Box to select the {@link ContrastStretchingContext}. */
    private JCheckBox       contrastStretching;
    
    /** Box to select the {@link PlaneSlicingContext}. */
    private JCheckBox       planeSlicing;
    
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        IconManager icons = IconManager.getInstance();
        contrastStretchingButton = new JButton(icons.getIcon(
                        IconManager.CONTRAST_STRETCHING));
        contrastStretchingButton.setEnabled(false);
        contrastStretchingButton.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
                /*
                CodomainMapContext ctx = 
                    model.getCodomainMap(PlaneSlicingContext.class);
                JDialog dialog = new ContrastStretchingDialog(view, ctx, 
                        model.getCodomainStart(), model.getCodomainEnd());
                dialog.addPropertyChangeListener(controller);
                UIUtilities.centerAndShow(dialog);
                */
                
                
            }
        });
        
        planeSlicingButton = new JButton(icons.getIcon(
                            IconManager.PLANE_SLICING));
        planeSlicingButton.setEnabled(false);
        planeSlicingButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                /*
                CodomainMapContext ctx = 
                    model.getCodomainMap(PlaneSlicingContext.class);
                JDialog dialog = new PlaneSlicingDialog(view, ctx, 
                        model.getCodomainStart(), model.getCodomainEnd());
                dialog.addPropertyChangeListener(controller);
                UIUtilities.centerAndShow(dialog);
                */
            }

        });
        reverseIntensity = new JCheckBox(
                controller.getAction(RendererControl.REVERSE_INTENSITY));
        planeSlicing = new JCheckBox(
                controller.getAction(RendererControl.PLANE_SLICING));
        contrastStretching = new JCheckBox(
                controller.getAction(RendererControl.CONTRAST_STRETCHING));
        setDefaultValues();
    }
    
    /** Sets the default values. */
    private void setDefaultValues()
    {
        List cdMaps = model.getCodomainMaps();
        Iterator i = cdMaps.iterator();
        CodomainMapContext ctx;
        while (i.hasNext()) {
            ctx = (CodomainMapContext) i.next();
            if (ctx instanceof ReverseIntensityContext) 
                reverseIntensity.setSelected(true);
            else if (ctx instanceof ContrastStretchingContext)  {
                contrastStretching.setSelected(true);
                contrastStretchingButton.setEnabled(true);
            } else if (ctx instanceof PlaneSlicingContext)  {
                planeSlicing.setSelected(true);
                planeSlicingButton.setEnabled(true);
            } 
        }
    }
    
    /**
     * lays out the controls.
     * 
     * @return See below.
     */
    private JPanel buildControlsPane()
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        p.add(reverseIntensity, c);
        c.gridy = 1;
        c.gridx = 0;
        c.fill = GridBagConstraints.NONE;
        p.add(contrastStretching , c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.EAST;
        p.add(contrastStretchingButton, c);
        
        c.gridy = 2;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        p.add(planeSlicing , c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.EAST;
        p.add(planeSlicingButton, c);
        return p;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        add(buildControlsPane());
    }
    
    /** 
     * Returns the name of the component. 
     * @see ControlPane#getPaneName()
     */
    protected String getPaneName() { return "Device Settings"; }

    /**
     * Returns the icon attached to the component.
     * @see ControlPane#getPaneIcon()
     */
    protected Icon getPaneIcon()
    {
        IconManager icons = IconManager.getInstance();
        return icons.getIcon(IconManager.CODOMAIN);
    }

    /**
     * Returns the brief description of the component.
     * @see ControlPane#getPaneDescription()
     */
    protected String getPaneDescription()
    {
        return "Selects the transformations happening in the device space.";
    }
    
    /**
     * Returns the index of the component.
     * @see ControlPane#getPaneIndex()
     */
    protected int getPaneIndex() { return ControlPane.CODOMAIN_PANE_INDEX; }

    /** Resets the default rendering settings. */
    protected void resetDefaultRndSettings()
    {
        reverseIntensity.removeActionListener(
                controller.getAction(RendererControl.REVERSE_INTENSITY));
        planeSlicing.removeActionListener(
                controller.getAction(RendererControl.PLANE_SLICING));
        contrastStretching.removeActionListener(
                controller.getAction(RendererControl.CONTRAST_STRETCHING));
        setDefaultValues();
        reverseIntensity.setAction(
                controller.getAction(RendererControl.REVERSE_INTENSITY));
        planeSlicing.setAction(
                controller.getAction(RendererControl.PLANE_SLICING));
        contrastStretching.setAction(
                controller.getAction(RendererControl.CONTRAST_STRETCHING));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     */
    CodomainPane(RendererModel model, RendererControl controller)
    {
        super(model, controller);
        initComponents();
        buildGUI();
    }

    
    /**
     * Updates the corresponding controls when a codomain transformation
     * is added.
     * 
     * @param mapType The type of codomain transformation. 
     */
    void addCodomainMap(Class mapType)
    {
        if (mapType.equals(PlaneSlicingContext.class))
            planeSlicingButton.setEnabled(true);
        else if (mapType.equals(ContrastStretchingContext.class))
            contrastStretchingButton.setEnabled(true);
    }
    
    /**
     * Updates the corresponding controls when a codomain transformation
     * is added.
     * 
     * @param mapType The type of codomain transformation. 
     */
    void removeCodomainMap(Class mapType)
    {
        if (mapType.equals(PlaneSlicingContext.class))
            planeSlicingButton.setEnabled(false);
        else if (mapType.equals(ContrastStretchingContext.class))
            contrastStretchingButton.setEnabled(false);
    }

}
