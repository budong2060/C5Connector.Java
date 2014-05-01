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
package de.thischwa.c5c;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.thischwa.c5c.exception.C5CException;
import de.thischwa.c5c.exception.FilemanagerException;
import de.thischwa.c5c.impl.LocalConnector;
import de.thischwa.c5c.requestcycle.BackendPathBuilder;
import de.thischwa.c5c.requestcycle.response.mode.FileInfoProperties;
import de.thischwa.c5c.resource.filemanager.Exclude;
import de.thischwa.c5c.util.StringUtils;

/**
 * The backend base class for all implementations of the {@link Connector} interface. It provides some useful helper methods.<br/>
 * In general a connector serves and manages files and folders accessed through the filemanager on an arbitrary backend system. The
 * connector will retrieve a valid request. 'Valid' means in terms of correct and reasonable parameters.<br/>
 * Most of the methods get a parameter named 'backendPath' or similar. This is the requested url-path mapped to a o path of the backend by
 * the {@link BackendPathBuilder}.<br/>
 * <br/>
 * <b>Hint for implementations:</b> For throwing known exceptions of the filemanager, the {@link FilemanagerException} must be used! Helpful
 * constructors are provided.
 */
public abstract class GenericConnector implements Connector {

	protected static Logger logger = LoggerFactory.getLogger(LocalConnector.class);

	private Set<String> imageExtensions;

	@Override
	public void setImageExtensions(Set<String> imageExtensions) {
		this.imageExtensions = imageExtensions;
	}

	/**
	 * Simple container object to hold data which is needed to stream content via {@link InputStream} e.g. it's needed for the download
	 * action.
	 */
	public static class StreamContent {

		private InputStream in;
		private long size;

		StreamContent(InputStream in, long size) {
			this.in = in;
			this.size = size;
		}

		public InputStream getInputStream() {
			return in;
		}

		public long getSize() {
			return size;
		}
	}

	/**
	 * Object to hold the properties of a file or folder. It is used for the response of a get-request.
	 */
	public static class FileProperties extends FileInfoProperties {

		private boolean isDir;

		FileProperties(String name, Date modified) {
			super(name, modified);
			isDir = true;
		}

		FileProperties(String name, long size, Date modified) {
			super(name, size, modified);
			isDir = false;
		}

		FileProperties(String name, int width, int height, long size, Date modified) {
			super(name, width, height, size, modified);
			isDir = false;
		}

		@JsonIgnore
		public boolean isDir() {
			return isDir;
		}
	}

	/**
	 * Initializes the connector. Can be overridden by the inherited object.
	 * 
	 * @throws Exception
	 *             if the initialization fails.
	 */
	@Override
	public void init() throws RuntimeException {
		logger.info("*** {} sucessful initialized.", this.getClass().getName());
	}

	@Override
	public abstract List<GenericConnector.FileProperties> getFolder(String backendPath, boolean needSize) throws C5CException;

	@Override
	public abstract GenericConnector.FileProperties getInfo(String backendPath, boolean needSize) throws C5CException;

	@Override
	public abstract boolean rename(String oldBackendPath, String sanitizedNewName) throws C5CException;

	@Override
	public abstract void createFolder(String backendDirectory, String sanitizedName) throws C5CException;

	@Override
	public abstract boolean delete(String backendPath) throws C5CException;

	@Override
	public abstract void upload(String backendDirectory, String sanitizedName, InputStream in) throws C5CException;

	@Override
	public abstract GenericConnector.StreamContent download(String backendPath) throws C5CException;
	
	@Override
	public abstract String editFile(String backendPath) throws C5CException;

