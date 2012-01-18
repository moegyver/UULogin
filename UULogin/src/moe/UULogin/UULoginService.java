package moe.UULogin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Vector;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

public class UULoginService extends Service implements OnSharedPreferenceChangeListener {

	private static int highestOctet = 130;
	private static long secondhighestOctetLower = 128;
	private static long secondhighestOctetUpper = 255;
	private Vector<String> whitelistedSSIDs = new Vector<String>();
	private Vector<String> blacklistedSSIDs = new Vector<String>();
	private static String URL = "https://netlogon.student.uu.se/";
	private BroadcastReceiver wifiStatusReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
				checkAndLogin();
			}
		}
	};
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerReceiver(this.wifiStatusReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		return START_STICKY;
	}
	protected void checkAndLogin() {
		WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		int upper = (ipAddress & 0xff);
		int lower = (ipAddress >> 8 & 0xff);
		if ((upper == highestOctet && lower > secondhighestOctetLower && lower < secondhighestOctetUpper || whitelistedSSIDs.contains(wifiInfo.getSSID())) && !blacklistedSSIDs.contains(wifiInfo.getSSID())) {
			login();
		}
	}
	private void login() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String username = sharedPrefs.getString("username", "");
		String password = sharedPrefs.getString("password", "");
		String usergroup = sharedPrefs.getString("usergroup", "");
		if (username.equals("") || this.isLoggedIn()) {
			// do nothing, no user set or already logged in
		} else {
			String data;
			try {
				data = URLEncoder.encode("action", "UTF-8") + "=" + URLEncoder.encode("Login", "UTF-8");
				data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
				data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
				data += "&" + URLEncoder.encode("usergroup", "UTF-8") + "=" + URLEncoder.encode(usergroup, "UTF-8");
				URL url = new URL(URL);
				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data);
				wr.flush();
				BufferedReader ir = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = ir.readLine()) != null) {
					if ((line.contains("Inloggad som") || line.contains("Logged in as")) && line.contains(usergroup) && line.contains(username)) {
						Context context = getApplicationContext();
						Toast toast = Toast.makeText(context, R.string.logged_in, Toast.LENGTH_SHORT);
						toast.show();
					} else if (line.contains("Inloggningen misslyckades") || line.contains("Login failed")) {
						Context context = getApplicationContext();
						Toast toast = Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				ir.close();
				wr.close();
			} catch (Exception e) {
				//System.out.println(e.getMessage());
				System.exit(1);
			}
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		this.checkAndLogin();
	}
	public boolean isLoggedIn() {
		try {
			URL url = new URL(URL);
			BufferedReader ir = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = ir.readLine()) != null) {
				if ((line.contains("Inloggad som") || line.contains("Logged in as"))) {
					return true;
				} else if (line.contains("Inloggningen misslyckades") || line.contains("Login failed")) {
					return false;
				}
			}
			ir.close();
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}