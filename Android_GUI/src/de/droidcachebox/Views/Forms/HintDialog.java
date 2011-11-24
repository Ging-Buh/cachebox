package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import CB_Core.GlobalCore;

public class HintDialog extends Activity {
	Button bClose;
	Button bDecode;
	EditText etHint;
	
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
//    	ActivityUtils.onActivityCreateSetTheme(this);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.hint);

        Bundle bundle = getIntent().getExtras();
        String hint  = Global.Rot13((String)bundle.getSerializable("Hint"));

        bClose = (Button) findViewById(R.id.hintdialog_button_close);
        bDecode = (Button) findViewById(R.id.hintdialog_button_decode);
        etHint = (EditText) findViewById(R.id.hintdialog_text);
    
        etHint.setMaxHeight(Sizes.getWindowHeight() - (Sizes.getQuickButtonHeight()*4));
        
        
        etHint.setText(hint);
        bClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();
            }
          });

        bDecode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	etHint.setText(Global.Rot13(etHint.getText().toString()));
            }
          });
        
        // Translations
        bClose.setText(GlobalCore.Translations.Get("close"));
        bDecode.setText(GlobalCore.Translations.Get("decode"));
        this.setTitle(GlobalCore.Translations.Get("hint"));
        
        
        
    }

}
