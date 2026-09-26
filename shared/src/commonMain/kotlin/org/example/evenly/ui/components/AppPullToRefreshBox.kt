package org.example.evenly.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.evenly.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPullToRefreshBox(isRefreshing: Boolean, onRefresh: () -> Unit, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = modifier,
        indicator = {
            if (isRefreshing || state.distanceFraction > 0f) {
                ActivityIndicator(modifier = Modifier.align(Alignment.TopCenter).padding(top = AppSpacing.lg), progress = if (isRefreshing) 1f else state.distanceFraction.coerceAtMost(1f))
            }
        },
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
    ) {
        content()
    }
}
