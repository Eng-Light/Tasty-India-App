package com.example.tastyindia.ui.search

import android.view.View
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.tastyindia.R
import com.example.tastyindia.base.BaseFragment
import com.example.tastyindia.data.DataManager
import com.example.tastyindia.data.DataManagerInterface
import com.example.tastyindia.data.domain.Recipe
import com.example.tastyindia.data.source.CsvDataSource
import com.example.tastyindia.databinding.FragmentSearchBinding
import com.example.tastyindia.ui.recipedetails.RecipeDetailsFragment
import com.example.tastyindia.utils.CsvParser
import com.example.tastyindia.utils.onClickBackFromNavigation
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

class SearchFragment : BaseFragment<FragmentSearchBinding>(),
    SearchAdapter.RecipeInteractionListener {

    private val dataSource by lazy { CsvDataSource(requireContext(), CsvParser()) }
    private val dataManager: DataManagerInterface by lazy { DataManager(dataSource) }
    private lateinit var adapter: SearchAdapter
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    override val TAG: String = this::class.java.simpleName.toString()
    override fun getViewBinding() = FragmentSearchBinding.inflate(layoutInflater)

    override fun setUp() {
        adapter = SearchAdapter(this)
        addCallbacks()
    }

    private fun addCallbacks() {
        addSearchListener()
        onClickBackFromNavigation()
    }

    private fun addSearchListener() {
        search()
    }

    private fun searchByQueryAndSetDataInAdapter(query: String?) {
        query?.let {
            val resultOfSearch = dataManager.searchByRecipeOrCuisine(it)
            binding.apply {
                visibilityOfImageAndRecyclerInSearchFragment(it, resultOfSearch)
                if (it.isNotEmpty()) {
                    setDataOnAdapter(resultOfSearch)
                }
            }
        }
    }

    private fun setDataOnAdapter(resultOfSearch: List<Recipe>) {
        adapter.setData(resultOfSearch)
        binding.rvSearchResult.adapter = adapter
    }

    private fun visibilityOfImageAndRecyclerInSearchFragment(query: String, result: List<Recipe>) {
        with(binding) {
            setOnQueryVisibility(query)
            setOnResultVisibility(result)
        }
    }

    private fun FragmentSearchBinding.setOnResultVisibility(result: List<Recipe>) {
        if (result.isEmpty()) {
            tvEmptySearch.visibility = View.VISIBLE
            imgSearch.visibility = View.VISIBLE
            tvDetails.visibility = View.VISIBLE
            tvEmptySearch.text =
                getString(R.string.no_recipes_with_this_name)
            tvDetails.text = getString(R.string.please_enter_valid_name)
            imgSearch.setImageResource(R.drawable.search_notfound)
        } else {
            tvEmptySearch.text = getString(R.string.dicover_recipes)
            tvDetails.text = getString(R.string.click_to_start_search)
        }
    }

    private fun FragmentSearchBinding.setOnQueryVisibility(query: String) {
        if (query.isEmpty()) {
            imgSearch.visibility = View.VISIBLE
            tvEmptySearch.visibility = View.VISIBLE
            tvDetails.visibility = View.VISIBLE
            imgSearch.setImageResource(R.drawable.ic_recipe_book)
            rvSearchResult.visibility = View.GONE
        } else {
            imgSearch.visibility = View.GONE
            tvEmptySearch.visibility = View.GONE
            tvDetails.visibility = View.GONE
            rvSearchResult.visibility = View.VISIBLE
        }
    }

    private fun performSearch(query: String) {
        searchByQueryAndSetDataInAdapter(query)
    }

    private fun search() {
        searchObservable
            .debounce(
                500,
                TimeUnit.MILLISECONDS
            )
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query ->
                performSearch(query)
            }.add(compositeDisposable)
    }

    private val searchObservable = Observable.create { emitter ->
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                emitter.onNext(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                emitter.onNext(query)
                return true
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment).addToBackStack(null)
        transaction.commit()
    }

    override fun onClickRecipe(recipeId: Int) {
        val recipeDetailsFragment = RecipeDetailsFragment.newInstance(recipeId)
        replaceFragment(recipeDetailsFragment)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}

private fun Disposable.add(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}
