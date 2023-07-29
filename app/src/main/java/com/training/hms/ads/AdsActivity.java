package com.training.hms.ads;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.ads.reward.Reward;
import com.huawei.hms.ads.reward.RewardAd;
import com.huawei.hms.ads.reward.RewardAdLoadListener;
import com.huawei.hms.ads.reward.RewardAdStatusListener;
import com.training.hms.R;

public class AdsActivity extends AppCompatActivity {

    private BannerView mBannerView;
    private RewardAd mRewardAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        // 初始化HUAWEI Ads SDK
        HwAds.init(this.getApplicationContext());
        mBannerView = findViewById(R.id.banner_view);
        initClickListener();
    }

    private void initClickListener() {
        findViewById(R.id.load_banner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取Banner广告
                onGetBannerView();
            }
        });

        findViewById(R.id.load_reward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 加载激励广告
                onLoadRewardAd();
            }
        });
    }

    /**
     * 获取Banner广告
     */
    private void onGetBannerView() {
        // 设置广告位ID和广告尺寸，"testw6vs28auh3"为测试专用的广告位ID
        mBannerView.setAdId(getString(R.string.banner_ads_id));
        mBannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_57);
        // 设置轮播时间间隔为60秒
        mBannerView.setBannerRefresh(60);
        // 创建广告请求，加载广告
        AdParam adParam = new AdParam.Builder().build();
        mBannerView.loadAd(adParam);
        mBannerView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // 广告加载成功时调用
            }

            @Override
            public void onAdFailed(int errorCode) {
                // 广告加载失败时调用
                Log.e("---Banner---", "load fail errorCode : " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // 广告打开时调用
            }

            @Override
            public void onAdClicked() {
                // 广告点击时调用
            }

            @Override
            public void onAdLeave() {
                // 广告离开应用时调用
            }

            @Override
            public void onAdClosed() {
                // 广告关闭时调用
            }
        });
    }

    /**
     * 加载激励广告
     */
    private void onLoadRewardAd() {
        mRewardAd = new RewardAd(this, getString(R.string.reward_ads_id));
        mRewardAd.loadAd(new AdParam.Builder().build(), new RewardAdLoadListener() {
            @Override
            public void onRewardedLoaded() {
                // 激励广告加载成功
                if (mRewardAd.isLoaded()) {
                    mRewardAd.show(AdsActivity.this, statusListener);
                }
            }

            @Override
            public void onRewardAdFailedToLoad(int errorCode) {
                // 激励广告加载失败
            }
        });
    }

    private final RewardAdStatusListener statusListener = new RewardAdStatusListener() {
        @Override
        public void onRewardAdClosed() {
            super.onRewardAdClosed();
            // 激励广告被关闭
        }

        @Override
        public void onRewardAdFailedToShow(int i) {
            super.onRewardAdFailedToShow(i);
            // 激励广告展示失败
        }

        @Override
        public void onRewardAdOpened() {
            super.onRewardAdOpened();
            // 激励广告被打开
        }

        @Override
        public void onRewarded(Reward reward) {
            super.onRewarded(reward);
            // 激励广告奖励达成
        }
    };
}