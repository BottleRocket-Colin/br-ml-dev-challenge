package com.br.ml.brpathfinder.onboarding

import androidx.annotation.NonNull
import com.br.ml.brpathfinder.R
import com.cleveroad.slidingtutorial.Direction
import com.cleveroad.slidingtutorial.PageSupportFragment
import com.cleveroad.slidingtutorial.TransformItem

class OnboardingSettingsVibrationInstructionsFragment : PageSupportFragment() {
    override fun getLayoutResId(): Int {
        return R.layout.fragment_page_onboarding_vibration_instruction
    }

    @NonNull
    override fun getTransformItems(): Array<TransformItem> {
        return arrayOf(
                TransformItem.create(R.id.ivFirstImage, Direction.RIGHT_TO_LEFT, 0.2f)
        )
    }
}