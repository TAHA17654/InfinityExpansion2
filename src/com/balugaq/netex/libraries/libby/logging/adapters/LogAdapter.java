package com.balugaq.netex.libraries.libby.logging.adapters;

import com.balugaq.netex.libraries.libby.logging.LogLevel;

public interface LogAdapter {
   void log(LogLevel level, String message);

   void log(LogLevel level, String message, Throwable throwable);
}
