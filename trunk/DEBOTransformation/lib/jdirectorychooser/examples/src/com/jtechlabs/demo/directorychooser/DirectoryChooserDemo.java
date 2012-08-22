/**
 * DirectoryChooserDemo.java
 *
 * Copyright (c) 2005-2006 by Aleksey Prochukhan. All Rights Reserved.
 * Contact e-mail: a.prochukhan@gmail.com
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jtechlabs.demo.directorychooser;

import com.jtechlabs.ui.widget.directorychooser.DirectoryChooserDefaults;
import com.jtechlabs.ui.widget.directorychooser.DirectoryChooserSelectionEvent;
import com.jtechlabs.ui.widget.directorychooser.DirectoryChooserSelectionListener;
import com.jtechlabs.ui.widget.directorychooser.JDirectoryChooser;
import sun.security.action.GetPropertyAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.AccessController;

/**
 * A simple demo appplication that demonstrates how to use <code>JDirectoryChooser</code> component.
 *
 * @version 1.0
 * @author Aleksey Prochukhan
 */
public class DirectoryChooserDemo extends JFrame {
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Constants.
    ////////////////////////////////////////////////////////////////////////////////////////////
    /** MacOSX look&amp;feel. */
    private static final String MAC         = "com.sun.java.swing.plaf.mac.MacLookAndFeel";

    /** Metal look look&amp;feel. */
    private static final String METAL       = "javax.swing.plaf.metal.MetalLookAndFeel";

    /** Motif look&amp;feel. */
    private static final String MOTIF       = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";

    /** Windows look&amp;feel. */
    private static final String WINDOWS     = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    /** GTK look&amp;feel. */
    private static final String GTK         = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Properties.
    ////////////////////////////////////////////////////////////////////////////////////////////
    /** The full name of the Look&amp;Feel class being currently used. */
    private String currentLookAndFeel;

    /** Displays currently selected path. */
    private JLabel infoLabel;

    /** The <code>JDirectoryChooser</code> component placed in the left side of the window. */
    private JDirectoryChooser dc;


    /**
     * Creates the main window.
     */
    public DirectoryChooserDemo() {
        // Set title of the Demo window.
        super("JDirectoryChooser Demo");

        // Override window default behavior.
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Install initial look&amp;feel.
        applyLookAndFeel(getInitialLookAndFeel());

        // Set initial window placement on the screen.
        setInitialWindowPlacement();

        // Create GUI.
        createGUI();
    }

    /**
     * Application entry point.
     *
     * @param args  the command line parameters.
     */
    public static void main(String[] args) {
        // Create an instance of the demo.
        DirectoryChooserDemo hInstance = new DirectoryChooserDemo();

        // Display it on the screen.
        hInstance.show();
    }

    /**
     * Returns intial look&amp;feel class name.
     *
     * @return  the initial look&amp;feel class name.
     */
    private String getInitialLookAndFeel() {
        String os = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
        String java = System.getProperty("java.version");
        if ((os != null) &&
            (java.compareTo("1.5") >= 0) &&
            (os.indexOf("Windows") != -1)) {
            return UIManager.getCrossPlatformLookAndFeelClassName();
        }
        return UIManager.getSystemLookAndFeelClassName();
    }

    /**
     * Creates GUI for this Demo.
     */
    private void createGUI() {
        // Set layout.
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // Create UI components.
        setJMenuBar(createMenuBar());
        contentPane.add(createInfoBar(),BorderLayout.NORTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createDirectoryChooser(), createMainPanel());
        splitPane.setDividerSize(6);
        splitPane.setDividerLocation(170);
        contentPane.add(splitPane,BorderLayout.CENTER);
        contentPane.add(createStatusBar(),BorderLayout.SOUTH);

        // Update GUI according to the current look.
        applyLookAndFeel(currentLookAndFeel);
    }

