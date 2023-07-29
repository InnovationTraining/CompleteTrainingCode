package com.training.hms.ml;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;
import com.training.hms.R;
import com.training.hms.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public class MLActivity extends AppCompatActivity {
    private TextView mTvResult;
    private ImageView mIvOriginalImage;
    private ImageView mIvResultImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml);
        mTvResult = findViewById(R.id.text_result);
        mTvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        mIvOriginalImage = findViewById(R.id.original_image);
        mIvResultImage = findViewById(R.id.result_image);
        initClickListener();
    }

    private void initClickListener() {
        findViewById(R.id.text_analyse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTextAnalyse();
            }
        });
        findViewById(R.id.document_skew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDocumentSkew();
            }
        });
    }

    private void onTextAnalyse() {
        mTvResult.setText("");
        //方式二：使用自定义参数MLLocalTextSetting配置端侧文本分析器。
        MLLocalTextSetting setting = new MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                // 设置识别语种。
                .setLanguage("zh")
                .create();
        MLTextAnalyzer analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);
        mIvOriginalImage.setImageResource(R.mipmap.ml_image);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ml_image);
        // 通过bitmap创建MLFrame，bitmap为输入的Bitmap格式图片数据。
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        Task<MLText> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<MLText>() {
            @Override
            public void onSuccess(MLText text) {
                // 识别成功处理。
                onDealAnalyzerText(text);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 识别失败处理。
            }
        });
    }

    private void onDealAnalyzerText(MLText mlText) {
        if (null == mlText) {
            onInitText("文本识别结果为空");
            return;
        }
        List<MLText.Block> blockList = mlText.getBlocks();
        if (CollectionUtil.isEmpty(blockList)) {
            onInitText("文本识别结果为空");
            return;
        }
        for (MLText.Block block : blockList) {
            onInitText(block.getStringValue());
        }
    }

    private void onInitText(String string) {
        mTvResult.append(string);
    }

    private void onDocumentSkew() {
        mTvResult.setText("");
        MLDocumentSkewCorrectionAnalyzerSetting setting = new MLDocumentSkewCorrectionAnalyzerSetting.Factory().create();
        MLDocumentSkewCorrectionAnalyzer analyzer = MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
                .getDocumentSkewCorrectionAnalyzer(setting);

        mIvOriginalImage.setImageResource(R.mipmap.ml_image_skew);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ml_image_skew);
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        // asyncDocumentSkewDetect异步调用。
        Task<MLDocumentSkewDetectResult> detectTask = analyzer.asyncDocumentSkewDetect(frame);
        detectTask.addOnSuccessListener(new OnSuccessListener<MLDocumentSkewDetectResult>() {
            @Override
            public void onSuccess(MLDocumentSkewDetectResult detectResult) {
                // 检测成功。
                onInitText("文档校正检测成功");
                onSkewDetect(analyzer, frame, detectResult);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 检测失败。
                onInitText("文档校正检测失败");
            }
        });
    }

    private void onSkewDetect(MLDocumentSkewCorrectionAnalyzer analyzer, MLFrame frame, MLDocumentSkewDetectResult detectResult) {
        Point leftTop = detectResult.getLeftTopPosition();
        Point rightTop = detectResult.getRightTopPosition();
        Point leftBottom = detectResult.getLeftBottomPosition();
        Point rightBottom = detectResult.getRightBottomPosition();
        List<Point> coordinates = new ArrayList<>();
        coordinates.add(leftTop);
        coordinates.add(rightTop);
        coordinates.add(rightBottom);
        coordinates.add(leftBottom);
        MLDocumentSkewCorrectionCoordinateInput coordinateData = new MLDocumentSkewCorrectionCoordinateInput(coordinates);

        // asyncDocumentSkewCorrect异步调用。
        Task<MLDocumentSkewCorrectionResult> correctionTask = analyzer.asyncDocumentSkewCorrect(frame, coordinateData);
        correctionTask.addOnSuccessListener(new OnSuccessListener<MLDocumentSkewCorrectionResult>() {
            @Override
            public void onSuccess(MLDocumentSkewCorrectionResult refineResult) {
                // 校正成功。
                onInitText("\n");
                onInitText("文档校正成功");
                mIvResultImage.setImageBitmap(refineResult.getCorrected());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 校正失败。
                onInitText("\n");
                onInitText("文档校正成功");
            }
        });
    }


}