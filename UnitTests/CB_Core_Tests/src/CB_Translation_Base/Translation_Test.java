package CB_Translation_Base;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_Utils.GdxTestRunner;
import __Static.InitTestDBs;

@RunWith(GdxTestRunner.class)
public class Translation_Test extends TestCase
{
	public static final String br = System.getProperty("line.separator");

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
	}

	@Test
	public void testInitial()
	{

		InitTestDBs.InitialTranslations("de");
		assertTrue("Translation Engine not initial", Translation.isInitial());
	}

	@Test
	public void testdefaultDE()
	{
		InitTestDBs.InitialTranslations("de");
		assertEquals("Deutsch", Translation.Get("lang"));

		assertEquals("Zeige Logs", Translation.Get("ShowLogs"));

		assertEquals("Fehler beim Erstellen der PocketQuery!", Translation.Get("ErrCreatePQ"));

		assertEquals("Querprodukt", Translation.Get("solverFuncCrossproduct"));

		assertEquals("Suche Caches", Translation.Get("SearchCache"));

		assertEquals("Erlaube Landscape Ansicht", Translation.Get("AllowLandscape"));

		assertEquals("Koordinaten des Breitengrads für den ersten Kartenaufruf", Translation.Get("Desc_MapInitLatitude"));

	}

	@Test
	public void testdefaultFr()
	{
		InitTestDBs.InitialTranslations("fr");
		assertEquals("Français", Translation.Get("lang".hashCode()));

		assertEquals("Journal", Translation.Get("ShowLogs".hashCode()));

		assertEquals("Création d'une PQ erronée!", Translation.Get("ErrCreatePQ".hashCode()));

		assertEquals("CrossProduct", Translation.Get("solverFuncCrossproduct".hashCode()));

		assertEquals("Recherche de cache", Translation.Get("SearchCache".hashCode()));

		assertEquals("Permettre la vue de paysage", Translation.Get("AllowLandscape".hashCode()));

		// not translate at FR, find default EN text
		assertEquals("Latitude of initial MapView", Translation.Get("Desc_MapInitLatitude".hashCode()));

	}

	@Test
	public void testWithParameter()
	{
		InitTestDBs.InitialTranslations("de");
		assertEquals("Die FieldNote" + br + br + "[Param1]" + br + br + " von Trackable" + br + br + "[Param2] wirklich löschen?",
				Translation.Get("confirmFieldnoteDeletionTB", "Param1", "Param2"));

		assertEquals("Fehler: Funktion Param1/Parameter Param2 (Param3) ist keine gültige Param4: [Param5]",
				Translation.Get("solverErrParamType", "Param1", "Param2", "Param3", "Param4", "Param5"));

	}

	@Test
	public void testNoID()
	{
		InitTestDBs.InitialTranslations("de");

		assertEquals("$ID: keineID", Translation.Get("keineID"));

		assertTrue("Size of Missing Strings must be 1", Translation.that.mMissingStringList.size() > 1);
	}
}
