package com.example.myfriend

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myfriend.dataApi.DataProduct
import com.example.myfriend.databinding.ActivityMenuHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuHomeBinding
    private lateinit var adapter: FriendAdapter
    private val viewModel: FriendViewModel by viewModels()
    private var productList = ArrayList<DataProduct>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        adapter = FriendAdapter(emptyList()) { friend ->
//            val intent = Intent(this, DetailFriendActivity::class.java).apply {
//                putExtra("EXTRA_NAME", friend.name)
//                putExtra("EXTRA_SCHOOL", friend.school)
//                putExtra("EXTRA_IMAGE_PATH", friend.photoPath)
//                putExtra("EXTRA_ID", friend.id)
//            }
//            startActivity(intent)
//        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.product.collect { data ->
                    productList.clear()
                    productList.addAll(data)
                    adapter.updateData(productList) // Corrected method call
                }
            }
        }



        adapter = FriendAdapter(emptyList()) { product ->
//            val intent = Intent(this, DetailProductActivity::class.java).apply {
//                putExtra("EXTRA_PRODUCT_NAME", product.title)
//                putExtra("EXTRA_PRODUCT_ID", product.id)
//            }
//            startActivity(intent)
        }



        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.getProduct()
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchFriends(s.toString())  // Memanggil fungsi pencarian
            }
            override fun afterTextChanged(s: Editable?) {}
        })

//        binding.btnAddFriend.setOnClickListener {
//            val intent = Intent(this, AddFriendActivity::class.java)
//            startActivity(intent)
//        }
    }


    private fun searchFriends(keyword: String) {
        lifecycleScope.launch {
            viewModel.searchProducts(keyword).collect { results ->
                if (results.isEmpty() && keyword.isNotEmpty()) {
                    binding.noDataLayout.visibility = View.VISIBLE
                    adapter.updateData(emptyList())
                } else {
                    binding.noDataLayout.visibility = View.GONE
                    adapter.updateData(results)
                }
            }
        }
    }

}
