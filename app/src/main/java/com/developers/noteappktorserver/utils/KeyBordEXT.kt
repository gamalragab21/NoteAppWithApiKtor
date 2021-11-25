package com.developers.shopapp.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

 fun View.hideKeyboard() {
    val inputManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, 0)
}