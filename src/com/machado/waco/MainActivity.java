package com.machado.waco;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.machado.waco.fragments.SearchFragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {

  private Pattern FILE_MATCHER = Pattern.compile("file: \"(.+)\"");
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
      getFragmentManager()
          .beginTransaction()
          .add(R.id.container, new SearchFragment(), SearchFragment.TAG)
          .commit();
    }
//    parseUrl();
  }

  private void parseUrl() {
    new AsyncTask<Void, Void, Void>() {
      String url = null;
      @Override
      protected Void doInBackground(Void... params) {
        Document doc;
        try {
          doc = Jsoup.connect("http://m.watchcartoononline.com/thunderbirds-are-go-episode-1-2-ring-of-fire").get();
          Element players = doc.getElementById("players");
          Elements scripts = players.getElementsByTag("script");
          for (int i = 0; i < scripts.size(); i++) {
            String data = scripts.get(i).data();
            Matcher m = FILE_MATCHER.matcher(data);
            if (m.find()) {
              url = m.group(1);
              break;
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }
      @Override
      protected void onPostExecute(Void result) {
        if (url == null) {
          return;
        }
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
//        intent.setDataAndType(Uri.parse(url), "video/mp4");
        startActivity(intent);
      }
    }.execute();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      return rootView;
    }
  }
}
