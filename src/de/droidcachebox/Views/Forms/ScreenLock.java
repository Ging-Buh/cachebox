package de.droidcachebox.Views.Forms;

import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;
import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class ScreenLock extends Activity {
	public void onCreate(Bundle savedInstanceState) {
/*		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenlock);
		RelativeLayout layout = (RelativeLayout)findViewById(R.layout.screenlock);
		
		Button button = (Button) findViewById(R.id.screenlock_button);
		button.setText("Unlock");
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();
            }
          });
        
	}

}
