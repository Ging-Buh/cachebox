import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import CB_Core.Import.Importer;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial use. If Jigloo is
 * being used commercially (ie, by a corporation, company or business for any purpose whatever) then you should purchase a license for each
 * developer using Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of these licensing terms. A
 * COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL
 * PURPOSE.
 */
public class main extends JFrame
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
	private JTextPane jTextPane1;
	private JButton jButton3;
	private JTextField jTextFieldVers;
	private JLabel jLabel3;
	private JTextField jTextFieldPackageName;
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
					main frame = new main();
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
	public main()
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
				jButton1.setBounds(194, 9, 108, 23);
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
				jLabel1.setBounds(12, 12, 170, 16);
				jLabel1.setForeground(new java.awt.Color(255, 255, 255));
			}
			{
				jButton2 = new JButton();
				jDesktopPane1.add(jButton2, JLayeredPane.DEFAULT_LAYER);
				jButton2.setText("to Release");
				jButton2.setBounds(313, 9, 108, 23);
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
				jLabel2.setBounds(451, 12, 132, 16);
				jLabel2.setForeground(new java.awt.Color(255, 255, 255));
			}
			{
				jTextFieldPackageName = new JTextField();
				jDesktopPane1.add(jTextFieldPackageName, JLayeredPane.DEFAULT_LAYER);
				jTextFieldPackageName.setText("de.droidcachebox");
				jTextFieldPackageName.setBounds(595, 9, 172, 23);
			}
			{
				jLabel3 = new JLabel();
				jDesktopPane1.add(jLabel3, JLayeredPane.DEFAULT_LAYER);
				jLabel3.setText("Versions Number:");
				jLabel3.setBounds(12, 56, 111, 16);
				jLabel3.setForeground(new java.awt.Color(255, 255, 255));
			}
			{
				jTextFieldVers = new JTextField();
				jDesktopPane1.add(jTextFieldVers, JLayeredPane.DEFAULT_LAYER);
				jTextFieldVers.setText("0.4.xxx");
				jTextFieldVers.setBounds(123, 56, 62, 23);
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
				jTextPane1 = new JTextPane();
				jDesktopPane1.add(jTextPane1, JLayeredPane.DEFAULT_LAYER);
				jTextPane1.setBounds(6, 230, 761, 337);
				jTextPane1.setEditable(false);
				jTextPane1.setAutoscrolls(true);
			}
		}

	}

	private void jButton1MouseClicked(MouseEvent evt)
	{
		jTextFieldPackageName.setText("de.cachebox_test");
	}

	private void jButton2MouseClicked(MouseEvent evt)
	{
		jTextFieldPackageName.setText("de.droidcachebox");
	}

	private void jButton3MouseClicked(MouseEvent evt)
	{
		String directoryPath = "Android_GUI/";

		ArrayList<File> files = new ArrayList<File>();
		files = Importer.recursiveDirectoryReader(new File(directoryPath), files, "xml");

		String Text = "";

		for (Iterator<File> it = files.iterator(); it.hasNext();)
		{

			Text += it.next().getAbsolutePath() + String.format("%n");
			jTextPane1.setText(Text);
		}

	}
}
