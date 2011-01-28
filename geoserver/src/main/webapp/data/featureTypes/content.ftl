<#list features as feature>
<feature>
<type>${type.name}</type>
<id>${feature.fid})</id>
<attributes>
<#list feature.attributes as attribute>
<#if !attribute.isGeometry>
<${attribute.name}>${attribute.value}</${attribute.name}>
</#if>
</#list>
</attributes>
</feature>
</#list>

