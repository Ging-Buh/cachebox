package de.droidcachebox.gdx.texturepacker;

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import com.badlogic.gdx.utils.*;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * @author Nathan Sweet
 */
public class TexturePackerFileProcessor extends FileProcessor {
    private final Settings defaultSettings;
    ArrayList<AbstractFile> ignoreDirs = new ArrayList<AbstractFile>();
    private ObjectMap<AbstractFile, Settings> dirToSettings = new ObjectMap<AbstractFile, Settings>();
    private Json json = new Json();
    private String packFileName;
    private AbstractFile root;

    public TexturePackerFileProcessor() {
        this(new Settings(), "pack.atlas");
    }

    public TexturePackerFileProcessor(Settings defaultSettings, String packFileName) {
        this.defaultSettings = defaultSettings;

        if (packFileName.indexOf('.') == -1)
            packFileName += ".atlas";
        this.packFileName = packFileName;

        setFlattenOutput(true);
        addInputSuffix(".png", ".jpg");
    }

    public ArrayList<Entry> process(AbstractFile inputAbstractFile, AbstractFile outputRoot) throws Exception {
        root = inputAbstractFile;

        // Collect pack.json setting files.
        final ArrayList<AbstractFile> settingsAbstractFiles = new ArrayList<AbstractFile>();
        FileProcessor settingsProcessor = new FileProcessor() {
            protected void processFile(Entry inputFile) throws Exception {
                settingsAbstractFiles.add(inputFile.inputAbstractFile);
            }
        };
        settingsProcessor.addInputRegex("pack\\.json");
        settingsProcessor.process(inputAbstractFile, null);
        // Sort parent first.
        Collections.sort(settingsAbstractFiles, new Comparator<AbstractFile>() {
            public int compare(AbstractFile abstractFile1, AbstractFile abstractFile2) {
                return abstractFile1.toString().length() - abstractFile2.toString().length();
            }
        });
        for (AbstractFile settingsAbstractFile : settingsAbstractFiles) {
            // Find first parent with settings, or use defaults.
            Settings settings = null;
            AbstractFile parent = settingsAbstractFile.getParentFile();
            while (true) {
                if (parent.equals(root))
                    break;
                parent = parent.getParentFile();
                settings = dirToSettings.get(parent);
                if (settings != null) {
                    settings = new Settings(settings);
                    break;
                }
            }
            if (settings == null)
                settings = new Settings(defaultSettings);
            // Merge settings from current directory.
            try {
                json.readFields(settings, new JsonReader().parse(settingsAbstractFile.getFileReader()));
            } catch (SerializationException ex) {
                throw new GdxRuntimeException("Error reading settings file: " + settingsAbstractFile, ex);
            }
            dirToSettings.put(settingsAbstractFile.getParentFile(), settings);
        }

        // Do actual processing.
        return super.process(inputAbstractFile, outputRoot);
    }

    public ArrayList<Entry> process(AbstractFile[] abstractFiles, AbstractFile outputRoot) throws Exception {
        // Delete pack file and images.
        if (outputRoot.exists()) {
            FileFactory.createFile(outputRoot, packFileName).delete();
            FileProcessor deleteProcessor = new FileProcessor() {
                protected void processFile(Entry inputFile) throws Exception {
                    inputFile.inputAbstractFile.delete();
                }
            };
            deleteProcessor.setRecursive(false);

            String prefix = packFileName;
            int dotIndex = prefix.lastIndexOf('.');
            if (dotIndex != -1)
                prefix = prefix.substring(0, dotIndex);
            deleteProcessor.addInputRegex(Pattern.quote(prefix) + "\\d*\\.(png|jpg)");

            deleteProcessor.process(outputRoot, null);
        }
        return super.process(abstractFiles, outputRoot);
    }

    protected void processDir(Entry inputDir, ArrayList<Entry> files) throws Exception {
        if (ignoreDirs.contains(inputDir.inputAbstractFile))
            return;

        // Find first parent with settings, or use defaults.
        Settings settings = null;
        AbstractFile parent = inputDir.inputAbstractFile;
        while (true) {
            settings = dirToSettings.get(parent);
            if (settings != null)
                break;
            if (parent.equals(root))
                break;
            parent = parent.getParentFile();
        }
        if (settings == null)
            settings = defaultSettings;

        if (settings.combineSubdirectories) {
            // Collect all files under subdirectories and ignore subdirectories so they won't be packed twice.
            files = new FileProcessor(this) {
                protected void processDir(Entry entryDir, ArrayList<Entry> files) {
                    ignoreDirs.add(entryDir.inputAbstractFile);
                }

                protected void processFile(Entry entry) {
                    addProcessedFile(entry);
                }
            }.process(inputDir.inputAbstractFile, null);
        }

        if (files.isEmpty())
            return;

        // Pack.
        System.out.println(inputDir.inputAbstractFile.getName());
        TexturePacker_Base packer = TexturePacker_Base.that.getInstanz(root, settings);
        for (Entry file : files)
            packer.addImage(file.inputAbstractFile);
        packer.pack(inputDir.outputDir, packFileName);
    }
}
