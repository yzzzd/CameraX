package com.nuryazid.camerax.util

import android.view.View

interface ClickPrevention : View.OnClickListener {
    override fun onClick(v: View?) {
        preventTwoClick(v)
    }

    fun preventTwoClick(view: View?) {
        view?.isEnabled = false
        view?.postDelayed({ view.isEnabled = true }, 2000)
    }

}