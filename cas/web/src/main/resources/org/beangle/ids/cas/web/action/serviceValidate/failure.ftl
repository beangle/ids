[#ftl attributes={"content_type","text/xml; charset=utf-8"}/]
<?xml version="1.0" encoding="utf-8"?>
<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
  <cas:authenticationFailure code='${result.code}'>${result.description?xml}</cas:authenticationFailure>
</cas:serviceResponse>
