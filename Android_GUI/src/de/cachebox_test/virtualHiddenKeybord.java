package de.cachebox_test;

import android.content.Context;
import android.content.Intent;
import de.cachebox_test.Views.Forms.keyBooardActivity;

public class virtualHiddenKeybord
{

	public virtualHiddenKeybord(final Context context)
	{

	}

	public static void setEditTextFocus(final boolean isFocused)
	{

		main.mainActivity.dontStop = true;

		final Intent mainIntent = new Intent().setClass(main.mainActivity, keyBooardActivity.class);
		main.mainActivity.startActivityForResult(mainIntent, 12345);

		// main.mainActivity.runOnUiThread(new Runnable()
		// {
		//
		// @Override
		// public void run()
		// {
		//
		// if (isFocused)
		// {
		//
		// mTextField.requestFocus();
		//
		// mTextField.setText("");
		//
		// RelativeLayout txtLayout = (RelativeLayout) main.mainActivity.findViewById(R.id.layoutTextField);
		// txtLayout.setVisibility(View.VISIBLE);
		//
		// }
		// else
		// {
		// RelativeLayout txtLayout = (RelativeLayout) main.mainActivity.findViewById(R.id.layoutTextField);
		// txtLayout.setVisibility(View.INVISIBLE);
		// }
		// }
		// });

	}

}
