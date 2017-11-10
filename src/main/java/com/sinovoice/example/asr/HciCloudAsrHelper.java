package com.sinovoice.example.asr;

import java.io.*;

import com.sinovoice.example.sys.AccountInfo;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.Session;
import com.sinovoice.hcicloudsdk.common.asr.AsrConfig;
import com.sinovoice.hcicloudsdk.common.asr.AsrGrammarId;
import com.sinovoice.hcicloudsdk.common.asr.AsrInitParam;
import com.sinovoice.hcicloudsdk.common.asr.AsrRecogResult;
import com.sinovoice.hcicloudsdk.api.HciCloudSys;
//import com.sinovoice.hcicloudsdk.common.asr.VoiceCheckResult;
import com.sinovoice.hcicloudsdk.api.HciLibPath;
import com.sinovoice.hcicloudsdk.api.asr.HciCloudAsr;
import org.springframework.web.multipart.MultipartFile;

public class HciCloudAsrHelper {
    String sPath = System.getProperty("user.dir");
    private static HciCloudAsrHelper mInstance;
    private String mCapKey = AccountInfo.getInstance().getCapKey();

    private String recognizeResult = null;

    static {
		String arch = System.getProperty("sun.arch.data.model");
    	String win_ver = "\\win_x86";
    	if (arch.equals("64")) {
			win_ver = "\\win_x64";
		}
		// 灵云sdk中dll文件目录
		String libPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\hcicloudasr\\libs" + win_ver;
		// 指定dll文件路径，顺序表示加载顺序；
		// 如果使用asr.local.grammar.chinese.v4能力，需要把mkl库放在工作目录
		// dialog能力，需要hci_nlu.dll、hci_nlu_local_recog和iTheinDialog.dll
		String asrLibPath[] = new String[] {
				libPath + "/jtspeex.dll", 
				libPath + "/jtopus.dll",
				libPath + "/libhci_curl.dll", 
				libPath + "/hci_sys.dll",
				libPath + "/hci_asr.dll",
				libPath + "/hci_asr_jni.dll", 
				libPath + "/hci_asr_cloud_recog.dll",
				libPath + "/iSpeakDNNLite.dll",
				libPath + "/hci_asr_local_ft_recog.dll",
				libPath + "/iSpeakGrmDNNLite.dll",
				libPath + "/hci_asr_local_recog.dll",
				libPath + "/iThinkDialog.dll",
				libPath + "/hci_nlu.dll",
				libPath + "/hci_nlu_local_recog.dll",
				};
		// 再加载hci_asr.dll
		HciLibPath.setAsrLibPath(asrLibPath);
    }
    
    private HciCloudAsrHelper() {
    }

    public static HciCloudAsrHelper getInstance() {
        if (mInstance == null) {
            mInstance = new HciCloudAsrHelper();
        }
        return mInstance;
    }

    /**
     * Asr引擎初始化 辅助工具 : AsrInitParam:该类的实例通过addParam(key, value)的方式添加Asr初始化的参数,
     * 再通过getStringConfig() 获取初始化时需要的字符串参数 config 初始化方法:
     * HciCloudAsr.hciAsrInit(config)
     */
    public int init() {
        // 构造Asr初始化的帮助类的实例
    	//Constructs an instance of the helper class that Asr initializes
        AsrInitParam AsrInitParam = new AsrInitParam();

        // 获取App应用中的lib的路径,放置能力所需资源文件。如果使用/data/data/packagename/lib目录,需要添加android_so的标记
        //Get the path of the lib in the App application, and place the required resource file. If you use the / data / data / packagename / lib directory, you need to add the android_so tag
        String dataPath = sPath + "\\src\\main\\resources\\static\\hcicloudasr\\data";
        AsrInitParam.addParam(AsrInitParam.PARAM_KEY_DATA_PATH, dataPath);
        AsrInitParam.addParam(AsrInitParam.PARAM_KEY_FILE_FLAG, "none");
        
        // 此处演示初始化的能力为Asr.cloud.xiaokun, 用户可以根据自己可用的能力进行设置, 另外,此处可以传入多个能力值,并用;隔开
        // 如 "Asr.cloud.xiaokun;Asr.cloud.zhangNan;Asr.local.xiaoKun"
        AsrInitParam.addParam(AsrInitParam.PARAM_KEY_INIT_CAP_KEYS, mCapKey);

        // 调用初始化方法
        int initResult = HciCloudAsr.hciAsrInit(AsrInitParam.getStringConfig());

        return initResult;
    }

