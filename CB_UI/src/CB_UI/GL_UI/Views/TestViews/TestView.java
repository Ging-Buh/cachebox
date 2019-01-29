package CB_UI.GL_UI.Views.TestViews;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_UI.GL_UI.Activitys.CreateTrackOverMapActivity;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.Energy;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog.ICancelListener;
import CB_UI_Base.GL_UI.Controls.ImageLoader;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUpMenu;
import CB_UI_Base.GL_UI.Controls.RadioButton;
import CB_UI_Base.GL_UI.Controls.RadioGroup;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enthält die TestContols
 *
 * @author Longri
 */
public class TestView extends CB_View_Base {

    public static final String br = System.getProperty("line.separator");
    public static final String splashMsg = "Team" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission" + br + " " + br + "7.Zeile";
    public static final int MAX_MAP_ZOOM = 22;
    private static TestView that;
    final int mapIntWidth = 3000;
    final int mapIntHeight = 3000;
    final Coordinate center = new CoordinateGPS(50.44, 9.28);
    final int drawingWidth = 3000;
    final int drawingHeight = 3000;
    ImageLoader testImg;
    OnClickListener click = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            // if (testImg != null) TestView.this.removeChild(testImg);
            // testImg = new Image(50, 50, 300, 500, "");
            // testImg.setImageURL("http://img.geocaching.com/track/display/2190cf73-ecab-468a-a61a-611c123e567a.jpg");
            // TestView.this.addChild(testImg);
            if (Energy.DisplayOff())
                Energy.setDisplayOn();
            else
                Energy.setDisplayOff();
            return true;
        }

    };
    String str;
    float camerazoom = 10;
    ProgressDialog PD;
    private CB_UI_Base.GL_UI.Controls.EditTextField wrappedTextField;

    private TestView() {
        super(TabMainView.leftTab.getContentRec(), "TestView");

        this.setClickable(true);

        setBackground(Sprites.ListBack);

        //	CB_RectF TextFieldRec = new CB_RectF(0, this.getHeight() - (UI_Size_Base.that.getButtonHeight() * 3), UI_Size_Base.that.getButtonWidth() * 6, UI_Size_Base.that.getButtonHeight() * 3);
        //
        //	wrappedTextField = new CB_UI_Base.GL_UI.Controls.EditTextField(TextFieldRec, this).setWrapType(WrapType.WRAPPED);
        //	wrappedTextField.setStyle(EditTextField.getDefaultStyle());
        //	wrappedTextField.setText(splashMsg);
        //	// wrappedTextField.setText("");
        //
        //	this.addChild(wrappedTextField);

        // ####################################################
        //
        //	Label label = new Label(new CB_RectF(50, 50, 300, 100), "/ExtSD/Карти/Vector Maps/asadasdasd dasasdasdasd");
        //	label.setHAlignment(HAlignment.SCROLL_LEFT);
        //	this.addChild(label);
        // ####################################################

        float margin = UI_Size_Base.that.getMargin();

        final RadioButton rb = new RadioButton("1 segment");

        float rbmargin = rb.getHeight() - margin;

        rb.setPos(5, this.getHeight() - rbmargin);
        rb.setWidth(this.getHalfWidth() - rb.getX());
        rb.setText("Option 1");
        this.addChild(rb);

        final RadioButton rb2 = new RadioButton("Test");
        rb2.setPos(5, rb.getY() - rbmargin);
        rb2.setWidth(this.getHalfWidth() - rb.getX());
        rb2.setText("Option 2");
        this.addChild(rb2);

        final RadioButton rb3 = new RadioButton("Test");
        rb3.setPos(5, rb2.getY() - rbmargin);
        rb3.setWidth(this.getHalfWidth() - rb.getX());
        rb3.setText("Option 3");
        this.addChild(rb3);

        final RadioButton rb4 = new RadioButton("Test");
        rb4.setPos(5, rb3.getY() - rbmargin);
        rb4.setWidth(this.getHalfWidth() - rb.getX());
        rb4.setText("Option 4");
        this.addChild(rb4);

        final RadioButton rb5 = new RadioButton("Test");
        rb5.setPos(5, rb4.getY() - rbmargin);
        rb5.setWidth(this.getHalfWidth() - rb.getX());
        rb5.setText("Option 5");
        this.addChild(rb5);

        final RadioButton rb6 = new RadioButton("Test");
        rb6.setPos(5, rb5.getY() - rbmargin);
        rb6.setWidth(this.getHalfWidth() - rb.getX());
        rb6.setText("Option 6");
        this.addChild(rb6);

        final RadioGroup Group = new RadioGroup();
        Group.add(rb);
        Group.add(rb2);
        Group.add(rb3);
        Group.add(rb4);
        Group.add(rb5);
        Group.add(rb6);
        Group.aktivate(rb);

        // Setting Button
        Button btnSetting = new Button(this.getWidth() - UI_Size_Base.that.getMargin() - (UI_Size_Base.that.getButtonWidthWide() * 2), this.getHeight() - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonWidthWide() * 2,
                UI_Size_Base.that.getButtonHeight(), "");
        btnSetting.setText("Show TrackView");
        btnSetting.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                TrackListView trk = new TrackListView(TestView.this, "TrackListView");

                CreateTrackOverMapActivity activity = new CreateTrackOverMapActivity("createTrackOverMap");
                activity.show();

                return true;
            }
        });

        this.addChild(btnSetting);

        Button btnMenu = new Button(this.getWidth() - UI_Size_Base.that.getMargin() - (UI_Size_Base.that.getButtonWidthWide() * 2), this.getHeight() - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonWidthWide() * 2,
                UI_Size_Base.that.getButtonHeight(), "");

        btnMenu.setY(btnMenu.getY() - (margin + btnMenu.getHeight()));
        btnMenu.setText("Show Menu");
        btnMenu.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                CB_RectF rec = new CB_RectF(x - 1, y - 1, 300, 300);

                RadioButton option = Group.getActSelection();

                int testCount = 0;
                if (option == rb)
                    testCount = 1;
                if (option == rb2)
                    testCount = 2;
                if (option == rb3)
                    testCount = 3;
                if (option == rb4)
                    testCount = 4;
                if (option == rb5)
                    testCount = 5;

                Menu menu = new Menu("RoundPopUpMenu");

                for (int i = 0; i < testCount; i++) {
                    MenuItem menuItem = new MenuItem(0, 0, "Item" + i);
                    menu.addItem(menuItem);
                }

                PopUpMenu popmenu = new PopUpMenu(rec, menu);
                popmenu.setPos(TestView.this.getHalfHeight() - menu.getHalfWidth(), menu.getHeight());
                popmenu.showNotCloseAutomaticly();
                return true;
            }
        });

        this.addChild(btnMenu);

        TestLabelView labelTest = new TestLabelView(this, "test Label");
        labelTest.setZeroPos();
        this.addChild(labelTest);
        requestLayout();

    }

    public static TestView getInstance() {
        if (that == null) that = new TestView();
        return that;
    }

    @Override
    protected void render(Batch batch) {
        // drawHausVomNikolaus(batch);

        renderDebugInfo(batch);
    }

    private void renderDebugInfo(Batch batch) {
        // str = "Coursor Pos:" + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorPos) + "/"
        // + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugRealCursorPos);
        // Fonts.getNormal().draw(batch, str, 20, 120);
        //
        // str = "LineCount: " + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugLineCount);
        // Fonts.getNormal().draw(batch, str, 20, 100);
        //
        // str = "L:" + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorLine) + " R:"
        // + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorRow);
        // Fonts.getNormal().draw(batch, str, 20, 80);
        //
        // String ch = CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCharBeforCursor;
        //
        // str = "Vor Cursor: " + ch;
        // Fonts.getNormal().draw(batch, str, 20, 60);

        // str = "TrackPoi: " + RouteOverlay.AllTrackPoints + " -  " + RouteOverlay.ReduceTrackPoints + " [" + RouteOverlay.DrawedLineCount
        // + "]";
        // Fonts.getNormal().draw(batch, str, 20, 40);
        //
        str = "fps: " + Gdx.graphics.getFramesPerSecond();
        Fonts.getNormal().draw(batch, str, 20, 20);

    }

    @Override
    public void onResized(CB_RectF rec) {
        requestLayout();
    }

    @Override
    public void onParentResized(CB_RectF rec) {
        this.setSize(rec.getSize());
    }

    private void requestLayout() {

        GL.that.renderOnce();
    }

    public boolean onTouchDown(int x, int y, int pointer, int button) {
        return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
    }

    public boolean onTouchUp(int x, int y, int pointer, int button) {
        return true;
    }

    public long getMapTilePosFactor(float zoom) {
        long result = 1;
        result = (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);
        return result;
    }

    private void showProgress() {
        final AtomicBoolean cancel = new AtomicBoolean(false);

        final RunnableReadyHandler UploadFieldNotesdThread = new RunnableReadyHandler() {

            int progress = 0;

            @Override
            public boolean doCancel() {
                return cancel.get();
            }

            @Override
            public void run() {

                do {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }

                    progress++;

                    PD.setProgress("", Integer.toString(progress) + "%", progress);

                    if (progress >= 100)
                        progress = 0;

                } while (!cancel.get());

                System.out.print("Ready");

            }

            @Override
            public void RunnableIsReady(boolean canceld) {
                GL.that.Toast("Runable Ready");
                PD.close();
            }
        };

        PD = ProgressDialog.Show("Upload FieldNotes", UploadFieldNotesdThread);

        PD.setCancelListener(new ICancelListener() {

            @Override
            public void isCanceled() {
                Timer t = new Timer();
                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        cancel.set(true);
                    }
                };
                t.schedule(tt, 3000);
            }
        });

    }

    public class result {
        public int count;
        public long time;
        public int zoom;
    }
}
