package xyz.sentrionic.harmony.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView

class Heart(var heartWhite: ImageView, var heartRed: ImageView) {

    fun toggleLike() : Int {

        val animationSet = AnimatorSet()

        if (heartRed.visibility == View.VISIBLE) {
            heartRed.scaleX = 0.1f
            heartRed.scaleY = 0.1f

            val scaleDownY = ObjectAnimator.ofFloat(heartRed, "scaleY", 1f, 0f)
            scaleDownY.duration = 300
            scaleDownY.interpolator = ACCELERATE_INTERPOLATOR

            val scaleDownX = ObjectAnimator.ofFloat(heartRed, "scaleX", 1f, 0f)
            scaleDownX.duration = 300
            scaleDownX.interpolator = ACCELERATE_INTERPOLATOR

            heartRed.visibility = View.GONE
            heartWhite.visibility = View.VISIBLE

            animationSet.playTogether(scaleDownY, scaleDownX)
        } else if (heartRed.visibility == View.GONE) {
            heartRed.scaleX = 0.1f
            heartRed.scaleY = 0.1f

            val scaleDownY = ObjectAnimator.ofFloat(heartRed, "scaleY", 0.1f, 1f)
            scaleDownY.duration = 300
            scaleDownY.interpolator = DECCELERATE_INTERPOLATOR

            val scaleDownX = ObjectAnimator.ofFloat(heartRed, "scaleX", 0.1f, 1f)
            scaleDownX.duration = 300
            scaleDownX.interpolator = DECCELERATE_INTERPOLATOR

            heartRed.visibility = View.VISIBLE
            heartWhite.visibility = View.GONE

            animationSet.playTogether(scaleDownY, scaleDownX)
        }

        animationSet.start()

        return if (heartRed.visibility == View.VISIBLE) 1 else -1
    }

    companion object {
        private val DECCELERATE_INTERPOLATOR = DecelerateInterpolator()
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
    }
}
