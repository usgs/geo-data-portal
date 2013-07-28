package gov.usgs.cida.gdp.dataaccess.bean;

import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ucar.nc2.VariableSimpleIF;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

@XStreamAlias("datatypecollection")
public class DataTypeCollection extends Response {

	private static final transient org.slf4j.Logger log = LoggerFactory.getLogger(DataTypeCollection.class);
	private static final long serialVersionUID = 32424L;
	@XStreamAlias("datatype")
	@XStreamAsAttribute
	private String dataType;
	@XStreamAlias("types")
	@XStreamImplicit(itemFieldName = "types")
	private List<DataTypeBean> dataTypeCollection;

	public DataTypeCollection() {
	}

	public DataTypeCollection(String type, DataTypeBean... dataTypeArray) {
		this.dataType = type;
		this.dataTypeCollection = Arrays.asList(dataTypeArray);
	}

	public DataTypeCollection(String type, VariableSimpleIF... variableSimpleIFArray) {
		this.dataType = type;
		List<DataTypeBean> dtbList = new ArrayList<DataTypeBean>(variableSimpleIFArray.length);
		for (VariableSimpleIF vsif : variableSimpleIFArray) {
			DataTypeBean dtb = new DataTypeBean(vsif);
			dtbList.add(dtb);
		}
		this.dataTypeCollection = dtbList;
	}

	@Override
	public String toXML() {
		String result;
		QNameMap qmap = new QNameMap();
		qmap.setDefaultNamespace("xsd/gdpdatatypecollection-1.0.xsd");
		qmap.setDefaultPrefix("gdp");
		StaxDriver sd = new StaxDriver(qmap);
		XStream xstream = new XStream(sd);
		xstream.autodetectAnnotations(true);
		result = xstream.toXML(this);
		return result;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the dataTypeCollection
	 */
	public List<DataTypeBean> getDataTypeCollection() {
		return dataTypeCollection;
	}

	/**
	 * @param dataTypeCollection the dataTypeCollection to set
	 */
	public void setDataTypeCollection(List<DataTypeBean> dataTypeCollection) {
		this.dataTypeCollection = dataTypeCollection;
	}

	public static DataTypeCollection buildFromCache(ResponseCache.CacheIdentifier ci) {
		DataTypeCollection result;
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		File cacheFile;
		try {
			cacheFile = ci.getFile();
		} catch (IOException ex) {
			log.warn("Could not create new cache file", ex);
			return null;
		}
		try {
			try {
				fileIn = new FileInputStream(cacheFile);
			} catch (FileNotFoundException ex) {
				log.warn("Could not find cache file {}", cacheFile.getPath(), ex);
				return null;
			}

			try {
				in = new ObjectInputStream(fileIn);
			} catch (IOException ex) {
				log.warn("Could not read cache file {}", cacheFile.getPath(), ex);
				return null;
			}

			try {
				result = (DataTypeCollection) in.readObject();
			} catch (IOException ex) {
				log.warn("Could not inflate cache file {}", cacheFile.getPath(), ex);
				return null;
			} catch (ClassNotFoundException ex) {
				log.warn("Could not inflate cache file {}", cacheFile.getPath(), ex);
				return null;
			}
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fileIn);
		}
		return result;
	}

	/**
	 *
	 * @param ci
	 * @return
	 */
	@Override
	public boolean writeToCache(ResponseCache.CacheIdentifier ci) {
		FileOutputStream filOutputStream = null;
		ObjectOutputStream objOutputStream = null;
		File cacheFile;
		try {
			cacheFile = ci.getFile();
		} catch (IOException ex) {
			log.warn("Could not create new cache file", ex);
			return false;
		}
		try {
			try {
				cacheFile.createNewFile();
				filOutputStream = new FileOutputStream(cacheFile);
			} catch (FileNotFoundException ex) {
				log.warn("Could not open output stream to file {}", cacheFile.getPath(), ex);
				return false;
			} catch (IOException ex) {
				log.warn("Could notcreate new cache file {}", cacheFile.getPath(), ex);
				return false;
			}

			try {
				objOutputStream = new ObjectOutputStream(filOutputStream);
			} catch (IOException ex) {
				log.warn("Could not open object output stream to file {}", cacheFile.getPath(), ex);
				return false;
			}
			try {
				objOutputStream.writeObject(this);
			} catch (IOException ex) {
				log.warn("Could not write object output to output stream", ex);
				return false;
			}
			return true;
		} finally {
			IOUtils.closeQuietly(filOutputStream);
			IOUtils.closeQuietly(objOutputStream);
		}
	}

