/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.WellFieldsCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Display all the fields for a given well; based on GridBagLayout
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
class RowFieldCanvas extends WellFieldsCanvas {

    /** The titles of the wells */
    private List<String> titles;

    /** The current thumbnail size */
    private Dimension thumbDim;

    /**
     * Creates a new instance.
     * 
     * @param parent
     *            The parent of the canvas.
     */
    public RowFieldCanvas(final WellFieldsView parent, final WellsModel model) {
        super(parent, model);
        setDoubleBuffered(true);
        setBackground(UIUtilities.BACKGROUND);
        setLayout(new GridBagLayout());

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                WellSampleNode node = getNode(e.getPoint());
                List<WellSampleNode> oldSelection = new ArrayList<WellSampleNode>();
                List<WellSampleNode> newSelection = new ArrayList<WellSampleNode>();

                oldSelection.addAll(model.getSelectedWells());
                
                newSelection.add(node);
                
                for (WellSampleNode n : model.getSelectedWells()) {
                    // copy the old selection if CTRL key down but also keep
                    // selected Wells
                    if (e.isControlDown() || n.isWell()) {
                        newSelection.add(n);
                    }
                }

                if (e.getClickCount() == 2)
                    RowFieldCanvas.this.firePropertyChange(VIEW_PROPERTY, null,
                            newSelection);

                else
                    RowFieldCanvas.this.firePropertyChange(SELECTION_PROPERTY,
                            oldSelection, newSelection);

            }

        });

    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#clear(List, int, Dimension)
     */
    public void clear(List<String> titles, int nFields, final Dimension thumbDim) {
        this.thumbDim = thumbDim;

        removeAll();

        this.titles = titles;

        if (!titles.isEmpty() && nFields > 0) {
            // column header
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 0, 0);
            c.fill = GridBagConstraints.NONE;
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 0;
            c.weighty = 0;
            for (int i = 0; i < nFields; i++) {
                JLabel f = new JLabel("" + (i + 1), SwingConstants.CENTER) {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        if (RowFieldCanvas.this.thumbDim != null)
                            d.width = RowFieldCanvas.this.thumbDim.width;
                        return d;
                    }

                    @Override
                    public Dimension getMinimumSize() {
                        return getPreferredSize();
                    }
                };
                f.setBackground(UIUtilities.BACKGROUND);
                add(f, c);
                c.gridx++;
            }

            // row header
            c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 0, 0);
            c.fill = GridBagConstraints.NONE;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0;
            c.weighty = 0;
            for (String title : titles) {
                JLabel f = new JLabel(title, SwingConstants.CENTER) {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        if (RowFieldCanvas.this.thumbDim != null)
                            d.height = RowFieldCanvas.this.thumbDim.height;
                        return d;
                    }

                    @Override
                    public Dimension getMinimumSize() {
                        return getPreferredSize();
                    }
                };
                f.setBackground(UIUtilities.BACKGROUND);
                add(f, c);
                c.gridy++;
            }

            // add empty component to fill up space
            c = new GridBagConstraints();
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = nFields + 1;
            c.gridy = titles != null ? titles.size() + 1 : 1;
            c.fill = GridBagConstraints.BOTH;
            add(UIUtilities.createComponent(JLabel.class,
                    UIUtilities.BACKGROUND), c);

            displayExistingThumbs();
        }

        revalidate();
        parent.revalidate();
    }

    /**
     * Populates the display with the already loaded {@link WellSampleNode}s.
     * (in contrast to {@link #updateFieldThumb(WellSampleNode)} which
     * adds/updates a specific single {@link WellSampleNode} to the display)
     */
    private void displayExistingThumbs() {
        List<WellSampleNode> l = parent.getNodes();
        if (l == null)
            return;

        for (WellSampleNode n : l) {
            if (!n.getThumbnail().isThumbnailLoaded())
                continue;

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;
            c.gridx = n.getIndex() + 1;
            c.gridy = titles != null ? titles.indexOf(n.getTitle()) + 1 : 1;
            c.weightx = 0;
            c.weighty = 0;
            c.insets = new Insets(2, 2, 0, 0);

            FieldDisplay fd = new FieldDisplay(n);
            add(fd, c);
        }
    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#updateFieldThumb(WellSampleNode)
     */
    public void updateFieldThumb(WellSampleNode node) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.gridx = node.getIndex() + 1;
        c.gridy = titles != null ? titles.indexOf(node.getTitle()) + 1 : 1;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(2, 2, 0, 0);

        FieldDisplay fd = new FieldDisplay(node);
        add(fd, c);

        revalidate();
    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#refreshUI()
     */
    public void refreshUI() {

        for (Component c : getComponents()) {
            // Get a FieldDisplay to determine the new thumbnail dimensions
            // (the scale factor could have been changed)
            if (c instanceof FieldDisplay) {
                FieldDisplay f = (FieldDisplay) c;
                f.refresh();
                this.thumbDim = f.getPreferredSize();
                break;
            }
        }

        for (Component c : getComponents()) {
            // re-scale all FieldDisplays (in case scale factor has been
            // changed)
            if (c instanceof FieldDisplay) {
                ((FieldDisplay) c).refresh();
            }
        }

        // validate and notify parent (to make sure scrollbars are adjusted,
        // etc.)
        revalidate();
        parent.revalidate();
    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#getNode(Point)
     */
    public WellSampleNode getNode(Point p) {
        Component c = findComponentAt(p);
        if (c != null && c instanceof FieldDisplay) {
            return ((FieldDisplay) c).getNode();
        }
        return null;
    }

    /**
     * Checks if the given {@link WellSampleNode} is selected
     * 
     * @param n
     *            The node to check
     * @return <code>true</code> if it is selected.
     */
    boolean isSelected(WellSampleNode n) {
        return this.parent.isSelected(n);
    }

    /**
     * Simple panel displaying the field thumbnail
     * 
     * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
     *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
     *
     */
    class FieldDisplay extends JPanel {

        /** Reference to the WellSamplenode */
        private final WellSampleNode node;

        /** Reference to some predefined color */
        private final Colors colors = Colors.getInstance();

        /** The current magnification factor */
        private double mag = 0;

        /**
         * Creates a new instance with the given node
         * 
         * @param node
         *            The node
         */
        public FieldDisplay(WellSampleNode node) {
            // work with copy otherwise scaling etc. would affect
            // the thumbnail in the well's view too!
            WellSampleNode copy = node.copy();
            copy.setTitle(node.getTitle());
            copy.setWell(false);

            this.node = copy;

            this.mag = parent.getMagnification();
            this.node.getThumbnail().scale(mag);

            setBackground(UIUtilities.BACKGROUND);

            Color col = isSelected(this.node) ? colors
                    .getColor(Colors.TITLE_BAR_HIGHLIGHT) : colors
                    .getColor(Colors.TITLE_BAR);
            setBorder(BorderFactory.createLineBorder(col, 2));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (node.getThumbnail().getDisplayedImage() != null)
                g.drawImage(node.getThumbnail().getDisplayedImage(),
                        getInsets().left, getInsets().top, null);
        }

        @Override
        public Dimension getPreferredSize() {
            BufferedImage img = node.getThumbnail().getDisplayedImage();
            if (img == null)
                return new Dimension(0, 0);

            Insets in = getInsets();
            return new Dimension(img.getWidth() + in.left + in.right,
                    img.getHeight() + in.top + in.bottom);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        /**
         * Adapts the display properties with respect to the current thumbnail
         * scale factor and if it is selected or not.
         */
        public void refresh() {
            Color col = isSelected(this.node) ? colors
                    .getColor(Colors.TITLE_BAR_HIGHLIGHT) : colors
                    .getColor(Colors.TITLE_BAR);
            setBorder(BorderFactory.createLineBorder(col, 2));

            if (this.mag != parent.getMagnification()) {
                this.mag = parent.getMagnification();
                this.node.getThumbnail().scale(mag);
            }
        }

        /**
         * Get the {@link WellSampleNode} represented by this FieldDisplay
         * 
         * @return See above
         */
        public WellSampleNode getNode() {
            return node;
        }

    }
}
