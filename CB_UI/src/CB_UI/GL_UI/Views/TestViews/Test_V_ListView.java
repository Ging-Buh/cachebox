package CB_UI.GL_UI.Views.TestViews;

import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.Math.CB_RectF;

import java.util.ArrayList;

public class Test_V_ListView extends V_ListView {

    ArrayList<String> TestArray = new ArrayList<String>();

    public Test_V_ListView(CB_RectF rec, String Name) {
        super(rec, Name);

        // ein Liste zusammen stellen
        TestArray.add("1. sdfasdfasdf");
        TestArray.add("2. sadfasdfasdf");
        TestArray.add("3. kghjkghjkhjk");
        TestArray.add("4. fghjghj");
        TestArray.add("5.");
        TestArray.add("6. dfsdfgggf");
        TestArray.add("7.gggsdeee");
        TestArray.add("8. ggerreaergzz");
        TestArray.add("9. 12323421354412345");
        TestArray.add("10. 12323421354412345");
        TestArray.add("11. sdfasdfasdf");
        TestArray.add("12. sadfasdfasdf");
        TestArray.add("13. kghjkghjkhjk");
        TestArray.add("14. fghjghj");
        TestArray.add("15.");
        TestArray.add("16. dfsdfgggf");
        TestArray.add("17.gggsdeee");
        TestArray.add("18. ggerreaergzz");
        TestArray.add("19. 12323421354412345");

        this.setBaseAdapter(new CustomAdapter());

    }

    public class CustomAdapter implements Adapter {

        public CustomAdapter() {

        }

        public long getItemId(int position) {
            return position;
        }

        public ListViewItemBase getView(int position) {

            Boolean BackGroundChanger = ((position % 2) == 1);
            TestListView_Item v = new TestListView_Item(position, TestArray.get(position), BackGroundChanger, "TestListView Item " + position);
            return v;
        }

        @Override
        public int getCount() {
            return TestArray.size();
        }

        @Override
        public float getItemSize(int position) {
            return 100;
        }
    }
}
