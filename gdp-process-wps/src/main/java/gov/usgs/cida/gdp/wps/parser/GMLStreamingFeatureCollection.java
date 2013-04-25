package gov.usgs.cida.gdp.wps.parser;

import com.vividsolutions.jts.geom.Geometry;
import gov.usgs.cida.gdp.wps.util.GMLUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.StreamingParser;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author tkunicki
 */
public class GMLStreamingFeatureCollection implements FeatureCollection {

    private final static Logger LOGGER = LoggerFactory.getLogger(GMLStreamingFeatureCollection.class);
    
	final private File file;
	final private SimpleFeatureType featureType;
	final private ReferencedEnvelope bounds;
	final private int size;
	final private Configuration configuration;

	GMLStreamingFeatureCollection(File file) {

        LOGGER.debug("Starting parse of file {}", file.getName());
		this.file = file;
		this.configuration = GMLUtil.generateGMLConfiguration(file);

		StreamingFeatureIterator iterator = null;
		try {
			MetaDataFilter metaDataFilter = new MetaDataFilter();
			iterator = new StreamingFeatureIterator(metaDataFilter, false);
			while (iterator.hasNext()) {
				iterator.next();
			}
			this.bounds = metaDataFilter.collectionBounds;
			this.featureType = metaDataFilter.wrappedFeatureType;
			this.size = metaDataFilter.size;

            if (size < 1) {
                throw new RuntimeException("Empty Feature Collection");
            }

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}
        LOGGER.debug("Finished parse of file {}, {} features found", file.getName(), size);
	}
    
    public void dispose() {
        FileUtils.deleteQuietly(file);
        // Need to wait for GeoTools 8.x
        //configuration.getXSD().dispose();
    }

	@Override
	public FeatureIterator features() {
		return createStreamingFeatureIterator();
	}

	@Override
	public Iterator iterator() {
		return createStreamingFeatureIterator();
	}

