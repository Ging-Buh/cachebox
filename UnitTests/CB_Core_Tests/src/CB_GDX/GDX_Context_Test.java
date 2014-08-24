/* 
 * Copyright (C) 2011-2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_GDX;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.graphics.GL_Matrix;
import CB_Utils.GdxTestRunner;
import __Static.InitTestDBs;

/**
 * This test class contains all the tests that require a GDX-context.
 * 
 * @author Longri
 */
@RunWith(GdxTestRunner.class)
public class GDX_Context_Test extends TestCase
{
	public static final String br = System.getProperty("line.separator");

	// ################################################
	// Translation tests
	// ################################################

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

	// ################################################
	// Matrix tests
	// ################################################

	@Test
	public void test_Constructor()
	{
		GL_Matrix matrix = new GL_Matrix();
		assertTrue("must be create", matrix != null);
	}

	@Test
	public void test_CopyConstructor()
	{
		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setTranslate(12.5f, 0.123456f);

		GL_Matrix matrix2 = new GL_Matrix(matrix1);

		assertTrue("must be equals", GL_Matrix.MatrixEquals(matrix1.getMatrix4(), matrix2.getMatrix4()));
	}

	@Test
	public void test_reset()
	{
		float[] targetValues = new float[]
			{ 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f };

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setTranslate(12.5f, 0.123456f);
		matrix1.reset();

		assertMatrix("Matrix must be default", targetValues, matrix1);

	}

