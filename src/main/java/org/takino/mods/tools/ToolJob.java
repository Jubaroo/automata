package org.takino.mods.tools;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSuchTemplateException;

import java.io.IOException;

public interface ToolJob {
    void doWork(Item target)  throws NoSuchTemplateException, FailedException, IOException;
}
