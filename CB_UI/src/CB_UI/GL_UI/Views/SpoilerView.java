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

import org.slf4j.LoggerFactory;

import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Activitys.ImageActivity;
import CB_UI_Base.GL_UI.Controls.GalleryBigItem;
import CB_UI_Base.GL_UI.Controls.GalleryItem;
import CB_UI_Base.GL_UI.Controls.GalleryView;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.ImageLoader;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.FileFactory;

public class SpoilerView extends CB_View_Base {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(SpoilerView.class);
	private final static int MAX_THUMB_WIDTH = 500;
	private final static int MAX_OVERVIEW_THUMB_WIDTH = 240;

	Cache actCache;
	CB_List<GalleryBigItem> bigItems = new CB_List<GalleryBigItem>();
	CB_List<GalleryItem> overviewItems = new CB_List<GalleryItem>();
	GalleryView gallery;
	GalleryView galleryOverwiew;
	boolean forceReload = false;

	public SpoilerView(CB_RectF rec, String Name) {
		super(rec, Name);

		CB_RectF gr = rec.copy();
		gr.setHeight(rec.getHeight() * 0.85f);

		gallery = new GalleryView(gr, "gallery") {
			@Override
			public void snapIn(int idx) {
				galleryOverwiew.setSelection(idx);
				galleryOverwiew.scrollItemToCenter(idx);
			}
		};
		gallery.setZeroPos();
		this.addChild(gallery);

		CB_RectF or = gr.copy();
		or.setHeight(rec.getHeight() - gr.getHeight());
		galleryOverwiew = new GalleryView(or, "overview");
		galleryOverwiew.setPos(0, this.getHeight() - or.getHeight());
		galleryOverwiew.showSelectedItemCenter(true);

		this.addChild(galleryOverwiew);
	}

	public void ForceReload() {
		forceReload = true;
		actCache = null;
		gallery.setBaseAdapter(new GalaryImageAdapter());
		galleryOverwiew.setBaseAdapter(new OverviewImageAdapter());
	}

	@Override
	public void onShow() {
		Log.info(log, "Start onShow");
		if (GlobalCore.isSetSelectedCache()) {

			if (!forceReload && GlobalCore.getSelectedCache().equals(actCache)) {
				return;
			}

			forceReload = false;

			actCache = GlobalCore.getSelectedCache();

			if (actCache.hasSpoiler()) {
				Log.info(log, "has Spoiler.");

				GalleryItem firstItem = null;
				synchronized (bigItems) {
					bigItems.clear();
					overviewItems.clear();

					CB_RectF orItemRec = galleryOverwiew.copy();
					orItemRec.setWidth(galleryOverwiew.getHeight());

					Log.info(log, "make images");
					for (int i = 0, n = actCache.getSpoilerRessources().size(); i < n; i++) {
						ImageEntry imageEntry = actCache.getSpoilerRessources().get(i);
						Log.info(log, "Image Nr.: " + i + " from " + imageEntry.LocalPath);

						ImageLoader loader = new ImageLoader(true); // image loader with thumb
						loader.setThumbWidth(MAX_THUMB_WIDTH, "");
						loader.setImage(imageEntry.LocalPath);
						String label;
						if (imageEntry.Name.length() > 0) {
							// label = imageEntry.Name;
							label = FileIO.GetFileNameWithoutExtension(imageEntry.LocalPath);
						} else {
							label = FileIO.GetFileNameWithoutExtension(imageEntry.LocalPath);
						}
						if (imageEntry.Description.length() > 0)
							label = label + "\n" + imageEntry.Description;
						GalleryBigItem item = new GalleryBigItem(gallery.copy(), i, loader, label);
						item.setOnDoubleClickListener(onItemClickListener);
						bigItems.add(item);

						ImageLoader overviewloader = new ImageLoader(true); // image loader with thumb
						overviewloader.setThumbWidth(MAX_OVERVIEW_THUMB_WIDTH, FileFactory.THUMB_OVERVIEW);
						overviewloader.setImage(imageEntry.LocalPath);
						GalleryItem overviewItem = new GalleryItem(orItemRec, i, loader);
						overviewItem.setOnClickListener(onItemSelectClickListener);
						if (firstItem == null)
							firstItem = overviewItem;
						overviewItems.add(overviewItem);
					}
				}
				Log.info(log, "Images loaded");
				gallery.setBaseAdapter(new GalaryImageAdapter());
				galleryOverwiew.setBaseAdapter(new OverviewImageAdapter());

				//select first item
				if (firstItem != null) {
					//	    gallery.scrollToItem(0);
					galleryOverwiew.setSelection(0);
					galleryOverwiew.scrollItemToCenter(0);
				}
			} else {
				bigItems.clear();
				overviewItems.clear();
				gallery.reloadItems();
				galleryOverwiew.reloadItems();
			}
		}
		Log.info(log, "End onShow");
	}

