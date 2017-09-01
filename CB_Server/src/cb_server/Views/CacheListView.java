package cb_server.Views;

import java.io.Serializable;
import java.util.HashMap;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import cb_server.Events.SelectedCacheChangedEventList;
import cb_server.Events.SelectedCacheChangedEventListner;

public class CacheListView extends CB_ViewBase implements SelectedCacheChangedEventListner {

	private static final long serialVersionUID = -8341714748837951953L;
	public Table table;
	private final CacheContainer beans;
	private final HashMap<Long, CacheBean> cacheBeans = new HashMap<Long, CacheListView.CacheBean>();
	private final String host;

	public CacheListView() {
		super();
		host = com.vaadin.server.Page.getCurrent().getLocation().getScheme() + "://" + com.vaadin.server.Page.getCurrent().getLocation().getAuthority() + "/";
		beans = new CacheContainer();
		// beans.setBeanIdProperty("GCCode");

		//		for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
		//			beans.addBean(new CacheBean(Database.Data.Query.get(i)));
		//		}

		this.table = new Table("CacheList", beans);
		this.setCompositionRoot(table);
		this.setSizeFull();
		table.setSizeFull();
		table.setSelectable(true);
		table.setImmediate(true);

		//		table.addGeneratedColumn("NewCol", new DescriptionColumnGenerator());
		//		table.setColumnHeader("NewCol", "NC");
		// Have to set explicitly to hide the "equatorial" property
		//		table.setVisibleColumns(new Object[]{"Description"});

		table.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -1246546962581855595L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object o = table.getValue();
				if (o instanceof CacheBean) {
					Cache cache = ((CacheBean) o).cache;
					Waypoint waypoint = cache.GetFinalWaypoint();
					if (waypoint == null) {
						waypoint = cache.GetStartWaypoint();
					}
					if (!doNotChange) {
						doNotChange = true;
						SelectedCacheChangedEventList.Call(cache, waypoint);
						doNotChange = false;
					}
				}
			}
		});
		/*		table.addGeneratedColumn("icon", new Table.ColumnGenerator() {
					private static final long serialVersionUID = 5199037506976926798L;
		
					@Override
					public Object generateCell(Table source, Object itemId, Object columnId) {
						Cache cache = null;
						if (itemId instanceof CacheBean) {
							cache = ((CacheBean)itemId).cache;
						}
						return new Embedded("", new ExternalResource(getCacheIcon(cache, 16, 0, false, false) + ".png"));
					}
					
				});
		*/
	}

	private String getCacheIcon(Cache cache, int iconSize, int backgroundSize, boolean selected, boolean showDT) {
		String url = host + "ics/";
		url += "C";
		url += String.format("%02d", cache.Type.ordinal()); // 2 stellig
		if (cache.isArchived())
			url += "A";
		if (!cache.isAvailable())
			url += "N";
		if (cache.isFound())
			url += "F";
		if (cache.ImTheOwner())
			url += "O";
		if (cache.CorrectedCoordiantesOrMysterySolved())
			url += "S";
		if (cache.HasStartWaypoint())
			url += "T";
		if (selected)
			url += "L";
		if (showDT) {
			url += "_D" + (int) (cache.getDifficulty() * 2);
			url += "_T" + (int) (cache.getTerrain() * 2);
		}
		url += "_S" + iconSize;
		if (backgroundSize > 0) {
			url += "_B" + backgroundSize;
		}
		return url + ".png";
	}

	@Override
	public void cacheListChanged() {
		super.cacheListChanged();
		log.debug("Remove all Beans");
		beans.removeAllItems();
		cacheBeans.clear();
		log.debug("Add new Beans for new CacheList");
		try {
			if (table.getUI() != null) {
				table.getUI().getSession().lock();
			}
			try {
				for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
					Cache cache = Database.Data.Query.get(i);
					CacheBean bean = new CacheBean(cache);
					cacheBeans.put(cache.Id, bean);
					BeanItem<CacheBean> item = beans.addBean(bean);
					table.setItemIcon(item, new ExternalResource(getCacheIcon(Database.Data.Query.get(i), 16, 0, false, false) + ".png"));
				}
			} finally {
				if (table.getUI() != null) {
					table.getUI().getSession().unlock();
				}
			}
			table.setVisibleColumns(new Object[] { "icon", "GCCode", "name", "state", "country" });
		} catch (Exception ex) {
			System.out.println("lskjdl");
		}
		if (getUI() != null) {
			getUI().push();
		}
		log.debug("Finished add Beans");
	}

	class DescriptionColumnGenerator implements Table.ColumnGenerator {
		private static final long serialVersionUID = 3741451390162331681L;

		/**
		 * Generates the cell containing the Date value. The column is
		 * irrelevant in this use case.
		 */
		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
			Item it = source.getItem(itemId);
			it.getItemPropertyIds();
			Property prop = source.getItem(itemId).getItemProperty("GCCode");
			//            if (prop.getType().equals(String.class)) {
			Label label = new Label((String) prop.getValue() + "--");
			label.addStyleName("column-type-date");
			return label;
			//            }

			//            return null;
		}
	}

	public class CacheBean implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5457794531550146509L;
		private String GCCode;
		private String Name;
		private String State;
		private String Country;
		private final Cache cache;
		private Resource icon;

		public CacheBean(Cache cacheLite) {
			this.cache = cacheLite;
			this.setGCCode("");
			this.setName("");
			this.setState("");
			this.setCountry("");
		}

		public String getName() {
			return cache.getName();
		}

		public void setName(String name) {
			Name = name;
		}

		public String getGCCode() {
			return cache.getGcCode();
		}

		public void setGCCode(String gCCode) {
			GCCode = gCCode;
		}

		public void setState(String state) {
			this.State = state;
		}

		public String getState() {
			return cache.getState();
		}

		public void setCountry(String desc) {
			this.Country = desc;
		}

		public String getCountry() {
			return cache.getCountry();
		}

		public Embedded getIcon() {
			return new Embedded("", new ExternalResource(getCacheIcon(cache, 16, 0, false, false) + ".png"));
		}
	}

	public class CacheContainer extends BeanItemContainer<CacheBean> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2808380911891848063L;

		public CacheContainer() {
			super(CacheBean.class);
		}

	}

	private boolean doNotChange = false;

	@Override
	public void SelectedCacheChangedEvent(Cache cache2, Waypoint waypoint, boolean cacheChanged, boolean waypointChanged) {
		if (doNotChange)
			return;
		System.out.println("lsdjflkasjlfasjklfdjasfsj");
		CacheBean bean = cacheBeans.get(cache2.Id);
		if (bean != null) {
			table.focus();
			doNotChange = true;
			table.select(bean);
			table.setCurrentPageFirstItemId(bean);
			doNotChange = false;
		}
	}

}
