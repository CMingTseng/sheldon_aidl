package com.example.remoteserver;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Entity implements Parcelable {
    private int age;
    private String name;
    private final String TAG = "Engity";

    public Entity() {
    }

    public Entity(int age, String name) {
        Log.i(TAG,"new age="+age+",name="+name);
        this.age = age;
        this.name = name;
    }

    protected Entity(Parcel in) {
        age = in.readInt();
        name = in.readString();
    }

    public static final Creator<Entity> CREATOR = new Creator<Entity>() {
        @Override
        public Entity createFromParcel(Parcel in) {
            return new Entity(in);
        }

        @Override
        public Entity[] newArray(int size) {
            return new Entity[size];
        }
    };

    public int getAge() {
        Log.i(TAG,"get age="+age);
        return this.age;
    }

    public void setAge(int age) {
        Log.i(TAG,"set age="+age);
        this.age = age;
    }

    public String getName() {
        Log.i(TAG,"get name="+name);
        return this.name;
    }

    public void setName(String name) {
        Log.i(TAG,"set name="+name);
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(age);
        dest.writeString(name);
    }

    @Override
    public String toString() {
        return String.format("age=%s, name=%s", getAge(), getName());
    }
}
