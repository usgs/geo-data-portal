#Files pulled 2012.02.21 with following URI:

curl 'http://incus.cr.usgs.gov/ArcGIS/services/NED_1/MapServer/WCSServer?service=WCS&version=1.1.1&request=DescribeCoverage&identifiers=1' | xmllint --format - > ArcGIS-9.31-DescribeCoverage-1.1.1.xml
curl 'http://incus.cr.usgs.gov/ArcGIS/services/NED_1/MapServer/WCSServer?service=WCS&version=1.1.0&request=DescribeCoverage&identifiers=1' | xmllint --format - > ArcGIS-9.31-DescribeCoverage-1.1.0.xml

curl 'http://cida.usgs.gov/ArcGIS/services/NLCD_2006/MapServer/WCSServer?service=WCS&version=1.1.1&request=DescribeCoverage&Identifiers=1' | xmllint --format - > ArcGIS-9.31-DescribeCoverage-1.1.1.xml
curl 'http://cida.usgs.gov/ArcGIS/services/NLCD_2006/MapServer/WCSServer?service=WCS&version=1.1.0&request=DescribeCoverage&Identifiers=1' | xmllint --format - > ArcGIS-9.31-DescribeCoverage-1.1.0.xml

curl 'http://igsarmewmaccave.gs.doi.net:8082/geoserver/ows?service=WCS&version=1.1.1&request=DescribeCoverage&identifiers=ned' | xmllint --format - > GeoServer-2.1.3-DescribeCoverage-1.1.1.xml
curl 'http://igsarmewmaccave.gs.doi.net:8082/geoserver/ows?service=WCS&version=1.1.0&request=DescribeCoverage&identifiers=ned' | xmllint --format - > GeoServer-2.1.3-DescribeCoverage-1.1.0.xml

