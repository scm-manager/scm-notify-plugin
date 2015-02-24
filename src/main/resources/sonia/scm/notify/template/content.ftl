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
      h1, h2 {
       font-family: Arial,'Arial CE','Lucida Grande CE',lucida,'Helvetica CE',sans-serif;
       font-weight: bold;
       color: #D20005;
       padding: 0;
      }      
      h1 {
       font-size: 18px;
       border-bottom: 1px solid #AFAFAF;
       margin: 0 0 5px 0;
      }
      h2 {
       font-size: 14px;
       margin: 0 0 2px 0;
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
      body {
          text-align: center;
      }
      #wrapper {
          display: inline-block;
          margin-top: 1em;
          min-width: 800px;
          text-align: left;
      }
      h2 {
          background: #fafafa;
          background: -moz-linear-gradient(#fafafa, #eaeaea);
          background: -webkit-linear-gradient(#fafafa, #eaeaea);
          -ms-filter: "progid:DXImageTransform.Microsoft.gradient(startColorstr='#fafafa',endColorstr='#eaeaea')";
          border: 1px solid #d8d8d8;
          border-bottom: 0;
          color: #555;
          font: 14px sans-serif;
          overflow: hidden;
          padding: 10px 6px;
          text-shadow: 0 1px 0 white;
          margin: 0;
      }
      .file-diff {
          border: 1px solid #d8d8d8;
          margin-bottom: 1em;
          overflow: auto;
          padding: 0.5em 0;
      }
      .file-diff > div {
          width: 100%:
      }
      pre {
          margin: 0;
          font-family: "Bitstream Vera Sans Mono", Courier, monospace;
          font-size: 12px;
          line-height: 1.4em;
          text-indent: 0.5em;
      }
      .file {
          color: #aaa;
      }
      .delete {
          background-color: #fdd;
      }
      .insert {
          background-color: #dfd;
      }
      .info {
          color: #a0b;
      }
    </style>
    </style>
  </head>
  <body>
    <#list branches as branch>
      <#if supportNamedBranches>
      <h2>Branch: ${branch.name}</h2>
      </#if>
    
      <table>
        <#list branch.changesets as changeset>
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
              <pre>${changeset.description}</pre>
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
              <div id="wrapper">${changeset.diff}</div>
            </td>
          </tr>
          </#if>
        </#list>
      </table>
      
    </#list>

  </body>
</html>

