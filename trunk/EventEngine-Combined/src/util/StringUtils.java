package util;

/**
 *
 * @author Stijn
 */
public class StringUtils {
    private StringUtils() {}

    public static String newline = System.getProperty("line.separator");

    public static String join(String glue, String... parts) {
        if (parts == null || parts.length == 0)
            return "";
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (glue != null)
                builder.append(glue);
            if (parts[i] != null)
                builder.append(parts[i]);
        }
        return builder.toString();
    }

    public static String times(String s, int times) {
        String answer = "";
        for (int i = 0; i < times; i++)
            answer += s;
        return answer;
    }

    public static String indent(String text, int amount) {
        if (text.isEmpty())
            return text;
        if (!text.isEmpty())
           text = StringUtils.times("   ", amount) + text;
        return StringUtils.join(newline + StringUtils.times("   ", amount), text.split("\r?\n|\r"));
    }
}
