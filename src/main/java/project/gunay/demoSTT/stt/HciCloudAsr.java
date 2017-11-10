package project.gunay.demoSTT.stt;

import com.sinovoice.example.asr.HciCloudAsrHelper;
import com.sinovoice.example.sys.AccountInfo;
import com.sinovoice.example.sys.HciCloudSysHelper;
import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Gunay Gultekin  on 5/25/2017.
 */
public class HciCloudAsr {

    private static String capkey = null;

    public static String runTool(MultipartFile uploadedFile) {
        String sPath = System.getProperty("user.dir");

        /**
         * 加载用户信息工具类
         */
        AccountInfo mAccountInfo;

        /**
         * HciCloud帮助类，可完成灵云系统初始化，释放操作。
         */
        HciCloudSysHelper mHciCloudSysHelper;

        /**
         * ASR帮助类， 可完成ASR能力的初始化，开始合成，释放操作。
         */
        HciCloudAsrHelper mHciCloudAsrHelper;

        String resultText = "";

        mAccountInfo = AccountInfo.getInstance();
        boolean loadResult = mAccountInfo.loadAccountInfo();
        if (loadResult) {
            // 加载信息成功进入主界面
            System.out.println("加载灵云账号成功");
        } else {
            // 加载信息失败，显示失败界面
            resultText = "加载灵云账号失败！请在assets/AccountInfo.txt文件中填写正确的灵云账户信息，账户需要从www.hcicloud.com开发者社区上注册申请。";
            System.out.println(resultText);
            return resultText;
        }

        mHciCloudSysHelper = HciCloudSysHelper.getInstance();
        mHciCloudAsrHelper = HciCloudAsrHelper.getInstance();

        // 此方法是线程阻塞的，当且仅当有结果返回才会继续向下执行。
        // 此处只是演示合成能力用法，没有对耗时操作进行处理。需要开发者放入后台线程进行初始化操作
        // 必须首先调用HciCloudSys的初始化方法
        int sysInitResult = mHciCloudSysHelper.init();
        if (sysInitResult != HciErrorCode.HCI_ERR_NONE) {
//            resultText = "hci init error, error code = " + HciCloudSys.hciGetErrorInfo(sysInitResult);
            resultText = "hci init error, " + HciCloudSys.hciGetErrorInfo(sysInitResult);
            System.out.println(resultText);
            return resultText;
        } else {
            System.out.println("hci init success");
        }

        // 此方法是线程阻塞的，当且仅当有结果返回才会继续向下执行。
        // 此处只是演示合成能力用法，没有对耗时操作进行处理。需要开发者放入后台线程进行初始化操作
        // 只有HciCloudSys初始化成功后，才能调用asr的初始化方法
        int asrInitResult = mHciCloudAsrHelper.init();
        if (asrInitResult != HciErrorCode.HCI_ERR_NONE) {
            resultText = "asr init error " + HciCloudSys.hciGetErrorInfo(asrInitResult);
            System.out.println(resultText);
            return resultText;
        } else {
            System.out.println("asr init success");
        }


        // --------------------------以下为各种用法的完整流程演示-----------------------------

        // 读取用户的调用的能力
        capkey = mAccountInfo.getCapKey();
        boolean nRet;
        String sSessionConfig = "";

        //非实时识别，现云端语法能力，只支持非实时识别
        nRet = mHciCloudAsrHelper.Recog(uploadedFile,sSessionConfig);
        //get the result
        resultText = mHciCloudAsrHelper.getRecognizeResult();
        if(nRet) {
            System.out.println("Recog success");
        } else {
            System.out.println("Recog failed");
        }

        // 反初始化
        // 终止 ASR 能力
        int asrReleaseResult = mHciCloudAsrHelper.release();
        if (asrReleaseResult != HciErrorCode.HCI_ERR_NONE) {
            resultText = "hciAsrRelease failed:" + HciCloudSys.hciGetErrorInfo(asrReleaseResult);
            System.out.println(resultText);
            mHciCloudSysHelper.release();
            return resultText;
        } else {
            System.out.println("hciAsrRelease success");
        }

        // 终止 灵云 系统
        int sysReleaseRet = mHciCloudSysHelper.release();
        if(HciErrorCode.HCI_ERR_NONE != sysReleaseRet) {
            System.out.println("hciRelease failed:" + HciCloudSys.hciGetErrorInfo(sysReleaseRet));
        }
        System.out.println("hciRelease Success");

        return resultText;
    }
}
