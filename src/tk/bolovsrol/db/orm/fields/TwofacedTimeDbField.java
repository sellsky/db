package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbTwofacedTime;
import tk.bolovsrol.db.orm.containers.DbTwofacedTimeContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.utils.time.TwofacedTime;

/** Текстовая строка, описывающая двуликое время в человекочитаемом виде. */
public class TwofacedTimeDbField extends AbstractDbDataField<TwofacedTime, DbTwofacedTimeContainer> implements DbTwofacedTimeContainer {

    public TwofacedTimeDbField(DbDataObject owner, String name) { super(owner, name, new DbTwofacedTime());}

    public TwofacedTimeDbField(DbDataObject owner, String name, boolean register) { super(owner, name, register, new DbTwofacedTime()); }

    @Override public DbTwofacedTimeContainer wrap(TwofacedTime value) { return new DbTwofacedTime(value); }

}