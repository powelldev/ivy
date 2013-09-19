package fireminder.podcastcatcher.valueobjects;

public class Podcast {

	public Podcast(){};
	public Podcast(String title){
		this._id = null;
		this.title = title;
	}
	public Podcast(Integer _id, String title) {
		super();
		this._id = _id;
		this.title = title;
	}
	public Podcast(Integer _id, String title, String descrip, String link) {
		super();
		this._id = _id;
		this.title = title;
	}
	private Integer _id;
	private String title;
	private String description;
	private String link;
	private String imagePath;
	
	
	public String toString() { return title; }
	public Integer get_id() {
		return _id;
	}
	public void setId(Integer _id) {
		this._id = _id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	
}
