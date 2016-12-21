package tw.com.softeader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final Properties prop = new Properties();

	String propFileName = "config.properties";

	private static final Config instance = new Config();

	private Config() {

		// Private constructor to restrict new instances
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(propFileName);
		log.debug("Read all properties from file : " + propFileName);
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Config getInstance() {
		return instance;
	}

	public String getValue(String propKey) {
		return this.prop.getProperty(propKey);
	}

}
