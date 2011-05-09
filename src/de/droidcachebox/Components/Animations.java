package de.droidcachebox.Components;

import de.droidcachebox.R;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class Animations 
{
	/*
	 * Schaltet die Visibility eines View um und animiert dabei ein Slidedown/Slideup
	 */
	public static void ToggleViewSlideUp_Down(final View v, Context context)
	{
		if(v.getVisibility() == View.VISIBLE)
    	{
    		Animation mShowAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_up);
    		v.startAnimation(mShowAnimation);
    		mShowAnimation.setAnimationListener(new AnimationListener() {
    		    @Override
    		    public void onAnimationStart(Animation animation) {
    		    }

    		    @Override
    		    public void onAnimationRepeat(Animation animation) {
    		    }

    		    @Override
    		    public void onAnimationEnd(Animation animation) {
    		    	v.setVisibility(View.GONE);
    		    }
    		});
    		
    	}
    	else
    	{
    		Animation mShowAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_down);
    		v.startAnimation(mShowAnimation);
    		v.setVisibility(View.VISIBLE);
    	}
	}
}
