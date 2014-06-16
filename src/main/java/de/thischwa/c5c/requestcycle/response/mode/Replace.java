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

import de.thischwa.c5c.FilemanagerAction;

/**
 * Holds the data of an upload GenericResponse.
 */
public final class Replace extends GenericPost {

	public Replace(String errorMessage, int errorCode) {
		super(FilemanagerAction.REPLACE, errorMessage, errorCode);
	}

	public Replace(String path, String name) {
		super(FilemanagerAction.REPLACE, path, name);
	}

	@Override
	public String toString() {
		String jsonStr = super.toString();
		return String.format("<textarea>%s</textarea>", jsonStr);
	}
}
