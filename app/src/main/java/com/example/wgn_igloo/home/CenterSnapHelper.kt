package com.example.wgn_igloo.home

import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class CenterSnapHelper : LinearSnapHelper() {
    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = super.calculateDistanceToFinalSnap(layoutManager, targetView) ?: IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            val startMargin = (layoutManager.width - targetView.width) / 2
            out[0] = out[0] + (targetView.left - startMargin)
        }
        return out
    }
}