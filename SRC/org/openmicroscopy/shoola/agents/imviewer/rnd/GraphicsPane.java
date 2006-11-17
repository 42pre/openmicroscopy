/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.GraphicsPane
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;

/** 
 * Component hosting the diagram and the controls to select the pixels intensity 
 * interval and the codomain interval.
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
class GraphicsPane
    extends JPanel
    implements ActionListener, FocusListener, PropertyChangeListener
{

    /** Action command ID to indicate that the start value is modified.*/
    private static final int        START_SELECTED = 0;
    
    /** Action command ID to indicate that the start value is modified.*/
    private static final int        END_SELECTED = 1;
    
    /** Slider to select a sub-interval of [0, 255]. */
    private TwoKnobsSlider      codomainSlider;
    
    /** Slider to select the pixels intensity interval. */
    private TwoKnobsSlider      domainSlider;
    
    /** Field to display the starting pixel intensity value. */
    private JTextField          startField;
    
    /** Field to display the ending pixel intensity value. */
    private JTextField          endField;

    /** The label displaying the global max. */
    private JLabel              maxLabel;
    
    /** The label displaying the global min. */
    private JLabel              minLabel;
    
    /** The component displaying the plane histogram. */
    private GraphicsPaneUI      uiDelegate;
    
    /** Reference to the Model.*/
    protected RendererModel     model;
    
    /** Reference to the Control.*/
    protected RendererControl   controller;

    /** Preview option for render settings */
    private JCheckBox			preview;

    
    /** Initializes the components. */
    private void initComponents()
    {
        uiDelegate = new GraphicsPaneUI(model);
        codomainSlider = new TwoKnobsSlider(RendererModel.CD_START, 
                                        RendererModel.CD_END, 
                                        model.getCodomainStart(),
                                        model.getCodomainEnd());
        codomainSlider.setPaintLabels(false);
        codomainSlider.setPaintEndLabels(false);
        codomainSlider.setPaintTicks(false);
        codomainSlider.setOrientation(TwoKnobsSlider.VERTICAL);
        codomainSlider.addPropertyChangeListener(this);
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        domainSlider = new TwoKnobsSlider((int) model.getGlobalMin(), 
                            (int) model.getGlobalMax(), s, e);
        domainSlider.setPaintLabels(false);
        domainSlider.setPaintEndLabels(false);
        domainSlider.setPaintTicks(false);
        domainSlider.addPropertyChangeListener(this);
        double min = model.getGlobalMin();
        double max = model.getGlobalMax();
        startField = new JTextField();
        startField.setColumns((""+min).length());
        endField = new JTextField();
        endField.setColumns((""+max).length());
        startField.setText(""+s);
        endField.setText(""+e);
        startField.addActionListener(this);
        startField.setActionCommand(""+START_SELECTED);
        startField.addFocusListener(this);
        endField.addActionListener(this);
        endField.setActionCommand(""+END_SELECTED);
        endField.addFocusListener(this);
        maxLabel = new JLabel(""+max);
        minLabel = new JLabel(""+min);
        preview = new JCheckBox("Preview");
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 8;
        p.add(codomainSlider,gbc);
        gbc.gridx = 1;
        gbc.weightx = 60;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 0);
        p.add(uiDelegate, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(domainSlider, gbc);
        gbc.gridy = 2;
        gbc.gridx = 1;
        p.add(preview,gbc);
        add(p);
        
        add(buildFieldsControls());
    }
    
    /**
     * Builds and lays out the UI component hosting the text fields.
     * 
     * @return See above.
     */
    private JPanel buildFieldsControls()
    {
        JPanel p = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(new GridBagLayout());
        c.insets = new Insets(5, 20, 5, 30);
        c.anchor = GridBagConstraints.WEST;
        p.add(buildFieldsPanel("Min", minLabel, "Start", startField), c);
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 1;
        p.add(buildFieldsPanel("Max", maxLabel, "End", endField), c);
        return p;
    }
    
    /**
     * Builds panel used to display the min/start pair or max/end pair.
     * 
     * @param txt1  The text associated to the global value.
     * @param l     The label displaying the global value.
     * @param txt2  The text associated to the interval bound.
     * @param f     The text field displaying the interval bound.
     * @return  See above.
     */
    private JPanel buildFieldsPanel(String txt1, JLabel l, String txt2, 
                                JTextField f)
    {
        JPanel p = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 10, 5, 10);
        c.weightx = 60;
        c.anchor = GridBagConstraints.WEST;
        p.setLayout(new GridBagLayout());
        JLabel label = new JLabel(txt1);
        p.add(label, c);
        c.gridx = 1;
        c.weightx = 40;
        c.anchor = GridBagConstraints.WEST;
        p.add(l, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 60;
        
        label = new JLabel(txt2);
        c.anchor = GridBagConstraints.CENTER;
        
        p.add(label, c);
        c.gridx = 1;
        c.weightx = 40;
        
        p.add(f, c);
        p.validate();
        return p;
    }
    
    /** 
     * Checks the validity of the startField. This method will be called when
     * the startField changes value; user enters data. 
     * 
     * @return true if startField is in a valid range. 
     */
    private boolean startFieldValid()
    {
        boolean valid = false;
        double val = 0;
        double e = model.getWindowEnd();
        try {
            val = Double.parseDouble(startField.getText());
            if (model.getGlobalMin() <= val && val < e) valid = true;
        } catch(NumberFormatException nfe) {}
        return valid;
    }
    
    /** 
     * Checks the validity of the endField. This method will be called when
     * the endField changes value; user enters data. 
     * 
     * @return true if endField is in a valid range. 
     */
    private boolean endFieldValid()
    {
    	 boolean valid = false;
         double val = 0;
         double s = model.getWindowStart();
         try {
             val = Double.parseDouble(endField.getText());
             if (s < val && val <= model.getGlobalMax()) valid = true;
         } catch(NumberFormatException nfe) {}
         return valid;
    }
    
    /** 
     * Handles the action event fired by the start text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a value, then we simply 
     * suggest the user to enter a valid one.
     */
    private void startSelectionHandler()
    {
    	double e = model.getWindowEnd();
    	double val = Double.parseDouble(startField.getText());
        if(val == model.getWindowStart())
        	return;
             
    	if(startFieldValid())
    	{
    		controller.setInputInterval(val, e, true);
    		onCurveChange();
    	}
    	else
    	{
    		startField.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid pixels intensity interval", 
                    "["+val+","+e+"]");
    	}
    }
    
    /** 
     * Handles the action event fired by the end text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a value, then we simply 
     * suggest the user to enter a valid one.
     */
    private void endSelectionHandler()
    {
    	double s = model.getWindowStart();
    	double val = Double.parseDouble(endField.getText());
        if(val == model.getWindowEnd())
        	return;
    	if(startFieldValid())
    	{
    		controller.setInputInterval(s, val, true);
    		onCurveChange();
    	}
    	else
    	{
    		startField.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid pixels intensity interval", 
                    "["+val+","+s+"]");
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the model. 
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the control.
     *                      Mustn't be <code>null</code>.
     */
    GraphicsPane(RendererModel model, RendererControl controller)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (controller == null) 
            throw new NullPointerException("No controller.");
        this.model = model;
        this.controller = controller;
        initComponents();
         buildGUI();
    }

    /** Updates the controls when a new channel is selected. */
    void setSelectedChannel()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        int min = (int) model.getGlobalMin();
        int max = (int) model.getGlobalMax();
        endField.setText(""+e);
        startField.setText(""+s);
        minLabel.setText(""+min);
        maxLabel.setText(""+max);
        domainSlider.setValues(max, min, s, e);
        onCurveChange();
    }
    
    /** Sets the pixels intensity interval. */
    void setInputInterval()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        endField.setText(""+e);
        startField.setText(""+s);
        domainSlider.setStartValue(s);
        domainSlider.setEndValue(e);
        onCurveChange();
    }
    
    /** Sets the value of the codomain interval. */
    void setCodomainInterval()
    {
        codomainSlider.setStartValue(model.getCodomainStart());
        codomainSlider.setEndValue(model.getCodomainEnd());
        onCurveChange();
    }
    
    /** 
     * Updates the UI when a new curve is selected i.e. when a new family
     * is selected or when a new gamma value is selected.
     */
    void onCurveChange()
    {
        uiDelegate.invalidate();
        uiDelegate.repaint();
    }
    
    /**
     * Reacts to property changes fired by the {@link TwoKnobsSlider}s.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		Object source = evt.getSource();
		if (!preview.isSelected()) {
			if (name.equals(TwoKnobsSlider.KNOB_RELEASED_PROPERTY)) {
				if (source.equals(domainSlider)) {
					controller.setInputInterval(domainSlider.getStartValue(),
							domainSlider.getEndValue(), true);
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					int s = codomainSlider.getStartValue();
					int e = codomainSlider.getEndValue();
					controller.setCodomainInterval(s, e, true);
					onCurveChange();
				}
			}
		} else {
			if (name.equals(TwoKnobsSlider.LEFT_MOVED_PROPERTY)
					|| name.equals(TwoKnobsSlider.RIGHT_MOVED_PROPERTY)) {
				if (source.equals(domainSlider)) {
					controller.setInputInterval(domainSlider.getStartValue(),
							domainSlider.getEndValue(), true);
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					int s = codomainSlider.getStartValue();
					int e = codomainSlider.getEndValue();
					controller.setCodomainInterval(s, e, true);
					onCurveChange();
				}
			}
		}
	}

    /**
	 * Sets the pixels intensity window when the start or end value is modified.
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case START_SELECTED:
                    startSelectionHandler(); break;
                case END_SELECTED:
                    endSelectionHandler(); 
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }

    /** 
     * Handles the lost of focus on the start text field and end
     * text field.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * timepoint.
     * @see FocusListener#focusLost(FocusEvent)
     */
    public void focusLost(FocusEvent fe)
    {
      if( fe.getSource() == startField)
    		if( startFieldValid())
    			startSelectionHandler();
    		else
    		{      
    			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
    			un.notifyInfo("Invalid pixels intensity interval", 
                    "["+startField.getText()+","+model.getWindowEnd()+"]");
    			startField.setText(model.getWindowStart()+"");
    		}
    	if( fe.getSource() == endField)
    		if( endFieldValid())
    			endSelectionHandler();
    		else
    		{
    			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
    			un.notifyInfo("Invalid pixels intensity interval", 
                "["+model.getWindowStart()+","+endField.getText()+"]");
    			endField.setText(model.getWindowEnd()+"");
    		}
    	
    }
    
    /** 
     * Required by the {@link FocusListener} I/F but not actually needed 
     * in our case, no op implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}
    
}
