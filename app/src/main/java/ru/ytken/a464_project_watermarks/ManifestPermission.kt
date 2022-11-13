package ru.ytken.a464_project_watermarks

import android.Manifest
import android.content.Context
import android.widget.Toast
import com.google.android.material.internal.ContextUtils.getActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import ru.ytken.a464_project_watermarks.ui.MainActivity

object ManifestPermission {

    fun checkPermission(context: Context) {
        Dexter.withActivity(getActivity(context) as MainActivity)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(context,  getActivity(context)?.getString(R.string.allPermission), Toast.LENGTH_SHORT).show()
                        }
                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(context, getActivity(context)?.getString(R.string.denyPermission), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                Toast.makeText(context, it.name, Toast.LENGTH_SHORT).show()
            }
            .check()
    }
}