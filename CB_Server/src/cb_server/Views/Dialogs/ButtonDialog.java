package cb_server.Views.Dialogs;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public abstract class ButtonDialog extends Window {
	private static final long serialVersionUID = -6204451157763600712L;
	private VerticalLayout content;
	
	public ButtonDialog(String title) {
		super(title);

		setModal(true);
		setResizable(false);
		content = new VerticalLayout();
		setContent(content);

	}
	

	private void addOKCancelButtons(AbstractLayout content) {
		Button bOK = new Button("OK");
		content.addComponent(bOK);
		Button bCancel = new Button("Abbrechen");
		content.addComponent(bCancel);

		bOK.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 8451830127082999189L;

			@Override
			public void buttonClick(ClickEvent event) {
				okClicked();
				close();
			}
		});

		bCancel.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 6832736272605304637L;

			@Override
			public void buttonClick(ClickEvent event) {
				cancelClicked();
				close();
			}
		});
	}

	@Override
	public void attach() {
		super.attach();
		createContent(content);
		
		HorizontalLayout layoutButtons = new HorizontalLayout();
		content.addComponent(layoutButtons);
		addOKCancelButtons(layoutButtons);
	}
			
	protected abstract void createContent(VerticalLayout content);
	protected abstract void cancelClicked();
	protected abstract void okClicked();

}
