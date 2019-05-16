package tk.bolovsrol.db.orm.refgen;

/**
 * Генерирует контейнер из мета-интерфейса.
 */
class ContainerGenerator implements ObjectGenerator {

    public static final String CONTAINER = "Container";

    @Override public String getSuffix() {
        return CONTAINER;
    }

    @Override public boolean isOverrideExisting() {
        return true;
    }

    @Override public String generate(MetaInterface mi) {
        StringBuilder sb = new StringBuilder(4096);

        // headed, name and fields
        sb.append(mi.getHeader());
        sb.append("\nimport tk.bolovsrol.utils.StringDumpBuilder;\n\nclass ")
              .append(mi.getName()).append(CONTAINER).append(" implements ").append(mi.getName()).append(" {\n\n");
        for (FieldContainer fc : mi.getFields()) {
            sb.append("    private final ").append(fc.getType()).append(' ').append(fc.getName()).append(";\n");
        }

        // constructor
        sb.append("\n    public ").append(mi.getName()).append(CONTAINER).append('(');
        for (FieldContainer fc : mi.getFields()) {
            sb.append(fc.getType()).append(' ').append(fc.getName()).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(") {\n");
        for (FieldContainer fc : mi.getFields()) {
            sb.append("        this.").append(fc.getName()).append(" = ").append(fc.getName()).append(";\n");
        }
        sb.append("    }\n\n");

        // getters
        for (FieldContainer fc : mi.getFields()) {
            sb.append("    @Override public ").append(fc.getType()).append(' ').append(fc.getGetterName()).append("() {\n        return ").append(fc.getName()).append(";\n    }\n\n");
        }

        // toString and finale
        sb.append("\n    @Override public String toString() {\n        StringDumpBuilder sdb = new StringDumpBuilder();\n");
        for (FieldContainer fc : mi.getFields()) {
            sb.append("        sdb.append(\"").append(fc.getName()).append("\", ").append(fc.getName()).append(");\n");
        }
        sb.append("        return sdb.toString();\n    }\n\n}\n");

        return sb.toString();
    }

}
