package com.example.simpleusbviewer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent


import android.os.PowerManager
import android.Manifest
import android.net.Uri
import androidx.core.app.ActivityCompat
import com.jiangdg.ausbc.utils.Utils
import androidx.core.content.PermissionChecker
import android.provider.Settings

import androidx.appcompat.app.AlertDialog

import com.jiangdg.ausbc.utils.*

import com.example.simpleusbviewer.databinding.ActivityMainBinding
/*
 * Simple(/primitive) Android USB Cam (/HDMI in dongle) viewer.
 * Plug and play (for me), 8-10MiB small when compiled.
 * 
 * 
 * Assembled by Mattias Englin
 */

class MainActivity : AppCompatActivity() {
    private var mWakeLock: PowerManager.WakeLock? = null
    private lateinit var viewBinding: ActivityMainBinding

    companion object {
        private const val PERMISSIONS_REQUIRED = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        checkPermissionsAndSetupFragment()
    }

    private fun checkPermissionsAndSetupFragment() {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val missingPermissions = requiredPermissions.filter {
            PermissionChecker.checkSelfPermission(this, it) != PermissionChecker.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isNotEmpty()) {
            val shouldShowRationale = missingPermissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (shouldShowRationale) {
                showPermissionRationaleDialog(missingPermissions)
            } else {
                ActivityCompat.requestPermissions(
                    this, missingPermissions, PERMISSIONS_REQUIRED
                )
            }
        } else {
            setupDemoFragment()
        }
    }

    private fun showPermissionRationaleDialog(permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Camera and audio permissions are needed.")
            .setPositiveButton("Grant") { _, _ ->
                ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUIRED)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                ToastUtils.show("Required permissions not granted")
                finish()
            }
            .show()
    }

    private fun setupDemoFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, DemoFragment())
        transaction.commitAllowingStateLoss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSIONS_REQUIRED -> {
                val allGranted = grantResults.all { it == PermissionChecker.PERMISSION_GRANTED }

                if (allGranted) {
                    setupDemoFragment()
                } else {
                    val neverAskAgain = permissions.filterIndexed { index, permission ->
                        grantResults[index] == PermissionChecker.PERMISSION_DENIED &&
                                !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                    }.isNotEmpty()

                    if (neverAskAgain) {
                        showAppSettingsDialog()
                    } else {
                        ToastUtils.show("Required permissions not granted")
                        finish()
                    }
                }
            }
        }
    }

    private fun showAppSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Please grant the required permissions.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel") { _, _ ->
                ToastUtils.show("Required permissions not granted")
                finish()
            }
            .show()
    }

    override fun onStart() {
        super.onStart()
        acquireWakeLock()
    }

    override fun onStop() {
        super.onStop()
        releaseWakeLock()
    }

    private fun acquireWakeLock() {
        if (mWakeLock == null) {
            mWakeLock = Utils.wakeLock(this)
        }
    }

    private fun releaseWakeLock() {
        mWakeLock?.let {
            Utils.wakeUnLock(it)
            mWakeLock = null
        }
    }
}
