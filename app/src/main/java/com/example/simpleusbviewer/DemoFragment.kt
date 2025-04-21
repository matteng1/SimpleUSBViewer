package com.example.simpleusbviewer

import android.hardware.usb.UsbDevice
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.example.simpleusbviewer.databinding.FragmentDemoBinding

/*
 * Simple(/primitive) Android USB Cam (/HDMI in dongle) viewer.
 * Plug and play (for me), 8-10MiB small when compiled.
 * 
 * 
 * Assembled by Mattias Englin
 */

class DemoFragment: CameraFragment(){
    private var mViewBinding: FragmentDemoBinding? = null


    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        if (mViewBinding == null) {
            mViewBinding = FragmentDemoBinding.inflate(inflater, container, false)
        }
        return mViewBinding?.root
    }

    override fun getCameraView(): IAspectRatio? {
        return AspectRatioTextureView(requireContext())
    }

    override fun getCameraViewContainer(): ViewGroup? {
        return mViewBinding?.cameraViewContainer
    }


    override fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(1920)
            .setPreviewHeight(1080)
            .setRenderMode(CameraRequest.RenderMode.OPENGL) // camera render mode
            .setDefaultRotateType(RotateType.ANGLE_0) // rotate camera image when opengl mode
            .setAudioSource(CameraRequest.AudioSource.SOURCE_AUTO) // set audio source
            .setAspectRatioShow(true) // aspect render,default is true
            .setCaptureRawImage(false) // capture raw image picture when opengl mode
            .setRawPreviewData(false)  // preview raw image when opengl mode
            .create()
    }

    // camera open status callback
    override fun onCameraState(self: MultiCameraClient.ICamera,
                               code: ICameraStateCallBack.State,
                               msg: String?) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> handleCameraOpened()
            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError(msg)
        }
    }

    override fun getGravity(): Int = Gravity.TOP


    override fun getDefaultCamera(): UsbDevice? {
        return super.getDefaultCamera()
    }

    private fun handleCameraError(msg: String?) {
        ToastUtils.show("camera opened error: $msg")
    }

    private fun handleCameraClosed() {
        ToastUtils.show("camera closed success")
    }

    private fun handleCameraOpened() {
        ToastUtils.show("camera opened success")
    }

}
