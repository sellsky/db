package tk.bolovsrol.db.orm.refgen;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

class MetaInterface {

    private static final String _INTERFACE_ = "interface ";
    private static final String DB_META = "@DbMeta(";
    private static final String IMPORT_RU_PLASTICMEDIA_DB_ORM_JDBC_REFGEN_DB_META = "import tk.bolovsrol.db.orm.refgen.DbMeta;";
    private static final String DEPRECATED = "@Deprecated";

    private static final Set<String> NON_ENUM_FIELDS = new TreeSet<>();

    static {
        NON_ENUM_FIELDS.add("boolean");
        NON_ENUM_FIELDS.add("int");
        NON_ENUM_FIELDS.add("long");
        NON_ENUM_FIELDS.add("BigDecimal");
        NON_ENUM_FIELDS.add("Boolean");
        NON_ENUM_FIELDS.add("Date");
		NON_ENUM_FIELDS.add("Duration");
		NON_ENUM_FIELDS.add("Flag");
		NON_ENUM_FIELDS.add("Integer");
		NON_ENUM_FIELDS.add("Long");
		NON_ENUM_FIELDS.add("Pattern");
        NON_ENUM_FIELDS.add("String");
        NON_ENUM_FIELDS.add("Uri");
    }

    /**
     * пекедж и импорты.
     */
    private String header;

    private String name;

    private String sqlCatalogName;
    private String sqlTableName;
    private String idFieldName;
    private String bigDecimalScale;

    private final List<FieldContainer> fields = new ArrayList<>();

    public MetaInterface() {
    }

    public MetaInterface(final String src) throws UnexpectedBehaviourException {
        parse(src);
    }

    public final MetaInterface parse(final String src) throws UnexpectedBehaviourException {
        String s = removeRemarks(src);

        int interfacePos = s.indexOf(_INTERFACE_);
        if (interfacePos < 0) {
            throw new UnexpectedBehaviourException("No interface is found!");
        }

        // header
        {
            int prevLf = s.lastIndexOf('\n', interfacePos);
            StringBuilder sb = new StringBuilder(prevLf);
            sb.append(s.substring(0, prevLf));

            int metadbPos = sb.indexOf(DB_META);
            if (metadbPos >= 0) {
                int metaDbEnd = sb.indexOf(")", metadbPos);
                String metaDbLine = sb.substring(metadbPos + DB_META.length(), metaDbEnd);
                sb.delete(metadbPos, metaDbEnd + 1);
                try {
                    if (sb.charAt(metadbPos) == '\n') {
                        sb.deleteCharAt(metadbPos);
                    }
                } catch (StringIndexOutOfBoundsException ignored) {
                }

                String[] tokens = StringUtils.parseDelimited(metaDbLine, ",");
                for (String token : tokens) {
                    String[] keyval = StringUtils.parseDelimited(token, '=');
                    switch (keyval[0]) {
                    case "sqlCatalog":
                        sqlCatalogName = keyval[1];
                        break;
                    case "sqlTable":
                        sqlTableName = keyval[1];
                        break;
                    case "keyField":
                        idFieldName = StringUtils.underscoreToCamel(StringUtils.trim(keyval[1], StringUtils.QUOTE_FILTER, StringUtils.TrimMode.BOTH), false);
                        break;
                    case "bigDecimalScale":
                        bigDecimalScale = keyval[1];
                        break;
                    default:
                        throw new UnexpectedBehaviourException("Unexpected keyword in @DbMeta annotation " + Spell.get(token));
                    }
                }
                {
                    int importPos = sb.indexOf(IMPORT_RU_PLASTICMEDIA_DB_ORM_JDBC_REFGEN_DB_META);
                    if (importPos >= 0) {
                        sb.delete(importPos, importPos + IMPORT_RU_PLASTICMEDIA_DB_ORM_JDBC_REFGEN_DB_META.length() + 1);
                    }
                }
            }
            int deprecatedPos = sb.indexOf(DEPRECATED);
            if (deprecatedPos >= 0) {
                sb.delete(deprecatedPos, deprecatedPos + DEPRECATED.length());
            }

            while (sb.charAt(sb.length() - 1) == '\n') {
                sb.deleteCharAt(sb.length() - 1);
            }
            header = sb.toString();
        }

        // name
        {
            int nameStart = interfacePos + _INTERFACE_.length();
            int nameEnd = s.indexOf(' ', nameStart);
            name = s.substring(nameStart, nameEnd);
        }

        fields.add(new FieldContainer("id", "getId", "Long", false));

        // body
        int bodyStart = s.indexOf('\n', interfacePos);
        StringTokenizer st = new StringTokenizer(s.substring(bodyStart), "\n", false);
        while (st.hasMoreTokens()) {
            String line = st.nextToken().trim();

            int remarkPos = line.indexOf("//");
            if (remarkPos == 0) {
                continue;
            } else if (remarkPos > 0) {
                line = line.substring(0, remarkPos).trim();
            }

            if (!line.endsWith("();")) {
                continue;
            }

            while (line.startsWith("@")) {
                line = StringUtils.subWords(line, 1);
            }

            String[] typeName = StringUtils.parseDelimited(line, ' ');
            if (typeName.length != 2) {
                continue;
            }

			@SuppressWarnings("NonConstantStringShouldBeStringBuffer")
			String type = typeName[0];
			String getterName = typeName[1];
			getterName = getterName.substring(0, getterName.length() - 3);

            int i;
            if (getterName.startsWith("get")) {
                i = 3;
            } else if (getterName.startsWith("is")) {
                i = 2;
            } else {
                continue;
            }
            String name = Character.toLowerCase(getterName.charAt(i)) + getterName.substring(i + 1);

            boolean isEnumField = !NON_ENUM_FIELDS.contains(type);

            if (isEnumField && src.contains("enum " + type + " {")) { //  если енум определён в интерфейсе, нужно добавить имя интерфейса к типу енума
                type = this.name + '.' + type;
            }
            fields.add(new FieldContainer(name, getterName, type, isEnumField));
        }
        return this;
    }


    private static String removeRemarks(String src) {
        int start = src.indexOf("/*");
        if (start < 0) {
            return src;
        }
        StringBuilder sb = new StringBuilder(src);
        do {
            int end = sb.indexOf("*/");
            sb.delete(start, end + 2);
            if (sb.charAt(start) == '\n') {
                sb.deleteCharAt(start);
            }
            start = sb.indexOf("/*", start);
        } while (start > 0);
        return sb.toString();
    }

    public String getHeader() {
        return header;
    }

    public String getName() {
        return name;
    }

    public String getSqlCatalogName() {
        return sqlCatalogName;
    }

	public void setSqlCatalogName(String sqlCatalogName) {
		this.sqlCatalogName = sqlCatalogName;
	}

	public String getSqlTableName() {
		return sqlTableName;
	}

    public String getIdFieldName() {
        return idFieldName;
    }

    public List<FieldContainer> getFields() {
        return fields;
    }

    public String getBigDecimalScale() {
        return bigDecimalScale;
    }
}
