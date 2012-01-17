package moe.UULogin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class UULogin extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.main);
	}
	@Override
	protected void onStart() {
		Context context = this.getApplicationContext();
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("moe.UULogin.UULoginService");
		context.startService(serviceIntent);
		super.onStart();
	}
}