package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.utils.containers.InstantContainer;

import java.time.Instant;

public interface DbInstantContainer extends InstantContainer, DbValueContainer<Instant> {
}
