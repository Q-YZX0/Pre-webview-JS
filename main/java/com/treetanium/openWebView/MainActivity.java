/**
@@ AntonioZX0  Openwebview
 */

package com.treetanium.tokenbase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progressbar;
    SwipeRefreshLayout swipe;
    IntentIntegrator integrador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview_id);
        swipe = findViewById(R.id.swipeContainer);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadSite(webView.getUrl());
                swipe.setRefreshing(false);
            }
        });
        LoadSite("https://yeansite.wee.app");
    }

    public void LoadSite(String url) {
        webView.addJavascriptInterface(new WebInterface(this), "Android");
        //webView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        //webView.getSettings().setAllowFileAccess(true);
        //webView.getSettings().setAppCacheEnabled(true);

        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        //webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                try {
                    webView.stopLoading();
                } catch (Exception e) {
                }

                //BACK BUTTON
                if (webView.canGoBack()) {
                    webView.goBack();
                }

                //TRY AGAIN ALERT WHEN NO INTERNET CONNECTION (DONT WORK YET)
                webView.loadUrl("about:blank");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("No internet connection:(");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(getIntent());
                    }
                });

                alertDialog.show();
                super.onReceivedError(webView, errorCode, description, failingUrl);
            }
        });

        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://api.whatsapp") || url.startsWith("https://api.whatsapp"))) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public class WebInterface {
        Context mContext;

        WebInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void OpenQR() {
            integrador = new IntentIntegrator(MainActivity.this);
            integrador.setCameraId(0);
            integrador.setBarcodeImageEnabled(true);
            integrador.initiateScan();
        }
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if(result!=null){
                    if(result.getContents()==null){
                        Toast.makeText(this, "Canceled Read", Toast.LENGTH_LONG).show();
                    }else{
                        LoadSite("https://www.yeansite.com/send/"+result.getContents());
                    }
                }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    // This method is used to detect back button
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }
}