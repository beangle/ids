[#ftl attributes={"content_type","text/xml; charset=utf-8"}/]
<?xml version="1.0" encoding="utf-8"?>
[#assign principal=result.ticket.principal/]
<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
  <cas:authenticationSuccess>
    <cas:user>${principal.name}</cas:user>
      <cas:attributes>
        <cas:userName>${principal.description}</cas:userName>
        [#if principal.authorities??]<cas:authorities>[#list principal.authorities as a]${a}[#if a_has_next],[/#if][/#list]</cas:authorities>[/#if]
        [#if principal.permissions??]<cas:permissions>[#list principal.permissions as a]${a}[#if a_has_next],[/#if][/#list]</cas:permissions>[/#if]
        [#if principal.details??]
        [#list principal.details?keys as key]
        <cas:${key}>${principal.details[key]}</cas:${key}>
        [/#list]
        [/#if]
      </cas:attributes>
  </cas:authenticationSuccess>
</cas:serviceResponse>
