package com.example.tastyindia.ui

import com.example.tastyindia.databinding.FragmentCuisineBinding

class CuisineFragment : BaseFragment<FragmentCuisineBinding>() {
    override val TAG: String = "CuisineFragment"
    override fun getViewBinding(): FragmentCuisineBinding =
        FragmentCuisineBinding.inflate(layoutInflater)

    override fun setUp() {
        log(getAllCuisines().size.toString())
    }

    override fun addCallbacks() {

    }

    private fun getAllCuisines() = getAllRecipes().map { it.cuisine }.distinct()

}