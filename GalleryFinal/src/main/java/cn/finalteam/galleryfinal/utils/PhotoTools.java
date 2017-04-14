/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.finalteam.galleryfinal.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.R;
import cn.finalteam.galleryfinal.model.PhotoFolderInfo;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desction:
 * Author:pengjianbo
 * Date:15/10/10 下午4:26
 */
public class PhotoTools {


    /**
     * 获取所有视频
     * @param context
     * @return
     */
    public static List<PhotoFolderInfo> getAllVideoFolder(Context context, List<PhotoInfo> selectPhotoMap) {
        List<PhotoFolderInfo> allFolderList = new ArrayList<>();
        final String[] projectionVideo = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
        };
        String selection = String.format("%1$s IN (?, ?, ?) AND %2$s >= %3$d AND %2$s < %4$d",
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION,
                2000,
                600500);
        final ArrayList<PhotoFolderInfo> allPhotoFolderList = new ArrayList<>();
        HashMap<Integer, PhotoFolderInfo> bucketMap = new HashMap<>();
        Cursor cursor = null;
        //所有图片
        PhotoFolderInfo allPhotoFolderInfo = new PhotoFolderInfo();
        allPhotoFolderInfo.setFolderId(0);
        allPhotoFolderInfo.setFolderName(context.getResources().getString(R.string.all_video));
        allPhotoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
        allPhotoFolderList.add(0, allPhotoFolderInfo);
        List<String> selectedList = GalleryFinal.getFunctionConfig().getSelectedList();
        List<String> filterList = GalleryFinal.getFunctionConfig().getFilterList();
        try {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projectionVideo,
                    selection, new String[]{
                            "video/mp4",
                            "video/ext-mp4", /* MEIZU 5.0 */
                            "video/3gpp",
                    },MediaStore.Video.Media.DATE_MODIFIED + " DESC");

