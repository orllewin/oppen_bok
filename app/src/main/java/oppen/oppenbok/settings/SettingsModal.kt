package oppen.oppenbok.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import oppen.oppenbok.R

class SettingsModal(
    private val currentPage: Int,
    private val pageCount: Int,
    private val title: String?,
    private val creator: String?,
    private val onSettingsOption: (SettingsOption) -> Unit): BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_modal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val metadata = view.findViewById<AppCompatTextView>(R.id.book_metadata)

        val titleLabel = title ?: ""
        val creatorLabel = creator ?: ""
        metadata.text = "$titleLabel - $creatorLabel"

        val slider = view.findViewById<Slider>(R.id.page_slider)
        val pageNumberLabel = view.findViewById<AppCompatTextView>(R.id.page_number_label)
        pageNumberLabel.text = "$currentPage/$pageCount"

        val percentComplete = currentPage / (pageCount/100f)

        val sliderValue = percentComplete/100.0f
        slider.value = sliderValue
        slider.labelBehavior = LabelFormatter.LABEL_GONE

        slider.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            val percentValue = (slider.value * 100).toInt()
            val page = ((pageCount/100f) * percentValue).toInt()
            pageNumberLabel.text = "$page/$pageCount"
        })

        slider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener{
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                val sliderPercent = (slider.value * 100).toInt()
                val page = ((pageCount/100f) * sliderPercent).toInt()
                onSettingsOption(SettingsOption.PageOption(page))
                dismiss()
            }

        })

        val overflow = view.findViewById<AppCompatImageButton>(R.id.overflow)

        overflow.setOnClickListener {
            val popup = PopupMenu(requireContext(), overflow)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.popup_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId){
                    R.id.menu_open_new -> {
                        onSettingsOption(SettingsOption.NewOption)
                        dismiss()
                    }
                    R.id.menu_about -> {
                        onSettingsOption(SettingsOption.AboutOption)
                        dismiss()
                    }
                }
                true
            }
            popup.show()
        }

    }

    fun show(context: Context, manager: FragmentManager, tag: String?){

        show(manager, tag)

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            else -> vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}