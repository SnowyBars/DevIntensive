package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecked;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends BaseActivity implements View.OnClickListener {

    private Button mSignInBtn;
    private EditText mLoginTxt, mPasswordTxt;
    private TextView mRememberPass;
    private CoordinatorLayout mCoordinatorLayout;

    private DataManager mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mSignInBtn = (Button)findViewById(R.id.enter_btn);
        mLoginTxt = (EditText)findViewById(R.id.login_text);
        mPasswordTxt = (EditText)findViewById(R.id.password_text);
        mRememberPass = (TextView)findViewById(R.id.remember_text);
        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_coordinator_container);

        mDataManager = DataManager.getInstance();

        mSignInBtn.setOnClickListener(this);
        mRememberPass.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.enter_btn:
                signIn();
                break;
            case R.id.remember_text:
                rememberPassword();
                break;
        }
    }

    private void showSnackbar(String message){
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void rememberPassword(){
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
        startActivity(rememberIntent);
    }

    private void loginSuccess(UserModelRes userModel){
        showSnackbar(userModel.getData().getToken());
        mDataManager.getPreferencesManager().saveAuthToken(userModel.getData().getToken());
        mDataManager.getPreferencesManager().saveUserId(userModel.getData().getUser().getId());
        saveUserValues(userModel);
        Intent loginIntent = new Intent(this, MainActivity.class);
        startActivity(loginIntent);
    }

    private void signIn(){
        if(NetworkStatusChecked.isNetworkAvailable(this)){
            Call<UserModelRes> call = mDataManager.loginUser(new UserLoginReq(mLoginTxt.getText().toString(), mPasswordTxt.getText().toString()));
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if(response.code() == 200){
                        loginSuccess(response.body());
                    } else if(response.code() == 404){
                        showSnackbar("Неверный логин или пароль");
                    } else {
                        showSnackbar("Что-то пошло не так");
                    }
                }
                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    //// TODO: 12.07.2016 обработать ошибки ретрофита
                }
            });
        } else {
            showSnackbar("Нет доступа к сети на данный момент, попробуйте позже");
        }
    }

    private void saveUserValues(UserModelRes userModel){
        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRating(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects()
        };
        mDataManager.getPreferencesManager().saveUserProfileValues(userValues);
    }
    private void saveUserProfileData(UserModelRes userModel){
        ArrayList<String> userProfileData = new ArrayList<>();
        userProfileData.add(userModel.getData().getUser().getContacts().getPhone());
        userProfileData.add(userModel.getData().getUser().getContacts().getEmail());
        userProfileData.add(userModel.getData().getUser().getContacts().getVk());
        userProfileData.add(userModel.getData().getUser().getRepositories().getRepo().get(0).getGit());
        userProfileData.add(userModel.getData().getUser().getPublicInfo().getBio());
        mDataManager.getPreferencesManager().saveUserProfileData(userProfileData);
    }

    private void saveMediaContent(UserModelRes userModel){
        Uri photo = Uri.parse(userModel.getData().getUser().getPublicInfo().getPhoto());
        Uri avatar = Uri.parse(userModel.getData().getUser().getPublicInfo().getAvatar());
        mDataManager.getPreferencesManager().saveUserPhoto(photo);
        mDataManager.getPreferencesManager().saveUserAvatar(avatar);
    }
    private void saveNameAndSecondName(UserModelRes userModel){
        String name = userModel.getData().getUser().getFirstName();
        String secondName = userModel.getData().getUser().getSecondName();
        mDataManager.getPreferencesManager().saveNameAndSecondName(name, secondName);
    }
}
