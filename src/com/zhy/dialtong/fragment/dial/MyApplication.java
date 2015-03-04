package com.zhy.dialtong.fragment.dial;

import java.util.List;

import android.content.Intent;

import com.yulore.superyellowpage.app.ApplicationMap;
import com.yulore.superyellowpage.impl.YulorePageConfiguration;
import com.zhy.dialtong.ContactBean;

public class MyApplication extends android.app.Application{
	
private List<ContactBean> contactBeanList;//
	
	public List<ContactBean> getContactBeanList() {//
		return contactBeanList;
	}
	public void setContactBeanList(List<ContactBean> contactBeanList) {//
		this.contactBeanList = contactBeanList;
	}

	public void onCreate() {
		
		System.out.println("项目启动");
		
		Intent startService = new Intent(MyApplication.this, T9Service.class);
		startService(startService);
		
		/*YulorePageConfiguration configuration = new YulorePageConfiguration.Builder()
			.apiKey("<GuQcEvda30O9zJ7uWieKmAJibp4rfD9m>")//初始化APIKEY
			.apiSecret("<mUSkcqODwbf5kWnPL8GmkWKmJmwqxKH6zqAubzx7nudZWhWHmwoFGho3VQK1P2PzjDLjcR9ZVpPxhwdmjep6HBvErKFZsyinM03sivFMKzLpmEP9jSkAxqEiocuZBJz1XblsHCbUji3smpy7lc3aZEhCcncqYKzmvdRtl>")//初始化APISECRET
			.offlineFileDir("/sdcard/yulore/apikey_GuQc")
			.build(getApplicationContext());
		ApplicationMap.getInstance().init(configuration);*/
			
	}

}
