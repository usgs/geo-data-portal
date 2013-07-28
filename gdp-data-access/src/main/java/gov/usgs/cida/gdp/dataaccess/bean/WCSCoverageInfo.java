package gov.usgs.cida.gdp.dataaccess.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache;
import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache.CacheIdentifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

@XStreamAlias("WCSCoverageInfo")
public class WCSCoverageInfo extends Response {

	private static transient final org.slf4j.Logger log = LoggerFactory.getLogger(WCSCoverageInfo.class);
	private static long serialVersionUID = 234234L;

	/**
	 * @return the serialVersionUID
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * @param aSerialVersionUID the serialVersionUID to set
	 */
	public static void setSerialVersionUID(long aSerialVersionUID) {
		serialVersionUID = aSerialVersionUID;
	}
	private String minResamplingFactor;
	private String fullyCovers;
	private String units;
	private String boundingBox;

	public WCSCoverageInfo(int minResamplingFactor, boolean fullyCovers,
			String units, String boundingBox) {

		this.minResamplingFactor = String.valueOf(minResamplingFactor);
		this.fullyCovers = String.valueOf(fullyCovers);
		this.units = units;
		this.boundingBox = boundingBox;
	}

	/**
	 * @return the minResamplingFactor
	 */
	public String getMinResamplingFactor() {
		return minResamplingFactor;
	}

	/**
	 * @param minResamplingFactor the minResamplingFactor to set
	 */
	public void setMinResamplingFactor(String minResamplingFactor) {
		this.minResamplingFactor = minResamplingFactor;
	}

	/**
	 * @return the fullyCovers
	 */
	public String getFullyCovers() {
		return fullyCovers;
	}

	/**
	 * @param fullyCovers the fullyCovers to set
	 */
	public void setFullyCovers(String fullyCovers) {
		this.fullyCovers = fullyCovers;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @return the boundingBox
	 */
	public String getBoundingBox() {
		return boundingBox;
	}

	/**
	 * @param boundingBox the boundingBox to set
	 */
	public void setBoundingBox(String boundingBox) {
		this.boundingBox = boundingBox;
	}

	public static WCSCoverageInfo buildFromCache(CacheIdentifier ci) {
		WCSCoverageInfo result;
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
				result = (WCSCoverageInfo) in.readObject();
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
				log.warn("Could not create new cache file {}", cacheFile.getPath(), ex);
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
