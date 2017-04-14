package com.duanqu.qupaicustomuidemo.editor.download;

import com.duanqu.qupai.cache.core.process.FileProcessor;
import com.duanqu.qupai.effect.asset.*;
import com.duanqu.qupai.effect.asset.AssetPackageFileExtractor;

import java.io.*;

public class ZIPFileProcessor implements FileProcessor {

	private final File mPackageDir;
	private final long id;
	private AbstractDownloadManager.ResourceDecompressListener decompressListener;

	public void setResourceDecompressListener(AbstractDownloadManager.ResourceDecompressListener l){
		decompressListener = l;
	}

	public ZIPFileProcessor(File packageDir, long id){
		this.mPackageDir = packageDir;
		this.id = id;
	}

	@Override
	public File process(File file) {
		if(decompressListener != null){
			decompressListener.onResourceDecompressStart(id);
		}
		AssetPackageFileExtractor xtr;
		try {
			xtr = new AssetPackageFileExtractor(file, mPackageDir);
		} catch (IOException e) {
			e.printStackTrace();
			file.delete();
			if(decompressListener != null){
				decompressListener.onResourceDecompressFailed(id);
			}
			return null;
		}

		try {
            while (xtr.extractNext()) {

            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            file.delete();
			if(decompressListener != null){
				decompressListener.onResourceDecompressFailed(id);
			}
            return null;
        } finally {
            try {
                xtr.close();
            } catch (IOException e) {
            }
        }
		file.delete();
		if(decompressListener != null){
			decompressListener.onResourceDecompressCompleted(id);
		}
		return mPackageDir;
	}

}
