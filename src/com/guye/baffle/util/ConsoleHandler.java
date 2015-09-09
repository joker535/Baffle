package com.guye.baffle.util;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ConsoleHandler extends Handler{

    @Override
    public void publish( LogRecord record ) {
        System.out.println(record.getLevel() + ":" + record.getMessage());
    }

    @Override
    public void flush() {
        
    }

    @Override
    public void close() throws SecurityException {
        
    }

}
