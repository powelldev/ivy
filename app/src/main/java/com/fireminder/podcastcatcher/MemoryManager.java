package com.fireminder.podcastcatcher;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

/**
 * Responsible for managing
 * 1) Size of Ivy-related files on the device
 * 2) Mapping id->file in a key-value type system
 * (Since episodes can have mp3s and images)
 */
public class MemoryManager {

  HashMap<Object, File> mFileHashMap = new HashMap<>();

  public MemoryManager() {}


}
