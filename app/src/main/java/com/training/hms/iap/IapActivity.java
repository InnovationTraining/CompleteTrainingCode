package com.training.hms.iap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;
import com.training.hms.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 消耗型：
 * training_consume_01
 * training_consume_02
 * training_consume_03
 * 非消耗型
 * training_nonconsume_01
 * 订阅型
 * training_subscribed_01
 */
public class IapActivity extends AppCompatActivity {
    private static final String TAG = "--IapActivity--";

    private IapAdapter mIapAdapter;

    private final List<ProductInfo> mItemData = new ArrayList<>();
    private final List<String> productIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iap);
        initView();
        isEnvReady();
    }

    private void initView() {
        RecyclerView mRecyclerView = findViewById(R.id.iap_recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        //设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        //设置为垂直布局，这也是默认的
        manager.setOrientation(RecyclerView.VERTICAL);
        //设置Adapter
        mIapAdapter = new IapAdapter(mItemData);
        mRecyclerView.setAdapter(mIapAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mIapAdapter.setListener((view, position) -> onAdapterItemClick(position));
    }

    /**
     * 购买点击
     */
    private void onAdapterItemClick(int position) {
        ProductInfo productInfo = mItemData.get(position);
        onBuyPurchases(productInfo.getProductId());
    }

    /**
     * 判断是否支持IAP
     */
    private void isEnvReady() {
        Task<IsEnvReadyResult> task = Iap.getIapClient(this).isEnvReady();
        task.addOnSuccessListener(new OnSuccessListener<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult isEnvReadyResult) {
                Toast.makeText(IapActivity.this, "支持应用内支付服务", Toast.LENGTH_SHORT).show();
                // Consumable_01
                productIdList.add("training_product_01");
                productIdList.add("training_product_03");
                onGetPurchases();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(IapActivity.this, "不支持应用内支付服务", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 获取商品
     */
    private void onGetPurchases() {
        ProductInfoReq req = new ProductInfoReq();
        // priceType: 0：消耗型商品; 1：非消耗型商品; 2：订阅型商品
        req.setPriceType(0);
        req.setProductIds(productIdList);
        // 获取调用接口的Activity对象
        // 调用obtainProductInfo接口获取AppGallery Connect网站配置的商品的详情信息
        Task<ProductInfoResult> task = Iap.getIapClient(this).obtainProductInfo(req);
        task.addOnSuccessListener(new OnSuccessListener<ProductInfoResult>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(ProductInfoResult result) {
                // 获取接口请求成功时返回的商品详情信息
                Log.i(TAG, "应用内支付~获取商品成功");
                mItemData.clear();
                mItemData.addAll(result.getProductInfoList());
                if (mIapAdapter != null) {
                    mIapAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "应用内支付~获取商品失败 ： " + e.getMessage());
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    int returnCode = apiException.getStatusCode();
                    Log.e(TAG, "应用内支付~returnCode ： " + returnCode);
                } else {
                    // 其他外部错误
                    Log.e(TAG, "应用内支付~Exception ： " + e.getMessage());
                }
            }
        });
    }

    /**
     * 购买商品
     */
    private void onBuyPurchases(String productId) {
        // 构造一个PurchaseIntentReq对象
        PurchaseIntentReq req = new PurchaseIntentReq();
        // 通过createPurchaseIntent接口购买的商品必须是您在AppGallery Connect网站配置的商品。
        req.setProductId(productId);//Consumable_01
        // priceType: 0：消耗型商品; 1：非消耗型商品; 2：订阅型商品
        req.setPriceType(0);
        req.setDeveloperPayload("testPurchases");
        // 调用createPurchaseIntent接口创建托管商品订单
        Task<PurchaseIntentResult> task = Iap.getIapClient(this).createPurchaseIntent(req);
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                // 获取创建订单的结果
                Log.e(TAG, "应用内支付~调用支付页面");
                Status status = result.getStatus();
                if (status.hasResolution()) {
                    try {
                        // 启动IAP返回的收银台页面
                        status.startResolutionForResult(IapActivity.this, 8888);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(IapActivity.this, "应用内支付~购买失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {
            Log.e(TAG, "onActivityResult : 8888");
            if (data == null) {
                Log.e(TAG, "data is null");
                return;
            }
            // 调用parsePurchaseResultInfoFromIntent方法解析支付结果数据
            PurchaseResultInfo mPurchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            Log.e(TAG, "onActivityResult : ReturnCode : " + mPurchaseResultInfo.getReturnCode());
            switch (mPurchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    // 用户取消
                    Log.e(TAG, "onActivityResult : 用户取消");
                    break;
                case OrderStatusCode.ORDER_STATE_FAILED:
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    // 检查是否存在未发货商品
                    Log.e(TAG, "onActivityResult : 检查是否存在未发货商品");
                    Log.e(TAG, "onActivityResult : ErrMsg : " + mPurchaseResultInfo.getErrMsg());
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // 支付成功
                    Toast.makeText(IapActivity.this, "应用内支付~购买成功", Toast.LENGTH_SHORT).show();
                    String inAppPurchaseData = mPurchaseResultInfo.getInAppPurchaseData();
                    String inAppPurchaseDataSignature = mPurchaseResultInfo.getInAppDataSignature();
                    try {
                        InAppPurchaseData hasInAppPurchaseData = new InAppPurchaseData(inAppPurchaseData);
                        // 消耗商品
                        onConsumeOwnedPurchase(hasInAppPurchaseData.getPurchaseToken());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // 使用您应用的IAP公钥验证签名
                    // 若验签成功，则进行发货
                    // 若用户购买商品为消耗型商品，您需要在发货成功后调用consumeOwnedPurchase接口进行消耗
                    Log.e(TAG, "inAppPurchaseData : " + inAppPurchaseData);
                    Log.e(TAG, "inAppPurchaseDataSignature : " + inAppPurchaseDataSignature);
                    break;
                default:
                    break;
            }
        }
    }

    private void onConsumeOwnedPurchase(String purchaseToken) {
        // 构造ConsumeOwnedPurchaseReq对象
        ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
        req.setPurchaseToken(purchaseToken);
        // 消耗型商品发货成功后，需调用consumeOwnedPurchase接口进行消耗
        Task<ConsumeOwnedPurchaseResult> task = Iap.getIapClient(this).consumeOwnedPurchase(req);
        task.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {
            @Override
            public void onSuccess(ConsumeOwnedPurchaseResult result) {
                // 获取接口请求结果
                Toast.makeText(IapActivity.this, "消耗商品成功", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(IapActivity.this, "消耗商品失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}