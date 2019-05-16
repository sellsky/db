package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.utils.containers.LongContainer;

public interface DbLongContainer extends LongContainer, DbNumberContainer<Long> {
    @Override default int signum() {
        return Long.signum(getValue());
    }

}
