package tk.bolovsrol.db.orm.refgen;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;

import java.util.Set;
import java.util.TreeSet;

/**
 * Генерирует кешируемый бд-объект из мета-интерфейса.
 * <p>
 * Ключевое поле должно называться id, ну или можно в параметрах задавать, но это пфф.
 */
class DbdoGenerator implements ObjectGenerator {

    public static final String DBDO = "Dbdo";

    @Override public String getSuffix() {
        return DBDO;
    }

    @Override public boolean isOverrideExisting() {
        return true;
    }

    @Override public String generate(MetaInterface mi) throws UnexpectedBehaviourException {
        StringBuilder sb = new StringBuilder(8192);


        // headed, name and fields
        sb.append(mi.getHeader());
        // стандартные импорты
        sb.append("\nimport tk.bolovsrol.db.orm.object.AbstractRefDbDataObject;\nimport tk.bolovsrol.db.orm.versioncache.CacheableDbDataObject;");
        // мы должны пройтись по всем типам и добавить им импорты
        Set<String> exceptions = new TreeSet<>();
        Set<String> imports = new TreeSet<>();
        for (FieldContainer fc : mi.getFields()) {
            if (fc.isEnumField()) {
                imports.add("tk.bolovsrol.db.orm.fields.EnumDbField");
            } else {

                String type = fc.getType();
                switch (type) {
                case "Integer":
                case "int":
                    imports.add("tk.bolovsrol.db.orm.fields.IntegerDbField");
                    break;
                case "Long":
                case "long":
					imports.add("tk.bolovsrol.db.orm.fields.LongDbField");
					break;
				case "BigDecimal":
                    imports.add("tk.bolovsrol.db.orm.fields.BigDecimalDbField");
                    break;
                case "Boolean":
                case "boolean":
                    imports.add("tk.bolovsrol.db.orm.fields.FlagDbField");
                    imports.add("tk.bolovsrol.utils.Flag");
                    break;
                case "Pattern":
                    imports.add("tk.bolovsrol.db.orm.fields.StringDbField");
                    imports.add("tk.bolovsrol.utils.PatternCompileException");
                    imports.add("tk.bolovsrol.utils.RegexUtils");
                    exceptions.add("PatternCompileException");
                    break;
                case "String":
                case "Date":
                case "Flag":
                    imports.add("tk.bolovsrol.db.orm.fields." + type + "DbField");
                    break;
                case "Uri":
                    imports.add("tk.bolovsrol.db.orm.fields.StringDbField");
                    imports.add("tk.bolovsrol.utils.Uri");
                    imports.add("tk.bolovsrol.utils.UriParsingException");
                    exceptions.add("UriParsingException");
                    break;
				case "Duration":
					imports.add("tk.bolovsrol.db.orm.fields.DurationDbField");
					break;
				default:
					throw new UnexpectedBehaviourException("Unexpected field type " + Spell.get(type) + " in " + Spell.get(fc));
				}
			}
		}
        for (String im : imports) {
            sb.append("\nimport ").append(im).append(';');
        }


        sb.append("\n\nclass ")
              .append(mi.getName()).append(DBDO)
              .append(" extends  AbstractRefDbDataObject implements  CacheableDbDataObject<").append(mi.getName()).append("> {\n\n");

        // constructor
        sb.append("    public ").append(mi.getName()).append(DBDO).append("() {\n        super(");
		if (mi.getSqlCatalogName().isEmpty()) {
            mi.setSqlCatalogName("Const.DB_CATALOG"); // placeholder, should be imported by hand
        }
		sb.append(mi.getSqlCatalogName()).append(", ");
		if (mi.getSqlTableName() == null) {
			sb.append('"');
            String sqlTableName = StringUtils.camelToUnderscore(mi.getName());
            sb.append(sqlTableName);
            if (sqlTableName.endsWith("y")) {
                sb.deleteCharAt(sb.length() - 1);
                sb.append("ies");
            } else if (!sqlTableName.endsWith("s")) {
                sb.append('s');
            }
            sb.append('"');
        } else {
            sb.append(mi.getSqlCatalogName());
        }
        sb.append(");\n    }\n\n");

        // fields
        for (FieldContainer fc : mi.getFields()) {
			if (fc.getName().equals("id")) { continue; }
			String type = fc.getType();
			if (fc.isEnumField()) {
				sb.append("    public final EnumDbField<").append(type).append("> ").append(fc.getName()).append(" = new EnumDbField<>(this, \"")
                      .append(StringUtils.camelToUnderscore(fc.getName())).append("\", ").append(type).append(".class);\n");
            } else {
				@SuppressWarnings("NonConstantStringShouldBeStringBuffer")
				String name = fc.getName();
				String suffix = "";
				switch (type) {
                case "Integer":
                case "int":
					type = "Integer";
					break;
				case "BigDecimal":
					suffix = ", " + mi.getBigDecimalScale();
					break;
                case "Boolean":
                case "boolean":
                    type = "Flag";
					name = name + "Fl";
					break;
				case "Pattern":
				case "Uri":
					type = "String";
					break;
                case "long":
                    type = "Long";
                    break;
                }
                sb.append("    public final ").append(type).append("DbField ").append(name).append(" = new ").append(type).append("DbField(this, \"")
                      .append(StringUtils.camelToUnderscore(name)).append('"').append(suffix).append(");\n");
            }
        }

        // simple cache gen -- may need some tune-up with hand
        sb.append("\n@Override public ").append(mi.getName()).append(" getCacheItem()");
        if (!exceptions.isEmpty()) {
            sb.append(" throws ");
            boolean needComma = false;
            for (String exception : exceptions) {
                if (needComma) {
                    sb.append(", ");
                } else {
                    needComma = true;
                }
                sb.append(exception);
            }
        }
        sb.append(" {\n        return new ").append(mi.getName()).append(ContainerGenerator.CONTAINER).append("(\n");
        for (FieldContainer fc : mi.getFields()) {
            String type = fc.getType();
            switch (type) {
            case "Boolean":
				sb.append("              Flag.toBoolean(this.").append(fc.getName()).append("Fl.getValue()),\n");
				break;
			case "boolean":
				sb.append("              this.").append(fc.getName()).append("Fl.getValue() == Flag.YES,\n");
				break;
			case "Pattern":
				sb.append("              RegexUtils.compilePattern(this.").append(fc.getName()).append(".getValue()),\n");
				break;
            case "Uri":
                sb.append("               Uri.parseUri(this.").append(fc.getName()).append(".getValue()),\n");
                break;
            default:
                sb.append("              this.");
                if (fc.getName().equals("id") && mi.getIdFieldName() != null) {
                    sb.append(mi.getIdFieldName());
                } else {
                    sb.append(fc.getName());
                }
                sb.append(".getValue(),\n");
                break;
            }
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("\n        );\n    }\n\n}\n");

        return sb.toString();
    }

}
