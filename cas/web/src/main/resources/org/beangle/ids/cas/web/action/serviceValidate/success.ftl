[#ftl attributes={"content_type","text/xml; charset=utf-8"}/]
<?xml version="1.0" encoding="utf-8"?>
[#assign principal=result.ticket.principal/]
<sso:serviceResponse>
  <sso:authenticationSuccess>
    <sso:user>${principal.name}</sso:user>
      <sso:attributes>
        <sso:attribute name="userName" type="String" value="${principal.userName}"/>
      </sso:attributes>
  </sso:authenticationSuccess>
</sso:serviceResponse>