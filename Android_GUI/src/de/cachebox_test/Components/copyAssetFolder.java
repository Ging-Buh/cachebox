package de.cachebox_test.Components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;




// Kopiert die OrdnerStructur des Asset-Ordners auf die SD-Karte (Cachbox Arbeitsverzeichniss)
public class copyAssetFolder 
{
	public void copyAll(AssetManager assets, String targetPath, String[] exludeFolder)
	{
		FileList = new  ArrayList<String>();
		try {
			listDir(assets, "", exludeFolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try
		{
			for (String tmp :FileList)
			{
				try {
					copyFile(assets, tmp, targetPath + "/" + tmp);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void copyAll(AssetManager assets, String targetPath)
	{
		String[]leer = new String[]{""};
		copyAll(assets,targetPath,leer);
	}
	
		
	public static ArrayList<String> FileList = new  ArrayList<String>();
	private void listDir(AssetManager assets,String dir,String[] excludeFolder ) throws IOException 
	{
		String[] files = assets.list(dir);
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				boolean exclude = false;
				for (String tmp : excludeFolder )
				{
					if (files[i].equals(tmp))
					{	
						exclude = true;
						break;
					}
				}
				if (!exclude)
				{
					String Entry = (dir.equals(""))? files[i] : dir +"/" + files[i];
					if (!Entry.contains(".")) {
						//System.out.print(" (Ordner)\n");
						listDir(assets,Entry,excludeFolder); // ruft sich selbst mit dem 
							// Unterverzeichnis als Parameter auf
						}
					else {
						FileList.add(Entry);
						
					}
				}
			}
		}
	}
	

	private static void copyFile(AssetManager assets, String source , String target) throws IOException{

	   
	    InputStream myInput = assets.open(source);

	    File ziel = new File(target);
	    ziel.getParentFile().mkdirs();
	    OutputStream myOutput = new FileOutputStream(target);

	    //transfer bytes from the inputfile to the outputfile
	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = myInput.read(buffer))>0){
	        myOutput.write(buffer, 0, length);
	    }

	    //Close the streams
	    myOutput.flush();
	    myOutput.close();
	    myInput.close();

	}

	
	private void makeDir(String Dir)
	{
		String[] dir = Dir.split("/");
		String dirReady = "";
		for (String tmp : dir)
		{
			
		}
	}



	
}
