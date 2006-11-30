/*
 * org.openmicroscopy.shoola.agents.hiviewer.IconManager
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports

//Third-party libraries
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;


/** 
 * Provides the icons used by the HiViewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the HiViewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class IconManager
    extends AbstractIconManager
{ 
    
    /** ID of the <code>Minus</code> icon. */
    public static final int     MINUS = 0;
    
    /** ID of the <code>Minus Over</code> icon. */
    public static final int     MINUS_OVER = 1;
  
    /** ID of the <code>Plus</code> icon. */
    public static final int     PLUS = 2;
    
    /** ID of the <code>Plus Over</code> icon. */
    public static final int     PLUS_OVER = 3;
    
    /** ID of the <code>Close</code> icon. */
    public static final int     CLOSE = 4;
    
    /** ID of the <code>Close over</code> icon. */
    public static final int     CLOSE_OVER = 5;
    
    /** ID of the <code>Properties</code> icon. */
    public static final int     PROPERTIES = 6;
    
    /** ID of the <code>Viewer</code> icon. */
    public static final int     VIEWER = 7;

    /** ID of the <code>Annotate</code> icon. */
    public static final int     ANNOTATE = 8;
    
    /** ID of the <code>Zoom In</code> icon. */
    public static final int     ZOOM_IN = 9;
    
    /** ID of the <code>Zoom Out</code> icon. */
    public static final int     ZOOM_OUT = 10;
    
    /** ID of the <code>Zoom Fit</code> icon. */
    public static final int     ZOOM_FIT = 11;
    
    /** ID of the <code>Exit</code> icon. */
    public static final int     EXIT = 12;
    
    /** ID of the <code>Save</code> icon. */
    public static final int     SAVE = 13;
    
    /** ID of the <code>Clear</code> icon. */
    public static final int     CLEAR = 14;
    
    /** ID of the <code>Classify</code> icon. */
    public static final int     CLASSIFY = 15;
    
    /** ID of the <code>Filter with annotation</code> icon. */
    public static final int     FILTER_W_ANNOTATION = 16;
    
    /** ID of the <code>Filter with Title</code> icon. */
    public static final int     FILTER_W_TITLE = 17;
    
    /** ID of the <code>Squary Layout</code> icon. */
    public static final int     SQUARY_LAYOUT = 18;
    
    /** ID of the <code>Tree Layout</code> icon. */
    public static final int     TREE_LAYOUT = 19;
    
    /** ID of the <code>Status Info</code> icon. */
    public static final int     STATUS_INFO = 20;
    
    /** ID of the <code>Root</code> icon. */
    public static final int     ROOT = 21;
    
    /** ID of the <code>Project</code> icon. */
    public static final int     PROJECT = 22;
    
    /** ID of the <code>Dataset</code> icon. */
    public static final int     DATASET = 23;
    
    /** ID of the <code>CategoryGroup</code> icon. */
    public static final int     CATEGORY_GROUP = 24;
    
    /** ID of the <code>Category</code> icon. */
    public static final int     CATEGORY = 25;
    
    /** ID of the <code>Image</code> icon. */
    public static final int     IMAGE = 26;
    
    /** ID of the single-view icon in the browser's internal frame. */
    public static final int     SINGLE_VIEW_MODE = 27;
    
    /** ID of the single-view over icon in the browser's internal frame. */
    public static final int     SINGLE_VIEW_MODE_OVER = 28;

    /** ID of the multi-view icon in the browser's internal frame. */
    public static final int     MULTI_VIEW_MODE = 29;
    
    /** ID of the views list icon in the browser's internal frame. */
    public static final int     VIEWS_LIST = 30;
    
    /** ID of the views list over icon in the browser's internal frame. */
    public static final int     VIEWS_LIST_OVER = 31;
    
    /** ID of the big <code>Category</code> icon. */
    public static final int     CATEGORY_BIG = 32;
    
    /** ID of the <code>Refresh</code> icon. */
    public static final int     REFRESH = 33;
    
    /** ID of the big <code>Save As</code> icon. */
    public static final int     SAVE_AS_BIG = 34;
    
    /** ID of the <code>Question</code> icon. */
    public static final int     QUESTION = 35;
    
    /** ID of the <code>Image medium</code> icon. */
    public static final int     IMAGE_MEDIUM = 36;
    
    /** ID of the <code>Collapse</code> icon. */
    public static final int     COLLAPSE = 37;
    
    /** ID of the <code>Close View</code> icon. */
    public static final int     CLOSE_VIEW = 38;
    
    /** ID of the <code>Exit Application</code> icon. */
    public static final int     EXIT_APPLICATION = 39;
    
    /** ID of the <code>Transparent</code> icon. */
    public static final int     TRANSPARENT = 40;
    
    /** ID of the <code>Find</code> icon. */
    public static final int     FIND = 41;
    
    /** ID of the <code>Annotated Dataset</code> icon. */
    public static final int     ANNOTATED_DATASET = 42;
    
    /** ID of the <code>Annotated Image</code> icon. */
    public static final int     ANNOTATED_IMAGE = 43;
    
    /** ID of the <code>Find</code> icon. */
    public static final int     CLASSIFIED_IMAGE = 44;
    
    /** ID of the <code>Find</code> icon. */
    public static final int     ANNOTATED_CLASSIFIED_IMAGE = 45;
    
    /** ID of the small <code>Annotated</code> icon. */
    public static final int     ANNOTATED_SMALL = 46;
    
    /** ID of the small <code>Annotated</code> icon. */
    public static final int     ANNOTATED_SMALL_OVER = 47;
    
    /** ID of the small <code>Classified</code> icon. */
    public static final int     CLASSIFIED_SMALL = 48;
    
    /** ID of the small <code>Annotated</code> icon. */
    public static final int     CLASSIFIED_SMALL_OVER = 49;
    
    /** The <code>Filter Menu</code> icon. */
    public static int           FILTER_MENU = 50;
    
    /** The <code>Warning</code> icon. */
    public static int           WARNING = 51;
    
    /** The <code>Highlight</code> icon. */
    public static int           HIGHLIGHT = 52;
    
    /** The <code>Info</code> icon. */
    public static int           INFO = 53;
    
    /** The <code>Pin</code> icon. */
    public static int           PIN = 54;
    
    /** The <code>DELETE</code> icon. */
    public static int           DELETE = 55;
    
    /** The <code>Tree View</code> icon. */
    public static int           TREE_VIEW = 56;
    
    /** The <code>ClipBoard View</code> icon. */
    public static int           CLIPBOARD_VIEW = 57;
    
    /** The <code>Sort by Name</code> icon. */
    public static int           SORT_BY_NAME = 58;
    
    /** The <code>Sort by Name</code> icon. */
    public static int           SORT_BY_DATE = 59;

    /** The <code>Lens</code> icon. */
    public static int           LENS = 60;
    
    /** The <code>File Manager 48</code> icon. */
    public static int           VIEWER_48 = 61;
    
    /** The <code>Pin small</code> icon. */
    public static int           PIN_SMALL = 62;
    
    /** The <code>Pin small over</code> icon. */
    public static int           PIN_SMALL_OVER = 63;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 63;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[MINUS] = "minus.png";
        relPaths[MINUS_OVER] = "minus_over.png";
        relPaths[PLUS] = "plus.png";
        relPaths[PLUS_OVER] = "plus_over.png";
        relPaths[CLOSE] = "cross.png";
        relPaths[CLOSE_OVER] = "cross_over.png";
        relPaths[PROPERTIES] = "nuvola_kate16.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[ANNOTATE] = "nuvola_kwrite16.png";
        relPaths[ZOOM_IN] = "nuvola_viewmag+16.png";
        relPaths[ZOOM_OUT] = "nuvola_viewmag-16.png";
        relPaths[ZOOM_FIT] = "nuvola_viewmagfit16.png";
        relPaths[EXIT] = "eclipse_close_view16.png";
        relPaths[SAVE] = "nuvola_save_all16.png";
        relPaths[CLEAR] = "nuvola_history_clear16.png";//"eclipse_clear_co16.png";
        relPaths[CLASSIFY] = "category16.png";
        relPaths[FILTER_W_ANNOTATION] = "eclipse_filter_ps16.png";
        relPaths[FILTER_W_TITLE] = "eclipse_filter_ps16.png";
        relPaths[SQUARY_LAYOUT] = "nuvola_view_multicolumn16.png";
        relPaths[TREE_LAYOUT] = "nuvola_view_tree16.png";
        relPaths[STATUS_INFO] = "nuvola_hwinfo16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[PROJECT] = "nuvola_document16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[CATEGORY_GROUP] = "category_group16.png";
        relPaths[CATEGORY] = "category16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[SINGLE_VIEW_MODE] = "sinlge_view_mode.png";
        relPaths[SINGLE_VIEW_MODE_OVER] = "sinlge_view_mode_over.png";
        relPaths[MULTI_VIEW_MODE] = "nuvola_view_multicolumn16.png";
        relPaths[VIEWS_LIST] = "frame_list.png";
        relPaths[VIEWS_LIST_OVER] = "frame_list_over.png";
        relPaths[CATEGORY_BIG] = "category48.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[SAVE_AS_BIG] = "nuvola_filesaveas48.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[IMAGE_MEDIUM] = "nuvola_image26.png";
        relPaths[CLOSE_VIEW] = "eclipse_close_view16.png";
        relPaths[COLLAPSE] = "eclipse_collapseall16.png";
        relPaths[EXIT_APPLICATION] = "OpenOffice_stock_exit-16.png";
        relPaths[TRANSPARENT] = "eclipse_transparent16.png";
        relPaths[FIND] = "eclipse_searchrecord16.png";
        relPaths[ANNOTATED_DATASET] = "annotated_dataset16.png";
        relPaths[ANNOTATED_IMAGE] = "annotated_image16.png";
        relPaths[CLASSIFIED_IMAGE] = "classified_image16.png";
        relPaths[ANNOTATED_CLASSIFIED_IMAGE] = 
                                    "annotated_classified_image16.png";
        relPaths[ANNOTATED_SMALL] = "kwrite8.png";
        relPaths[ANNOTATED_SMALL_OVER] = "kwrite_over8.png";
        relPaths[CLASSIFIED_SMALL] = "category8.png";
        relPaths[CLASSIFIED_SMALL_OVER] = "category_over8.png";
        relPaths[FILTER_MENU] = "eclipse_view_menu16.png";  
        relPaths[HIGHLIGHT] = "eclipse_default_log_co16.png";
        relPaths[WARNING] = "eclipse_showwarn_tsk16.png";
        relPaths[INFO] = "nuvola_messagebox_info16.png";
        relPaths[PIN] = "nuvola_attach16.png";
        relPaths[DELETE] =  "eclipse_delete_edit16.png";
        relPaths[TREE_VIEW] =  "nuvola_view_tree16.png";
        relPaths[CLIPBOARD_VIEW] =  "nuvola_editpaste16.png";
        relPaths[SORT_BY_NAME] =  "eclipse_alphab_sort_co16.png";
        relPaths[SORT_BY_DATE] =  "eclipse_trace_persp16.png";
        relPaths[LENS] =  "nuvola_viewmag16.png";
        relPaths[VIEWER_48] =  "nuvola_file-manager48.png";
        relPaths[PIN_SMALL] =  "attach8.png";
        relPaths[PIN_SMALL_OVER] = "attach8.png";
    }
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    
    /**
     * Returns the <code>IconManager</code> object. 
     * 
     * @return See above.
     */
    public static IconManager getInstance() 
    { 
        if (singleton == null) 
            singleton = new IconManager(HiViewerAgent.getRegistry());
        return singleton; 
    }
    
    /**
     * Creates a new instance and configures the parameters.
     * 
     * @param registry  Reference to the registry.
     */
    private IconManager(Registry registry)
    {
        super(registry, "/resources/icons/Factory", relPaths);
    }
    
}