    /**
     * Creates menu item.
     *
     * @param m             the parent menu.
     * @param action        the action to bind to this menu item.
     * @param keyStroke     the hot key for this menu item.
     * @param mnemonic      the mnemonic for this menu item.
     * @return              the created menu item.
     */
    private JMenuItem createJMenuItem(JMenu m, Action action, String keyStroke, char mnemonic) {
        JMenuItem mi = ((JMenuItem)m.add(new JMenuItem(action)));
        mi.setAccelerator(KeyStroke.getKeyStroke(keyStroke));
        mi.setMnemonic(mnemonic);
        return mi;
    }

    /**
     * Creates radio button looks menu item.
     *
     * @param m             the parent menu.
     * @param group         the button group.
     * @param name          the name of this menu item.
     * @param mnemonic      the mnemonic for this menu item.
     * @return              the created menu item.
     */
    private JRadioButtonMenuItem createJRadioButtonLooksMenuItem(
            JMenu m,
            ButtonGroup group,
            String name,
            String look,
            char mnemonic) {
        JRadioButtonMenuItem mi = ((JRadioButtonMenuItem)m.add(new JRadioButtonMenuItem(name)));
        mi.setMnemonic(mnemonic);
        mi.setEnabled(isAvailableLookAndFeel(look));
        if (mi.isEnabled()) {
            mi.addActionListener(new ChangeLookAndFeelAction(look));
            group.add(mi);
            if (currentLookAndFeel.compareTo(look) == 0) {
                mi.setSelected(true);
            }
        }
        return mi;
    }

    /**
     * Creates radio button icons menu item.
     *
     * @param m             the parent menu.
     * @param group         the button group.
     * @param name          the name of this menu item.
     * @param mnemonic      the mnemonic for this menu item.
     * @return              the created menu item.
     */
    private JRadioButtonMenuItem createJRadioButtonIconsMenuItem(
            JMenu m,
            ButtonGroup group,
            String name,
            int theme,
            boolean isSelected,
            char mnemonic) {
        JRadioButtonMenuItem mi = ((JRadioButtonMenuItem)m.add(new JRadioButtonMenuItem(name)));
        group.add(mi);
        mi.setMnemonic(mnemonic);
        mi.setSelected(isSelected);
        mi.addActionListener(new ChangeIconsAction(theme));
        return mi;
    }

    /**
     * Creates check box menu item.
     *
     * @param m             the parent menu.
     * @param name          the name of this menu item.
     * @param mnemonic      the mnemonic for this menu item.
     * @return              the created menu item.
     */
    private JCheckBoxMenuItem createJCheckBoxMenuItem (
            JMenu m,
            String name,
            int access,
            char mnemonic) {
        JCheckBoxMenuItem mi = ((JCheckBoxMenuItem)m.add(new JCheckBoxMenuItem(name,true)));
        mi.setMnemonic(mnemonic);
        mi.addActionListener(new ChangeAccessAction(access));
        return mi;
    }


