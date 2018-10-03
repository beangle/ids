<!DOCTYPE html>
<html lang="zh_CN">
  <head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <meta http-equiv="content-style-type" content="text/css"/>
    <meta http-equiv="content-script-type" content="text/javascript"/>
    <meta http-equiv="expires" content="0"/>
  </head>
  <body>
    正在退出业务系统...
    [#list services  as service]
      <iframe src="${service}[#if service?contains('?')]&[#else]?[/#if]logoutRequest=1" style="width:1px;height:1px;display:none"></iframe>
    [/#list]
    <script>
      setTimeout(logout,700);
      function logout(){
         document.location="${base}/logout[#if Parameters['service']??]?service=${Parameters['service']}[/#if]"
      }
    </script>
  </body>
</html>
