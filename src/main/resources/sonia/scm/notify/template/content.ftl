<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional //EN">
<html>
  <head>
    <title>${title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <style type="text/css">
      body {
        background-color: #ffffff;
        margin: 10px;
        color: #202020;
        font-family: Verdana,Helvetica,Arial,sans-serif;
        font-size: 75%; 
      }
      h1 {
       font-family: Arial,'Arial CE','Lucida Grande CE',lucida,'Helvetica CE',sans-serif;
       font-weight: bold;
       margin: 0px;
       padding: 0px;
       color: #D20005;
       font-size: 18px;
       border-bottom: 1px solid #AFAFAF; 
      }
      table {
        border: 0 none;
        border-collapse: collapse;
        font-size: 100%;
        margin: 20px 0;
        padding: 20px;
        width: 100%;
      }
      td {
        padding: 3px;
        vertical-align: top;
        border: 1px solid #CCCCCC;
        text-align: left;
      }
      a {
        color: #045491;
        font-weight: bold;
        text-decoration: none;
      }
    </style>
  </head>
  <body>
    <h1>Changesets:</h1>
    <table>
      <tr style="text-align: center; font: bold">
          <th></th>
          <th>Branch</th>
          <th>Author</th>
          <th style="text-align: left;">Description</th>
      </tr>
      <#list changesets as changeset>
        <tr>
          <td style="width: 10%"><a href="${changeset.link}" target="_blank">${changeset.shortId}</a></td>
          <td style="width: 10%">${changeset.branchesAsString}</td>
          <td style="min-width: 100px; width: 10%">${changeset.author.name}</td>
          <td style="width: 70%"><pre>${changeset.description}</pre></td>
        </tr>
        <#list changeset.modifications.added as added>
          <tr>
            <td colspan="4">+ ${added}</td>
          </tr>
        </#list>
        <#list changeset.modifications.removed as removed>
          <tr>
            <td colspan="4">- ${removed}</td>
          </tr>
        </#list>
        <#list changeset.modifications.modified as modified>
          <tr>
            <td colspan="4">M ${modified}</td>
          </tr>
        </#list>
      </#list>
    </table>

    <#if diff?has_content>
      <h1>Diffs:</h1>
      <pre>
${diff}
      </pre>
    </#if>
  </body>
</html>

