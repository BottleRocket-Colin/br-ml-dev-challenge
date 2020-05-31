package com.br.ml.brpathfinder.onboarding

import androidx.annotation.NonNull
import com.br.ml.brpathfinder.R
import com.cleveroad.slidingtutorial.Direction
import com.cleveroad.slidingtutorial.PageSupportFragment
import com.cleveroad.slidingtutorial.TransformItem

class OnboardingStartFragment : PageSupportFragment() {
    override fun getLayoutResId(): Int {
        return R.layout.fragment_page_onboarding_start
    }

    @NonNull
    override fun getTransformItems(): Array<TransformItem> {
        return arrayOf(
                TransformItem.create(R.id.ivFirstImage, Direction.LEFT_TO_RIGHT, 0.2f)//,
//                TransformItem.create(R.id.ivSecondImage, Direction.RIGHT_TO_LEFT, 0.06f),
//                TransformItem.create(R.id.ivThirdImage, Direction.LEFT_TO_RIGHT, 0.08f),
//                TransformItem.create(R.id.ivFourthImage, Direction.RIGHT_TO_LEFT, 0.1f),
//                TransformItem.create(R.id.ivFifthImage, Direction.RIGHT_TO_LEFT, 0.03f),
//                TransformItem.create(R.id.ivSixthImage, Direction.RIGHT_TO_LEFT, 0.09f),
//                TransformItem.create(R.id.ivSeventhImage, Direction.RIGHT_TO_LEFT, 0.14f),
//                TransformItem.create(R.id.ivEighthImage, Direction.RIGHT_TO_LEFT, 0.07f)
        )
    }
}