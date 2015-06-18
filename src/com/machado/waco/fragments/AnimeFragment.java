package com.machado.waco.fragments;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.machado.waco.R;
import com.machado.waco.WacoURLs;
import com.machado.waco.model.Anime;
import com.machado.waco.model.Episode;
import com.squareup.picasso.Picasso;

public class AnimeFragment extends Fragment {

  private static final String KEY_ANIME = "anime";
  
  public static AnimeFragment newInstance(Anime anime) {
    AnimeFragment fragment = new AnimeFragment();
    
    Bundle args = new Bundle();
    args.putParcelable(KEY_ANIME, anime);
    fragment.setArguments(args);
    
    return fragment;
  }

  private List<Episode> episodes;
  private ListView episodeList;
  private Anime anime;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    anime = getArguments().getParcelable(KEY_ANIME);
    View rootView = inflater.inflate(R.layout.fragment_anime, container, false);
    episodeList = (ListView) rootView.findViewById(R.id.episode_list);
    episodeList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Episode episode = (Episode) parent.getItemAtPosition(position);
        if (episode == null) {
          return;
        }
        getFragmentManager()
          .beginTransaction()
          .hide(AnimeFragment.this)
          .add(R.id.container, EpisodeFragment.newInstance(episode), episode.id)
          .addToBackStack(null)
          .commit();
      }
    });
    new AnimeLoadTask().execute();
    return rootView;
  }
  
  class AnimeLoadTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
      episodes = new ArrayList<Episode>();
//      if (anime.isComplete()) {
//        // TODO this triggers npe bcuz episodes is null/empty
//        return null;
//      }
      anime.episodes = new ArrayList<String>();
      try {
        Document animePage = Jsoup.connect(WacoURLs.ANIME_PREFIX_URL + anime.id).get();
        anime.description = animePage.getElementById("category_description").getElementsByTag("p").first().text();
        Elements episodeLinks = animePage.getElementsByTag("ul").get(1).getElementsByTag("a");
        for (int i = 0; i < episodeLinks.size(); i++) {
          Element episodeLink = episodeLinks.get(i);
          Episode episode = new Episode();
          episode.anime_id = anime.id;
          episode.id = episodeLink.attr("href").substring(1);
          episode.title = episodeLink.text();
          episodes.add(episode);
          anime.episodes.add(episode.id);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      if (episodes.isEmpty()) {
        return;
      }
      EpisodeAdapter adapter = new EpisodeAdapter(getActivity(), episodes);
      episodeList.setAdapter(adapter);
    }
  }
  
  class EpisodeAdapter extends ArrayAdapter<Episode> {

    private static final String TAG_EPISODE_HEADER = "com.machado.waco.EPISODE_HEADER";
    
    public EpisodeAdapter(Context context, List<Episode> objects) {
      super(context, 0, objects);
    }

    @Override
    public int getCount() {
      return super.getCount() + 1;
    }

    @Override
    public Episode getItem(int position) {
      if (position == 0) {
        return null;
      } else {
        return super.getItem(position - 1);
      }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (position == 0) {
        if (convertView != null && convertView.getTag().equals(TAG_EPISODE_HEADER)) {
          return convertView;
        }
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.header_anime, parent, false);
        ImageView img = (ImageView) convertView.findViewById(R.id.anime_img);
        TextView title = (TextView) convertView.findViewById(R.id.anime_title);
        TextView desc = (TextView) convertView.findViewById(R.id.anime_desc);
        
        title.setText(anime.title);
        desc.setText(anime.description);
        Picasso.with(getContext()).load(anime.img_url).into(img);
        
        convertView.setTag(TAG_EPISODE_HEADER); 
      } else {
        Episode episode = getItem(position);
        if (convertView == null || convertView.getTag().equals(TAG_EPISODE_HEADER)) {
          convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        } else if (convertView.getTag().equals(episode.id)) {
          return convertView;
        }
        
        convertView.setTag(episode.id);
        TextView title = (TextView) convertView.findViewById(android.R.id.text1);
        title.setText(episode.title);
      }
      return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public boolean isEnabled(int position) {
      return position != 0;
    }
    
  }
}
