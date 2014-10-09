package CB.Release_Rename;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import CB_Utils.Util.FileIO;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.XMLParserException;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial use. If Jigloo is
 * being used commercially (ie, by a corporation, company or business for any purpose whatever) then you should purchase a license for each
 * developer using Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of these licensing terms. A
 * COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL
 * PURPOSE.
 */
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
	private JButton jButton1;
	private JLabel jLabel5;
	private JRadioButton jRadioButton2;
	private JRadioButton jRadioButton1;
	private JRadioButton jRadioButtonRelease;
	private JLabel jLabel8;
	private JLabel jLabel7;
	private JTextField jTextField1;
	private JLabel jLabel6;
	private JLabel jLabel4;
	private JButton jButton3;
	private JTextField jTextFieldVers;
	private JLabel jLabel3;
	private JTextField jTextFieldPackageName;
	private JPanel jPanel1;
	private JLabel jLabel2;
	private JButton jButton2;
	private JLabel jLabel1;

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
		setTitle("Rename Cachebox Workspce for Release");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 795, 611);
		getContentPane().setLayout(new BorderLayout(0, 0));

		{
			jDesktopPane1 = new JDesktopPane();
			getContentPane().add(jDesktopPane1, BorderLayout.CENTER);
			{
				jButton1 = new JButton();
				jDesktopPane1.add(jButton1, JLayeredPane.DEFAULT_LAYER);
				jButton1.setText("to Test");
				jButton1.setBounds(162, 78, 89, 23);
				jButton1.addMouseListener(new MouseAdapter()
				{
					public void mouseClicked(MouseEvent evt)
					{
						jButton1MouseClicked(evt);
					}
				});
			}
			{
				jLabel1 = new JLabel();
				jDesktopPane1.add(jLabel1, JLayeredPane.DEFAULT_LAYER);
				jLabel1.setText("Change Package Name");
				jLabel1.setBounds(12, 80, 170, 18);
				jLabel1.setForeground(new java.awt.Color(255, 255, 255));
			}
			{
				jButton2 = new JButton();
				jDesktopPane1.add(jButton2, JLayeredPane.DEFAULT_LAYER);
				jButton2.setText("to Release");
				jButton2.setBounds(342, 79, 88, 23);
				jButton2.addMouseListener(new MouseAdapter()
				{
					public void mouseClicked(MouseEvent evt)
					{
						jButton2MouseClicked(evt);
					}
				});
			}
			{
				jLabel2 = new JLabel();
				jDesktopPane1.add(jLabel2, JLayeredPane.DEFAULT_LAYER);
				jLabel2.setText("new Package Name :");
				jLabel2.setBounds(451, 82, 132, 16);
				jLabel2.setForeground(new java.awt.Color(255, 255, 255));
			}
			{
				jTextFieldPackageName = new JTextField();
				jDesktopPane1.add(jTextFieldPackageName, JLayeredPane.DEFAULT_LAYER);
				jTextFieldPackageName.setText("de.droidcachebox");
				jTextFieldPackageName.setBounds(595, 76, 172, 26);
			}
			{
				jLabel3 = new JLabel();
				jDesktopPane1.add(jLabel3, JLayeredPane.DEFAULT_LAYER);
				jLabel3.setText("Versions Number:");
				jLabel3.setBounds(12, 126, 111, 16);
				jLabel3.setForeground(new java.awt.Color(255, 255, 255));
			}
			{
				jTextFieldVers = new JTextField();
				jDesktopPane1.add(jTextFieldVers, JLayeredPane.DEFAULT_LAYER);
				jTextFieldVers.setText(getVersionFromGlobalCore());
				jTextFieldVers.setBounds(123, 121, 80, 28);
			}
			{
				jButton3 = new JButton();
				jDesktopPane1.add(jButton3, JLayeredPane.DEFAULT_LAYER);
				jButton3.setText("Run");
				jButton3.setBounds(18, 190, 51, 28);
				jButton3.addMouseListener(new MouseAdapter()
				{
					public void mouseClicked(MouseEvent evt)
					{
						jButton3MouseClicked(evt);
					}
				});
			}
			{
				jLabel4 = new JLabel();
				jDesktopPane1.add(jLabel4, JLayeredPane.DEFAULT_LAYER);
				jLabel4.setText("Current Package Name");
				jLabel4.setForeground(new java.awt.Color(255, 255, 255));
				jLabel4.setBounds(18, 26, 170, 18);
			}
			{
				jLabel5 = new JLabel();
				jDesktopPane1.add(jLabel5, JLayeredPane.DEFAULT_LAYER);
				jLabel5.setText("Current Name");
				jLabel5.setForeground(new java.awt.Color(255, 255, 255));
				jLabel5.setBounds(165, 21, 423, 23);
				jLabel5.setFont(new java.awt.Font("SansSerif", 0, 16));
			}
			{
				jLabel6 = new JLabel();
				jDesktopPane1.add(jLabel6, JLayeredPane.DEFAULT_LAYER);
				jLabel6.setText("Version");
				jLabel6.setFont(new java.awt.Font("SansSerif", 0, 16));
				jLabel6.setForeground(new java.awt.Color(255, 255, 255));
				jLabel6.setBounds(606, 21, 155, 23);
			}
			{
				jTextField1 = new JTextField();
				jDesktopPane1.add(jTextField1, JLayeredPane.DEFAULT_LAYER);
				jTextField1.setText(getVersionPrefixFromGlobalCore());
				jTextField1.setBounds(123, 156, 80, 28);
			}
			{
				jLabel7 = new JLabel();
				jDesktopPane1.add(jLabel7, JLayeredPane.DEFAULT_LAYER);
				jLabel7.setText("Versions Präfix:");
				jLabel7.setForeground(new java.awt.Color(255, 255, 255));
				jLabel7.setBounds(12, 161, 111, 16);
			}
			{
				jPanel1 = new JPanel();
				jDesktopPane1.add(jPanel1, JLayeredPane.DEFAULT_LAYER);
				jPanel1.setBounds(265, 126, 156, 78);

				jPanel1.setLayout(null);
				{
					jRadioButtonRelease = new JRadioButton();
					jPanel1.add(jRadioButtonRelease);
					jRadioButtonRelease.setText("Release Icon");
					jRadioButtonRelease.setBounds(31, 29, 108, 20);
					jRadioButtonRelease.addMouseListener(new MouseAdapter()
					{
						public void mouseClicked(MouseEvent evt)
						{
							iconState = 0;
							setIconState();
						}
					});

				}
				{
					jRadioButton2 = new JRadioButton();
					jPanel1.add(jRadioButton2);
					jRadioButton2.setText("Donate Icon");
					jRadioButton2.setBounds(31, 6, 108, 24);
					jRadioButton2.addMouseListener(new MouseAdapter()
					{
						public void mouseClicked(MouseEvent evt)
						{
							iconState = 2;
							setIconState();
						}
					});

				}
				{
					jRadioButton1 = new JRadioButton();
					jPanel1.add(jRadioButton1);
					jRadioButton1.setText("Test Icon");
					jRadioButton1.setBounds(31, 52, 72, 18);
					jRadioButton1.addMouseListener(new MouseAdapter()
					{
						public void mouseClicked(MouseEvent evt)
						{
							iconState = 1;
							setIconState();
						}
					});
				}

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
			{
				jLabel8 = new JLabel();
				jDesktopPane1.add(jLabel8, JLayeredPane.DEFAULT_LAYER);
				jLabel8.setText("App Name:");
				jLabel8.setForeground(new java.awt.Color(255, 255, 255));
				jLabel8.setBounds(451, 136, 132, 16);
			}
			{
				jTextField2 = new JTextField();
				jDesktopPane1.add(jTextField2, JLayeredPane.DEFAULT_LAYER);
				jTextField2.setText("Cachebox");
				jTextField2.setBounds(595, 130, 172, 26);
			}
			{
				jButton4 = new JButton();
				jDesktopPane1.add(jButton4, JLayeredPane.DEFAULT_LAYER);
				jButton4.setText("to Donate");
				jButton4.setBounds(252, 79, 89, 23);
				jButton4.addMouseListener(new MouseAdapter()
				{
					public void mouseClicked(MouseEvent evt)
					{
						jButton4MouseClicked(evt);
					}
				});
			}

		}

		init();

	}

	final String directoryPath = "Android_GUI/";
	private JButton jButton4;
	private JTextField jTextField2;
	// final String directoryPath = "C:/@Work/Cachebox/JAVA WorkSpace/droidcachebox/trunk/Android_GUI/";
	private JScrollPane jScrollPane1;
	private JTextArea jTextArea1;
	private static String currentPackageName;
	// private static String currentVerNumber;
	// private static String currentPrafix;
	private static String currentIcon;
	private static String currentMainfestVersionString;

	private static String newPackageName;
	// private static String newVerNumber;
	// private static String newPrafix;
	private static String newIcon;
	private static String newMainfestVersionString;

	private void init()
	{

		iconState = 0;
		setIconState();

		// read current Mainfest
		Map<String, String> values = new HashMap<String, String>();

		System.setProperty("sjxp.namespaces", "false");

		List<IRule<Map<String, String>>> ruleList = new ArrayList<IRule<Map<String, String>>>();

		ruleList.add(new DefaultRule<Map<String, String>>(com.thebuzzmedia.sjxp.rule.IRule.Type.ATTRIBUTE, "/manifest", "package",
				"android:versionName")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("attribute_" + this.getAttributeNames()[index], value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(com.thebuzzmedia.sjxp.rule.IRule.Type.ATTRIBUTE, "/manifest/application",
				"android:icon")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("attribute_" + this.getAttributeNames()[index], value);
			}
		});

		@SuppressWarnings("unchecked")
		XMLParser<Map<String, String>> parserCache = new XMLParser<Map<String, String>>(
				ruleList.toArray(new com.thebuzzmedia.sjxp.rule.IRule[0]));
		boolean error = false;

		try
		{
			// parserCache.parse(new FileInputStream("Android_GUI/AndroidManifest.xml"), values);
			parserCache.parse(new FileInputStream(directoryPath + "AndroidManifest.xml"), values);
		}
		catch (IllegalArgumentException e)
		{

			e.printStackTrace();
		}
		catch (XMLParserException e)
		{

			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			jLabel5.setText("Mainfest not found.");
			jLabel5.setForeground(Color.RED);
			jButton3.setEnabled(false);
			error = true;
			e.printStackTrace();
		}

		currentMainfestVersionString = values.get("attribute_android:versionName");
		currentPackageName = values.get("attribute_package");
		currentIcon = values.get("attribute_android:icon");

		if (!error)
		{
			jLabel5.setText(currentPackageName);
			jLabel6.setText(currentMainfestVersionString);
		}

	}

	private void jButton1MouseClicked(MouseEvent evt)
	{
		jTextFieldPackageName.setText("de.cachebox_test");
		jTextField2.setText("CB_Test");
		iconState = 1;
		setIconState();
	}

	private void jButton2MouseClicked(MouseEvent evt)
	{
		jTextFieldPackageName.setText("de.droidcachebox");
		jTextField2.setText("Cachebox");
		iconState = 0;
		setIconState();
	}

	private void jButton4MouseClicked(MouseEvent evt)
	{
		jTextFieldPackageName.setText("de.cachebox_donate");
		jTextField2.setText("CB_Donate");
		iconState = 2;
		setIconState();
	}

	private void jButton3MouseClicked(MouseEvent evt)
	{
		run();
	}

	private int iconState = 0;

	private void setIconState()
	{
		switch (iconState)
		{
		case 0:
			jRadioButtonRelease.setSelected(true);
			jRadioButton1.setSelected(false);
			jRadioButton2.setSelected(false);
			break;
		case 1:
			jRadioButtonRelease.setSelected(false);
			jRadioButton1.setSelected(true);
			jRadioButton2.setSelected(false);
			break;
		case 2:
			jRadioButtonRelease.setSelected(false);
			jRadioButton1.setSelected(false);
			jRadioButton2.setSelected(true);
			break;
		}
	}

	private void run()
	{

		// set new values
		newPackageName = jTextFieldPackageName.getText();
		newMainfestVersionString = jTextFieldVers.getText() + " (" + jTextField1.getText() + ")";

		switch (iconState)
		{
		case 0:
			newIcon = "@drawable/cb";
			break;
		case 1:
			newIcon = "@drawable/cb_test";
			break;
		case 2:
			newIcon = "@drawable/cb_donate";
			break;
		}

		Thread t = new Thread()
		{
			public void run()
			{

				try
				{
					String newSourceFolder = "";

					// Replace Package Name in Files
					replaceInFiles("xml");
					replaceInFiles("java");

					// Rename Package Folder
					String[] packageFolder = currentPackageName.split("\\.");
					String[] newpackageFolder = newPackageName.split("\\.");
					for (int i = packageFolder.length; i > 0; i--)
					{
						String pathname = "";
						for (int j = 0; j < i; j++)
						{
							pathname += packageFolder[j] + "/";
						}

						String newPathname = "";
						for (int j = 0; j < i; j++)
						{
							newPathname += newpackageFolder[j] + "/";
						}

						File Folder = new File(directoryPath + "src/" + pathname);
						newSourceFolder = directoryPath + "src/" + newPathname;
						File newFolder = new File(newSourceFolder);

						addMsg("[Rename Folder:] " + pathname + " to " + newPathname);
						Folder.renameTo(newFolder);

						i = -1;

					}

					// Rename Values at Mainfest.xml
					try
					{

						File file = new File(directoryPath + "AndroidManifest.xml");
						BufferedReader reader = null;

						reader = new BufferedReader(new FileReader(file));

						String line = "", oldtext = "";

						while ((line = reader.readLine()) != null)
						{
							oldtext += line + "\r\n";
						}

						reader.close();

						String newtext = oldtext.replace(currentPackageName, newPackageName);
						newtext = newtext.replace(currentMainfestVersionString, newMainfestVersionString);
						newtext = newtext.replace(currentIcon, newIcon);

						addMsg("[Replace in:] " + file.getPath());
						FileWriter writer = null;

						writer = new FileWriter(file);

						writer.write(newtext);

						writer.close();

					}
					catch (IOException ioe)
					{
						ioe.printStackTrace();
					}

					// Rename Version Values at GlobalCore.java
					try
					{

						File file = new File(newSourceFolder + "../../../../CB_UI/src/CB_UI/GlobalCore.java");
						BufferedReader reader = null;

						reader = new BufferedReader(new FileReader(file));

						String line = "", oldtext = "";
						String altCurrentRevision = null;
						String altCurrentVersion = null;
						String altVersionPrefix = null;

						while ((line = reader.readLine()) != null)
						{
							oldtext += line + "\r\n";

							if (line.contains("public static final int CurrentRevision")) altCurrentRevision = line;
							if (line.contains("public static final String CurrentVersion")) altCurrentVersion = line;
							if (line.contains("public static final String VersionPrefix")) altVersionPrefix = line;

						}

						reader.close();

						String[] splitedVersion = jTextFieldVers.getText().split("\\.");

						String newCurrentRevision = splitedVersion[splitedVersion.length - 1];
						String newCurrentVersion = jTextFieldVers.getText().replace(newCurrentRevision, "");
						String newVersionPrefix = jTextField1.getText();
						String newtext = oldtext.replace(altCurrentRevision, "public static final int CurrentRevision = "
								+ newCurrentRevision + ";");
						newtext = newtext.replace(altCurrentVersion, "public static final String CurrentVersion = \"" + newCurrentVersion
								+ "\"" + ";");
						newtext = newtext.replace(altVersionPrefix, "public static final String VersionPrefix = \"" + newVersionPrefix
								+ "\"" + ";");

						addMsg("[Replace in:] " + file.getPath());
						FileWriter writer = null;

						writer = new FileWriter(file);

						writer.write(newtext);

						writer.close();

					}
					catch (IOException ioe)
					{
						ioe.printStackTrace();
					}

					// Rename AppName at strings.xml
					try
					{

						File file = new File(directoryPath + "/res/values/strings.xml");
						BufferedReader reader = null;

						reader = new BufferedReader(new FileReader(file));

						String line = "", oldtext = "";
						String altAppName = "";

						while ((line = reader.readLine()) != null)
						{
							oldtext += line + "\r\n";

							if (line.contains("<string name=\"app_name\">")) altAppName = line;

						}

						reader.close();

						String newtext = oldtext.replace(altAppName, "<string name=\"app_name\">" + jTextField2.getText() + "</string>");

						addMsg("[Replace in:] " + file.getPath());
						FileWriter writer = null;

						writer = new FileWriter(file);

						writer.write(newtext);

						writer.close();

					}
					catch (IOException ioe)
					{
						ioe.printStackTrace();
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JOptionPane.showMessageDialog(null, "Ready", "", JOptionPane.OK_OPTION);

			}

		};

		t.start();

	}

	private String getVersionFromGlobalCore()
	{
		File file = new File(directoryPath + "../CB_UI/src/CB_UI/GlobalCore.java");
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));

			String line = "";
			String altCurrentRevision = null;
			String altCurrentVersion = null;

			while ((line = reader.readLine()) != null)
			{

				if (line.contains("public static final int CurrentRevision")) altCurrentRevision = line;
				if (line.contains("public static final String CurrentVersion")) altCurrentVersion = line;

			}

			reader.close();

			int pos = altCurrentRevision.indexOf(";");
			String rev = altCurrentRevision.substring("public static final int CurrentRevision = ".length() + 1, pos);

			pos = altCurrentVersion.lastIndexOf("\"");
			String ver = altCurrentVersion.substring("public static final String CurrentVersion = \"".length() + 1, pos);

			return ver + rev;

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";

	}

	private String getVersionPrefixFromGlobalCore()
	{
		File file = new File(directoryPath + "../CB_UI/src/CB_UI/GlobalCore.java");
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));

			String line = "";
			String altVersionPrefix = null;

			while ((line = reader.readLine()) != null)
			{

				if (line.contains("public static final String VersionPrefix"))
				{
					altVersionPrefix = line;
					break;
				}

			}

			reader.close();

			int pos = altVersionPrefix.indexOf("\"") + 1;
			int pos2 = altVersionPrefix.lastIndexOf("\"");
			String pre = altVersionPrefix.substring(pos, pos2);

			return pre;

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";

	}

	private void replaceInFiles(String Ext)
	{
		ArrayList<File> files = new ArrayList<File>();
		files = FileIO.recursiveDirectoryReader(new File(directoryPath), files, Ext, true);

		for (Iterator<File> it = files.iterator(); it.hasNext();)
		{

			try
			{
				File file = it.next();
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = "", oldtext = "";
				while ((line = reader.readLine()) != null)
				{
					oldtext += line + "\r\n";
				}
				reader.close();
				// replace a word in a file

				if (oldtext.contains(currentPackageName))
				{
					String newtext = oldtext.replaceAll(currentPackageName, newPackageName);
					addMsg("[Replace in:] " + file.getPath());
					FileWriter writer = new FileWriter(file);
					writer.write(newtext);
					writer.close();
				}

			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}

		}
	}

	private void addMsg(String msg)
	{
		// String alt = jTextArea1.getText();
		// alt += msg + String.format("%n");

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

}
