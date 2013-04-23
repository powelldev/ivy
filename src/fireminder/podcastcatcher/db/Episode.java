package fireminder.podcastcatcher.db;

public class Episode {



	public Episode(Integer _id, Integer podcast_id, String title,
			String description, String url, long pubDate, byte[] mp3) {
		super();
		this._id = _id;
		this.podcast_id = podcast_id;
		this.title = title;
		this.description = description;
		this.url = url;
		this.pubDate = pubDate;
		this.mp3 = mp3;
	}
	
	public Episode(Integer _id, Integer podcast_id, String title) {
		super();
		this._id = _id;
		this.podcast_id = podcast_id;
		this.title = title;
	}

	private Integer _id;
	private Integer podcast_id;
	private String title;
	private String description;
	private String url;
	private long pubDate;
	private byte[] mp3;
	
	public Episode(){
		this.title = "";
		this.description = "";
		this.url = "";
		this.pubDate = 0;
		this.mp3 = null;
	}
	
	
	public Integer get_id() {
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

	public byte[] getMp3() {
		return mp3;
	}

	public void set_id(Integer _id) {
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

	public void setMp3(byte[] mp3) {
		this.mp3 = mp3;
	}

	public Integer getPodcast_id() {
		return podcast_id;
	}

	public void setPodcast_id(Integer podcast_id) {
		this.podcast_id = podcast_id;
	}
}
