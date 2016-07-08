package com.softdesign.devintensive.ui.activities;


import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.RoundedAvatar;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {


    private static final String TAG = ConstantManager.TAG_PREFIX + "Main Activity";

    private DataManager mDataManager;

    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private int mCurrentEditMode = 0;
    private FloatingActionButton mFab;
    private ImageView mUserAvatar, mProfileImage, mImgCall, mImgMail, mImgVK, mImgGit;
    private RelativeLayout mProfilePlaceholder;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private AppBarLayout.LayoutParams mAppBarParams = null;
    private AppBarLayout mAppBarLayout;
    private File mPhotoFile = null;
    private Uri mSelectedImage = null;

    private EditText mUserPhone, mUserMail, mUserVK, mUserGit, mUserBio;

    private List<EditText> mUserInfoViews;

    //организация жизненного цикла программы
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //инициализация используемых компонент
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_container);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mProfilePlaceholder = (RelativeLayout) findViewById(R.id.profile_placeholder);
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        mProfileImage = (ImageView) findViewById(R.id.user_photo);
        mImgCall = (ImageView)findViewById(R.id.call_img);
        mImgMail = (ImageView)findViewById(R.id.send_mail_img);
        mImgVK = (ImageView)findViewById(R.id.show_vk_img);
        mImgGit = (ImageView)findViewById(R.id.show_git_img);

        mUserPhone = (EditText) findViewById(R.id.user_phone);
        mUserMail = (EditText) findViewById(R.id.user_mail);
        mUserVK = (EditText) findViewById(R.id.user_vk);
        mUserGit = (EditText) findViewById(R.id.user_git);
        mUserBio = (EditText) findViewById(R.id.user_bio);

        mUserInfoViews = new ArrayList<>();
        mUserInfoViews.add(mUserPhone);
        mUserInfoViews.add(mUserMail);
        mUserInfoViews.add(mUserVK);
        mUserInfoViews.add(mUserGit);
        mUserInfoViews.add(mUserBio);

        //установка логирования
        Log.d(TAG, "onCreate");

        mDataManager = DataManager.getInstance();

        //подключение слушателей обработки нажатия
        mFab.setOnClickListener(this);
        mProfilePlaceholder.setOnClickListener(this);
        mImgCall.setOnClickListener(this);
        mImgMail.setOnClickListener(this);
        mImgVK.setOnClickListener(this);
        mImgGit.setOnClickListener(this);

        setupToolbar();
        setupDrawer();
        loadUserInfoValue();
        Picasso.with(this)
                .load(mDataManager.getPreferencesManager().loadUserPhoto())
                .placeholder(R.drawable.user_photo)
                .into(mProfileImage);

        //проверка сохранения данных пользователя
        if (savedInstanceState == null) {
            //первый запуск
        } else {
            mCurrentEditMode = savedInstanceState.getInt(ConstantManager.EDIT_MODE_KEY, 0);
            changeEditMode(mCurrentEditMode);
        }
    }

    //обработка открытия DrawerLayout
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        //сохранение данных пользователя
        saveUserInfoValue();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }


    //обработка нажатия Button и ImageView с использованием функций, вызывающих неявные Intent'ы
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (mCurrentEditMode == 0) {
                    changeEditMode(1);
                    mCurrentEditMode = 1;
                } else {
                    changeEditMode(0);
                    mCurrentEditMode = 0;
                }
                break;
            case R.id.profile_placeholder:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;
            case R.id.call_img:
                actionCall();
                break;
            case R.id.send_mail_img:
                actionEmail();
                break;
            case R.id.show_vk_img:
                actionVK();
                break;
            case R.id.show_git_img:
                actionGit();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
    }


    //обработка нажатия системной кнопки back
    @Override
    public void onBackPressed() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ConstantManager.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mSelectedImage = data.getData();
                    insertProfileImage(mSelectedImage);
                }
                break;
            case ConstantManager.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mSelectedImage = Uri.fromFile(mPhotoFile);
                    insertProfileImage(mSelectedImage);
                }

        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectItems = {getString(R.string.user_profile_dialog_load_from_gallery), getString(R.string.user_profile_dialog_camera), getString(R.string.user_profile_dialog_cancel)};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.user_profile_placeholder_image));
                builder.setItems(selectItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choiseItem) {
                        switch (choiseItem) {
                            case 0:
                                loadPhotoFromGallery();
                                break;
                            case 1:
                                loadPhotoFromCamera();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                return builder.create();
            default:
                return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConstantManager.CAMERA_REQUEST_PERMISSION_CODE && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }

        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {

        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        mAppBarParams = (AppBarLayout.LayoutParams) mCollapsingToolbar.getLayoutParams();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View view = navigationView.getHeaderView(0);
        mUserAvatar = (ImageView) view.findViewById(R.id.user_avatar);
        mUserAvatar.setImageBitmap(RoundedAvatar.getRoundedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.avatar)));
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                showSnackbar(item.getTitle().toString());
                item.setChecked(true);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    private void changeEditMode(int mode) {
        if (mode == 1) {
            mFab.setImageResource(R.drawable.ic_check_black_24dp);
            for (EditText userValue : mUserInfoViews) {
                userValue.setEnabled(true);
                userValue.setFocusable(true);
                userValue.setFocusableInTouchMode(true);
                showProfilePlaceholder();
                lockToolbar();
                mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);

            }
        } else {
            mFab.setImageResource(R.drawable.ic_create_black_24dp);
            for (EditText userValue : mUserInfoViews) {
                userValue.setEnabled(false);
                userValue.setFocusable(false);
                userValue.setFocusableInTouchMode(false);
                hideProfilePlaceholder();
                unlockToolbar();
                mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.white));
                saveUserInfoValue();
            }
        }

    }

    private void loadUserInfoValue() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();
        for (int i = 0; i < userData.size(); i++) {
            mUserInfoViews.get(i).setText(userData.get(i));
        }
    }

    private void saveUserInfoValue() {
        List<String> userData = new ArrayList<>();
        for (EditText userFieldView : mUserInfoViews) {
            userData.add(userFieldView.getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);
    }

    private void loadPhotoFromGallery() {
        Intent takeGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        takeGalleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(takeGalleryIntent, getString(R.string.user_profile_choose_message)), ConstantManager.REQUEST_GALLERY_PICTURE);
    }

    private void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mPhotoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PICTURE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, ConstantManager.CAMERA_REQUEST_PERMISSION_CODE);

                Snackbar.make(mCoordinatorLayout, R.string.user_profile_need_permission, Snackbar.LENGTH_LONG)
                        .setAction(R.string.user_profile_permit, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openApplicationSettings();
                            }
                        }).show();
            }
        }
    }

    private void hideProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.GONE);
    }

    private void showProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.VISIBLE);
    }

    private void lockToolbar() {
        mAppBarLayout.setExpanded(true, true);
        mAppBarParams.setScrollFlags(0);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    private void unlockToolbar() {
        mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());
        this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return image;
    }

    private void insertProfileImage(Uri selectedImage) {
        Picasso.with(this)
                .load(selectedImage)
                .into(mProfileImage);

        mDataManager.getPreferencesManager().saveUserPhoto(selectedImage);
    }

    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, ConstantManager.PERMISSION_REQUEST_SETTINGS_CODE);
    }

    private void actionCall() {
        String number = mUserPhone.getText().toString();
        Intent dialCallIntent = new Intent(Intent.ACTION_CALL);
        dialCallIntent.setData(Uri.parse("tel:"+number));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},ConstantManager.PERMISSION_CALL_REQUEST_CODE);
        }else{
            startActivity(dialCallIntent);
        }
    }
    private void actionEmail(){
        String address = mUserMail.getText().toString();
        Intent dialEmailIntent = new Intent(Intent.ACTION_SEND);
        dialEmailIntent.setType("text/html");
        dialEmailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        dialEmailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.intent_send_extra_subject));
        dialEmailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.user_profile_extra_text));
        startActivity(Intent.createChooser(dialEmailIntent, "Send mail"));
    }
    private void actionVK(){
        String addressVK = mUserVK.getText().toString();
        Intent dialVKIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + addressVK));
        startActivity(dialVKIntent);
    }

    private void actionGit(){
        String addressRepoGit = mUserGit.getText().toString();
        Intent dialRepoGitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + addressRepoGit));
        startActivity(dialRepoGitIntent);
    }
    private boolean validationCheck(EditText editText){
        switch(editText.getId()){
            case R.id.user_phone:
                String number = editText.getText().toString();
                if(number.length()>=11&&number.length()<=20){
                    if(number.charAt(0) == '+' && number.charAt(1) == 8){
                        number.replace('8', '7');
                    }
                    else if(number.charAt(0) != '8' && number.charAt(0) != '+'){
                        number = '+' + number;
                    }
                    return true;
                }
                else{
                    showSnackbar(getString(R.string.user_profile_error_input_number_phone));
                    return false;
                }
            case R.id.user_mail:
                String mail = editText.getText().toString();
                if(mail.contains("@")){
                    int i = 0;
                    while(mail.charAt(i) != '@'){
                        i++;
                    }
                    if(i >= 3){
                        while(mail.charAt(i) != '.'){
                            i++;
                        }
                        if(i >= 6){
                            if(mail.length() > 8){
                                return true;
                            } else {showSnackbar(getString(R.string.user_profile_error_input_email));
                                return false;}
                        } else {showSnackbar(getString(R.string.user_profile_error_input_email));
                            return false;}
                    } else {showSnackbar(getString(R.string.user_profile_error_input_email));
                        return false;}
                } else {
                    showSnackbar(getString(R.string.user_profile_error_input_email));
                    return false;
                }
            case R.id.user_vk:
                String vk_account = editText.getText().toString();
                if(vk_account.startsWith("vk.com/")){
                    return true;
                } else {
                    showSnackbar(getString(R.string.user_profile_error_input_account_vk));
                    return false;
                }
            case R.id.user_git:
                String git_repo = editText.getText().toString();
                if(git_repo.startsWith("github.com/")){
                    return true;
                } else {
                    showSnackbar(getString(R.string.user_profile_error_input_repo_git));
                    return false;
                }
                default: return true;
        }
    }
}