package com.mmgpt.shoplistgpt

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.mmgpt.shoplistgpt.PlanCategoryFragment
import com.mmgpt.shoplistgpt.PlanCategoriesFragment

class CategoryPagerAdapter(
    activity: FragmentActivity,
    private val viewModel: ShoppingListViewModel,
    private val planMode: Boolean = false
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int =
        (viewModel.categories.value?.size ?: 0) + 1

    // Category fragments represent indices and observe the latest list at that index.
    // The overview needs a separate identity because its Shop position changes with size.
    override fun getItemId(position: Int): Long {
        val size = viewModel.categories.value?.size ?: 0
        return when {
            planMode && position == 0 -> OVERVIEW_ID
            !planMode && position == size -> OVERVIEW_ID
            else -> (if (planMode) position - 1 else position).toLong()
        }
    }

    override fun containsItem(itemId: Long): Boolean =
        itemId == OVERVIEW_ID || itemId in 0L until (viewModel.categories.value?.size ?: 0).toLong()

    private companion object {
        const val OVERVIEW_ID = Long.MIN_VALUE
    }

    override fun createFragment(position: Int): Fragment =
        if (planMode) {
            if (position == 0) {
                PlanCategoriesFragment()
            } else {
                PlanCategoryFragment.newInstance(position - 1)
            }
        } else {
            val size = viewModel.categories.value?.size ?: 0
            if (position == size) {
                ShopCategoriesFragment()
            } else {
                CategoryFragment.newInstance(position)
            }
        }
}
