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

// For parse method part:
/*
 * Copyright 2014 William Seemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        Log.w("CurrentTrack", "Current track #: " + playlist.get(currentTrackIndex).trackNumber);
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

    // This m3u parser is partially from:
    // https://github.com/wseemann/JavaPlaylistParser/blob/master/src/wseemann/media/jplaylistparser/parser/m3u/M3UPlaylistParser.java
    public static Playlist parse(Context context, Uri uri) throws IOException {
        final String EXTENDED_INFO_TAG = "#EXTM3U";
        final String RECORD_TAG = "^[#][E|e][X|x][T|t][I|i][N|n][F|f].*";

        InputStream stream = context.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String line;
        String metadata = null, path = null;
        int trackNumber = 0;
        List<Entry> playlist = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            if (!(line.equalsIgnoreCase(EXTENDED_INFO_TAG) || line.trim().equals(""))) {
                if (line.matches(RECORD_TAG)) {
                    metadata = line.replaceAll("^(.*?),", "");
                } else {
                    path = Uri.encode(line.trim());
                    playlist.add(new Entry(path, metadata, ++trackNumber));
                }
            }
        }

        return new Playlist(uri, playlist);
    }

}