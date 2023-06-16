package org.project.playgrounds.exceptions;

public class EnqueueRequestRejected extends RuntimeException {
    public EnqueueRequestRejected(String message) {
        super(message);
    }
}
