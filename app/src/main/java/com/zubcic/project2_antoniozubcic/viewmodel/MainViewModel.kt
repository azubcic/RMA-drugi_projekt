package com.zubcic.project2_antoniozubcic.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zubcic.project2_antoniozubcic.model.CameraDbEntity
import com.zubcic.project2_antoniozubcic.model.db.CameraDao
import com.zubcic.project2_antoniozubcic.model.db.CameraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val cameraDao: CameraDao

    private var _camera: MutableLiveData<CameraDbEntity?> = MutableLiveData()
    val camera: LiveData<CameraDbEntity?> = _camera

    private var _cameraList: MutableLiveData<List<CameraDbEntity>?> = MutableLiveData()
    val cameraList: LiveData<List<CameraDbEntity>?> = _cameraList

    init {
        cameraDao = CameraDatabase.getDatabase(application.applicationContext).cameraDao()
        cameras()
    }

    fun cameras() = viewModelScope.launch(Dispatchers.IO) {
        _cameraList.postValue(cameraDao.cameras())
    }

    fun getCamera(cameraId: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (cameraId != -1) {
            _camera.postValue(cameraDao.getCamera(cameraId))
        } else _camera.postValue(null)
    }

    fun insertCamera(cameraDbEntity: CameraDbEntity) = viewModelScope.launch(Dispatchers.IO) {
        cameraDao.insertCamera(cameraDbEntity)
        Log.i("XXX", "inserted camera id: ${cameraDbEntity.id}")
    }

    fun deleteCamera() = viewModelScope.launch(Dispatchers.IO) {
        _camera.value?.let { cameraDao.deleteCamera(it) }
    }

    fun updateCamera(cameraDbEntity: CameraDbEntity) = viewModelScope.launch(Dispatchers.IO) {
        cameraDao.updateCamera(cameraDbEntity)
    }
}