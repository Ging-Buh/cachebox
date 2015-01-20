/* 
 * Copyright (C) 2011-2014 team-cachebox.de
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
package ___Test_Suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import API.searchForGeoCache_Test;
import CB_Core.CB_Core.Api.Bug384;
import CB_Core.CB_Core.Api.PQ_Download;
import CB_Core.CB_Core.Api.PocketQueryTest;
import CB_Core.CB_Core.Api.Trackable_Test;
import CB_Core.CB_Core.Api.isPremium_GetFound_Test;
import CB_Utils.Downloader_test;

@RunWith(Suite.class)
@SuiteClasses(
	{ searchForGeoCache_Test.class, Bug384.class, isPremium_GetFound_Test.class, PocketQueryTest.class, PQ_Download.class,
			Trackable_Test.class, Downloader_test.class })
public class Online_tests
{

}
