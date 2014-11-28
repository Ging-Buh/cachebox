package CB.SkinPacker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class launch extends JFrame
{

	{
		// Set Look & Feel
		try
		{
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JDesktopPane jDesktopPane1;
	private JButton btnPackNow;
	private String selectedPath = "";

	public static final ImageIcon ICON_COMPUTER = new ImageIcon("computer.gif");
	public static final ImageIcon ICON_DISK = new ImageIcon("disk.gif");
	public static final ImageIcon ICON_FOLDER = new ImageIcon("folder.gif");
	public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon("expandedfolder.gif");

	protected JTree m_tree;
	protected DefaultTreeModel m_model;
	protected JTextField m_display;

	public static launch that;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					launch frame = new launch();
					frame.setVisible(true);
					that = frame;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public launch()
	{

		TeePrintStream tee = new TeePrintStream(System.out);
		System.setOut(tee);

		setTitle("Pack Skin Images");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 795, 611);
		getContentPane().setLayout(new BorderLayout(0, 0));

		{
			jDesktopPane1 = new JDesktopPane();
			getContentPane().add(jDesktopPane1, BorderLayout.CENTER);

			// {
			// jLabel1 = new JLabel();
			// jDesktopPane1.add(jLabel1, JLayeredPane.DEFAULT_LAYER);
			// jLabel1.setText("Selected \"LibgdxPacker\" path");
			// jLabel1.setBounds(12, 80, 170, 18);
			// jLabel1.setForeground(new java.awt.Color(255, 255, 255));
			// }

			// {
			// jLabel2 = new JLabel();
			// jDesktopPane1.add(jLabel2, JLayeredPane.DEFAULT_LAYER);
			// jLabel2.setText("new Package Name :");
			// jLabel2.setBounds(451, 82, 132, 16);
			// jLabel2.setForeground(new java.awt.Color(255, 255, 255));
			// }

			{
				btnPackNow = new JButton();
				jDesktopPane1.add(btnPackNow, JLayeredPane.DEFAULT_LAYER);
				btnPackNow.setText("Pack Images");
				btnPackNow.setBounds(420, 10, 150, 28);
				btnPackNow.setEnabled(false);
				btnPackNow.addMouseListener(new MouseAdapter()
				{
					public void mouseClicked(MouseEvent evt)
					{
						if (btnPackNow.isEnabled()) PackNowClicked(evt);
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
			for (int k = 0; k < roots.length; k++)
			{
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

	final String directoryPath = "Android_GUI/";

	private JScrollPane jScrollPane1;
	private JTextArea jTextArea1;

	private void PackNowClicked(MouseEvent evt)
	{
		run();
	}

	private void run()
	{

		Thread t = new Thread()
		{
			public void run()
			{

				CreateTextureAndCopy();
				JOptionPane.showMessageDialog(null, "Ready", "", JOptionPane.OK_OPTION);

			}

		};

		t.start();

	}

	private void CreateTextureAndCopy()
	{

		Settings textureSettings = new Settings();

		textureSettings.pot = true;
		textureSettings.paddingX = 2;
		textureSettings.paddingY = 2;
		textureSettings.duplicatePadding = true;
		textureSettings.edgePadding = true;
		textureSettings.rotation = false;
		if (true)
		{
			textureSettings.minWidth = 128;
			textureSettings.minHeight = 128;
			textureSettings.maxWidth = 1024;
			textureSettings.maxHeight = 1024;
		}
		else
		{
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

		// Pack Default day
		String inputFolder = selectedPath + "\\input\\day\\UI_IconPack";
		String outputFolder = selectedPath + "\\Output\\day";
		String Name = "UI_IconPack.spp";
		ArrayList<String> outPutFolders = new ArrayList<String>();

		try
		{
			TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
		}
		catch (Exception e1)
		{
			writeMsg(e1.getCause().getMessage());
			try
			{
				writeMsg(e1.getCause().getCause().getMessage());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Pack Default night
		inputFolder = selectedPath + "\\input\\night\\UI_IconPack";
		outputFolder = selectedPath + "\\Output\\night";
		outPutFolders.add(outputFolder);
		Name = "UI_IconPack.spp";
		try
		{
			TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
		}
		catch (Exception e1)
		{
			writeMsg(e1.getCause().getMessage());
			try
			{
				writeMsg(e1.getCause().getCause().getMessage());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Pack small day
		inputFolder = selectedPath + "\\input\\day\\UI_IconPack";
		outputFolder = selectedPath + "\\Output\\day";
		outPutFolders.add(outputFolder);
		Name = "UI_IconPack.spp";
		try
		{
			TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
		}
		catch (Exception e1)
		{
			writeMsg(e1.getCause().getMessage());
			try
			{
				writeMsg(e1.getCause().getCause().getMessage());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Pack small night
		inputFolder = selectedPath + "\\input\\night\\UI_IconPack";
		outputFolder = selectedPath + "\\Output\\night";
		outPutFolders.add(outputFolder);
		Name = "UI_IconPack.spp";
		try
		{
			TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
		}
		catch (Exception e1)
		{
			writeMsg(e1.getCause().getMessage());
			try
			{
				writeMsg(e1.getCause().getCause().getMessage());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Pack Default day
		inputFolder = selectedPath + "\\input\\splash";
		outputFolder = selectedPath + "\\Output\\day";
		outPutFolders.add(outputFolder);
		Name = "SplashPack.spp";
		try
		{
			TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
		}
		catch (Exception e1)
		{
			writeMsg(e1.getCause().getMessage());
			try
			{
				writeMsg(e1.getCause().getCause().getMessage());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		writeMsg("Copy Textures");
		writeMsg("Copy: ");

		// Change TexturFilter at *.spp files with in all output folder
		String br = System.getProperty("line.separator");
		for (String folder : outPutFolders)
		{
			File dir = new File(folder);
			File[] files = dir.listFiles(new FileFilter()
			{

				@Override
				public boolean accept(File pathname)
				{
					if (pathname.getName().endsWith(".spp"))
					{
						if (pathname.getName().endsWith("_MipMap.spp")) return false;
						return true;
					}
					return false;
				}
			});

			for (File tmp : files)
			{
				// now open and change Line
				// "filter: Linear,Linear"
				// to
				// "filter: MipMapLinearNearest,Nearest"

				BufferedReader in;
				try
				{
					in = new BufferedReader(new FileReader(tmp));

					String line; // a line in the file

					StringBuilder builder = new StringBuilder();

					while ((line = in.readLine()) != null)
					{
						if (line.contains("filter:"))
						{
							builder.append("filter: MipMapLinearNearest,Nearest" + br);
						}
						else if (line.contains("format:"))
						{
							builder.append("format: RGBA4444" + br);
						}
						else
						{
							builder.append(line + br);
						}
					}

					in.close();

					FileWriter writer;

					String newName = tmp.getAbsolutePath().replace(".spp", "_MipMap.spp");
					File newPerformanceFile = new File(newName);

					writer = new FileWriter(newPerformanceFile, false);
					writer.write(builder.toString());
					writer.close();

				}
				catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		// Copy copy = new Copy(getCopyRulesTexture());
		// try
		// {
		// copy.Run(new CopyMsg()
		// {
		// @Override
		// public void Msg(String msg)
		// {
		// writeMsg(msg);
		// }
		// });
		// }
		// catch (IOException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		writeMsg("Ready");
		writeMsg("");
		writeMsg("Don´t forgot refresh and clean on Eclipse");

	}

	// private ArrayList<CopyRule> getCopyRulesTexture()
	// {
	// // Path to extracted LibGdx.zip
	// String cs = selectedPath;
	//
	// ArrayList<CopyRule> rules = new ArrayList<CopyRule>();
	//
	// // for Android Proj
	// rules.add(new CopyRule(cs + " \\Output\\day", workPath + "\\Android_GUI\\assets\\skins\\default"));
	// rules.add(new CopyRule(cs + " \\Output\\day", workPath + "\\Android_GUI\\assets\\skins\\small"));
	// rules.add(new CopyRule(cs + " \\Output\\night", workPath + "\\Android_GUI\\assets\\skins\\default"));
	// rules.add(new CopyRule(cs + " \\Output\\night", workPath + "\\Android_GUI\\assets\\skins\\small"));
	//
	// return rules;
	// }

	public void writeMsg(String msg)
	{
		writeMsg(msg, true);
	}

	public void writeMsg(String msg, boolean linebreak)
	{
		String alt = jTextArea1.getText();
		if (msg.contains("Packing"))
		{

			char cr = ("\n").charAt(0);
			String lastLine = "";
			for (int i = alt.length() - 2; i >= 0; i--)
			{
				if (alt.charAt(i) == cr)
				{
					break;
				}
				lastLine += alt.charAt(i);
			}
			String finalLastLine = "";
			for (int i = lastLine.length() - 1; i >= 0; i--)
			{
				finalLastLine += lastLine.charAt(i);
			}

			if (finalLastLine.contains("Packing"))
			{
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

		try
		{
			Thread.sleep(20);
		}
		catch (InterruptedException e)
		{

			e.printStackTrace();
		}

	}

	DefaultMutableTreeNode getTreeNode(TreePath path)
	{
		return (DefaultMutableTreeNode) (path.getLastPathComponent());
	}

	FileNode getFileNode(DefaultMutableTreeNode node)
	{
		if (node == null) return null;
		Object obj = node.getUserObject();
		if (obj instanceof IconData) obj = ((IconData) obj).getObject();
		if (obj instanceof FileNode) return (FileNode) obj;
		else
			return null;
	}

	// Make sure expansion is threaded and updating the tree model
	// only occurs within the event dispatching thread.
	class DirExpansionListener implements TreeExpansionListener
	{
		public void treeExpanded(TreeExpansionEvent event)
		{
			final DefaultMutableTreeNode node = getTreeNode(event.getPath());
			final FileNode fnode = getFileNode(node);

			Thread runner = new Thread()
			{
				public void run()
				{
					if (fnode != null && fnode.expand(node))
					{
						Runnable runnable = new Runnable()
						{
							public void run()
							{
								m_model.reload(node);
							}
						};
						SwingUtilities.invokeLater(runnable);
					}
				}
			};
			runner.start();
		}

		public void treeCollapsed(TreeExpansionEvent event)
		{
		}
	}

	class DirSelectionListener implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent event)
		{
			DefaultMutableTreeNode node = getTreeNode(event.getPath());
			FileNode fnode = getFileNode(node);
			if (fnode != null)
			{
				selectedPath = fnode.getFile().getAbsolutePath();
				// chk if path a Libgdx Pack Path
				// has folder Input and output

				File folder = new File(selectedPath);
				if (folder.isDirectory())
				{
					File input = new File(selectedPath + "//input");
					File output = new File(selectedPath + "//output");
					if (input.isDirectory() && output.isDirectory())
					{
						btnPackNow.setEnabled(true);
					}
					else
					{
						btnPackNow.setEnabled(false);
					}
				}
				else
				{
					btnPackNow.setEnabled(false);
				}
			}
			else
			{
				btnPackNow.setEnabled(false);
				selectedPath = "";
			}

			writeMsg("Select: " + selectedPath);
		}
	}

}

class IconCellRenderer extends JLabel implements TreeCellRenderer
{
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

	public IconCellRenderer()
	{
		super();
		m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
		m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
		m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
		m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
		m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
		setOpaque(false);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus)

	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();
		setText(obj.toString());

		if (obj instanceof Boolean) setText("Retrieving data...");

		if (obj instanceof IconData)
		{
			IconData idata = (IconData) obj;
			if (expanded) setIcon(idata.getExpandedIcon());
			else
				setIcon(idata.getIcon());
		}
		else
			setIcon(null);

		setFont(tree.getFont());
		setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
		setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
		m_selected = sel;
		return this;
	}

	public void paintComponent(Graphics g)
	{
		Color bColor = getBackground();
		Icon icon = getIcon();

		g.setColor(bColor);
		int offset = 0;
		if (icon != null && getText() != null) offset = (icon.getIconWidth() + getIconTextGap());
		g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);

		if (m_selected)
		{
			g.setColor(m_borderSelectionColor);
			g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
		}
		super.paintComponent(g);
	}
}

class IconData
{
	protected Icon m_icon;
	protected Icon m_expandedIcon;
	protected Object m_data;

	public IconData(Icon icon, Object data)
	{
		m_icon = icon;
		m_expandedIcon = null;
		m_data = data;
	}

	public IconData(Icon icon, Icon expandedIcon, Object data)
	{
		m_icon = icon;
		m_expandedIcon = expandedIcon;
		m_data = data;
	}

	public Icon getIcon()
	{
		return m_icon;
	}

	public Icon getExpandedIcon()
	{
		return m_expandedIcon != null ? m_expandedIcon : m_icon;
	}

	public Object getObject()
	{
		return m_data;
	}

	public String toString()
	{
		return m_data.toString();
	}
}

class FileNode
{
	protected File m_file;

	public FileNode(File file)
	{
		m_file = file;
	}

	public File getFile()
	{
		return m_file;
	}

	public String toString()
	{
		return m_file.getName().length() > 0 ? m_file.getName() : m_file.getPath();
	}

	public boolean expand(DefaultMutableTreeNode parent)
	{
		DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent.getFirstChild();
		if (flag == null) // No flag
		return false;
		Object obj = flag.getUserObject();
		if (!(obj instanceof Boolean)) return false; // Already expanded

		parent.removeAllChildren(); // Remove Flag

		File[] files = listFiles();
		if (files == null) return true;

		Vector v = new Vector();

		for (int k = 0; k < files.length; k++)
		{
			File f = files[k];
			if (!(f.isDirectory())) continue;

			FileNode newNode = new FileNode(f);

			boolean isAdded = false;
			for (int i = 0; i < v.size(); i++)
			{
				FileNode nd = (FileNode) v.elementAt(i);
				if (newNode.compareTo(nd) < 0)
				{
					v.insertElementAt(newNode, i);
					isAdded = true;
					break;
				}
			}
			if (!isAdded) v.addElement(newNode);
		}

		for (int i = 0; i < v.size(); i++)
		{
			FileNode nd = (FileNode) v.elementAt(i);
			IconData idata = new IconData(launch.ICON_FOLDER, launch.ICON_EXPANDEDFOLDER, nd);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
			parent.add(node);

			if (nd.hasSubDirs()) node.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		return true;
	}

	public boolean hasSubDirs()
	{
		File[] files = listFiles();
		if (files == null) return false;
		for (int k = 0; k < files.length; k++)
		{
			if (files[k].isDirectory()) return true;
		}
		return false;
	}

	public int compareTo(FileNode toCompare)
	{
		return m_file.getName().compareToIgnoreCase(toCompare.m_file.getName());
	}

	protected File[] listFiles()
	{
		if (!m_file.isDirectory()) return null;
		try
		{
			return m_file.listFiles();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, "Error reading directory " + m_file.getAbsolutePath(), "Warning",
					JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}

}
