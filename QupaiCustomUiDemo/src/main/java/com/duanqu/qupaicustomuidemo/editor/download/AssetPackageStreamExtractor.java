package com.duanqu.qupaicustomuidemo.editor.download;

import com.duanqu.qupaicustomuidemo.utils.ZipStreamExtractor;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;

/**
 * extract an asset zip package input stream into a directory
 * stripping the top level dir
 *
 */
public final class AssetPackageStreamExtractor extends ZipStreamExtractor {

    private final File _OutputDir;

    public AssetPackageStreamExtractor(InputStream istream, File output_dir) {
        super(istream);
        _OutputDir = output_dir;
    }

    @Override
    protected File getOutputFile(ZipEntry entry) {
        String name = entry.getName();
        int first_slash = name.indexOf('/');
        if (first_slash >= 0) {
            name = name.substring(first_slash + 1);
        }
        return new File(_OutputDir, name);
    }

}
