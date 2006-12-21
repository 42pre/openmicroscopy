/*
 * org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaver
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

package org.openmicroscopy.shoola.agents.imviewer.util.saver;



//Java imports
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.image.io.Encoder;
import org.openmicroscopy.shoola.util.image.io.TIFFEncoder;
import org.openmicroscopy.shoola.util.image.io.WriterImage;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A modal dialog to save the currently rendered image
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
public class ImgSaver
    extends JDialog
{
    
    /** The window's title. */
    static final String TITLE = "Save Image";
    
    /** The title of the preview window. */
    static final String PREVIEW_TITLE =  "Preview image to save.";
    
    /** Reference to the model. */
    private ImViewer        model;
    
    /** The UI delegate. */
    private ImgSaverUI      uiDelegate;
    
    /** The name of the file to save. */
    private String          name;
    
    /** The file's format. */
    private String          format;
    
    /** The message displayed when the file is saved. */
    private String          saveMessage;
    
    /** The main image i.e. the one displayed in the viewer. */
    private BufferedImage   mainImage;
    
    /** 
     * The list of images composing the main image. <code>null</code> if no
     * components.
     */
    private List            imageComponents;
    
    /**
     * Displays the preview dialog with images depending on the 
     * saving type.
     * 
     * @param savingType The type of saving.
     */
    private void showPreview(int savingType)
    {
        ImgSaverPreviewer preview = new ImgSaverPreviewer(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        switch (savingType) {
            default:
            case ImgSaverUI.IMAGE:
                mainImage = model.getDisplayedImage();
                imageComponents = null;
                break;
            case ImgSaverUI.IMAGE_AND_COMPONENTS:
                mainImage = model.getDisplayedImage();
                imageComponents = model.getImageComponents();
                break;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        preview.initialize();
        UIUtilities.centerAndShow(preview);
    }
    
    /**
     * Writes the image.
     * 
     * @param image The image to write to the file.
     * @param n     The name of the image.
     */
    private void writeImage(BufferedImage image, String n)
    {
        UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
        n += "."+format;
        File f = new File(n);
        try {
            if (format.equals(TIFFFilter.TIF)) {
                Encoder encoder = new TIFFEncoder(Factory.createImage(image), 
                        new DataOutputStream(new FileOutputStream(f)));
                WriterImage.saveImage(encoder);
            } else WriterImage.saveImage(f, image, format);
            un.notifyInfo("Image Saved", saveMessage);
            setClosed(true);
        } catch (Exception e) {
            e.printStackTrace();
            f.delete();
            un.notifyError("Save image failure", "Unable to save the image", e);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public ImgSaver(JFrame owner, ImViewer model)
    {
        super(owner);
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        setTitle(TITLE);
        setModal(true);
        uiDelegate = new ImgSaverUI(this);
        pack();
    }
 
    /**
     * Sets the name of the file to save.
     * 
     * @param name The name to set.
     */
    void setFileName(String name) { this.name = name; }
    
    /**
     * Sets the format of the file.
     * 
     * @param format The file's format.
     */
    void setFileFormat(String format) { this.format = format; }
    
    /** 
     * Sets the save message.
     * 
     * @param saveMessage The message to set.
     */
    void setFileMessage(String saveMessage) { this.saveMessage = saveMessage; }
    
    /** Brings up a preview of the image or images to save. 
     * 
     * @param exist Pass <code>true</code> to bring up the selection dialog
     *              prior to the preview image widget, <code>false</code>
     *              otherwise.
     */
    void previewImage(boolean exist)
    {
        if (exist) {
            IconManager im = IconManager.getInstance();
            ImgSaverSelectionDialog d = new ImgSaverSelectionDialog(this, 
                    im.getIcon(IconManager.QUESTION));
            UIUtilities.centerAndShow(d);
        } else showPreview(uiDelegate.getSavingType());
    }
    
    /**
     * Sets the window visible depending on the specified flag.
     * 
     * @param b Pass <code>true</code> to close the window, <code>false</code>
     *          otherwise.
     */
    void setClosed(boolean b)
    {
       if (b) {
           setVisible(false);
           dispose();
       }
    }

    /** Saves the displayed image. */
    void saveImage()
    {
        //Builds the image to display.
        boolean unitBar = model.isUnitBar();
        String v = model.getUnitBarValue(); 
        int s = (int) model.getUnitBarSize();
        if (imageComponents == null) {
            int width = mainImage.getWidth();
            int h = mainImage.getHeight();

            BufferedImage newImage = new BufferedImage(width, h, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = (Graphics2D) newImage.getGraphics();
            g2.setColor(Color.WHITE);
            ImagePaintingFactory.setGraphicRenderingSettings(g2);
            //Paint the original image.
            g2.drawImage(mainImage, null, 0, 0); 
            
            if (unitBar && v != null)
                ImagePaintingFactory.paintScaleBar(g2, width-s-10, h-10, s, v);
            writeImage(newImage, name);
        } else {
            int width = mainImage.getWidth();
            int h = mainImage.getHeight();
            int n = imageComponents.size();
            int w = width*(n+1)+ImgSaverPreviewer.SPACE*(n-1);

            BufferedImage newImage = new BufferedImage(w, h, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = (Graphics2D) newImage.getGraphics();
            g2.setColor(Color.WHITE);
            ImagePaintingFactory.setGraphicRenderingSettings(g2);
            //Paint the original image.
            Iterator i = imageComponents.iterator();
            int x = 0;
            while (i.hasNext()) {
                g2.drawImage((BufferedImage) i.next(), null, x, 0); 
                if (unitBar && v != null)
                    ImagePaintingFactory.paintScaleBar(g2, x+width-s-10, h-10, 
                    									s, v);
                x += width;
                g2.fillRect(x, 0, ImgSaverPreviewer.SPACE, h);
                x += ImgSaverPreviewer.SPACE;
            }
            g2.drawImage(mainImage, null, x, 0); 
            if (unitBar && v != null)
                ImagePaintingFactory.paintScaleBar(g2, x+width-s-10, h-10, s, 
                									v);
            writeImage(newImage, name);
        }
    }

    /**
     * Returns the main image.
     * 
     * @return See above.
     */
    BufferedImage getImage() { return mainImage; }
    
    /**
     * Returns the images composing the main image i.e. one per channel
     * if two or more channels compose the main image. 
     * Returns <code>null</code> otherwise.
     * 
     * @return See above.
     */
    List getImageComponents() { return imageComponents; }

    /**
     * Returns <code>true</code> if the unit bar is painted on top of 
     * the displayed image, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isUnitBar() { return model.isUnitBar(); }

    /**
     * Returns the value (with two decimals) of the unit bar or 
     * <code>null</code> if the actual value is <i>negative</i>.
     * 
     * @return See above.
     */
    String getUnitBarValue() { return model.getUnitBarValue(); }

    /**
     * Returns the size of the unit bar.
     * 
     * @return See above.
     */
    double getUnitBarSize() { return model.getUnitBarSize(); }
    
    /**
     * Returns the color of the unit bar.
     * 
     * @return See above.
     */
    Color getUnitBarColor() { return model.getUnitBarColor(); }
    
}
