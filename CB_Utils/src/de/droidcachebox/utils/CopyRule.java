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

package de.droidcachebox.utils;

/**
 * A structure inherits the Name,SourcePath and TargetPath of an Copy job
 *
 * @author Longri
 */
public class CopyRule {
    public File sourcePath;
    public File targetPath;
    public String Name;

    /**
     * Constructor
     *
     * @param source SourcePath as String
     * @param target TargetPath as String
     */
    public CopyRule(String source, String target) {
        sourcePath = FileFactory.createFile(source);
        Name = sourcePath.getName();
        targetPath = FileFactory.createFile(target + "/" + Name);
    }

}
