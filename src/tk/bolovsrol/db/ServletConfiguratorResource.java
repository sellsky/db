package tk.bolovsrol.db;

import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.Cfg;

/** Фальшивый ресурс для инициализации базы данных в резине. */
public class ServletConfiguratorResource {
    public void setValue(String configName) {
        try {
            Cfg.init(configName);
        } catch (Exception e) {
            Log.exception(e);
            throw new RuntimeException(e);
        }
    }
}
