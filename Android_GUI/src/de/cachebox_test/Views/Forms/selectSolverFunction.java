/* 
 * Copyright (C) 2011 team-cachebox.de
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

package de.cachebox_test.Views.Forms;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Solver.Solver;
import CB_Core.Solver.Functions.Function;
import CB_Core.Solver.Functions.Functions;
import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.Components.Animations;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.Sizes;

/**
 * @author Longri
 */
public class selectSolverFunction extends Activity
{
	private Intent aktIntent;
	Button btnOk;
	Button btnCancel;
	LinearLayout content;
	Function selectedFunction;
	TextView desc;
	Context context;
	ScrollView scrollView;

	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_solver_function_layout);
		context = this;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		aktIntent = getIntent();
		this.getWindow().setBackgroundDrawableResource(Config.settings.nightMode.getValue() ? color.darker_gray : color.background_dark);

		findViewsById();
		setLang();
		fillContent();

		btnCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				finish();
			}
		});

		btnOk.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				if (selectedFunction != null)
				{
					aktIntent.putExtra("SOMETHING", "EXTRAS");
					Bundle extras = new Bundle();
					extras.putSerializable("FunctionResult", selectedFunction);
					aktIntent.putExtras(extras);
					setResult(RESULT_OK, aktIntent);
					finish();
				}

			}
		});

	}

	void findViewsById()
	{
		((LinearLayout) findViewById(R.id.main_LinearLayout)).setBackgroundColor(Global.getColor(R.attr.EmptyBackground));
		btnOk = (Button) findViewById(R.id.solver_function_ok);
		btnCancel = (Button) findViewById(R.id.solver_function_cancel);
		content = (LinearLayout) findViewById(R.id.solver_function_scrollView);
		desc = (TextView) findViewById(R.id.solver_function_desc);
		scrollView = (ScrollView) findViewById(R.id.solver_scroll_view);
		desc.setTextColor(Global.getColor(R.attr.TextColor));
	}

	void setLang()
	{
		btnOk.setText(GlobalCore.Translations.Get("ok"));
		btnCancel.setText(GlobalCore.Translations.Get("cancel"));
	}

	void fillContent()
	{

		final ArrayList<Button> functBtnList = new ArrayList<Button>();

		Resources res = this.getResources();
		Iterator<Functions> iteratorCat = Solver.functions.values().iterator();

		if (iteratorCat != null && iteratorCat.hasNext())
		{
			do
			{
				Functions cat = iteratorCat.next();
				final Button btn = new Button(this);
				btn.setWidth(Sizes.getQuickButtonWidth());
				btn.setHeight(Sizes.getQuickButtonHeight());
				btn.setText(GlobalCore.Translations.Get(cat.getName()));
				content.addView(btn);

				// add Cat einträge
				final LinearLayout lay = new LinearLayout(this);

				Iterator<Function> iteratorFunctions = cat.iterator();
				if (iteratorFunctions != null && iteratorFunctions.hasNext())
				{
					int layoutHeight = 0;
					do
					{
						final Function fct = iteratorFunctions.next();
						final Button btnFct = new Button(this);

						btnFct.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (Sizes.getQuickButtonHeight() * 0.8)));

						btnFct.setText(fct.getName());
						btnFct.setFocusable(true);

						btnFct.setOnClickListener(new OnClickListener()
						{

							@Override
							public void onClick(View arg0)
							{
								Iterator<Button> btnIterator = functBtnList.iterator();
								do
								{
									Drawable d = btnIterator.next().getBackground();
									d.clearColorFilter();
								}
								while (btnIterator.hasNext());

								Drawable d = btnFct.getBackground();
								PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED,
										android.graphics.PorterDuff.Mode.SRC_ATOP);
								d.setColorFilter(filter);
								desc.setText(fct.getDescription());
								content.invalidate();
								selectedFunction = fct;
							}
						});

						lay.addView(btnFct);
						layoutHeight += Sizes.getQuickButtonHeight();

						functBtnList.add(btnFct);
					}
					while (iteratorFunctions.hasNext());

					lay.setOrientation(LinearLayout.VERTICAL);

					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);

					// layoutParams.setMargins(20, 0, 20, 0);

					lay.setLayoutParams(layoutParams);
					lay.setBackgroundDrawable(Global.getDrawable(R.drawable.day_settings_group, res));
					lay.setVisibility(View.GONE);
					content.addView(lay);
				}

				btn.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View arg0)
					{
						Animations.ToggleViewSlideUp_Down((View) lay, context, scrollView, btn);
					}
				});

			}
			while (iteratorCat.hasNext());

		}
	}
}
