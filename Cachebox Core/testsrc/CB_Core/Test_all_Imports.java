package CB_Core;

import CB_Core.Import.GPX_Unzip_Import_Test;
import CB_Core.Import.GSAKGpxImportTest;
import CB_Core.Import.GpxImportTest;
import junit.framework.TestCase;

/**
 * Führt einen Test durch, in dem alle einzelnen Import Tests abgearbeitet werden.
 * 
 * @author Longri
 *
 */
public class Test_all_Imports extends TestCase
{
	
	public static void test_all_Import() throws Exception
	{
		//Teste UnZip
		GPX_Unzip_Import_Test.testUnzip_Import();
		
		// Teste GpxImport
		GpxImportTest.testGpxImport();
		
		// Teste GSAK Import
		GSAKGpxImportTest.testGpxImport();
		
	}
}
