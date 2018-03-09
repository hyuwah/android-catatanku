package io.github.hyuwah.catatanku.storage.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by hyuwah on 07/02/18.
 */

@Entity
public class Note {

  @NonNull
  @PrimaryKey(autoGenerate = true)
  private int id;

  @ColumnInfo(name = "title")
  private String title;

  @ColumnInfo(name = "body")
  private String body;

  @ColumnInfo(name = "created_time")
  private long createdTime;

  @ColumnInfo(name = "updated_time")
  private long updatedTime;

  public Note(){

  }

  @NonNull
  public int getId() {
    return id;
  }

  public void setId(@NonNull int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
  }

  public long getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(long updatedTime) {
    this.updatedTime = updatedTime;
  }
}
