package fireminder.podcastcatcher.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.ocpsoft.prettytime.PrettyTime;

import fireminder.podcastcatcher.valueobjects.Episode;

class EpisodeReadableInfo {
    Episode episode;

    public EpisodeReadableInfo(Episode episode) {
        this.episode = episode;
    }

    public boolean isDownloaded() {
        String uri = episode.getMp3();
        if (uri != null) {
            if (new File(uri).exists()) {
                return true;
            }
            return false;
        }
        return false;
    }

    public long getId() {
        return episode.get_id();
    }

    public String getTitle() {
        return episode.getTitle();
    }

    public String getDate() {
        long milliseconds = episode.getPubDate();
        long diff = System.currentTimeMillis() - milliseconds;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        if (diff > 2629740000L) {
            Date date = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
            return sdf.format(date);
        } else {
            PrettyTime p = new PrettyTime();
            return p.format(calendar);
        }
    }

    public String getDescrip(int limit) {
        int descripSize = limit;
        String descrip = episode.getDescription();
        if (descrip.length() > descripSize) {
            descrip = descrip.substring(0, descripSize - 3) + "...";
        }
        return descrip;
    }

}