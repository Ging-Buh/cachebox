package CB_Utils.Settings;

/**
 * Global = config.db3<br>
 * Local = aktuelle DB<br>
 * Platform = über den PlatformConnector bzw Java Settings (= Windows -> registry,...). Wenn die DB noch nicht zur Verfügung steht
 *
 * @author Longri
 */
public enum SettingStoreType {
    Global, Local, Platform
}
