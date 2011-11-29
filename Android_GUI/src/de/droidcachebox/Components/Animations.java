package de.droidcachebox.Components;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler.Callback;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import de.droidcachebox.R;
import de.droidcachebox.main;

public class Animations
{
	/*
	 * Schaltet die Visibility eines View um und animiert dabei ein
	 * Slidedown/Slideup
	 */
	public static void ToggleViewSlideUp_Down(final View v, Context context, final ScrollView scrollView, final View button)
	{
		ToggleViewSlideUp_Down(v, context, scrollView, button, null);
	}

	public static void ToggleViewSlideUp_Down(final View v, Context context, final ScrollView scrollView, final View button,
			final Callback AnimationReadyCallBack)
	{
		if (v.getVisibility() == View.VISIBLE)
		{
			Animation mShowAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_up);
			v.startAnimation(mShowAnimation);
			mShowAnimation.setAnimationListener(new AnimationListener()
			{
				@Override
				public void onAnimationStart(Animation animation)
				{

				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{
				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					v.setVisibility(View.GONE);
				}
			});

		}
		else
		{
			Animation mShowAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_down);
			v.startAnimation(mShowAnimation);
			v.setVisibility(View.VISIBLE);
			mShowAnimation.setAnimationListener(new AnimationListener()
			{
				@Override
				public void onAnimationStart(Animation animation)
				{

				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{
				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					Rect r = new Rect();
					Point offset = new Point();

					if (scrollView != null)
					{
						scrollView.requestLayout();
						scrollView.getChildVisibleRect(button, r, offset);

						main.Toast(offset.y + " " + scrollView.getScrollY());

						scrollView.scrollTo(0, offset.y + scrollView.getScrollY());
					}

					if (AnimationReadyCallBack != null)
					{
						AnimationReadyCallBack.handleMessage(null);
					}
				}
			});

		}

	}
}
