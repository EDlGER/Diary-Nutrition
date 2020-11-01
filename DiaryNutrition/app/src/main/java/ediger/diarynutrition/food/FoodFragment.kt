package ediger.diarynutrition.food

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.FragmentTabBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FoodFragment : Fragment() {

    private lateinit var binding: FragmentTabBinding
    private val viewModel: FoodViewModel by activityViewModels()
    private val adapter = FoodPagingAdapter()
    private var searchJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTabBinding.inflate(inflater, container, false)

        binding.list.adapter = adapter
        binding.list.setHasFixedSize(false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        observeListQuery()
    }

    override fun onPause() {
        super.onPause()
        viewModel.queryValue.removeObservers(viewLifecycleOwner)
    }

    private fun observeListQuery() {
        val foodVariance = arguments?.get(VARIANCE) as FoodVariance

        viewModel.queryValue.observe(viewLifecycleOwner) { query ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                viewModel.searchFood(foodVariance)?.collectLatest {
                    adapter.isDefaultQuery = query.isEmpty() && foodVariance == FoodVariance.ALL
                    adapter.submitData(it)
                    binding.list.scrollToPosition(0)
                }
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val clickedItemId = item.order
        when (item.itemId) {
            R.integer.action_context_favorite_add ->
                viewModel.setFavoriteFood(clickedItemId, true)
            R.integer.action_context_favorite_remove ->
                viewModel.setFavoriteFood(clickedItemId, false)
            R.integer.action_context_delete ->
                viewModel.deleteFood(clickedItemId)
            R.integer.action_context_change -> {
                val dialog = AddFoodDialog().apply {
                    arguments = bundleOf(FoodViewModel.FOOD_ID to clickedItemId)
                }
                requireActivity().supportFragmentManager
                        .beginTransaction()
                        .add(android.R.id.content, dialog)
                        .commit()
            }
            else -> return super.onContextItemSelected(item)
        }
        return true

    }

    companion object {
        const val VARIANCE = "variance"
    }
}