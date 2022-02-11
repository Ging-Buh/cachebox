package de.droidcachebox.settings;

import static de.droidcachebox.settings.SettingStoreType.Global;
import static de.droidcachebox.settings.SettingStoreType.Local;
import static de.droidcachebox.settings.SettingStoreType.Platform;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import de.droidcachebox.database.Database_Core;
import de.droidcachebox.utils.log.Log;

public abstract class SettingsList extends ArrayList<SettingBase<?>> {
    private static final String sClass = "SettingsList";
    private static final long serialVersionUID = -969846843815877942L;
    private static SettingsList that;
    private boolean isLoaded = false;

    public SettingsList() {
        that = this;
        Member[] mbrs = this.getClass().getFields();
        for (Member mbr : mbrs) {
            if (mbr instanceof Field) {
                try {
                    Object obj = ((Field) mbr).get(this);
                    if (obj instanceof SettingBase<?>) {
                        add((SettingBase<?>) obj);
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public boolean add(SettingBase<?> setting) {
        if (!that.contains(setting)) {
            return super.add(setting);
        }
        return false;
    }

    protected abstract Database_Core getSettingsDB();

    protected abstract Database_Core getDataDB();

    protected abstract SettingsDAO createSettingsDAO();

    protected abstract boolean canNotUsePlatformSettings();

    /**
     * Return true, if setting changes need restart
     *
     * @return ?
     */
    public boolean writeToDatabases() {
        getSettingsDB().beginTransaction();
        SettingsDAO dao = createSettingsDAO();
        // if used from Splash, DataDB is not possible = Data == null
        Database_Core dataDB = getDataDB();

        boolean needRestart = false;
        String settingName = "";
        try {
            for (SettingBase<?> setting : this) {
                if (setting.isDirty()) {
                    settingName = setting.name;

                    if (Local == setting.getStoreType()) {
                        if (dataDB != null) {
                            if (dataDB.isOpen()) {
                                // there are only a few settings that go into CBDB
                                try {
                                    dataDB.beginTransaction();
                                    dao.writeSetting(dataDB, setting);
                                    dataDB.setTransactionSuccessful();
                                    dataDB.endTransaction();
                                } catch (Exception ex) {
                                    dataDB.endTransaction();
                                }
                            }
                        }
                    } else if (Global == setting.getStoreType() || (canNotUsePlatformSettings() && Platform == setting.getStoreType())) {
                        dao.writeSetting(getSettingsDB(), setting);
                    } else if (Platform == setting.getStoreType()) {
                        dao.writePlatformSetting(setting);
                        dao.writeSetting(getSettingsDB(), setting);
                    }

                    if (setting.needRestart) {
                        needRestart = true;
                    }
                    setting.clearDirty();
                }
            }
            getSettingsDB().setTransactionSuccessful();
        } catch (Exception ex) {
            Log.err(sClass, settingName, ex);
        } finally {
            getSettingsDB().endTransaction();
        }
        return needRestart;
    }

    public void readFromDB() {
        AtomicInteger tryCount = new AtomicInteger(0);
        SettingsDAO dao = createSettingsDAO();
        Database_Core dataDB = getDataDB();
        boolean dataDBIsOpen = dataDB != null && dataDB.isOpen();
        while (tryCount.incrementAndGet() < 10) {
            try {
                for (SettingBase<?> setting : this) {
                    if (Local == setting.getStoreType()) {
                        if (dataDBIsOpen) {
                            dao.readSettingOrDefault(dataDB, setting);
                        } else {
                            setting.loadDefault();
                        }
                    } else if (Global == setting.getStoreType() || (canNotUsePlatformSettings() && Platform == setting.getStoreType())) {
                        dao.readSettingOrDefault(getSettingsDB(), setting);
                    } else if (Platform == setting.getStoreType()) {
                        SettingBase<?> platformSetting = setting.copy(); // cause we do not know how to new
                        dao.readSettingOrDefault(getSettingsDB(), setting);
                        dao.readPlatformSetting(platformSetting);
                        if (!platformSetting.value.equals(setting.value)) {
                            if (platformSetting.value.equals(setting.defaultValue)) {
                                // override Platformsettings with UserDBSettings
                                platformSetting.setValueFrom(setting);
                                dao.writePlatformSetting(platformSetting);
                                setting.clearDirty();
                                // isPlattformoverride = true;
                            } else {
                                // override UserDBSettings with Platformsettings
                                setting.setValueFrom(platformSetting);
                                dao.writeSetting(getSettingsDB(), platformSetting);
                            }
                        }
                    }
                }
                tryCount.set(100);
            } catch (Exception e) {
                Log.err(sClass, "Error read settings, try again");
            }

        }
        Log.debug(sClass, "Settings are loaded");
        isLoaded = true;
    }

    public void loadFromLastValues() {
        for (SettingBase<?> setting : this) {
            setting.loadFromLastValue();
        }
    }

    public void saveToLastValues() {
        for (SettingBase<?> setting : this) {
            setting.saveToLastValue();
        }
    }

    public void loadAllDefaultValues() {
        for (SettingBase<?> setting : this) {
            setting.loadDefault();
        }
    }
}
