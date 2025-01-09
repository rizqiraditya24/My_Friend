package com.example.myfriend

import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.crocodic.core.base.activity.CoreActivity
import com.crocodic.core.base.adapter.PaginationAdapter
import com.crocodic.core.extension.openActivity
import com.crocodic.core.extension.toJson
import com.example.myfriend.btn_sheet.FilterProducts
import com.example.myfriend.btn_sheet.ShortingProducts
import com.example.myfriend.data.Friend
import com.example.myfriend.dataApi.DataProduct
import com.example.myfriend.databinding.ActivityItemFriendBinding
import com.example.myfriend.databinding.ActivityMenuHomeBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MenuHomeActivity :  CoreActivity<ActivityMenuHomeBinding, FriendViewModel>(R.layout.activity_menu_home) {

    private var friendList = ArrayList<Friend>()
    private var productList = ArrayList<DataProduct>()

    @Inject
    lateinit var gson: Gson

    private val adapterCore by lazy {
        PaginationAdapter<ActivityItemFriendBinding, DataProduct>(R.layout.activity_item_friend).initItem { position, data ->
            openActivity<DetailProductActivity> {
                val dataProduct = data.toJson(gson)
                putExtra(DetailProductActivity.DATA, dataProduct)
            }
        }
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
                    viewModel.slider.collect { data ->
                        binding.ivSlider.setImageList(data)
                    }
                }

                launch {
                    viewModel.getPagingProducts().collectLatest { data ->
                        adapterCore.submitData(data)
                    }
                }
            }
        }

        viewModel.getSlider()
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