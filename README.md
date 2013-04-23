Podcast Catcher

MainActivity.java
	presents PodcastFragment
	will present the Playlist and Player Fragments when implemented

PodcastFragment.java
	coordinates subscription to a podcast
	lists subscribed podcasts

Podcast
	class file for a podcast

PodcastSqlHelper
	contains constants for db references
	values for strings, creation, etc

PodcastDAO
	coordinates access to the database

Episode/EpisodeDAO/EpisodeSqlHelper
	same as Podcast classes
	coordinate episode database access

