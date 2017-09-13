package com.dubedivine.samples.features.main

import com.dubedivine.samples.data.model.Question
import com.dubedivine.samples.features.base.MvpView

interface MainMvpView : MvpView {

    fun showPokemon(pokemon: List<String>)

    fun showProgress(show: Boolean)

    fun showError(error: Throwable)

    fun showProgressOnAutoComplete(show: Boolean)

    fun  showSuggestions(question: List<Question>)

}