	@Override
	public void onResized(CB_RectF rec) {
		super.onResized(rec);

		CB_RectF gr = rec.copy();
		gr.setHeight(rec.getHeight() - galleryOverwiew.getHeight());
		gallery.setRec(gr);
		gallery.setZeroPos();

		galleryOverwiew.setPos(0, this.getHeight() - galleryOverwiew.getHeight());

		//resize gallery items
		for (GalleryBigItem item : bigItems) {
			item.setRec(gr);
		}
		gallery.reloadItemsNow();
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

	}

	public class GalaryImageAdapter implements Adapter {
		public GalaryImageAdapter() {
		}

		@Override
		public int getCount() {
			synchronized (bigItems) {
				if (bigItems == null)
					return 0;
				return bigItems.size();
			}
		}

		@Override
		public ListViewItemBase getView(int position) {
			synchronized (bigItems) {
				if (bigItems == null)
					return null;
				if (bigItems.size() == 0)
					return null;
				return bigItems.get(position);
			}
		}

		@Override
		public float getItemSize(int position) {
			synchronized (bigItems) {
				if (bigItems == null)
					return 0;
				if (bigItems.size() == 0)
					return 0;
				GalleryBigItem item = bigItems.get(position);
				if (item != null)
					return item.getWidth();
				return 0;
			}
		}
	}

	public class OverviewImageAdapter implements Adapter {
		public OverviewImageAdapter() {
		}

		@Override
		public int getCount() {
			synchronized (overviewItems) {
				if (overviewItems == null)
					return 0;
				return overviewItems.size();
			}
		}

		@Override
		public ListViewItemBase getView(int position) {
			synchronized (overviewItems) {
				if (overviewItems == null)
					return null;
				if (overviewItems.size() == 0)
					return null;
				return overviewItems.get(position);
			}
		}

		@Override
		public float getItemSize(int position) {
			synchronized (overviewItems) {
				if (overviewItems == null)
					return 0;
				if (overviewItems.size() == 0)
					return 0;
				GalleryItem item = overviewItems.get(position);
				if (item != null)
					return item.getWidth();
				return 0;
			}
		}
	}

	private final OnClickListener onItemClickListener = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			Image selectionImage = ((GalleryBigItem) v).getImage();

			String path = selectionImage.getImageLoader().getOriginalImagePath();

			Image img = new Image(SpoilerView.this, "Image for Activity", true);
			img.setImage(path);

			ImageActivity ac = new ImageActivity(img);
			ac.show();
			return true;
		}
	};

	private final OnClickListener onItemSelectClickListener = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			final int idx = ((GalleryItem) v).getIndex();

			gallery.notifyDataSetChanged();

			gallery.scrollToItem(idx);
			galleryOverwiew.setSelection(idx);
			galleryOverwiew.scrollItemToCenter(idx);
			return true;
		}
	};

	public String getSelectedFilePath() {
		String file = null;
		try {
			file = ((GalleryBigItem) gallery.getSelectedItem()).getImage().getImageLoader().getImagePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

}
