package cb_server.Views;

import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;
import cb_server.CB_ServerUI;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class ProgressView extends Panel implements ProgressChangedEvent {
	private static final long serialVersionUID = 7231123705947964790L;
	private Label lProgress;
	private HorizontalLayout layout;

	public ProgressView() {
		layout = new HorizontalLayout();
		this.setContent(layout);
		layout.setSizeFull();

		lProgress = new Label();
		lProgress.setCaption("Hallo");
		layout.addComponent(lProgress);

		ProgresssChangedEventList.Add(this);
	}

	@Override
	public void detach() {
		ProgresssChangedEventList.Remove(this);
		super.detach();
	}

	private long lastUpdate = 0;
	@Override
	public void ProgressChangedEventCalled(String Message, String ProgressMessage, int Progress) {
		lProgress.setCaption(Message + " - " + ProgressMessage + " - " + Progress + "%");
		if (lastUpdate > System.currentTimeMillis() - 1000) return;
		HasComponents parent = this;
		while (parent != null) {
			parent = parent.getParent();
			if (parent instanceof CB_ServerUI) {
				((CB_ServerUI) parent).pushChangedContent();
				break;
			}
		}
		lastUpdate = System.currentTimeMillis();
	}
}
