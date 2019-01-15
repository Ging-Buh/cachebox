package de;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

public class launch extends JFrame {

    public static final ImageIcon ICON_COMPUTER = new ImageIcon("computer.gif");
    public static final ImageIcon ICON_DISK = new ImageIcon("disk.gif");
    public static final ImageIcon ICON_FOLDER = new ImageIcon("folder.gif");
    public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon("expandedfolder.gif");
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static launch that;
    final String directoryPath = "Android_GUI/";
    private final String BR = System.getProperty("line.separator");
    protected JTree m_tree;
    protected DefaultTreeModel m_model;
    protected JTextField m_display;
    private JDesktopPane jDesktopPane1;
    private JButton btnPackNow;
    private String selectedPath = "";
    private JScrollPane jScrollPane1;
    private JTextArea jTextArea1;

    {
        // Set Look & Feel

        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Create the frame.
     */
    public launch() {

        TeePrintStream tee = new TeePrintStream(System.out);
        System.setOut(tee);

        setTitle("Pack Skin Images");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 795, 611);
        getContentPane().setLayout(new BorderLayout(0, 0));

        {
            jDesktopPane1 = new JDesktopPane();
            getContentPane().add(jDesktopPane1, BorderLayout.CENTER);

            {
                btnPackNow = new JButton();
                jDesktopPane1.add(btnPackNow, JLayeredPane.DEFAULT_LAYER);
                btnPackNow.setText("Pack Images");
                btnPackNow.setBounds(420, 10, 150, 28);
                btnPackNow.setEnabled(false);
                btnPackNow.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        if (btnPackNow.isEnabled())
                            PackNowClicked(evt);
                    }
                });
            }

            {
                jScrollPane1 = new JScrollPane();
                jDesktopPane1.add(jScrollPane1, JLayeredPane.DEFAULT_LAYER);
                jScrollPane1.setBounds(12, 224, 755, 343);
                {
                    jTextArea1 = new JTextArea();
                    jScrollPane1.setViewportView(jTextArea1);
                    jTextArea1.setText("");
                    jTextArea1.setBounds(451, 120, 55, 42);
                    jTextArea1.setEditable(false);
                }
            }

        }

        {
            DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(ICON_COMPUTER, null, "Computer"));

            DefaultMutableTreeNode node;
            File[] roots = File.listRoots();
            // String basedir = System.getProperty("user.dir");
            for (int k = 0; k < roots.length; k++) {
                node = new DefaultMutableTreeNode(new IconData(ICON_DISK, null, new FileNode(roots[k])));
                top.add(node);
                node.add(new DefaultMutableTreeNode(new Boolean(true)));
            }

            m_model = new DefaultTreeModel(top);

            m_tree = new JTree(m_model);

            TreeCellRenderer renderer = new IconCellRenderer();
            m_tree.setCellRenderer(renderer);

            m_tree.addTreeExpansionListener(new DirExpansionListener());

            m_tree.addTreeSelectionListener(new DirSelectionListener());

            m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            m_tree.setShowsRootHandles(true);
            m_tree.setEditable(false);

            m_tree.setSize(this.getWidth(), this.getHeight());

            JScrollPane s = new JScrollPane();
            s.getViewport().add(m_tree);

            s.setBounds(10, 10, 400, 200);

            jDesktopPane1.add(s, BorderLayout.CENTER);

        }

    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    launch frame = new launch();
                    frame.setVisible(true);
                    that = frame;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void PackNowClicked(MouseEvent evt) {
        run();
    }

    private void run() {

        Thread t = new Thread() {
            public void run() {
                CreateTextureAndCopy("/default");
                CreateTextureAndCopy("/small");
                JOptionPane.showMessageDialog(null, "Ready", "", JOptionPane.OK_OPTION);
            }

        };

        t.start();

    }

    private void CreateTextureAndCopy(String skinFolderName) {

        Settings textureSettings = new Settings();

        textureSettings.pot = true;
        textureSettings.paddingX = 2;
        textureSettings.paddingY = 2;
        textureSettings.duplicatePadding = true;
        textureSettings.edgePadding = true;
        textureSettings.rotation = false;
        if (skinFolderName.equals("/small")) {
            textureSettings.minWidth = 128;
            textureSettings.minHeight = 128;
            textureSettings.maxWidth = 1024;
            textureSettings.maxHeight = 1024;
        } else {
            textureSettings.minWidth = 128;
            textureSettings.minHeight = 128;
            textureSettings.maxWidth = 2048;
            textureSettings.maxHeight = 2048;
        }

        textureSettings.stripWhitespaceX = false;
        textureSettings.stripWhitespaceY = false;
        textureSettings.alphaThreshold = 0;
        textureSettings.filterMin = TextureFilter.Linear;
        textureSettings.filterMag = TextureFilter.Linear;
        textureSettings.wrapX = TextureWrap.ClampToEdge;
        textureSettings.wrapY = TextureWrap.ClampToEdge;
        textureSettings.format = Format.RGBA8888;
        textureSettings.alias = true;
        textureSettings.outputFormat = "png";
        textureSettings.jpegQuality = 0.9f;
        textureSettings.ignoreBlankImages = true;
        textureSettings.fast = false;
        textureSettings.debug = false;

        String inputFolder;
        String outputFolder;
        String Name;
        ArrayList<String> outPutFolders = new ArrayList<String>();

        // Pack Day
        inputFolder = selectedPath + "/Icons" + skinFolderName + "/day/UI_IconPack";
        outputFolder = selectedPath + "/skins" + skinFolderName + "/day";
        outPutFolders.add(outputFolder);
        Name = "UI_IconPack.spp.atlas";
        try {
            TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
        } catch (Exception e1) {
            writeMsg(BR + BR);
            writeMsg("#######################################################");
            writeMsg(e1.getCause().getMessage());
            try {
                writeMsg(e1.getCause().getCause().getMessage());
            } catch (Exception e) {
            }
            writeMsg("#######################################################");
            writeMsg(BR + BR);
        }

        // Pack Night
        inputFolder = selectedPath + "/Icons" + skinFolderName + "/night/UI_IconPack";
        outputFolder = selectedPath + "/skins" + skinFolderName + "/night";
        outPutFolders.add(outputFolder);
        Name = "UI_IconPack.spp.atlas";
        try {
            TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
        } catch (Exception e1) {
            writeMsg(BR + BR);
            writeMsg("#######################################################");
            writeMsg(e1.getCause().getMessage());
            try {
                writeMsg(e1.getCause().getCause().getMessage());
            } catch (Exception e) {
            }
            writeMsg("#######################################################");
            writeMsg(BR + BR);
        }

        // Pack Splash
        if (skinFolderName.equals("/default")) {
            inputFolder = selectedPath + "/Icons" + skinFolderName + "/splash";
            outputFolder = selectedPath + "/skins" + skinFolderName + "/day";
            outPutFolders.add(outputFolder);
            Name = "SplashPack.spp.atlas";
            try {
                TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
            } catch (Exception e1) {
                writeMsg(BR + BR);
                writeMsg("#######################################################");
                writeMsg(e1.getCause().getMessage());
                try {
                    writeMsg(e1.getCause().getCause().getMessage());
                } catch (Exception e) {
                }
                writeMsg("#######################################################");
                writeMsg(BR + BR);
            }
        }

        writeMsg("Copy Textures");
        writeMsg("Copy: ");

        // Change TexturFilter at *.spp.atlas files with in all output folder

        for (String folder : outPutFolders) {
            File dir = new File(folder);
            File[] files = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".spp.atlas")) {
                        if (pathname.getName().endsWith("_MipMap.spp.atlas"))
                            return false;
                        return true;
                    }
                    return false;
                }
            });

            for (File tmp : files) {
                // now open and change Line
                // "filter: Linear,Linear"
                // to
                // "filter: MipMapLinearNearest,Nearest"

                BufferedReader in;
                try {
                    in = new BufferedReader(new FileReader(tmp));

                    String line; // a line in the file

                    StringBuilder builder = new StringBuilder();

                    while ((line = in.readLine()) != null) {
                        if (line.contains("filter:")) {
                            builder.append("filter: MipMapLinearNearest,Nearest" + BR);
                        } else if (line.contains("format:")) {
                            builder.append("format: RGBA8888" + BR);
                        } else {
                            builder.append(line + BR);
                        }
                    }

                    in.close();

                    FileWriter writer;

                    String newName = tmp.getAbsolutePath().replace(".spp.atlas", "_MipMap.spp.atlas");
                    File newPerformanceFile = new File(newName);

                    writer = new FileWriter(newPerformanceFile, false);
                    writer.write(builder.toString());
                    writer.close();

                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }

            }

        }

        writeMsg("Ready");
        writeMsg("");
        writeMsg("Don't forgot refresh and clean on Eclipse");

    }

    public void writeMsg(String msg) {
        writeMsg(msg, true);
    }

    public void writeMsg(String msg, boolean linebreak) {
        String alt = jTextArea1.getText();
        if (msg.contains("Packing")) {

            char cr = ("\n").charAt(0);
            String lastLine = "";
            for (int i = alt.length() - 2; i >= 0; i--) {
                if (alt.charAt(i) == cr) {
                    break;
                }
                lastLine += alt.charAt(i);
            }
            String finalLastLine = "";
            for (int i = lastLine.length() - 1; i >= 0; i--) {
                finalLastLine += lastLine.charAt(i);
            }

            if (finalLastLine.contains("Packing")) {
                int pos = alt.lastIndexOf(finalLastLine);
                alt = alt.substring(0, pos);
            }

        }

        String br = linebreak ? String.format("%n") : "";

        alt += msg + br;
        jTextArea1.setText(alt);

        int x;
        jTextArea1.selectAll();
        x = jTextArea1.getSelectionEnd();
        jTextArea1.select(x, x);
        jTextArea1.invalidate();

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }

    }

    DefaultMutableTreeNode getTreeNode(TreePath path) {
        return (DefaultMutableTreeNode) (path.getLastPathComponent());
    }

    FileNode getFileNode(DefaultMutableTreeNode node) {
        if (node == null)
            return null;
        Object obj = node.getUserObject();
        if (obj instanceof IconData)
            obj = ((IconData) obj).getObject();
        if (obj instanceof FileNode)
            return (FileNode) obj;
        else
            return null;
    }

    // Make sure expansion is threaded and updating the tree model
    // only occurs within the event dispatching thread.
    class DirExpansionListener implements TreeExpansionListener {
        public void treeExpanded(TreeExpansionEvent event) {
            final DefaultMutableTreeNode node = getTreeNode(event.getPath());
            final FileNode fnode = getFileNode(node);

            Thread runner = new Thread() {
                public void run() {
                    if (fnode != null && fnode.expand(node)) {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                m_model.reload(node);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                }
            };
            runner.start();
        }

        public void treeCollapsed(TreeExpansionEvent event) {
        }
    }

    class DirSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent event) {
            DefaultMutableTreeNode node = getTreeNode(event.getPath());
            FileNode fnode = getFileNode(node);
            if (fnode != null) {
                selectedPath = fnode.getFile().getAbsolutePath();
                // chk if path a Libgdx Pack Path
                // has folder Input and output

                File folder = new File(selectedPath);
                if (folder.isDirectory()) {
                    File defaultFolder = new File(selectedPath + "/Icons/default");
                    File smallFolder = new File(selectedPath + "/Icons/small");
                    if (defaultFolder.isDirectory() && smallFolder.isDirectory()) {
                        btnPackNow.setEnabled(true);
                    } else {
                        btnPackNow.setEnabled(false);
                    }
                } else {
                    btnPackNow.setEnabled(false);
                }
            } else {
                btnPackNow.setEnabled(false);
                selectedPath = "";
            }
            if (btnPackNow.isEnabled()) {
                writeMsg("Select: " + selectedPath);
                try {
                    File folder = new File(selectedPath + "/skins");
                    if (folder.exists()) {
                        deleteDir(folder);
                    }
                } catch (Exception e) {
                    writeMsg("Could not delete Output Folder!");
                }
            }
        }
    }

    void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

}

