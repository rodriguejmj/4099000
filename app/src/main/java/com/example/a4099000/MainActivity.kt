package com.example.a4099000

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class MainActivity : AppCompatActivity() {

    private lateinit var movieViewModel: MovieViewModel
    private lateinit var movieAdapter: MovieAdapter
    private val apiKey = "e92f38a51904b2c9de79b5fa5fc3adb6"
    private val detailRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movieAdapter = MovieAdapter { movie ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("movie", movie)
            }
            startActivityForResult(intent, detailRequestCode)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = movieAdapter

        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        if (isOnline()) {
            fetchMovies()
        } else {
            loadFavoriteMovies()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == detailRequestCode && resultCode == RESULT_OK) {
            data?.let {
                val updatedMovie = it.getSerializableExtra("updated_movie") as Movie
                movieAdapter.updateMovie(updatedMovie)
                movieViewModel.updateMovie(updatedMovie)
            }
        }
    }

    private fun fetchMovies() {
        RetrofitInstance.api.getPopularMovies(apiKey).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { movieResponse ->
                        movieAdapter.setMovies(movieResponse.results)
                        movieViewModel.setAllMovies(movieResponse.results)
                    }
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Falha ao buscar filmes", Toast.LENGTH_SHORT).show()
                loadFavoriteMovies()  // Fallback to favorite movies if API call fails
            }
        })
    }

    private fun loadFavoriteMovies() {
        movieViewModel.favoriteMovies.observe(this, Observer { movies ->
            movieAdapter.setMovies(movies)
        })
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
