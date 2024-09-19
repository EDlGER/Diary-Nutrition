package ediger.diarynutrition.usermeal

import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.navigation.fragment.NavHostFragment
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.FragmentMealUserBinding
import ediger.diarynutrition.util.clickWithThrottle

class UserMealFragment: Fragment() {

    private lateinit var binding: FragmentMealUserBinding

    private val viewModel: UserMealViewModel by viewModels()

    private lateinit var adapter: UserMealAdapter

    private lateinit var dragListItemManager: RecyclerViewDragDropManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMealUserBinding.inflate(inflater, container, false)

        initList()

        binding.fabAddMeal.clickWithThrottle {
            showAddMealDialog()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mealList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        observeNewMealName()
        observeOptionsDialogDismiss()
    }

    private fun initList() = with(binding) {
        dragListItemManager = RecyclerViewDragDropManager().apply {
            setDraggingItemShadowDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.material_shadow_z3) as NinePatchDrawable
            )
        }

        adapter = UserMealAdapter { mealId ->
            showMealOptionsDialog(mealId)
        }

        val wrappedAdapter = dragListItemManager.createWrappedAdapter(adapter)
        list.adapter = wrappedAdapter
        list.itemAnimator = DraggableItemAnimator()

        dragListItemManager.attachRecyclerView(list)
    }

    private fun observeNewMealName() {
        val currentEntry = NavHostFragment.findNavController(this)
                .getBackStackEntry(R.id.nav_user_meal)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                    && currentEntry.savedStateHandle.contains(AddMealDialog.ARG_MEAL_NAME_NEW)) {
                currentEntry.savedStateHandle.get<String>(AddMealDialog.ARG_MEAL_NAME_NEW)?.let { mealName ->
                    viewModel.insertMeal(mealName)
                    currentEntry.savedStateHandle.remove<String>(AddMealDialog.ARG_MEAL_NAME_NEW)
                }
            }
        }
        currentEntry.lifecycle.addObserver(observer)

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                currentEntry.lifecycle.removeObserver(observer)
            }
        })
    }

    private fun observeOptionsDialogDismiss() {
        val currentEntry = NavHostFragment.findNavController(this)
                .getBackStackEntry(R.id.nav_user_meal)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                    && currentEntry.savedStateHandle.contains(MealOptionsBottomDialog.ARG_DIALOG_DISMISSED)) {
                currentEntry.savedStateHandle.get<Boolean>(MealOptionsBottomDialog.ARG_DIALOG_DISMISSED)?.let {
                    adapter.notifyDataSetChanged()
                    currentEntry.savedStateHandle.remove<Boolean>(MealOptionsBottomDialog.ARG_DIALOG_DISMISSED)
                }
            }
        }
        currentEntry.lifecycle.addObserver(observer)

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                currentEntry.lifecycle.removeObserver(observer)
            }
        })
    }

    private fun showAddMealDialog() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_nav_user_meal_to_nav_dialog_add_meal)

    }

    private fun showMealOptionsDialog(mealId: Int) {
        val bundle = bundleOf(MealOptionsBottomDialog.ARG_MEAL_ID to mealId)
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_nav_user_meal_to_nav_dialog_meal_options, bundle)
    }

    override fun onPause() {
        dragListItemManager.cancelDrag()
        super.onPause()
    }

}