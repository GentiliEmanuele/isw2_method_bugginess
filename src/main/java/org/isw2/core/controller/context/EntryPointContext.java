package org.isw2.core.controller.context;

import org.isw2.factory.ExecutionContext;

public record EntryPointContext(String projectName) implements ExecutionContext {
}
