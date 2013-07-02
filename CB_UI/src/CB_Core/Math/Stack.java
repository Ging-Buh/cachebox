package CB_Core.Math;

import java.util.ArrayList;

public class Stack<T>
{
	ArrayList<T> m_list;

	public Stack()
	{
		m_list = new ArrayList<T>();
	}

	public void push(T value)
	{
		m_list.add(0, value);
	}

	public T pop()
	{
		T temp = null;
		if (m_list.size() > 0)
		{
			m_list.get(0);
			m_list.remove(0);
		}
		return temp;
	}

}
