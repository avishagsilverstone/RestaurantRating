package com.intro.restaurant.presentation.ui.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intro.restaurant.R
import com.intro.restaurant.data.newsapi.NewsResponse
import com.intro.restaurant.data.newsapi.RetrofitClient
import com.intro.restaurant.presentation.ui.adapter.NewsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NewsAdapter()
        recyclerView.adapter = adapter

        fetchNews()

        return view
    }

    private fun fetchNews() {
        val country = "iq"
        val apiKey = "647f0c864ea54fc8bdc2c025b1587910"

        RetrofitClient.instance.getTopHeadlines(country, apiKey).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    val newsList = newsResponse?.articles ?: emptyList()
                    adapter.setNewsList(newsList)
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
}
