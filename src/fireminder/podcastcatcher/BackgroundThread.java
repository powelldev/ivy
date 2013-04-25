package fireminder.podcastcatcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.db.EpisodeSqlHelper;
import fireminder.podcastcatcher.db.Podcast;
import fireminder.podcastcatcher.db.PodcastDAO;

public class BackgroundThread {
	
	Context context;
	
	public BackgroundThread(Context context){
		this.context = context;
	}
	
	public void getEpisodesFromBackgroundThread(String url, long id){
		new ParseXmlForEpisodes().execute(new String[] {url, ""+id});
	}
	
	public void getPodcastImageFromBackgroundThread(String url, long id){
		new ParseXmlForImage().execute(new String[] { url, ""+id});
	}
	private class ParseXmlForImage extends AsyncTask<String, Void, ByteArrayBuffer>{
		String idForQuery;
		PodcastDAO podcastDao;
		@Override
		protected ByteArrayBuffer doInBackground(String... urls) {
			URL url;
			idForQuery = urls[1];
			podcastDao = new PodcastDAO(context);
			BufferedReader reader = null;
			ByteArrayBuffer baf = null;
			String imagelink = null;
			try {
				url = new URL(urls[0]);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				InputStream is = con.getInputStream();
				reader = new BufferedReader( new InputStreamReader(is));
				imagelink = RssParser.parsePodcastImageFromXml(reader);
				
				//Download image
				URL imageurl = new URL(imagelink);
				HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
				InputStream istream = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(istream, 128);
				baf = new ByteArrayBuffer(128);
				int current = 0;
				while ((current = bis.read()) != -1){
					baf.append((byte) current);
				}
				} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			return baf;
		}
		
		@Override
		protected void onPostExecute(ByteArrayBuffer result){
			podcastDao.open();	
			if(result != null){
				Log.d("Add this to db: ", result + " AT " + idForQuery);
				Podcast podcast = podcastDao.getPodcast(Long.parseLong(idForQuery));
				podcast.setImagelink(result.toByteArray());
				podcastDao.updatePodcastImagelink(podcast);
//				BackgroundThread bt = new BackgroundThread(getActivity());
//				bt.getEpisodesFromBackgroundThread(podcast.getLink(), podcast.get_id());
			//	new ParseXmlForEpisodes().execute(new String[] {podcast.getLink(), String.valueOf(podcast.get_id())});
			} else {
				Podcast podcast = podcastDao.getPodcast(Long.parseLong(idForQuery));
				Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				podcast.setImagelink(byteArray);
				podcastDao.updatePodcastImagelink(podcast);
			}

			
		}
		
	}
	private class ParseXmlForEpisodes extends AsyncTask<String, Void, Void>{
		long idForQuery;
		EpisodeDAO edao;
		
		@Override
		protected Void doInBackground(String... urls) {
			URL url;
			idForQuery = Long.parseLong(urls[1]);
			BufferedReader reader = null;
			List<ContentValues> episodes = null;
			try {
				url = new URL(urls[0]);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				InputStream is = con.getInputStream();
				reader = new BufferedReader( new InputStreamReader(is));
				Log.d("EpisodeParsing", "here: " + urls[0] + " " + urls[1]);
				episodes = RssParser.parseEpisodesFromXml(reader, idForQuery);
				edao = new EpisodeDAO(context);
				edao.open();
				for(ContentValues e : episodes){
					Log.d("Inserting: ", e.getAsString(EpisodeSqlHelper.COLUMN_TITLE));
					edao.insertEpisode(e);
				}
				edao.close();
				
				return null;
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		
	}
		
	}
	
}
