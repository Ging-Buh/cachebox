package CB_Core.CB_Core.Export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Core.Export.GpxSerializer;
import CB_Core.Export.GpxSerializer.ProgressListener;
import __Static.InitTestDBs;

public class GPX_Export extends TestCase
{

	@Test
	public void testSingleExport()
	{
		InitTestDBs.InitalConfig();

		String exportPath = "./testdata/gpx/ExportTest_GC2T9RW.gpx";
		File exportFile = new File(exportPath);

		// Delete File if exist
		if (exportFile.exists()) exportFile.delete();

		ArrayList<String> allGeocodesIn = new ArrayList<String>();
		allGeocodesIn.add("GC2T9RW");
		try
		{
			final GpxSerializer ser = new GpxSerializer();
			final FileWriter writer = new FileWriter(exportFile);
			ser.writeGPX(allGeocodesIn, writer, new ProgressListener()
			{

				@Override
				public void publishProgress(int countExported)
				{

				}
			});
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
