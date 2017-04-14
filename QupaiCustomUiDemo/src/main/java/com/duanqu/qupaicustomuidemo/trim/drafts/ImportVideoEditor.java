package com.duanqu.qupaicustomuidemo.trim.drafts;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.duanqu.qupai.project.Project;
import com.duanqu.qupai.widget.android.widget.HAdapterView;
import com.duanqu.qupai.widget.android.widget.HListView;
import com.duanqu.qupaicustomuidemo.R;

import java.io.File;
import java.util.ArrayList;

public class ImportVideoEditor extends ImportEditor {

    private String[] mediaColumns = new String[]{
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
    };

    private String[] thumbColumns = new String[]{
            MediaStore.Video.Thumbnails.DATA,
            MediaStore.Video.Thumbnails.VIDEO_ID
    };

    public ImportVideoEditor(View root) {
        super(root.getContext());
        init(root);
    }

    private void init(View root) {
        videoList = (HListView) root.findViewById(R.id.hlist_view);

        View headView = LayoutInflater.from(mContext).inflate(
                R.layout.item_header_qupai_import, videoList, false);
        videoList.addHeaderView(headView, null, false);
        headView.setOnClickListener(_albumButtonOnClickListener);
        dataList = new ArrayList<VideoInfoBean>();

        _adapter = new VideoSortAdapter(dataList);
        videoList.setAdapter(_adapter);
        TextView empty_view =(TextView)root.findViewById(R.id.empty_view);
        videoList.setEmptyView(empty_view);

        videoList.setOnItemClickListener(new HAdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(HAdapterView<?> parent, View view,
                                    int position, long id) {
                currentPath = _adapter.getItem(position).getFilePath();
                setCurrentPath(currentPath);
                currentDuation = _adapter.getItem(position).getDuration();
                setCurrentDuration(currentDuation);

                if (_Listener != null) {
                    _Listener.onPlayCurrent(currentPath);
                }
            }
        });
    }

    private final OnClickListener _albumButtonOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (_Listener != null) {
                _Listener.onPlayCurrent(null);
            }
        }
    };

    @Override
    protected String generateSelection() {
        return String.format("%1$s IN (?, ?, ?) AND %2$s >= %3$d AND %2$s < %4$d",
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION,
                0,
                600500);


    }

    @Override
    protected String[] fillSelectionArgs() {
        return new String[]{
                "video/mp4",
                "video/ext-mp4", /* MEIZU 5.0 */
                "video/3gpp",
        };
    }

    @Override
    protected Uri generateQueryUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String[] generateProjection() {
        return mediaColumns;
    }

    @Override
    protected String sortOrder() {
        return MediaStore.Video.Media.DATE_MODIFIED + " DESC";
    }

    @Override
    protected String generateThumbnailSelection() {
        return MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
    }

    @Override
    protected Uri generateThumbnailQueryUri() {
        return MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String[] generateThumbnailProjection() {
        return thumbColumns;
    }

    @Override
    protected String getThumbnailPathColumn() {
        return MediaStore.Video.Thumbnails.DATA;
    }

    @Override
    protected int getImportType() {
        return Project.TYPE_VIDEO;
    }

    int col_duration;
    int col_mine_type;
    int col_data;
    int col_title;
    int col_id;
    int col_date_added;
    @Override
    protected void initCursorColume(Cursor cursor) throws IllegalArgumentException {
        col_duration = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
        col_mine_type = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
        col_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        col_title = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
        col_id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        col_date_added = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
    }

    @Override
    protected VideoInfoBean fillInfoToBeanFromCursor(Cursor cursor) {
        String filePath = cursor.getString(col_data);
        if (!new File(filePath).exists()) {
            return null;
        }
        VideoInfoBean videoInfo = new VideoInfoBean();

        int duration = cursor.getInt(col_duration);
        String mimeType = cursor.getString(col_mine_type);
        String title = cursor.getString(col_title);
        videoInfo.setFilePath(filePath);
        videoInfo.setMimeType(mimeType);
        videoInfo.setDuration(duration);
        videoInfo.setTitle(title);

        int id = cursor.getInt(col_id);
        videoInfo.setOrigId(id);

        long addTime = cursor.getLong(col_date_added);
        videoInfo.setAddTime(addTime);

        return videoInfo;
    }

}
