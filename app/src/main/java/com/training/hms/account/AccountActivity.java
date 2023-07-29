package com.training.hms.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.hms.support.api.entity.common.CommonConstant;
import com.training.hms.R;

public class AccountActivity extends AppCompatActivity {

    // 华为帐号登录授权服务，提供静默登录接口silentSignIn，获取前台登录视图getSignInIntent，登出signOut等接口
    private AccountAuthService mAccountAuthService;

    // 华为帐号登录授权参数
    private AccountAuthParams mAccountAuthParams;

    private TextView mTvResult;

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(result.getData());
            if (authAccountTask.isSuccessful()) {
                // 登录成功，获取到登录帐号信息对象authAccount
                AuthAccount authAccount = authAccountTask.getResult();
                onDealAuthAccount(authAccount);
            } else {
                // 登录失败，status code标识了失败的原因，请参见API参考中的错误码了解详细错误原因
                onInitText("登录失败：" + authAccountTask.getException().getMessage());
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        mTvResult = findViewById(R.id.text_result);
        mTvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        initClickListener();
    }

    private void initClickListener() {
        findViewById(R.id.id_token_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onIdTokenSignIn();
            }
        });
        findViewById(R.id.silent_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSilentSignIn();
            }
        });
        findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignOut();
            }
        });
        findViewById(R.id.cancel_auth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelAuthorization();
            }
        });
    }

    private void onIdTokenSignIn() {
        mAccountAuthParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setEmail()
                .setIdToken()
                .createParams();
        AccountAuthService mAccountAuthService = AccountAuthManager.getService(this, mAccountAuthParams);
        launcher.launch(mAccountAuthService.getSignInIntent());
    }

    private void onSilentSignIn() {
        mAccountAuthParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setEmail()
                .setIdToken()
                .createParams();
        mAccountAuthService = AccountAuthManager.getService(this, mAccountAuthParams);
        Task<AuthAccount> task = mAccountAuthService.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
            @Override
            public void onSuccess(AuthAccount authAccount) {
                onDealAuthAccount(authAccount);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 静默登录失败，使用getSignInIntent()方法进行前台显式登录
                if (e instanceof ApiException) {
                    Intent signInIntent = mAccountAuthService.getSignInIntent();
                    // 如果应用是全屏显示，即顶部无状态栏的应用，需要在Intent中添加如下参数：
                    // intent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true);
                    // 具体详情可以参见应用调用登录接口的时候是全屏页面，为什么在拉起登录页面的过程中顶部的状态栏会闪一下？应该如何解决？
                    signInIntent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true);
                    // startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
                    launcher.launch(signInIntent);
                }
            }
        });
    }

    private void onSignOut() {
        if (null == mAccountAuthService) {
            return;
        }
        Task<Void> task = mAccountAuthService.signOut();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                onInitText("退出登录成功");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                onInitText("退出登录失败");
            }
        });
    }

    private void onCancelAuthorization() {
        if (null == mAccountAuthService) {
            return;
        }
        Task<Void> task = mAccountAuthService.cancelAuthorization();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                onInitText("取消授权成功");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                onInitText("取消授权失败");
            }
        });
    }

    private void onDealAuthAccount(AuthAccount authAccount) {
        onInitText("IDToken模式登录成功");
        onInitText("昵称：" + authAccount.getDisplayName());
        onInitText("邮箱：" + authAccount.getEmail());
        onInitText("头像：" + authAccount.getAvatarUriString());
        Log.e("----Account----", authAccount.getAvatarUriString());
        onInitText("IDToken：" + authAccount.getIdToken());
    }

    private void onInitText(String string) {
        mTvResult.append(string);
        mTvResult.append("\n");
    }
}