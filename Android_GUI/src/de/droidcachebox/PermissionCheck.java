/*
 * Copyright (C) 2016 team-cachebox.de
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
package de.droidcachebox;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Longri on 11.05.2016.
 */
public class PermissionCheck {

    public static final int MY_PERMISSIONS_REQUEST = 11052016;

    private static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    private static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    private static final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    private static final String WAKE_LOCK = "android.permission.WAKE_LOCK";
    private static final String INTERNET = "android.permission.INTERNET";
    private static final String ACCESS_NETWORK_STATE = "android.permission.ACCESS_NETWORK_STATE";
    private static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    private static final String CAMERA = "android.permission.CAMERA";
    private static final String VIBRATE = "android.permission.VIBRATE";
    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private static final String FOREGROUND_SERVICE = "android.permission.FOREGROUND_SERVICE";
    public static ArrayList<String> neededPermissions;

    public static void checkNeededPermissions(Activity context) {
        neededPermissions = new ArrayList<>(
                Arrays.asList(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        WAKE_LOCK,
                        INTERNET,
                        ACCESS_NETWORK_STATE,
                        RECORD_AUDIO,
                        CAMERA,
                        VIBRATE,
                        WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE
                ));

        ArrayList<String> DENIED_List = new ArrayList<>();

        for (String permission : neededPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                DENIED_List.add(permission);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(context, FOREGROUND_SERVICE); // result doesn't matter
        }

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(context, ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               // todo ? don't start service
            };
        }

        if (!DENIED_List.isEmpty()) {
            ActivityCompat.requestPermissions(context, DENIED_List.toArray(new String[0]), MY_PERMISSIONS_REQUEST);
        }

    }

}
