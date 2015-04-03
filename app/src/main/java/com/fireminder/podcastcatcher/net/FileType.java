package com.fireminder.podcastcatcher.net;

public enum FileType {
  MP3("mp3");

  private String type;

  private FileType(String type) {
    this.type = type;
  }

  public String getTypeName() {
    return type;
  }


}
