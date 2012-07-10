package com.rarnu.tools.root;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.rarnu.tools.root.api.LogApi;
import com.rarnu.tools.root.base.ActivityIntf;
import com.rarnu.tools.root.comp.TitleBar;
import com.rarnu.tools.root.utils.DirHelper;
import com.rarnu.tools.root.utils.FileUtils;
import com.rarnu.tools.root.utils.root.CommandResult;
import com.rarnu.tools.root.utils.root.RootUtils;

public class HostEditActivity extends Activity implements ActivityIntf, OnClickListener {

	// [region] field define
	TitleBar tbTitle;
	EditText etEditHosts;
	// [/region]
	
	// [region] const define

	private static final String PATH_HOSTS = "/system/etc/hosts";
	private static final String LOCAL_HOSTS = DirHelper.HOSTS_DIR + "hosts";
	// [/region]

	
	// [region] life circle
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_host_edit);
		init();
		loadHosts();
		LogApi.logEnterManualEditHosts();
	}
	// [/region]
	
	// [region] business logic
	private void loadHosts() {
		List<String> hosts = null;
		try {
			hosts = FileUtils.readFile(PATH_HOSTS);
			String hostsStr = "";
			if (hosts != null && hosts.size() != 0) {
				for (String s : hosts) {
					hostsStr += s + "\n";
				}
			}
			etEditHosts.setText(hostsStr);
		} catch (Exception e) {
			etEditHosts.setText("");
		}
	}

	private boolean saveHosts() {
		LogApi.logManualEditHosts();
		String hosts = etEditHosts.getText().toString();
		try {
			FileUtils.rewriteFile(LOCAL_HOSTS, hosts);
			String cmd = String.format("busybox cp %s /system/etc/", LOCAL_HOSTS);
			CommandResult result = RootUtils.runCommand(cmd, true);
			return result.error.equals("");
		} catch (Exception e) {
			return false;
		}
	}
	
	// [/region]

	
	// [region] events
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:
			if (!saveHosts()) {
				Toast.makeText(this, R.string.save_hosts_error, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, R.string.save_hosts_succ, Toast.LENGTH_LONG).show();
				finish();
			}
			break;
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
		etEditHosts = (EditText) findViewById(R.id.etEditHosts);
	}

	@Override
	public void initTitle() {
		tbTitle.setText(getString(R.string.manual_edit_hosts));
		tbTitle.setLeftButtonText(getString(R.string.back));
		tbTitle.setRightButtonText(getString(R.string.save));
		tbTitle.getLeftButton().setVisibility(View.VISIBLE);
		tbTitle.getRightButton().setVisibility(View.VISIBLE);

	}

	@Override
	public void initSearchBar() {

	}

	@Override
	public void initEvents() {
		tbTitle.getLeftButton().setOnClickListener(this);
		tbTitle.getRightButton().setOnClickListener(this);

	}
	
	// [/region]
}
