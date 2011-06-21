package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Events.ProgressChangedEvent;
import de.droidcachebox.Events.ProgresssChangedEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * <h1>ProgressDialog</h1>
 * 
 * <img src="doc-files/ProgressDialog.png" width=146 height=117>
 * 
 * </br>
 * @author Longri
 * 
 * 
 * </br></br>
   <h3>
		Diesem ProgessDialog kann ein Thread zur Abarbeitung und ein Runnable zur Cancel behandlung übergeben werden.</br>
		Er hat drei TextView´s, eine ProgressBar und ein Cancel-Button.</br>
		Über einen EventHandler kann dem ProgressDialog eine Änderung mitgeteilt werden.</br></br> 
		Als erstes brauchen wir den Thrad, der abgearbeitet werden soll.</br>
	</h3>
		Code:</br>
<pre>Thread UploadFieldNotesdThread = <span style=' color: Blue;'>new</span> Thread() 
{
    <span style=' color: Blue;'>public</span> <span style=' color: Blue;'>void</span> run() 
    {
        ProgresssChangedEventList.Call(<span style=' color: Maroon;'>"Was tue ich hier eigentlich?"</span>,<span style=' color: Maroon;'>""</span>, <span style=' color: Maroon;'>0</span>);
        
        <span style=' color: Green;'>//Tue was zu tun ist</span>
        <span style=' color: Blue;'>for</span>(<span style=' color: Blue;'>int</span> i = <span style=' color: Maroon;'>0</span>; i &lt; <span style=' color: Maroon;'>100</span>; i++)
        {
            <span style=' color: Green;'>//Progress status Melden</span>
                ProgresssChangedEventList.Call(<span style=' color: Maroon;'>"Thread Läuft"</span> + String.valueOf(i), i);
                
                <span style=' color: Blue;'>try</span> 
                {
                    <span style=' color: Blue;'>if</span>(ThreadCancel) <span style=' color: Green;'>// wenn im ProgressDialog Cancel gedrückt wurde.</span>
                        <span style=' color: Blue;'>break</span>;
                    
                    Thread.sleep(<span style=' color: Maroon;'>50</span>);
                } 
                <span style=' color: Blue;'>catch</span> (InterruptedException e) 
                {
                    e.printStackTrace();
                }
        }
        <span style=' color: Blue;'>if</span>(!ThreadCancel)
        {
            ProgressDialog.Ready();             <span style=' color: Green;'>//Dem Progress Dialog melden, dass der Thread fertig ist!</span>
            ProgressHandler.post(ProgressReady);    <span style=' color: Green;'>// auf das Ende des Threads reagieren</span>
        }
    }
};
    </pre>
     
    </Br></Br>
    <h3>
    Dann brauchen wir noch das Runnable, das den Cancel-Button behandelt, wenn er denn gedrückt wird und das Flag, das gesetzt wird um den Thread zu Unterbrechen!</Br>
    </h3>
    Code: </Br>
    <pre>
    <span style=' color: Blue;'>private</span> Boolean ThreadCancel = <span style=' color: Maroon;'>false</span>; 
    <span style=' color: Blue;'>final</span> Runnable ProgressCanceld = <span style=' color: Blue;'>new</span> Runnable() 
    {
        <span style=' color: Blue;'>public</span> <span style=' color: Blue;'>void</span> run() 
        {
            ThreadCancel=<span style=' color: Maroon;'>true</span>;
            MessageBox.Show(<span style=' color: Maroon;'>"Progress abgebrochen!"</span>);
        }
    };
    </pre>


	</Br></Br>
     <h3>
     Jetzt noch einen Handler, der das Runnable zur Fertigmeldung aufruft. 	
     </h3>
    Code: </Br>
    	
    <pre>
    <span style=' color: Blue;'>final</span> Handler ProgressHandler = <span style=' color: Blue;'>new</span> Handler();
    <span style=' color: Blue;'>final</span> Runnable ProgressReady = <span style=' color: Blue;'>new</span> Runnable() 
    {
        <span style=' color: Blue;'>public</span> <span style=' color: Blue;'>void</span> run() 
        {
            MessageBox.Show(<span style=' color: Maroon;'>"Progress fertig!"</span>);
        }
    };</pre>
    
      	</Br></Br>
     	<h3>
      	Als letztes kann der ProgressDialog aufgerufen werden und der Name, Thread und Runnable übergeben werden.
      	  </h3>
    Code: </Br>
    <pre>
    <span style=' color: Green;'>// ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.</span>
        ProgressDialog.Show(<span style=' color: Maroon;'>"Upload FieldNotes"</span>, UploadFieldNotesdThread, ProgressCanceld);</pre>
    
    	</Br></Br>
     	<h3>
    Über einen Event Aufruf, kann dem ProgressDialog ein neuer Status zu gewiesen werden.</Br>
	Wobei die Call() Methode überladen ist.</Br>