class IconCellRenderer extends JLabel implements TreeCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected Color m_textSelectionColor;
    protected Color m_textNonSelectionColor;
    protected Color m_bkSelectionColor;
    protected Color m_bkNonSelectionColor;
    protected Color m_borderSelectionColor;

    protected boolean m_selected;

    public IconCellRenderer() {
        super();
        m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
        m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
        m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
        m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
        m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
        setOpaque(false);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)

    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();
        setText(obj.toString());

        if (obj instanceof Boolean)
            setText("Retrieving data...");

        if (obj instanceof IconData) {
            IconData idata = (IconData) obj;
            if (expanded)
                setIcon(idata.getExpandedIcon());
            else
                setIcon(idata.getIcon());
        } else
            setIcon(null);

        setFont(tree.getFont());
        setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
        setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
        m_selected = sel;
        return this;
    }

    public void paintComponent(Graphics g) {
        Color bColor = getBackground();
        Icon icon = getIcon();

        g.setColor(bColor);
        int offset = 0;
        if (icon != null && getText() != null)
            offset = (icon.getIconWidth() + getIconTextGap());
        g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);

        if (m_selected) {
            g.setColor(m_borderSelectionColor);
            g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
        }
        super.paintComponent(g);
    }
}

class IconData {
    protected Icon m_icon;
    protected Icon m_expandedIcon;
    protected Object m_data;

