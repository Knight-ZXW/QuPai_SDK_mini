package com.duanqu.qupaicustomuidemo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;


import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.duanqu.qupaicustomuidemo.editor.download.AssetDownloadManagerImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static android.R.id.list;

/**
 * 文件操作工具包
 * @author yds
 *
 */
public class FileUtil {

	public final static String DEFAULT_SDPATH=Environment.getExternalStorageDirectory().getPath();

	public final static String DEFAULT_SAVE_IMAGE_PATH = Environment.getExternalStorageDirectory()+
			                        File.separator+ "qupai"+ File.separator+"image"+ File.separator;

	public final static String DEFAULT_SAVE_VIDEO_PATH = Environment.getExternalStorageDirectory()+
                                    File.separator+ "qupai"+ File.separator+"video"+ File.separator;

	public static final String MUSIC_PATH = Environment.getExternalStorageDirectory()
			+ File.separator + "qupai/" + "Shop_Music/";

	public static final String PREFIX = "default";

	private static final String LOG = "qupai" + File.separator + "log";
//	private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory()
//			+ File.separator + "qupai/" + "Music/";

	/**
	 * 获取文件扩展名
	 * @param fileName
	 */
	public static String getFileFormat(String fileName) {
		if (TextUtils.isEmpty(fileName))
			return "";

		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

	/**
	 *
	 * 遍历文件夹，查询文件是否存在
	 **/
	public static boolean isFileExist(String dir, String file) {
		File tmpFile;
		File[] files = new File(dir).listFiles();
		if(files == null || files.length < 1) {
			return false;
		}

		for(int i = 0; i < files.length; i++) {
			tmpFile = files[i];

			if(tmpFile.getName().equals(file)) {
				return true;
			}
		}

		return false;
	}

	public static String getLogDir(){
		if(isReadWrite()){
			String path = getSdCardPath() + File.separator + LOG;
			File f = new File(path);
			if(!f.exists()){
				f.mkdirs();
			}
			return path;
		}else{
			return "";
		}
	}

	public static String getSdCardPath(){
		if(isReadWrite()){
			return DEFAULT_SDPATH;
		}else{
			return "";
		}
	}

    /**
	 * 外部存储是否可写
	 *
	 * @return
	 */
	public static boolean isReadWrite() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 外部存储是否只读
	 */
	public static boolean isReadOnly() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED_READ_ONLY);
	}

	/**
	 * 得到剩余存储空间
	 *
	 * @return
	 */
	public static long SDFreeSize() {
		if (isReadWrite() || isReadOnly()) {
			StatFs statfs = new StatFs(DEFAULT_SDPATH);

			//
			long nBlocSize = statfs.getBlockSize();
			//
			long nAvailaBlock = statfs.getAvailableBlocks();
			long nSDFreeSize = nAvailaBlock * nBlocSize;
			return nSDFreeSize;
		}
		return -1;
	}

	public static String getDEFAULT_SAVE_USERINFO_PATH(Context context) {
		return  Environment.getDataDirectory().getPath()
				+ "/data/"+context.getPackageName()+"/loginform.object";
	}


    private static File getResourcesCachePath(Context context) {
		File path = context.getExternalCacheDir();
		if(path!=null&&path.exists()){
			return path;
		}
		return null;
	}


	public static String getDEFAULT_SAVE_IMV_PATH(Context context, int id) {
		if(context == null || getResourcesCachePath(context) == null) {
			return null;
		}

		File iMVFile = new File(getResourcesCachePath(context)
				.getAbsolutePath() + "/iMV/");
		if(!iMVFile.exists()) {
			iMVFile.mkdir();
		}

		return iMVFile.getAbsolutePath() + "/" + id + ".object";
	}

	public static File getDEFAULT_SAVE_PASTER_PATH(Context context) {
        if(getResourcesCachePath(context) == null) {
            return null;
        }

        File pasterFile = new File(getResourcesCachePath(context)
                .getAbsolutePath() + "/paster");
        if(!pasterFile.exists()) {
            pasterFile.mkdir();
        }

        return pasterFile;
    }

    public static File getResourcesGuidePath(Context context) {
        File path=context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if(path!=null&&path.exists()){
            return path;
        }
        return null;
    }


	public static String getDEFAULT_GUIDE_PATH(Context context) {
		return getResourcesGuidePath(context).getAbsolutePath()+"/";
	}

    public static File getResourcesDownloadPath(Context context) {
        File path=context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if(path!=null&&path.exists()){
            return path;
        }
        return null;
    }

    public static String getDEFAULT_MUSIC_PATH(Context context) {
		return getResourcesDownloadPath(context).getAbsolutePath()+"/";
	}

	public static String getUNZIP_MUSIC_PATH(Context context) {
		return AssetDownloadManagerImpl.getResourcesUnzipPath(context).getAbsolutePath()+"/";
	}

	public static String getDEFAULT_SAVE_UNUPLOAD_PATH(Context context, long uid) {
		return  Environment.getDataDirectory().getPath()
				+ "/data/"+context.getPackageName()+"/" + uid + "ul.list";
	}

//	public static String getUNUPLOAD_VIDEO_PATH(Context context, String time) {
//		return  Environment.getExternalStorageDirectory().getPath()
//				+ "/Android/data/" + context.getPackageName() + "/" + time + "log.log";
//	}

//	public static void removeUploadLogFile(Context context, String time){
//		String path = getUNUPLOAD_VIDEO_PATH(context, time);
//		File log = new File(path);
//		if(log.exists()){
//			log.delete();
//		}
//	}

