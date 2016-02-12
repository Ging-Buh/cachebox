package CB_Core;

import junit.framework.TestCase;
import CB_UI.Config;

public class Config_Crypt extends TestCase {
	public void test_crypt() {
		// String encrypt = "8jrcNgBkNHcUfe4FAhlQ23e6CXntl83EvxXiSg==";
		// String decrypt = "I0FLLWuncFfOr9Hz0yRYL/X6SXM=";

		String encrypt = "7EXbNDQDCWkNaNAAF2FJ4x+VdEz29dm5niKWBBQ=";
		String decrypt = "WOANx0HpzSXJgAQBXV/lWMLKro9s=";

		String dec = Config.decrypt(encrypt);
		assertEquals("Sollte gleich sein", dec, decrypt);

		String enc = Config.encrypt(decrypt);
		assertEquals("Sollte gleich sein", enc, encrypt);

	}
}
