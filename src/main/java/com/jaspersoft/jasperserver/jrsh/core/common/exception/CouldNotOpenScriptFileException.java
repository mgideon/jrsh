package com.jaspersoft.jasperserver.jrsh.core.common.exception;

import static java.lang.String.format;

/**
 * @author Alexander Krasnyanskiy
 */
public class CouldNotOpenScriptFileException extends RuntimeException {
    public CouldNotOpenScriptFileException(String file) {
        super(format("Could not open a script file: %s", file));
    }
}
