package moe.UULogin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UULoginAutostartBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("moe.UULogin.autostart");
		context.startService(serviceIntent);

	}
}