package com.navigation.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.navigation.sample.databinding.FragmentBinding

class MainFragment : Fragment(R.layout.fragment) {

    private lateinit var binding: FragmentBinding
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()
        binding = FragmentBinding.bind(view)
        binding.openDialogButton.setOnClickListener {
            navController.navigate(R.id.action_open_dialog)
        }
        binding.openTwoDialogsButton.setOnClickListener {
            navController.navigate(R.id.action_open_dialog)
            navController.navigate(R.id.action_open_dialog)
        }
    }

}
