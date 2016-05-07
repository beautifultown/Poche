package kaist.cs550_2016.poche;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class PlaylistScanAsyncTask extends AsyncTaskLoader<List<File>> {

    private List<File> result;
    private PlaylistFilenameFilter filenameFilter;

    public PlaylistScanAsyncTask(Context context) {
        super(context);
        filenameFilter = new PlaylistFilenameFilter();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (result != null) deliverResult(result);

        result = new ArrayList<>();
        forceLoad();
    }

    @Override
    public List<File> loadInBackground() {
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        File rootDirectory = new File(rootPath);
        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) return null;

        scan(rootDirectory);
        return result;
    }

    private void scan(File directory) {
        File[] files = directory.listFiles(filenameFilter);
        for (File file : files) {
            if (file.isDirectory()) scan(file);
            else result.add(file);
        }
    }

    private static class PlaylistFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            if (filename.endsWith(".m3u") || filename.endsWith(".m3u8")
                    || filename.endsWith(".M3U") || filename.endsWith(".M3U8")) {
                return true;
            }
            else if (new File(dir.getAbsolutePath() + "/" + filename).isDirectory()) {
                return true;
            }

            return false;
        }
    }
}
