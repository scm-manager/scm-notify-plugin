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
      tr {
        border: 0;
      }
      td {
        padding: 3px;
        vertical-align: top;
        border: 1px solid #CCCCCC;
        text-align: left;
        border: 0;
      }
      a {
        color: #045491;
        font-weight: bold;
        text-decoration: none;
      }
    </style>
  </head>
  <body>
    <h1>Repository ${repository.name} has changed</h1>
    
    <table>
      <#list changesets as changeset>
        <tr>
          <td style="width: 60px;">
            <a href="${changeset.link}" target="_blank">
              ${changeset.shortId}
            </a>
          </td>
          <td style="width: 150px;">
            ${changeset.date?string("yyyy-MM-dd HH:mm:ss")}
          </td>
          <td>
            ${changeset.author.name}
          </td>
        </tr>
        <tr>
          <td colspan="3">
            ${changeset.description}
          </td>
        </tr>
        <tr>
          <td colspan="3">
            <#list changeset.modifications.added as added>
            <span>A ${added}</span><br />
            </#list>
            <#list changeset.modifications.modified as modified>
            <span>M ${modified}</span><br />
            </#list>
            <#list changeset.modifications.removed as removed>
            <span>R ${removed}</span><br />
            </#list>
          </td>
        </tr>
        <#if changeset.diff?has_content>
        <tr>
          <td colspan="3">
            <pre>${changeset.diff}</pre>
          </td>
        </tr>
        </#if>
      </#list>
    </table>

  </body>
</html>

