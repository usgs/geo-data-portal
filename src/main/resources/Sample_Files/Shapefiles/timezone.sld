<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld"
  xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.cubewerx.com/schemas/sld/1.0.0-cw/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>Timezones</Name>
    <UserStyle>
      <Name>Timezones</Name>
      <IsDefault>1</IsDefault>
      <FeatureTypeStyle>
        <FeatureTypeName>timezone</FeatureTypeName>
					<Rule>

	       		<PolygonSymbolizer>
							<Stroke>
								<CssParameter name="stroke">#000000</CssParameter>
								<CssParameter name="stroke-width">2</CssParameter>
							</Stroke>
						</PolygonSymbolizer>

					<TextSymbolizer>
						<Label>
							<ogc:PropertyName>ZONE</ogc:PropertyName>
						</Label>
						<Font>
							<CssParameter name="font-style">normal</CssParameter>
						</Font>
					</TextSymbolizer>
				</Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
