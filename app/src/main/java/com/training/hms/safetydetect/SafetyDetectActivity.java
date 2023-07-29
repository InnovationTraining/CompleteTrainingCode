package com.training.hms.safetydetect;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.entity.safetydetect.SysIntegrityRequest;
import com.huawei.hms.support.api.entity.safetydetect.SysIntegrityResp;
import com.huawei.hms.support.api.safetydetect.SafetyDetect;
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient;
import com.training.hms.R;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SafetyDetectActivity extends AppCompatActivity {
    private TextView mTvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_detect);
        mTvResult = findViewById(R.id.text_result);
        mTvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        initClickListener();
    }

    private void initClickListener() {
        findViewById(R.id.sysIntegrity_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSysIntegrity();
            }
        });
    }

    private void onSysIntegrity() {
        SafetyDetectClient mClient = SafetyDetect.getClient(this);
        SysIntegrityRequest sysintegrityrequest = new SysIntegrityRequest();
        // 一个nonce值只能被使用一次。,nonce值必须为16至66字节之间。推荐的做法是从发送到您的服务器的数据中派生nonce值。
        sysintegrityrequest.setNonce(getDetectNonce());
        // 从agconnect-services.json文件中读取app_id
        String app_Id = AGConnectInstance.getInstance().getOptions().getString("client/app_id");
        sysintegrityrequest.setAppId(app_Id);
        sysintegrityrequest.setAlg("RS256");// 签名算法
        Task<SysIntegrityResp> task = mClient.sysIntegrity(sysintegrityrequest);
        task.addOnSuccessListener(new OnSuccessListener<SysIntegrityResp>() {
            @Override
            public void onSuccess(SysIntegrityResp response) {
                // 验证系统完整性检测结果
                onCheckSysIntegrityResult(response);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e("---SafetyDetect---", "ERROR:" + e.getMessage());
            }
        });
    }

    private byte[] getDetectNonce() {
        byte[] nonce = new byte[24];
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(nonce);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return nonce;
    }

    /**
     * 验证系统完整性检测结果
     */
    private void onCheckSysIntegrityResult(SysIntegrityResp response) {
        String jwsStr = response.getResult();
        onInitText("检测结果数据：");
        onInitText(jwsStr);
        String[] jwsSplit = jwsStr.split("\\.");
        String jwsPayloadStr = jwsSplit[1];
        String payloadDetail = new String(Base64.decode(jwsPayloadStr.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE), StandardCharsets.UTF_8);
        onInitText("解析结果数据：");
        onInitText(payloadDetail);
        try {
            JSONObject jsonObject = new JSONObject(payloadDetail);
            boolean basicIntegrity = jsonObject.getBoolean("basicIntegrity");
            if (basicIntegrity) {
                onInitText("系统完整性检测结果：安全");
            } else {
                onInitText("系统完整性检测结果：不安全");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onInitText(String string) {
        mTvResult.append(string);
        mTvResult.append("\n");
    }
}