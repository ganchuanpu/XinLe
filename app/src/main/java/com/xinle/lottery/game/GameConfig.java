package com.xinle.lottery.game;

import android.app.Activity;
import android.text.TextUtils;

import com.xinle.lottery.data.Lottery;
import com.xinle.lottery.data.Method;

/**
 * 配置不同玩法对应的处理类
 * Created by Alashi on 2016/2/16.
 */
public class GameConfig {
    private static final String TAG = "GameConfig";

    public static Game createGame(Activity activity, Method method, Lottery lottery) {
        //Log.d("Config", "createGame: " + GsonHelper.toJson(method));
        String name = method.getNameEn();
        if (!TextUtils.isEmpty(name)) {
            switch (name) {
                case "houerdaxiaodanshuang":
                case "housandaxiaodanshuang":
                case "qianerdaxiaodanshuang":
                case "qiansandaxiaodanshuang":
                case "wuxinghezhidaxiaodanshuang":
                case "teshuhaoma":
                case "baozi":
                case "shunzi":
                case "duizi":
                case "wq":
                case "wb":
                case "ws":
                case "wg":
                case "qb":
                case "qs":
                case "qg":
                case "bs":
                case "bg":
                case "sg":
                case "dxds":
                case "longhu":
                    return new TextGame(activity, method, lottery);
                case "hezhi":
                case "houerhezhi":
                case "qianerhezhi":
                    switch (method.getId()) {
                        case 75:
                        case 76:
                        case 77:
                        case 80:
                        case 140:
                        case 154:
                            return new SpecialTwoGame(activity, method, lottery);
                        case 175:
                            return new Pk10CommonGame(activity, method, lottery);
                        default:
                            return new SpecialOneGame(activity, method, lottery);
                    }
                /*if(method.getId()==75||method.getId()==154||method.getId()==80||method.getId()==77||method.getId()
                ==76){
                    return new SpecialTwoGame(activity,method,lottery);
                }else{
                    return new SpecialOneGame(activity,method,lottery);
                }*/
                case "danshi":
                case "ds":
                case "zusandanshi":
                case "zuliudanshi":
                case "houerdanshi":
                case "qianerdanshi":
                    return new DsGame(activity, method, lottery);
            }
            
            switch (name + method.getId())
            {
                case "zhixuankuadu198":
                case "zuxuanhezhi197":
                case "zhixuanhezhi196":
                case "zuxuanfushi195":
                case "zu3fushi181":
                case "zu6fushi182":
                case "zuxuanhezhi184":
                case "zhixuanhezhi183":
                case "zuxuan24194":
                case "zuxuan12193":
                case "zuxuan6192":
                case "zuxuan4191":
                case "zhixuankuadu185":
                case "wumaquweisanxing38":
                case "simaquweisanxing39":
                case "housanquweierxing55":
                case "qiansanquweierxing40":
                case "wumaqujiansanxing41":
                case "simaqujiansanxing42":
                case "housanqujianerxing56":
                case "qiansanqujianerxing43":
                    return new ArbitraryGame(activity, method, lottery);
                case "renxuanyi86":
                case "renxuaner87":
                case "renxuansan88":
                case "renxuansi89":
                case "renxuanwu90":
                case "renxuanliu91":
                case "renxuanqi92":
                case "renxuanba93":
                case "rx2418":
                case "rx3419":
                case "rx4420":
                case "rx5421":
                case "rx6464":
                case "rx7465":
                case "rx8466":
                    return new DsGame(activity, method, lottery);
            }
            switch (name + method.getId())
            {
                case "zhixuankuadu198":
                case "zuxuanhezhi197":
                case "zhixuanhezhi196":
                case "zuxuanfushi195":
                case "zu3fushi181":
                case "zu6fushi182":
                case "zuxuanhezhi184":
                case "zhixuanhezhi183":
                case "zuxuan24194":
                case "zuxuan12193":
                case "zuxuan6192":
                case "zuxuan4191":
                case "zhixuankuadu185":
                case "wumaquweisanxing38":
                case "simaquweisanxing39":
                case "housanquweierxing55":
                case "qiansanquweierxing40":
                case "wumaqujiansanxing41":
                case "simaqujiansanxing42":
                case "housanqujianerxing56":
                case "qiansanqujianerxing43":
                    return new ArbitraryGame(activity, method, lottery);
                case "renxuanyi86":
                case "renxuaner87":
                case "renxuansan88":
                case "renxuansi89":
                case "renxuanwu90":
                case "renxuanliu91":
                case "renxuanqi92":
                case "renxuanba93":
                case "rx2439": //快乐十分 任选 By Ace
                case "rx3440":
                case "rx4441":
                case "rx5442":
                case "rx6459":
                case "rx7460":
                    return new DsGame(activity, method, lottery);
            }
            
            //山东11选5，除“定单双, SDDDS”
            switch (lottery.getSeriesId())
            {
                case 2://山东11选5
                    if (name.equals("dingdanshuang"))
                    {
                        return new SdddsGame(activity, method, lottery);
                    } else
                    {
                        return new SyxwCommonGame(activity, method, lottery);
                    }
                case 1://重庆时时彩
                    return new SscCommonGame(activity, method, lottery);
                case 4://苹果快三分分彩
                    return new KsCommonGame(activity, method, lottery);
                case 5://苹果极速PK10
                    return new Pk10CommonGame(activity, method, lottery);
                case 3://苹果极速3D
                    return new Fc3dCommonGame(activity, method, lottery);
                case 8://快乐十二
                    return new Kl12CommonGame(activity, method, lottery);
                case 9: //快乐十分
                    return new Kl10CommonGame(activity, method, lottery);
                default:
                    break;
            }
        }
        return new NonsupportGame(activity, method, lottery);
    }
    
    public static final int DS_TYPE_SSC = 1;
    public static final int DS_TYPE_SYXW = 2;
    public static final int DS_TYPE_PK10 = 3;
    public static final int DS_TYPE_KL10 = 4;
    public static final int DS_TYPE_KL12 = 5;
    
    /**
     * 特定彩种，单选玩法的数字类型
     */
    public static int getDsType(Method method, Lottery lottery)
    {
        switch (lottery.getSeriesId())
        {
            case 2://山东11选5
                return DS_TYPE_SYXW;
            case 1://重庆时时彩
            case 3://苹果极速3D
                return DS_TYPE_SSC;
            case 5://苹果极速PK10
                return DS_TYPE_PK10;
            case 8:
                return DS_TYPE_KL12;
            case 9://快乐十分
                return DS_TYPE_KL10;
            default:
                return -1;
        }
    }
}
