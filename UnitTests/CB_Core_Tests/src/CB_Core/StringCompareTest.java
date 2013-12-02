package CB_Core;

import junit.framework.TestCase;

public class StringCompareTest extends TestCase
{
	final int TESTCOUNT = 10000000;

	final String TEST = "rendertheme";

	int count = 0;

	public void test()
	{

		long start = System.currentTimeMillis();
		for (int i = 0; i < TESTCOUNT; i++)
		{
			if ("rendertheme".equals("rendertheme")) count++;
		}
		long time = System.currentTimeMillis() - start;
		System.out.print("Time: " + time);
	}
}
