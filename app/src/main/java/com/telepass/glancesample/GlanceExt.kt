package com.telepass.glancesample

import android.content.res.Resources

fun Float.toPx() = this * Resources.getSystem().displayMetrics.density