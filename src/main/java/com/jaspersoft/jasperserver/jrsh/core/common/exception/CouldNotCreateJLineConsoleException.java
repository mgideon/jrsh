package com.jaspersoft.jasperserver.jrsh.core.common.exception;

/**
 * @author Alexander Krasnyanskiy
 */
public class CouldNotCreateJLineConsoleException extends RuntimeException {
    public CouldNotCreateJLineConsoleException() {
        super("Could not create a console");
    }
}
