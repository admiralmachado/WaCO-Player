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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.machado.waco.R;
import com.machado.waco.WacoURLs;
import com.machado.waco.model.Anime;
import com.squareup.picasso.Picasso;

public class SearchFragment extends Fragment {

  public static final String TAG = "search_fragment";
  
  private EditText queryEditText;
  private ListView resultsList;
  private Button submitButton;
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    System.out.println("starting oncreateview in searchfrag");
    if (submitButton == null) {
      System.out.println("button is null");
    }
    System.out.println("savedInstState is " + (savedInstanceState == null ? "null" : "not null"));
    if (getView() == null) {
      System.out.println("rootview is null");
    }
    View rootView = inflater.inflate(R.layout.fragment_search, container, false);
    queryEditText = (EditText) rootView.findViewById(R.id.query);
    queryEditText.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
    queryEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        startSearch();
        return true;
      }
    });
    resultsList = (ListView) rootView.findViewById(R.id.results);
    resultsList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Anime anime = (Anime) parent.getItemAtPosition(position);
        getFragmentManager()
            .beginTransaction()
            .hide(SearchFragment.this)
            .add(R.id.container, AnimeFragment.newInstance(anime), anime.id)
            .addToBackStack(null)
            .commit();
      }
    });
    
    submitButton = (Button) rootView.findViewById(R.id.submit);
    submitButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startSearch();
      }
    });
    return rootView;
  }

  private void startSearch() {
    queryEditText.setEnabled(false);
    submitButton.setEnabled(false);
    new SearchTask().execute();
  }
  
  class SearchTask extends AsyncTask<Void, Void, Void> {
    
    private List<Anime> animes;
    @Override
    protected Void doInBackground(Void... params) {
      try {
        Document searchResponse = Jsoup.connect(WacoURLs.ANIME_SEARCH_URL)
            .userAgent("Mozilla")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .data("queryString", queryEditText.getText().toString())
            .data("singleValues", "2")
            .post();
        Elements results = searchResponse.getElementsByTag("a");
        animes = new ArrayList<Anime>();
        for (int i = 0; i < results.size(); i++) {
          Element result = results.get(i);
          Anime anime = new Anime();
          String href = result.attr("href");
          if (href == null || href.isEmpty()) {
            continue;
          }
          anime.id = href.substring(WacoURLs.ANIME_PREFIX_URL.length());
          anime.img_url = result.getElementsByTag("img").first().attr("src");
          anime.title = result.getElementsByTag("span").first().text();
          animes.add(anime);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }
    @Override
    protected void onPostExecute(Void result) {
      ResultsAdapter adapter = new ResultsAdapter(getActivity(), animes);
      resultsList.setAdapter(adapter);
      submitButton.setEnabled(true);
      queryEditText.setEnabled(true);
    }
    
  }
  
  class ResultsAdapter extends ArrayAdapter<Anime> {

    public ResultsAdapter(Context context, List<Anime> animes) {
      super(context, 0, animes.toArray(new Anime[0]));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      Anime anime = getItem(position);
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.series_result, parent, false);
      } else if (convertView.getTag().equals(anime.id)) {
        return convertView;
      }
      
      convertView.setTag(anime.id);
      ImageView img = (ImageView) convertView.findViewById(R.id.series_img);
      TextView title = (TextView) convertView.findViewById(R.id.series_title);
      
      title.setText(anime.title);
      Picasso.with(getContext()).load(anime.img_url).into(img);
      return convertView;
    }
  }
}
