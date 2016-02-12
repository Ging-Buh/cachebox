package Types;

import junit.framework.TestCase;
import CB_UI.Config;

public class Config_Crypt extends TestCase {
	public void test_crypt() {

		String encrypt = "7EXbNDQDCWkNaNAAF2FJ4x+VdEz29dm5niKWBBQ=";
		String decrypt = "WOANx0HpzSXJgAQBXV/lWMLKro9s=";

		String dec = Config.decrypt(encrypt);
		assertEquals("Sollte gleich sein", dec, decrypt);

		String enc = Config.encrypt(decrypt);
		assertEquals("Sollte gleich sein", enc, encrypt);

	}
}