	/**
	 * Checks whether the name of the file is valid in terms of the configuration.
	 * 
	 * @param name
	 *            the name of the file to check
	 * @return <code>true</code> if the name of the file is valid, otherwise <code>false</code>
	 * 
	 * @throws IOException
	 *             if the based regex isn't valid
	 */
	protected boolean checkFilename(String name) throws IOException {
		Exclude exclude = UserObjectProxy.getFilemanagerConfig().getExclude();
		Set<String> disAllowedFiles = exclude.getDisallowedFiles();
		String regex = exclude.getDisallowedFilesRegex();
		return checkFilename(name, disAllowedFiles, regex);
	}

	/**
	 * Checks whether the name of the directory is valid in terms of the configuration.
	 * 
	 * @param name
	 *            the name of the directory to check
	 * @return <code>true</code> if the name of the directory is valid, otherwise <code>false</code>
	 * 
	 * @throws IOException
	 *             if the based regex isn't valid
	 */
	protected boolean checkDirectoryname(String name) throws IOException {
		Exclude exclude = UserObjectProxy.getFilemanagerConfig().getExclude();
		Set<String> disAllowedDirs = exclude.getDisallowedDirs();
		String regex = exclude.getDisallowedDirsRegex();
		return checkFilename(name, disAllowedDirs, regex);
	}

	private boolean checkFilename(String name, Set<String> disallowedNames, String disallowedRegex) throws IOException {
		if(disallowedNames != null && disallowedNames.contains(name))
			return false;
		if(StringUtils.isNullOrEmptyOrBlank(disallowedRegex))
			return true;

		if(disallowedRegex.startsWith("/"))
			disallowedRegex = disallowedRegex.substring(1);
		try {
			Pattern pattern = Pattern.compile(disallowedRegex);
			Matcher matcher = pattern.matcher(name);
			return !matcher.find();
		} catch (PatternSyntaxException e) {
			throw new IOException(String.format("Regex [%s] could not be parsed!", disallowedRegex), e);
		}
	}

	/**
	 * Builds the {@link FileInfoProperties} which holds the basic properties of a representation of a image of the filemanager.
	 * 
	 * @param name
	 *            the name of the file
	 * @param width
	 *            the width of the image
	 * @param height
	 *            the height of the image
	 * @param size
	 *            the absolute size of the file
	 * @param modified
	 *            the date the file was last modified
	 * @return The initialized {@link FileInfoProperties}.
	 */
	protected GenericConnector.FileProperties buildForImage(String name, int width, int height, long size, Date modified) {
		return new GenericConnector.FileProperties(name, width, height, size, modified);
	}

	/**
	 * Builds the {@link FileInfoProperties} which holds the basic properties of a representation of a file of the filemanager.
	 * 
	 * @param name
	 *            the name of the file
	 * @param size
	 *            the absolute size of the file
	 * @param modified
	 *            the date the file was last modified
	 * @return The initialized {@link FileInfoProperties}.
	 */
	protected GenericConnector.FileProperties buildForFile(String name, long size, Date modified) {
		return new GenericConnector.FileProperties(name, size, modified);
	}

	/**
	 * Builds the {@link FileInfoProperties} which holds the basic properties of a representation of a directory of the filemanager.
	 * 
	 * @param name
	 *            the name of the file
	 * @param modified
	 *            the date the file was last modified
	 * @return The initialized {@link FileInfoProperties}.
	 */
	protected GenericConnector.FileProperties buildForDirectory(String name, Date modified) {
		return new GenericConnector.FileProperties(name, modified);
	}

	/**
	 * Builds the {@link GenericConnector.StreamContent} which holds the data for the response of the download.
	 * 
	 * @param in
	 *            {@link InputStream} of the file to download
	 * @param fileSize
	 *            size of the file to download
	 * @return The initialized {@link GenericConnector.StreamContent}.
	 */
	protected GenericConnector.StreamContent buildStreamContent(InputStream in, long fileSize) {
		return new GenericConnector.StreamContent(in, fileSize);
	}

	protected boolean isImageExtension(String ext) {
		if(StringUtils.isNullOrEmpty(ext) || imageExtensions == null)
			return false;
		return imageExtensions.contains(ext);
	}
}
