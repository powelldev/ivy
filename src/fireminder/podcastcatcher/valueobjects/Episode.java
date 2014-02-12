package fireminder.podcastcatcher.valueobjects;

/***
 * A representation of an episode of a podcast.
 */
public class Episode {
    public Episode(Long _id, Long podcast_id, String title,
            String description, String url, long pubDate, String mp3) {
        super();
        this._id = _id;
        this.podcast_id = podcast_id;
        this.title = title;
        this.description = description;
        this.url = url;
        this.pubDate = pubDate;
        this.mp3 = mp3;
    }
    
    public Episode(Long _id, Long podcast_id, String title) {
        super();
        this._id = _id;
        this.podcast_id = podcast_id;
        this.title = title;
    }

    // id assigned by sqlite
    private Long _id;
    // sqlite id of the podcast this episode belongs to
    private Long podcast_id;
    private String title;
    private String description;
    private String url;
    private long pubDate;
    //mp3 location on disk of downloaded episode
    private String mp3;
    private String mDuration;
    
    @Override
    public String toString(){
        String info = "";
        info += "_id: " + _id + "\n"; 
        info += "podcast_id: " + podcast_id + "\n"; 
        info += "title: " + title + "\n"; 
        info += "description: " + description + "\n"; 
        info += "url: " + url + "\n"; 
        info += "pubDate: " + pubDate + "\n"; 
        //No need to catch exception - this just checks if it has been downlaoded.
        try { info += "mp3: " + mp3 + "\n"; } catch (Exception e) {} 
        return info;
    }

    public Episode(){
        this.title = "";
        this.description = "";
        this.url = "";
        this.pubDate = 0;
        this.mp3 = "";
    }
    
    
    public Long get_id() {
        return _id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public long getPubDate() {
        return pubDate;
    }

    public String getMp3() {
        return mp3;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPubDate(long pubDate) {
        this.pubDate = pubDate;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }

    public Long getPodcast_id() {
        return podcast_id;
    }

    public void setPodcast_id(Long podcast_id) {
        this.podcast_id = podcast_id;
    }
}
