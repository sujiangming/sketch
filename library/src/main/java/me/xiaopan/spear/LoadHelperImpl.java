/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.spear;

import android.util.Log;
import android.widget.ImageView;

import me.xiaopan.spear.process.ImageProcessor;

/**
 * LoadHelper
 */
public class LoadHelperImpl implements LoadHelper{
    private static final String NAME = "LoadHelperImpl";

    // 基本属性
    protected Spear spear;
    protected String uri;
    protected String name;

    // 下载属性
    protected boolean enableDiskCache = true;
    protected ProgressListener progressListener;

    // 加载属性
    protected boolean disableGifImage;
    protected ImageSize maxSize;
    protected ImageSize resize;
    protected ImageProcessor imageProcessor;
    protected ImageView.ScaleType scaleType;
    protected LoadListener loadListener;

    /**
     * 创建加载请求生成器
     * @param spear Spear
     * @param uri 图片Uri，支持以下几种
     * <blockquote>"http://site.com/image.png"; // from Web
     * <br>"https://site.com/image.png"; // from Web
     * <br>"/mnt/sdcard/image.png"; // from SD card
     * <br>"/mnt/sdcard/app.apk"; // from SD card apk file
     * <br>"content://media/external/audio/albumart/13"; // from content provider
     * <br>"asset://image.png"; // from assets
     * <br>"drawable://" + R.drawable.image; // from drawables (only images, non-9patch)
     * </blockquote>
     */
    public LoadHelperImpl(Spear spear, String uri) {
        this.spear = spear;
        this.uri = uri;
        this.maxSize = spear.getConfiguration().getImageSizeCalculator().getDefaultImageMaxSize(spear.getConfiguration().getContext());
    }

    @Override
    public LoadHelperImpl name(String name){
        this.name = name;
        return this;
    }

    @Override
    public LoadHelperImpl disableDiskCache() {
        this.enableDiskCache = false;
        return this;
    }

    @Override
    public LoadHelperImpl disableGifImage() {
        this.disableGifImage = true;
        return this;
    }

    @Override
    public LoadHelperImpl maxSize(ImageSize maxSize){
        this.maxSize = maxSize;
        return this;
    }

    @Override
    public LoadHelperImpl maxSize(int width, int height){
        this.maxSize = new ImageSize(width, height);
        return this;
    }

    @Override
    public LoadHelperImpl resize(ImageSize resize){
        this.resize = resize;
        return this;
    }

    @Override
    public LoadHelperImpl resize(int width, int height){
        this.resize = new ImageSize(width, height);
        return this;
    }

    @Override
    public LoadHelperImpl processor(ImageProcessor processor){
        this.imageProcessor = processor;
        return this;
    }

    @Override
    public LoadHelperImpl listener(LoadListener loadListener){
        this.loadListener = loadListener;
        return this;
    }

    @Override
    public LoadHelperImpl scaleType(ImageView.ScaleType scaleType){
        this.scaleType = scaleType;
        return this;
    }

    @Override
    public LoadHelperImpl progressListener(ProgressListener progressListener){
        this.progressListener = progressListener;
        return this;
    }

    @Override
    public LoadHelperImpl options(LoadOptions options){
        if(options == null){
            return this;
        }

        this.enableDiskCache = options.isEnableDiskCache();
        if(this.maxSize == null){
            this.maxSize = options.getMaxSize();
        }
        if(this.resize == null){
            this.resize = options.getResize();
        }
        if(this.scaleType == null){
            this.scaleType = options.getScaleType();
        }
        if(this.imageProcessor == null){
            this.imageProcessor = options.getImageProcessor();
        }
        this.disableGifImage = options.isDisableGifImage();

        return this;
    }

    @Override
    public LoadHelperImpl options(Enum<?> optionsName){
        return options((LoadOptions) Spear.getOptions(optionsName));
    }

    @Override
    public Request fire() {
        if(loadListener != null){
            loadListener.onStarted();
        }

        // 验证uri参数
        if(uri == null || "".equals(uri.trim())){
            if(Spear.isDebugMode()){
                Log.e(Spear.TAG, NAME + " - " + "uri is null or empty");
            }
            if(loadListener != null){
                loadListener.onFailed(FailCause.URI_NULL_OR_EMPTY);
            }
            return null;
        }

        // 过滤掉不支持的URI协议类型
        UriScheme uriScheme = UriScheme.valueOfUri(uri);
        if(uriScheme == null){
            if(Spear.isDebugMode()){
                Log.e(Spear.TAG, NAME + " - " + "unknown uri scheme" + " - " + uri);
            }
            if(loadListener != null){
                loadListener.onFailed(FailCause.URI_NO_SUPPORT);
            }
            return null;
        }

        // 创建请求
        LoadRequest request = spear.getConfiguration().getRequestFactory().newLoadRequest(spear, uri, uriScheme);

        request.setName(name != null ? name : uri);

        request.setEnableDiskCache(enableDiskCache);
        request.setProgressListener(progressListener);

        request.setResize(resize);
        request.setMaxSize(maxSize);
        request.setScaleType(scaleType);
        request.setLoadListener(loadListener);
        request.setImageProcessor(imageProcessor);
        request.setDisableGifImage(disableGifImage);

        request.postRunDispatch();

        return request;
    }
}