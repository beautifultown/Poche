package kaist.cs550_2016.poche;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents list of playlist entry.<br>
 * Must be created with {@link Playlist#parse(Context, Uri)}.
 */
public class Playlist {

    private int currentTrackIndex;
    private List<Entry> playlist;
    private List<Entry> randomPlaylist;
    private String path;

    private Playlist(Uri uri, List<Entry> playlist) {
        this.playlist = playlist;
        this.randomPlaylist = new ArrayList<>();

        randomPlaylist.addAll(playlist);
        Collections.shuffle(randomPlaylist);

        int lastSeperatorPos = uri.toString().lastIndexOf(File.separator) + 1;
        path = uri.toString().substring(0, lastSeperatorPos);

        if (ConfigHelper.getInstance().getPlayOrder() == ConfigHelper.PlayOrder.SHUFFLE) {
            currentTrackIndex = new Random().nextInt(playlist.size());
        }
    }

    public Uri GetCurrentTrack() {
        Debug.log("CurrentTrack", "Current track #: " + playlist.get(currentTrackIndex).trackNumber);
        return Uri.parse(path + playlist.get(currentTrackIndex).path);
    }

    public void NextTrack() {
        switch (ConfigHelper.getInstance().getPlayOrder()) {
            case ORDERED:
                currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
                break;
            case SHUFFLE:
                int currentRandomTrackIndex =
                        randomPlaylist.indexOf(playlist.get(currentTrackIndex));
                int nextRandomTrackIndex = (currentRandomTrackIndex + 1) % playlist.size();
                currentTrackIndex = playlist.indexOf(randomPlaylist.get(nextRandomTrackIndex));
                break;
        }
    }

    public void PrevTrack() {
        switch (ConfigHelper.getInstance().getPlayOrder()) {
            case ORDERED:
                currentTrackIndex = (currentTrackIndex + playlist.size() - 1) % playlist.size();
                break;
            case SHUFFLE:
                int currentRandomTrackIndex =
                        randomPlaylist.indexOf(playlist.get(currentTrackIndex));
                int nextRandomTrackIndex =
                        (currentRandomTrackIndex + playlist.size() - 1) % playlist.size();
                currentTrackIndex = playlist.indexOf(randomPlaylist.get(nextRandomTrackIndex));
                break;
        }
    }

    private static class Entry {
        public final String path;
        public final String metadata;
        public final int trackNumber;

        public Entry(String path, String metadata, int trackNumber) {
            this.path = path;
            this.metadata = metadata;
            this.trackNumber = trackNumber;
        }
    }

    /**
     * Given Uri, parses the stream and create an instance of Playlist.
     * @param context Needed to call {@link Context#getContentResolver()}.
     * @param uri Data source to obtain stream.
     * @return Parsed playlist object.
     * @throws IOException if the given uri stream is invalid M3U format.
     */
    public static Playlist parse(Context context, Uri uri) throws Exception {
        final String HEADER = "#EXTM3U";
        final String TRACKINFO_PREFIX = "#EXTINF";

        InputStream stream = context.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = reader.readLine();

        if (!line.equals(HEADER)) {
            throw new IOException("This file is not in valid M3U format.");
        }

        List<Entry> playlist = new ArrayList<>();
        int trackIndex = 0;
        String trackInfo = null;

        // Parse [(track information)path]+
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Track information or path
            if (line.startsWith(TRACKINFO_PREFIX)) {
                if (trackInfo == null) {
                    // Strip until first comma
                    line = line.substring(line.indexOf(','));
                    trackInfo = line.trim();
                }
                else {
                    throw new IOException("This file is not in valid M3U format.");
                }
            }
            else {
                String path = Uri.encode(line); // Consider unicode url (UTF-8)
                playlist.add(new Entry(path, trackInfo, ++trackIndex));
                trackInfo = null;
            }
        }

        if (playlist.isEmpty()) {
            throw new IOException("Playlist is empty.");
        }

        return new Playlist(uri, playlist);
    }

}
