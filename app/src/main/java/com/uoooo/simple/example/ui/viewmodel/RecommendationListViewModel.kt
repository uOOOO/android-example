package com.uoooo.simple.example.ui.viewmodel

import android.app.Application
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.uoooo.simple.example.domain.model.Movie
import com.uoooo.simple.example.domain.repository.MovieRepository
import com.uoooo.simple.example.ui.common.BasePagingViewModel
import com.uoooo.simple.example.ui.movie.source.RecommendationMovieDataSourceFactory
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

class RecommendationListViewModel(application: Application, private val repository: MovieRepository) :
    BasePagingViewModel<Movie>(application) {
    private lateinit var pagedList: Subject<PagedList<Movie>>

    fun getRecommendationList(id: Int, startPage: Int, endPage: Int): Observable<PagedList<Movie>> {
//        if (!::pagedList.isInitialized) {
            pagedList = createPagedRecommendationList(id, startPage, endPage)
                .subscribeWith(BehaviorSubject.create())
//        }
        return pagedList
    }

    private fun createPagedRecommendationList(id: Int, startPage: Int, endPage: Int): Observable<PagedList<Movie>> {
        val factory = RecommendationMovieDataSourceFactory(repository, id, startPage, endPage, _networkState)
            .apply { factoryList += this }
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()
        return RxPagedListBuilder(factory, config)
            .buildObservable()
    }
}