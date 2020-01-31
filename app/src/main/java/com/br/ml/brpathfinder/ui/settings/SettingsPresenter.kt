package com.br.ml.brpathfinder.ui.settings

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.br.ml.brpathfinder.R

class SettingsPresenter {

    interface Listener {
        fun onVibrateRadioChecked(isChecked: Boolean)
        fun onSoundRadioChecked(isChecked: Boolean)
        fun onBothVibrateAndSoundRadioChecked(isChecked: Boolean)
        fun onNoFeedbackRadioChecked(isChecked: Boolean)
    }

    class Container(root: View, val listener: Listener) {
        val feedbackRadioGroup: RadioGroup? =
            root.findViewById(R.id.settings_feedback_radio_group)
        val vibrateFeedbackRadioButton: RadioButton? =
            root.findViewById(R.id.settings_feedback_vibrate_radio_button)
        val soundFeedbackRadioButton: RadioButton? =
            root.findViewById(R.id.settings_feedback_sound_radio_button)
        val vibrateAndSoundFeedbackRadioButton: RadioButton? =
            root.findViewById(R.id.settings_feedback_vibrate_and_sound_radio_button)
        val noFeedbackRadioButton: RadioButton? =
            root.findViewById(R.id.settings_feedback_no_feedback_radio_button)
    }

    companion object {

        fun present(container: Container, viewModel: SettingsViewModel) {
            container.run {

                vibrateFeedbackRadioButton?.apply {
                    setOnCheckedChangeListener { _, isChecked ->
                        listener.onVibrateRadioChecked(isChecked)
                    }
                    this.isChecked = viewModel.vibrateRadioButtonChecked
                }

                soundFeedbackRadioButton?.apply {
                    setOnCheckedChangeListener { _, isChecked ->
                        listener.onSoundRadioChecked(isChecked)
                    }
                    this.isChecked = viewModel.soundRadioButtonChecked
                }

                vibrateAndSoundFeedbackRadioButton?.apply {
                    setOnCheckedChangeListener { _, isChecked ->
                        listener.onBothVibrateAndSoundRadioChecked(isChecked)
                    }
                    this.isChecked = viewModel.bothVibrateAndSoundButtonChecked
                }

                noFeedbackRadioButton?.apply {
                    setOnCheckedChangeListener { _, isChecked ->
                        listener.onNoFeedbackRadioChecked(isChecked)
                    }
                    this.isChecked = viewModel.noFeedbackButtonChecked
                }
            }
        }
    }
}