            if (cursor != null) {
                int bucketNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                while (cursor.moveToNext()) {
                    int bucketId = cursor.getInt(bucketIdColumn);
                    String bucketName = cursor.getString(bucketNameColumn);
                    final int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                    final int imageIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                    int imageColumnWidth = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH);
                    int imageColumnHeight = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT);
                    int thumbImageColumn = cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA);
                    int  durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                    final int imageId = cursor.getInt(imageIdColumn);
                    final String path = cursor.getString(dataColumn);
                    final int imageWidth = cursor.getInt(imageColumnWidth);
                    final int imageHeight = cursor.getInt(imageColumnHeight);
                    final String thumb = cursor.getString(thumbImageColumn);
                    final long duration = cursor.getLong(durationColumn);
                    File file = new File(path);
                    if( imageWidth != 0 || imageHeight != 0)
                        if ( (filterList == null || !filterList.contains(path)) && file.exists() && file.length() > 0 ) {
                            final PhotoInfo photoInfo = new PhotoInfo();
                            photoInfo.setPhotoId(imageId);
                            photoInfo.setPhotoPath(thumb);
                            photoInfo.setVideoPath(path);
                            photoInfo.setWidth(imageWidth);
                            photoInfo.setHeight(imageHeight);
                            photoInfo.setDuration(duration);
                            photoInfo.setType(PhotoInfo.Media.VIDEO);
                            //photoInfo.setThumbPath(thumb);
                            if (allPhotoFolderInfo.getCoverPhoto() == null) {
                                allPhotoFolderInfo.setCoverPhoto(photoInfo);
                            }
                            //添加到所有图片
                            allPhotoFolderInfo.getPhotoList().add(photoInfo);

                            //通过bucketId获取文件夹
                            PhotoFolderInfo photoFolderInfo = bucketMap.get(bucketId);

                            if (photoFolderInfo == null) {
                                photoFolderInfo = new PhotoFolderInfo();
                                photoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
                                photoFolderInfo.setFolderId(bucketId);
                                photoFolderInfo.setFolderName(bucketName);
                                photoFolderInfo.setCoverPhoto(photoInfo);
                                bucketMap.put(bucketId, photoFolderInfo);
                                allPhotoFolderList.add(photoFolderInfo);
                            }
                            photoFolderInfo.getPhotoList().add(photoInfo);

                            if (selectedList != null && selectedList.size() > 0 && selectedList.contains(path)) {
                                selectPhotoMap.add(photoInfo);
                            }
                        }
                }
            }
        } catch (Exception ex) {
            ILogger.e(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        allFolderList.addAll(allPhotoFolderList);
        if (selectedList != null) {
            selectedList.clear();
        }
        return allFolderList;
    }

    /**
     * 获取所有图片
     * @param context
     * @return
     */
    public static List<PhotoFolderInfo> getAllPhotoFolder(Context context, List<PhotoInfo> selectPhotoMap) {
        List<PhotoFolderInfo> allFolderList = new ArrayList<>();
        final String[] projectionPhotos = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
        };
        final ArrayList<PhotoFolderInfo> allPhotoFolderList = new ArrayList<>();
        HashMap<Integer, PhotoFolderInfo> bucketMap = new HashMap<>();
        Cursor cursor = null;
        //所有图片
        PhotoFolderInfo allPhotoFolderInfo = new PhotoFolderInfo();
        allPhotoFolderInfo.setFolderId(0);
        allPhotoFolderInfo.setFolderName(context.getResources().getString(R.string.all_photo));
        allPhotoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
        allPhotoFolderList.add(0, allPhotoFolderInfo);
        List<String> selectedList = GalleryFinal.getFunctionConfig().getSelectedList();
        List<String> filterList = GalleryFinal.getFunctionConfig().getFilterList();
        try {
            cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos,  MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=? or "+MediaStore.Images.Media.MIME_TYPE +"=?"
                    , new String[] {"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            if (cursor != null) {
                int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                while (cursor.moveToNext()) {
                    int bucketId = cursor.getInt(bucketIdColumn);
                    String bucketName = cursor.getString(bucketNameColumn);
                    final int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    final int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int imageColumnWidth = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
                    int imageColumnHeight = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);
//                    int thumbImageColumn = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                    final int imageId = cursor.getInt(imageIdColumn);
                    final String path = cursor.getString(dataColumn);
                    final int imageWidth = cursor.getInt(imageColumnWidth);
                    final int imageHeight = cursor.getInt(imageColumnHeight);
                    //final String thumb = cursor.getString(thumbImageColumn);
                    File file = new File(path);
                    if( imageWidth != 0 || imageHeight != 0)
                    if ( (filterList == null || !filterList.contains(path)) && file.exists() && file.length() > 0 ) {
                        final PhotoInfo photoInfo = new PhotoInfo();
                        photoInfo.setPhotoId(imageId);
                        photoInfo.setPhotoPath(path);
                        photoInfo.setWidth(imageWidth);
                        photoInfo.setHeight(imageHeight);
                        //photoInfo.setThumbPath(thumb);
                        photoInfo.setType(PhotoInfo.Media.PHOTO);
                        if (allPhotoFolderInfo.getCoverPhoto() == null) {
                            allPhotoFolderInfo.setCoverPhoto(photoInfo);
                        }
                        //添加到所有图片
                        allPhotoFolderInfo.getPhotoList().add(photoInfo);

                        //通过bucketId获取文件夹
                        PhotoFolderInfo photoFolderInfo = bucketMap.get(bucketId);

                        if (photoFolderInfo == null) {
                            photoFolderInfo = new PhotoFolderInfo();
                            photoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
                            photoFolderInfo.setFolderId(bucketId);
                            photoFolderInfo.setFolderName(bucketName);
                            photoFolderInfo.setCoverPhoto(photoInfo);
                            bucketMap.put(bucketId, photoFolderInfo);
                            allPhotoFolderList.add(photoFolderInfo);
                        }
                        photoFolderInfo.getPhotoList().add(photoInfo);

                        if (selectedList != null && selectedList.size() > 0 && selectedList.contains(path)) {
                            selectPhotoMap.add(photoInfo);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ILogger.e(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        allFolderList.addAll(allPhotoFolderList);
        if (selectedList != null) {
            selectedList.clear();
        }
        return allFolderList;
    }
}
