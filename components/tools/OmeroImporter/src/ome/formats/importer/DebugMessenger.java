/*
 * ome.formats.importer.DebugMessenger
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.importer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ome.formats.importer.util.FileUploadContainer;
import ome.formats.importer.util.GuiCommonElements;
import ome.formats.importer.util.FileUploader;
import ome.formats.importer.util.HtmlMessenger;
import ome.formats.importer.util.IniFileLoader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import layout.TableLayout;

/**
 * @author TheBrain
 *
 */
public class DebugMessenger extends JDialog implements ActionListener
{
	/** Logger for this class */
	private Log log = LogFactory.getLog(DebugMessenger.class);
	
    private static final long serialVersionUID = -1026712513033611084L;

    IniFileLoader ini = IniFileLoader.getIniFileLoader();
    
    private Preferences    userPrefs = 
        Preferences.userNodeForPackage(Main.class);

    private String userEmail = userPrefs.get("userEmail", "");
    
    boolean debug = false;

    String url = "http://users.openmicroscopy.org.uk/~brain/omero/bugcollector.php";   
    String tokenUrl = "http://mage.openmicroscopy.org.uk/qa/token/"; 
    String uploaderUrl = "http://mage.openmicroscopy.org.uk/qa/processing/";
    
    private String[] files = null;
    
    private static final String ICON = "gfx/nuvola_error64.png";
    
    GuiCommonElements       gui;
    
    JPanel                  mainPanel;
    JPanel                  commentPanel;
    JPanel                  debugPanel;

    JButton                 quitBtn;
    JButton                 cancelBtn;
    JButton                 sendBtn;
    JButton                 sendWithFilesBtn;
    JButton                 ignoreBtn;
    JButton                 copyBtn;
    
    JTextField              emailTextField;
    String                  emailText           = "";          
    
    JTextArea               commentTextArea;
    String                  commentText         = "";
    
    JTextPane               debugTextPane;
    StyledDocument          debugDocument;
    Style                   debugStyle;
    
