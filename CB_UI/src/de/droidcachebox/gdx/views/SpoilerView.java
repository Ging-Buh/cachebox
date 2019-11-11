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
package de.droidcachebox.gdx.views;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.ImageDAO;
import de.droidcachebox.database.ImageEntry;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.activities.ImageActivity;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;

import java.util.ArrayList;

public class SpoilerView extends CB_View_Base {
    private final static int MAX_THUMB_WIDTH = 500;
    private final static int MAX_OVERVIEW_THUMB_WIDTH = 240;
    private static SpoilerView that;
    private final CB_List<GalleryBigItem> bigItems = new CB_List<>();
    private final CB_List<GalleryItem> overviewItems = new CB_List<>();
    private Cache actCache;
    private GalleryView gallery;
    private GalleryView galleryOverwiew;
    private boolean forceReload = false;
    private ImageDAO imageDAO = new ImageDAO();

    private SpoilerView() {
        super(ViewManager.leftTab.getContentRec(), "SpoilerView");

        CB_RectF gr = this.copy();
        gr.setHeight(this.getHeight() * 0.85f);

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
        or.setHeight(this.getHeight() - gr.getHeight());
        galleryOverwiew = new GalleryView(or, "overview");
        galleryOverwiew.setPos(0, this.getHeight() - or.getHeight());
        galleryOverwiew.showSelectedItemCenter(true);

        this.addChild(galleryOverwiew);
    }

    public static SpoilerView getInstance() {
        if (that == null) that = new SpoilerView();
        return that;
    }

    public void ForceReload() {
        forceReload = true;
        actCache = null;
        gallery.setBaseAdapter(new GalaryImageAdapter());
        galleryOverwiew.setBaseAdapter(new OverviewImageAdapter());
    }

    @Override
    public void onShow() {
        if (GlobalCore.isSetSelectedCache()) {

            if (!forceReload && GlobalCore.getSelectedCache().equals(actCache)) {
                return;
            }

            forceReload = false;

            actCache = GlobalCore.getSelectedCache();

            if (actCache.hasSpoiler()) {

                GalleryItem firstItem = null;
                synchronized (bigItems) {
                    bigItems.clear();
                    overviewItems.clear();

                    CB_RectF orItemRec = galleryOverwiew.copy();
                    orItemRec.setWidth(galleryOverwiew.getHeight());

                    ArrayList<ImageEntry> dbImages = imageDAO.getImagesForCache(actCache.getGcCode());

                    for (int i = 0, n = actCache.getSpoilerRessources().size(); i < n; i++) {
                        ImageEntry imageEntry = actCache.getSpoilerRessources().get(i);

                        String description = "";

                        String localName = FileIO.getFileNameWithoutExtension(imageEntry.LocalPath);
                        for (ImageEntry dbImage : dbImages) {
                            String localNameFromDB = FileIO.getFileNameWithoutExtension(dbImage.LocalPath);
                            if (localNameFromDB.equals(localName)) {
                                // Description
                                description = dbImage.Name + "\n" + dbImage.Description;
                                break;
                            } else {
                                if (FileIO.getFileNameWithoutExtension(dbImage.Name).equals(localName)) {
                                    // Spoiler CacheWolf
                                    description = dbImage.Description;
                                    break;
                                } else {
                                    if (localName.contains(FileIO.getFileNameWithoutExtension(dbImage.Name))) {
                                        // Spoiler ACB
                                        description = localName + "\n" + dbImage.Description;
                                        break;
                                    }
                                }
                            }
                        }

                        ImageLoader loader = new ImageLoader(true); // image loader with thumb
                        loader.setThumbWidth(MAX_THUMB_WIDTH, "");
                        loader.setImage(imageEntry.LocalPath);
                        String label;
                        if (description.length() > 0)
                            label = removeHashFromLabel(description);
                        else {
                            label = removeHashFromLabel(FileIO.getFileNameWithoutExtension(imageEntry.Name));
                        }

                        GalleryBigItem item = new GalleryBigItem(gallery.copy(), i, loader, label);
                        item.setOnDoubleClickListener((v, x, y, pointer, button) -> {
                            Image selectionImage = ((GalleryBigItem) v).getImage();

                            String path = selectionImage.getImageLoader().getOriginalImagePath();

                            Image img = new Image(SpoilerView.this, "Image for Activity", true);
                            img.setImage(path);

                            ImageActivity ac = new ImageActivity(img);
                            ac.show();
                            return true;
                        });
                        bigItems.add(item);

                        ImageLoader overviewloader = new ImageLoader(true); // image loader with thumb
                        overviewloader.setThumbWidth(MAX_OVERVIEW_THUMB_WIDTH, FileFactory.THUMB_OVERVIEW);
                        overviewloader.setImage(imageEntry.LocalPath);
                        GalleryItem overviewItem = new GalleryItem(orItemRec, i, loader);
                        overviewItem.addClickHandler((v, x, y, pointer, button) -> {
                            final int idx = ((GalleryItem) v).getIndex();

                            gallery.notifyDataSetChanged();

                            gallery.scrollToItem(idx);
                            galleryOverwiew.setSelection(idx);
                            galleryOverwiew.scrollItemToCenter(idx);
                            return true;
                        });
                        if (firstItem == null)
                            firstItem = overviewItem;
                        overviewItems.add(overviewItem);
                    }
                }
                // Log.info(log, "Images loaded");
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
        // Log.info(log, "End onShow");
    }

    private String removeHashFromLabel(String label) {
        int p1 = label.indexOf(" - ");
        if (p1 < 0)
            p1 = 0;
        else
            p1 = p1 + 3;
        int p2 = label.indexOf("@");
        if (p2 < 0)
            label = label.substring(p1);
        else
            label = label.substring(p1, p2);
        return label.trim();
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

    public String getSelectedFilePath() {
        String file = null;
        try {
            file = ((GalleryBigItem) gallery.getSelectedItem()).getImage().getImageLoader().getImagePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public class GalaryImageAdapter implements Adapter {
        GalaryImageAdapter() {
        }

        @Override
        public int getCount() {
            synchronized (bigItems) {
                return bigItems.size();
            }
        }

        @Override
        public ListViewItemBase getView(int position) {
            synchronized (bigItems) {
                if (bigItems.size() == 0)
                    return null;
                return bigItems.get(position);
            }
        }

        @Override
        public float getItemSize(int position) {
            synchronized (bigItems) {
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
        OverviewImageAdapter() {
        }

        @Override
        public int getCount() {
            synchronized (overviewItems) {
                return overviewItems.size();
            }
        }

        @Override
        public ListViewItemBase getView(int position) {
            synchronized (overviewItems) {
                if (overviewItems.size() == 0)
                    return null;
                return overviewItems.get(position);
            }
        }

        @Override
        public float getItemSize(int position) {
            synchronized (overviewItems) {
                if (overviewItems.size() == 0)
                    return 0;
                GalleryItem item = overviewItems.get(position);
                if (item != null)
                    return item.getWidth();
                return 0;
            }
        }
    }

}
