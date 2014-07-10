package de.CB.TestBase;

import CB_UI_Base.Math.UI_Size_Base;

public class UiSizes extends CB_UI_Base.Math.UiSizes
{
	public static UI_Size_Base that;

	public UiSizes()
	{
		super();
		that = this;
	}

	@Override
	public void instanzeInitial()
	{
		super.instanzeInitial();
	}

}
