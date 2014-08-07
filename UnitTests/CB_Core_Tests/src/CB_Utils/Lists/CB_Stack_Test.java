package CB_Utils.Lists;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Locator.Map.Descriptor;

public class CB_Stack_Test extends TestCase
{
	@Test
	public void test_Stack()
	{
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
}
