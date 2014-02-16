package de.cachebox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;

import CB_UI_Base.Global;
import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_Utils.Util.Downloader;
import CB_Utils.Util.CopyHelper.Copy;
import CB_Utils.Util.CopyHelper.Copy.CopyMsg;
import CB_Utils.Util.CopyHelper.CopyRule;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class Core implements ApplicationListener
{

	private static Boolean DontDownloadIfExist = false;

	private Stage stage;

	private String Msg = "This is a app to refresh all used LibGdx libaries for Cachebox";
	float w, h, margin;
	private Label lblMsg, lblOut;

	private TextButton btnRunLibGdx, btnRunTranslations, btnRunTexturePacker;

	private String workPath = "/";
	private String imageWorkPath = "/";

	private int WorkPathFound = 0;
	private boolean ImageWorkPathFound = false;

	@Override
	public void create()
	{

		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();
		margin = 10;
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/arial.ttf"));
		BitmapFont font15 = generator.generateFont(15);
		generator.dispose();

		LabelStyle lblStyle = new LabelStyle(skin.get(LabelStyle.class));
		lblStyle.font = font15;
		lblStyle.fontColor = Color.BLACK;
		lblStyle.background = null;

		LabelStyle lblOutStyle = new LabelStyle(skin.get(LabelStyle.class));
		lblOutStyle.font = font15;
		lblOutStyle.fontColor = Color.BLACK;
		lblOutStyle.background = new ColorDrawable(Color.WHITE);

		TextButtonStyle btnStyle = skin.get(TextButtonStyle.class);
		btnStyle.font = font15;
		btnStyle.fontColor = Color.BLACK;

		lblMsg = new Label(Msg, lblStyle);
		stage.addActor(lblMsg);

		lblOut = new Label("", lblOutStyle);
		// lblOut = new Label("", lblStyle);
		lblOut.setText(" ");
		stage.addActor(lblOut);

		btnRunLibGdx = new TextButton("", btnStyle);
		stage.addActor(btnRunLibGdx);

		btnRunTranslations = new TextButton("download and copy latest translations", btnStyle);
		btnRunTranslations.addListener(runListnerTranslations);
		stage.addActor(btnRunTranslations);

		btnRunTexturePacker = new TextButton("Pack and copy Texture Images", btnStyle);
		btnRunTexturePacker.addListener(runListnerTexture);
		stage.addActor(btnRunTexturePacker);

		TeePrintStream tee = new TeePrintStream(System.out);
		System.setOut(tee);

		chkSource();

		layout();

	}

	private void chkSource()
	{
		File f = new File(workPath + "/Android_GUI");
		if (f.exists())
		{
			btnRunLibGdx.setText("download and copy latest LibGdx (nightly)");
			btnRunLibGdx.removeListener(selectTrunkListner);
			btnRunLibGdx.addListener(runListnerLibGdx);
			WorkPathFound = 1;
			writeMsg("Trunk folder found");
		}
		else
		{
			btnRunLibGdx.setText("select 'Trunk' folder");
			btnRunLibGdx.addListener(selectTrunkListner);
			WorkPathFound = 0;
			writeMsg("Trunk folder not found, please select");
		}

		File fi = new File(workPath + "/LibgdxPacker");
		if (fi.exists())
		{
			ImageWorkPathFound = true;
			imageWorkPath = workPath;
			WorkPathFound++;
		}
		else
			ImageWorkPathFound = false;

		layout();
	}

	private ArrayList<CopyRule> getCopyRulesLibGdx()
	{
		// Path to extracted LibGdx.zip
		String cs = workPath + "\\" + "libgdx-nightly-latest\\";

		ArrayList<CopyRule> rules = new ArrayList<CopyRule>();

		// for Android Proj
		rules.add(new CopyRule(cs + "gdx.jar", workPath + "\\Android_GUI\\libs"));
		rules.add(new CopyRule(cs + "gdx-backend-android.jar", workPath + "\\Android_GUI\\libs"));
		rules.add(new CopyRule(cs + "armeabi", workPath + "\\Android_GUI\\libs"));
		rules.add(new CopyRule(cs + "armeabi-v7a", workPath + "\\Android_GUI\\libs"));

		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\armeabi-v7a\\libgdx-freetype.so", workPath
				+ "\\Android_GUI\\libs\\armeabi-v7a"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\armeabi\\libgdx-freetype.so", workPath + "\\Android_GUI\\libs\\armeabi"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype.jar", workPath + "\\Android_GUI\\libs"));

		// ############################################################################################################################

		// for Core Proj
		rules.add(new CopyRule(cs + "gdx.jar", workPath + "\\Cachebox Core\\lib"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype.jar", workPath + "\\Cachebox Core\\lib"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype-natives.jar", workPath + "\\Cachebox Core\\lib"));

		// ############################################################################################################################

		// for Desktop Proj
		rules.add(new CopyRule(cs + "gdx-natives.jar", workPath + "\\DesctopLibGdx_Test\\lib"));
		rules.add(new CopyRule(cs + "gdx-backend-lwjgl-natives.jar", workPath + "\\DesctopLibGdx_Test\\lib"));
		rules.add(new CopyRule(cs + "gdx-backend-lwjgl.jar", workPath + "\\DesctopLibGdx_Test\\lib"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype.jar", workPath + "\\DesctopLibGdx_Test\\lib"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype-natives.jar", workPath + "\\DesctopLibGdx_Test\\lib"));

		// for refresh_libraries
		rules.add(new CopyRule(cs + "\\extensions\\gdx-tools\\gdx-tools.jar", workPath + "\\refresh_libraries\\refresh_libraries\\libs"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype.jar", workPath
				+ "\\refresh_libraries\\refresh_libraries\\libs"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype-natives.jar", workPath
				+ "\\refresh_libraries\\refresh_libraries\\libs"));
		rules.add(new CopyRule(cs + "gdx.jar", workPath + "\\refresh_libraries\\refresh_libraries\\libs"));

		rules.add(new CopyRule(cs + "gdx-natives.jar", workPath + "\\refresh_libraries\\refresh_libraries-desktop\\libs"));
		rules.add(new CopyRule(cs + "gdx-backend-lwjgl-natives.jar", workPath + "\\refresh_libraries\\refresh_libraries-desktop\\libs"));
		rules.add(new CopyRule(cs + "gdx-backend-lwjgl.jar", workPath + "\\refresh_libraries\\refresh_libraries-desktop\\libs"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype.jar", workPath
				+ "\\refresh_libraries\\refresh_libraries-desktop\\libs"));
		rules.add(new CopyRule(cs + "\\extensions\\gdx-freetype\\gdx-freetype-natives.jar", workPath
				+ "\\refresh_libraries\\refresh_libraries-desktop\\libs"));

		return rules;
	}

	private ArrayList<CopyRule> getCopyRulesTranslations()
	{
		// Path to extracted LibGdx.zip
		String cs = workPath + "\\" + "translations\\cachebox\\";

		String allLangs[] = (new File(cs)).list(new FilenameFilter()
		{

			@Override
			public boolean accept(File arg0, String arg1)
			{
				if (arg1.contains(".")) return false;
				return true;
			}
		});

		ArrayList<CopyRule> rules = new ArrayList<CopyRule>();

		for (String lang : allLangs)
		{
			rules.add(new CopyRule(cs + lang, workPath + "\\Android_GUI\\assets\\data\\lang"));
		}

		return rules;
	}

	ClickListener selectTrunkListner = new ClickListener()
	{
		@Override
		public void clicked(InputEvent event, float x, float y)
		{
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(new java.io.File(workPath));
			chooser.setDialogTitle("select Trunk Folder");

			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				workPath = chooser.getSelectedFile().getAbsolutePath();
				chkSource();
				System.out.println("You chose to open this file: " + chooser.getSelectedFile().getAbsolutePath());
			}
		}
	};

	ClickListener runListnerLibGdx = new ClickListener()
	{
		@Override
		public void clicked(InputEvent event, float x, float y)
		{
			Thread t = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					downloadAndExtractLibgdx();
				}
			});
			t.start();
		}
	};

	ClickListener runListnerTranslations = new ClickListener()
	{
		@Override
		public void clicked(InputEvent event, float x, float y)
		{
			Thread t = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					downloadAndExtractTranslations();
				}
			});
			t.start();
		}
	};

	ClickListener runListnerTexture = new ClickListener()
	{
		@Override
		public void clicked(InputEvent event, float x, float y)
		{
			Thread t = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					CreateTextureAndCopy();
				}
			});
			t.start();
		}
	};

	@Override
	public void dispose()
	{
		stage.dispose();

	}

	@Override
	public void render()
	{
		Gdx.gl.glClearColor(0.8f, 0.9f, 0.8f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(Gdx.graphics.getDeltaTime());
		try
		{
			stage.draw();
		}
		catch (Exception e)
		{
			stage.getSpriteBatch().end();
		}
	}

	@Override
	public void resize(int width, int height)
	{
		w = width;
		h = height;
		layout();
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}

	private void layout()
	{
		lblMsg.setHeight(50);
		lblMsg.setWidth(w);
		lblMsg.setAlignment(Align.center, Align.center);
		lblMsg.setWrap(true);
		lblMsg.setText(Msg);
		lblMsg.setPosition(0, h - lblMsg.getHeight());

		if (WorkPathFound > 0)
		{
			btnRunTranslations.setVisible(true);
			if (ImageWorkPathFound) btnRunTexturePacker.setVisible(true);
			else
				btnRunTexturePacker.setVisible(false);

			btnRunLibGdx.setWidth(300);
			btnRunLibGdx.setHeight(35);
			btnRunLibGdx.setY(lblMsg.getY() - margin - btnRunLibGdx.getHeight());
			btnRunLibGdx.setX(margin);

			btnRunTranslations.setWidth(300);
			btnRunTranslations.setHeight(35);
			btnRunTranslations.setY(lblMsg.getY() - margin - btnRunLibGdx.getHeight());
			btnRunTranslations.setX(btnRunLibGdx.getX() + btnRunLibGdx.getWidth() + margin);

			btnRunTexturePacker.setWidth(300);
			btnRunTexturePacker.setHeight(35);
			btnRunTexturePacker.setY(lblMsg.getY() - margin - btnRunLibGdx.getHeight());
			btnRunTexturePacker.setX(btnRunTranslations.getX() + btnRunLibGdx.getWidth() + margin);

		}
		else
		{
			btnRunTranslations.setVisible(false);
			btnRunTexturePacker.setVisible(false);

			btnRunLibGdx.setWidth(300);
			btnRunLibGdx.setHeight(35);
			btnRunLibGdx.setY(lblMsg.getY() - margin - btnRunLibGdx.getHeight());
			btnRunLibGdx.setX(w / 2 - btnRunLibGdx.getWidth() / 2);

		}

		lblOut.setHeight(btnRunLibGdx.getY() - margin - margin);
		lblOut.setWidth(w - margin - margin);
		lblOut.setWrap(true);
		lblOut.setPosition(margin, margin);

	}

	private ArrayList<String> outLines = new ArrayList<String>();

	private void writeMsg(String msg)
	{
		writeMsg(msg, false);
	}

	private void writeMsg(String msg, boolean deleteLastMsg)
	{
		if (deleteLastMsg) outLines.remove(outLines.size() - 1);
		outLines.add(msg);
		String out = "";
		if (outLines.size() > 25) outLines.remove(0);

		for (String tmp : outLines)
		{
			out += tmp + "\n";
		}

		lblOut.setText(out);
		lblOut.invalidate();
	}

	Thread dlProgressChecker = null;;
	Downloader dl = null;
	int lastProgress = 0;

	private void downloadAndExtractLibgdx()
	{
		System.out.println("RUN");
		writeMsg("RUN LibGdx");

		String zipFile = "libgdx-nightly-latest.zip";

		File target = new File(workPath + "\\" + zipFile);

		if (!target.exists() || !DontDownloadIfExist)
		{
			try
			{
				dl = new Downloader(new URL("http://libgdx.badlogicgames.com/nightlies/libgdx-nightly-latest.zip"), target);
			}
			catch (MalformedURLException e)
			{
				writeMsg("MalformedURLException" + e.toString());
				return;
			}

			writeMsg("Download libgdx-nightly-latest.zip");

			lastProgress = 0;
			dlProgressChecker = new Thread(new Runnable()
			{

				@Override
				public void run()
				{

					while (!dl.isCompleted())
					{
						int progress = dl.getProgressPercent();
						if (lastProgress != progress)
						{
							writeMsg("Download libgdx-nightly-latest.zip  " + progress + "%", true);
							lastProgress = progress;
						}
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}
			});

			dlProgressChecker.start();

			dl.run();
			writeMsg("Download libgdx-nightly-latest.zip  100%", true);

		}
		else
		{
			writeMsg("Exist: libgdx-nightly-latest.zip ", true);
		}

		/*
		 * STEP 1 : Create directory with the name of the zip file
		 * 
		 * For e.g. if we are going to extract c:/demo.zip create c:/demo directory where we can extract all the zip entries
		 */

		String source = workPath + "\\libgdx-nightly-latest.zip";

		unzip(source);

		writeMsg("Copy Libaries");
		writeMsg("Copy: ");

		Copy copy = new Copy(getCopyRulesLibGdx());
		try
		{
			copy.Run(new CopyMsg()
			{
				@Override
				public void Msg(String msg)
				{
					writeMsg(msg, true);
				}
			});
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// delete download and extracted files
		target.delete();
		int pos = source.lastIndexOf(".");
		source = source.substring(0, pos);
		File f = new File(source);
		deleteDir(f);

		writeMsg("Ready", true);
		writeMsg("");
		writeMsg("Don´t forgot refresh and clean on Eclipse");
	}

	private void downloadAndExtractTranslations()
	{
		writeMsg("RUN Translations");

		String zipFile = "translations\\cachebox.zip";

		File target = new File(workPath + "\\" + zipFile);

		if (!target.exists() || !DontDownloadIfExist)
		{
			try
			{
				dl = new Downloader(new URL("http://crowdin.net/download/project/cachebox.zip"), target);
			}
			catch (MalformedURLException e)
			{
				writeMsg("MalformedURLException" + e.toString());
				return;
			}

			writeMsg("Download crowdin translations");

			lastProgress = 0;
			dlProgressChecker = new Thread(new Runnable()
			{

				@Override
				public void run()
				{

					while (!dl.isCompleted())
					{
						int progress = dl.getProgressPercent();
						if (lastProgress != progress)
						{
							writeMsg("Download crowdin translations  " + progress + "%", true);
							lastProgress = progress;
						}
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}
			});

			dlProgressChecker.start();

			dl.run();
			writeMsg("Download crowdin translations  100%", true);

		}
		else
		{
			writeMsg("Exist: crowdin translations ", true);
		}

		/*
		 * STEP 1 : Create directory with the name of the zip file
		 * 
		 * For e.g. if we are going to extract c:/demo.zip create c:/demo directory where we can extract all the zip entries
		 */

		String source = workPath + "\\" + zipFile;

		unzip(source);

		writeMsg("Copy Translations");
		writeMsg("Copy: ");

		Copy copy = new Copy(getCopyRulesTranslations());
		try
		{
			copy.Run(new CopyMsg()
			{
				@Override
				public void Msg(String msg)
				{
					writeMsg(msg, true);
				}
			});
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		writeMsg("Ready", true);
		writeMsg("");
		writeMsg("Don´t forgot refresh and clean on Eclipse");
	}

	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns false.
	public static boolean deleteDir(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++)
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success)
				{
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	private void unzip(String strZipFile)
	{

		try
		{
			/*
			 * STEP 1 : Create directory with the name of the zip file
			 * 
			 * For e.g. if we are going to extract c:/demo.zip create c:/demo directory where we can extract all the zip entries
			 */
			File fSourceZip = new File(strZipFile);
			String zipPath = strZipFile.substring(0, strZipFile.length() - 4);
			File temp = new File(zipPath);
			temp.mkdir();
			System.out.println(zipPath + " created");
			writeMsg(zipPath + " created");

			/*
			 * STEP 2 : Extract entries while creating required sub-directories
			 */
			ZipFile zipFile = new ZipFile(fSourceZip);
			Enumeration<? extends ZipEntry> e = zipFile.entries();

			while (e.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry) e.nextElement();
				File destinationFilePath = new File(zipPath, entry.getName());

				// create directories if required.
				destinationFilePath.getParentFile().mkdirs();

				// if the entry is directory, leave it. Otherwise extract it.
				if (entry.isDirectory())
				{
					continue;
				}
				else
				{
					// System.out.println("Extracting " + destinationFilePath);
					writeMsg("Extracting " + destinationFilePath.getName(), true);
					/*
					 * Get the InputStream for current entry of the zip file using
					 * 
					 * InputStream getInputStream(Entry entry) method.
					 */
					BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

					int b;
					byte buffer[] = new byte[1024];

					/*
					 * read the current entry from the zip file, extract it and write the extracted file.
					 */
					FileOutputStream fos = new FileOutputStream(destinationFilePath);
					BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

					while ((b = bis.read(buffer, 0, 1024)) != -1)
					{
						bos.write(buffer, 0, b);
					}

					// flush the output stream and close it.
					bos.flush();
					bos.close();

					// close the input stream.
					bis.close();
				}

			}
			zipFile.close();
		}
		catch (IOException ioe)
		{
			System.out.println("IOError :" + ioe);
			writeMsg("IOError :" + ioe);
		}

		writeMsg("Extracting Ready", true);

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
		textureSettings.minWidth = 16;
		textureSettings.minHeight = 16;
		textureSettings.maxWidth = 512;
		textureSettings.maxHeight = 1024;
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
		String inputFolder = imageWorkPath + "\\LibgdxPacker\\default\\input\\day\\UI_IconPack";
		String outputFolder = imageWorkPath + "\\LibgdxPacker\\default\\Output\\day";
		String Name = "UI_IconPack.spp";
		ArrayList<String> outPutFolders = new ArrayList<String>();

		try
		{
			TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);
		}
		catch (Exception e1)
		{
			writeMsg(e1.getCause().getMessage());
			writeMsg(e1.getCause().getCause().getMessage());
		}

		// Pack Default night
		inputFolder = imageWorkPath + "\\LibgdxPacker\\default\\input\\night\\UI_IconPack";
		outputFolder = imageWorkPath + "\\LibgdxPacker\\default\\Output\\night";
		outPutFolders.add(outputFolder);
		Name = "UI_IconPack.spp";
		TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);

		// Pack small day
		inputFolder = imageWorkPath + "\\LibgdxPacker\\small\\input\\day\\UI_IconPack";
		outputFolder = imageWorkPath + "\\LibgdxPacker\\small\\Output\\day";
		outPutFolders.add(outputFolder);
		Name = "UI_IconPack.spp";
		TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);

		// Pack small night
		inputFolder = imageWorkPath + "\\LibgdxPacker\\small\\input\\night\\UI_IconPack";
		outputFolder = imageWorkPath + "\\LibgdxPacker\\small\\Output\\night";
		outPutFolders.add(outputFolder);
		Name = "UI_IconPack.spp";
		TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);

		// Pack Default day
		inputFolder = imageWorkPath + "\\LibgdxPacker\\default\\input\\splash";
		outputFolder = imageWorkPath + "\\LibgdxPacker\\default\\Output\\day";
		outPutFolders.add(outputFolder);
		Name = "SplashPack.spp";
		TexturePacker.process(textureSettings, inputFolder, outputFolder, Name);

		writeMsg("Copy Textures");
		writeMsg("Copy: ");

		// Change TexturFilter at *.spp files with in all output folder
		String br = Global.br;
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

		Copy copy = new Copy(getCopyRulesTexture());
		try
		{
			copy.Run(new CopyMsg()
			{
				@Override
				public void Msg(String msg)
				{
					writeMsg(msg, true);
				}
			});
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		writeMsg("Ready", true);
		writeMsg("");
		writeMsg("Don´t forgot refresh and clean on Eclipse");

	}

	private ArrayList<CopyRule> getCopyRulesTexture()
	{
		// Path to extracted LibGdx.zip
		String cs = imageWorkPath;

		ArrayList<CopyRule> rules = new ArrayList<CopyRule>();

		// for Android Proj
		rules.add(new CopyRule(cs + "\\LibgdxPacker\\default\\Output\\day", workPath + "\\Android_GUI\\assets\\skins\\default"));
		rules.add(new CopyRule(cs + "\\LibgdxPacker\\small\\Output\\day", workPath + "\\Android_GUI\\assets\\skins\\small"));
		rules.add(new CopyRule(cs + "\\LibgdxPacker\\default\\Output\\night", workPath + "\\Android_GUI\\assets\\skins\\default"));
		rules.add(new CopyRule(cs + "\\LibgdxPacker\\small\\Output\\night", workPath + "\\Android_GUI\\assets\\skins\\small"));

		return rules;
	}

	public class TeePrintStream extends PrintStream
	{
		private final PrintStream second;

		public TeePrintStream(PrintStream second)
		{
			super(second);
			this.second = second;
		}

		/**
		 * Closes the main stream. The second stream is just flushed but <b>not</b> closed.
		 * 
		 * @see java.io.PrintStream#close()
		 */
		@Override
		public void close()
		{
			// just for documentation
			super.close();
		}

		@Override
		public void flush()
		{
			super.flush();
			second.flush();
		}

		private String last = "";

		@Override
		public void write(byte[] buf, int off, int len)
		{
			super.write(buf, off, len);

			String intermediate = (new String(buf)).substring(0, len);

			if (!intermediate.contains("\r\n"))
			{
				last += intermediate;
				writeMsg(last, true);
			}
			else
			{
				last += intermediate;
				writeMsg(last);
				last = "";
			}

		}

		@Override
		public void write(int b)
		{
			super.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException
		{
			super.write(b);
		}
	}

}