    /**
     * Creates main menu for this Demo.
     * @return  the menu bar to use for this Demo.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Create File menu.
        JMenu mFile = new JMenu("File");
        mFile.setMnemonic('F');
        createJMenuItem(mFile,new ShowDialogAction(),"ctrl O",'S');
        createJMenuItem(mFile,new UserHomeAction(),"ctrl H",'U');
        mFile.addSeparator();
        createJMenuItem(mFile,new ExitAction(),"ctrl Q",'x');
        menuBar.add(mFile);

        // Create Look &amp; Feel menu.
        JMenu mLook = new JMenu("Look & Feel");
        mLook.setMnemonic('L');
        ButtonGroup lookGroup = new ButtonGroup();
        createJRadioButtonLooksMenuItem(mLook,lookGroup,"Java Look & Feel",METAL,'o');
        createJRadioButtonLooksMenuItem(mLook,lookGroup,"Macintosh Look & Feel",MAC,'M');
        createJRadioButtonLooksMenuItem(mLook,lookGroup,"Windows Style Look & Feel",WINDOWS,'W');
        createJRadioButtonLooksMenuItem(mLook,lookGroup,"Motif Look & Feel",MOTIF,'f');
        createJRadioButtonLooksMenuItem(mLook,lookGroup,"GTK Style Look & Feel",GTK,'G');
        menuBar.add(mLook);

        JMenu mIcons = new JMenu("Theme");
        mIcons.setMnemonic('T');
        ButtonGroup iconsGroup = new ButtonGroup();
        createJRadioButtonIconsMenuItem(mIcons,iconsGroup,"Default icons",JDirectoryChooser.ICONS_DEFAULT,true,'D');
        createJRadioButtonIconsMenuItem(mIcons,iconsGroup,"Look & Feel icons",JDirectoryChooser.ICONS_LOOK_AND_FEEL,false,'L');
        createJRadioButtonIconsMenuItem(mIcons,iconsGroup,"Native Icons",JDirectoryChooser.ICONS_NATIVE,false,'N');
        createJRadioButtonIconsMenuItem(mIcons,iconsGroup,"Custom icons...",JDirectoryChooser.ICONS_CUSTOM,false,'C');
        menuBar.add(mIcons);

        JMenu mSecurity = new JMenu("Security");
        mSecurity.setMnemonic('S');
        createJCheckBoxMenuItem(mSecurity,"CREATE",JDirectoryChooser.ACCESS_NEW,'C');
        createJCheckBoxMenuItem(mSecurity,"DELETE",JDirectoryChooser.ACCESS_DELETE,'D');
        createJCheckBoxMenuItem(mSecurity,"RENAME",JDirectoryChooser.ACCESS_RENAME,'R');
        createJCheckBoxMenuItem(mSecurity,"MOVE",JDirectoryChooser.ACCESS_MOVE,'M');
        createJCheckBoxMenuItem(mSecurity,"COPY",JDirectoryChooser.ACCESS_COPY,'O');
        menuBar.add(mSecurity);

        JMenu mHelp = new JMenu("Help");
        mHelp.setMnemonic('H');
        createJMenuItem(mHelp,new AboutAction(),"F1",'A');
        menuBar.add(mHelp);

        return menuBar;
    }

    /**
     * Creates info bar for this Demo.
     *
     * @return  the info bar to use for this Demo.
     */
    private JComponent createInfoBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
        infoLabel = new JLabel("",getIcon("info.png"),SwingConstants.LEFT);
        infoLabel.setFont(new Font("Dialog",Font.BOLD,14));
        infoLabel.setBorder(new EmptyBorder(3,6,3,6));
        infoLabel.setOpaque(true);
        infoLabel.setBackground(UIManager.getColor("controlShadow"));
        infoLabel.setForeground(UIManager.getColor("textHighlightText"));
        p.add(infoLabel,BorderLayout.CENTER);
        return p;
    }


    /**
     * Creates status bar for this Demo.
     *
     * @return  the status bar to use for this Demo.
     */
    private JComponent createStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(1,6,1,6));
        JLabel l = new JLabel(" \u00a9 2005-2006 JTechLabs.com. All Rights Reserved. ");
        l.setOpaque(false);
        p.add(l,BorderLayout.WEST);
        return p;
    }

    /**
     * Creates directory chooser for this Demo.
     *
     * @return  the directory chooser for this Demo.
     */
    private JComponent createDirectoryChooser() {
        // Set default options.
        DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_ICONS_THEME, JDirectoryChooser.ICONS_DEFAULT);
        DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_ACCESS, JDirectoryChooser.ACCESS_FULL);
        DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_SIZE, new Dimension(183,296));
        DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_ROOT_ICON,getIcon("customroot.png"));
        DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_DIRECTORY_ICON,getIcon("customdir.png"));
        DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_SELECTED_DIRECTORY_ICON,getIcon("customdirselected.png"));
        DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_COLLAPSED_ICON,getIcon("customcollapsed.png"));
        DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_EXPANDED_ICON,getIcon("customexpanded.png"));

        // Create directory chooser with deafult settings.
        dc = new JDirectoryChooser();
        dc.setBorder(new EmptyBorder(0,0,0,0));
        dc.addDirectoryChooserSelectionListener(new DemoDirectoryChooserSelectionListener());
        dc.expandFirstRoot();

        // Return component.
        return dc;
    }

    /**
     * Creates main panel with description of the <code>JDirectoryChooser</code> component.
     *
     * @return  the control panel for <code>JDirectoryChooser</code>.
     */
    private JComponent createMainPanel() {
        JEditorPane ep = new JEditorPane();
        ep.setEditable(false);
        try {
            ep.setPage(getClass().getResource("resources/about.html"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        JScrollPane sp = new JScrollPane(ep);
        sp.setBorder(new EmptyBorder(0,0,0,0));
        return sp;
    }


    /**
     * Loads given icon.
     *
     * @param icon  the name of the icon to load.
     * @return  the loaded icon.
     */
    private Icon getIcon(final String icon) {
        try {
	        final Class c = JDirectoryChooser.class;
            java.awt.image.ImageProducer ip = (java.awt.image.ImageProducer)
            java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                java.net.URL url;
                if ((url = c.getResource("/com/jtechlabs/demo/directorychooser/resources/" + icon)) == null) {
                    return null;
                } else {
                    try {
                    return url.getContent();
                    } catch (java.io.IOException ioe) {
                    return null;
                    }
                }
                }
            });

            if (ip == null)
            return null;
            java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
            return new ImageIcon(tk.createImage(ip));
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * Checks if given look&amp;feel is supported on the current platform.
     *
     * @param lookClassName     the full name of the class of the given look &amp; feel.
     * @return                  <code>true</code> if given look is supported, <code>false</code> otherwise.
     */
    private boolean isAvailableLookAndFeel(String lookClassName) {
        try {
            Class lookClass = Class.forName(lookClassName);
            LookAndFeel lookAndFeel = (LookAndFeel)(lookClass.newInstance());
            return lookAndFeel.isSupportedLookAndFeel();
        } catch(Exception e) {
            return false;
        }
     }

    /**
     * Applies given look &amp; feel.
     *
     * @param look  the full name of the look&amp;feel class.
     */
    private void applyLookAndFeel(String look) {
        if ((currentLookAndFeel == null) ||
            (currentLookAndFeel.compareTo(look) != 0)) {
            try {
                // Apply look&amp;feel.
                UIManager.setLookAndFeel(currentLookAndFeel = look);

                // Notify components about update.
                SwingUtilities.updateComponentTreeUI(this);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,"Failed to apply '" + look + "' Look & Feel","Error",JOptionPane.ERROR_MESSAGE);
            }

        }

        if (infoLabel != null) {
            infoLabel.setFont(new Font("Dialog",Font.BOLD,14));
            infoLabel.setBorder(new EmptyBorder(1,6,1,6));
            infoLabel.setOpaque(true);
            infoLabel.setBackground(UIManager.getColor("controlShadow"));
            infoLabel.setForeground(UIManager.getColor("textHighlightText"));
        }
    }

    /**
     * Sets initial placement of the window on the screen.
     */
    private void setInitialWindowPlacement () {
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = ss.width;
        int screenHeight = ss.height;
        int initWidth = 678;
        int initHeight = 394;
        int initX = (screenWidth - initWidth) / 2;
        int initY = (screenHeight - initHeight) / 2;
        setSize(initWidth,initHeight);
        setLocation(initX,initY);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes.
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles event of changing selection in the <code>JDirectoryChooser</code> component.
     */
    private class DemoDirectoryChooserSelectionListener implements DirectoryChooserSelectionListener {
        /**
         * Called whenever the selection in a <code>JDirectoryChooser</code> changes.
         *
         * @param event   the event object that contains information about new selection.
         */
        public void valueChanged(DirectoryChooserSelectionEvent event) {
            infoLabel.setText(event.getPath().toString());
        }
    }

    /**
     * Action handler for 'File->User Home Directory...' menu item.
     */
    private class UserHomeAction extends AbstractAction {
        /**
         * Constructor.
         */
        public UserHomeAction() {
            super("User Home Directory...",getIcon("userhome.png"));
        }

        /**
         * Event handler.
         *
         * @param e     the action event.
         */
        public void actionPerformed(ActionEvent e) {
            dc.setSelectedDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        }
    }

    /**
     * Action handler for 'Show Dialog' button.
     */
    private class ShowDialogAction extends AbstractAction {
        /**
         * Constructor.
         */
        public ShowDialogAction() {
            super("Select Directory...", getIcon("open.png"));
        }

        /**
         * Event handler.
         *
         * @param e     the action event.
         */
        public void actionPerformed(ActionEvent e) {
            dc.setSelectedDirectory(
                    JDirectoryChooser.showDialog(
                            DirectoryChooserDemo.this,
                            dc.getSelectedDirectory(), "Select Path", "Select Working Directory", dc.getAccess())
            );
        }
    }

    /**
     * Action handler for 'File->Exit' menu item.
     */
    private class ExitAction extends AbstractAction {
        /**
         * Constructor.
         */
        public ExitAction() {
            super("Exit");
        }

        /**
         * Event handler.
         *
         * @param e     the action event.
         */
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    /**
     * Changes Look&amp;Feel of this Demo.
     */
    private class ChangeLookAndFeelAction extends AbstractAction {
        /**
         * Full name of the look&amp;feel class.
         */
        private String look;

        /**
         * Constructor.
         */
        public ChangeLookAndFeelAction(String look) {
            super("");
            this.look = look;
        }

        /**
         * Event handler.
         *
         * @param e     the action event.
         */
        public void actionPerformed(ActionEvent e) {
            applyLookAndFeel(look);
        }
    }

    /**
      * Changes Icons' theme of the <code>JDirectoryChooser</code> component.
      */
     private class ChangeIconsAction extends AbstractAction {
         /**
          * Icons theme..
          */
         private int theme;

         /**
          * Constructor.
          */
         public ChangeIconsAction(int theme) {
             super("");
             this.theme = theme;
         }

         /**
          * Event handler.
          *
          * @param e     the action event.
          */
         public void actionPerformed(ActionEvent e) {
             dc.setIconsTheme(theme);
             DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_ICONS_THEME, theme);
         }
     }

    /**
     * Modifies security rights of the <code>JDirectoryChooser</code>.
     */
    private class ChangeAccessAction extends AbstractAction {
        /**
         * The access to add/remove.
         */
        private int access;

        /**
         * Constructor.
         */
        ChangeAccessAction(int access) {
            this.access = access;
        }

        /**
         * Event handler.
         *
         * @param e     the action event.
         */
        public void actionPerformed(ActionEvent e) {
            if (((JCheckBoxMenuItem)e.getSource()).isSelected()) {
                dc.addAccess(access);
            } else {
                dc.removeAccess(access);
            }
        }
    }

    /**
     * Action handler for 'Help->About' menu item.
     */
    private class AboutAction extends AbstractAction {
        /**
         * Constructor.
         */
        public AboutAction() {
            super("About",getIcon("about.png"));
        }

        /**
         * Event handler.
         *
         * @param e     the action event.
         */
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(
                    DirectoryChooserDemo.this,
                    "<html>The simple JDirectoryChooser Demo Application<br><br>" +
                    " \u00a9 2005-2006 JTechLabs.com. All Rights Reserved.",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}