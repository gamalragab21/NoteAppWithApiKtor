package com.developers.noteappktorserver.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.developers.noteappktorserver.R
import com.developers.noteappktorserver.data.local.DataStoreManager
import com.developers.noteappktorserver.databinding.FragmentSplashBinding

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNoteApp.animate().translationY(-1400f).setDuration(2700).setStartDelay(0)
       // binding.animationView.animate().translationX(2000f).setDuration(2700).setStartDelay(2900)



            Handler().postDelayed({
                  checkUserStatsAndNavigate(savedInstanceState)

            }, 5000)


    }

    private  fun checkUserStatsAndNavigate(savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenStarted {
            dataStoreManager.infoUser.collect {userInfo->
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()

                if (userInfo.token.isNotEmpty()) {
                    findNavController().navigate(
                        R.id.action_splashFragment_to_homeFragment,
                        savedInstanceState,
                        navOptions
                    )
                } else {
                    findNavController().navigate(
                        R.id.action_splashFragment_to_loginFragment,
                        savedInstanceState,
                        navOptions
                    )
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}