/* 
 * Copyright (C) 2015 team-cachebox.de
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
package CB_UI.GL_UI.Views;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import CB_Core.Attributes;
import CB_Core.Database;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Types.Cache;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.CacheInfo;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.Global;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Handler;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.Controls.html.HtmlView;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.Geometry.Line;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_Utils.Log.Log;

/**
 * @author Longri
 */
public class DescriptionView extends CB_View_Base {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(DescriptionView.class);
	final static String STRING_POWERD_BY = "Powerd by Geocaching Live";
	final static String BASIC = "Basic";
	final static String PREMIUM = "Premium";
	final static String BASIC_LIMIT = "3";
	final static String PREMIUM_LIMIT = "6000";
	private GlyphLayout layout;

	private Cache aktCache;
	private final LinkedList<String> NonLocalImages = new LinkedList<String>();
	private final LinkedList<String> NonLocalImagesUrl = new LinkedList<String>();
	private final int downloadTryCounter = 0;

	private CacheListViewItem cacheInfo;
	private Button downloadButton;
	private Label MessageLabel, PowerdBy;
	private Image LiveIcon;
	private PolygonDrawable Line;
	private float margin;
	private boolean forceReload = false;

	private final HtmlView htmlView;

	public DescriptionView(CB_RectF rec, String Name) {
		super(rec, Name);
		htmlView = new HtmlView(this);
		htmlView.setZeroPos();
		this.addChild(htmlView);
		//Log.info(log, "DescriptionView create");
		registerSkinChangedEvent();
	}

	final static org.slf4j.Logger htmllog = LoggerFactory.getLogger("HTML_PARSER");

