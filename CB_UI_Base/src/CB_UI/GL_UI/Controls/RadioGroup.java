package CB_UI.GL_UI.Controls;

import java.util.ArrayList;

public class RadioGroup
{
	private ArrayList<RadioButton> radios;
	RadioButton aktSelected;

	public RadioGroup()
	{
		radios = new ArrayList<RadioButton>();
	}

	public void add(RadioButton radio)
	{
		radio.setRadioGroup(this);
		radios.add(radio);
	}

	public void remove(RadioButton radio)
	{
		radios.remove(radio);
	}

	public void aktivate(RadioButton radioButton)
	{
		aktSelected = radioButton;

		// alle anderen ausschalten
		for (RadioButton tmp : radios)
		{
			if (tmp == aktSelected) continue;
			tmp.setChecked(false);

		}
	}

}
