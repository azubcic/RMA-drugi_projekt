package com.zubcic.project2_antoniozubcic.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.zubcic.project2_antoniozubcic.R
import com.zubcic.project2_antoniozubcic.databinding.FragmentCrudBinding
import com.zubcic.project2_antoniozubcic.model.CameraDbEntity
import com.zubcic.project2_antoniozubcic.viewmodel.MainViewModel
import com.zubcic.project2_antoniozubcic.viewmodel.MainViewModelFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class CrudFragment : Fragment() {

    private val args: CrudFragmentArgs by navArgs()

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(requireActivity().application)
    }

    private var _binding: FragmentCrudBinding? = null
    private val binding get() = _binding!!

    private var picturePath: String? = null
    private var selectedDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrudBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCamera(args.cameraId)
        Log.i("XXX", "${args.cameraId}")

        viewModel.camera.observe(viewLifecycleOwner) {
            assignData(viewModel.camera)
        }
        activateButtons()
    }

    private fun activateButtons() = with(binding) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        datePicker.init(
            year,
            month,
            day,
            DatePicker.OnDateChangedListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.set(selectedYear, selectedMonth, selectedDay)

                selectedDate = selectedDateCalendar.time
            })

        btnInsert.setOnClickListener {
            if (args.cameraId != -1) {
                viewModel.updateCamera(inputExistingFighter())
            } else {
                viewModel.insertCamera(inputNewFighter())
            }
            findNavController().navigateUp()
        }

        btnDelete.setOnClickListener {
            viewModel.deleteCamera()
            findNavController().navigateUp()
        }

        btnTakePicture.setOnClickListener {
            checkPermission()
        }
    }

    private fun checkPermission() = when {
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
            takePicture()
        }

        else -> {
            val cameraPermission = Manifest.permission.CAMERA
            val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

            val hasCameraPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
            val hasStoragePermission = ContextCompat.checkSelfPermission(
                requireContext(),
                storagePermission
            ) == PackageManager.PERMISSION_GRANTED

            val permissionsToRequest = mutableListOf<String>()
            if (!hasCameraPermission) {
                permissionsToRequest.add(cameraPermission)
            }
            if (!hasStoragePermission) {
                permissionsToRequest.add(storagePermission)
            }
            requestPermissions(permissionsToRequest.toTypedArray(), 2)
        }
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity?.packageManager ?: return) == null) {
            return
        }

        picturePath = createPictureFile()

        if (picturePath == null) {
            return
        }

        val pictureFile = File(picturePath)
        val pictureUri: Uri = FileProvider.getUriForFile(
            requireActivity(),
            "zubcic.project2_antoniozubcic.provider",
            pictureFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
        startActivityForResult(intent, 1)
    }

    private fun createPictureFile(): String? {
        val name = SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + "_camera"
        val dir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(name, ".jpg", dir)
        return file.path
    }

    private fun inputNewFighter(): CameraDbEntity = CameraDbEntity(
        manufactrer = binding.etManufacturerName.text.toString(),
        model = binding.etModel.text.toString(),
        image = picturePath ?: "",
        releaseDate = selectedDate,
        type = view?.findViewById<RadioButton>(binding.rgType.checkedRadioButtonId)?.text.toString()
    )

    private fun inputExistingFighter(): CameraDbEntity = CameraDbEntity(
        id = viewModel.camera.value!!.id,
        manufactrer = binding.etManufacturerName.text.toString(),
        model = binding.etModel.text.toString(),
        image = picturePath ?: "",
        releaseDate = selectedDate,
        type = view?.findViewById<RadioButton>(binding.rgType.checkedRadioButtonId)?.text.toString()
    )

    private fun loadTakenImage(imageUri: String) = with(binding) {
        Glide
            .with(root)
            .load(imageUri)
            .into(pictureTaken)
    }

    private fun assignData(cameraLiveData: LiveData<CameraDbEntity?>) {
        cameraLiveData.value?.let { camera ->
            with(binding) {
                etManufacturerName.setText(camera.manufactrer)
                etModel.setText(camera.model)
                when (camera.type) {
                    "DSLR" -> rgType.check(R.id.rb_dslr)
                    "Mirrorless" -> rgType.check(R.id.rb_mirrorless)
                }
                Glide
                    .with(root)
                    .load(camera.image)
                    .into(pictureTaken)

                val calendar = Calendar.getInstance()

                if (viewModel.camera.value?.releaseDate != null) {
                    selectedDate = viewModel.camera.value?.releaseDate!!
                    calendar.time = selectedDate!!
                } else selectedDate = calendar.time
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                datePicker.init(
                    year,
                    month,
                    day,
                    DatePicker.OnDateChangedListener { _, selectedYear, selectedMonth, selectedDay ->
                        val selectedDateCalendar = Calendar.getInstance()
                        selectedDateCalendar.set(selectedYear, selectedMonth, selectedDay)

                        selectedDate = selectedDateCalendar.time
                    })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    picturePath?.let { loadTakenImage(it) }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            2 -> {
                val allPermissionsGranted =
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }

                if (allPermissionsGranted) {
                    takePicture()
                } else {
//                    showPermissionDeniedDialog()
                    Toast.makeText(
                        requireContext(),
                        "Permission denied!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            else -> {
                // Ignore
            }
        }
    }
}