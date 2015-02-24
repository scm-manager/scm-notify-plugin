package sonia.scm.notify;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class DiffWrapper {
    private static final Logger logger = LoggerFactory.getLogger(DiffWrapper.class);

    private final static ImmutableMap<String, String> diffClasses = new ImmutableMap.Builder<String, String>()
            .put("d", "file")
            .put("i", "file")
            .put("/", "file")
            .put("@", "info")
            .put("-", "delete")
            .put("+", "insert")
            .put(" ", "context").build();

    public static String[] wrap(String[] lines) {
        lines = Arrays.copyOf(lines, lines.length);
        for (String line : lines) {
            logger.info("line:{}", line);
        }

        boolean firstFileFound = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            StringBuilder builder = new StringBuilder();

            if (line.startsWith("diff ")) {
                if (!firstFileFound) {
                    builder.append("</div>");
                    firstFileFound = true;
                }
                String filename = line.substring(
                        line.indexOf("a/") + 2,
                        line.indexOf("b/") - 1
                ).trim();
                builder.append("<div class=\"file-diff\">")
                        .append("<h2>")
                        .append(filename)
                        .append("</h2>");
            }

            builder.append("<pre class = \"")
                    .append(diffClasses.get(String.valueOf(line.charAt(0))))
                    .append("\">")
                    .append(line)
                    .append("</pre>");
            lines[i] = builder.toString();
        }
        lines[lines.length - 1] += "</div>";

        return lines;
    }
}
