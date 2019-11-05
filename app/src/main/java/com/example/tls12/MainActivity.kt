package com.example.tls12

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val githubService = RestUtil.instance.retrofit.create(GithubApi::class.java)

        // launch a coroutine for doing the network task
        GlobalScope.launch(Dispatchers.Main) {
            // slowdown the process a bit to demonstrate everything in this coroutine is sequential
            // everything outside of this launch block is not blocked by this coroutine
            delay(5000)

            val response  = githubService.getGithubAccount("google").await()
            progress_circular.visibility = View.GONE
            if (response.isSuccessful) {
                if (response.body() != null) {
                    tv_content.text = "${response.body()!!.login} \n ${response.body()!!.createdAt}"
                }
            } else {
                Toast.makeText(applicationContext, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        }

        Toast.makeText(applicationContext, "The above network call in coroutine is not blocking this toast", Toast.LENGTH_LONG).show()

    }

}
