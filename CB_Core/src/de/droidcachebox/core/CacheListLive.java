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
package de.droidcachebox.core;

import com.badlogic.gdx.utils.Array;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.locator.map.Descriptor;

/**
 * This list holds the Live loaded Caches with a maximum capacity and the Descriptor for Live request.
 *
 * @author Longri
 */
public class CacheListLive {
    private final HashMap<Long, Array<Cache>> geoCachesPerDescriptor = new HashMap<>();
    private final int maxCapacity;
    private int noOfGeoCaches;
    private Descriptor mapCenterDesc;
    private final byte usedZoom;

    /**
     * Constructor
     *
     * @param _maxCapacity ?
     */
    public CacheListLive(int _maxCapacity, byte _usedZoom) {
        maxCapacity = _maxCapacity;
        usedZoom = _usedZoom;
        noOfGeoCaches = 0;
    }

    public Array<Cache> addAndReduce(Descriptor descriptor, Array<Cache> caches) {
        synchronized (geoCachesPerDescriptor) {
            if (geoCachesPerDescriptor.containsKey(descriptor.getHashCode())) {
                return null;
            } else {
                Array<Cache> cleanedCaches = removeGeoCachesNotInDesctriptorsArea(descriptor, caches);
                geoCachesPerDescriptor.put(descriptor.getHashCode(), cleanedCaches);
                noOfGeoCaches = noOfGeoCaches + cleanedCaches.size;
                return removeGeoCachesForNotToExceedCapacityLimit();
            }
        }
    }

    private Array<Cache> removeGeoCachesNotInDesctriptorsArea(Descriptor descriptor, Array<Cache> geoCachesToClean) {
        int zoom = descriptor.getZoom();
        Array<Cache> cleanedCaches = new Array<>();
        for (Cache geoCache : geoCachesToClean) {
            Descriptor descriptorOfGeoCache = new Descriptor(geoCache.getCoordinate(), zoom);
            if (descriptorOfGeoCache.equals(descriptor)) cleanedCaches.add(geoCache);
        }
        return cleanedCaches;
    }

    private Array<Cache> removeGeoCachesForNotToExceedCapacityLimit() {
        Array<Cache> removedCaches = new Array<>();
        while (noOfGeoCaches > maxCapacity && geoCachesPerDescriptor.keySet().size() > 1) {
            Descriptor descriptor = getFarestDescriptorFromMapCenter();
            if (descriptor != null) {
                removedCaches.addAll(geoCachesPerDescriptor.get(descriptor.getHashCode()));
                geoCachesPerDescriptor.remove(descriptor.getHashCode());
            }
        }
        noOfGeoCaches = noOfGeoCaches - removedCaches.size;
        return removedCaches;
    }

    private Descriptor getFarestDescriptorFromMapCenter() {
        if (mapCenterDesc == null)
            return null;

        int descX = mapCenterDesc.getX();
        int descY = mapCenterDesc.getY();

        int tmpDistance = 0;
        Descriptor tmpDesc = null;
        for (Long l2 : geoCachesPerDescriptor.keySet()) {
            Array<Cache> cl2 = geoCachesPerDescriptor.get(l2);
            Descriptor desc2 = new Descriptor(cl2.get(0).getCoordinate(), usedZoom);
            int distance = Math.abs(descX - desc2.getX()) + Math.abs(descY - desc2.getY());
            if (distance > tmpDistance) {
                tmpDistance = distance;
                tmpDesc = desc2;
            }
        }
        return tmpDesc;
    }

    public int getSize() {
        return noOfGeoCaches;
    }

    public void setCenterDescriptor(Descriptor descriptor) {
        mapCenterDesc = descriptor;
    }

    public boolean contains(Descriptor descriptor) {
        return geoCachesPerDescriptor.containsKey(descriptor.getHashCode());
    }

    public Collection<Array<Cache>> getAllCacheLists() {
        return geoCachesPerDescriptor.values();
    }

    public Set<Long> getDescriptorsHashCodes() {
        return geoCachesPerDescriptor.keySet();
    }

    /*
    public Array<Cache> getCachesOfDescriptor(Descriptor descriptor) {
        return geoCachesPerDescriptor.get(descriptor.getHashCode());
    }
     */

    public int getNoOfGeoCachesForDescriptor(Descriptor descriptor) {
        long hc = descriptor.getHashCode();
        if (geoCachesPerDescriptor.containsKey(hc)){
            return geoCachesPerDescriptor.get(hc).size;
        }
        else {
            return 0;
        }
    }
}
