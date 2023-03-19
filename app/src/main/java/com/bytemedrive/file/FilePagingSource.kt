package com.bytemedrive.file

import androidx.paging.PagingSource
import androidx.paging.PagingState

class FilePagingSource(private val getData: (pageIndex: Int, pageSize: Int) -> List<File>) : PagingSource<Int, File>() {

    override fun getRefreshKey(state: PagingState<Int, File>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, File> {

        val pageIndex = params.key ?: 0
        val data = getData(pageIndex, 20)

        return LoadResult.Page(
            data = data,
            prevKey = if (pageIndex == 0) null else pageIndex.minus(1),
            nextKey = if (data.isEmpty()) null else pageIndex.plus(1),
        )
    }
}