package tk.bolovsrol.db.orm.refgen;

import tk.bolovsrol.utils.reflectiondump.ReflectionDump;

class FieldContainer {
    private final String name;
    private final String getterName;
    private final String type;
    private final boolean enumField;

    FieldContainer(String name, String getterName, String type, boolean enumField) {
        this.name = name;
        this.getterName = getterName;
        this.type = type;
        this.enumField = enumField;
    }

    public String getName() {
        return name;
    }

    public String getGetterName() {
        return getterName;
    }

    public String getType() {
        return type;
    }

    public boolean isEnumField() {
        return enumField;
    }

    @Override public String toString() {
        return ReflectionDump.getFor(this);
    }

}
