package de.droidcachebox.Custom_Controls.IconContextMenu;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import CB_Core.Config;
import android.content.Context;
import android.view.*;
import android.widget.*;

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
        	res = (TextView) LayoutInflater.from(context).inflate(android.R.layout.select_dialog_item, null);
        }
        
        //set Text Color (day or night)
        res.setTextColor(Global.getColor(R.attr.TextColor));

        res.setTag(item);
        res.setText(item.getTitle());
        if(item.isCheckable())
        {
        	if(item.isChecked())
        	{
        		res.setCompoundDrawablesWithIntrinsicBounds( Global.ChkIcons[1], null,item.getIcon(), null);
        	}
        	else
        	{
        		res.setCompoundDrawablesWithIntrinsicBounds( Global.ChkIcons[0], null,item.getIcon(), null);
        	}
        }
        else
        {
        	res.setCompoundDrawablesWithIntrinsicBounds( null, null,item.getIcon(), null);
        }
        
              
        res.setEnabled(item.isEnabled());
        return res;
    }
}
