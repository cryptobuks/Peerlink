package com.dubedivine.samples.features.base

import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * attachView() and detachView(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 */
open class BasePresenter<T : MvpView> : Presenter<T> {

    var mvpView: T? = null
        private set // making the setter of the mvpView private!!!
    private val mCompositeSubscription = CompositeSubscription()

    override fun attachView(mvpView: T) {
        this.mvpView = mvpView
    }

    override fun detachView() {
        mvpView = null
        if (!mCompositeSubscription.isUnsubscribed) {
            mCompositeSubscription.clear()
        }
    }

    val isViewAttached: Boolean
        get() = mvpView != null

    fun checkViewAttached() {
        if (!isViewAttached) throw MvpViewNotAttachedException()
    }

    fun addSubscription(subs: Subscription) {
        mCompositeSubscription.add(subs)
    }

    /**
     * @param action its a function to be executed
     * this function will show a loader while the function is being executed
     * remember to close(hide) the progress bar
    * */
   inline fun doLongTaskOnView(action: BasePresenter<T>.() -> Unit) {
        checkViewAttached()
        mvpView!!.showProgress(true)
        action()
    }

    private class MvpViewNotAttachedException internal constructor() : RuntimeException("Please call Presenter.attachView(MvpView) before" + " requesting data to the Presenter")

}

