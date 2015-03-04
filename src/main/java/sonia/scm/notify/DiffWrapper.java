package sonia.scm.notify;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;

public class DiffWrapper
{
  private final static ImmutableMap<String, String> diffClasses = new ImmutableMap.Builder<String, String>()
      .put("d", "file")
      .put("i", "file")
      .put("/", "file")
      .put("@", "info")
      .put("-", "delete")
      .put("+", "insert")
      .put(" ", "context").build();

  private final static ImmutableMap<String, String> diffStyleAttributes = new ImmutableMap.Builder<String, String>()
      .put("h2", "font-family: Arial,'Arial CE','Lucida Grande CE',lucida,'Helvetica CE',sans-serif;font-weight: bold;color: #555;padding: 10px 6px;font-size: 14px;margin: 0;background: -webkit-linear-gradient(#fafafa, #eaeaea);-ms-filter: &quot;progid:DXImageTransform.Microsoft.gradient(startColorstr='#fafafa',endColorstr='#eaeaea')&quot;;border: 1px solid #d8d8d8;border-bottom: 0;font: 14px sans-serif;overflow: hidden;text-shadow: 0 1px 0 white;text-align: center;")
      .put("file-diff", "border: 1px solid #d8d8d8;margin-bottom: 1em;overflow: auto;padding: 0.5em 0;")
      .put("file", "color: #aaa;")
      .put("info", "color: #a0b;")
      .put("delete", "background-color: #fdd;")
      .put("insert", "background-color: #dfd;")
      .put("pre", "margin: 0;font-family: &quot;Bitstream Vera Sans Mono&quot;, Courier, monospace;font-size: 12px;line-height: 1.4em;text-indent: 0.5em;")
      .build();

  public static String[] wrap(String[] lines)
  {
    lines = Arrays.copyOf(lines, lines.length);

    boolean firstFileFound = false;

    for (int i = 0; i < lines.length; i++)
    {
      String line = lines[i];

      StringBuilder builder = new StringBuilder();

      if (line.startsWith("diff "))
      {
        if (!firstFileFound)
        {
          builder.append("</div>");
          firstFileFound = true;
        }
        String filename = line.substring(
            line.indexOf("a/") + 2,
            line.indexOf("b/") - 1
        ).trim();
        builder.append("<div class=\"file-diff\" ")
            .append("style=\"")
            .append(diffStyleAttributes.get("file-diff"))
            .append("\">")
            .append("<h2 style=\"")
            .append(diffStyleAttributes.get("h2"))
            .append("\">")
            .append(filename)
            .append("</h2>");
      }

      String diffClass = diffClasses.get(String.valueOf(line.charAt(0)));

      if (diffClass == null)
      {
        diffClass = "context";
      }

      builder.append("<pre class = \"")
          .append(diffClass)
          .append("\" style=\"")
          .append(diffStyleAttributes.get("pre"))
          .append(diffStyleAttributes.get(diffClass))
          .append("\">")
          .append(line)
          .append("</pre>");
      lines[i] = builder.toString();
    }
    lines[lines.length - 1] += "</div>";

    return lines;
  }
}
