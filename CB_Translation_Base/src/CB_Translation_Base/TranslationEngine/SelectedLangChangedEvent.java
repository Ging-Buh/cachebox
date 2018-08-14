/*
 * Copyright (C) 2013 team-cachebox.de
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

package CB_Translation_Base.TranslationEngine;

/**
 * this is an interface for all Objects which should receive the selectedCacheChanged Event
 *
 * @author Longri
 */
public interface SelectedLangChangedEvent {
    public void SelectedLangChangedEventCalled();
}
