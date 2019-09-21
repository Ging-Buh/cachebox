package CB_UI.GL_UI.Activitys.settings;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Controls.QuickButtonList;
import CB_UI.GL_UI.Main.Actions.QuickButton.QuickActions;
import CB_UI.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Util.HSV_Color;
import CB_Utils.Util.MoveableList;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

public class SettingsItem_QuickButton extends CB_View_Base {

    public static MoveableList<QuickButtonItem> tmpQuickList = null;
    private ImageButton up, down, del, add;
    private V_ListView listView;
    private Box boxForListView;

    public SettingsItem_QuickButton(CB_RectF rec, String Name) {
        super(rec, Name);

        this.addClickHandler((v, x, y, pointer, button) -> {
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

        final ArrayList<QuickActions> AllActionList = new ArrayList<>();
        QuickActions[] tmp = QuickActions.values();

        for (QuickActions item : tmp) {
            // don't show QuickButton Torch if Torch not available
            if (item == QuickActions.torch) {
                if (!PlatformConnector.isTorchAvailable())
                    continue;
            }

            boolean exist = false;
            for (int i = 0, n = tmpQuickList.size(); i < n; i++) {
                QuickButtonItem listItem = tmpQuickList.get(i);
                if (listItem.getAction() == item)
                    exist = true;
            }
            if (!exist)
                AllActionList.add(item);
        }

        Menu icm = new Menu("Select QuickButtonItem");
        for (QuickActions item : AllActionList) {
            if (item == QuickActions.empty)
                continue;
            icm.addMenuItem("", QuickActions.getName(item.ordinal()),
                    new SpriteDrawable(QuickActions.getActionEnumById(item.ordinal()).getIcon()),
                    (v, x, y, pointer, button) -> {
                        icm.close();
                        QuickActions type = (QuickActions) v.getData();
                        float itemHeight = UiSizes.getInstance().getQuickButtonListHeight() * 0.93f;
                        QuickButtonItem tmp1 = new QuickButtonItem(new CB_RectF(0, 0, itemHeight, itemHeight),
                                tmpQuickList.size(), QuickActions.getActionEnumById(type.ordinal()), QuickActions.getName(type.ordinal()), type);
                        tmpQuickList.add(tmp1);
                        reloadListViewItems();
                        return true;
                    }).setData(item);
        }
        icm.setPrompt(Translation.get("selectQuickButtemItem"));
        icm.show();
    }

    private void initialButtons() {

        up = new ImageButton("up");
        down = new ImageButton("down");
        del = new ImageButton("del");
        add = new ImageButton("add");

        up.setWidth(up.getHeight());
        down.setWidth(up.getHeight());
        del.setWidth(up.getHeight());
        add.setWidth(up.getHeight());

        up.setImage(new SpriteDrawable(Sprites.Arrows.get(11)));
        down.setImage(new SpriteDrawable(Sprites.Arrows.get(11)));
        del.setImage(new SpriteDrawable(Sprites.getSprite(IconName.DELETE.name())));
        add.setImage(new SpriteDrawable(Sprites.getSprite(IconName.ADD.name())));

        up.setImageScale(0.7f);
        down.setImageScale(0.7f);
        del.setImageScale(0.7f);
        add.setImageScale(0.7f);

        up.setImageRotation(90f);
        down.setImageRotation(-90f);

        this.addChild(up);
        this.addChild(down);
        this.addChild(del);
        this.addChild(add);

        add.addClickHandler((v, x, y, pointer, button) -> {
            showSelect();
            return true;
        });

        del.addClickHandler((v, x, y, pointer, button) -> {
            int index = listView.getSelectedIndex();

            if (index >= 0 && index < tmpQuickList.size()) {
                tmpQuickList.remove(index);

                reloadListViewItems();
            }

            return true;
        });

        down.addClickHandler((v, x, y, pointer, button) -> {
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

        up.addClickHandler((v, x, y, pointer, button) -> {
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
        CB_RectF rec = new CB_RectF(0, 0, this.getWidth(), this.getHeight());
        boxForListView = new Box(rec, "");
        boxForListView.setBackground(Sprites.activityBackground);

        listView = new V_ListView(rec.copy(), "");
        listView.setDisposeFlag(false);
        boxForListView.addChildDirekt(listView);

        {
            // TODO die Listview wird hier nur angezeigt, wenn ein
            // Hintergrund gesetzt ist! Warum weis ich nicht ?!?!
            // Ich habe ihn erstmal auf Tranzparent gesetzt!
            HSV_Color c = new HSV_Color(1, 1, 1, 0);
            listView.setBackground(new ColorDrawable(c));
        }

        this.addChild(boxForListView);

    }

    private void reloadListViewItems() {
        listView.setBaseAdapter(null);
        listView.setBaseAdapter(new CustomAdapter());
        listView.notifyDataSetChanged();
    }

    private void layout() {
        float btnLeft = this.getWidth() - rightBorder - up.getWidth();
        float margin = up.getHalfHeight() / 2;

        add.setX(btnLeft);
        add.setY(this.getBottomHeight() + margin);

        del.setX(btnLeft);
        del.setY(add.getMaxY() + margin);

        down.setX(btnLeft);
        down.setY(del.getMaxY() + margin);

        up.setX(btnLeft);
        up.setY(down.getMaxY() + margin);

        this.setHeight(up.getMaxY() + margin);

        boxForListView.setX(margin);
        boxForListView.setY(margin);
        boxForListView.setWidth(add.getX() - margin - margin);
        boxForListView.setHeight(this.getHeight() - (margin * 2));

        listView.setX(margin);
        listView.setY(margin / 2);
        listView.setWidth(boxForListView.getWidth() - (margin * 2));
        listView.setHeight(boxForListView.getHeight() - margin);
    }

    public class CustomAdapter implements Adapter {

        @Override
        public ListViewItemBase getView(int position) {
            if (tmpQuickList == null)
                return null;
            Menu icm = new Menu("virtuell");
            QuickButtonItem item = tmpQuickList.get(position);
            QuickActions action = item.getAction();
            MenuItem mi = icm.addMenuItem("", QuickActions.getName(action.ordinal()),
                    new SpriteDrawable(QuickActions.getActionEnumById(action.ordinal()).getIcon()),
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
            return UI_Size_Base.ui_size_base.getButtonHeight();
        }

        @Override
        public int getCount() {
            if (tmpQuickList == null)
                return 0;
            return tmpQuickList.size();
        }
    }
}
