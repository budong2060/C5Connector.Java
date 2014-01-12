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
package de.thischwa.c5c.requestcycle.response.mode;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.thischwa.c5c.FilemanagerAction;
import de.thischwa.c5c.requestcycle.response.GenericResponse;

/**
 * Holds the data for a Delete response.
 */
public final class Delete extends GenericResponse {

	private String fullPath;

	public Delete(String fullPath) {
		super(FilemanagerAction.DELETE);
		this.fullPath = fullPath;
	}
	
	@JsonProperty("Path")
	public String getFullPath() {
		return fullPath;
	}

}