    /**
     * Asr的反初始化和系统的反初始化, 在Init方法成功后, 执行结束时需要调用反初始化, 否则会导致引擎没有停止运行, 在下一次运行时会出现返回
     * HCI_ERR_SYS_ALREADY_INIT 或其他错误信息
     */
    public int release() {
        // Asr反初始化
        int nRet = HciCloudAsr.hciAsrRelease();
        
        return nRet;
    }
	
	/*
	 * ASR本地识别（语法识别，语法类型为jsgf），
	 */
	public boolean Recog(String sAudioFile, String sSessionConfig) {
		System.out.println("...... LocalRecogGrammarJsgf ......");

		// 载入语音数据文件
		byte[] voiceData = getFileData(sAudioFile);
		if (null == voiceData) {
			System.out.println("Open input voice file" + sAudioFile + " error!");
			return false;
		}
		
		// 启动 ASR Session
		this.mCapKey = AccountInfo.getInstance().getCapKey();
		sSessionConfig += ",capkey=" + mCapKey + ",audioformat=pcm16k16bit,realtime=no";
		System.out.println("hciAsrSessionStart config: " + sSessionConfig);
		Session nSessionId = new Session();
		int errCode = HciCloudAsr.hciAsrSessionStart(sSessionConfig, nSessionId);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			System.out.println("hciAsrSessionStart return " + errCode);
			return false;
		}
		System.out.println("hciAsrSessionStart Success");

		AsrRecogResult asrResult = new AsrRecogResult();

		try {
			errCode = HciCloudAsr.hciAsrRecog(nSessionId, voiceData, null, null, asrResult);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			System.out.println("hciAsrRecog return " + errCode);
			HciCloudAsr.hciAsrSessionStop(nSessionId);
			return false;
		}

		System.out.println("hciAsrRecog Success");

		// 输出识别结果
		printAsrResult(asrResult);

