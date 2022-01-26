package de.droidcachebox.settings;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

import de.droidcachebox.Platform;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.QuickButtonList;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.ImageButton;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.graphics.ColorDrawable;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.QuickButtonItem;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MoveableList;

public class SettingsItem_QuickButton extends CB_View_Base {

    private final MoveableList<QuickButtonItem> tmpQuickList;
    private ImageButton up, down, del, add;
    private V_ListView listView;
    private Box boxForListView;

    public SettingsItem_QuickButton(CB_RectF rec, String Name) {
        super(rec, Name);

        setClickHandler((v, x, y, pointer, button) -> {
            showSelect();
            return true;
        });

        initialButtons();
        initialListView();

        layout();
        reloadListViewItems();

        tmpQuickList = new MoveableList<>(QuickButtonList.quickButtonList);
        reloadListViewItems();
    }



    private void showSelect() {
        // erstelle Menu mit allen Actions, die noch nicht in der QuickButton List enthalten sind.

        final ArrayList<Action> actions = new ArrayList<>();
        Action[] tmp = Action.values();

        for (Action item : tmp) {
            // don't show QuickButton Torch if Torch not available
            if (item == Action.SwitchTorch) {
                if (!Platform.isTorchAvailable())
                    continue;
            }

            boolean exist = false;
            for (int i = 0, n = tmpQuickList.size(); i < n; i++) {
                QuickButtonItem listItem = tmpQuickList.get(i);
                if (listItem.getQuickAction() == item)
                    exist = true;
            }
            if (!exist)
                actions.add(item);
        }

        Menu icm = new Menu("Select QuickButtonItem");
        for (Action action : actions) {
            if (action.action == null)
                continue;
            icm.addMenuItem(action.translationId, "",
                    new SpriteDrawable(action.action.getIcon()),
                    (v, x, y, pointer, button) -> {
                        icm.close();
                        Action clickedAction = (Action) v.getData();
                        float itemHeight = UiSizes.getInstance().getQuickButtonListHeight() * 0.93f;
                        tmpQuickList.add(new QuickButtonItem(new CB_RectF(0, 0, itemHeight), tmpQuickList.size(), clickedAction));
                        reloadListViewItems();
                        return true;
                    }).setData(action);
        }
        icm.setPrompt(Translation.get("selectQuickButtemItem"));
        icm.show();
    }

    private void initialButtons() {

        up = new ImageButton();
        down = new ImageButton();
        del = new ImageButton();
        add = new ImageButton();

        up.setWidth(up.getHeight());
        down.setWidth(up.getHeight());
        del.setWidth(up.getHeight());
        add.setWidth(up.getHeight());

        up.setImage(new SpriteDrawable(Sprites.Arrows.get(11)));
        up.setImageScale(0.7f);
        up.setImageRotation(90f);
        down.setImage(new SpriteDrawable(Sprites.Arrows.get(11)));
        down.setImageScale(0.7f);
        down.setImageRotation(-90f);
        del.setImage(new SpriteDrawable(Sprites.getSprite(IconName.DELETE.name())));
        del.setImageScale(0.7f);
        add.setImage(new SpriteDrawable(Sprites.getSprite(IconName.ADD.name())));
        add.setImageScale(0.7f);


        addChild(up);
        addChild(down);
        addChild(del);
        addChild(add);

        add.setClickHandler((v, x, y, pointer, button) -> {
            showSelect();
            return true;
        });

        del.setClickHandler((v, x, y, pointer, button) -> {
            int index = listView.getSelectedIndex();

            if (index >= 0 && index < tmpQuickList.size()) {
                tmpQuickList.remove(index);

                reloadListViewItems();
            }

            return true;
        });

        down.setClickHandler((v, x, y, pointer, button) -> {
            int index = listView.getSelectedIndex();

            if (index >= 0 && index < tmpQuickList.size()) {
                tmpQuickList.MoveItem(index, 1);

                reloadListViewItems();
                int newIndex = index + 1;
                if (newIndex >= tmpQuickList.size())
                    newIndex = 0;

                listView.setSelection(newIndex);
            }

            return true;
        });

        up.setClickHandler((v, x, y, pointer, button) -> {
            int index = listView.getSelectedIndex();

            if (index >= 0 && index < tmpQuickList.size()) {
                tmpQuickList.MoveItem(index, -1);

                reloadListViewItems();

                int newIndex = index - 1;
                if (newIndex < 0)
                    newIndex = tmpQuickList.size() - 1;

                listView.setSelection(newIndex);
            }

            return true;
        });

    }

    private void initialListView() {
        CB_RectF rec = new CB_RectF(0, 0, getWidth(), getHeight());
        boxForListView = new Box(rec, "");
        boxForListView.setBackground(Sprites.activityBackground);

        listView = new V_ListView(new CB_RectF(rec), "");
        listView.setDisposeFlag(false);
        boxForListView.addChildDirect(listView);

        {
            // TODO die Listview wird hier nur angezeigt, wenn ein
            // Hintergrund gesetzt ist! Warum weis ich nicht ?!?!
            // Ich habe ihn erstmal auf Tranzparent gesetzt!
            HSV_Color c = new HSV_Color(1, 1, 1, 0);
            listView.setBackground(new ColorDrawable(c));
        }

        addChild(boxForListView);

    }

    private void reloadListViewItems() {
        listView.setAdapter(null);
        listView.setAdapter(new CustomAdapter());
        listView.notifyDataSetChanged();
    }

    private void layout() {
        float btnLeft = getWidth() - rightBorder - up.getWidth();
        float margin = up.getHalfHeight() / 2;

        add.setX(btnLeft);
        add.setY(getBottomHeight() + margin);

        del.setX(btnLeft);
        del.setY(add.getMaxY() + margin);

        down.setX(btnLeft);
        down.setY(del.getMaxY() + margin);

        up.setX(btnLeft);
        up.setY(down.getMaxY() + margin);

        setHeight(up.getMaxY() + margin);

        boxForListView.setX(margin);
        boxForListView.setY(margin);
        boxForListView.setWidth(add.getX() - margin - margin);
        boxForListView.setHeight(getHeight() - (margin * 2));

        listView.setX(margin);
        listView.setY(margin / 2);
        listView.setWidth(boxForListView.getWidth() - (margin * 2));
        listView.setHeight(boxForListView.getHeight() - margin);
    }

    public MoveableList<QuickButtonItem> getTmpQuickList() {
        return tmpQuickList;
    }

    public class CustomAdapter implements Adapter {

        @Override
        public ListViewItemBase getView(int position) {
            if (tmpQuickList == null)
                return null;
            Menu icm = new Menu("virtuell");
            QuickButtonItem item = tmpQuickList.get(position);
            Action action = item.getQuickAction();
            MenuItem mi = icm.addMenuItem(action.translationId,"",
                    new SpriteDrawable(action.action == null ? null : action.action.getIcon()),
                    (v, x, y, pointer, button) -> {
                        listView.setSelection(((ListViewItemBase) v).getIndex());
                        return false;
                    });
            mi.setIndex(position);
            mi.setWidth(listView.getWidth() - (listView.getBackground().getLeftWidth() * 2));
            mi.setClickable(true);
            return mi;
        }

        @Override
        public float getItemSize(int position) {
            return UiSizes.getInstance().getButtonHeight();
        }

        @Override
        public int getCount() {
            if (tmpQuickList == null)
                return 0;
            return tmpQuickList.size();
        }
    }
}
