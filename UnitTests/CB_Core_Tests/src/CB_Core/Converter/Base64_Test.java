package CB_Core.Converter;

import java.io.IOException;

import CB_Utils.Converter.Base64;

import junit.framework.TestCase;

/**
 * test only CB used methods
 * 
 * @author Longri
 */
public class Base64_Test extends TestCase {

	public void testDE_Encrypt() {
		final String value = "Test";
		final String enc = "VGVzdA==";

		String encrypted = Base64.encodeBytes(value.getBytes());
		assertEquals("must", enc, encrypted);
		byte[] decrypted = null;
		try {
			decrypted = Base64.decode(encrypted.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		char[] c = new char[decrypted.length];
		for (int x = 0; x < decrypted.length; x++) {
			c[x] = (char) decrypted[x];
		}

		String decryptedString = String.copyValueOf(c);
		assertEquals("must", value, decryptedString);
	}

}
