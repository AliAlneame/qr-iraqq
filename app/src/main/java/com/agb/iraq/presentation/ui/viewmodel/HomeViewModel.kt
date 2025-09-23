package com.agb.iraq.presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.agb.iraq.data.remote.model.ProductData
import com.agb.iraq.data.remote.model.QuotationDetailResponse
import com.agb.iraq.data.remote.model.QuotationItem
import com.agb.iraq.data.repository.ErpRepository
import com.agb.iraq.presentation.ui.model.ErpType
import com.agb.iraq.util.buildConfirmFields
import com.agb.iraq.util.buildQuotationFields
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: ErpRepository
) : ViewModel() {

    private val _erpType = MutableStateFlow(
        savedStateHandle.get<String>("erpType")?.let { ErpType.valueOf(it) } ?: ErpType.QUOTATIONS
    )
    val erpType = _erpType.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    var quotations: Flow<PagingData<QuotationItem>> =
        _erpType
            .flatMapLatest { type ->
                when (type) {
                    ErpType.QUOTATIONS -> repo.getQuotationsPager().flow
                    ErpType.PURCHASES -> repo.getPurchasesPager().flow
                }
            }
            .cachedIn(viewModelScope)

    private var currentQuotationId = MutableStateFlow<Int?>(null)

    private val _quotationsItems = MutableStateFlow<QuotationDetailResponse?>(null)
    val quotationsItems = _quotationsItems.asStateFlow()

    private val _scannedSkus = MutableStateFlow<Set<String>>(emptySet())
    val scannedSkus = _scannedSkus.asStateFlow()

    private val _confirmResult = MutableSharedFlow<String>()
    val confirmResult = _confirmResult.asSharedFlow()

    private val _products = MutableStateFlow<List<ProductData>>(emptyList())
    val products: StateFlow<List<ProductData>> = _products

    private val _scannedCode = MutableStateFlow<String?>(null)
    val scannedCode: StateFlow<String?> = _scannedCode

//    fun fetchProducts(sku: String, warehouseId: Int): List<ProductData> {
//        var list: List<ProductData> = emptyList()
//        viewModelScope.launch {
//            try {
//                list = repo.getProducts(sku, warehouseId)
//                _products.value = list
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        return list
//    }

    suspend fun fetchProducts(sku: String, warehouseId: Int): List<ProductData> {
        return try {
            val list = repo.getProducts(sku, warehouseId)
            _products.value = list
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    fun confirmQuotation() {
        viewModelScope.launch {
            val quotation = _quotationsItems.value ?: return@launch
            try {
                val response = when (_erpType.value) {
                    ErpType.QUOTATIONS -> {
                        val fields = quotation.data?.items?.let { buildQuotationFields(it) }
                        repo.updateQuotation(
                            quotationId = quotation.data?.id ?: 0,
                            warehouseId = quotation.data?.warehouse_id ?: 9,
                            customerId = quotation.data?.customer_id ?: 4,
                            quotationDate = quotation.data?.quotation_date ?: "",
                            items = fields ?: emptyMap()
                        )
                    }

                    ErpType.PURCHASES -> {
                        val fields = quotation.data?.items?.let { buildConfirmFields(it) }
                        repo.confirmPurchases(
                            quotation.data?.id ?: 0,
                            fields ?: emptyMap()
                        )
                    }
                }
                _confirmResult.emit(response.message ?: "Unknown")
            } catch (e: Exception) {
                _confirmResult.emit("Error confirming: ${e.message}")
            }
        }
    }

    fun markAsScanned(sku: String) {
        _scannedSkus.value = _scannedSkus.value + sku
    }

    fun isAllScanned(): Boolean {
        val items = _quotationsItems.value?.data?.items ?: return false
        return items.all { it.product?.sku in _scannedSkus.value }
    }

    fun setErpType(type: ErpType) {
        try {
            _erpType.value = type
            when (type) {
                ErpType.QUOTATIONS -> setQuotations()
                ErpType.PURCHASES -> setPurchases()
            }
        } catch (e: Exception) {
            Log.e("setErpType: ", e.message.toString())
        }
    }

    private fun setQuotations() {
        viewModelScope.launch {
            quotations = repo.getQuotationsPager()
                .flow
                .cachedIn(viewModelScope)
        }
    }

    private fun setPurchases() {
        viewModelScope.launch {
            quotations = repo.getPurchasesPager()
                .flow
                .cachedIn(viewModelScope)
        }
    }

    fun setCurrentQuotationId(id: Int) {
        currentQuotationId.value = id
    }

    fun setQuotationsItems() {
        _quotationsItems.value = null
        try {
            viewModelScope.launch {
                _quotationsItems.value = when (_erpType.value) {
                    ErpType.QUOTATIONS -> repo.getQuotationById(currentQuotationId.value ?: 0)
                    ErpType.PURCHASES -> repo.getPurchasesById(currentQuotationId.value ?: 0)
                }
            }
        } catch (e: Exception) {
            Log.e("setQuotationsItems: ", e.message.toString())
        }
    }
}
