package com.guye.baffle.util;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.LogLevel;

public class AntLogHandle extends Handler {

    private Task task;

    public AntLogHandle(Task task) {
        this.task = task;
    }

    @Override
    public void publish( LogRecord record ) {
        task.log(record.getMessage(), LogLevel.INFO.getLevel());

    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

 
}
