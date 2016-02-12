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

import CB_Core.CB_Core.Import.GPX_Unzip_Import_Test;
import CB_Core.CB_Core.Import.GSAKGpxImportTest;
import CB_Core.CB_Core.Import.GpxImportTest;
import CB_Core.CB_Core.Import.IndexDBTest;
import CB_Core.CB_Core.Import.UnzipTest;

@RunWith(Suite.class)
@SuiteClasses({ GPX_Unzip_Import_Test.class, GpxImportTest.class, GSAKGpxImportTest.class, UnzipTest.class, IndexDBTest.class })
public class Import_tests {

}
