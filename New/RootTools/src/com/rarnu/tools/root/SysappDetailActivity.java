package com.rarnu.tools.root;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rarnu.tools.root.api.LogApi;
import com.rarnu.tools.root.base.ActivityIntf;
import com.rarnu.tools.root.common.SysappInfo;
import com.rarnu.tools.root.comp.AlertDialogEx;
import com.rarnu.tools.root.comp.TitleBar;
import com.rarnu.tools.root.utils.ApkUtils;

public class SysappDetailActivity extends Activity implements ActivityIntf, OnClickListener {

	// [region] field define
	TitleBar tbTitle;
	ImageView appIcon;
	TextView appName;
	TextView appPath;
	TextView appVersion;
	Button btnDelete;

	TextView tvPathDetail;
	TextView tvOdexDetail;
	TextView tvFileSizeDetail;
	TextView tvDataPathDetail;
	TextView tvSharedIdDetail;
	TextView tvDataSizeDetail;

	// [/region]

	// [region] variable define
	SysappInfo info = null;
	PackageInfo pinfo = null;

	// [/region]

	// [region] life circle
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_sysapp_detail);
		init();
		showAppInfo();

	}

	// [/region]
	
	// [region] events
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnDelete:
			// 0:android|1.google|2:other

			String hintStr = "";
			switch (info.level) {
			case 0:
				hintStr = getResources().getString(R.string.delete_android_app);
				break;
			case 1:
				hintStr = getResources().getString(R.string.delete_google_app);
				break;
			case 2:
				hintStr = getResources().getString(R.string.delete_htc_app);
				break;
			case 3:
				hintStr = getResources().getString(R.string.delete_system_app);
				break;
			}

			// delete system app
			AlertDialogEx.showAlertDialogEx(this, getString(R.string.hint), hintStr, getString(R.string.ok),
					new AlertDialogEx.DialogButtonClickListener() {

						@Override
						public void onClick(View v) {
							deleteApp(GlobalInstance.backupBeforeDelete, GlobalInstance.alsoDeleteData);
						}
					}, getString(R.string.cancel), null);
			break;
		case R.id.btnLeft:
			finish();
			break;
		}

	}

	// [/region]

	// [region] business logic
	public void deleteApp(boolean backup, boolean deleteData) {
		// need delete app's data also

		if (backup) {
			ApkUtils.backupSystemApp(info.info.sourceDir);
		}

		LogApi.logDeleteSystemApp(info.info.packageName);

		boolean ret = ApkUtils.deleteSystemApp(info.info.sourceDir);
		if (!ret) {
			Toast.makeText(this, R.string.delete_fail, Toast.LENGTH_LONG).show();
			return;
		}

		if (deleteData) {
			ApkUtils.deleteSystemAppData(info.info.dataDir);
		}

		Intent inRet = new Intent();
		inRet.putExtra("needRefresh", true);
		setResult(RESULT_OK, inRet);
		finish();
	}

	public void showAppInfo() {
		info = GlobalInstance.currentSysapp;
		try {
			pinfo = GlobalInstance.pm.getPackageInfo(info.info.packageName, MODE_APPEND);
		} catch (NameNotFoundException e) {
			pinfo = null;
		}

		appIcon.setBackgroundDrawable(GlobalInstance.pm.getApplicationIcon(info.info));
		appName.setText(GlobalInstance.pm.getApplicationLabel(info.info));
		appVersion.setText(getResources().getString(R.string.version)
				+ (pinfo == null ? getResources().getString(R.string.unknown) : pinfo.versionName));

		tvPathDetail.setText(info.info.sourceDir.replace("/system/app/", ""));

		String odexPath = info.info.sourceDir.substring(0, info.info.sourceDir.length() - 3) + "odex";
		File fOdex = new File(odexPath);
		tvOdexDetail.setText(fOdex.exists() ? odexPath.replace("/system/app/", "") : getResources().getString(
				R.string.na));

		tvFileSizeDetail.setText(ApkUtils.getAppSize(info.info.sourceDir) + " KB "
				+ String.format("(%s)", fOdex.exists() ? "APK+ODEX" : "APK"));

		tvDataPathDetail.setText(info.info.dataDir.replace("/data/data/", ""));

		String dataSize = ApkUtils.getDataSize(info.info.dataDir);
		tvDataSizeDetail.setText(dataSize.equals("") ? getResources().getString(R.string.unknown) : dataSize + " KB");

		String sid = pinfo.sharedUserId;
		if (sid == null) {
			sid = "";
		}
		sid = sid.trim();
		tvSharedIdDetail.setText(sid.equals("") ? getResources().getString(R.string.na) : sid);
		if (!GlobalInstance.allowDeleteLevel0) {
			if (info.level == 0) {
				RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) btnDelete.getLayoutParams();
				rlp.height = 0;
				btnDelete.setLayoutParams(rlp);
				btnDelete.setEnabled(false);
			}
		}
	}

	// [/region]

	// [region] init
	@Override
	public void init() {
		mappingComp();
		initTitle();
		initSearchBar();
		initEvents();

	}

	@Override
	public void mappingComp() {
		tbTitle = (TitleBar) findViewById(R.id.tbTitle);
		appIcon = (ImageView) findViewById(R.id.appIcon);
		appName = (TextView) findViewById(R.id.appName);
		appVersion = (TextView) findViewById(R.id.appVersion);
		btnDelete = (Button) findViewById(R.id.btnDelete);

		tvPathDetail = (TextView) findViewById(R.id.tvPathDetail);
		tvOdexDetail = (TextView) findViewById(R.id.tvOdexDetail);
		tvFileSizeDetail = (TextView) findViewById(R.id.tvFileSizeDetail);
		tvDataPathDetail = (TextView) findViewById(R.id.tvDataPathDetail);
		tvSharedIdDetail = (TextView) findViewById(R.id.tvSharedIdDetail);
		tvDataSizeDetail = (TextView) findViewById(R.id.tvDataSizeDetail);
	}

	@Override
	public void initTitle() {
		tbTitle.setText(getString(R.string.sysapp_name));
		tbTitle.setLeftButtonText(getString(R.string.back));
		tbTitle.getLeftButton().setVisibility(View.VISIBLE);
	}

	@Override
	public void initSearchBar() {

	}

	@Override
	public void initEvents() {
		tbTitle.getLeftButton().setOnClickListener(this);
		btnDelete.setOnClickListener(this);
	}

	// [/region]
}