		// 终止session
		errCode = HciCloudAsr.hciAsrSessionStop(nSessionId);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			System.out.println("hciAsrSessionStop return " + errCode);
			return false;
		}
		System.out.println("hciAsrSessionStop Success");
		
		return true;
	}

    public boolean Recog(MultipartFile uploadAudioFile, String sSessionConfig) {
        //bug-fix
	    recognizeResult = null;//reset

	    System.out.println("...... LocalRecogGrammarJsgf ......");

        // 载入语音数据文件
        byte[] voiceData = getUploadFileData(uploadAudioFile);
        if (null == voiceData) {
            System.out.println("Open input voice file" + uploadAudioFile.getName() + " error!");
            return false;
        }

        // 启动 ASR Session
        this.mCapKey = AccountInfo.getInstance().getCapKey();
        sSessionConfig += ",capkey=" + mCapKey + ",audioformat=pcm16k16bit,realtime=no";
        System.out.println("hciAsrSessionStart config: " + sSessionConfig);
        Session nSessionId = new Session();
        int errCode = HciCloudAsr.hciAsrSessionStart(sSessionConfig, nSessionId);
        if (HciErrorCode.HCI_ERR_NONE != errCode) {
            System.out.println("hciAsrSessionStart return " + errCode);
            return false;
        }
        System.out.println("hciAsrSessionStart Success");

        AsrRecogResult asrResult = new AsrRecogResult();

        try {
            errCode = HciCloudAsr.hciAsrRecog(nSessionId, voiceData, null, null, asrResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (HciErrorCode.HCI_ERR_NONE != errCode) {
            recognizeResult = HciCloudSys.hciGetErrorInfo(errCode);
            System.out.println(recognizeResult);
            HciCloudAsr.hciAsrSessionStop(nSessionId);
            return false;
        }

        System.out.println("hciAsrRecog Success");

        // 输出识别结果
        printAsrResult(asrResult);

        // 终止session
        errCode = HciCloudAsr.hciAsrSessionStop(nSessionId);
        if (HciErrorCode.HCI_ERR_NONE != errCode) {
            System.out.println("hciAsrSessionStop return " + errCode);
            return false;
        }
        System.out.println("hciAsrSessionStop Success");

        return true;
    }

	/*
	 * ASR实时识别
	 */
	public boolean RealtimeRecog(String sAudioFile, String sSessionConfig) {
		System.out.println("...... localRealtimeRecogFreetalk ......");

		// 载入语音数据文件
		byte[] voiceData = getFileData(sAudioFile);
		if (null == voiceData) {
			System.out.println("Open input voice file" + sAudioFile + " error!");
			return false;
		}

		// 配置为本地语法识别引擎，实时识别参数realtime=yes
		this.mCapKey = AccountInfo.getInstance().getCapKey();
		sSessionConfig += ",capkey=" + mCapKey + ",audioformat=pcm16k16bit,realtime=yes";
		// 启动 ASR Session
		System.out.println("hciAsrSessionStart config: " + sSessionConfig);
		Session nSessionId = new Session();
		int errCode = HciCloudAsr.hciAsrSessionStart(sSessionConfig, nSessionId);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			System.out.println("hciAsrSessionStart return " + errCode);
			return false;
		}
		System.out.println("hciAsrSessionStart Success");

		AsrRecogResult asrResult = new AsrRecogResult();
		int nPerLen = 6400; //0.2s
		int nLen = 0;

		while (nLen + nPerLen < voiceData.length) {
			if (voiceData.length - nLen <= nPerLen) {
				nPerLen = voiceData.length - nLen;
			}

			byte[] subVoiceData = new byte[nPerLen];
			System.arraycopy(voiceData, nLen, subVoiceData, 0, nPerLen);
			// 调用识别方法,将音频数据不短的传入引擎
			errCode = HciCloudAsr.hciAsrRecog(nSessionId, subVoiceData,	null, null, asrResult);
			if (errCode == HciErrorCode.HCI_ERR_ASR_REALTIME_END) {
				// 检测到末端点，
				errCode = HciCloudAsr.hciAsrRecog(nSessionId, null, null, null, asrResult);
				if (HciErrorCode.HCI_ERR_NONE == errCode) {
					// 输出识别结果
					printAsrResult(asrResult);
				}
				else {
					//识别失败，跳出循环
					System.out.println("hciAsrRecog return " + errCode);
					break;
				}
			}
			else if (errCode == HciErrorCode.HCI_ERR_ASR_REALTIME_WAITING
					|| errCode == HciErrorCode.HCI_ERR_ASR_REALTIME_NO_VOICE_INPUT) {
				//在连续识别的场景，忽略这两个情况，继续识别后面的音频。
				//HCI_ERR_ASR_REALTIME_WAITING （实时识别等待音频）含义是：还没有数据，或者是需要更多数据。
				//HCI_ERR_ASR_REALTIME_NO_VOICE_INPUT 含义是：没有检测到音频起点，即超过了vadHead的范围（此时可以让设备进入休眠状态）
				
				System.out.println("hciAsrRecog errCode:" + errCode);
				
				nLen += nPerLen;
			}
			else {
				//识别失败
				System.out.println("hciAsrRecog failed:" + errCode);
				break;
			}
	
			try {
				Thread.sleep(200); //模拟真实说话人语速，发送200ms数据后需等待200ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		if (errCode == HciErrorCode.HCI_ERR_ASR_REALTIME_WAITING || errCode == HciErrorCode.HCI_ERR_ASR_REALTIME_END) {
			// 检测到末端点，
			errCode = HciCloudAsr.hciAsrRecog(nSessionId, null, null, null, asrResult);
			if (HciErrorCode.HCI_ERR_NONE == errCode) {
				// 输出识别结果
				printAsrResult(asrResult);
			}
			else {
				//识别失败，跳出循环
				System.out.println("hciAsrRecog return " + errCode);
			}
		}
		// 终止session
		errCode = HciCloudAsr.hciAsrSessionStop(nSessionId);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			System.out.println("hciAsrSessionStop return " + errCode);
			return false;
		}
		System.out.println("hciAsrSessionStop Success");

		return true;
	}
		
	/**
	 * 获取指定Assert文件中的数据
	 * 
	 * @param fileName
	 * @return
	 */
	private byte[] getFileData(String fileName) {
		
		File fileSrc = new File(fileName);	
		if (!fileSrc.exists())
			return null;
		
		int size = (int)fileSrc.length();
		
		try {
			FileInputStream rd = new FileInputStream(fileSrc);
			byte[] data = new byte[size];
			
			rd.read(data);
			
			return data;			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
			return null;
		}
	}

	private byte[] getUploadFileData(MultipartFile file){

        try {
            FileInputStream rd = (FileInputStream) file.getInputStream();
            byte[] data = file.getBytes();

            rd.read(data);

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
            return null;
        }
	}
	
	// ////////////////////////////////////////////////////////////////////////
	
	/**
	 * 输出ASR识别结果
	 * 
	 * @param recogResult
	 *            识别结果
	 */
	private void printAsrResult(AsrRecogResult recogResult) {
		if (recogResult.getRecogItemList().size() < 1) {
			System.out.println("recognize result is null");
		}
		for (int i = 0; i < recogResult.getRecogItemList().size(); i++) {
			if (recogResult.getRecogItemList().get(i).getRecogResult() != null) {
				String utf8 = recogResult.getRecogItemList().get(i)
						.getRecogResult();
				recognizeResult = utf8;
				System.out.println("recognize result:" + utf8);
			} else {
				recognizeResult = null;
				System.out.println("recognize result is null");
			}
		}
	}

    public String getRecognizeResult() {
        return recognizeResult;
    }
}
