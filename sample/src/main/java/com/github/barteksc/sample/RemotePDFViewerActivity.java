package com.github.barteksc.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.remote.DownloadFile;
import com.github.barteksc.pdfviewer.remote.DownloadFileUrlConnectionImpl;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;
import java.io.File;
import java.util.List;

/**
 * Created by yelinaung on 20/12/16.
 */

public class RemotePDFViewerActivity extends AppCompatActivity
    implements DownloadFile.Listener, OnPageChangeListener, OnLoadCompleteListener {

  LinearLayout root;
  EditText pdfUrl;
  Button btnDownload;

  protected DownloadFile.Listener listener;

  private static final String TAG = "pdf";

  PDFView pdfView;

  Integer pageNumber = 0;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_remote_pdf);

    Log.d(TAG, "starting");

    pdfView = (PDFView) findViewById(R.id.pdfView);
    root = (LinearLayout) findViewById(R.id.remote_pdf_root);
    pdfUrl = (EditText) findViewById(R.id.pdfUrl);
    btnDownload = (Button) findViewById(R.id.btnDownload);

    listener = this;
    btnDownload.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.d(TAG, "click click");
        String pdfUrl = getUrlFromEditText();
        Log.d(TAG, pdfUrl);
        DownloadFile downloadFile =
            new DownloadFileUrlConnectionImpl(RemotePDFViewerActivity.this, new Handler(),
                listener);
        downloadFile.download(pdfUrl,
            new File(getCacheDir(), extractFileNameFromURL(pdfUrl)).getAbsolutePath());
        hideDownloadButton();
      }
    });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  private void displayFromUri(File file) {

    pdfView.fromFile(file)
        .defaultPage(pageNumber)
        .onPageChange(this)
        .enableAnnotationRendering(true)
        .onLoad(this)
        .scrollHandle(new DefaultScrollHandle(this))
        .load();
  }

  protected String getUrlFromEditText() {
    return pdfUrl.getText().toString().trim();
  }

  public void showDownloadButton() {
    btnDownload.setVisibility(View.VISIBLE);
  }

  public void hideDownloadButton() {
    btnDownload.setVisibility(View.INVISIBLE);
  }

  @Override public void onSuccess(String url, String destinationPath) {
    Log.i(TAG, "reach success");
    File file = new File(destinationPath);
    displayFromUri(file);
    showDownloadButton();
  }

  @Override public void onFailure(Exception e) {
    e.printStackTrace();
    showDownloadButton();
  }

  @Override public void onProgressUpdate(int progress, int total) {
    Log.i(TAG, "progress  " + progress);
    Log.i(TAG, "total     " + total);
  }

  public static String extractFileNameFromURL(String url) {
    return url.substring(url.lastIndexOf('/') + 1);
  }

  @Override public void onPageChanged(int page, int pageCount) {
    Log.d("pdf", "changed to " + page);
    Log.d("pdf", "page count " + pageCount);
  }

  @Override public void loadComplete(int nbPages) {

    PdfDocument.Meta meta = pdfView.getDocumentMeta();
    Log.e(TAG, "title = " + meta.getTitle());
    Log.e(TAG, "author = " + meta.getAuthor());
    Log.e(TAG, "subject = " + meta.getSubject());
    Log.e(TAG, "keywords = " + meta.getKeywords());
    Log.e(TAG, "creator = " + meta.getCreator());
    Log.e(TAG, "producer = " + meta.getProducer());
    Log.e(TAG, "creationDate = " + meta.getCreationDate());
    Log.e(TAG, "modDate = " + meta.getModDate());

    printBookmarksTree(pdfView.getTableOfContents(), "-");
  }

  public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
    for (PdfDocument.Bookmark b : tree) {

      Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

      if (b.hasChildren()) {
        printBookmarksTree(b.getChildren(), sep + "-");
      }
    }
  }
}
