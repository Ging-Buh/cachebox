/* 
 * Copyright (C) 2014 team-cachebox.de
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
package CB_Core.Types;

import java.util.HashMap;

import CB_Locator.Map.Descriptor;
import CB_Utils.Lists.CB_List;

/**
 * This list holds the Live loaded Caches with a maximum capacity and the Descriptor for Live request.
 * 
 * @author Longri
 */
public class CacheListLive
{
	private int maxCapacity = 100;
	HashMap<Descriptor, CB_List<Cache>> map;
	CB_List<Descriptor> descriptorList;

	/**
	 * Constructor
	 * 
	 * @param maxCapacity
	 */
	public CacheListLive(int maxCapacity)
	{
		this.maxCapacity = maxCapacity;
		map = new HashMap<Descriptor, CB_List<Cache>>();
		descriptorList = new CB_List<Descriptor>();
	}

	public void add(Descriptor desc, CB_List<Cache> caches)
	{
		synchronized (map)
		{
			if (descriptorList.contains(desc)) return;

			CB_List<Cache> cleanedCaches = removeExistCaches(caches);
			if (map.containsKey(desc)) return;
			map.put(desc, cleanedCaches);
			descriptorList.add(desc);
			chkCapacity();
			includedList = null;
		}
	}

	private CB_List<Cache> removeExistCaches(CB_List<Cache> caches)
	{
		CB_List<Cache> returnList = new CB_List<Cache>(caches);
		for (CB_List<Cache> list : map.values())
		{

			for (int i = 0; i < caches.size(); i++)
			{
				if (list.contains(caches.get(i))) returnList.remove(caches.get(i));
			}
		}

		// remove double

		CB_List<Cache> clearList = new CB_List<Cache>();
		for (int i = 0; i < returnList.size(); i++)
		{
			Cache ca = returnList.get(i);
			if (!clearList.contains(ca)) clearList.add(ca);
		}

		return clearList;
	}

	/**
	 * Returns the max capacity of this CacheList
	 * 
	 * @return
	 */
	public int getCapacity()
	{
		return this.maxCapacity;
	}

	private void chkCapacity()
	{

		if (descriptorList.size() > 1)
		{
			if (getSize() > maxCapacity)
			{
				// delete the Descriptor-Caches with highest distance to last added Descriptor-Caches
				Descriptor desc = getFarestDescriptorFromLast();
				if (desc == null) return; // can not clear!

				CB_List<Cache> list = map.get(desc);
				for (int i = 0; i < list.size(); i++)
				{
					Cache ca = list.get(i);
					if (ca != null && ca.isDisposed()) ca.dispose();
				}
				map.remove(desc);
				descriptorList.remove(desc);
				list.clear();
				list = null;
			}
			if (getSize() > maxCapacity) chkCapacity();
		}
	}

	private Descriptor getFarestDescriptorFromLast()
	{
		Descriptor desc = descriptorList.last();

		int descX = desc.getX();
		int descY = desc.getY();

		int tmpDistance = 0;
		Descriptor tmpDesc = null;

		for (int i = 0; i < descriptorList.size() - 1; i++)
		{
			Descriptor desc2 = descriptorList.get(i);

			int distance = Math.abs(descX - desc2.getX()) + Math.abs(descY - desc.getY());

			if (distance > tmpDistance)
			{
				tmpDistance = distance;
				tmpDesc = desc2;
			}

		}

		return tmpDesc;

	}

	public int getSize()
	{
		synchronized (map)
		{
			if (includedList != null) return includedList.size();

			int count = 0;
			for (CB_List<Cache> list : map.values())
			{
				count += list.size();
			}
			return count;
		}
	}

	public boolean contains(Cache ca)
	{
		synchronized (map)
		{
			if (includedList != null) return includedList.contains(ca);

			for (CB_List<Cache> list : map.values())
			{
				if (list.contains(ca)) return true;
			}
			return false;
		}
	}

	CB_List<Cache> includedList = null;

	public Cache get(int i)
	{
		synchronized (map)
		{

			if (includedList == null)
			{
				includedList = new CB_List<Cache>();

				for (CB_List<Cache> list : map.values())
				{
					includedList.addAll(list);
				}
			}

			return includedList.get(i);
		}
	}
}