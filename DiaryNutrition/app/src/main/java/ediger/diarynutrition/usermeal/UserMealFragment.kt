package ediger.diarynutrition.usermeal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import ediger.diarynutrition.databinding.FragmentMealUserBinding

class UserMealFragment: Fragment() {

    private lateinit var binding: FragmentMealUserBinding

    private val viewModel: UserMealViewModel by viewModels()

    private lateinit var adapter: UserMealAdapter

    private lateinit var dragListItemManager: RecyclerViewDragDropManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMealUserBinding.inflate(inflater, container, false)

        initList(savedInstanceState)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mealList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    private fun initList(savedInstanceState: Bundle?) = with(binding) {
        dragListItemManager = RecyclerViewDragDropManager()

        adapter = UserMealAdapter()

        val wrappedAdapter = dragListItemManager.createWrappedAdapter(adapter)
        list.adapter = wrappedAdapter
        list.itemAnimator = DraggableItemAnimator()

        dragListItemManager.attachRecyclerView(list)
    }

    override fun onPause() {
        dragListItemManager.cancelDrag()
        super.onPause()
    }

}