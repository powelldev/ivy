package com.fireminder.podcastcatcher.models;

public class Podcast {
    public String podcastId;
    public String title;
    public String description;
    public String feed;
    public String imagePath;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(podcastId)
                .append("\n")
                .append(title)
                .append("\n")
                .append(description)
                .append("\n")
                .append(feed)
                .append("\n")
                .append(imagePath);
        return sb.toString();
    }


}
