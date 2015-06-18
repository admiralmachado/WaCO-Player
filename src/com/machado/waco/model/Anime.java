package com.machado.waco.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Anime implements Parcelable {
  
  public String id;
  public String title;
  public String img_url;
  
  public String description;
  public List<String> episodes = null;

  public boolean isComplete() {
    return episodes != null && description != null;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(title);
    dest.writeString(img_url);
    dest.writeInt(isComplete() ? 1 : 0);
    if (isComplete()) {
      dest.writeString(description);
      dest.writeStringList(episodes);
    }
  }
  
  public static final Parcelable.Creator<Anime> CREATOR = new Parcelable.Creator<Anime>() {
    public Anime createFromParcel(Parcel in) {
      Anime anime = new Anime();
      anime.id = in.readString();
      anime.title = in.readString();
      anime.img_url = in.readString();
      if (in.readInt() == 1) {
        anime.description = in.readString();
        anime.episodes = new ArrayList<String>();
        in.readStringList(anime.episodes);
      }
      return anime;
    }

    @Override
    public Anime[] newArray(int size) {
      return new Anime[size];
    }
  };
}
