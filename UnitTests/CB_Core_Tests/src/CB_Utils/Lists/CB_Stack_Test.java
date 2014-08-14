package CB_Utils.Lists;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Locator.Map.Descriptor;
import CB_UI_Base.Global;
import CB_Utils.Lists.CB_Stack.iCompare;
import __Static.InitTestDBs;

public class CB_Stack_Test extends TestCase
{
	@Test
	public void test_Stack()
	{
		InitTestDBs.InitalConfig();

		CB_Stack<Descriptor> stack = new CB_Stack<Descriptor>();

		Descriptor desc_1 = new Descriptor(1, 0, 1, false);
		Descriptor desc_2 = new Descriptor(2, 0, 1, false);
		Descriptor desc_3 = new Descriptor(3, 0, 1, false);
		Descriptor desc_4 = new Descriptor(4, 0, 1, false);
		Descriptor desc_5 = new Descriptor(5, 0, 1, false);
		Descriptor desc_6 = new Descriptor(6, 0, 1, false);
		Descriptor desc_7 = new Descriptor(7, 0, 1, false);

		assertTrue("Stack must be empty", stack.empty());

		stack.add(desc_1);
		stack.add(desc_2);
		stack.add(desc_3);
		stack.add(desc_4);
		stack.add(desc_5);
		stack.add(desc_6);
		stack.add(desc_7);

		assertFalse("Stack must not be empty", stack.empty());
		assertTrue("Stack.size must be 7", stack.getSize() == 7);
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_1));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_2));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_3));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_4));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_5));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_6));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_7));

		stack.add(desc_1);
		stack.add(desc_2);
		stack.add(desc_3);
		stack.add(desc_4);
		stack.add(desc_5);
		stack.add(desc_6);
		stack.add(desc_7);

		assertFalse("Stack must not be empty", stack.empty());
		assertTrue("Stack.size must be 7", stack.getSize() == 7);
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_1));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_2));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_3));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_4));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_5));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_6));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_7));

		stack.setMaxItemSize(4);

		assertFalse("Stack must not be empty", stack.empty());
		assertTrue("Stack.size must be 4", stack.getSize() == 4);
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_4));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_5));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_6));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_7));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_1));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_2));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_3));

		Descriptor testDesc_1 = stack.get();

		assertFalse("Stack must not be empty", stack.empty());
		assertTrue("Stack.size must be 3", stack.getSize() == 3);
		assertEquals("Getted Object must be equals", testDesc_1, desc_4);
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_5));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_6));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_7));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_1));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_2));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_3));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_4));

		Descriptor testDesc_2 = stack.get();

		assertFalse("Stack must not be empty", stack.empty());
		assertTrue("Stack.size must be 2", stack.getSize() == 2);
		assertEquals("Getted Object must be equals", testDesc_2, desc_5);
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_6));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_7));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_1));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_2));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_3));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_4));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_5));

		Descriptor testDesc_3 = stack.get();

		assertFalse("Stack must not be empty", stack.empty());
		assertTrue("Stack.size must be 1", stack.getSize() == 1);
		assertEquals("Getted Object must be equals", testDesc_3, desc_6);
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_7));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_1));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_2));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_3));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_4));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_5));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_6));

		Descriptor testDesc_4 = stack.get();

		assertTrue("Stack must be empty", stack.empty());
		assertTrue("Stack.size must be 0", stack.getSize() == 0);
		assertEquals("Getted Object must be equals", testDesc_4, desc_7);
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_1));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_2));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_3));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_4));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_5));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_6));
		assertFalse("Stack must contain this Descriptor", stack.contains(desc_7));

	}

	@Test
	public void test_Stack_Sort()
	{
		InitTestDBs.InitalConfig();

		CB_Stack<Descriptor> stack = new CB_Stack<Descriptor>();

		final Descriptor desc_1 = new Descriptor(1, 1, 1, false);
		final Descriptor desc_2 = new Descriptor(0, 0, 1, false);
		final Descriptor desc_3 = new Descriptor(2, 2, 1, false);
		final Descriptor desc_4 = new Descriptor(2, 3, 1, false);
		final Descriptor desc_5 = new Descriptor(3, 2, 1, false);
		final Descriptor desc_6 = new Descriptor(3, 3, 1, false);
		final Descriptor desc_7 = new Descriptor(0, 2, 1, false);

		assertTrue("Stack must be empty", stack.empty());

		stack.add(desc_1);
		stack.add(desc_2);
		stack.add(desc_3);
		stack.add(desc_4);
		stack.add(desc_5);
		stack.add(desc_6);
		stack.add(desc_7);

		assertFalse("Stack must not be empty", stack.empty());
		assertTrue("Stack.size must be 7", stack.getSize() == 7);
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_1));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_2));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_3));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_4));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_5));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_6));
		assertTrue("Stack must contain this Descriptor", stack.contains(desc_7));

		stack.sort(new iCompare<Descriptor>()
		{

			@Override
			public int compare(Descriptor item1, Descriptor item2)
			{
				int distanceFromCenter1 = item1.getDistance(desc_3);
				int distanceFromCenter2 = item2.getDistance(desc_3);
				if (distanceFromCenter1 == distanceFromCenter2) return 0;
				if (distanceFromCenter1 > distanceFromCenter2) return 1;
				return -1;
			}
		});

		Descriptor[] test = new Descriptor[]
			{ desc_3, desc_1, desc_4, desc_5, desc_6, desc_2, desc_7 };

		for (int i = 0; i < 7; i++)
		{
			Descriptor desc = stack.get();
			System.out.print(desc.toString() + " == " + test[i].toString() + Global.br);
			assertEquals("wrong sequence", desc, test[i]);
		}

	}
}
