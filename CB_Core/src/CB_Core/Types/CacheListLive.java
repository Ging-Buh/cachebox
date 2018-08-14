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

import CB_Locator.Map.Descriptor;
import CB_Utils.Lists.CB_List;

import java.util.HashMap;

/**
 * This list holds the Live loaded Caches with a maximum capacity and the Descriptor for Live request.
 *
 * @author Longri
 */
public class CacheListLive {
    HashMap<Descriptor, CB_List<Cache>> map;
    CB_List<Cache> includedList = null;
    private int maxCapacity = 100;
    private CB_List<Descriptor> descriptorList;
    private Descriptor MapCenterDesc;

    /**
     * Constructor
     *
     * @param maxCapacity
     */
    public CacheListLive(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        map = new HashMap<Descriptor, CB_List<Cache>>();
        descriptorList = new CB_List<Descriptor>();
    }

    public CB_List<Cache> add(Descriptor desc, CB_List<Cache> caches) {
        synchronized (map) {
            if (getDescriptorList().contains(desc))
                return null;

            CB_List<Cache> cleanedCaches = removeExistCaches(caches);
            if (map.containsKey(desc))
                return null;
            includedList = null;
            map.put(desc, cleanedCaches);
            getDescriptorList().add(desc);
            return chkCapacity();
        }
    }

    private CB_List<Cache> removeExistCaches(CB_List<Cache> caches) {
        if (caches == null || caches.size() == 0)
            return new CB_List<Cache>();
        CB_List<Cache> returnList = new CB_List<Cache>(caches);
        for (CB_List<Cache> list : map.values()) {

            for (int i = 0; i < caches.size(); i++) {
                if (list.contains(caches.get(i)))
                    returnList.remove(caches.get(i));
            }
        }

        // remove double

        CB_List<Cache> clearList = new CB_List<Cache>();
        for (int i = 0; i < returnList.size(); i++) {
            Cache ca = returnList.get(i);
            if (!clearList.contains(ca))
                clearList.add(ca);
        }

        return clearList;
    }

    /**
     * Returns the max capacity of this CacheList
     *
     * @return
     */
    public int getCapacity() {
        return this.maxCapacity;
    }

    private CB_List<Cache> chkCapacity() {
        CB_List<Cache> removeList = new CB_List<Cache>();
        if (getDescriptorList().size() > 1) {
            if (getSize() > maxCapacity) {
                // delete the Descriptor-Caches with highest distance to last added Descriptor-Caches
                Descriptor desc = getFarestDescriptorFromMapCenter();
                if (desc == null)
                    return removeList; // can not clear!

                removeList = map.get(desc);
                for (int i = 0; i < removeList.size(); i++) {
                    Cache ca = removeList.get(i);
                    if (ca != null && ca.isDisposed())
                        ca.dispose();
                }
                map.remove(desc);
                getDescriptorList().remove(desc);
                includedList = null;
            }
            if (getSize() > maxCapacity)
                removeList.addAll(chkCapacity());
        }
        return removeList;
    }

    private Descriptor getFarestDescriptorFromMapCenter() {
        if (MapCenterDesc == null)
            return null;

        int descX = MapCenterDesc.getX();
        int descY = MapCenterDesc.getY();

        int tmpDistance = 0;
        Descriptor tmpDesc = null;

        for (int i = 0; i < getDescriptorList().size() - 1; i++) {
            Descriptor desc2 = getDescriptorList().get(i);

            int distance = Math.abs(descX - desc2.getX()) + Math.abs(descY - desc2.getY());

            if (distance > tmpDistance) {
                tmpDistance = distance;
                tmpDesc = desc2;
            }

        }
        return tmpDesc;
    }

    public int getSize() {
        synchronized (map) {
            if (includedList != null)
                return includedList.size();

            int count = 0;
            for (CB_List<Cache> list : map.values()) {
                count += list.size();
            }
            return count;
        }
    }

    public boolean contains(Cache ca) {
        synchronized (map) {
            if (includedList != null)
                return includedList.contains(ca);

            for (CB_List<Cache> list : map.values()) {
                if (list.contains(ca))
                    return true;
            }
            return false;
        }
    }

    public Cache get(int i) {
        synchronized (map) {

            if (includedList == null) {
                includedList = new CB_List<Cache>();

                for (CB_List<Cache> list : map.values()) {
                    includedList.addAll(list);
                }
            }

            try {
                return includedList.get(i);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public boolean contains(Descriptor desc) {
        return getDescriptorList().contains(desc);
    }

    public CB_List<Descriptor> getDescriptorList() {
        return descriptorList;
    }

    public void setCenterDescriptor(Descriptor desc) {
        this.MapCenterDesc = desc;
    }

}
