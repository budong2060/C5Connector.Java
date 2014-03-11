/*
 * C5Connector.Java - The Java backend for the filemanager of corefive.
 * It's a bridge between the filemanager and a storage backend and 
 * works like a transparent VFS or proxy.
 * Copyright (C) Thilo Schwarz
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.thischwa.c5c.resource;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static object for managing the default and user-defined properties files.<br/>
 * The files are loaded in the following order:
 * <ol>
 * <li>The default properties.</li>
 * <li>The user-defined properties <code>c5connector.properties</code> if present.</li>
 * </ol> 
 * Static wrapper methods are provided for all properties.<br/>
 * Please note: The user-defined properties <em>override</em> the default one.<br/> 
 * Moreover, you can set properties programmatically too ({@link #setProperty(String, String)}), 
 * but make sure to override them <em>before</em> the first call of any property.
 */
public class PropertiesLoader {
	
	private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);
	
	private static final String DEFAULT_FILENAME = "default.properties";
	
	private static final String LOCAL_PROPERTIES = "/c5connector.properties";
	
	private static Properties properties = new Properties();
	
	private static Locale defaultLocale;

	static {
		// 1. load library defaults
		InputStream inDefault = PropertiesLoader.class.getResourceAsStream(DEFAULT_FILENAME);

		if (inDefault == null) {
			logger.error("{} not found", DEFAULT_FILENAME);
			throw new RuntimeException(DEFAULT_FILENAME + " not found");
		} else {
			if (!(inDefault instanceof BufferedInputStream))
				inDefault = new BufferedInputStream(inDefault);
			try {
				properties.load(inDefault);
				logger.info("{} successful loaded", DEFAULT_FILENAME);
			} catch (Exception e) {
				String msg = "Error while processing " + DEFAULT_FILENAME;
				logger.error(msg);
				throw new RuntimeException(msg, e);
			} finally {
				IOUtils.closeQuietly(inDefault);
			}
		}

		// 2. load user defaults if present
		InputStream inUser = PropertiesLoader.class.getResourceAsStream(LOCAL_PROPERTIES);
		if (inUser == null) {
			logger.info("{} not found", LOCAL_PROPERTIES);
		} else {
			if (!(inUser instanceof BufferedInputStream))
				inUser = new BufferedInputStream(inUser);
			try {
				properties.load(inUser);
				inUser.close();
				logger.info("{} successful loaded", LOCAL_PROPERTIES);
			} catch (Exception e) {
				String msg = "Error while processing " + LOCAL_PROPERTIES;
				logger.error(msg);
				throw new RuntimeException(msg, e);
			} finally {
				IOUtils.closeQuietly(inUser);
			}
		}
		
		// 3. build the default locale
		defaultLocale = new Locale(properties.getProperty("default.language"));
	}

	/**
	 * Searches for the property with the specified key in this property list.
	 *
	 * @param key the key
	 * @return the property
	 * @see Properties#getProperty(String)
	 */
	public static String getProperty(final String key) {
		return properties.getProperty(key);
	}

	/**
	 * Sets the property with the specified key in this property list.
	 *
	 * @param key the key
	 * @param value the value
	 * @see Properties#setProperty(String, String)
	 */
	public static void setProperty(final String key, final String value) {
		properties.setProperty(key, value);
	}

	/**
	 * Returns <code>default.encoding</code> property.
	 *
	 * @return the default character encoding
	 */
	public static String getDefaultEncoding() {
		return properties.getProperty("default.encoding");
	}
	
	/**
	 * Returns <code>connector.defaultEncoding</code> property.
	 *
	 * @return the default character encoding of the connector (http response)
	 */
	public static String getConnectorDefaultEncoding() {
		return properties.getProperty("connector.defaultEncoding");
	}
	
	/**
	 * Obtains the default locale dependent on the <code>default.language</code> property.
	 * 
	 * @return the default locale
	 */
	public static Locale getDefaultLocale() {
		return defaultLocale;
	}
	
	/**
	 * Returns <code>connector.filemanagerPath</code> property
	 *
	 * @return the filemanager path
	 */
	public static String getFilemanagerPath() {
		return properties.getProperty("connector.filemanagerPath");
	}

	/**
	 * Returns <code>connector.secureImageUploads</code> property
	 *
	 * @return true, if is secure image uploads
	 */
	public static boolean isSecureImageUploads() {
		return Boolean.valueOf(properties.getProperty("connector.secureImageUploads"));
	}
	
	/**
	 * Returns <code>connector.iconResolverImpl</code> property
	 *
	 * @return the icon resolver impl
	 */
	public static String getIconResolverImpl() {
		return properties.getProperty("connector.iconResolverImpl");
	}

	/**
	 * Returns <code>jii.impl</code> property
	 *
	 * @return the dimension provider impl
	 */
	public static String getDimensionProviderImpl() {
		return properties.getProperty("jii.impl");
	}

	/**
	 * Returns <code>connector.userPathBuilderImpl</code> property
	 *
	 * @return the user action impl
	 */
	public static String getUserPathBuilderImpl() {
		return properties.getProperty("connector.userPathBuilderImpl");
	}

	/**
	 * Returns <code>connector.messageResolverImpl</code> property
	 *
	 * @return the message resolver impl
	 */
	public static String getMessageResolverImpl() {
		return properties.getProperty("connector.messageResolverImpl");
	}

	/**
	 * Returns <code>connector.forceSingleExtension</code> property
	 *
	 * @return true, if is force single extension
	 */
	public static boolean isForceSingleExtension() {
		return Boolean.valueOf(properties.getProperty("connector.forceSingleExtension"));
	}

	/**
	 * Gets the connector impl.
	 *
	 * @return <code>connector.impl</code> property
	 */
	public static String getConnectorImpl() {
		return properties.getProperty("connector.impl");
	}
	
	/**
	 * Gets the default capacity.
	 *
	 * @return <code>connector.capabilities</code> property
	 */
	public static String getDefaultCapacity() {
		return properties.getProperty("connector.capabilities");
	} 
	
	/**
	 * Gets the maximum allowed file size in MB for upload. 
	 * 
	 * @return <code>connector.maxUploadSize</code> property, or null if not set or a {@link NumberFormatException} was thrown.
	 */
	public static Integer getMaxUploadSize() {
		try {
			return Integer.valueOf(properties.getProperty("connector.maxUploadSize"));
		} catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * Gets the file capability impl.
	 *
	 * @return <code>connector.fileCapabilityImpl</code> property
	 */
	public static String getFileCapabilityImpl() {
		return properties.getProperty("connector.fileCapabilityImpl");
	} 

	/**
	 * Gets the file config impl.
	 *
	 * @return <code>connector.filemanagerConfigImpl</code> property
	 */
	public static String getFilemanagerConfigImpl() {
		return properties.getProperty("connector.filemanagerConfigImpl");
	} 
	
	/**
	 * Gets the dimension for thumbnails.
	 *
	 * @return <code>connector.thumbnail.dimension</code> property
	 */
	public static String getThumbnailDimension() {
		return properties.getProperty("connector.thumbnail.dimension");
	} 
}
