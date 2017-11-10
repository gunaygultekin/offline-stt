package com.sinovoice.example.asr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.sinovoice.example.sys.AccountInfo;
import com.sinovoice.example.sys.HciCloudSysHelper;
import com.sinovoice.example.asr.HciCloudAsrHelper;
import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.api.asr.HciCloudAsr;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.asr.AsrGrammarId;

public class HciCloudAsrMain {
	private static String capkey = null;
	private static String testdataPath = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
		
        mAccountInfo = AccountInfo.getInstance();
        boolean loadResult = mAccountInfo.loadAccountInfo();
        if (loadResult) {
            // 加载信息成功进入主界面
        	System.out.println("加载灵云账号成功");
        } else {
            // 加载信息失败，显示失败界面
        	System.out.println("加载灵云账号失败！请在assets/AccountInfo.txt文件中填写正确的灵云账户信息，账户需要从www.hcicloud.com开发者社区上注册申请。");
            return;
        }
        
        mHciCloudSysHelper = HciCloudSysHelper.getInstance();
        mHciCloudAsrHelper = HciCloudAsrHelper.getInstance();

        // 此方法是线程阻塞的，当且仅当有结果返回才会继续向下执行。
        // 此处只是演示合成能力用法，没有对耗时操作进行处理。需要开发者放入后台线程进行初始化操作
        // 必须首先调用HciCloudSys的初始化方法
        int sysInitResult = mHciCloudSysHelper.init();
        if (sysInitResult != HciErrorCode.HCI_ERR_NONE) {
        	System.out.println("hci init error, error code = " + sysInitResult);
            return;
        } else {
        	System.out.println("hci init success");
        }
        
        // 此方法是线程阻塞的，当且仅当有结果返回才会继续向下执行。
        // 此处只是演示合成能力用法，没有对耗时操作进行处理。需要开发者放入后台线程进行初始化操作
        // 只有HciCloudSys初始化成功后，才能调用asr的初始化方法
        int asrInitResult = mHciCloudAsrHelper.init();
        if (asrInitResult != HciErrorCode.HCI_ERR_NONE) {
        	System.out.println("asr init error " + asrInitResult);
            return;
        } else {
        	System.out.println("asr init success");
        }

        testdataPath = sPath + "\\testdata";
		//String testData = testdataPath + "\\sinovoice.pcm";
		String testData = testdataPath + "\\2a63ad291844acd3dc7a221ac15bab4f.wav";
		
		// 语法文件
		String sJsgfGrammar = testdataPath + "\\stock_10001.gram";
		String sWordlistGrammar = testdataPath + "\\wordlist_utf8.txt";

		// --------------------------以下为各种用法的完整流程演示-----------------------------

		// 读取用户的调用的能力
		capkey = mAccountInfo.getCapKey();
		boolean nRet;
		byte[] grammarData = null;
		String sGrammarConfig = "";
		AsrGrammarId grammarId = new AsrGrammarId();
		String sSessionConfig = "";
		if (capkey.contains("local.grammar")) {
			grammarData = getFileData(sJsgfGrammar);
			if (null == grammarData) {
				System.out.println("read grammar file" + sJsgfGrammar + "error!");
				return;
			}
			String strGrammarData = null;
			try {
				strGrammarData = new String(grammarData, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			sGrammarConfig += "grammartype=jsgf,isfile=no,capkey=asr.local.grammar.v4";
			int errCode = HciCloudAsr.hciAsrLoadGrammar(sGrammarConfig, strGrammarData, grammarId);
	    	if (errCode != HciErrorCode.HCI_ERR_NONE) {
	    		System.out.println("hciAsrLoadGrammar error:" + HciCloudSys.hciGetErrorInfo(errCode));
	            HciCloudAsr.hciAsrRelease();        
	            return;
	        } else {
	        	System.out.println("hciAsrLoadGrammar Success");
	        }
	    	sSessionConfig += "grammarid=" + grammarId.toString();
		}
		
		if (capkey.contains("cloud.grammar")) {
			//如果是云端语法识别，需要开发者通过开发者社区自行上传语法文件，并获得可以使用的ID
			sSessionConfig += "grammartype=id,grammarid=10439";
		}
		
		//非实时识别，现云端语法能力，只支持非实时识别
		nRet = mHciCloudAsrHelper.Recog(testData,sSessionConfig);
		if(nRet) {
			System.out.println("Recog success");
		} else {
			System.out.println("Recog failed");
		}
		
		//实时识别
		nRet = mHciCloudAsrHelper.RealtimeRecog(testData,sSessionConfig);
		if(nRet) {
			System.out.println("RealtimeRecog success");
		} else {
			System.out.println("RealtimeRecog failed");
		}
		
		// 反初始化
		// 终止 ASR 能力
        int asrReleaseResult = mHciCloudAsrHelper.release();
        if (asrReleaseResult != HciErrorCode.HCI_ERR_NONE) {
        	System.out.println("hciAsrRelease failed:" + asrReleaseResult);
        	mHciCloudSysHelper.release();
            return;
        } else {
        	System.out.println("hciAsrRelease success");
        }
        
		// 终止 灵云 系统
		int sysReleaseRet = mHciCloudSysHelper.release();
		if(HciErrorCode.HCI_ERR_NONE != sysReleaseRet) {
			System.out.println("hciRelease failed:" + sysReleaseRet);
		}
		System.out.println("hciRelease Success");
	}	
	
	/**
	 * 获取指定Assert文件中的数据
	 * 
	 * @param fileName
	 * @return
	 */
	private static byte[] getFileData(String fileName) {
		
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
}