    public IconData(Icon icon, Object data) {
        m_icon = icon;
        m_expandedIcon = null;
        m_data = data;
    }

    public IconData(Icon icon, Icon expandedIcon, Object data) {
        m_icon = icon;
        m_expandedIcon = expandedIcon;
        m_data = data;
    }

    public Icon getIcon() {
        return m_icon;
    }

    public Icon getExpandedIcon() {
        return m_expandedIcon != null ? m_expandedIcon : m_icon;
    }

    public Object getObject() {
        return m_data;
    }

    public String toString() {
        return m_data.toString();
    }
}

class FileNode {
    protected File m_file;

    public FileNode(File file) {
        m_file = file;
    }

    public File getFile() {
        return m_file;
    }

    public String toString() {
        return m_file.getName().length() > 0 ? m_file.getName() : m_file.getPath();
    }

    public boolean expand(DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent.getFirstChild();
        if (flag == null) // No flag
            return false;
        Object obj = flag.getUserObject();
        if (!(obj instanceof Boolean))
            return false; // Already expanded

        parent.removeAllChildren(); // Remove Flag

        File[] files = listFiles();
        if (files == null)
            return true;

        Vector v = new Vector();

        for (int k = 0; k < files.length; k++) {
            File f = files[k];
            if (!(f.isDirectory()))
                continue;

            FileNode newNode = new FileNode(f);

            boolean isAdded = false;
            for (int i = 0; i < v.size(); i++) {
                FileNode nd = (FileNode) v.elementAt(i);
                if (newNode.compareTo(nd) < 0) {
                    v.insertElementAt(newNode, i);
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded)
                v.addElement(newNode);
        }

        for (int i = 0; i < v.size(); i++) {
            FileNode nd = (FileNode) v.elementAt(i);
            IconData idata = new IconData(launch.ICON_FOLDER, launch.ICON_EXPANDEDFOLDER, nd);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
            parent.add(node);

            if (nd.hasSubDirs())
                node.add(new DefaultMutableTreeNode(new Boolean(true)));
        }

        return true;
    }

    public boolean hasSubDirs() {
        File[] files = listFiles();
        if (files == null)
            return false;
        for (int k = 0; k < files.length; k++) {
            if (files[k].isDirectory())
                return true;
        }
        return false;
    }

    public int compareTo(FileNode toCompare) {
        return m_file.getName().compareToIgnoreCase(toCompare.m_file.getName());
    }

    protected File[] listFiles() {
        if (!m_file.isDirectory())
            return null;
        try {
            return m_file.listFiles();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error reading directory " + m_file.getAbsolutePath(), "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

}
