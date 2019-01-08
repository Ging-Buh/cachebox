/* 
 * Copyright (C) 2011-2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cb_server;

import CB_Translation_Base.TranslationEngine.Lang;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Settings.*;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import fi.jasoft.qrcode.QRCode;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

public class SettingsWindow extends Window {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ComboBox langSpinner = new ComboBox();
	final private static SettingsWindow INSTANZ = new SettingsWindow();

	public static SettingsWindow getInstanz() {
		return new SettingsWindow();
	}

	private ArrayList<SettingCategory> Categorys;

	private final VerticalLayout content;
	private VerticalLayout Settingscontent;

	private SettingsWindow() {

		super("Server Settings"); // Set window caption

		this.setWidth(50, Unit.PERCENTAGE);
		this.setHeight(80, Unit.PERCENTAGE);

		center();

		//save act settings for cancel restore
		Config.settings.SaveToLastValue();

		// Some basic content for the window
		content = new VerticalLayout();
		content.setMargin(true);
		setContent(content);

		addSaveCancelButtons();

		fillContent();

	}

	private void addSaveCancelButtons() {
		HorizontalLayout hl = new HorizontalLayout();
		final Button btnSave = new Button(Translation.Get("save".hashCode()));
		final Button btnCancel = new Button(Translation.Get("cancel".hashCode()));

		hl.addComponent(btnSave);
		hl.addComponent(btnCancel);

		btnCancel.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -4799987364890297976L;

			@Override
			public void buttonClick(ClickEvent event) {
				Config.settings.LoadFromLastValue();
				SettingsWindow.this.close();
			}
		});

		btnSave.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -878673538684730570L;

			@Override
			public void buttonClick(ClickEvent event) {
				String lang = (String) langSpinner.getValue();

				for (Lang tmp : Translation.GetLangs(SettingsClass.LanguagePath.getValue())) {
					if (lang.equals(tmp.Name)) {
						CB_UI_Base_Settings.Sel_LanguagePath.setValue(tmp.Path);
						try {
							Translation.LoadTranslation(tmp.Path);
						} catch (Exception e) {
							try {
								Translation.LoadTranslation(CB_UI_Base_Settings.Sel_LanguagePath.getDefaultValue());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						break;
					}

				}

				Config.settings.WriteToDB();
				Config.settings.SaveToLastValue();

				SettingsWindow.this.close();
			}
		});

		content.addComponent(hl);
	}

	private void fillContent() {

		if (Settingscontent != null) {
			content.removeComponent(Settingscontent);
			Settingscontent = null;
		}

		Settingscontent = new VerticalLayout();
		content.addComponent(Settingscontent);

		InetAddress addr;
		QRCode code = null;
		try {
			addr = InetAddress.getLocalHost();

			//Getting IPAddress of localhost - getHostAddress return IP Address
			// in textual format
			String ipAddress = addr.getHostAddress();

			ipAddress = "";
			// Network Interfaces nach IPv4 Adressen durchsuchen
			try {
				Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
				for (NetworkInterface netint : Collections.list(nets)) {
					Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
					for (InetAddress inetAddress : Collections.list(inetAddresses)) {
						if (inetAddress.isLoopbackAddress())
							continue;
						if (inetAddress instanceof Inet4Address) {
							System.out.println("InetAddress: " + inetAddress);
							if (ipAddress.length() > 0) {
								ipAddress += ";";
							}
							ipAddress += inetAddress;
						}
					}
				}

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			code = new QRCode();
			code.setWidth(150, Unit.PIXELS);
			code.setHeight(150, Unit.PIXELS);
			code.setValue(ipAddress);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TabSheet tabSheet = new TabSheet();
		tabSheet.setWidth(100, Unit.PERCENTAGE);
		tabSheet.setHeight(100, Unit.PERCENTAGE);
		Settingscontent.addComponent(tabSheet);

		VerticalLayout lay = new VerticalLayout();

		// add Lang Spinner

		langSpinner = new ComboBox();
		langSpinner.setCaption("select Lang");

		for (Lang lang : Translation.GetLangs(SettingsClass.LanguagePath.getValue())) {
			langSpinner.addItem(lang.Name);
			langSpinner.setItemCaption(lang.Name, lang.Name);
		}

		langSpinner.setValue(Translation.getLangId());
		langSpinner.setNullSelectionAllowed(false);
		lay.addComponent(langSpinner);

		lay.addComponent(code);
		SettingsLinearLayoutPanel info = new SettingsLinearLayoutPanel();

		info.setContent(lay, 400);

		tabSheet.addTab(info, "Info");

		// Categorie List zusammen stellen

		if (Categorys == null) {
			Categorys = new ArrayList<SettingCategory>();
		}

		Categorys.clear();
		SettingCategory[] tmp = SettingCategory.values();
		for (SettingCategory item : tmp) {
			if (item != SettingCategory.Button) {
				Categorys.add(item);
			}

		}

		Iterator<SettingCategory> iteratorCat = Categorys.iterator();
		if (iteratorCat != null && iteratorCat.hasNext()) {

			ArrayList<SettingBase<?>> SortedSettingList = new ArrayList<SettingBase<?>>();

			for (Iterator<SettingBase<?>> it = Config.settings.iterator(); it.hasNext();) {
				SettingBase<?> setting = it.next();

				if (setting.getModus() != SettingModus.NEVER && (setting.getUsage() == SettingUsage.ALL || setting.getUsage() == SettingUsage.CBS)) {
					SortedSettingList.add(setting);
				}
			}

			do {
				int position = 0;

				SettingCategory cat = iteratorCat.next();

				// add Cat eintr�ge

				lay = new VerticalLayout();
				int entryCount = 0;

				if (cat == SettingCategory.Login) {
					final Component view = getLogInButton(position++);
					lay.addComponent(view);
				}

				// int layoutHeight = 0;
				for (Iterator<SettingBase<?>> it = SortedSettingList.iterator(); it.hasNext();) {
					SettingBase<?> settingItem = it.next();
					if (settingItem.getCategory().name().equals(cat.name())) {

						if ((settingItem.getModus() == SettingModus.NORMAL) && (settingItem.getModus() != SettingModus.NEVER)) {

							final Component view = getView(settingItem, position++);

							if (view == null)
								continue;

							lay.addComponent(view);
							entryCount++;
							Config.settings.indexOf(settingItem);

						}

					}
				}

				if (entryCount > 0) {

					SettingsLinearLayoutPanel catPanel = new SettingsLinearLayoutPanel();

					catPanel.setContent(lay, 400);

					tabSheet.addTab(catPanel, cat.name());

					//					addControlToLinearLayout(lay, 100);

				}

			} while (iteratorCat.hasNext());

		}

	}

	private Component getLogInButton(int i) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.Button button = new com.vaadin.ui.Button();
		button.setCaption("Get API Key");

		button.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -1417363407758383092L;

			@Override
			public void buttonClick(ClickEvent event) {
				LogInWindow sub = LogInWindow.getInstanz();

				if (!UI.getCurrent().getWindows().contains(sub))

					// Add it to the root component
					UI.getCurrent().addWindow(sub);
			}
		});

		box.addComponent(button);

		return box;
	}

	private void addControlToLinearLayout(Component view, float itemMargin) {
		Settingscontent.addComponent(view);
	}

	private Component getView(SettingBase<?> SB, int BackgroundChanger) {
		if (SB instanceof SettingBool) {
			return getBoolView((SettingBool) SB, BackgroundChanger);
		} else if (SB instanceof SettingIntArray) {
			return getIntArrayView((SettingIntArray) SB, BackgroundChanger);
		} else if (SB instanceof SettingStringArray) {
			return getStringArrayView((SettingStringArray) SB, BackgroundChanger);
		} else if (SB instanceof SettingTime) {
			return getTimeView((SettingTime) SB, BackgroundChanger);
		} else if (SB instanceof SettingInt) {
			return getIntView((SettingInt) SB, BackgroundChanger);
		} else if (SB instanceof SettingDouble) {
			return getDblView((SettingDouble) SB, BackgroundChanger);
		} else if (SB instanceof SettingFloat) {
			return getFloatView((SettingFloat) SB, BackgroundChanger);
		} else if (SB instanceof SettingFolder) {
			return getFolderView((SettingFolder) SB, BackgroundChanger);
		} else if (SB instanceof SettingFile) {
			return getFileView((SettingFile) SB, BackgroundChanger);
		} else if (SB instanceof SettingEnum) {
			return getEnumView((SettingEnum<?>) SB, BackgroundChanger);
		} else if (SB instanceof SettingEncryptedString) {
			return getEncryptedStringView((SettingEncryptedString) SB, BackgroundChanger);
		} else if (SB instanceof SettingString) {
			return getStringView((SettingString) SB, BackgroundChanger);
		} else if (SB instanceof SettingsAudio) {
			return getAudioView((SettingsAudio) SB, BackgroundChanger);
		}

		return null;
	}

	private Component getAudioView(SettingsAudio sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.Label label = new com.vaadin.ui.Label();
		label.setCaption(sB.getName());
		box.addComponent(label);

		return box;
	}

	private Component getStringView(final SettingString sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		box.setWidth(100, Unit.PERCENTAGE);
		com.vaadin.ui.TextField input = new TextField(sB.getName(), String.valueOf(sB.getValue()));
		input.setWidth(50, Unit.PERCENTAGE);
		input.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = -634498493292006581L;

			@Override
			public void textChange(TextChangeEvent event) {
				sB.setValue(event.getText());
			}
		});

		box.addComponent(input);
		return box;
	}

	private Component getEncryptedStringView(final SettingEncryptedString sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		box.setWidth(100, Unit.PERCENTAGE);
		com.vaadin.ui.TextField input = new TextField(sB.getName(), String.valueOf(sB.getEncryptedValue()));
		input.setWidth(50, Unit.PERCENTAGE);
		input.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = -634498493292006581L;

			@Override
			public void textChange(TextChangeEvent event) {
				sB.setEncryptedValue(event.getText());
			}
		});

		box.addComponent(input);
		return box;
	}

	private Component getEnumView(SettingEnum<?> sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.Label label = new com.vaadin.ui.Label();
		label.setCaption(sB.getName());
		box.addComponent(label);

		return box;
	}

	private Component getFileView(final SettingFile sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		box.setWidth(100, Unit.PERCENTAGE);
		com.vaadin.ui.TextField input = new TextField(sB.getName(), String.valueOf(sB.getValue()));
		input.setWidth(50, Unit.PERCENTAGE);
		input.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = -634498493292006581L;

			@Override
			public void textChange(TextChangeEvent event) {
				sB.setValue(event.getText());
			}
		});

		box.addComponent(input);

		return box;
	}

	private Component getFolderView(final SettingFolder sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		box.setWidth(100, Unit.PERCENTAGE);
		com.vaadin.ui.TextField input = new TextField(sB.getName(), String.valueOf(sB.getValue()));
		input.setWidth(50, Unit.PERCENTAGE);
		input.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = -634498493292006581L;

			@Override
			public void textChange(TextChangeEvent event) {
				sB.setValue(event.getText());
			}
		});

		box.addComponent(input);
		return box;
	}

	private Component getFloatView(final SettingFloat sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.TextField input = new TextField(sB.getName(), String.valueOf(sB.getValue()));
		input.setWidth(50, Unit.PERCENTAGE);
		input.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = -634498493292006581L;

			@Override
			public void textChange(TextChangeEvent event) {
				sB.setValue(Float.parseFloat(event.getText()));
			}
		});

		box.addComponent(input);
		return box;
	}

	private Component getDblView(final SettingDouble sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.TextField input = new TextField(sB.getName(), String.valueOf(sB.getValue()));
		input.setWidth(50, Unit.PERCENTAGE);
		input.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = -634498493292006581L;

			@Override
			public void textChange(TextChangeEvent event) {
				sB.setValue(Double.parseDouble(event.getText()));
			}
		});

		box.addComponent(input);
		return box;
	}

	private Component getIntView(final SettingInt sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.TextField input = new TextField(sB.getName(), String.valueOf(sB.getValue()));
		input.setWidth(50, Unit.PERCENTAGE);
		input.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = -634498493292006581L;

			@Override
			public void textChange(TextChangeEvent event) {

				int newValue = Integer.parseInt(event.getText());
				sB.setValue(newValue);
			}
		});

		box.addComponent(input);
		return box;
	}

	private Component getTimeView(SettingTime sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.Label label = new com.vaadin.ui.Label();
		label.setCaption(sB.getName());
		box.addComponent(label);

		return box;
	}

	private Component getStringArrayView(SettingStringArray sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.Label label = new com.vaadin.ui.Label();
		label.setCaption(sB.getName());
		box.addComponent(label);

		return box;
	}

	private Component getIntArrayView(SettingIntArray sB, int backgroundChanger) {
		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.Label label = new com.vaadin.ui.Label();
		label.setCaption(sB.getName());
		box.addComponent(label);

		return box;
	}

	private Component getBoolView(SettingBool sB, int backgroundChanger) {

		com.vaadin.ui.HorizontalLayout box = new HorizontalLayout();
		com.vaadin.ui.Label label = new com.vaadin.ui.Label();
		label.setCaption(sB.getName());
		box.addComponent(label);

		return box;
	}

}
