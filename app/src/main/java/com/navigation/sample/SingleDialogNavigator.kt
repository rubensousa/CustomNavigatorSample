package com.navigation.sample

/*
* Copyright 2019 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.NavHostFragment

/**
 * A custom DialogFragmentNavigator that only allows showing one dialog at a time
 */
@Navigator.Name("dialog")
class SingleDialogNavigator(
    private val context: Context,
    private val fragmentManager: FragmentManager
) : Navigator<DialogFragmentNavigator.Destination>() {

    companion object {
        private const val TAG = "DialogFragmentNavigator"
        private const val KEY_DIALOG_COUNT = "androidx-nav-dialogfragment:navigator:count"
        private const val KEY_DESTINATION_ID = "androidx-nav-dialogfragment:navigator:destination"
        private const val DIALOG_TAG = "androidx-nav-fragment:navigator:dialog:"
    }

    private var dialogCount = 0
    private var currentDestinationId = -1
    private val observer = LifecycleEventObserver { source, event ->
        if (event == Lifecycle.Event.ON_STOP) {
            val dialogFragment = source as DialogFragment
            if (!dialogFragment.requireDialog().isShowing) {
                NavHostFragment.findNavController(dialogFragment).popBackStack()
            }
        }
    }

    override fun popBackStack(): Boolean {
        if (dialogCount == 0) {
            return false
        }
        if (fragmentManager.isStateSaved) {
            Log.i(
                TAG,
                "Ignoring popBackStack() call: FragmentManager has already"
                        + " saved its state"
            )
            return false
        }
        val existingFragment = fragmentManager.findFragmentByTag(DIALOG_TAG + --dialogCount)
        if (existingFragment != null) {
            existingFragment.lifecycle.removeObserver(observer)
            (existingFragment as DialogFragment).dismiss()
            currentDestinationId = -1
        }
        return true
    }

    override fun createDestination(): DialogFragmentNavigator.Destination {
        return DialogFragmentNavigator.Destination(this)
    }

    override fun navigate(
        destination: DialogFragmentNavigator.Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        if (fragmentManager.isStateSaved) {
            Log.i(
                TAG,
                "Ignoring navigate() call: FragmentManager has already"
                        + " saved its state"
            )
            return null
        }

        // Don't allow multiple dialogs
        if (dialogCount > 0) {
            return null
        }

        var className = destination.className
        if (className[0] == '.') {
            className = context.packageName + className
        }
        val frag = fragmentManager.fragmentFactory.instantiate(
            context.classLoader, className
        )
        require(DialogFragment::class.java.isAssignableFrom(frag.javaClass)) {
            ("Dialog destination " + destination.className
                    + " is not an instance of DialogFragment")
        }
        val dialogFragment = frag as DialogFragment
        dialogFragment.arguments = args
        dialogFragment.lifecycle.addObserver(observer)
        dialogFragment.show(fragmentManager, DIALOG_TAG + dialogCount++)
        currentDestinationId = destination.id
        return destination
    }

    override fun onSaveState(): Bundle? {
        if (dialogCount == 0) {
            return null
        }
        val b = Bundle()
        b.putInt(KEY_DIALOG_COUNT, dialogCount)
        b.putInt(KEY_DESTINATION_ID, currentDestinationId)
        return b
    }

    override fun onRestoreState(savedState: Bundle) {
        currentDestinationId = savedState.getInt(KEY_DESTINATION_ID, -1)
        dialogCount = savedState.getInt(KEY_DIALOG_COUNT, 0)
        for (index in 0 until dialogCount) {
            val fragment = fragmentManager.findFragmentByTag(DIALOG_TAG + index) as DialogFragment?
            if (fragment != null) {
                fragment.lifecycle.addObserver(observer)
            } else {
                throw IllegalStateException(
                    "DialogFragment " + index
                            + " doesn't exist in the FragmentManager"
                )
            }
        }
    }

}
