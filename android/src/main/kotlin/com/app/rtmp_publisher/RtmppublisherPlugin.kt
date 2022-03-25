package com.app.rtmp_publisher

import android.app.Activity
import android.os.Build
import android.util.Log

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.TextureRegistry
import io.flutter.embedding.engine.FlutterEngine

interface PermissionStuff {
  fun adddListener(listener: PluginRegistry.RequestPermissionsResultListener);
}

/** RtmpPublisherPlugin */
// class RtmpPublisherPlugin: FlutterPlugin, MethodCallHandler {
class RtmpPublisherPlugin: FlutterPlugin, ActivityAware {
  val TAG = "RtmppublisherPlugin"

  private var methodCallHandler: MethodCallHandlerImplNew? = null
  private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    Log.v(TAG, "onAttachedToEngine $flutterPluginBinding")
    this.flutterPluginBinding = flutterPluginBinding

    // channel = MethodChannel(flutterPluginBinding.binaryMessenger, "rtmp_publisher")
    // channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    Log.v(TAG, "onDetachedFromEngine $binding")
    flutterPluginBinding = null
    // channel.setMethodCallHandler(null)
  }

  private fun maybeStartListening(
          activity: Activity,
          messenger: BinaryMessenger,
          permissionsRegistry: PermissionStuff,
          flutterEngine: FlutterEngine) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      // If the sdk is less than 21 (min sdk for Camera2) we don't register the plugin.
      return
    }
    methodCallHandler = MethodCallHandlerImplNew(
            activity,
            messenger,
            CameraPermissions(),
            permissionsRegistry,
            flutterEngine)
  }

  override fun onDetachedFromActivity() {
    Log.v(TAG, "onDetachedFromActivity")
    methodCallHandler?.stopListening()
    methodCallHandler = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    Log.v(TAG, "onAttachedToActivity $binding")
    flutterPluginBinding?.apply {
      maybeStartListening(
              binding.activity,
              binaryMessenger,
              object : PermissionStuff {
                override fun adddListener(listener: PluginRegistry.RequestPermissionsResultListener) {
                  binding.addRequestPermissionsResultListener(listener);
                }
              },
              flutterEngine
      )
    }
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  // private lateinit var channel : MethodChannel

  /*override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else {
      result.notImplemented()
    }
  }*/
}
