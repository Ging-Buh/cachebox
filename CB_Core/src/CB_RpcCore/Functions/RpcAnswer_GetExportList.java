package CB_RpcCore.Functions;

import java.io.Serializable;
import java.util.ArrayList;

import cb_rpc.Functions.RpcAnswer;

public class RpcAnswer_GetExportList extends RpcAnswer
{
	private static final long serialVersionUID = 2379129141743406809L;
	private ArrayList<ListItem> list;

	public RpcAnswer_GetExportList(int result)
	{
		super(result);
		list = new ArrayList<ListItem>();
	}

	public void addListItem(int id, String description, int cacheCount)
	{
		ListItem it = new ListItem(id, description, cacheCount);
		list.add(it);
	}

	public ArrayList<ListItem> getList()
	{
		return list;
	}

	public void setList(ArrayList<ListItem> list)
	{
		this.list = list;
	}

	public class ListItem implements Serializable
	{

		private static final long serialVersionUID = 8029139607543312389L;
		private int id;
		private String description;
		private int cacheCount;
		private boolean download;

		public ListItem(int id, String description, int cacheCount)
		{
			this.id = id;
			this.description = description;
			this.setCacheCount(cacheCount);
			this.download = false;
		}

		public int getId()
		{
			return id;
		}

		public void setId(int id)
		{
			this.id = id;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public int getCacheCount()
		{
			return cacheCount;
		}

		public void setCacheCount(int cacheCount)
		{
			this.cacheCount = cacheCount;
		}

		public void setDownload(boolean download)
		{
			this.download = download;
		}

		public boolean getDownload()
		{
			return this.download;
		}

	}
}
