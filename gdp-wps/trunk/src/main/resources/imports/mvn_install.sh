#!/bin/sh
mvn install:install-file -Dfile=net.opengis.wps-2.7.2.jar -DgroupId=org.geotools.ogc -DartifactId=net.opengis.wps -Dpackaging=jar -Dversion=2.7.2
mvn install:install-file -Dfile=gt-geojson-2.7.2.jar -DgroupId=org.geotools -DartifactId=gt-geojson -Dpackaging=jar -Dversion=2.7.2
mvn install:install-file -Dfile=gt-process-2.7.2.jar -DgroupId=org.geotools -DartifactId=gt-process -Dpackaging=jar -Dversion=2.7.2
mvn install:install-file -Dfile=gt-xsd-wps-2.7.2.jar -DgroupId=org.geotools.xsd -DartifactId=gt-xsd-wps -Dpackaging=jar -Dversion=2.7.2
mvn install:install-file -Dfile=jt-attributeop-1.1.1.jar -DgroupId=com.googlecode.jaitools -DartifactId=jt-attributeop -Dpackaging=jar -Dversion=1.1.1
mvn install:install-file -Dfile=jt-contour-1.1.1.jar -DgroupId=com.googlecode.jaitools -DartifactId=jt-contour -Dpackaging=jar -Dversion=1.1.1
mvn install:install-file -Dfile=jt-rangelookup-1.1.1.jar -DgroupId=com.googlecode.jaitools -DartifactId=jt-rangelookup -Dpackaging=jar -Dversion=1.1.1
mvn install:install-file -Dfile=jt-utils-1.1.1.jar -DgroupId=com.googlecode.jaitools -DartifactId=jt-utils -Dpackaging=jar -Dversion=1.1.1
mvn install:install-file -Dfile=jt-vectorbinarize-1.1.1.jar -DgroupId=com.googlecode.jaitools -DartifactId=jt-vectorbinarize -Dpackaging=jar -Dversion=1.1.1
mvn install:install-file -Dfile=jt-vectorize-1.1.1.jar -DgroupId=com.googlecode.jaitools -DartifactId=jt-vectorize -Dpackaging=jar -Dversion=1.1.1
mvn install:install-file -Dfile=jt-zonalstats-1.1.1.jar -DgroupId=com.googlecode.jaitools -DartifactId=jt-zonalstats -Dpackaging=jar -Dversion=1.1.1
mvn install:install-file -Dfile=web-wps-2.1.1.jar -DgroupId=org.geoserver.extension -DartifactId=web-wps -Dpackaging=jar -Dversion=2.1.1
mvn install:install-file -Dfile=wps-core-2.1.1.jar -DgroupId=org.geoserver.extension -DartifactId=wps-core -Dpackaging=jar -Dversion=2.1.1