//	public static void writeUploadLog2Local(Context context, String time, String info){
//		String path = getUNUPLOAD_VIDEO_PATH(context, time);
//		File f = new File(path);
//		try {
//			if(!f.exists())
//				f.createNewFile();
//			BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
//			writer.write(info + "\n");
//			writer.flush();
//			writer.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public static boolean deleteSelectFiles(AssetInfo[] array, Context context) {
		if(array == null || array.length == 0) {
			return false;
		}
		for(AssetInfo ai : array) {
			String path = ai.getContentURIString();
			String fileName = path.substring(path.lastIndexOf("/"));
			String musicPath = getUNZIP_MUSIC_PATH(context) + fileName;
			File musicFile = new File(musicPath);
			if(musicFile.isDirectory()) {
				File[] childFiles = musicFile.listFiles();
				if(childFiles == null || childFiles.length == 0) {
					musicFile.delete();
				}else {
					deleteFile(musicFile);
					musicFile.delete();
				}

			}

			String downloadPath = getDEFAULT_MUSIC_PATH(context) + fileName + ".zip";
			File downlaodFile = new File(downloadPath);
			if(downlaodFile.exists()) {
				downlaodFile.delete();
			}
		}
		return true;
	}

    public static boolean deleteSelectFiles(List<VideoEditResources> list, Context context) {
    	if(list == null || list.size() == 0) {
    		return false;
    	}
    	for(VideoEditResources srf:list) {
    		String fileName = srf.getLocalPath().substring(srf.getLocalPath().lastIndexOf("/"));
    		String musicPath = getUNZIP_MUSIC_PATH(context) + fileName;
    		File musicFile = new File(musicPath);
    		if(musicFile.isDirectory()) {
    			File[] childFiles = musicFile.listFiles();
    			if(childFiles == null || childFiles.length == 0) {
    				musicFile.delete();
    			}else {
    				deleteFile(musicFile);
    				musicFile.delete();
    			}

    		}

    		String downloadPath = getDEFAULT_MUSIC_PATH(context) + fileName + ".zip";
    		File downlaodFile = new File(downloadPath);
    		if(downlaodFile.exists()) {
    			downlaodFile.delete();
    		}
    	}
    	return true;
    }

    private  static void deleteFile(File file) {
    	File[] delFile = file.listFiles();

    	for(File tmpFile:delFile) {
    		if(tmpFile.exists()) {
    			tmpFile.delete();
    		}
    	}
    }

	private static void copyStream(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024*16];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}

	/**
	 * 文件拷贝
	 * @param srcFileName 源文件路径
	 * @param desFileName 新文件路径
	 * @return
	 */
	public static boolean copyFile( String srcFileName, String desFileName) {
		return copyFile(new File(srcFileName), new File(desFileName));
	}

	/**
	 * 文件拷贝
	 * @return
	 */
	public static boolean copyFile( File srcFile, File desFile) {
		if(!srcFile.exists()){
			return false;
		}
        InputStream in = null;
        OutputStream out = null;
        try {
          in = new FileInputStream(srcFile);
          File parFile = desFile.getParentFile();
          if (parFile!=null && !parFile.exists()) {
        	  parFile.mkdirs();
          }
          out = new FileOutputStream(desFile);
          copyStream(in, out);
          out.flush();
          return true;
        } catch(IOException e) {
            e.printStackTrace();
        } finally{
        	if (in!=null) {
        		try { in.close(); } catch (IOException e) { e.printStackTrace();}
			}
            if (out!=null) {
            	try { out.close(); } catch (IOException e) { e.printStackTrace();}
			}
        }
        return false;
	}

	/**
	* 获取版本号
	* @return 当前应用的版本号
	*/
	public static int getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			int version = info.versionCode;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static class SimpleComparator<T> implements Comparator<T> {

		@Override
		public int compare(T lhs, T rhs) {
			if(lhs instanceof VideoEditResources){
				return compare(((VideoEditResources)lhs).getName(), ((VideoEditResources)rhs).getName());
			}
			return 0;

		}

	      public int compare(String o1, String o2) {
	          String s1 = o1;
	          String s2 = o2;
	          int len1 = s1.length();
	          int len2 = s2.length();
	          int n = Math.min(len1, len2);
	          char v1[] = s1.toCharArray();
	          char v2[] = s2.toCharArray();
	          int pos = 0;

	          while (n-- != 0) {
	            char c1 = v1[pos];
	            char c2 = v2[pos];
	            if (c1 != c2) {
	              return c1 - c2;
	            }
	            pos++;
	          }
	          return len1 - len2;
	        }

	}

	/**
	 * 递归删除文件和文件夹
	 * @param file    要删除的根目录
	 */
	public static void RecursionDeleteFile(File file){
		if(file.isFile()){
			file.delete();
			return;
		}
		if(file.isDirectory()){
			File[] childFile = file.listFiles();
			if(childFile == null || childFile.length == 0){
				file.delete();
				return;
			}
			for(File f : childFile){
				RecursionDeleteFile(f);
			}
			file.delete();
		}
	}

	public static void deleteThumbFile(String[] filePath, String selectPath) {
		if(filePath == null || filePath.length < 1) {
			return;
		}

		List<File> fileList = new ArrayList<File>();
		for(int i=0; i<filePath.length; i++) {
			if(!selectPath.equals(filePath[i])) {
				File file = new File(filePath[i]);
				if(file != null) {
					fileList.add(file);
				}
			}
		}

		for(File tmpFile:fileList) {
    		if(tmpFile.exists()) {
    			tmpFile.delete();
    		}
    	}
	}

}