	@Test
	public void test_rotate()
	{
		float[] targetValues = AndroidSortedValues(0.977699f, -0.21001105f, 0.0f, 0.21001105f, 0.977699f, 0.0f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setRotate(12.123f);

		assertMatrix("Matrix rotate failer", targetValues, matrix1);
	}

	@Test
	public void test_rotatePivot()
	{
		float[] targetValues = AndroidSortedValues(0.977699f, -0.21001105f, 0.8380657f, 0.21001105f, 0.977699f, -5.6452446f, 0.0f, 0.0f,
				1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.rotate(12.123f, 27f, 1.123456f);

		assertMatrix("Matrix rotate pivot failer", targetValues, matrix1);
	}

	@Test
	public void test_scale()
	{
		float[] targetValues = AndroidSortedValues(2.0f, 0.0f, 0.0f, 0.0f, 1.3f, 0.0f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setScale(2, 1.3f);

		assertMatrix("Matrix scale failer", targetValues, matrix1);
	}

	@Test
	public void test_scalePivot()
	{
		float[] targetValues = AndroidSortedValues(2.0f, 0.0f, -1.123456f, 0.0f, 1.3f, -38.43599f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setScale(2, 1.3f, 1.123456f, 128.12f);

		assertMatrix("Matrix scale pivot failer", targetValues, matrix1);
	}

	@Test
	public void test_translate()
	{
		float[] targetValues = AndroidSortedValues(1.0f, 0.0f, 1.123456f, 0.0f, 1.0f, 128.12f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setTranslate(1.123456f, 128.12f);

		assertMatrix("Matrix translate failer", targetValues, matrix1);
	}

	@Test
	public void test_set()
	{
		float[] targetValues = new float[]
			{ 1.0f, 2.0f, 1.123456f, 0.4f, 1.0f, 128.12f, 12f, 32.0f, 1.0f };

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setValues(targetValues);

		assertMatrix("Matrix set failer", targetValues, matrix1);
	}

	@Test
	public void test_setMatrix4()
	{
		float[] targetValues = new float[]
			{ 1.0f, 2.0f, 1.123456f, 0.4f, 1.0f, 128.12f, 12f, 32.0f, 1.0f };

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setValues(targetValues);

		GL_Matrix matrix2 = new GL_Matrix();

		matrix2.set(matrix1);

		assertMatrix("Matrix set failer", targetValues, matrix2);
	}

	@Test
	public void test_postConcat()
	{
		float[] targetValues = AndroidSortedValues(0.9781476f, -0.2079117f, 10.0f, 0.2079117f, 0.9781476f, 12.0f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setTranslate(10, 12);

		GL_Matrix matrix2 = new GL_Matrix();
		matrix2.setRotate(12);

		matrix2.postConcat(matrix1);

		assertMatrix("Matrix postConcat failer", targetValues, matrix2);

	}

	public void test_preTranslate()
	{
		float[] targetValues = AndroidSortedValues(1.0f, 0.0f, 1.123456f, 0.0f, 1.0f, 128.12f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.preTranslate(1.123456f, 128.12f);

		assertMatrix("Matrix translate failer", targetValues, matrix1);
	}

	@Test
	public void test_preScale()
	{
		float[] targetValues = AndroidSortedValues(2.0f, 0.0f, 0.0f, 0.0f, 1.3f, 0.0f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.preScale(2, 1.3f);

		assertMatrix("Matrix scale failer", targetValues, matrix1);
	}

	@Test
	public void test_preScaleTranslate()
	{
		float[] targetValues = AndroidSortedValues(1.955398f, -0.27301437f, 6.973581f, 0.4200221f, 1.2710086f, -37.814766f, 0.0f, 0.0f,
				1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setRotate(12.123f);
		matrix1.preScale(2, 1.3f, 1.123456f, 128.12f);

		assertMatrix("Matrix preScaleTranslate failer", targetValues, matrix1);
	}

	@Test
	public void test_postRotate()
	{
		float[] targetValues = AndroidSortedValues(0.977699f, -0.21001105f, 5.576769f, 0.21001105f, 0.977699f, 21.65409f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setTranslate(10, 20);
		matrix1.postRotate(12.123f);

		assertMatrix("Matrix rotate failer", targetValues, matrix1);
	}

	@Test
	public void test_postScale()
	{
		float[] targetValues = AndroidSortedValues(2.0f, 0.0f, 20.0f, 0.0f, 1.3f, 26.0f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setTranslate(10, 20);
		matrix1.postScale(2, 1.3f);

		assertMatrix("Matrix scale failer", targetValues, matrix1);
	}

	@Test
	public void test_postTranslate()
	{
		float[] targetValues = AndroidSortedValues(0.9396926f, -0.34202012f, 24.234f, 0.34202012f, 0.9396926f, 4.3f, 0.0f, 0.0f, 1.0f);

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setRotate(20);
		matrix1.postTranslate(24.234f, 4.3f);

		assertMatrix("Matrix scale failer", targetValues, matrix1);
	}

	@Test
	public void test_preRotate()
	{
		float[] targetValues = new float[]
			{ 0.977699f, -0.21001105f, 0.0f, 0.21001105f, 0.977699f, 0.0f, 0.0f, 0.0f, 1.0f };

		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setRotate(12.123f);

		assertMatrix("Matrix rotate failer", targetValues, matrix1);
	}

	public void test_setValues()
	{
		// TODO implement
	}

	public void test_preSkew()
	{
		// TODO implement
	}

	public void test_preRotateTranslate()
	{
		// TODO implement
	}

	public void test_preConcat()
	{
		// TODO implement
	}

	@Test
	public void test_MapPoint()
	{
		GL_Matrix matrix1 = new GL_Matrix();
		matrix1.setTranslate(10, 20);

		float[] point = new float[]
			{ 10, 10 };

		matrix1.mapPoints(point);

		assertTrue("Maped point must be 20,30", point[0] == 20 && point[1] == 30);

	}

	// ######################################################################################
	// Test Helper Method's
	// ######################################################################################

	final float allowed_error = 0.000005f;

	private float[] AndroidSortedValues(float m0, float m1, float m2, float m3, float m4, float m5, float m6, float m7, float m8)
	{
		return new float[]
			{ m0, m1, m2, m3, m4, m5, m6, m7, m8 };
		// falsch { m0, m3, m6, m1, m4, m7, m2, m5, m8 };
	}

	private void assertMatrix(String failerMassage, float[] values, GL_Matrix matrix)
	{

		float[] matrixValues = new float[9];
		matrix.getValues(matrixValues);

		boolean equals = true;

		for (int i = 0; i < 9; i++)
		{
			float f = 100f;

			if (matrixValues[i] > values[i])
			{
				f = matrixValues[i] - values[i];
			}
			else
			{
				f = values[i] - matrixValues[i];
			}
			if (f > allowed_error) equals = false;
		}

		assertTrue(failerMassage, equals);

	}

}