</h3>
    
    
    
    <pre>ProgresssChangedEventList.Call(String Msg,String ProgressMessage, <span style=' color: Blue;'>int</span> Progress)
</pre>

    <pre>ProgresssChangedEventList.Call(String ProgressMessage, <span style=' color: Blue;'>int</span> Progress)
</pre>
   
    
    <h3>
Die Msg wird über der ProgressBar angezeigt.</Br>
Die ProgressMessage wird unterhlab der ProgressBar angezeigt.</Br>
Der Progress Wert kann Werte von 0- 100 annehmen.</Br>
</Br>
Wird die Call- Methode ohne Msg aufgerufen, wird die Bestehende Msg nicht überschrieben und bleibt stehen.</Br>

    </h3>
      	
 * </Br>
 * 
 * 
 * 
 */
public class ProgressDialog extends Activity implements ProgressChangedEvent,ViewOptionsMenu {
	Context context;
	

	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progress_dialog_layout);
		
		Me=this;		
		context = this.getBaseContext();
				
	
		((TextView) this.findViewById(R.id.title)).setText(titleText);
		CancelButton =(Button) this.findViewById(R.id.cancelButton);
		CancelButton.setText(Global.Translations.Get("cancel"));
		CancelButton.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{	
				//ProgressThread.stop();
				final Handler cancelHandler = new Handler();
				cancelHandler.post(CancelRunable);
				finish();
			}
		});
		
		OnShow();
		
		
	}
	
	
	
	public static void Show (String title, Thread RunThread, Runnable cancel )
	{
		
		if(ProgressThread!=null)
		{
			ProgressThread =null;
			
		}
		
		final Intent mainIntent = new Intent().setClass( main.mainActivity, ProgressDialog.class);
		main.mainActivity.startActivityForResult(mainIntent, 12345);
		ProgressThread=RunThread;	 
		titleText=title;
		CancelRunable=cancel;
		
		
	}
	
	public static void Ready()
	{
		Me.finish();
	}
	
		
    private TextView messageTextView;
    private TextView progressMessageTextView;
    private ProgressBar progressBar;
    private static Thread ProgressThread;
    private static String titleText;
    private static Runnable CancelRunable;
    private Button CancelButton;
    private static ProgressDialog Me;
    
   
    
	 public void setProgress(final String Msg,final String ProgressMessage, final int value)
		{
		 
		 		 
		 if (progressMessageTextView == null)
		 {
			 
		 progressMessageTextView = (TextView) this.findViewById(R.id.ProgressMessage);
		 progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
		 messageTextView = (TextView) this.findViewById(R.id.message);
		 }
		 
		Thread t = new Thread() //UI update Thread
		 {
			    public void run() 
			    {
			        runOnUiThread(new Runnable() 
			        {
			            @Override
			            public void run() 
			            {
			            	progressBar.setProgress(value);
			       		 	progressMessageTextView.setText(ProgressMessage);
			       		 	if(ProgressMessage.equals(""))
			       		 		messageTextView.setText(Msg);
			       		 		
			            }
			        });
			    }
			};

			t.start();
		}	
	
	


	@Override
	public void OnShow() 
	{
		// Registriere Progress Changed Event
        ProgresssChangedEventList.Add(this);
        ProgressThread.start();
                
	}

	@Override
	public void OnHide() 
	{
		// lösche Registrierung Progress Changed Event
        ProgresssChangedEventList.Del(this);	
	}

	@Override
	public void OnFree() {
		
	}

	@Override
	public int GetMenuId() {
	
		return 0;
	}

	

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		
		
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		
		
	}

	@Override
	public int GetContextMenuId() {
		
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		
		return false;
	}

	
	@Override
	public void ProgressChangedEvent(String Msg,String ProgressMessage, int Progress) 
	{
		setProgress(Msg,ProgressMessage, Progress);	
		
	}
    
 
}
