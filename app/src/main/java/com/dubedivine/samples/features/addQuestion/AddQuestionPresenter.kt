package com.dubedivine.samples.features.addQuestion

import android.util.Log
import com.dubedivine.samples.data.DataManager
import com.dubedivine.samples.data.model.Question
import com.dubedivine.samples.data.model.StatusResponse
import com.dubedivine.samples.data.model.Tag
import com.dubedivine.samples.features.base.BasePresenter
import com.dubedivine.samples.util.BasicUtils
import com.dubedivine.samples.util.rx.scheduler.SchedulerUtils
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Created by divine on 2017/09/23.
 */

class AddQuestionPresenter @Inject
constructor(private val mDataManager: DataManager) : BasePresenter<AddQuestionMvpView>() {
    override fun attachView(mvpView: AddQuestionMvpView) {
        super.attachView(mvpView)
    }

    fun getTagSuggestion(tag: CharSequence, tagStartIndex: Int, tagStopIndex: Int) {
        checkViewAttached()
            mvpView!!.showProgress(false)  // diable the default behaviour
             mvpView!!.showTagSuggestionProgress(true)
            mDataManager.getTagSuggestion(tag)
                    .compose(SchedulerUtils.ioToMain<List<Tag>>())
                    .subscribe({
                        mvpView!!.showTagSuggestionProgress(false)
                        mvpView!!.showTagsSuggestion(it, tag, tagStartIndex, tagStopIndex)
                    }, {
                        mvpView!!.showError(it)
                        mvpView!!.showTagSuggestionProgress(false)
                    })
    }

    fun publishNewQuestion(question: Question, files: List<String>? = null) {
        doLongTaskOnView {
            mDataManager.postQuestion(question)
                    .compose(SchedulerUtils.ioToMain<StatusResponse<Question>>())
                    .subscribe({
                        Log.d(TAG, "the return v is $it")
                        when (it.status) {
                            true -> {
                                mvpView!!.showProgress(false)
                                if (files != null && files.isNotEmpty()) {
                                    mvpView!!.showProgress(true, "Now saving attached question file(s)., just a sec...")
                                    val retrofitFileParts: MutableList<MultipartBody.Part>
                                            = BasicUtils.createMultiPartFromFile(files)
                                    mDataManager.postQuestionFiles(it.entity!!.id!!,   retrofitFileParts)
                                            .compose(SchedulerUtils.ioToMain<StatusResponse<Question>>())
                                            .subscribe({ // todo should check status here
                                                mvpView!!.showProgress(false, "...")
                                                Log.d(TAG, "We are now here we hot the question $it")
                                                mvpView!!.showQuestion(it.entity!!)
                                            }, {
                                                mvpView!!.showError(it)
                                                mvpView!!.showProgress(false, "...")
                                            })
                                } else {
                                    mvpView!!.showQuestion(it.entity!!)
                                }
                            }
                            false -> {
                                mvpView!!.showError(Throwable("Sorry failed to upload Question"))
                            }
                        }
                    }, {
                        mvpView!!.showError(it)
                    })
        }
    }

    companion object {
        val TAG = "__AddQuestionPresenter"
    }

}
