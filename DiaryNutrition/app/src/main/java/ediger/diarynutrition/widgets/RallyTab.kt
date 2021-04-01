package ediger.diarynutrition.widgets

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import ediger.diarynutrition.R
import kotlinx.android.synthetic.main.tabs_rally.view.*


class RallyTab @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var viewPager: ViewPager2? = null
    var previousClickedPosition = 0
    var lastClickedPosition = 0

    private val transition by lazy {
        AutoTransition().apply {
            excludeTarget(textView, true)
        }.apply {
            interpolator = FastOutSlowInInterpolator()
            duration = 300
        }
    }

    private val titleAlphaAnimator by lazy {
        ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f).apply {
            startDelay = duration  * 1 / 3
            duration = 300
        }
    }

    private val titleSlideAnimator by lazy {
        val tvWidth = resources.displayMetrics.widthPixels - ( image1.width * 5 )
        ObjectAnimator.ofFloat(
                textView, "translationX", tvWidth.toFloat(), 0f
        ).apply {
            interpolator = FastOutSlowInInterpolator()
            duration = 300
        }
    }

    private val tabNames = listOf(
            resources.getString(R.string.tab_products),
            resources.getString(R.string.tab_favorite),
            resources.getString(R.string.tab_user)
    )

    init {
        View.inflate(context, R.layout.tabs_rally, this)

        image1.setOnClickListener {
            viewPager?.setCurrentItem(0, true)
        }

        image2.setOnClickListener {
            viewPager?.setCurrentItem(1, true)
        }

        image3.setOnClickListener {
            viewPager?.setCurrentItem(2, true)
        }
    }

    fun clickedItem(position: Int) {
        val flow = findViewById<Flow>(R.id.flow)
        val refs = flow.referencedIds.toMutableList()

        if(refs.size < position){
            viewPager?.currentItem = 0
            return
        }

        TransitionManager.beginDelayedTransition(cl, transition)
        previousClickedPosition = lastClickedPosition
        lastClickedPosition = position


        if (lastClickedPosition != previousClickedPosition) {
            textView.alpha = 0f

            refs.remove(R.id.textView)

            refs.filterIndexed { index, _ -> index != position }
                    .forEach {
                        findViewById<MaterialButton>(it).iconTint =
                                ColorStateList.valueOf(
                                        ContextCompat.getColor(context, R.color.tabsScrollColor)
                                )
                    }

            findViewById<MaterialButton>(refs[position]).iconTint =
                    ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.onBarPrimary)
                    )

            findViewById<TextView>(R.id.textView).setTextColor(
                    ContextCompat.getColor(context, R.color.onBarPrimary)
            )

            requestLayout()

            textView.text = tabNames[position]

            if (previousClickedPosition < lastClickedPosition) {
                val set = AnimatorSet()
                set.playTogether(titleAlphaAnimator, titleSlideAnimator)
                set.start()
            } else {
                titleAlphaAnimator.start()
            }

            refs.add(position + 1, R.id.textView)
            flow.referencedIds = refs.toIntArray()
            flow.removeView(stub)
        }

    }

    fun setUpWithViewPager(viewPager: ViewPager2?) {
        this.viewPager = viewPager
        //this.viewPager?.swipeEnabled = true

        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                clickedItem(position)
            }
        })
    }

}