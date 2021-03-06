package com.xinle.lottery.game;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinle.lottery.R;
import com.xinle.lottery.data.Lottery;
import com.xinle.lottery.data.Method;
import com.xinle.lottery.material.ConstantInformation;
import com.xinle.lottery.util.NumbericUtils;
import com.xinle.lottery.util.ToastUtils;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.xinle.lottery.game.GameConfig.DS_TYPE_KL12;
import static com.xinle.lottery.game.GameConfig.DS_TYPE_KL10;
import static com.xinle.lottery.game.GameConfig.DS_TYPE_PK10;
import static com.xinle.lottery.game.GameConfig.DS_TYPE_SSC;
import static com.xinle.lottery.game.GameConfig.DS_TYPE_SYXW;

/**
 * Created by Sakura on 2016/11/18.
 * 单式玩法
 */
public class DsGame extends Game {
    private static final String TAG = "DsGame";
    private static final int FLAG_CAL_DONE = 1;
    private boolean isCalculating = false;
    private int digit;
    private boolean hasIllegal;
    private ArrayList<String[]> codeList;

    private Button clear;
    private Button submit;
    private EditText codesInput;
    private LinearLayout mainLayout;
    private LinearLayout loadingLayout;

    private LinearLayout parentLayout;
    private TextView pickNoticeView;

    private ExecutorService executorService;
    private CalThread calThread;
    private Handler mHandler;

    public DsGame(Activity activity, Method method, Lottery lottery) {
        super(activity, method, lottery);
        switch (method.getId()) {
            case 86:
                digit = 1;
                break;
            case 4:
            case 11:
            case 87:
            case 94:
            case 96:
            case 127:
            case 126:
            case 128:
            case 129:
            case 396:
            case 398:
            case 418:
            case 422:
            case 425:
            case 445: //快乐十分 任选 By Ace
            case 446:
            case 439:
                digit = 2;
                break;
            case 1:
            case 2:
            case 3:
            case 8:
            case 9:
            case 10:
            case 88:
            case 95:
            case 97:
            case 123:
            case 124:
            case 125:
            case 142:
            case 143:
            case 144:
            case 397:
            case 399:
            case 419:
            case 423:
            case 424:
            case 443:
            case 444:
            case 440:
                digit = 3;
                break;
            case 6:
            case 89:
            case 351:
            case 420:
            case 488:
            case 489:
            case 441:
                digit = 4;
                break;
            case 7:
            case 90:
            case 421:
            case 492:
            case 493:
            case 442:
                digit = 5;
                break;
            case 91:
            case 464:
            case 459:
                digit = 6;
                break;
            case 92:
            case 465:
            case 460:
                digit = 7;
                break;
            case 93:
            case 466:
                digit = 8;
                break;
            default:
                Log.w(TAG, "DsGame: 不支持的method类型：" + method.getId());
                ToastUtils.showShortToast(getActivity(), "不支持的类型");
        }
        int type = GameConfig.getDsType(method, lottery);
        switch (type) {
            case DS_TYPE_SSC:
            case DS_TYPE_SYXW:
            case DS_TYPE_PK10:
            case DS_TYPE_KL10:
            case DS_TYPE_KL12:
                break;
            default:
                Log.w(TAG, "DsGame: 不支持的类型：method:" + method.getId() + ", lottery:" + lottery.getId());
                ToastUtils.showShortToast(getActivity(), "不支持的类型");
                break;
        }
        codeList = new ArrayList<>();
        setSingleNum(0);
        initThread();
    }

