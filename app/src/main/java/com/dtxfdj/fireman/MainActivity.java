package com.dtxfdj.fireman;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    Handler mHandler = new Handler();

    WebView mWebView;

    private View mCustomView;
    private FrameLayout mFullscreenContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;


//    private final static String DEFAULT_URL = "http://dh.123.sogou.com";
//    private final static String DEFAULT_URL = "http://10.129.192.204";
    private final static String DEFAULT_URL = "http://m.youtube.com";

    Runnable mDismissStartImg = new Runnable() {
        @Override
        public void run() {
            ImageView imgView = findViewById(R.id.start_img);
            if (imgView != null) {
                imgView.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

//        ImageView imgView = findViewById(R.id.start_img);
//        imgView.setImageDrawable(getApplicationContext().getDrawable(R.drawable.start_img));


        initWebView();
        mHandler.postDelayed(mDismissStartImg, 3000);
    }

    private void initWebView() {
        mWebView = findViewById(R.id.content_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setDomStorageEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                // avoid open url with android default browser
                return true;
            }

            @Override
            public void onReceivedSslError(final WebView view,
                                           final SslErrorHandler handler, final SslError error) {
                handler.proceed();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, true);
            }

            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
                onShowDefaultCustomView(view, callback);
            }


            public void onHideCustomView() {
                onHideDefaulCustomView();
            }
        });
        mWebView.loadUrl(DEFAULT_URL);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && mWebView != null
                && mWebView.canGoBack()
                && mCustomView == null) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    static class FullscreenHolder extends FrameLayout {
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(0xFF000000);
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void onShowDefaultCustomView(
            View view, WebChromeClient.CustomViewCallback callback) {
        if (mCustomView != null) {
            return;
        }
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(this);
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        mFullscreenContainer.addView(view, params);
        decor.addView(mFullscreenContainer, params);
        mCustomView = view;
        setFullscreen(true);
        mCustomViewCallback = callback;
    }

    private void onHideDefaulCustomView() {
        if (mFullscreenContainer == null || mCustomViewCallback == null) {
            return;
        }
        setFullscreen(false);
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(mFullscreenContainer);
        mFullscreenContainer.removeView(mCustomView);
        mFullscreenContainer.setVisibility(View.GONE);
        mFullscreenContainer = null;
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        mCustomViewCallback = null;
    }

    private void setFullscreen(boolean enabled) {
        View decor = getWindow().getDecorView();
        if (enabled) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        int systemUiVisibility = decor.getSystemUiVisibility();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (enabled) {
            systemUiVisibility |= flags;
        } else {
            systemUiVisibility &= ~flags;
        }
        decor.setSystemUiVisibility(systemUiVisibility);
    }
}