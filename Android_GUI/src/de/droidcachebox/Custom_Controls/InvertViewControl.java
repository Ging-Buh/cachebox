/*
 * Copyright (C) 2011-2012 team-cachebox.de
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

package de.droidcachebox.Custom_Controls;

import CB_UI.Config;
import CB_UI.GL_UI.Views.DescriptionView;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.ViewConst;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import de.droidcachebox.Global;
import de.droidcachebox.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Longri
 */
public final class InvertViewControl extends View {

    public static InvertViewControl Me;
    LinearLayout WebViewLayout = null;
    Bitmap b = null;
    boolean firstDraw = true;
    Activity mainActivity;

    public InvertViewControl(Context context) {
        super(context);
        if (context instanceof Activity)
            mainActivity = (Activity) context;
        Me = this;
    }

    public InvertViewControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof Activity)
            mainActivity = (Activity) context;
        Me = this;
    }

    public InvertViewControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (context instanceof Activity)
            mainActivity = (Activity) context;
        Me = this;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Global.getColor(R.attr.EmptyBackground));

        WebViewLayout = (LinearLayout) findViewById(R.id.WebViewLayout);
        if (WebViewLayout != null) {
            b = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            WebViewLayout.draw(c);
            canvas.drawBitmap(b, 0, 0, Config.nightMode.getValue() ? Global.invertPaint : new Paint());
        }


        super.onDraw(canvas);

        // beim ersten zeichnen muss gewartet werden, bis das WebView auch
        // gezeichnet ist.
        // dies Zeichnet aber nur wenn das DescView nochmal aufgerufen wird.
        // Also machen wir das nach 100 msec
        if (firstDraw) {
            final TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Thread t = new Thread() {
                        public void run() {
                            mainActivity.runOnUiThread(() -> {
                                if (firstDraw) {
                                    firstDraw = false;
                                    DescriptionView dv = DescriptionView.getInstance();
                                    PlatformConnector.showView(ViewConst.DESCRIPTION_VIEW, dv.getX(), dv.getY(), dv.getWidth(), dv.getHeight());
                                }
                            });
                        }
                    };

                    t.start();

                }
            };

            Timer timer = new Timer();
            timer.schedule(task, 100);

        }

        if (b != null) b.recycle();
        b = null;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

    }

}
