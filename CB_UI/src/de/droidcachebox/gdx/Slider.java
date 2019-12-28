package de.droidcachebox.gdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.CB_UI_Settings;
import de.droidcachebox.Config;
import de.droidcachebox.SelectedCacheChangedEventListener;
import de.droidcachebox.SelectedCacheChangedEventListeners;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.CacheInfo;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.SizeChangedEvent;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.WaypointViewItem;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.utils.CB_List;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Slider extends CB_View_Base implements SelectedCacheChangedEventListener {
    private static final int MAX_ANIMATION_COUNT = 1000;
    // private static final ColorDrawable transparent = new ColorDrawable(new HSV_Color(0, 0.1f, 0, 0.8f));
    private static final SpriteDrawable red = new SpriteDrawable(Sprites.getSprite("red"));
    private static final SpriteDrawable yellow = new SpriteDrawable(Sprites.getSprite("yellow"));
    private static final SpriteDrawable green = new SpriteDrawable(Sprites.getSprite("green"));
    private static final SpriteDrawable transparent = new SpriteDrawable(Sprites.getSprite("transparent"));
    public static Slider that;
    private static Box mSlideBox, mSlideBoxContent;
    private final int ANIMATION_TIME = 50;
    private final QuickButtonList quickButtonList;
    private final CB_Label mLblCacheName, geoCacheType;
    private final CB_Label[] last5Logs;
    private final int QuickButtonMaxHeight;
    private final Handler handler = new Handler();
    private final ArrayList<YPositionChanged> eventList = new ArrayList<>();
    private WaypointViewItem cacheDesc;
    private WaypointViewItem waypointDesc;
    private final SizeChangedEvent onItemSizeChanged = () -> {
        layout();
        GL.that.renderOnce(true);
    };
    private Cache actCache;
    private Waypoint actWaypoint;
    private boolean swipeUp = false;
    private boolean swipeDown = false;
    private boolean animationIsRunning = false;
    private int animationDirection = -1;
    private int AnimationTarget = 0;
    private boolean isKinetigPan = false;
    private float yPos = 0;
    private float touchYoffset = 0;
    private boolean oneTouchUP = false;
    private AtomicInteger animationCounter = new AtomicInteger(0);
    private Runnable AnimationTask = new Runnable() {

        @Override
        public void run() {

            GL.that.renderOnce(true);

            if (!animationIsRunning)
                return; // Animation wurde abgebrochen

            if (animationCounter.incrementAndGet() > MAX_ANIMATION_COUNT) {
                //break a never ending animation
                setPos_onUI(AnimationTarget);
                animationIsRunning = false;
                return;
            }


            int newValue;
            double animationMulti = 1.4;
            if (animationDirection == -1) {
                float tmp = yPos - AnimationTarget;
                if (tmp <= 0)// Div 0 vehindern
                {
                    setPos_onUI(AnimationTarget);
                    animationIsRunning = false;
                }

                newValue = (int) (yPos - (tmp / animationMulti));
                if (newValue <= AnimationTarget) {
                    setPos_onUI(AnimationTarget);
                    animationIsRunning = false;
                } else {
                    setPos_onUI(newValue);
                    handler.postDelayed(AnimationTask, ANIMATION_TIME);
                }
            } else {
                float tmp = AnimationTarget - yPos;
                if (tmp <= 0)// Div 0 vehindern
                {
                    setPos_onUI(AnimationTarget);
                    animationIsRunning = false;
                } else {
                    newValue = (int) (yPos + (tmp / animationMulti));
                    if (newValue >= AnimationTarget) {
                        setPos_onUI(AnimationTarget);
                        animationIsRunning = false;
                    } else {
                        setPos_onUI(newValue);
                        handler.postDelayed(AnimationTask, ANIMATION_TIME);
                    }
                }
            }
        }
    };

    public Slider(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;
        registerSkinChangedEvent();
        SelectedCacheChangedEventListeners.getInstance().add(this);
        this.setClickable(true);

        QuickButtonMaxHeight = UiSizes.getInstance().getQuickButtonListHeight();

        quickButtonList = new QuickButtonList(new CB_RectF(0, this.getHeight() - QuickButtonMaxHeight, this.getWidth(), QuickButtonMaxHeight), "QuickButtonList");

        mSlideBox = new Box(new CB_RectF(-15, 100, this.getWidth() + 30, UiSizes.getInstance().getInfoSliderHeight()), "SlideBox");
        mSlideBox.setBackground(Sprites.ProgressBack); // topBorder + bottomBorder are ignored, so initRow
        mSlideBox.initRow(TOPDOWN, mSlideBox.getHeight());
        float w = Fonts.measureForBigFont("G.").width;
        last5Logs = new CB_Label[5];
        for (int i = 0; i < 5; i++) {
            last5Logs[i] = new CB_Label();
            last5Logs[i].setSize(w, mSlideBox.getHeight());
            mSlideBox.addNext(last5Logs[i], FIXED);
        }
        geoCacheType = new CB_Label();
        geoCacheType.setSize(mSlideBox.getHeight(), mSlideBox.getHeight());
        geoCacheType.setHAlignment(HAlignment.CENTER);
        mSlideBox.addNext(geoCacheType, FIXED);

        mLblCacheName = new CB_Label(new CB_RectF(20, 0, this.getWidth() - 30, mSlideBox.getHeight())).setFont(Fonts.getBig());
        mLblCacheName.setPos(30, 0);
        mLblCacheName.setHAlignment(HAlignment.SCROLL_CENTER);
        mSlideBox.addLast(mLblCacheName);

        mSlideBoxContent = new Box(this, "SlideBoxContent");

        mSlideBoxContent.setBackground(transparent);
        this.addChild(mSlideBoxContent);
        this.addChild(quickButtonList);
        this.addChild(mSlideBox);

        //register QuickButtonStateChangedEvent
        CB_UI_Settings.quickButtonShow.addSettingChangedListener(() -> {
            if (CB_UI_Settings.quickButtonShow.getValue()) {
                quickButtonList.setHeight(QuickButtonMaxHeight);
            } else {
                quickButtonList.setHeight(0);
            }
            quickButtonList.notifyDataSetChanged();
            initialize();
        });

    }

    public static boolean setAndroidSliderHeight(int height) {
        if (that != null && mSlideBox != null) {
            // the Android Slider has transparent zones on top and button,
            // so we reduce this given height at ~10%.
            mSlideBox.setHeight(height * 0.9f);
            return true;
        }
        return true;
    }

    public void registerPosChangedEvent(YPositionChanged listener) {
        if (!eventList.contains(listener))
            eventList.add(listener);
    }

    public void removePosChangedEvent(YPositionChanged listener) {
        eventList.remove(listener);
    }

    private void callPosChangedEvent() {
        for (YPositionChanged event : eventList) {
            event.Position(mSlideBox.getMaxY(), mSlideBox.getY());
        }
    }

    @Override
    protected void initialize() {
        float initialPos;
        if (Config.quickButtonShow.getValue()) {
            initialPos = this.getHeight() - mSlideBox.getHeight() - QuickButtonMaxHeight;
        } else {
            initialPos = this.getHeight() - mSlideBox.getHeight();
        }
        setSliderPos(initialPos);
        ActionUp();
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
    }

    @Override
    public void selectedCacheChanged(final Cache cache, Waypoint waypoint) {
        // view must be refilled with values
        actCache = cache; // normally these are the same objects
        actWaypoint = waypoint; // normally these are the same objects

        GL.that.RunOnGL(() -> {
            if (cache != null) {
                fillCacheWpInfo();
                String header = "";
                if (mLblCacheName != null) {
                    /*
                    if (cacheDesc.getCacheInfo().needsMaintenance()) {
                        header = header + "!!!M!";
                    }
                     */
                    CB_List<LogEntry> logEntries = Database.getLogs(cache);
                    for (int i = 0; i < 5; i++) {
                        last5Logs[i].setText(" ");
                        if (i < logEntries.size()) {
                            switch (logEntries.get(i).geoCacheLogType) {
                                case found:
                                case owner_maintenance:
                                    last5Logs[i].setBackground(green);
                                    break;
                                case didnt_find:
                                    last5Logs[i].setBackground(red);
                                    break;
                                case needs_archived:
                                case needs_maintenance:
                                    last5Logs[i].setBackground(yellow);
                                    break;
                                default:
                                    last5Logs[i].setBackground(transparent);
                            }
                        } else {
                            last5Logs[i].setBackground(Sprites.ProgressBack);
                        }
                    }
                    geoCacheType.setBackground(new SpriteDrawable(Sprites.getSprite("map" + cache.getType().name()))); // GeoCacheType.toShortString(cache) +
                    header = header + terrDiffToShortString(cache.getDifficulty()) + "/" + terrDiffToShortString(cache.getTerrain()) + GeoCacheSize.toShortString(cache) + " " + cache.getName();
                }
                mLblCacheName.setText(header);
            }
        });
    }

    private String terrDiffToShortString(float value) {
        int intValue = (int) value;
        String retValue;
        if (value == intValue) {
            retValue = "" + intValue;
        } else {
            retValue = "" + value;
        }
        return retValue;
    }

    private void setSliderPos(float value) {
        if (value == yPos || mSlideBox == null)
            return;

        yPos = value;
        mSlideBox.setY(value);
        mSlideBoxContent.setY(mSlideBox.getMaxY() - GL_UISizes.margin);
        setQuickButtonListHeight();
        GL.that.renderOnce();
        callPosChangedEvent();
    }

    private void setQuickButtonListHeight() {
        if (Config.quickButtonShow.getValue()) {
            if (this.getHeight() - mSlideBox.getMaxY() < QuickButtonMaxHeight) {
                quickButtonList.setHeight(this.getHeight() - mSlideBox.getMaxY());
                quickButtonList.setY(this.getHeight() - quickButtonList.getHeight());
            } else {
                quickButtonList.setHeight(QuickButtonMaxHeight);
                quickButtonList.setY(this.getHeight() - quickButtonList.getHeight());
            }

        } else {
            quickButtonList.setHeight(0);
        }

        ViewManager.that.setContentMaxY(this.getHeight() - quickButtonList.getHeight() - mSlideBox.getHeight());
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        if (KineticPan) {
            GL_Input.that.StopKinetic(x, y, pointer, true);
            isKinetigPan = true;
            return onTouchUp(x, y, pointer, 0);
        }

        float newY = y - mSlideBox.getHeight() - touchYoffset;
        setSliderPos(newY);
        return true;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        isKinetigPan = false;
        oneTouchUP = false;
        animationIsRunning = false;
        if (mSlideBox.contains(x, y)) {
            touchYoffset = y - mSlideBox.getMaxY();
            return true;
        }

        // return true if slider down
        return yPos <= 0;

    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        if (isKinetigPan) {
            if (oneTouchUP)
                return true;
            oneTouchUP = true;
        }

        ActionUp();
        return true;
    }

    private void ActionUp() // Slider zurück scrollen lassen
    {
        boolean QuickButtonShow = Config.quickButtonShow.getValue();

        // check if QuickButtonList snap in
        int quickButtonHeight;
        if (this.getHeight() - mSlideBox.getMaxY() >= (QuickButtonMaxHeight * 0.5) && QuickButtonShow) {
            quickButtonHeight = QuickButtonMaxHeight;
            Config.quickButtonLastShow.setValue(true);
            Config.AcceptChanges();
        } else {
            quickButtonHeight = 0;
            Config.quickButtonLastShow.setValue(false);
            Config.AcceptChanges();
        }

        if (swipeUp || swipeDown) {
            if (swipeUp) {
                startAnimationTo(QuickButtonShow ? quickButtonHeight : 0);
            } else {
                startAnimationTo((int) (getHeight() - mSlideBox.getHeight()));
            }
            swipeUp = swipeDown = false;

        } else {
            if (yPos > getHeight() * 0.5) {
                startAnimationTo((int) (getHeight() - mSlideBox.getHeight() - (QuickButtonShow ? quickButtonHeight : 0)));
            } else {
                startAnimationTo(0);

            }
        }
    }

    private void startAnimationTo(int newYPos) {
        if (yPos == newYPos)
            return; // wir brauchen nichts Animieren

        animationIsRunning = true;
        animationCounter.set(0);
        AnimationTarget = newYPos;
        if (yPos > newYPos)
            animationDirection = -1;
        else
            animationDirection = 1;
        handler.postDelayed(AnimationTask, ANIMATION_TIME);
    }

    private void setPos_onUI(final int newValue) {

        GL.that.RunOnGL(() -> setSliderPos(newValue));

    }

    @Override
    protected void skinIsChanged() {
        mSlideBox.setBackground(Sprites.ProgressBack);

    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
    }

    private void fillCacheWpInfo() {
        mSlideBoxContent.removeChildsDirekt();

        CB_RectF rec = UiSizes.getInstance().getCacheListItemRec().asFloat();
        rec.setWidth(this.getWidth());
        if (actCache != null) {
            cacheDesc = new WaypointViewItem(rec, 0, actCache, null, CacheInfo.VIEW_MODE_SLIDER);
            cacheDesc.setHeight(cacheDesc.getHeight() + cacheDesc.getAttributeHeight() + cacheDesc.getStarsHeight() + (cacheDesc.getTexteHeight() / 2));
            cacheDesc.requestLayout();
            cacheDesc.isSelected = true;
            cacheDesc.addListener(onItemSizeChanged);
            mSlideBoxContent.addChild(cacheDesc);
        }

        if (actWaypoint != null) {
            waypointDesc = new WaypointViewItem(rec, 1, actCache, actWaypoint);
            waypointDesc.isSelected = true;
            cacheDesc.isSelected = false;
            waypointDesc.addListener(onItemSizeChanged);
            mSlideBoxContent.addChild(waypointDesc);
        } else {
            if (waypointDesc != null)
                waypointDesc.dispose();
            waypointDesc = null;
        }

        layout();
    }

    private void layout() {

        float YLayoutPos = GL_UISizes.margin;

        if (waypointDesc != null) {
            waypointDesc.setPos(0, YLayoutPos);
            YLayoutPos += GL_UISizes.margin + waypointDesc.getHeight();
        }

        if (cacheDesc != null) {
            cacheDesc.setPos(0, YLayoutPos);
            // YLayoutPos += GL_UISizes.margin + cacheDesc.getHeight();
        }
    }

    public interface YPositionChanged {
        void Position(float top, float Bottom);
    }

}
