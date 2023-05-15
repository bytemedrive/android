package com.bytemedrive.file.root

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlin.math.min

class FilePagingSource(private val data: List<Item>) : PagingSource<Int, Item>() {

    private fun getData(pageIndex: Int = 0, pageSize: Int = 20): List<Item> {
        val offset = pageIndex * pageSize
        val lastItemIndex = min(data.size - 1, offset + pageSize - 1)

        return data.slice(offset..lastItemIndex)
    }

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {

        val pageIndex = params.key ?: 0
        val data = getData(pageIndex, 20)

        return LoadResult.Page(
            data = data,
            prevKey = if (pageIndex == 0) null else pageIndex.minus(1),
            nextKey = if (data.isEmpty()) null else pageIndex.plus(1),
        )
    }
}