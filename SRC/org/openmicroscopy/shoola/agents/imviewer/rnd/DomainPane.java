/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.DomainPane
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.actions.NoiseReductionAction;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelButton;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelToggleButton;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Pane displaying the controls used to define the transformation process
 * of the pixels intensity values.
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
class DomainPane
    extends ControlPane
    implements ActionListener, ChangeListener
{
    
    /** 
     * For slider control only. The minimum value for the curve coefficient. 
     * The real value is divived by 10.
     */
    static final int            	MIN_GAMMA = 1;
    
    /** 
     * For slider control only. The maximum value for the curve coefficient. 
     * The real value is divived by 10.
     */
    static final int            	MAX_GAMMA = 40;
    
    /** 
     * For slider control only. The default value for the curve coefficient. 
     * The real value is divived by 10.
     */
    static final int            	DEFAULT_GAMMA = 10;
 
    /** The minimum value of the bit resolution. */ 
    static final int            	MIN_BIT_DEPTH = 1;
    
    /** The maximum value of the bit resolution. */ 
    static final int            	MAX_BIT_DEPTH = 8;
    
    /** The default value of the bit resolution. */ 
    static final int            	DEFAULT_BIT_DEPTH = 8;
    
    /** The factor .*/
    private static final int    	FACTOR = 10;
    
    /** Identifies the <code>Family</code> selection. */
    private static final int    	FAMILY = 0;
    
    /** Text displayed when the advanced options are hidden. */
    private static final String		SHOW_ADVANCED_OPTIONS = 
    									"Show Advanced Options"; 
    
    /** Text displayed when the advanced options are shown. */
    private static final String		HIDE_ADVANCED_OPTIONS = 
    									"Hide Advanced Options"; 
    
    /** Dimension of the box between the channel buttons. */
    private static final Dimension 	VBOX = new Dimension(1, 10);
       
    /** Box to select the family used in the mapping process. */
    private JComboBox       			familyBox;

    /** 
     * A collection of ColourButtons which represent the channel selected 
     * in the mapping process. 
     */
    private List<ChannelToggleButton>	channelList;
    
    /** A panel containing the channel buttons. */
    private JPanel						channelButtonPanel;
    
    /** A panel displaying the advanced features. */
    private JPanel						advancedPanel;
    
    /** Slider to select a curve in the family. */
    private JSlider         			gammaSlider;
    
    /** Slider to select the bit resolution of the rendered image. */
    private JSlider        	 			bitDepthSlider;
    
    /** Field displaying the <code>Gamma</code> value. */
    private JTextField      			gammaLabel;
    
    /** Field displaying the <code>Bit Depth</code> value. */
    private JTextField      			bitDepthLabel;
    
    /** Box to select the mapping algorithm. */
    private JCheckBox       			noiseReduction;
    
    /** Button to bring up the histogram widget on screen. */
    private JButton         			histogramButton;
    
    /** The UI component hosting the interval selections. */
    private GraphicsPane    			graphicsPane;
    
    /** The button will display the advanced mapping options when clicked. */
    private JButton						advancedOptionsButton;
    
    /** A flag denoting whether the advanced options are showing. */
    private boolean 					isAdvancedSettingsShowing;
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        graphicsPane = new GraphicsPane(model, controller);
        familyBox = new JComboBox(model.getFamilies().toArray());
        String family = model.getFamily();
        familyBox.setSelectedItem(family);
        familyBox.addActionListener(this);
        familyBox.setActionCommand(""+FAMILY);
        
        double k = model.getCurveCoefficient();
        gammaSlider = new JSlider(JSlider.HORIZONTAL, 
        							MIN_GAMMA, MAX_GAMMA, 
                                    (int) (k*FACTOR));
        gammaSlider.setEnabled(!(family.equals(RendererModel.LINEAR) || 
                family.equals(RendererModel.LOGARITHMIC)));
        gammaSlider.addChangeListener(this);
        gammaLabel = new JTextField(""+k);
        gammaLabel.setEnabled(false);
        gammaLabel.setEditable(false);
        int v = model.getBitResolution();
        bitDepthSlider = new JSlider(JSlider.HORIZONTAL, MIN_BIT_DEPTH, 
                                MAX_BIT_DEPTH, convertBitResolution(v));
        bitDepthSlider.addChangeListener(this);
        bitDepthLabel = new JTextField(""+v);
        bitDepthLabel.setEnabled(false);
        bitDepthLabel.setEditable(false);
        noiseReduction = new JCheckBox();
        noiseReduction.setSelected(model.isNoiseReduction());
        noiseReduction.setAction(
                controller.getAction(RendererControl.NOISE_REDUCTION));
        histogramButton = new JButton(
                controller.getAction(RendererControl.HISTOGRAM));
        
        channelList = new ArrayList<ChannelToggleButton>();
        channelButtonPanel = createChannelButtons();
        isAdvancedSettingsShowing = false;
        advancedOptionsButton = new JButton(SHOW_ADVANCED_OPTIONS);
        advancedOptionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { handleClick(); }
		});
    }
    
    /** Resets the value of the bit resolution. */
    private void resetBitResolution()
    {
        int v = model.getBitResolution();
        bitDepthSlider.removeChangeListener(this);
        bitDepthSlider.setValue(convertBitResolution(v));
        bitDepthSlider.addChangeListener(this);
        bitDepthLabel.setText(""+v);
        bitDepthLabel.repaint();
    }
    
	/**
	 * Handles mouse clicks on the {@link #advancedOptionsButton}.
	 * The {@link #advancedPanel} is shown/hidden depending on the current 
	 * value of {@link #isAdvancedSettingsShowing}, which is then modified to
	 * reflect the new state.  Also the {@link #advancedOptionsButton} text 
	 * is changed accordingly.
	 */
	private void handleClick()
	{
		if (isAdvancedSettingsShowing) {
			advancedOptionsButton.setText(SHOW_ADVANCED_OPTIONS);
			remove(advancedPanel);
		} else {
			advancedOptionsButton.setText(HIDE_ADVANCED_OPTIONS);
			add(advancedPanel);
		}
		controller.resizeRenderUI();
		isAdvancedSettingsShowing = !isAdvancedSettingsShowing;
	}
    
    /**
     * Creates the channel buttons on the left hand side of the histogram.
     * 
     * @return panel containing the buttons.
     */
    private JPanel createChannelButtons()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        ChannelMetadata[] data = model.getChannelData();
        boolean gs = model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL);
        ChannelMetadata d;
        ChannelToggleButton item;
        p.add(Box.createRigidArea(VBOX));
        for (int j = 0; j < data.length; j++) {
        	d = data[j];
        	item = new ChannelToggleButton(""+d.getEmissionWavelength(), 
        							model.getChannelColor(j), j);
        	channelList.add(item);
        	item.setSelected(model.getSelectedChannel() == j);

        	item.setGrayedOut(gs);
        	item.addPropertyChangeListener(controller);
        	item.setPreferredSize(ChannelButton.DEFAULT_MIN_SIZE);
        	item.setMinimumSize(ChannelButton.DEFAULT_MIN_SIZE);
            p.add(item);
            p.add(Box.createRigidArea(VBOX));
        }
        return UIUtilities.buildComponentPanel(p);     
    }
    
    /**
     * Create a panel showing the channel buttons and histogram.
     *  
     * @return See above.
     */
    private JPanel buildChannelGraphicsPanel()
    {
    	JPanel p = new JPanel();
    	p.setLayout(new BorderLayout());
    	p.add(channelButtonPanel, BorderLayout.WEST);
    	p.add(graphicsPane, BorderLayout.CENTER);
    	return p;
    }
    
    /**
     * Lays out the slider and its corresponding text area.
     * 
     * @param slider    The slider to lay out.
     * @param field     The text area.
     * @return A panel hosting the component.
     */
    private JPanel buildSliderPane(JSlider slider, JTextField field)
    {
        JPanel p = new JPanel();
        p.add(slider);
        p.add(field);
        return UIUtilities.buildComponentPanel(p);
    }
    
    /**
     * Builds the pane hosting the main rendering controls.
     * 
     * @return See above.
     */
    private JPanel buildControlsPane()
    {
        JPanel p = new JPanel();
        double size[][] =
        {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED},  // Columns
         {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}; // Rows
        p.setLayout(new TableLayout(size));
        JLabel label = new JLabel("Map");
        p.add(label, "0, 0");
        p.add(UIUtilities.buildComponentPanel(familyBox), "2, 0");
        label = new JLabel("Gamma");
        p.add(label, "0, 1");
        p.add(buildSliderPane(gammaSlider, gammaLabel), "2, 1");
        label = new JLabel("Bit Depth");
        p.add(label, "0, 2");
        p.add(buildSliderPane(bitDepthSlider, bitDepthLabel), "2, 2");
        /*
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(new GridBagLayout());
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel label;
        c.gridx = 1;
        label = new JLabel("Map");
        c.gridx = 0;
        c.gridy = 1;
        p.add(label, c);
        c.gridx = 1;
        p.add(UIUtilities.buildComponentPanel(familyBox), c);
        label = new JLabel("Gamma");
        c.gridx = 0;
        c.gridy = 2;
        p.add(label, c);
        c.gridx = 1;
        p.add(buildSliderPane(gammaSlider, gammaLabel), c);
        label = new JLabel("Bit Depth");
        c.gridx = 0;
        c.gridy = 3;
        p.add(label, c);
        c.gridx = 1;
        p.add(buildSliderPane(bitDepthSlider, bitDepthLabel), c);
        */
        
        return p;
    }
    
    /**
     * Builds and lays out the panel hosting the {@link #noiseReduction}
     * and {@link #histogramButton} controls.
     * 
     * @return See above.
     */
    private JPanel buildPane()
    {
        JPanel p = new JPanel();
        double size[][] =
        {{TableLayout.PREFERRED},  // Columns
         {TableLayout.PREFERRED, TableLayout.PREFERRED}}; // Rows
        p.setLayout(new TableLayout(size));
        p.add(noiseReduction, "0, 0");
        p.add(histogramButton, "0, 1");
        /*
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        p.add(noiseReduction, c);
        c.gridy = 1;
        p.add(histogramButton, c);
        */
        return UIUtilities.buildComponentPanel(p);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	double size[][] =
        {{TableLayout.PREFERRED},  // Columns
         {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, 
        	TableLayout.PREFERRED}}; // Rows
    	setLayout(new TableLayout(size));
    	add(buildChannelGraphicsPanel(), "0, 0");
    	add(new JSeparator(), "0, 1");
    	add(buildControlsPane(), "0, 2");
    	add(new JSeparator(), "0, 3");
    	add(buildPane(), "0, 4");
    	/*
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(buildChannelGraphicsPanel());
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(advancedOptionsButton, BorderLayout.EAST);
        //add(UIUtilities.buildComponentPanelRight(advancedOptionsButton));
        advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, 
        								BoxLayout.Y_AXIS));
        advancedPanel.add(new JSeparator());
        advancedPanel.add(buildControlsPane());
        advancedPanel.add(new JSeparator());
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(buildPane());
        //p.add(Box.createHorizontalStrut(200));
        advancedPanel.add(p);
        add(advancedPanel);
        //if (isAdvancedSettingsShowing) add(advancedPanel);
         *
         */
    }
    
    /**
     * Returns the bit resolution corresponding to the UI value.
     * 
     * @param uiValue   The UI value to convert.
     * @return See above.
     */
    private int convertUIBitResolution(int uiValue)
    {
        switch (uiValue) {
            case 1: return RendererModel.DEPTH_1BIT;
            case 2: return RendererModel.DEPTH_2BIT;  
            case 3: return RendererModel.DEPTH_3BIT;
            case 4: return RendererModel.DEPTH_4BIT;
            case 5: return RendererModel.DEPTH_5BIT;
            case 6: return RendererModel.DEPTH_6BIT;
            case 7: return RendererModel.DEPTH_7BIT;
            case 8: 
            default:
                    return RendererModel.DEPTH_8BIT;
        }
    }
    
    /**
     * Converts the bit resolution value into its corresponding UI value.
     * 
     * @param value The value to convert.
     * @return See above.
     */
    private int convertBitResolution(int value)
    {
        switch (value) {
            case RendererModel.DEPTH_1BIT: return 1;
            case RendererModel.DEPTH_2BIT: return 2;  
            case RendererModel.DEPTH_3BIT: return 3;
            case RendererModel.DEPTH_4BIT: return 4;
            case RendererModel.DEPTH_5BIT: return 5;
            case RendererModel.DEPTH_6BIT: return 6;
            case RendererModel.DEPTH_7BIT: return 7;
            case RendererModel.DEPTH_8BIT: 
            default:
                    return 8;
        }
    }

    /**
     * Resets the value of the gamma slider and the gamma label.
     * 
     * @param k The value to set.
     */
    private void resetGamma(double k)
    {
    	gammaSlider.removeChangeListener(this);
        gammaSlider.setValue((int) (k*FACTOR));
        gammaSlider.addChangeListener(this);
        gammaLabel.setText(""+k);
    }
    
    /** 
     * Returns the name of the component. 
     * @see ControlPane#getPaneName()
     */
    protected String getPaneName() { return "Mapping"; }

    /**
     * Returns the icon attached to the component.
     * @see ControlPane#getPaneIcon()
     */
    protected Icon getPaneIcon()
    {
        IconManager icons = IconManager.getInstance();
        return icons.getIcon(IconManager.DOMAIN);
    }

    /**
     * Returns the brief description of the component.
     * @see ControlPane#getPaneDescription()
     */
    protected String getPaneDescription()
    {
        return "Define the mapping context for the pixels intensity values.";
    }

    /**
     * Returns the index of the component.
     * @see ControlPane#getPaneIndex()
     */
    protected int getPaneIndex() { return ControlPane.DOMAIN_PANE_INDEX; }
    
    /** Resets the default rendering settings. */
    protected void resetDefaultRndSettings()
    {
        setInputInterval();
        setSelectedChannel(model.getSelectedChannel());
        setCodomainInterval();
        resetBitResolution();
        ChannelToggleButton btn;
        boolean gs = model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL);
        for (int i = 0; i < channelList.size(); i++) {
            btn = channelList.get(i);
            btn.setColor(model.getChannelColor(btn.getChannelIndex()));
            btn.setGrayedOut(gs);
        }  
        resetGamma(model.getCurveCoefficient());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     */
    DomainPane(RendererModel model, RendererControl controller)
    {
        super(model, controller);
        initComponents();
        buildGUI();
    }
    
    /**
     * Modifies the rendering controls depending on the currently selected
     * channel.
     * 
     * @param c The selected channel.
     */
    void setSelectedChannel(int c)
    {
        graphicsPane.setSelectedChannel();
        String f = model.getFamily();
        familyBox.removeActionListener(this);
        familyBox.setSelectedItem(f);
        familyBox.addActionListener(this);
        
        
        boolean b = !(f.equals(RendererModel.LINEAR) || 
    			f.equals(RendererModel.LOGARITHMIC));
        double k = 1;
        if (b) k = model.getCurveCoefficient();
        resetGamma(k);
        gammaSlider.setEnabled(b);
        noiseReduction.removeActionListener(
                controller.getAction(RendererControl.NOISE_REDUCTION));
        noiseReduction.setSelected(model.isNoiseReduction());
        noiseReduction.addActionListener(
                controller.getAction(RendererControl.NOISE_REDUCTION));
        noiseReduction.setText(NoiseReductionAction.NAME);
        for (int i = 0; i < channelList.size(); i++) 
        	channelList.get(i).setSelected(i==c);
    }
    
    /** Sets the pixels intensity interval. */
    void setInputInterval() { graphicsPane.setInputInterval(); }
    
    /** Sets the value of the codomain interval. */
    void setCodomainInterval() { graphicsPane.setCodomainInterval(); }
    
    /**
     * Set the colour of the channel button c.
     *  
     * @param c The channel whose colour changed.
     */
    void setChannelButtonColor(int c)
    {
        ChannelToggleButton btn = channelList.get(c);
        btn.setColor(model.getChannelColor(c));
        boolean gs = model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL);
        if (gs)  btn.setGrayedOut(gs);
    }
    
    /**
     * Fired if the colour model has been changed, toggles between RGB and 
     * Greyscale. 
     */
    void setColorModelChanged() 
    {
        ChannelToggleButton btn;
        String colorModel = model.getColorModel();
        for (int i = 0 ; i < channelList.size() ; i++) {
            btn = channelList.get(i);
            btn.setColor(model.getChannelColor(i));
            btn.setGrayedOut(colorModel.equals (ImViewer.GREY_SCALE_MODEL));
        }
    }
    
    /** 
     * Updates the UI when a new curve is selected i.e. when a new family
     * is selected or when a new gamma value is selected.
     */
    void onCurveChange()
    { 
        String f = model.getFamily();
        boolean b = !(f.equals(RendererModel.LINEAR) || 
        			f.equals(RendererModel.LOGARITHMIC));
        double k = 1;
        if (b) k = model.getCurveCoefficient();
        resetGamma(k);
        gammaSlider.setEnabled(b);
        graphicsPane.onCurveChange(); 
    }
    
    /**
     * Depending on the source of the event. Sets the gamma value or
     * the bit resolution.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        Object source = e.getSource();
        if (source.equals(gammaSlider)) {
            double v = (double) gammaSlider.getValue()/FACTOR;
            double oldValue = model.getCurveCoefficient();
            gammaLabel.setText(""+v);
            firePropertyChange(GAMMA_PROPERTY, new Double(oldValue), 
                            new Double(v));
        } else if (source.equals(bitDepthSlider)) {
            int v = convertUIBitResolution(bitDepthSlider.getValue());
            int oldValue = model.getBitResolution();
            bitDepthLabel.setText(""+v);
            firePropertyChange(BIT_RESOLUTION_PROPERTY, new Integer(oldValue), 
                    new Integer(v));
        }
    }
    
    /**
     * Reacts to family or channel selection.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case FAMILY:
                    String f = (String) 
                            ((JComboBox) e.getSource()).getSelectedItem();
                    firePropertyChange(FAMILY_PROPERTY, model.getFamily(), f);
                    break;
            }
        } catch(NumberFormatException nfe) {  
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }

}
