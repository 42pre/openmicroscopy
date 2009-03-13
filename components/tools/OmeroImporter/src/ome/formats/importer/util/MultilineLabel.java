/*
 * org.openmicroscopy.shoola.util.ui.MultilineLabel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 
package ome.formats.importer.util;


//Java imports
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;

//Third-party libraries

//Application-internal dependencies

/** 
 * A multiline, text-only label.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4695 $ $Date: 2006-12-15 17:08:05 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
@SuppressWarnings("serial")
public class MultilineLabel
	extends JTextArea
{

    /** Creates a new instance. */
    public MultilineLabel() { this(""); }
    
	/**
	 * Creates a new instance to display the specified text.
	 * 
	 * @param text	The text to display.
	 */
	public MultilineLabel(String text)
	{
		super(text == null ? "" : text);
	}
	
	/** Plugs into <i>Swing</i>. */
	public void updateUI()
	{
		super.updateUI();
		
		//Turn on wrapping and disable editing.
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
		setOpaque(false);
		
		//Make it appear as a label.
		LookAndFeel.installBorder(this, "Label.border");
		LookAndFeel.installColorsAndFont(this, "Label.background", 
											"Label.foreground", "Label.font");
	}

}
