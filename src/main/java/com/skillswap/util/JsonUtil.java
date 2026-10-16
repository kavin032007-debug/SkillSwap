package com.skillswap.util;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON serializer and parser for persistence.
 * Parses JSON into standard Java maps, lists, strings, booleans, and numbers.
 */
public final class JsonUtil {

    private JsonUtil() {}

    // ==========================================
    // Parser
    // ==========================================

    public static Object parse(String json) {
        if (json == null) return null;
        String clean = json.trim();
        if (clean.isEmpty()) return null;
        return new Parser(clean).parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object val = parse(json);
        if (val instanceof Map) {
            return (Map<String, Object>) val;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseList(String json) {
        Object val = parse(json);
        if (val instanceof List) {
            return (List<Object>) val;
        }
        return new ArrayList<>();
    }

    private static class Parser {
        private final String src;
        private int pos = 0;

        Parser(String src) {
            this.src = src;
        }

        Object parseValue() {
            skipWhitespace();
            if (pos >= src.length()) return null;
            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            return parseNumber();
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            consume('{');
            skipWhitespace();
            if (peek('}')) {
                consume('}');
                return map;
            }
            while (pos < src.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                consume(':');
                skipWhitespace();
                Object val = parseValue();
                map.put(key, val);
                skipWhitespace();
                if (peek(',')) {
                    consume(',');
                } else if (peek('}')) {
                    consume('}');
                    break;
                } else {
                    break;
                }
            }
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            consume('[');
            skipWhitespace();
            if (peek(']')) {
                consume(']');
                return list;
            }
            while (pos < src.length()) {
                skipWhitespace();
                Object val = parseValue();
                list.add(val);
                skipWhitespace();
                if (peek(',')) {
                    consume(',');
                } else if (peek(']')) {
                    consume(']');
                    break;
                } else {
                    break;
                }
            }
            return list;
        }

        String parseString() {
            consume('"');
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (pos >= src.length()) break;
                    char escape = src.charAt(pos++);
                    switch (escape) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (pos + 4 <= src.length()) {
                                String hex = src.substring(pos, pos + 4);
                                pos += 4;
                                sb.append((char) Integer.parseInt(hex, 16));
                            }
                        }
                        default -> sb.append(escape);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid boolean at position " + pos);
        }

        Object parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null at position " + pos);
        }

        Number parseNumber() {
            int start = pos;
            if (peek('-')) pos++;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                pos++;
            }
            String numStr = src.substring(start, pos);
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.parseDouble(numStr);
            }
            try {
                return Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                return Double.parseDouble(numStr);
            }
        }

        void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        boolean peek(char c) {
            skipWhitespace();
            return pos < src.length() && src.charAt(pos) == c;
        }

        void consume(char c) {
            skipWhitespace();
            if (pos >= src.length() || src.charAt(pos) != c) {
                throw new IllegalArgumentException("Expected '" + c + "' at position " + pos + " but found '" + (pos < src.length() ? src.charAt(pos) : "EOF") + "'");
            }
            pos++;
        }
    }

    // ==========================================
    // Serializer
    // ==========================================

    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        serialize(obj, sb, 0);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void serialize(Object obj, StringBuilder sb, int indent) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String s) {
            sb.append('"').append(escapeJson(s)).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj);
        } else if (obj instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                sb.append("{}");
                return;
            }
            sb.append("{\n");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (i++ > 0) sb.append(",\n");
                indent(sb, indent + 2);
                sb.append('"').append(escapeJson(String.valueOf(entry.getKey()))).append("\": ");
                serialize(entry.getValue(), sb, indent + 2);
            }
            sb.append("\n");
            indent(sb, indent);
            sb.append("}");
        } else if (obj instanceof Iterable<?> iter) {
            Iterator<?> it = iter.iterator();
            if (!it.hasNext()) {
                sb.append("[]");
                return;
            }
            sb.append("[\n");
            boolean first = true;
            while (it.hasNext()) {
                if (!first) sb.append(",\n");
                first = false;
                indent(sb, indent + 2);
                serialize(it.next(), sb, indent + 2);
            }
            sb.append("\n");
            indent(sb, indent);
            sb.append("]");
        } else {
            sb.append('"').append(escapeJson(obj.toString())).append('"');
        }
    }

    private static void indent(StringBuilder sb, int spaces) {
        sb.append(" ".repeat(Math.max(0, spaces)));
    }

    public static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        String hex = String.format("%04x", (int) c);
                        sb.append("\\u").append(hex);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
