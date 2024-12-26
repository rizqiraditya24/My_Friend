package com.example.myfriend

import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.crocodic.core.base.activity.CoreActivity
import com.crocodic.core.base.adapter.PaginationAdapter
import com.example.myfriend.btn_sheet.FilterProducts
import com.example.myfriend.btn_sheet.ShortingProducts
import com.example.myfriend.data.Friend
import com.example.myfriend.dataApi.DataProduct
import com.example.myfriend.databinding.ActivityItemFriendBinding
import com.example.myfriend.databinding.ActivityMenuHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuHomeActivity :  CoreActivity<ActivityMenuHomeBinding, FriendViewModel>(R.layout.activity_menu_home) {

    private var friendList = ArrayList<Friend>()
    private var productList = ArrayList<DataProduct>()

    private val adapterCore by lazy {
        PaginationAdapter<ActivityItemFriendBinding, DataProduct>(R.layout.activity_item_friend)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)

        val adapterWithFooter = adapterCore.withLoadStateFooter(
            footer = LoadingAdapter()
        )
        binding.recyclerView.adapter = adapterWithFooter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getPagingProducts().collectLatest { data ->
                        adapterCore.submitData(data)
                    }
                }
            }
        }

        binding.searchBar.doOnTextChanged { text, start, before, count ->
            val keyword = "%${text.toString().trim()}%"
            viewModel.getProduct(keyword)
        }

        binding.btnFilter.setOnClickListener {
            val btmSht = FilterProducts { filter ->
                viewModel.filterProducts(filter)
            }
            btmSht.show(supportFragmentManager, "BtmShtFilteringProducts")
        }

        binding.btnSort.setOnClickListener {
            val btmSht = ShortingProducts { sortBy, order ->
                viewModel.sortProducts(sortBy, order)
            }
            btmSht.show(supportFragmentManager, "BtmShtSortingProducts")
        }
    }


}


