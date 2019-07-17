package com.uoooo.mvvm.example.ui.movie.source

import androidx.paging.DataSource
import com.uoooo.mvvm.example.domain.model.Movie
import com.uoooo.mvvm.example.domain.repository.MovieRepository
import com.uoooo.mvvm.example.ui.common.BaseDataSourceFactory
import com.uoooo.mvvm.example.ui.viewmodel.state.PagingState
import io.reactivex.subjects.Subject

class PopularDataSourceFactory(
    private val repository: MovieRepository,
    override val startPage: Int,
    override val endPage: Int,
    override val pagingState: Subject<PagingState>
) : BaseDataSourceFactory<Movie>(startPage, endPage, pagingState) {
    private lateinit var popularDataSource: PopularDataSource

    override fun create(): DataSource<Int, Movie> {
        popularDataSource = PopularDataSource(repository, startPage, endPage, pagingState)
        return popularDataSource
    }

    override fun invalidate() {
        if (::popularDataSource.isInitialized) popularDataSource.invalidate()
    }

    override fun onCleared() {
        if (::popularDataSource.isInitialized) popularDataSource.onCleared()
    }
}
