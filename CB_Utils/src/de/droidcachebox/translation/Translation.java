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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author Longri
 */
public class Translation {
    private static final String log = "Translation";
    /**
     * @uml.property name="that"
     * @uml.associationEnd
     */
    public static Translation that;
    public final Array<MissingTranslation> mMissingStringList;
    private Array<Translations> translations;
    private Array<Translations> references;
    private final String mWorkPath;
    private FileType mFiletype;
    private String mLangID;
    private String langPath;

    /**
     * Constructor
     *
     * @param workPath
     * @param internal true for loading from asset
     */
    public Translation(String workPath, FileType internal) {
        that = this;
        mWorkPath = workPath;
        mMissingStringList = new Array<>();
        mFiletype = internal;
    }

    // #######################################################################
    // Public static access
    // #######################################################################

    /**
     * Load the Translation from File
     *
     * @param langPath
     * @throws IOException
     */
    public void loadTranslation(String langPath) {
        this.langPath = langPath;
        readTranslationsFile(this.langPath);
    }

    /**
     * Returns the translation from StringID </br>
     * with params ??????
     *
     * @param StringId as String
     * @param params  The occurences of {n} in the translation will be replaced by the n'th param
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
     * @throws IOException
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
     * Return a String from File
     *
     * @param Name File Name
     * @return String from File
     * @throws IOException
     */
    public static String GetTextFile(String Name, String overrideLangId) throws IOException {
        if (that == null)
            return "Translation not initial";
        return that.getTextFile(Name, overrideLangId);
    }

    /**
     * Returns true if Translation initial and reference language is loaded
     *
     * @return boolean
     */
    public static boolean isInitialized() {
        if (that != null && that.references != null)
            return true;
        return false;
    }

    // #######################################################################
    // Private access
    // #######################################################################

    private String getLangNameFromFile(String FilePath) throws IOException {

        FileHandle lang = Gdx.files.getFileHandle(FilePath, mFiletype);
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

        SelectedLangChangedEventList.Call();
    }

    private Array<Translations> readFile(String FilePath, boolean asTranslation) {
        Array<Translations> result = new Array<>();
        FileHandle file = Gdx.files.getFileHandle(FilePath, mFiletype);

        String text = file.readString("UTF-8");

        String[] lines = text.split("\n");
        for (String line : lines) {
            int pos;

            // skip empty lines
            if (line == "") {
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
                result.add(new Translations(readID, replacedRead));
            } else {
                if (getTranslation(readID).startsWith("$ID: "))
                    result.add(new Translations(readID, replacedRead));
            }
        }
        return result;
    }

    private String getTranslation(String StringId, String... params) {
        String retString = getTranslation(StringId.hashCode(), params);
        if (retString == "") {
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

        if (translations != null) {
            for (int i = 0, n = translations.size; i < n; i++) {
                Translations tmp = translations.get(i);
                if (tmp.getIdString() == Id) {
                    retString = tmp.getTranslation();
                    break;
                }
            }
        }

        if (references != null) {
            if (retString.length() == 0) {
                for (int i = 0, n = references.size; i < n; i++) {
                    Translations tmp = references.get(i);
                    if (tmp.getIdString() == Id) {
                        retString = tmp.getTranslation();
                        break;
                    }
                }
            }
        }

        if (retString.length() == 0) {
            if (translations == null || references == null)
                retString = "Translation not initialized";
        }
        else {
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

    public ArrayList<Lang> getLangs(String FilePath) {
        ArrayList<Lang> Temp = new ArrayList<Lang>();

        FileHandle Dir = Gdx.files.getFileHandle(FilePath, mFiletype);
        final FileHandle[] files;

        if (Dir.type() == FileType.Classpath) {
            // Cannot list a classpath directory
            // so we hardcoded the lang path
            files = new FileHandle[]{ //
                    Gdx.files.classpath("data/lang/cs"), //
                    Gdx.files.classpath("data/lang/de"), //
                    Gdx.files.classpath("data/lang/en-GB"), //
                    Gdx.files.classpath("data/lang/fr"), //
                    Gdx.files.classpath("data/lang/hu"), //
                    Gdx.files.classpath("data/lang/nl"), //
                    Gdx.files.classpath("data/lang/pl"), //
                    Gdx.files.classpath("data/lang/pt-PT"),//
            };
        } else {
            files = Dir.list();
        }

        for (FileHandle tmp : files) {
            try {

                String stringFile = tmp + "/strings.ini";

                FileHandle langFile = Gdx.files.getFileHandle(stringFile, mFiletype);

                if (langFile.exists()) {
                    String tmpName = getLangNameFromFile(stringFile);
                    Temp.add(new Lang(tmpName, stringFile));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return Temp;
    }

    private void writeMissingStrings() throws IOException {
        File file = FileFactory.createFile(mWorkPath + "/debug.txt");

        if (file.exists()) {

            BufferedReader reader = new BufferedReader(file.getFileReader());
            StringBuilder sb = new StringBuilder();
            String line;

            boolean override = false;

            while ((line = reader.readLine()) != null) {
                if (!override)
                    sb.append(line + "\n\r");
                if (line.contains("##########  Missing Lang Strings ######")) {
                    // Beginn des schreibbereichs
                    for (int i = 0, n = mMissingStringList.size; i < n; i++) {
                        if (i >= mMissingStringList.size)
                            break;
                        MissingTranslation tmp = mMissingStringList.get(i);
                        sb.append(tmp.getMissingString() + "\n\r");
                    }
                    override = true;
                }
                if (override && line.contains("############################")) {
                    // jetzt kann weiter gelesen werden
                    override = false;
                    sb.append(line + "\n\r");
                }
            }
            reader.close();

            PrintWriter writer = new PrintWriter(file.getFileWriter());

            writer.write(sb.toString());
            writer.close();

        }

    }

    /*
    private void readMissing() throws IOException {
        File file = FileFactory.createFile(mWorkPath + "/debug.txt");
        BufferedReader reader = new BufferedReader(file.getFileReader());
        String line;

        boolean read = false;

        while ((line = reader.readLine()) != null) {
            if (read && line.contains("############################"))
                break;

            if (read) {
                MissingTranslation notFound = null;
                try {
                    notFound = new MissingTranslation(line, "??");
                } catch (Exception e) {
                }
                if (notFound != null && !mMissingStringList.contains(notFound)) {
                    mMissingStringList.add(notFound);
                }
            }

            if (line.contains("##########  Missing Lang Strings ######"))
                read = true;

        }
        reader.close();

    }
     */

    private String getTextFile(String Name, String overrideLangId) throws IOException {

        String FilePath = "data/string_files/" + Name + "." + overrideLangId + ".txt";
        FileHandle file = Gdx.files.getFileHandle(FilePath, FileType.Internal);
        String text = file.readString();
        return text;
    }

}
