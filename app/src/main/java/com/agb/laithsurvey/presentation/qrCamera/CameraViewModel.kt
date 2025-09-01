package com.agb.laithsurvey.presentation.qrCamera

import android.util.Log
import com.agb.laithsurvey.data.repository.IErpRepository
import com.agb.laithsurvey.presentation.base.BaseViewModel
import com.agb.laithsurvey.presentation.shared.SharedInteractionListener
import com.agb.laithsurvey.presentation.shared.SharedUiEffect
import com.agb.laithsurvey.presentation.shared.SharedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: IErpRepository,
) : BaseViewModel<SharedUiState, SharedUiEffect>(SharedUiState()), SharedInteractionListener {

    fun sendQrCode(code: String) {
//        tryToExecute(
//            { repository.sendQrCode(code) },
//            ::sendQrCodeSuccess,
//            { onError(it) })
    }

//    private fun sendQrCodeSuccess(result: QRCodeResponse?) {
//        sendEffect(SharedUiEffect.ShowToast("Code Scanned Success"))
//        Log.d("sendQrCodeSuccess: ", "$result")
//    }

    private fun onError(error: Exception) {
//        sendEffect(SharedUiEffect.ShowToast("Error"))
//        Log.d("onError: ", "${state.value.toDto()} - ${error.message}")
    }


}