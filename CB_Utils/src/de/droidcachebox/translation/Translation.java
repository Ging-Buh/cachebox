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

package de.droidcachebox.translation;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;

/**
 * @author Longri
 */
public class Translation {
    public static Translation that;
    public final Array<MissingTranslation> mMissingStringList;
    private final String mWorkPath;
    private Properties translations;
    private Properties references;
    private String mLangID;

    /**
     * Constructor
     *
     * @param workPath ?
     */
    public Translation(String workPath) {
        that = this;
        mWorkPath = workPath;
        mMissingStringList = new Array<>();
    }

    /**
     * Returns the translation from StringID </br>
     * with params ??????
     *
     * @param StringId as String
     * @param params   The occurences of {n} in the translation will be replaced by the n'th param
     * @return String
     */
    public static String get(String StringId, String... params) {
        if (that == null)
            return "Translation not initial";
        return that.getTranslation(StringId, params);
    }

    /**
     * Returns the actual loaded LangID
     *
     * @return LangID as String
     */
    public static String getLangId() {
        if (that == null)
            return "Translation not initial";
        return that.mLangID;
    }

    /**
     * Write list of missing StringId's to debug.txt
     *
     */
    public static void writeMissingStringsToFile() {
        if (that != null)
            try {
                that.writeMissingStrings();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Returns true if Translation initial and reference language is loaded
     *
     * @return boolean
     */
    public static boolean isInitialized() {
        return that != null && that.references != null;
    }

    /**
     * Load the Translation from File
     *
     * @param langPath ?
     */
    public void loadTranslation(String langPath) {
        readTranslationsFile(langPath);
    }

    /**
     * Return a String from File
     *
     * @param Name File Name
     * @return String from File
     */
    public String getTextFile(String Name, String overrideLangId) {
        String FilePath = "data/string_files/" + Name + "." + overrideLangId + ".txt";
        FileHandle file = FileFactory.getInternalFileHandle(FilePath);
        return file.readString();
    }

    // #######################################################################
    // Private access
    // #######################################################################

    private String getLangNameFromFile(String filePath) {

        FileHandle lang = FileFactory.getInternalFileHandle(filePath);
        String langRead = lang.readString();

        int pos1 = langRead.indexOf("lang=") + 5;
        int pos2 = langRead.indexOf("\n", pos1);
        String Value = langRead.substring(pos1, pos2);
        if (Value.endsWith("\r"))
            Value = langRead.substring(pos1, pos2 - 1);
        return Value;
    }

    private void readTranslationsFile(String filePath) {
        if (filePath.equals("")) {
            return;
        }

        translations = readFile(filePath, true);

        // read refFile (en-GB)
        String fileName = FileIO.getFileName(filePath);
        int pos = filePath.lastIndexOf("lang") + 4;
        String LangFileName = filePath.substring(0, pos) + "/en-GB/" + fileName;
        references = readFile(LangFileName, false);

        // mLangID = getLangNameFromFile(filePath);
        mLangID = getTranslation("lang");

        LanguageChanged.fire();
    }

    private Properties readFile(String filePath, boolean asTranslation) {
        Properties result = new Properties();
        FileHandle file = FileFactory.getInternalFileHandle(filePath);

        String text = file.readString("UTF-8");

        String[] lines = text.split("\n");
        for (String line : lines) {
            int pos;

            // skip empty lines
            if (line.length() == 0) {
                continue;
            }

            // skip line without value
            pos = line.indexOf("=");
            if (pos == -1) {
                continue;
            }

            // skip comment line
            if (line.startsWith("//")) {
                continue;
            }

            String readID = line.substring(0, pos).trim();
            String readTransl;
            if (line.endsWith("\r"))
                readTransl = line.substring(pos + 1, line.length() - 1);
            else
                readTransl = line.substring(pos + 1);
            String replacedRead = readTransl.trim().replace("\\n", "\n");
            if (replacedRead.endsWith("\"")) {
                replacedRead = replacedRead.substring(0, replacedRead.length() - 1);
            }
            if (replacedRead.startsWith("\"")) {
                replacedRead = replacedRead.substring(1);
            }
            replacedRead = replacedRead.replace("\\\"", "\"");
            if (asTranslation) {
                result.put(readID, replacedRead);
            } else {
                if (getTranslation(readID).startsWith("$ID: "))
                    result.put(readID, replacedRead);
            }
        }
        return result;
    }

    private String getTranslation(String StringId, String... params) {
        String retString = new String();
        if (translations != null && translations.containsKey(StringId)) {
            retString = translations.getProperty(StringId);
        }
        if (retString.isEmpty() && references != null && references.containsKey(StringId)) {
            retString = references.getProperty(StringId);
        }
        if (retString.isEmpty()) {
            retString = getTranslation(StringId.hashCode(), params);
        }
        if (retString.isEmpty()) {
            retString = "$ID: " + StringId;// "No translation found";

            MissingTranslation notFound = new MissingTranslation(StringId, "??");
            if (!mMissingStringList.contains(notFound, true)) {
                mMissingStringList.add(notFound);
            }
            return retString;
        }
        return retString;
    }

    private String getTranslation(int Id, String... params) {
        String retString = "";

        if (retString.length() == 0) {
            if (translations == null || references == null)
                retString = "Translation not initialized";
        } else {
            if (params != null && params.length > 0) {
                retString = replaceParams(retString, params);
            }
        }

        return retString;
    }

    private String replaceParams(String retString, String... params) {
        int i = 1;
        for (String param : params) {
            if ((param.length() >= 1) && (param.charAt(0) == '$'))
                param = get(param.substring(1));
            retString = retString.replace("{" + i + "}", param);
            i++;
        }
        return retString;
    }

    public ArrayList<Lang> getLangs(String filePath) {
        ArrayList<Lang> langs = new ArrayList<>();
        FileHandle directory = FileFactory.getInternalFileHandle(filePath);
        FileHandle[] files = directory.list();
        for (FileHandle tmp : files) {
            String stringFile = tmp + "/strings.ini";
            FileHandle langFile = FileFactory.getInternalFileHandle(filePath);
            if (langFile.exists()) {
                String tmpName = getLangNameFromFile(stringFile);
                langs.add(new Lang(tmpName, stringFile));
            }
        }
        return langs;
    }

    private void writeMissingStrings() throws IOException {
        AbstractFile abstractFile = FileFactory.createFile(mWorkPath + "/debug.txt");

        if (abstractFile.exists()) {

            BufferedReader reader = new BufferedReader(abstractFile.getFileReader());
            StringBuilder sb = new StringBuilder();
            String line;

            boolean override = false;

            while ((line = reader.readLine()) != null) {
                if (!override)
                    sb.append(line).append("\n\r");
                if (line.contains("##########  Missing Lang Strings ######")) {
                    for (int i = 0, n = mMissingStringList.size; i < n; i++) {
                        if (i >= mMissingStringList.size)
                            break;
                        MissingTranslation tmp = mMissingStringList.get(i);
                        sb.append(tmp.getMissingString()).append("\n\r");
                    }
                    override = true;
                }
                if (override && line.contains("############################")) {
                    // jetzt kann weiter gelesen werden
                    override = false;
                    sb.append(line).append("\n\r");
                }
            }
            reader.close();

            PrintWriter writer = new PrintWriter(abstractFile.getFileWriter());

            writer.write(sb.toString());
            writer.close();

        }

    }

}
