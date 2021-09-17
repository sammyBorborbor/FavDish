package com.sammy.favdish.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.sammy.favdish.R
import com.sammy.favdish.application.FavDishApplication
import com.sammy.favdish.databinding.FragmentRandomDishBinding
import com.sammy.favdish.model.entities.FavDish
import com.sammy.favdish.model.entities.RandomDish
import com.sammy.favdish.utils.Constants
import com.sammy.favdish.view.viewmodel.FavDishViewModel
import com.sammy.favdish.view.viewmodel.FavDishViewModelFactory
import com.sammy.favdish.view.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private var mBinding: FragmentRandomDishBinding? = null
    private lateinit var mRandomDishViewModel: RandomDishViewModel
    private var mProgressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentRandomDishBinding.inflate(inflater, container, false)

        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRandomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)

        mRandomDishViewModel.getRandomRecipeFromAPI()

        randomDishViewModelObserver()

        mBinding!!.srlRandomDish.setOnRefreshListener {
            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
            mRandomDishViewModel.getRandomRecipeFromAPI()
        }

    }

    private fun randomDishViewModelObserver() {

        mRandomDishViewModel.randomDishResponse.observe(
            viewLifecycleOwner,
            { randomDishResponse ->
                randomDishResponse?.let {
                    Log.i("Random Dish Response", "${randomDishResponse.recipes[0]}")

                    if (mBinding!!.srlRandomDish.isRefreshing) {
                        mBinding!!.srlRandomDish.isRefreshing = false
                    }

                    setRandomDishResponseInUI(randomDishResponse.recipes[0])
                }
            })

        mRandomDishViewModel.randomDishLoadingError.observe(
            viewLifecycleOwner,
            { dataError ->
                dataError?.let {
                    Log.i("Random Dish API Error", "$dataError")

                    if (mBinding!!.srlRandomDish.isRefreshing) {
                        mBinding!!.srlRandomDish.isRefreshing = false
                    }
                }
            })

        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner, Observer { loadRandomDish ->
            loadRandomDish?.let {
                Log.i("Random Dish Loading", "$loadRandomDish")
                if (loadRandomDish && !mBinding!!.srlRandomDish.isRefreshing) {
                    showCustomProgressDialog() // Used to show the progress dialog
                } else {
                    hideProgressDialog()
                }
            }
        })
    }

    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {

        // Load the dish image in the ImageView.
        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(mBinding!!.ivDishImage)

        mBinding!!.tvTitle.text = recipe.title

        // Default Dish Type
        var dishType: String = "other"

        if (recipe.dishTypes.isNotEmpty()) {
            dishType = recipe.dishTypes[0]
            mBinding!!.tvType.text = dishType
        }

        // There is not category params present in the response so we will define it as Other.
        mBinding!!.tvCategory.text = "Other"

        var ingredients = ""
        for (value in recipe.extendedIngredients) {

            if (ingredients.isEmpty()) {
                ingredients = value.original
            } else {
                ingredients = ingredients + ", \n" + value.original
            }
        }

        mBinding!!.tvIngredients.text = ingredients

        // The instruction or you can say the Cooking direction text is in the HTML format so we will you the fromHtml to populate it in the TextView.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBinding!!.tvCookingDirection.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            @Suppress("DEPRECATION")
            mBinding!!.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        mBinding!!.tvCookingTime.text =
            resources.getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString()
            )

        mBinding!!.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        )

        var addedToFavorite = false

        mBinding!!.ivFavoriteDish.setOnClickListener {

            if (addedToFavorite) {
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_already_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val randomDishDetails = FavDish(
                    recipe.image,
                    Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title,
                    dishType,
                    "Other",
                    ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )

                val mFavDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }

                mFavDishViewModel.insert(randomDishDetails)

                addedToFavorite = true

                mBinding!!.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )

                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(requireActivity())

        mProgressDialog?.let {
            /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
            it.setContentView(R.layout.dialog_custom_progress)

            //Start the dialog and display it on screen.
            it.show()
        }
    }

    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    private fun hideProgressDialog() {
        mProgressDialog?.let {
            it.dismiss()
        }
    }

}