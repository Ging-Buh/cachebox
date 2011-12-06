package de.cachebox_test.Custom_Controls.IconContextMenu;

import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.Ui.Sizes;
import CB_Core.Config;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.graphics.PorterDuff.Mode;

public class IconContextMenuAdapter extends BaseAdapter {
	private Context context;
	private Menu menu;

	public IconContextMenuAdapter(Context context, Menu menu) {
		this.context = context;
		this.menu = menu;
	}

	@Override
	public int getCount() {
		return menu.size();
	}

	@Override
	public MenuItem getItem(int position) {
		return menu.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getItemId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MenuItem item = getItem(position);

		TextView res = (TextView) convertView;
		if (res == null) {
//			res = (TextView) LayoutInflater.from(context).inflate(
//					android.R.layout.select_dialog_item, null);
			res = (TextView) LayoutInflater.from(context).inflate(
					R.layout.icon_context_menu_item_layout, null);
		}

		

		res.setTag(item);
		res.setText(item.getTitle());

		if (!item.isEnabled()) {
			if (item.getIcon() != null)
				item.getIcon().setColorFilter(
						Global.getColor(R.attr.TextColor_disable), Mode.SRC_IN);
			res.setTextColor(Global.getColor(R.attr.TextColor_disable));
		} else {
			if (item.getIcon() != null)
				item.getIcon().setColorFilter(null);
			res.setTextColor(Global.getColor(R.attr.TextColor));
		}

		if (item.isCheckable()) {
			if (item.isChecked()) {
				res.setCompoundDrawablesWithIntrinsicBounds(Global.ChkIcons[1],
						null, item.getIcon(), null);
			} else {
				res.setCompoundDrawablesWithIntrinsicBounds(Global.ChkIcons[0],
						null, item.getIcon(), null);
			}
		} else {
			res.setCompoundDrawablesWithIntrinsicBounds(null, null,
					item.getIcon(), null);
		}

		res.setEnabled(item.isEnabled());

		res.setClickable(!item.isEnabled());

		res.setMinHeight(40);
		
		res.setHeight(Sizes.getIconContextMenuHeight());
		return res;
	}
}
