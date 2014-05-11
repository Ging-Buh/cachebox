package CB_Translation_Base;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import CB_Core.InitTestDBs;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_Utils.GdxTestRunner;

import com.badlogic.gdx.Files.FileType;

@RunWith(GdxTestRunner.class)
public class Translation_Test extends TestCase
{
	public static final String br = System.getProperty("line.separator");

	@Override
	public void setUp() throws Exception
	{

		super.setUp();

		InitTestDBs.InitalConfig();
		new Translation(Config.WorkPath, FileType.Absolute);
		try
		{
			Translation.LoadTranslation(Config.WorkPath + "/lang/de/strings.ini");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testInitial()
	{

		InitTestDBs.InitialTranslations();
		assertTrue("Translation Engine not initial", Translation.isInitial());
	}

	@Test
	public void testdefault()
	{
		InitTestDBs.InitialTranslations();
		assertEquals("Deutsch", Translation.Get("lang"));

		assertEquals("Zeige Logs", Translation.Get("ShowLogs"));

		assertEquals("Fehler beim Erstellen der PocketQuery!", Translation.Get("ErrCreatePQ"));

		assertEquals("Querprodukt", Translation.Get("solverFuncCrossproduct"));

		assertEquals("Suche Caches", Translation.Get("SearchCache"));

		assertEquals("Erlaube Landscape Ansicht", Translation.Get("AllowLandscape"));

		assertEquals("Koordinaten des Breitengrads für den ersten Kartenaufruf", Translation.Get("Desc_MapInitLatitude"));

	}

	@Test
	public void testWithParameter()
	{
		InitTestDBs.InitialTranslations();
		assertEquals("Die FieldNote" + br + br + "[Param1]" + br + br + " von Trackable" + br + br + "[Param2] wirklich löschen?",
				Translation.Get("confirmFieldnoteDeletionTB", "Param1", "Param2"));

		assertEquals("Fehler: Funktion Param1/Parameter Param2 (Param3) ist keine gültige Param4: [Param5]",
				Translation.Get("solverErrParamType", "Param1", "Param2", "Param3", "Param4", "Param5"));

	}
}
