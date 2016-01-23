package CB_Utils;

import java.io.IOException;

import CB_Utils.Converter.Base64;

public abstract class Config_Core
{
	static Config_Core that;

	public Config_Core(String workPath)
	{
		that = this;
		mWorkPath = workPath;
	}

	public static String mWorkPath = "";

	static final int[] Key =
		{ 128, 56, 20, 78, 33, 225 };

	public static String decrypt(String value)
	{
		int[] b = null;
		try
		{
			b = byte2intArray(Base64.decode(value));
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}

		RC4(b, Key);
		String decrypted = "";

		char[] c = new char[b.length];
		for (int x = 0; x < b.length; x++)
		{
			c[x] = (char) b[x];
		}

		decrypted = String.copyValueOf(c);

		return decrypted;

	}

	private static int[] byte2intArray(byte[] b)
	{
		int[] i = new int[b.length];

		for (int x = 0; x < b.length; x++)
		{
			int t = b[x];
			if (t < 0)
			{
				t += 256;
			}
			i[x] = t;
		}

		return i;
	}

	private static byte[] int2byteArray(int[] i)
	{
		byte[] b = new byte[i.length];

		for (int x = 0; x < i.length; x++)
		{

			int t = i[x];
			if (t > 128)
			{
				t -= 256;
			}

			b[x] = (byte) t;
		}

		return b;
	}

	public static String encrypt(String value)
	{
		String encrypted = "";
		try
		{
			int[] b = byte2intArray(value.getBytes());
			RC4(b, Key);
			encrypted = Base64.encodeBytes(int2byteArray(b));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return encrypted;
	}

	public static void RC4(int[] bytes, int[] key)
	{
		int[] s = new int[256];
		int[] k = new int[256];
		int temp;
		int i, j;

		for (i = 0; i < 256; i++)
		{
			s[i] = (int) i;
			k[i] = (int) key[i % key.length];
		}

		j = 0;
		for (i = 0; i < 256; i++)
		{
			j = (j + s[i] + k[i]) % 256;
			temp = s[i];
			s[i] = s[j];
			s[j] = temp;
		}

		i = j = 0;
		for (int x = 0; x < bytes.length; x++)
		{
			i = (i + 1) % 256;
			j = (j + s[i]) % 256;
			temp = s[i];
			s[i] = s[j];
			s[j] = temp;
			int t = (s[i] + s[j]) % 256;
			bytes[x] = (int) (bytes[x] ^ s[t]);
		}
	}

	protected abstract void acceptChanges();

	public static void AcceptChanges()
	{
		if (that != null) that.acceptChanges();
	}

}
