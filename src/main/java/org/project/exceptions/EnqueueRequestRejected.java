package org.project.exceptions;

public class EnqueueRequestRejected extends RuntimeException {
    public EnqueueRequestRejected(String message) {
        super(message);
    }
}
