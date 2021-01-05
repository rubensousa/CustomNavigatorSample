package com.navigation.sample

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

class CustomNavHostFragment : NavHostFragment() {

    override fun onCreateNavController(navController: NavController) {
        super.onCreateNavController(navController)
        navController.navigatorProvider.addNavigator(
            SingleDialogNavigator(requireContext(), childFragmentManager)
        )
    }
}