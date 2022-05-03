package com.example.tools

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.messenger.R

class LoadingProgress(var context: Context) {

    var loadingProgress:AlertDialog

    init {
        val progress = AlertDialog.Builder(context ,R.style.WrapContentDialog)
        val view = View.inflate(context ,R.layout.loading_progress ,null)
        progress.setView(view)
        progress.setCancelable(false)
        loadingProgress = progress.create()
        loadingProgress.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        loadingProgress.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun show()
    {
        loadingProgress.show()
    }

    fun hide()
    {
        if (loadingProgress.isShowing)
        {
            loadingProgress.dismiss()
        }
    }
}