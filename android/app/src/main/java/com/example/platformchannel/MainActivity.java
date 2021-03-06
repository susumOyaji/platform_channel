// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.platformchannel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
  private static final String BATTERY_CHANNEL = "samples.flutter.io/battery";
  private static final String CHARGING_CHANNEL = "samples.flutter.io/charging";
  private static final String CALL_CHANNEL = "samples.flutter.io/call";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);
    new EventChannel(getFlutterView(), CHARGING_CHANNEL).setStreamHandler(
        new StreamHandler() {
          private BroadcastReceiver chargingStateChangeReceiver;
          @Override
          public void onListen(Object arguments, EventSink events) {
            chargingStateChangeReceiver = createChargingStateChangeReceiver(events);
            registerReceiver(
                chargingStateChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
          }

          @Override
          public void onCancel(Object arguments) {
            unregisterReceiver(chargingStateChangeReceiver);
            chargingStateChangeReceiver = null;
          }
        }
    );

    new MethodChannel(getFlutterView(), CALL_CHANNEL).setMethodCallHandler(
        new MethodCallHandler() {
          @Override
          public void onMethodCall(MethodCall call, Result result) {
            if (call.method.equals("getBatteryLevel9")) {
              int batteryLevel = getBatteryLevel9();

              if (batteryLevel != -1) {
                result.success(batteryLevel);
              } else {
                result.error("UNAVAILABLE", "Battery level not available.", null);
              }
            } else {
              result.notImplemented();
            }
          }
        }
    );
  }

  private BroadcastReceiver createChargingStateChangeReceiver(final EventSink events) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
          events.error("UNAVAILABLE", "Charging status unavailable", null);
        } else {
          boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                               status == BatteryManager.BATTERY_STATUS_FULL;
          events.success(isCharging ? "charging" : "discharging");
        }
      }
    };
  }


  //APIレベル21（Android 5.0、Lollipop）
  private int getBatteryLevel9() {
    String phone = "7769942159";
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      TelecomManager telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
      //Intent intent = new Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      Intent intent = new Intent(Intent.ACTION_CALL);
      intent.setData(Uri.parse("tel:" + phone));
      startActivity(intent);
      //telecomManager.isInCall（）;
      return TelephonyManager.CALL_STATE_RINGING;//TelecomManager.EXTRA_STATE_IDLE;//getIntProperty(TelecomManager.BATTERY_PROPERTY_CAPACITY);
    } else {
      Intent intent = new ContextWrapper(getApplicationContext()).
          registerReceiver(null, new IntentFilter(Intent.ACTION_DIAL));
          //    Intent intent = new Intent(Intent.ACTION_DIAL);
          //intent.setData(Uri.parts("tel:" + phone));
          //Intent intent=new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+phone);
          //startActivity(intent);

      return 10;//(intent.getIntExtra(TelecomManager.EXTRA_STATE_IDLE));// / intent.getIntExtra(TelecomManager.EXTRA_SCALE, -1);
    }
  }


  //呼び出しを開始するcall（）メソッド
  /*
  public void call() {
    TelecomManager manager = (TelecomManager) getSystemService(TELECOM_SERVICE);
    PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(
            new ComponentName(getPackageName(),
                    MyConnectionService.class.getName()), "myConnectionServiceId");
    Bundle test = new Bundle();
    test.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
    manager.placeCall(Uri.parse("tel:" + "1212312312"), test);
  }
*/



  
  private int getBatteryLevel() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
      return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    } else {
      Intent intent = new ContextWrapper(getApplicationContext()).
          registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
      return (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
          intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    }
  }
}