	@Override
	public void onShow() {
		super.onShow();
		//Log.info(log, "DescriptionView onShow");
		margin = GL_UISizes.margin;

		Cache sel = GlobalCore.getSelectedCache();
		if (sel != null) {
			setCache(sel, forceReload);
		}
		forceReload = false;
		Timer t = new Timer();
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				DescriptionView.this.onResized(DescriptionView.this);
			}
		};
		t.schedule(tt, 70);
	}

	public void toggleTxt_Html() {
		htmlView.setTextOnly(!htmlView.getTextOnly());
		setCache(aktCache, true);
	}

	private void setCache(Cache cache, boolean force) {
		if (cache == null)
			return;

		if (cache.equals(aktCache) && !force) {
			//Log.info(log, "setCache " + cache.getGcCode() + " no change.");
			return;
		}

		/*
		String logstr = "setCache " + cache.getGcCode();
		if (aktCache != null) {
		logstr = logstr + " old:" + aktCache.getGcCode();
		}
		logstr = logstr + " must:" + force;
		Log.info(log, logstr);
		*/

		aktCache = cache;

		if (cacheInfo != null)
			this.removeChild(cacheInfo);
		cacheInfo = new CacheListViewItem(UiSizes.that.getCacheListItemRec().asFloat(), 0, cache);
		cacheInfo.setY(this.getHeight() - cacheInfo.getHeight());
		cacheInfo.info.setViewMode(CacheInfo.VIEW_MODE_DESCRIPTION);

		if (!Global.isTab)
			this.addChild(cacheInfo);
		resetUi();
		if (cache.isLive() || cache.getApiStatus() == 1) {
			showDownloadButton();
		}

		NonLocalImages.clear();
		NonLocalImagesUrl.clear();
		String cachehtml = Database.GetShortDescription(cache);
		if (cachehtml.isEmpty()) {
			cachehtml = Database.GetDescription(cache);
		} else {
			cachehtml += "<br/><hr/><br/>" + Database.GetDescription(cache);
		}

		String html = "";
		if (cache.getApiStatus() == 1)// GC.com API lite
		{ // Load Standard HTML
			String nodesc = Translation.Get("GC_NoDescription");
			html = "</br>" + nodesc + "</br></br></br><form action=\"download\"><input type=\"submit\" value=\" " + Translation.Get("GC_DownloadDescription") + " \"></form>";
		} else {
			html = DescriptionImageGrabber.ResolveImages(cache, cachehtml, false, NonLocalImages, NonLocalImagesUrl);

			if (!Config.DescriptionNoAttributes.getValue())
				html = getAttributesHtml(cache) + html;

			// add 2 empty lines so that the last line of description can be selected with the markers
			html += "</br></br>";
		}

		final String FinalHtml = html;

		try {
			htmlView.showHtml(FinalHtml);
		} catch (Exception e) {
			e.printStackTrace();
			Log.info(htmllog, cache.toString() + " " + e.toString());
		}

		// Falls nicht geladene Bilder vorliegen und eine Internetverbindung
		// erlaubt ist, diese laden und Bilder erneut auflösen
		if (NonLocalImagesUrl.size() > 0) {
			downloadThread = new Thread() {
				@Override
				public void run() {

					if (downloadTryCounter > 0) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Log.err(log, "DescriptionViewControl.setCache()", "Thread.sleep fehler", e);
							e.printStackTrace();
						}
					}

					while (NonLocalImagesUrl != null && NonLocalImagesUrl.size() > 0) {
						String local, url;
						local = NonLocalImages.poll();
						url = NonLocalImagesUrl.poll();

						try {
							DescriptionImageGrabber.Download(url, local);
						} catch (Exception e) {
							Log.err(log, "DescriptionViewControl.setCache()", "downloadThread run()", e);
						}
					}
					downloadReadyHandler.post(downloadComplete);

				}
			};
			downloadThread.start();
		}

		if (cache != null) {
			cache.ReloadSpoilerRessources();
		}

	}

	public Cache getCache() {
		return aktCache;
	}

	@Override
	public void onResized(CB_RectF rec) {
		super.onResized(rec);
		// onShow();
		if (cacheInfo != null)
			cacheInfo.setY(this.getHeight() - cacheInfo.getHeight());
		layout();

		float infoHeight = -(UiSizes.that.getInfoSliderHeight());
		if (cacheInfo != null && !Global.isTab)
			infoHeight += cacheInfo.getHeight();
		infoHeight += margin * 2;

		try {
			if (htmlView != null && cacheInfo != null)
				htmlView.setHeight(this.getHeight() - (cacheInfo.getHeight() + (margin * 2)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			CB_RectF world = this.getWorldRec();
			PlatformConnector.setContentSize((int) world.getX(), (int) ((GL_UISizes.SurfaceSize.getHeight() - (world.getMaxY() - infoHeight))), (int) (GL_UISizes.SurfaceSize.getWidth() - world.getMaxX()), (int) world.getY());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onHide() {
		super.onHide();
	}

	@Override
	protected void Initial() {

	}

	@Override
	protected void SkinIsChanged() {
		setCache(aktCache, true);
	}

	private void resetUi() {
		if (MessageLabel != null) {
			this.removeChildsDirekt(MessageLabel);
			MessageLabel.dispose();
			MessageLabel = null;
		}
		if (downloadButton != null) {
			this.removeChildsDirekt(downloadButton);
			downloadButton.dispose();
			downloadButton = null;
		}
		if (LiveIcon != null) {
			this.removeChildsDirekt(LiveIcon);
			LiveIcon.dispose();
			LiveIcon = null;
		}
		if (PowerdBy != null) {
			this.removeChildsDirekt(PowerdBy);
			PowerdBy.dispose();
			PowerdBy = null;
		}
	}

	private void layout() {
		if (LiveIcon != null) {
			float IconX = this.getHalfWidth() - LiveIcon.getHalfWidth();
			float IconY = this.cacheInfo.getY() - (LiveIcon.getHeight() + margin);
			LiveIcon.setPos(IconX, IconY);

			if (PowerdBy != null) {
				PowerdBy.setY(LiveIcon.getY() - (PowerdBy.getHeight() + margin));

				if (MessageLabel != null) {
					MessageLabel.setY(this.PowerdBy.getY() - (MessageLabel.getHeight() + (margin * 3)));
					MessageLabel.setX(this.getHalfWidth() - MessageLabel.getHalfWidth());
				}
				downloadButton.setX(this.getHalfWidth() - downloadButton.getHalfWidth());
				downloadButton.setY(margin);
			}
		}
		Line = null;
	}

	private void showDownloadButton() {
		final Thread getLimitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int result = CB_Core.Api.GroundspeakAPI.GetCacheLimits(null);
				if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
					GL.that.Toast(ConnectionError.INSTANCE);
					return;
				}
				if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
					GL.that.Toast(ApiUnavailable.INSTANCE);
					return;
				}
				resetUi();
				showDownloadButton();
			}
		});

		if (CB_Core.Api.GroundspeakAPI.CachesLeft == -1)
			getLimitThread.start();

		float contentWidth = this.getWidth() * 0.95f;

		LiveIcon = new Image(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight, "LIVE-ICON", false);
		LiveIcon.setSprite(Sprites.LiveBtn.get(0), false);

		this.addChild(LiveIcon);

		PowerdBy = new Label(this.name + " PowerdBy", this);

		if (layout == null)
			layout = new GlyphLayout();
		layout.setText(Fonts.getNormal(), STRING_POWERD_BY);
		PowerdBy.setHeight(layout.height + (margin * 2));

		PowerdBy.setFont(Fonts.getNormal()).setHAlignment(HAlignment.CENTER);

		PowerdBy.setWrappedText(STRING_POWERD_BY);
		this.addChild(PowerdBy);

		MessageLabel = new Label(this.name + " MessageLabel", this);
		MessageLabel.setWidth(contentWidth);
		MessageLabel.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
		MessageLabel.setHeight(this.getHalfHeight());

		MessageLabel.setWrappedText(getMessage());
		this.addChild(MessageLabel);

		downloadButton = new Button(Translation.Get("DownloadDetails"));
		downloadButton.setWidth(this.getWidth() * 0.8f);

		this.addChild(downloadButton);

		downloadButton.setOnClickListener(downloadClicked);

		if (CB_Core.Api.GroundspeakAPI.CachesLeft <= 0)
			downloadButton.disable();
		layout();
	}

	final static OnClickListener downloadClicked = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			GL.that.RunOnGL(new IRunOnGL() {
				@Override
				public void run() {
					TabMainView.actionShowDescriptionView.ReloadSelectedCache();
				}
			});
			return true;
		}
	};

	@Override
	public void render(Batch batch) {
		super.render(batch);

		if (PowerdBy != null) {
			if (Line == null) {
				float strokeWidth = 3 * UI_Size_Base.that.getScale();

				Line l1 = new Line(margin, PowerdBy.getY() - margin, this.getWidth() - margin, PowerdBy.getY() - margin);

				Quadrangle q1 = new Quadrangle(l1, strokeWidth);

				GL_Paint paint = new GL_Paint();
				paint.setGLColor(Color.DARK_GRAY);
				Line = new PolygonDrawable(q1.getVertices(), q1.getTriangles(), paint, this.getWidth(), this.getHeight());

				l1.dispose();

				q1.dispose();

			}

			Line.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);
		}
	}

	private String getMessage() {
		StringBuilder sb = new StringBuilder();
		boolean basic = CB_Core.Api.GroundspeakAPI.GetMembershipType(null) == 1;
		String MemberType = basic ? BASIC : PREMIUM;
		String limit = basic ? BASIC_LIMIT : PREMIUM_LIMIT;
		String actLimit = Integer.toString(CB_Core.Api.GroundspeakAPI.CachesLeft - 1);

		if (CB_Core.Api.GroundspeakAPI.CachesLeft == -1) {
			actLimit = "?";
		}

		sb.append(Translation.Get("LiveDescMessage", MemberType, limit));
		sb.append(Global.br);
		sb.append(Global.br);
		if (CB_Core.Api.GroundspeakAPI.CachesLeft > 0)
			sb.append(Translation.Get("LiveDescAfter", actLimit));

		if (CB_Core.Api.GroundspeakAPI.CachesLeft == 0) {
			sb.append(Translation.Get("LiveDescLimit"));
			sb.append(Global.br);
			sb.append(Global.br);
			if (basic)
				sb.append(Translation.Get("LiveDescLimitBasic"));

		}

		return sb.toString();

	}

	final Handler downloadReadyHandler = new Handler();
	Thread downloadThread;

	final Runnable downloadComplete = new Runnable() {
		@Override
		public void run() {
			if (downloadTryCounter < 10) { // nur 10 Download versuche zu lassen
				setCache(aktCache, false);
			}
		}
	};

	public static String getAttributesHtml(Cache cache) {
		StringBuilder sb = new StringBuilder();
		try {
			Iterator<Attributes> attrs = cache.getAttributes().iterator();

			if (attrs == null || !attrs.hasNext())
				return "";

			do {
				Attributes attribute = attrs.next();
				String src = Config.mWorkPath + "/data/Attributes/" + attribute.getImageName() + ".png";
				// File result = FileFactory.createFile(src);
				// src = result.getAbsolutePath();
				sb.append("<input name=\"Button\" type=\"image\" src=\"file://" + src + "\" value=\" " + attribute.getImageName() + " \">");
			} while (attrs.hasNext());

			if (sb.length() > 0)
				sb.append("<br><hr><br><br><br>");
			return sb.toString();
		} catch (Exception ex) {
			Log.err(log, "getAttributesHtml(" + cache.getGcCode() + "):", ex);
			return "";
		}
	}

	public void forceReload() {
		forceReload = true;
	}

	public boolean getTxtOnly() {
		return this.htmlView.getTextOnly();
	}
}
