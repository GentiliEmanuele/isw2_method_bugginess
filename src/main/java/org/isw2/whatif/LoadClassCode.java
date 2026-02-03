package org.isw2.whatif;

import org.isw2.dataset.exceptions.ProcessingException;
import weka.core.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LoadClassCode {

    private LoadClassCode() {
    }

    public static String codeToString(String path) throws ProcessingException {
        try (InputStream is = ResourceUtils.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new ProcessingException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }
}