	private StreamingFeatureIterator createStreamingFeatureIterator() {
		StreamingFeatureIterator iterator = null;
		try {
			iterator = new StreamingFeatureIterator(FILTER_PASSTHRU);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return iterator;
	}

	@Override
	public void close(FeatureIterator close) {
		destroyStreamingFeatureIterator(close);
	}

	@Override
	public void close(Iterator close) {
		destroyStreamingFeatureIterator(close);
	}

	protected void destroyStreamingFeatureIterator(Object object) {
		if (object instanceof StreamingFeatureIterator) {
			StreamingFeatureIterator iterator = (StreamingFeatureIterator) object;
			if (iterator.getParent() == this) {
				iterator.close();
				return;
			}
		}
		throw new RuntimeException("iterator not known by this instance");
	}

	@Override
	public void addListener(CollectionListener listener) throws NullPointerException {
		// do nothing, this collection is read-only
	}

	@Override
	public void removeListener(CollectionListener listener) throws NullPointerException {
		// do nothing, this collection is read-only
	}

	@Override
	public FeatureType getSchema() {
		return featureType;
	}

	@Override
	public String getID() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public FeatureCollection subCollection(Filter filter) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public FeatureCollection sort(SortBy order) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ReferencedEnvelope getBounds() {
		return bounds;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void purge() {
		throw new UnsupportedOperationException("This instance is read-only");
	}

	@Override
	public boolean add(Feature obj) {
		throw new UnsupportedOperationException("This instance is read-only");
	}

	@Override
	public boolean addAll(Collection collection) {
		throw new UnsupportedOperationException("This instance is read-only");
	}

	@Override
	public boolean addAll(FeatureCollection resource) {
		throw new UnsupportedOperationException("This instance is read-only");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("This instance is read-only");
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean containsAll(Collection o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isEmpty() {
		return size() > 0;
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("This instance is read-only");
	}

	@Override
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException("This instance is read-only");
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException("This instance is read-only");
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Only streaming supported");
	}

	@Override
	public Object[] toArray(Object[] a) {
		throw new UnsupportedOperationException("Only streaming supported");
	}

    private SimpleFeature wrap(SimpleFeature base) {
        return new SimpleFeatureImpl(
                base.getAttributes(),
                featureType,
                base.getIdentifier());
    }

	private final class StreamingFeatureIterator implements FeatureIterator<Feature>, Iterator<Feature> {

		private StreamingParser parser;
		private InputStream inputStream;
		private Filter filter;
		private SimpleFeature next;
		private boolean open;
        private boolean wrap;

        private StreamingFeatureIterator(Filter filter) throws ParserConfigurationException, SAXException, FileNotFoundException {
            this(filter, true);
        }

		private StreamingFeatureIterator(Filter filter, boolean wrap) throws ParserConfigurationException, SAXException, FileNotFoundException {
			this.filter = filter;
            this.wrap = wrap;

			inputStream = new BufferedInputStream(
					new FileInputStream(file),
					16 << 10);
			parser = new StreamingParser(
					configuration,
					inputStream,
					SimpleFeature.class);
			open = true;
		}

		@Override
		public synchronized boolean hasNext() {
			if (next == null) {
				findNext();
			}
			return next != null;
		}

		@Override
		public synchronized Feature next() throws NoSuchElementException {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			SimpleFeature current = next;
			next = null;
			return current;
		}

		@Override
		public synchronized void close() {
			if (open) {
				next = null;
				filter = null;
				parser = null;
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						// do nothing, cleaning up
					}
					inputStream = null;
				}
				open = false;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		public GMLStreamingFeatureCollection getParent() {
			return GMLStreamingFeatureCollection.this;
		}

		protected void findNext() {
			while (next == null && open) {
				Object parsed = parser.parse();
				if (parsed instanceof SimpleFeature) {
					SimpleFeature candidate = (SimpleFeature) parsed;
					if (filter.evaluate(candidate)) {
						next = wrap ? wrap(candidate) : candidate;
					}
				} else {
					close();
				}
			}
		}
	}

	private static Filter FILTER_PASSTHRU = new Filter() {
		@Override public boolean evaluate(Object object) { return true; }
		@Override public Object accept(FilterVisitor visitor, Object extraData) { return true; }
	};

	private class MetaDataFilter implements Filter {

        private CoordinateReferenceSystem collectionCRS;
		private ReferencedEnvelope collectionBounds;
        private SimpleFeatureType baseFeatureType;
		private SimpleFeatureType wrappedFeatureType;
		private int size;

		@Override
		public boolean evaluate(Object object) {
			if (object instanceof SimpleFeature) {
				SimpleFeature feature = (SimpleFeature) object;
                
                Object geometryObject = feature.getDefaultGeometry();
                if (geometryObject instanceof Geometry) {
                    Object crsObject = ((Geometry)feature.getDefaultGeometry()).getUserData();
                    if (crsObject instanceof CoordinateReferenceSystem) {
                        CoordinateReferenceSystem featureCRS = (CoordinateReferenceSystem)crsObject;
                        if (collectionCRS == null) {
                            collectionCRS = featureCRS;
                            LOGGER.debug("CRS for file {}: {}", collectionCRS);
                        }
                        if (collectionBounds == null) {
                            collectionBounds = new ReferencedEnvelope(collectionCRS);
                        }
                        if (CRS.equalsIgnoreMetadata(featureCRS, collectionCRS)) {
                            collectionBounds.include(feature.getBounds());
                        } else {
                            throw new RuntimeException("Inconsistent CRS encountered.");
                        }
                    } else {
                        throw new RuntimeException("Error extracting CRS from feature geometry");
                    }
                } else {
                    throw new RuntimeException("Error extracting geometry from feature");
                }

                if (baseFeatureType == null) {
                    baseFeatureType = feature.getFeatureType();
                } else if (!baseFeatureType.equals(feature.getFeatureType())) {
                    throw new RuntimeException("FeatureType mismatch");
                }
                if (wrappedFeatureType == null) {
                    // FeatureType from GML parser deosn't contain CRS, we
                    // need to regenerate new FeatureType w/ CRS as this
                    // information is required downstream...
                    GeometryDescriptor baseGeometryDescriptor = baseFeatureType.getGeometryDescriptor();
                    GeometryType baseGeometryType = baseGeometryDescriptor.getType();
                    GeometryType collectionGeometryType = new GeometryTypeImpl(
                            baseGeometryType.getName(),
                            baseGeometryType.getBinding(),
                            collectionCRS,
                            baseGeometryType.isIdentified(),
                            baseGeometryType.isAbstract(),
                            baseGeometryType.getRestrictions(),
                            baseGeometryType.getSuper(),
                            baseGeometryType.getDescription());
                    GeometryDescriptor collectionGeometryDescriptor =
                            new GeometryDescriptorImpl(
                                collectionGeometryType,
                                baseGeometryDescriptor.getName(),
                                baseGeometryDescriptor.getMinOccurs(),
                                baseGeometryDescriptor.getMaxOccurs(),
                                baseGeometryDescriptor.isNillable(),
                                null);
                    wrappedFeatureType = new SimpleFeatureTypeImpl(
                            baseFeatureType.getName(),
                            baseFeatureType.getAttributeDescriptors(),
                            collectionGeometryDescriptor,
                            baseFeatureType.isAbstract(),
                            baseFeatureType.getRestrictions(),
                            baseFeatureType.getSuper(),
                            baseFeatureType.getDescription());
                }
                LOGGER.debug("Processed feature index {} in file {}", size, file.getName());
                ++size;
                return true;
			} else {
                throw new RuntimeException("Error extracting feature from GML.");
            }
		}

		@Override
		public Object accept(FilterVisitor visitor, Object extraData) {
			throw new UnsupportedOperationException("Unimplemented.");
		}
	}

}
