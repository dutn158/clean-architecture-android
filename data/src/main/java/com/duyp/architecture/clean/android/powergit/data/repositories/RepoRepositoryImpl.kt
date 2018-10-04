package com.duyp.architecture.clean.android.powergit.data.repositories

import com.duyp.architecture.clean.android.powergit.data.api.UserService
import com.duyp.architecture.clean.android.powergit.data.database.RepoDao
import com.duyp.architecture.clean.android.powergit.data.entities.repo.RepoApiToLocalMapper
import com.duyp.architecture.clean.android.powergit.data.entities.repo.RepoListApiToEntityMapper
import com.duyp.architecture.clean.android.powergit.data.entities.repo.RepoLocalToEntityMapper
import com.duyp.architecture.clean.android.powergit.domain.entities.FilterOptions
import com.duyp.architecture.clean.android.powergit.domain.entities.ListEntity
import com.duyp.architecture.clean.android.powergit.domain.entities.repo.RepoEntity
import com.duyp.architecture.clean.android.powergit.domain.repositories.RepoRepository
import io.reactivex.Single
import javax.inject.Inject

class RepoRepositoryImpl @Inject constructor(
        private val mRepoDao: RepoDao,
        private val mUserService: UserService
) : RepoRepository {

    private val mRepoListApiToEntityMapper = RepoListApiToEntityMapper()

    private val mRepoAPiToLocalMapper = RepoApiToLocalMapper()

    private val mRepoLocalToEntityMapper = RepoLocalToEntityMapper()

    override fun getUserRepoList(username: String, isMyUser: Boolean, filterOptions: FilterOptions, page: Int):
            Single<ListEntity<RepoEntity>> {
        val api = if (isMyUser)
            mUserService.getMyRepos(filterOptions.getQueryMap(), page)
        else
            mUserService.getRepos(username, filterOptions.getQueryMap(), page)
        return api
                .doOnSuccess {
                    // save to database
                    mRepoDao.insertList(mRepoAPiToLocalMapper.mapFrom(it.items))
                }
                .map { mRepoListApiToEntityMapper.mapFrom(it) }
                .onErrorResumeNext { throwable ->
                    if (page == ListEntity.STARTING_PAGE) {
                        // api error when loading first page, let load from database
                        return@onErrorResumeNext mRepoDao.getUserRepos(username)
                                .map { mRepoLocalToEntityMapper.mapFrom(it) }
                                .map { ListEntity(items = it, isOfflineData = true, apiError = throwable) }
                    } else {
                        return@onErrorResumeNext Single.error(throwable)
                    }
                }
    }
}