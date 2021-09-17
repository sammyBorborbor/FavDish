package com.sammy.favdish.view.adapters

import android.content.Intent
import android.provider.SyncStateContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sammy.favdish.R
import com.sammy.favdish.databinding.ItemDishLayoutBinding
import com.sammy.favdish.model.entities.FavDish
import com.sammy.favdish.utils.Constants
import com.sammy.favdish.view.activities.AddUpdateDishActivity
import com.sammy.favdish.view.fragments.AllDishesFragment
import com.sammy.favdish.view.fragments.FavoriteDishesFragment

class FavDishAdapter(private val fragment: Fragment) :
    RecyclerView.Adapter<FavDishAdapter.ViewHolder>() {

    private var dishes: List<FavDish> = listOf()

    class ViewHolder(view: ItemDishLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        val ivDishImage = view.ivDishImage
        val tvTitle = view.tvDishTitle
        val ibMore = view.ibMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDishLayoutBinding = ItemDishLayoutBinding.inflate(
            LayoutInflater.from(fragment.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dish = dishes[position]
        Glide.with(fragment)
            .load(dish.image)
            .into(holder.ivDishImage)

        holder.tvTitle.text = dish.title
        holder.itemView.setOnClickListener {
            if(fragment is AllDishesFragment) {
                fragment.gotoDishDetails(dish)
            }
            if(fragment is FavoriteDishesFragment) {
                fragment.gotoDishDetails(dish)
            }
        }

        holder.ibMore.setOnClickListener {
            val popupMenu = PopupMenu(fragment.context, holder.ibMore)
            popupMenu.menuInflater.inflate(R.menu.menu_adapter, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                if(it.itemId == R.id.action_edit_dish) {
                    val intent = Intent(fragment.requireActivity(), AddUpdateDishActivity::class.java)
                    intent.putExtra(Constants.EXTRA_DISH_DETAILS, dish)
                    fragment.requireActivity().startActivity(intent)

                } else if(it.itemId == R.id.action_delete_dish) {
                    if (fragment is AllDishesFragment) {
                        fragment.deleteDish(dish)
                    }
                }
                true
            }

            if(fragment is AllDishesFragment) {
                holder.ibMore.visibility = View.VISIBLE
            } else {
                holder.ibMore.visibility = View.GONE
            }

            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return dishes.size
    }

    fun dishesList(list: List<FavDish>) {
        dishes = list
        notifyDataSetChanged()
    }
}