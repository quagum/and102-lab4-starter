package com.codepath.articlesearch

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.articlesearch.databinding.ActivityMainBinding
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Headers
import org.json.JSONException
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

fun createJson() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

private const val TAG = "MainActivity/"
private const val SEARCH_API_KEY = BuildConfig.API_KEY
private const val ARTICLE_SEARCH_URL =
    "https://api.nytimes.com/svc/search/v2/articlesearch.json?api-key=${SEARCH_API_KEY}"

class MainActivity : AppCompatActivity() {
    private lateinit var articlesRecyclerView: RecyclerView
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var binding: ActivityMainBinding
    private val articles = mutableListOf<DisplayArticle>()

    private lateinit var offlineStatus: TextView
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        articlesRecyclerView = findViewById(R.id.articles)
        swipeContainer = findViewById(R.id.swipeContainer)

        offlineStatus = findViewById(R.id.offlineStatus)

        val articleAdapter = ArticleAdapter(this, articles)
        lifecycleScope.launch {
            (application as ArticleApplication).db.articleDao().getAll().collect { databaseList ->
                databaseList.map { entity ->
                    DisplayArticle(
                        entity.headline,
                        entity.articleAbstract,
                        entity.byline,
                        entity.mediaImageUrl
                    )
                }.also { mappedList ->
                    articles.clear()
                    articles.addAll(mappedList)
                    articleAdapter.notifyDataSetChanged()
                }
            }
        }

        articlesRecyclerView.adapter = articleAdapter
        articlesRecyclerView.layoutManager = LinearLayoutManager(this).also {
            val dividerItemDecoration = DividerItemDecoration(this, it.orientation)
            articlesRecyclerView.addItemDecoration(dividerItemDecoration)
        }

        swipeContainer.setOnRefreshListener {
            fetchData(articleAdapter)
        }

        fetchData(articleAdapter)

        // Set up network change receiver
        networkChangeReceiver = NetworkChangeReceiver(
            onNetworkAvailable = {
                offlineStatus.visibility = View.GONE
                Toast.makeText (this, "Network is available", Toast.LENGTH_SHORT).show()
                fetchData(articleAdapter)
            },
            onNetworkUnavailable = {
                offlineStatus.visibility = View.VISIBLE
                Toast.makeText (this, "Network is unavailable", Toast.LENGTH_SHORT).show()
            })
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onDestroy(){
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }

    private fun fetchData(articleAdapter: ArticleAdapter) {
        val client = AsyncHttpClient()
        client.get(ARTICLE_SEARCH_URL, object : JsonHttpResponseHandler() {
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.e(TAG, "Failed to fetch articles: $statusCode")
                swipeContainer.isRefreshing = false
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "Successfully fetched articles: $json")
                try {
                    val parsedJson = createJson().decodeFromString(
                        SearchNewsResponse.serializer(),
                        json.jsonObject.toString()
                    )
                    parsedJson.response?.docs?.let { list ->
                        lifecycleScope.launch(IO) {
                            (application as ArticleApplication).db.articleDao().deleteAll()
                            (application as ArticleApplication).db.articleDao().insertAll(list.map {
                                ArticleEntity(
                                    headline = it.headline?.main,
                                    articleAbstract = it.abstract,
                                    byline = it.byline?.original,
                                    mediaImageUrl = it.mediaImageUrl
                                )
                            })
                        }
                        articles.clear()
                        articles.addAll(list.map {
                            DisplayArticle(
                                it.headline?.main,
                                it.abstract,
                                it.byline?.original,
                                it.mediaImageUrl
                            )
                        })
                        articleAdapter.notifyDataSetChanged()
                        swipeContainer.isRefreshing = false
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Exception: $e")
                    swipeContainer.isRefreshing = false
                }
            }
        })
    }
}
