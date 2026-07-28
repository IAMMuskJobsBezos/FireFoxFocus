/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.session

import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.Store
import org.mozilla.focus.ext.isCustomTab

/**
 * Middleware enforcing a maximum number of open (non-custom-tab) tabs.
 * When the limit is exceeded, the oldest tabs are silently closed.
 */
class TabLimitMiddleware(
    private val maxTabCount: Int = MAX_TAB_COUNT,
) : Middleware<BrowserState, BrowserAction> {

    override fun invoke(
        store: Store<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction,
    ) {
        next(action)

        if (action is TabListAction.AddTabAction || action is TabListAction.AddMultipleTabsAction) {
            enforceTabLimit(store)
        }
    }

    private fun enforceTabLimit(store: Store<BrowserState, BrowserAction>) {
        val tabs = store.state.tabs.filter { !it.isCustomTab() }
        val excessCount = tabs.size - maxTabCount

        if (excessCount > 0) {
            tabs.sortedBy { it.createdAt }
                .take(excessCount)
                .forEach { store.dispatch(TabListAction.RemoveTabAction(it.id)) }
        }
    }

    companion object {
        private const val MAX_TAB_COUNT = 10
    }
}
