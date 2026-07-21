package com.fielddevice.healthmonitor.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps [kotlinx.coroutines.Dispatchers.Main] for a test dispatcher so `viewModelScope`
 * coroutines (which are hard-wired to `Dispatchers.Main`) run deterministically in tests
 * instead of needing a real Android main-thread looper. Uses [UnconfinedTestDispatcher] so
 * mocked (non-delaying) suspend calls launched via `viewModelScope.launch` complete
 * synchronously, letting tests assert on ViewModel state immediately without manually
 * advancing a scheduler.
 */
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
