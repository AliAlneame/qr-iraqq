package com.agb.iraq.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.agb.iraq.data.remote.model.QuotationItem
import com.agb.iraq.data.remote.api.QuotationApi

class QuotationPagingSource(
    private val api: QuotationApi
) : PagingSource<Int, QuotationItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuotationItem> {
        return try {
            val page = params.key ?: 1
            val response = api.getQuotations(page)

            LoadResult.Page(
                data = response.data.data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page < response.data.lastPage) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, QuotationItem>): Int? {
        return state.anchorPosition
    }
}
