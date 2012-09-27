package de.cachebox_test.Custom_Controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.EditText;
import de.cachebox_test.Views.Forms.keyBooardActivity;

public class hiddenTextField extends EditText
{

	public hiddenTextField(Context context)
	{
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawColor(Color.TRANSPARENT);

		// Debug
		canvas.drawColor(Color.argb(50, 0, 0, 255));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{

		super.onSizeChanged(w, h, oldw, oldh);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (!keyBooardActivity.isKeyBoardVisible()) keyBooardActivity.close();

	}

}
