package org.telegram.android.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import com.extradea.framework.images.utils.ImageUtils;
import org.telegram.android.R;
import org.telegram.android.activity.CropImageActivity;
import org.telegram.android.media.Optimizer;
import org.telegram.android.ui.pick.PickIntentClickListener;
import org.telegram.android.ui.pick.PickIntentDialog;
import org.telegram.android.ui.pick.PickIntentItem;
import org.telegram.android.video.VideoRecorderActivity;

import java.io.*;
import java.util.*;

/**
 * Author: Korshakov Stepan
 * Created: 01.08.13 6:25
 */
public class MediaReceiverFragment extends TelegramFragment {

    private static final int REQ_M = 5;

    private static final int REQUEST_BASE = 100;

    private String imageFileName;
    private String videoFileName;

    private Random rnd = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            imageFileName = savedInstanceState.getString("picker:imageFileName");
            videoFileName = savedInstanceState.getString("picker:videoFileName");
        }
    }

    protected String getUploadTempAudioFile() {
        return getUploadTempFile(".m4a");
    }

    protected String getUploadTempFile() {
        return getUploadTempFile(".jpg");
    }

    protected String getUploadVideoTempFile() {
        return getUploadTempFile(".mp4");
    }

    public void requestPhotoChooserWithDelete(final int requestId) {
        imageFileName = getTempExternalFile(".jpg");
        final Uri fileUri = Uri.fromFile(new File(imageFileName));

        ArrayList<PickIntentItem> items = new ArrayList<PickIntentItem>();
        Collections.addAll(items, createPickIntents(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)));
        Collections.addAll(items, createPickIntents(new Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*")));
        items.add(new PickIntentItem(R.drawable.holo_light_ic_delete, "Delete"));

        PickIntentDialog dialog = new PickIntentDialog(getActivity(),
                items.toArray(new PickIntentItem[items.size()]),
                new PickIntentClickListener() {
                    @Override
                    public void onItemClicked(int index, PickIntentItem item) {
                        if (item.getIntent() == null) {
                            onPhotoDeleted(requestId);
                        } else {
                            if (MediaStore.ACTION_IMAGE_CAPTURE.equals(item.getIntent().getAction())) {
                                startActivityForResult(item.getIntent().putExtra(MediaStore.EXTRA_OUTPUT, fileUri),
                                        requestId * REQ_M + REQUEST_BASE);
                            } else if (Intent.ACTION_GET_CONTENT.equals(item.getIntent().getAction())) {
                                startActivityForResult(item.getIntent(), requestId * REQ_M + REQUEST_BASE + 1);
                            } else {
                                startActivityForResult(item.getIntent(), requestId * REQ_M + REQUEST_BASE + 4);
                            }
                        }
                    }
                });
        dialog.setTitle("Edit photo");
        dialog.show();
    }

    public void requestWallpaperChooser(final int requestId) {
        imageFileName = getTempExternalFile(".jpg");
        final Uri fileUri = Uri.fromFile(new File(imageFileName));

        ArrayList<PickIntentItem> items = new ArrayList<PickIntentItem>();
        items.add(new PickIntentItem(R.drawable.app_icon, "Built-In").setTag("built-in"));
        items.add(new PickIntentItem(R.drawable.app_icon, "Default").setTag("default"));
        items.add(new PickIntentItem(R.drawable.holo_light_ic_delete, "No Wallpaper").setTag("empty"));
        if (hasApplication("com.whatsapp") && hasApplication("com.whatsapp.wallpaper")) {
            Collections.addAll(items, createPickIntents(new Intent().setClassName("com.whatsapp", "com.whatsapp.wallpaper.WallpaperPicker")));
        }
        Collections.addAll(items, createPickIntents(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)));
        Collections.addAll(items, createPickIntents(new Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*")));


        PickIntentDialog dialog = new PickIntentDialog(getActivity(),
                items.toArray(new PickIntentItem[items.size()]),
                new PickIntentClickListener() {
                    @Override
                    public void onItemClicked(int index, PickIntentItem item) {
                        if ("empty".equals(item.getTag())) {
                            application.getUserSettings().setWallpaperSet(true);
                            application.getUserSettings().setWallpaperSolid(true);
                            application.getUserSettings().setCurrentWallpaperId(0);
                            application.getUserSettings().setCurrentWallpaperSolidColor(0xffD2E2EE);
                            application.getWallpaperHolder().dropCache();
                        } else if ("default".equals(item.getTag())) {
                            application.getUserSettings().setWallpaperSet(false);
                            application.getWallpaperHolder().dropCache();
                        } else if ("built-in".equals(item.getTag())) {
                            getRootController().openWallpaperSettings();
                        } else if (item.getIntent() == null) {
                            onPhotoDeleted(requestId);
                        } else {
                            if (MediaStore.ACTION_IMAGE_CAPTURE.equals(item.getIntent().getAction())) {
                                startActivityForResult(item.getIntent().putExtra(MediaStore.EXTRA_OUTPUT, fileUri),
                                        requestId * REQ_M + REQUEST_BASE);
                            } else if (Intent.ACTION_GET_CONTENT.equals(item.getIntent().getAction())) {
                                startActivityForResult(item.getIntent(), requestId * REQ_M + REQUEST_BASE + 1);
                            } else {
                                startActivityForResult(item.getIntent(), requestId * REQ_M + REQUEST_BASE + 4);
                            }
                        }
                    }
                });
        dialog.setTitle("Change wallpaper");
        dialog.show();
    }

    public void requestPhotoChooser(final int requestId) {
        imageFileName = getTempExternalFile(".jpg");
        final Uri fileUri = Uri.fromFile(new File(imageFileName));

        ArrayList<PickIntentItem> items = new ArrayList<PickIntentItem>();
        Collections.addAll(items, createPickIntents(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)));
        Collections.addAll(items, createPickIntents(new Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*")));

        PickIntentDialog pickIntentDialog = new PickIntentDialog(getActivity(),
                items.toArray(new PickIntentItem[items.size()]),
                new PickIntentClickListener() {
                    @Override
                    public void onItemClicked(int index, PickIntentItem item) {
                        if (MediaStore.ACTION_IMAGE_CAPTURE.equals(item.getIntent().getAction())) {
                            startActivityForResult(item.getIntent().putExtra(MediaStore.EXTRA_OUTPUT, fileUri),
                                    requestId * REQ_M + REQUEST_BASE);
                        } else {
                            startActivityForResult(item.getIntent(), requestId * REQ_M + REQUEST_BASE + 1);
                        }
                    }
                });
        pickIntentDialog.setTitle("Choose photo");
        pickIntentDialog.show();
    }

    public void requestVideo(int requestId) {
        try {
            videoFileName = getTempExternalFile(".mp4");

            Intent intent = new Intent();
            intent.setClass(getActivity(), VideoRecorderActivity.class);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(videoFileName)));

            startActivityForResult(intent, requestId * REQ_M + REQUEST_BASE);
//            PackageManager pm = application.getPackageManager();
//
//            List<ResolveInfo> rList = application.getPackageManager().queryIntentActivities(
//                    intent, PackageManager.MATCH_DEFAULT_ONLY);
//
//            ArrayList<PickIntentDialog.PickIntentItem> items = new ArrayList<PickIntentDialog.PickIntentItem>();
//            for (ResolveInfo info : rList) {
//                items.add(new PickIntentDialog.PickIntentItem(info.loadIcon(pm), info.loadLabel(pm).toString()));
//            }
//
//            new PickIntentDialog(getActivity(), items.toArray(new PickIntentDialog.PickIntentItem[0])).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.st_error_unsupported, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean cropSupported(Uri data) {
        return true;
    }

    public void requestCrop(String fileName, int width, int height, int requestId) {
        try {
            imageFileName = getUploadTempFile();
            String cropFileName = getUploadTempFile();
            Optimizer.optimize(fileName, cropFileName);
            Intent intent = CropImageActivity.cropIntent(cropFileName, 1, 1, imageFileName, getActivity());
            startActivityForResult(intent, requestId * REQ_M + REQUEST_BASE + 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestCrop(Uri uri, int width, int height, int requestId) {
        try {
            imageFileName = getUploadTempFile();
            String cropFileName = getUploadTempFile();
            Optimizer.optimize(uri.toString(), application, cropFileName);
            Intent intent = CropImageActivity.cropIntent(cropFileName, 1, 1, imageFileName, getActivity());
            startActivityForResult(intent, requestId * REQ_M + REQUEST_BASE + 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onPhotoArrived(String fileName, int width, int height, int requestId) {

    }

    protected void onPhotoArrived(Uri uri, int width, int height, int requestId) {

    }

    protected void onPhotoCropped(Uri uri, int requestId) {

    }

    protected void onPhotoDeleted(int requestId) {

    }

    protected void onVideoArrived(String fileName, int requestId) {

    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode >= REQUEST_BASE) {
                if (requestCode % REQ_M == 0) {
                    if (imageFileName == null) {
                        return;
                    }

                    int width = 0;
                    int height = 0;

                    try {
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(imageFileName, o);
                        width = o.outWidth;
                        height = o.outHeight;
                    } catch (OutOfMemoryError e) {

                    }

                    if (!ImageUtils.isVerticalImage(imageFileName)) {
                        int tmp = height;
                        height = width;
                        width = tmp;
                    }

                    final int finalWidth = width;
                    final int finalHeight = height;
                    secureCallback(new Runnable() {
                        @Override
                        public void run() {
                            onPhotoArrived(imageFileName, finalWidth, finalHeight, (requestCode - REQUEST_BASE) / REQ_M);
                        }
                    });
                } else if (requestCode % REQ_M == 1) {
                    if (data == null || data.getData() == null || data.getData().getPath() == null)
                        return;

                    final Uri selectedImageUri = data.getData();

                    int width = 0;
                    int height = 0;

                    try {
                        InputStream stream = application.getContentResolver().openInputStream(selectedImageUri);
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(stream, new Rect(), o);
                        width = o.outWidth;
                        height = o.outHeight;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (!ImageUtils.isVerticalImage(selectedImageUri, getActivity())) {
                        int tmp = height;
                        height = width;
                        width = tmp;
                    }

                    final int finalWidth = width;
                    final int finalHeight = height;
                    secureCallback(new Runnable() {
                        @Override
                        public void run() {
                            if (selectedImageUri.getScheme().equals("file")) {
                                onPhotoArrived(selectedImageUri.getPath(), finalWidth, finalHeight, (requestCode - REQUEST_BASE) / REQ_M);
                            } else {
                                onPhotoArrived(selectedImageUri, finalWidth, finalHeight, (requestCode - REQUEST_BASE) / REQ_M);
                            }
                        }
                    });
                } else if (requestCode % REQ_M == 2) {
                    secureCallback(new Runnable() {
                        @Override
                        public void run() {
                            onVideoArrived(videoFileName, (requestCode - REQUEST_BASE) / REQ_M);
                        }
                    });
                } else if (requestCode % REQ_M == 3) {
                    /*Uri selectedImageUri = data.getData();
                    if (selectedImageUri == null) {

                    }*/
                    secureCallback(new Runnable() {
                        @Override
                        public void run() {
                            onPhotoCropped(Uri.fromFile(new File(imageFileName)), (requestCode - REQUEST_BASE) / REQ_M);
                        }
                    });
                } else if (requestCode % REQ_M == 4) {
                    try {
                        Integer resourceId = data.getExtras().getInt("redId");
                        BitmapDrawable drawable = (BitmapDrawable) application.getPackageManager().getResourcesForApplication("com.whatsapp.wallpaper").getDrawable(resourceId);
                        Bitmap bitmap = drawable.getBitmap();
                        String fileName = getUploadTempFile();
                        FileOutputStream outputStream = new FileOutputStream(fileName);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 87, outputStream);
                        outputStream.close();
                        onPhotoArrived(fileName, bitmap.getWidth(), bitmap.getHeight(), (requestCode - REQUEST_BASE) / REQ_M);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("picker:imageFileName", imageFileName);
        outState.putString("picker:videoFileName", videoFileName);
    }
}
