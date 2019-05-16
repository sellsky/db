package tk.bolovsrol.db;

import tk.bolovsrol.utils.properties.ReadOnlyProperties;
import tk.bolovsrol.utils.properties.SystemResourceProperties;

public final class DbProperties {

    public static final String RESOURCE_NAME = "ru/plasticmedia/db/db.properties";

    private DbProperties() {
    }

    public static ReadOnlyProperties properties() {
        try {
            return SystemResourceProperties.get(RESOURCE_NAME);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}