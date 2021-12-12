//package com.example.webnovellibrary;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Folder implements Parcelable {
//    String name;
//    List<WebNovel> webNovels;
//
////    public Folder(String folderName) {
////        this.name = folderName;
////        this.webNovels = new ArrayList<>();
////    }
//    public String getFolderName() {
//        return name;
//    }
//
//    public void setFolderName(String folderName) {
//        this.name = folderName;
//    }
//
//    public List<WebNovel> getWebNovels() {
//        return webNovels;
//    }
//
//    public void setWebNovels(List<WebNovel> webNovels) {
//        this.webNovels = webNovels;
//    }
//
////    // You can include parcel data types
////    private String name;
////    private List<WebNovel> webNovels;
//
//    // This is where you write the values you want to save to the `Parcel`.
//    // The `Parcel` class has methods defined to help you save all of your values.
//    // Note that there are only methods defined for simple values, lists, and other Parcelable objects.
//    // You may need to make several classes Parcelable to send the data you want.
//    @Override
//    public void writeToParcel(Parcel out, int flags) {
//        out.writeList(webNovels);
//        out.writeString(name);
//    }
//
//    // Using the `in` variable, we can retrieve the values that
//    // we originally wrote into the `Parcel`.  This constructor is usually
//    // private so that only the `CREATOR` field can access.
//    private Folder(Parcel in) {
//        webNovels = new ArrayList<WebNovel>();
//        in.readList(webNovels, WebNovel.class.getClassLoader());
//        name = in.readString();
//    }
//
//    public Folder(String folderName){
//        this.name = folderName;
//        this.webNovels = new ArrayList<>();
//    }
//
//    // In the vast majority of cases you can simply return 0 for this.
//    // There are cases where you need to use the constant `CONTENTS_FILE_DESCRIPTOR`
//    // But this is out of scope of this tutorial
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    // After implementing the `Parcelable` interface, we need to create the
//    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
//    // Notice how it has our class specified as its type.
//    public static final Parcelable.Creator<Folder> CREATOR
//            = new Parcelable.Creator<Folder>() {
//
//        // This simply calls our new constructor (typically private) and
//        // passes along the unmarshalled `Parcel`, and then returns the new object!
//        @Override
//        public Folder createFromParcel(Parcel in) {
//            return new Folder(in);
//        }
//
//        // We just need to copy this and change the type to match our class.
//        @Override
//        public Folder[] newArray(int size) {
//            return new Folder[size];
//        }
//    };
//
//
//}