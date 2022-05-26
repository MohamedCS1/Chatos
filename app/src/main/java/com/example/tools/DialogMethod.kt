package com.example.tools

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.example.messenger.R

open class DialogMethod(var context: Context) {

    lateinit var dialog: AlertDialog.Builder
    lateinit var methodDialog: AlertDialog
    var dialogMethodClick:DialogMethodClick? = null


    fun show()
    {
        dialog = AlertDialog.Builder(context , R.style.WrapContentDialog)
        val view = View.inflate(context , R.layout.dilog_choice_method ,null)
        dialog.setView(view)
        dialog.setCancelable(true)
        methodDialog = dialog.create()
        methodDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        methodDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        methodDialog.show()
        val bu_camera = methodDialog.findViewById<ImageView>(R.id.bu_camera)
        val bu_storage = methodDialog.findViewById<ImageView>(R.id.bu_storage)

        bu_camera?.setOnClickListener {
            dialogMethodClick?.onClickDialogMethod(it.id)
            hide()
        }

        bu_storage?.setOnClickListener {
            dialogMethodClick?.onClickDialogMethod(it.id)
            hide()
        }
    }

    fun hide()
    {
        if (methodDialog.isShowing)
        {
            methodDialog.dismiss()
        }
    }

    fun setOnMethodClick(dialogMethodClick:DialogMethodClick)
    {
        this.dialogMethodClick = dialogMethodClick
    }
}

 interface DialogMethodClick{
    fun onClickDialogMethod(id:Int)
 }


