package com.digitaldukaan.constants;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.digitaldukaan.R;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadPdfManager {

    private static final String TAG = DownloadPdfManager.class.getSimpleName();
    private static final String GOOGLE_DRIVE_PDF_READER_PREFIX = "http://drive.google.com/viewer?url=";
    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final String HTML_MIME_TYPE = "text/html";

    public static void downloadPDFFromUrl(final Context context, final String pdfUrl) {
        if (isPDFSupported(context)) {
            downloadAndOpenPDF(context, pdfUrl);
        } else {
            askToOpenPDFThroughGoogleDrive(context, pdfUrl);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void downloadAndOpenPDF(final Context context, final String pdfUrl) {
        String filename = "";
        try {
            filename = new GetFileInfo().execute(pdfUrl).get();
        } catch (Exception e) {
            Log.e(TAG, "downloadAndOpenPDF: " + e.getMessage(), e);
        }
        final File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);
        Log.e(TAG, "File Path:" + tempFile);
        if (tempFile.exists()) {
            openPDF(context, Uri.fromFile(tempFile));
            return;
        }
        final ProgressDialog progress = ProgressDialog.show(context, context.getString(R.string.please_wait), "Downloading", true);
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(pdfUrl));
        r.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);
        final DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!progress.isShowing()) {
                    return;
                }
                context.unregisterReceiver(this);
                progress.dismiss();
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Cursor c = dm.query(new DownloadManager.Query().setFilterById(downloadId));
                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        openPDF(context, Uri.fromFile(tempFile));
                    }
                }
                c.close();
            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        dm.enqueue(r);
    }

    private static void askToOpenPDFThroughGoogleDrive(final Context context, final String pdfUrl) {
        new AlertDialog.Builder(context)
                .setTitle("Open PDF")
                .setMessage("Do you want to open PDF")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialog, which) -> openPDFThroughGoogleDrive(context, pdfUrl))
                .show();
    }

    private static void openPDFThroughGoogleDrive(final Context context, final String pdfUrl) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse(GOOGLE_DRIVE_PDF_READER_PREFIX + pdfUrl), HTML_MIME_TYPE);
        context.startActivity(i);
    }

    private static void openPDF(Context context, Uri localUri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(localUri, PDF_MIME_TYPE);
        context.startActivity(i);
    }

    private static boolean isPDFSupported(Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        final File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "test.pdf");
        i.setDataAndType(Uri.fromFile(tempFile), PDF_MIME_TYPE);
        return context.getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    private static class GetFileInfo extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... urls) {
            URL url;
            String filename = null;
            try {
                url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                conn.setInstanceFollowRedirects(false);
                if (conn.getHeaderField("Content-Disposition") != null) {
                    String disposition = conn.getHeaderField("Content-Disposition");
                    String[] dispositionSplit = disposition.split("filename=");
                    filename = dispositionSplit[1].replace("filename=", "").replace("\"", "").trim();
                } else {
                    filename = "download.pdf";
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: " + e.getMessage(), e);
            }
            return filename;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}
