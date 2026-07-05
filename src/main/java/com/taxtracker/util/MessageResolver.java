package com.taxtracker.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

// Resolves message keys (e.g. "app.message.user.not.found") from
// application.properties via Environment.getProperty(). Exception messages
// are never hardcoded; services throw exceptions carrying the property KEY.
@Component
public class MessageResolver {

    @Autowired
    private Environment environment;

    public String resolve(String key) {
        String value = environment.getProperty(key);
        return value != null ? value : key;
    }
}
