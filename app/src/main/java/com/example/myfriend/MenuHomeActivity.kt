package com.example.myfriend

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myfriend.databinding.ActivityMenuHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuHomeBinding
    private lateinit var adapter: FriendAdapter
    private val viewModel: FriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = FriendAdapter(emptyList()) { _ ->
            // Tambahkan logika klik item di sini
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.getProduct()
        }

        lifecycleScope.launch {
            viewModel.product.collect { data ->
                adapter.updateData(data)
                binding.noDataLayout.visibility = View.GONE // Tidak tampil sebelum pencarian
            }
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchFriends(s.toString()) // Panggil fungsi pencarian
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchFriends(keyword: String) {
        lifecycleScope.launch {
            viewModel.product.collect { allProducts ->
                if (keyword.isEmpty()) {
                    // Jika keyword kosong, tampilkan semua produk
                    adapter.updateData(allProducts)
                    binding.noDataLayout.visibility = View.GONE // Jangan tampilkan "No Data"
                } else {
                    // Jika ada keyword, lakukan pencarian
                    val filteredProducts = allProducts.filter { product ->
                        product.title.contains(keyword, ignoreCase = true) ||
                                product.description.contains(keyword, ignoreCase = true)
                    }
                    adapter.updateData(filteredProducts)
                    binding.noDataLayout.visibility = if (filteredProducts.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}