    @Override
    public void onInflate() {
        try {
            createPicklayout(this);
            /*java.lang.reflect.Method function = getClass().getMethod(method.getNameEn() +
            method.getId(), Game.class);
            function.invoke(null, this);*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("SscTextGame", "onInflate: " + "//" + method.getNameCn() + " " + method.getNameEn() + method.getId
                    () + " public static void " + method.getNameEn() + method.getId() + "(Game game) {}");
            Toast.makeText(topLayout.getContext(), "不支持的类型", Toast.LENGTH_LONG).show();
        }
    }

    private void initThread() {
        executorService = Executors.newSingleThreadExecutor();
        calThread = new CalThread();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case FLAG_CAL_DONE:
                        isCalculating = false;
                        loadingLayout.setVisibility(View.GONE);
                        submit.setEnabled(true);
                        codesInput.setText(getSubmitCodes());
                        setSingleNum(codeList.size());
                        pickNoticeView.callOnClick();
                        if (hasIllegal && !executorService.isShutdown())
                            ToastUtils.showShortToastLocation(activity, "您的注单存在错误/重复项，已为您优化注单。", Gravity.CENTER, 0, -300);
                }
            }
        };
    }

    private void calculate() {
        if (!isCalculating) {
            loadingLayout.setVisibility(View.VISIBLE);
            submit.setEnabled(false);
            executorService.execute(calThread);
        }
    }

    private void doCalculation() {
        isCalculating = true;
        verify();
        int type = GameConfig.getDsType(method, lottery);
        switch (type) {
            case DS_TYPE_SSC:
                codeList = NumbericUtils.delDupWithOrder(codeList);
                break;
            case DS_TYPE_SYXW:
            case DS_TYPE_PK10:
            case DS_TYPE_KL12:
            case DS_TYPE_KL10:
                codeList = NumbericUtils.delDup(codeList);
            default:
                break;
        }
        if (!activity.isFinishing())
            mHandler.sendEmptyMessage(FLAG_CAL_DONE);
    }

    private void verify() {
        hasIllegal = false;
        codeList.clear();
        if ("".equals(codesInput.getText().toString()))
            return;
        String[] codes;
        int type = GameConfig.getDsType(method, lottery);
        switch (type) {
            case DS_TYPE_SSC:
                codes = codesInput.getText().toString().split("[,，;；：:|｜.\n ]");
                break;
            default:
                codes = codesInput.getText().toString().split("[,，;；：:|｜.\n]");
                break;
        }

        for (String code : codes) {
            String[] strs;
            switch (type) {
                case DS_TYPE_SSC:
                    int length = code.length();
                    strs = new String[length];
                    for (int i = 0; i < length; i++)
                        strs[i] = String.valueOf(code.charAt(i));
                    break;
                default:
                    strs = code.split(" ");
                    if (NumbericUtils.hasDupString(strs)) {
                        hasIllegal = true;
                        continue;
                    }
                    break;
            }
            if (strs.length != digit) {
                hasIllegal = true;
                continue;
            }

            switch (type) {
                case DS_TYPE_SSC:
                    verifyNumber(strs, ConstantInformation.LEGAL_NUMBER_SSC);
                    break;
                case DS_TYPE_SYXW:
                    verifyNumber(strs, ConstantInformation.LEGAL_NUMBER_SYXW);
                    break;
                case DS_TYPE_PK10:
                    verifyNumber(strs, ConstantInformation.LEGAL_NUMBER_PK10);
                    break;
                case DS_TYPE_KL12:
                    verifyNumber(strs, ConstantInformation.LEGAL_NUMBER_KL12);
                    break;
                case DS_TYPE_KL10:
                    verifyNumber(strs, ConstantInformation.LEGAL_NUMBER_KL10);
                    break;
                default:
                    Log.w(TAG, "verify: 不支持的类型：" + type);
                    ToastUtils.showShortToast(getActivity(), "不支持的类型");
                    break;
            }
        }
        if (NumbericUtils.hasDupArray(codeList))
            hasIllegal = true;
    }

    private void verifyNumber(String[] strs, ArrayList<String> legals) {
        for (String str : strs) {
            if (!legals.contains(str)) {
                hasIllegal = true;
                return;
            }
        }
        switch (method.getId()) {
            //组三
            case 2:
            case 124:
            case 143:
                if (!NumbericUtils.isDupStrCountUnique(strs, 2)) {
                    hasIllegal = true;
                    return;
                }
                break;
            //组六
            case 3:
            case 125:
            case 144:
                if (NumbericUtils.hasDupString(strs)) {
                    hasIllegal = true;
                    return;
                }
        }
        codeList.add(strs);
    }

    public String getSubmitCodes() {
        StringBuilder builder = new StringBuilder();
        int type = GameConfig.getDsType(method, lottery);
        switch (type) {
            case DS_TYPE_SSC:
                for (int i = 0, size = codeList.size(); i < size; i++) {
                    for (int j = 0, length = codeList.get(i).length; j < length; j++) {
                        builder.append(codeList.get(i)[j]);
                    }
                    if (i < size - 1)
                        builder.append("|");
                }
                break;
            case DS_TYPE_SYXW:
            case DS_TYPE_PK10:
            case DS_TYPE_KL10:
            case DS_TYPE_KL12:
                for (int i = 0, size = codeList.size(); i < size; i++) {
                    for (int j = 0, length = codeList.get(i).length; j < length; j++) {
                        builder.append(codeList.get(i)[j]);
                        if (j < length - 1)
                            builder.append(" ");
                    }
                    if (i < size - 1)
                        builder.append("|");
                }
                break;
            default:
                Log.w(TAG, "getSubmitCodes: 不支持的类型：" + type);
                ToastUtils.showShortToast(getActivity(), "不支持的类型");
                break;
        }

        return builder.toString();
    }

    private View createDefaultPickLayout(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.single_layout, null, false);
    }

    private void createPicklayout(Game game) {
        View view = createDefaultPickLayout(game.getTopLayout());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ViewGroup topLayout = game.getTopLayout();
        topLayout.addView(view);
        game.setInputStatus(true);
        loadingLayout = (LinearLayout) view.findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);
        mainLayout = (LinearLayout) view.findViewById(R.id.main_layout);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codesInput.clearFocus();
            }
        });

        codesInput = (EditText) view.findViewById(R.id.input_multiline_text);
        codesInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context
                            .INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(codesInput.getWindowToken(), 0);
                    if (!isCalculating) {
                        calculate();
                    }
                }
            }
        });
        codesInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codesInput.setFocusable(true);
            }
        });

        clear = (Button) view.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codesInput.setText("");
                if (!isCalculating) {
                    calculate();
                }
            }
        });

        /*对GameFragment的View进行操作*/
        pickNoticeView = (TextView) activity.getWindow().getDecorView().findViewById(R.id.pick_notice);
        parentLayout = (LinearLayout) activity.getWindow().getDecorView().findViewById(R.id.parent_layout);
        parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codesInput.clearFocus();
            }
        });
        submit = (Button) activity.getWindow().getDecorView().findViewById(R.id.choose_done_button);
    }

    private class CalThread extends Thread {
        public volatile boolean exit = false;

        @Override
        public void run() {
            doCalculation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        calThread.exit = true;
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdownNow();
    }
}