	@XStreamAlias("type")
	static public class DataTypeBean extends Response {

		private static final transient org.slf4j.Logger dtbLog = LoggerFactory.getLogger(DataTypeCollection.class);
		private String description;
		private String name;
		private int rank;
		private int[] shape;
		private String shortname;
		private String unitsstring;

		public DataTypeBean() {
		}

		public DataTypeBean(VariableSimpleIF variableSimpleIF) {
			this.description = variableSimpleIF.getDescription();
			this.name = variableSimpleIF.getName();
			this.rank = variableSimpleIF.getRank();
			this.shape = variableSimpleIF.getShape();
			this.shortname = variableSimpleIF.getShortName();
			this.unitsstring = variableSimpleIF.getUnitsString();
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		public int[] getShape() {
			return shape;
		}

		public void setShape(int[] shape) {
			this.shape = shape.clone();
		}

		public String getShortname() {
			return shortname;
		}

		public void setShortname(String shortname) {
			this.shortname = shortname;
		}

		public String getUnitsstring() {
			return unitsstring;
		}

		public void setUnitsstring(String unitsstring) {
			this.unitsstring = unitsstring;
		}

		public static DataTypeBean buildFromCache(ResponseCache.CacheIdentifier ci) {
			DataTypeBean result;
			FileInputStream fileIn = null;
			ObjectInputStream in = null;
			File cacheFile;
			try {
				cacheFile = ci.getFile();
			} catch (IOException ex) {
				log.warn("Could not create new cache file", ex);
				return null;
			}

			try {
				try {
					fileIn = new FileInputStream(cacheFile);
				} catch (FileNotFoundException ex) {
					dtbLog.warn("Could not find cache file {}", cacheFile.getPath(), ex);
					return null;
				}

				try {
					in = new ObjectInputStream(fileIn);
				} catch (IOException ex) {
					dtbLog.warn("Could not read cache file {}", cacheFile.getPath(), ex);
					return null;
				}

				try {
					result = (DataTypeBean) in.readObject();
				} catch (IOException ex) {
					dtbLog.warn("Could not inflate cache file {}", cacheFile.getPath(), ex);
					return null;
				} catch (ClassNotFoundException ex) {
					dtbLog.warn("Could not inflate cache file {}", cacheFile.getPath(), ex);
					return null;
				}
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(fileIn);
			}
			return result;
		}

		/**
		 *
		 * @param ci
		 * @return
		 */
		@Override
		public boolean writeToCache(ResponseCache.CacheIdentifier ci) {
			FileOutputStream filOutputStream = null;
			ObjectOutputStream objOutputStream = null;
			File cacheFile;
			try {
				cacheFile = ci.getFile();
			} catch (IOException ex) {
				log.warn("Could not create new cache file", ex);
				return false;
			}
			
			try {
				try {
					cacheFile.createNewFile();
					filOutputStream = new FileOutputStream(cacheFile);
				} catch (FileNotFoundException ex) {
					log.warn("Could not open output stream to file {}", cacheFile.getPath(), ex);
					return false;
				} catch (IOException ex) {
					log.warn("Could notcreate new cache file {}", cacheFile.getPath(), ex);
					return false;
				}

				try {
					objOutputStream = new ObjectOutputStream(filOutputStream);
				} catch (IOException ex) {
					log.warn("Could not open object output stream to file {}", cacheFile.getPath(), ex);
					return false;
				}
				try {
					objOutputStream.writeObject(this);
				} catch (IOException ex) {
					log.warn("Could not write object output to output stream", ex);
					return false;
				}
				return true;
			} finally {
				IOUtils.closeQuietly(filOutputStream);
				IOUtils.closeQuietly(objOutputStream);
			}
		}
	}
}