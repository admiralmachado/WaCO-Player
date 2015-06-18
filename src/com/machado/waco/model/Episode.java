package com.machado.waco.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Episode implements Parcelable {

  public String id;
  public String title;
  public String anime_id;

  public String description = null;
  public List<String> video_urls = null;

  public boolean isComplete() {
    return video_urls != null && description != null;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(title);
    dest.writeString(anime_id);
    dest.writeInt(isComplete() ? 1 : 0);
    if (isComplete()) {
      dest.writeString(description);
      dest.writeStringList(video_urls);
    }
  }
  
  public static final Parcelable.Creator<Episode> CREATOR = new Parcelable.Creator<Episode>() {

    @Override
    public Episode createFromParcel(Parcel source) {
      Episode episode = new Episode();
      episode.id = source.readString();
      episode.title = source.readString();
      episode.anime_id = source.readString();
      if (source.readInt() == 1) {
        episode.description = source.readString();
        episode.video_urls = new ArrayList<String>();
        source.readStringList(episode.video_urls);
      }
      return episode;
    }

    @Override
    public Episode[] newArray(int size) {
      return new Episode[size];
    }
  };
}
