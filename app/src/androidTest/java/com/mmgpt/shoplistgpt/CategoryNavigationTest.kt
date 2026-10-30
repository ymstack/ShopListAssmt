package com.mmgpt.shoplistgpt

import android.content.Context
import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Dispatches touch sequences through the Activity, without Espresso's API 37 injector. */
@RunWith(AndroidJUnit4::class)
class CategoryNavigationTest {
    private lateinit var scenario: ActivityScenario<MainActivity>
    private var savedState: String? = null
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val categories = listOf(
        ShoppingCategory(10, "Alpha", listOf(ShoppingItem(100, "Apples"))),
        ShoppingCategory(30, "Beta", listOf(ShoppingItem(101, "Bread"))),
        ShoppingCategory(20, "Gamma", listOf(ShoppingItem(102, "Carrots")))
    )

    @Before fun setUp() {
        savedState = context.getSharedPreferences("state", Context.MODE_PRIVATE).getString("state", null)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            ViewModelProvider(activity)[ShoppingListViewModel::class.java].setCategories(categories)
        }
        settle()
    }

    @After fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        val editor = context.getSharedPreferences("state", Context.MODE_PRIVATE).edit()
        if (savedState == null) editor.remove("state") else editor.putString("state", savedState)
        editor.commit()
    }

    @Test fun planNavigatesToEveryCategory() = verifyCategories(plan = true, reordered = false)
    @Test fun shopNavigatesToEveryCategory() = verifyCategories(plan = false, reordered = false)
    @Test fun planNavigatesAfterReordering() = verifyCategories(plan = true, reordered = true)
    @Test fun shopNavigatesAfterReordering() = verifyCategories(plan = false, reordered = true)

    @Test fun planNavigatesWhileReorderDiffIsPending() = verifyPendingReorder(true)
    @Test fun shopNavigatesWhileReorderDiffIsPending() = verifyPendingReorder(false)

    @Test fun planIgnoresAVisibleCategoryRemovedFromModel() = verifyRemovedCategory(true)
    @Test fun shopIgnoresAVisibleCategoryRemovedFromModel() = verifyRemovedCategory(false)
    @Test fun planHandlesDuplicateNamesByIdentity() = verifyDuplicateNames(true)
    @Test fun shopHandlesDuplicateNamesByIdentity() = verifyDuplicateNames(false)
    @Test fun planHandlesEmptyAndSingleCategory() = verifyEmptyAndSingle(true)
    @Test fun shopHandlesEmptyAndSingleCategory() = verifyEmptyAndSingle(false)

    @Test fun planSingleTapDoesNotNavigate() = verifySingleTap(true)
    @Test fun shopSingleTapDoesNotNavigate() = verifySingleTap(false)

    private fun verifySingleTap(plan: Boolean) {
        openOverview(plan)
        tapVisibleView(R.id.tvName, twice = false)
        settle()
        scenario.onActivity { activity ->
            assertEquals(if (plan) 0 else 3, activity.findViewById<ViewPager2>(R.id.viewPager).currentItem)
        }
    }

    @Test fun planEditingAndShopPurchaseControlsStillWork() {
        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.btnPlan).performClick()
            val model = ViewModelProvider(activity)[ShoppingListViewModel::class.java]
            model.editCategory(0, "Fruit")
            model.editItem(model.categories.value!![0].items.single(), "Green apples")
            model.addItem(0, 1, "Temporary")
            model.deleteItem(model.categories.value!![0].items.last())
        }
        settle()
        doubleTapName("Fruit")
        settle()
        scenario.onActivity { activity ->
            val page = activity.supportFragmentManager.fragments.single { it.isResumed }.requireView()
            assertEquals("Green apples", descendants(page).filterIsInstance<TextView>()
                .single { it.id == R.id.tvName }.text.toString())
            page.findViewById<View>(R.id.chkSelectAll).performClick()
            assertTrue(ViewModelProvider(activity)[ShoppingListViewModel::class.java]
                .categories.value!![0].items.single().isChecked)
        }
        settle()
        scenario.onActivity { activity ->
            activity.supportFragmentManager.fragments.single { it.isResumed }.requireView()
                .findViewById<View>(R.id.chkSelectAll).performClick()
            val home = activity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).menu.findItem(R.id.action_home)
            activity.onOptionsItemSelected(home)
            activity.findViewById<View>(R.id.btnShop).performClick()
        }
        settle()
        scenario.onActivity { activity ->
            val page = activity.supportFragmentManager.fragments.single { it.isResumed }.requireView()
            page.findViewById<View>(R.id.chkPurchased).performClick()
        }
        settle()
        scenario.onActivity { activity ->
            val page = activity.supportFragmentManager.fragments.single { it.isResumed }.requireView()
            page.findViewById<View>(R.id.btnFlag).performClick()
        }
        settle()
        scenario.onActivity { activity ->
            val category = ViewModelProvider(activity)[ShoppingListViewModel::class.java].categories.value!![0]
            assertEquals("Fruit", category.heading)
            assertEquals("Green apples", category.items.single().name)
            assertTrue(category.items.single().isChecked)
            assertTrue(category.items.single().isFlagged)
            val page = activity.supportFragmentManager.fragments.single { it.isResumed }.requireView()
            assertEquals("1/1", page.findViewById<TextView>(R.id.tvCount).text.toString())
        }
    }

    private fun openOverview(plan: Boolean) {
        scenario.onActivity { activity ->
            activity.findViewById<View>(if (plan) R.id.btnPlan else R.id.btnShop).performClick()
            val pager = activity.findViewById<ViewPager2>(R.id.viewPager)
            pager.setCurrentItem(if (plan) 0 else pager.adapter!!.itemCount - 1, false)
        }
        settle()
    }

    private fun verifyRemovedCategory(plan: Boolean) {
        openOverview(plan)
        doubleTapName("Alpha") { activity ->
            // Keep the count unchanged to isolate stale row identity from pager resizing.
            val model = ViewModelProvider(activity)[ShoppingListViewModel::class.java]
            model.setCategories(listOf(ShoppingCategory(99, "Delta", emptyList())) + categories.drop(1))
        }
        settle()
        scenario.onActivity { activity ->
            assertEquals(if (plan) 0 else 3, activity.findViewById<ViewPager2>(R.id.viewPager).currentItem)
        }
    }

    private fun verifyDuplicateNames(plan: Boolean) {
        scenario.onActivity { activity ->
            ViewModelProvider(activity)[ShoppingListViewModel::class.java].editCategory(1, "Alpha")
        }
        openOverview(plan)
        doubleTapName("Alpha") { activity ->
            ViewModelProvider(activity)[ShoppingListViewModel::class.java].reorderCategory(0, 2)
        }
        settle()
        scenario.onActivity { activity ->
            assertEquals(2 + if (plan) 1 else 0, activity.findViewById<ViewPager2>(R.id.viewPager).currentItem)
            val page = activity.supportFragmentManager.fragments.single { it.isResumed }.requireView()
            assertEquals("Apples", descendants(page).filterIsInstance<TextView>()
                .single { it.id == R.id.tvName }.text.toString())
        }
    }

    private fun verifyEmptyAndSingle(plan: Boolean) {
        scenario.onActivity { activity ->
            ViewModelProvider(activity)[ShoppingListViewModel::class.java].setCategories(emptyList())
        }
        settle()
        openOverview(plan)
        scenario.onActivity { activity ->
            val pager = activity.findViewById<ViewPager2>(R.id.viewPager)
            assertEquals(1, pager.adapter!!.itemCount)
            assertEquals(0, pager.currentItem)
        }
        if (plan) {
            tapVisibleView(R.id.etAdd, twice = true)
            scenario.onActivity { activity ->
                assertEquals(0, activity.findViewById<ViewPager2>(R.id.viewPager).currentItem)
            }
            // Dismiss the IME without sending Back through the app.
            scenario.onActivity { activity ->
                val input = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                input.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
                activity.window.decorView.clearFocus()
            }
        }
        scenario.onActivity { activity ->
            ViewModelProvider(activity)[ShoppingListViewModel::class.java].setCategories(categories.take(1))
        }
        settle()
        scenario.onActivity { activity ->
            activity.findViewById<ViewPager2>(R.id.viewPager).setCurrentItem(if (plan) 0 else 1, false)
        }
        settle()
        doubleTapName("Alpha")
        settle()
        scenario.onActivity { activity ->
            assertEquals(if (plan) 1 else 0, activity.findViewById<ViewPager2>(R.id.viewPager).currentItem)
            val page = activity.supportFragmentManager.fragments.single { it.isResumed }.requireView()
            assertEquals("Alpha", page.findViewById<TextView>(R.id.tvHeading).text.toString())
        }
    }

    private fun verifyPendingReorder(plan: Boolean) {
        scenario.onActivity { activity ->
            activity.findViewById<View>(if (plan) R.id.btnPlan else R.id.btnShop).performClick()
            activity.findViewById<ViewPager2>(R.id.viewPager).setCurrentItem(if (plan) 0 else 3, false)
        }
        settle()
        doubleTapName("Alpha") { activity ->
            // Same main-thread turn: model has changed, visible diff has not committed.
            ViewModelProvider(activity)[ShoppingListViewModel::class.java].reorderCategory(0, 2)
        }
        settle()
        scenario.onActivity { activity ->
            val page = activity.supportFragmentManager.fragments.single { it.isResumed }
            assertEquals("The visible name Alpha must still open Alpha",
                "Alpha", page.requireView().findViewById<TextView>(R.id.tvHeading).text.toString())
        }
    }

    private fun verifyCategories(plan: Boolean, reordered: Boolean) {
        scenario.onActivity { activity ->
            activity.findViewById<View>(if (plan) R.id.btnPlan else R.id.btnShop).performClick()
        }
        settle()
        if (reordered) {
            scenario.onActivity { activity ->
                ViewModelProvider(activity)[ShoppingListViewModel::class.java].reorderCategory(0, 2)
            }
            settle()
        }
        val expected = if (reordered) listOf("Beta", "Gamma", "Alpha") else listOf("Alpha", "Beta", "Gamma")
        expected.forEachIndexed { index, heading ->
            scenario.onActivity { activity ->
                activity.findViewById<ViewPager2>(R.id.viewPager).setCurrentItem(if (plan) 0 else 3, false)
            }
            settle()
            doubleTapName(heading)
            settle()
            scenario.onActivity { activity ->
                assertEquals("Page for $heading in plan=$plan reordered=$reordered",
                    index + if (plan) 1 else 0, activity.findViewById<ViewPager2>(R.id.viewPager).currentItem)
                val page = activity.supportFragmentManager.fragments.single { it.isResumed }
                assertEquals(heading, page.requireView().findViewById<TextView>(R.id.tvHeading).text.toString())
            }
        }
    }

    private fun doubleTapName(name: String, beforeTouch: (MainActivity) -> Unit = {}) {
        scenario.onActivity { activity ->
            val target = descendants(activity.window.decorView).filterIsInstance<TextView>()
                .first { it.id == R.id.tvName && it.text.toString() == name && it.getGlobalVisibleRect(Rect()) }
            dispatchTaps(activity, target, twice = true, beforeTouch = beforeTouch)
        }
    }

    private fun tapVisibleView(id: Int, twice: Boolean) {
        scenario.onActivity { activity ->
            val target = descendants(activity.window.decorView).first { it.id == id && it.getGlobalVisibleRect(Rect()) }
            dispatchTaps(activity, target, twice)
        }
    }

    private fun dispatchTaps(activity: MainActivity, target: View, twice: Boolean, beforeTouch: (MainActivity) -> Unit = {}) {
        val rect = Rect()
        assertTrue(target.getGlobalVisibleRect(rect))
        val decorLocation = IntArray(2)
        activity.window.decorView.getLocationOnScreen(decorLocation)
        val x = rect.exactCenterX() - decorLocation[0]
        val y = rect.exactCenterY() - decorLocation[1]
        beforeTouch(activity)
        val start = SystemClock.uptimeMillis()
        val events = listOf(0L to MotionEvent.ACTION_DOWN, 40L to MotionEvent.ACTION_UP,
            100L to MotionEvent.ACTION_DOWN, 140L to MotionEvent.ACTION_UP)
        (if (twice) events else events.take(2)).forEach { (offset, action) ->
            val downTime = if (offset < 100) start else start + 100
            val event = MotionEvent.obtain(downTime, start + offset, action, x, y, 0)
            event.source = android.view.InputDevice.SOURCE_TOUCHSCREEN
            try {
                activity.dispatchTouchEvent(event)
            } finally {
                event.recycle()
            }
        }
    }

    private fun descendants(view: View): Sequence<View> = sequence {
        yield(view)
        if (view is ViewGroup) for (i in 0 until view.childCount) yieldAll(descendants(view.getChildAt(i)))
    }

    private fun settle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(600) // Allow ViewPager2 transitions and AsyncListDiffer/layout to complete.
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }
}
