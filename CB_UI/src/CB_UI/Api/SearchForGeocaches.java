package CB_UI.Api;

import CB_Core.Api.SearchForGeocaches_Core;
import CB_Core.Types.Cache;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IReadyListner;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;

public class SearchForGeocaches extends SearchForGeocaches_Core {
    private static SearchForGeocaches instance = null;

    public static SearchForGeocaches getInstance() {
	if (instance == null) {
	    instance = new SearchForGeocaches();
	}
	return instance;
    }

    @Override
    protected void actualizeSpoilerOfActualCache(Cache cache) {
	super.actualizeSpoilerOfActualCache(cache);
	// Spoiler aktualisieren
	// wenn der aktuelle Cache über diese API aktualisiert wird dann müssen hier dessen Spoiler erneut geladen werden
	// damit die Anzeige in der Description und SpoilerView aktualisiert wird
	if (GlobalCore.ifCacheSelected()) {
	    if (GlobalCore.getSelectedCache().getGcCode().equals(cache.getGcCode())) {
		GlobalCore.ImportSpoiler().setReadyListner(new IReadyListner() {
		    @Override
		    public void isReady() {
			GL.that.RunOnGL(new IRunOnGL() {
			    @Override
			    public void run() {
				if (GlobalCore.getSelectedCache() != null)
				    GlobalCore.getSelectedCache().ReloadSpoilerRessources();
			    }
			});
		    }
		});
	    }
	}

    }

    @Override
    protected void showToastConnectionError() {
	super.showToastConnectionError();
	// hier im Core nichts machen da hier keine UI vorhanden ist
	GL.that.Toast(ConnectionError.INSTANCE);
    }
}