    DebugMessenger(JFrame owner, String title, Boolean modal, Throwable e, String[] files)
    {
        super(owner);
        gui = new GuiCommonElements();
        this.files = files;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setTitle(title);
        setModal(modal);
        setResizable(true);
        setSize(new Dimension(680, 500));
        setLocationRelativeTo(owner);
              
        //Get the full debug text
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        
        String debugText = sw.toString();
        
        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
                {{10, 150, TableLayout.FILL, 100, 5, 150, 5, 150, 10}, // columns
                {TableLayout.FILL, 40}}; // rows
        
        mainPanel = gui.addMainPanel(this, mainTable, 10, 10, 10, 10, debug);
        
        
        // Add the quit, cancel and send buttons to the main panel
        quitBtn = gui.addButton(mainPanel, "Quit Application", 'Q',
                "Quit the application", "1, 1, f, c", debug);
        quitBtn.addActionListener(this);
        

        cancelBtn = gui.addButton(mainPanel, "Cancel", 'C',
                "Cancel your message", "3, 1, f, c", debug);
        cancelBtn.addActionListener(this);

        sendBtn = gui.addButton(mainPanel, "Send Comment", 'S',
                "Send your comment to the development team", "5, 1, f, c", debug);
        sendBtn.addActionListener(this);
        
        sendWithFilesBtn = gui.addButton(mainPanel, "Send With Files", 'S',
                "Send your comment and your files to the development team", "7, 1, f, c", debug);
        sendWithFilesBtn.addActionListener(this);
        
        if (this.files == null)
        {
            sendWithFilesBtn.setEnabled(false);
        }

        this.getRootPane().setDefaultButton(sendBtn);
        gui.enterPressesWhenFocused(sendBtn);
        
        // set up the tabbed panes
        JTabbedPane tPane = new JTabbedPane();
        tPane.setOpaque(false); // content panes must be opaque
        
        if (debug == true)
            tPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.red),
                    tPane.getBorder()));
        
        // fill out the comments panel (changes according to icon existance)        
        Icon icon = gui.getImageIcon(ICON);
        
        int iconSpace = 0;
        if (icon != null) iconSpace = icon.getIconWidth() + 10;
        
        double commentTable[][] = 
        {{iconSpace, (160 - iconSpace), TableLayout.FILL}, // columns
                {100, 30, TableLayout.FILL, 110}}; // rows
        
        commentPanel = gui.addMainPanel(this, commentTable, 10, 10, 10, 10, debug);

        tPane.addTab("Comments", null, commentPanel, "Your comments go here.");

        String message = "An error message has been generated by the " +
        "application.\n\nTo help us improve our software, please fill " +
        "out the following form. Your personal details are purely optional, " +
        "and will only be used for development purposes.\n\nPlease note that " +
        "your application may need to be restarted to work properly.";

        JLabel iconLabel = new JLabel(icon);
        commentPanel.add(iconLabel, "0,0, l, c");
        
        @SuppressWarnings("unused")
        JTextPane instructions = 
                gui.addTextPane(commentPanel, message, "1,0,2,0", debug);

        emailTextField = gui.addTextField(commentPanel, "Email: ", emailText, 'E',
        "Input your email address here.", "(Optional)", TableLayout.PREFERRED, "0, 1, 2, 1", debug);
        
        emailTextField.setText(userEmail);
        
        commentTextArea = gui.addTextArea(commentPanel, "What you were doing when you crashed?", 
                "", 'W', "0, 2, 2, 2", debug);
        
        String message2 = "\nIf you choose, you may also upload any files causing this error " +
        		"to our testing team. Once fixed, your files will then be added to our testing " +
        		"suite for regular testing. " +
        		"\n\n" +
        		"Any files you choose to send us will be kept confidential and only used for testing. " +
        		"For details on our data privacy policy, go to http://www.openmicroscopy.org.uk/site/privacy.";
        
        JTextPane upload_instructions = 
            gui.addTextPane(commentPanel, message2, "0,3,2,0", debug);
        
        // fill out the debug panel
        double debugTable[][] = 
        {{TableLayout.FILL}, // columns
        {TableLayout.FILL, 32}}; // rows
        
        debugPanel = gui.addMainPanel(this, debugTable, 10, 10, 10, 10, debug);

        debugTextPane = gui.addTextPane(debugPanel, "", "", 'W', "0, 0", debug);  
        debugTextPane.setEditable(false);

        debugDocument = (StyledDocument) debugTextPane.getDocument();
        debugStyle = debugDocument.addStyle("StyleName", null);
        StyleConstants.setForeground(debugStyle, Color.black);
        StyleConstants.setFontFamily(debugStyle, "SansSerif");
        StyleConstants.setFontSize(debugStyle, 12);
        StyleConstants.setBold(debugStyle, false);

        gui.appendTextToDocument(debugDocument, debugStyle, debugText);
        
        copyBtn = gui.addButton(debugPanel, "Copy to Clipboard", 'C', 
                "Copy the Exception Message to the clipboard", "0, 1, c, b", debug);
        copyBtn.addActionListener(this);
        
        tPane.addTab("Error Message", null, debugPanel,
        "The Exception Message.");

        // Add the tab panel to the main panel
        mainPanel.add(tPane, "0, 0, 8, 0");
        
        add(mainPanel, BorderLayout.CENTER);
        
        setVisible(true);      
        
    }

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        
        
        if (source == quitBtn)
        {
            if (gui.quitConfirmed(this, "Abandon your import and quit the application?") == true)
            {
                System.exit(0);
            }
        }
        
        
        if (source == cancelBtn)
        {
            dispose();
        }
        
        if (source == sendBtn)
        {           
            emailText = emailTextField.getText();
            commentText = commentTextArea.getText();
            String debugText = debugTextPane.getText();
            
            userPrefs.put("userEmail", emailText);
            
            sendRequest(emailText, commentText, debugText, "Extra data goes here.");
        }
        
        if (source == sendWithFilesBtn)
        {
            emailText = emailTextField.getText();
            commentText = commentTextArea.getText();
            String debugText = debugTextPane.getText();
            
            userPrefs.put("userEmail", emailText);
            
            sendFileRequest(emailText, commentText, debugText, "Extra data goes here.");
        }
        
        if (source == ignoreBtn)
        {
            dispose();
        }
        
        if (source == copyBtn)
        {
            debugTextPane.selectAll();
            debugTextPane.copy();
        }
    }

    private void sendRequest(String email, String comment, String error, String extra)
    {
        Map <String, String>map = new HashMap<String, String>();
        extra = "(" + ini.getVersionNumber() + ") " + extra;
        
        map.put("email",email);
        map.put("comment", comment);
        map.put("error", error);
        map.put("extra", extra);
        
        map.put("type", "importer_bugs");
        map.put("java_version", System.getProperty("java.version"));
        map.put("java_class_path", System.getProperty("java.class.path"));
        map.put("os_name", System.getProperty("os.name"));
        map.put("os_arch", System.getProperty("os.arch"));
        map.put("os_version", System.getProperty("os.version"));

        try {
            HtmlMessenger messenger = new HtmlMessenger(url, map);
            String serverReply = messenger.executePost();
            JEditorPane reply = new JEditorPane("text/html", serverReply);
            reply.setEditable(false);
            reply.setOpaque(false);
            JOptionPane.showMessageDialog(this, reply);
            this.dispose();
        }
        catch( Exception e ) {
        	log.error("Error while sending debug information.", e);
            //Get the full debug text
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            String debugText = sw.toString();
            
            gui.appendTextToDocument(debugDocument, debugStyle, "----\n"+debugText);
            String internalURL = "Sorry, but due to an error we were not able " +
            "to automatically \n send your debug information. \n\n" +
            "You can still send us the error message by clicking on the \n" +
            "error message tab, copying the error message to the clipboard, \n" +
            "and sending it to <a href='mailto:comments@openmicroscopy.org.uk'>.";
            JEditorPane message;
            try
            {
                message = new JEditorPane(internalURL);
                JOptionPane.showMessageDialog(this, message);
            } catch (IOException e1){}
        }
    }
    
    private void sendFileRequest(String email, String comment, String error, String extra)
    {
        FileUploadContainer upload = new FileUploadContainer();
        
        upload.setEmail(email);
        upload.setComment(comment);
        upload.setError(error);
        upload.setExtra(extra);
        upload.setCommentType("2");
        upload.setJavaVersion(System.getProperty("java.version"));
        upload.setJavaClasspath(System.getProperty("java.class.path"));
        upload.setOSName(System.getProperty("os.name"));
        upload.setOSArch(System.getProperty("os.arch"));
        upload.setOSVersion(System.getProperty("os.version"));
        upload.setAppVersion(ini.getVersionNumber());
        upload.setFiles(files);

        try {
            Map <String, String>map = new HashMap<String, String>();
            HtmlMessenger messenger = new HtmlMessenger(tokenUrl, map);
            String tokenReply = messenger.executePost();
            
            upload.setSessionId(tokenReply);
            System.err.println(tokenReply);
            
            FileUploader fileUploader = new FileUploader();
            fileUploader.uploadFiles(uploaderUrl, 5000, upload);
        }
        catch( Exception e ) {
            log.error("Error while sending debug information.", e);
            //Get the full debug text
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            String debugText = sw.toString();
            
            gui.appendTextToDocument(debugDocument, debugStyle, "----\n"+debugText);
            String internalURL = "Sorry, but due to an error we were not able " +
            "to automatically \n send your debug information. \n\n" +
            "You can still send us the error message by clicking on the \n" +
            "error message tab, copying the error message to the clipboard, \n" +
            "and sending it to <a href='mailto:comments@openmicroscopy.org.uk'>.";
            JEditorPane message;
            try
            {
                message = new JEditorPane(internalURL);
                JOptionPane.showMessageDialog(this, message);
            } catch (IOException e1){}
        }
    }
        
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        
        if (laf.equals("apple.laf.AquaLookAndFeel"))
        {
            System.setProperty("Quaqua.design", "panther");
            
            try {
                UIManager.setLookAndFeel(
                    "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                );
           } catch (Exception e) { System.err.println(laf + " not supported.");}
        } else {
            try {
                UIManager.setLookAndFeel(laf);
            } catch (Exception e) 
            { System.err.println(laf + " not supported."); }
        }
        
        try {
                HttpClient client = new HttpClient();
                PostMethod method = new PostMethod( "blarg" );
                client.executeMethod( method );
        }
        catch (Exception e)
        {
            new DebugMessenger(null, "Error Dialog Test", true, e, null);
        }
    }
}
