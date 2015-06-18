package com.machado.waco.fragments;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.machado.waco.R;
import com.machado.waco.WacoURLs;
import com.machado.waco.model.Episode;

public class EpisodeFragment extends Fragment implements OnClickListener {

  private static final String KEY_EPISODE = "episode";
  private static final Pattern FILE_MATCHER = Pattern.compile("file: \"(.+)\"");
  
  public static EpisodeFragment newInstance(Episode episode) {
    EpisodeFragment fragment = new EpisodeFragment();
    Bundle args = new Bundle();
    args.putParcelable(KEY_EPISODE, episode);
    fragment.setArguments(args);
    
    return fragment;
  }

  private Episode episode;
  private TextView title;
  private TextView desc;
  private Button player;
  private Button browser;
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    episode = getArguments().getParcelable(KEY_EPISODE);
    View rootView = inflater.inflate(R.layout.fragment_episode, container, false);
    title = (TextView) rootView.findViewById(R.id.episode_title);
    desc = (TextView) rootView.findViewById(R.id.episode_desc);
    player = (Button) rootView.findViewById(R.id.view_player);
    browser = (Button) rootView.findViewById(R.id.view_browser);
    player.setOnClickListener(this);
    browser.setOnClickListener(this);
    player.setEnabled(false);
    browser.setEnabled(false);
    new EpisodeLoadTask().execute();
    return rootView;
  }

  @Override
  public void onClick(View v) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    if (v.getId() == R.id.view_player) {
      intent.setDataAndType(Uri.parse(episode.video_urls.get(0)), "video/mp4");
      Intent chooser = Intent.createChooser(intent, "Select a video player");
      if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
        startActivity(chooser);
      }
    } else {
      intent.setData(Uri.parse(episode.video_urls.get(0)));
      startActivity(intent);
    }
  } 
  
  class EpisodeLoadTask extends AsyncTask<Void, Void, Void> {
    
    @Override
    protected Void doInBackground(Void... params) {
      episode.video_urls = new ArrayList<String>();
      try {
        Document doc = Jsoup.connect(WacoURLs.EPISODE_PREFIX_URL + episode.id).get();
        Element descriptionElement = doc.select(".ui-grid-solo > .ui-block-a p").first();
        episode.description = descriptionElement.text();
        Element players = doc.getElementById("players");
        Elements scripts = players.getElementsByTag("script");
        for (int i = 0; i < scripts.size(); i++) {
          String data = scripts.get(i).data();
          Matcher m = FILE_MATCHER.matcher(data);
          if (m.find()) {
            episode.video_urls.add(m.group(1));
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
      title.setText(episode.title);
      desc.setText(episode.description);
      if (episode.video_urls.isEmpty()) {
        return;
      }
      
      browser.setEnabled(true);
      player.setEnabled(true);
    }
  }
}
