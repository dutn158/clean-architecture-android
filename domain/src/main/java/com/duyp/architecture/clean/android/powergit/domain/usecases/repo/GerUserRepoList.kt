package com.duyp.architecture.clean.android.powergit.domain.usecases.repo

import com.duyp.architecture.clean.android.powergit.domain.entities.FilterOptions
import com.duyp.architecture.clean.android.powergit.domain.entities.ListEntity
import com.duyp.architecture.clean.android.powergit.domain.entities.mergeWithPreviousPage
import com.duyp.architecture.clean.android.powergit.domain.entities.repo.RepoEntity
import com.duyp.architecture.clean.android.powergit.domain.repositories.RepoRepository
import com.duyp.architecture.clean.android.powergit.domain.usecases.GetUser
import io.reactivex.Single
import javax.inject.Inject

class GerUserRepoList @Inject constructor(
        private val mRepoRepository: RepoRepository,
        private val mGetUser: GetUser
) {

    /**
     * Get repo list of given user with given filter options. The next page of given list will be load and appended
     * with previous page, that means the result list contains all items from starting page to current page
     *
     * @param currentList current list entity
     * @param filterOptions option to filter result
     *
     * @return new list appended with previous list, contains all items from starting page to current page
     */
    fun getRepoList(currentList: ListEntity<RepoEntity>, username: String, filterOptions: FilterOptions):
            Single<ListEntity<RepoEntity>> =
            mRepoRepository.getUserRepoList(username, false, filterOptions, currentList.next)
                    .mergeWithPreviousPage(currentList)

    /**
     * Get next page of repo list of current logged in user with given filter options. The next page of given list
     * will be load and appended with previous page, that means the result list contains all items from starting
     * page to current page
     *
     * @param currentList current list entity
     * @param filterOptions option to filter result
     *
     * @return new list appended with previous list, contains all items from starting page to current page
     */
    fun getCurrentUserRepoList(currentList: ListEntity<RepoEntity>, filterOptions: FilterOptions):
            Single<ListEntity<RepoEntity>> =
            mGetUser.getCurrentLoggedInUsername()
                    .flatMap { username ->
                        mRepoRepository.getUserRepoList(username, true, filterOptions, currentList.next)
                    }
                    .mergeWithPreviousPage(currentList)
}