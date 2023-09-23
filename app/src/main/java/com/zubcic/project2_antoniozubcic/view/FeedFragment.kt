package com.zubcic.project2_antoniozubcic.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.zubcic.project2_antoniozubcic.R
import com.zubcic.project2_antoniozubcic.databinding.CameraListItemBinding
import com.zubcic.project2_antoniozubcic.databinding.FragmentFeedBinding
import com.zubcic.project2_antoniozubcic.model.CameraDbEntity
import com.zubcic.project2_antoniozubcic.viewmodel.MainViewModel
import com.zubcic.project2_antoniozubcic.viewmodel.MainViewModelFactory

class FeedFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(requireActivity().application)
    }

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.cameras()
        viewModel.cameraList.observe(viewLifecycleOwner) {
            populateListView(viewModel.cameraList)
        }
        activateComponents()

    }

    private fun populateListView(cameraList: LiveData<List<CameraDbEntity>?>) {
        binding.linearLayout.removeAllViews()
        cameraList.value?.let {cameraList ->
            for (camera in cameraList) {
                val viewHolder = LayoutInflater.from(this.requireContext()).inflate(R.layout.camera_list_item, null)

                viewHolder.findViewById<TextView>(R.id.tv_manufacturer).text = camera.manufactrer
                viewHolder.findViewById<TextView>(R.id.tv_model).text = camera.model

                viewHolder.setOnClickListener {
                    val action = FeedFragmentDirections.actionFeedFragmentToCrudFragment(camera.id)
                    findNavController().navigate(action)
                }
                binding.linearLayout.addView(viewHolder)
            }
        }
    }

    private fun activateComponents() {
        binding.newCameraFab.setOnClickListener {
            val action = FeedFragmentDirections.actionFeedFragmentToCrudFragment(-1)
            findNavController().navigate(action)
        }
    }
}