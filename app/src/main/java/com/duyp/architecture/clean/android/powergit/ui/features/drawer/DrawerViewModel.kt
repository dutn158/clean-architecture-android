package com.duyp.architecture.clean.android.powergit.ui.features.drawer

import com.duyp.architecture.clean.android.powergit.domain.entities.UserEntity
import com.duyp.architecture.clean.android.powergit.domain.usecases.GetUser
import com.duyp.architecture.clean.android.powergit.domain.usecases.LogoutUser
import com.duyp.architecture.clean.android.powergit.ui.Event
import com.duyp.architecture.clean.android.powergit.ui.base.BaseViewModel
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class DrawerViewModel @Inject constructor(
        private val mGetUser: GetUser,
        private val mLogoutUser: LogoutUser
): BaseViewModel<DrawerViewState, DrawerIntent>() {

    override fun initState() = DrawerViewState()

    override fun composeIntent(intentSubject: Observable<DrawerIntent>) {

        addDisposable {
            intentSubject.ofType(DrawerIntent.RefreshUser::class.java)
                    .observeOn(Schedulers.io())
                    .switchMap {
                        mGetUser.getCurrentLoggedInUser()
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext { user ->
                                    setState { copy(user = user) }
                                }
                                .doOnError { _ ->
                                    setState { copy(user = null) }
                                }
                                .onErrorResumeNext(Flowable.empty())
                                .toObservable()
                    }
                    .subscribe()
        }

        addDisposable {
            intentSubject.ofType(DrawerIntent.LogoutIntent::class.java)
                    .observeOn(Schedulers.io())
                    .switchMapCompletable {
                        mLogoutUser.logoutCurrentUser()
                                .onErrorComplete()
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete {
                                    setState { copy(refreshUser = Event(Unit)) }
                                }
                    }
                    .subscribe()
        }

        addDisposable {
            intentSubject.ofType(DrawerIntent.OpenUserProfile::class.java)
                    .observeOn(Schedulers.io())
                    .switchMapSingle {
                        mGetUser.getCurrentLoggedInUsername()
                                .onErrorReturnItem("")
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSuccess { username ->
                                    if (username.isEmpty()) {
                                        setState { copy(navigation = Event(DrawerNavigation.LOGIN)) }
                                    } else {
                                        setState { copy(navigation = Event(DrawerNavigation.MY_PROFILE)) }
                                    }
                                }
                    }
                    .subscribe()
        }
    }
}

data class DrawerViewState(
        val user: UserEntity? = null,
        val navigation: Event<DrawerNavigation>? = null,
        val refreshUser: Event<Unit>? = null
)

interface DrawerIntent {
    class RefreshUser: DrawerIntent
    class LogoutIntent: DrawerIntent
    class OpenUserProfile: DrawerIntent
}

enum class DrawerNavigation {
    LOGIN,
    MY_PROFILE
}