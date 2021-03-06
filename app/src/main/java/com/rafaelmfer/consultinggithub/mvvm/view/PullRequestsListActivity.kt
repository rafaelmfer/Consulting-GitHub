package com.rafaelmfer.consultinggithub.mvvm.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.rafaelmfer.consultinggithub.R
import com.rafaelmfer.consultinggithub.mvvm.ViewModelFactory
import com.rafaelmfer.consultinggithub.mvvm.viewmodel.GitHubRepositoriesViewModel
import kotlinx.android.synthetic.main.activity_pull_requests_list.*
import kotlinx.android.synthetic.main.content_pull_requests_list.*

class PullRequestsListActivity : AppCompatActivity(), OnClickListenerGitHub {

    private val gitHubRepositoriesViewModel by lazy { ViewModelProviders.of(this,
        ViewModelFactory()
    ).get(GitHubRepositoriesViewModel::class.java) }
    private val page = 1
    var gitHubPullRequestsOpened = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pull_requests_list)
        val creator = intent.extras?.getString("creator")
        val repositoryId = intent.extras?.getString("repoId")
        toolbarPullRequests.title = repositoryId
        setSupportActionBar(toolbarPullRequests)
        toolbarPullRequests.setNavigationOnClickListener { onBackPressed() }



        setList()
        setObservers()
        if (creator != null && repositoryId != null) {
            gitHubRepositoriesViewModel.getPullRequestsList(creator, repositoryId, page)
        }


    }

    override fun onClickOpenWeb(htmlUrl: String) {
        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(htmlUrl)))
    }

    private fun setList() {
        rvPullRequestsList.apply {
            layoutManager = LinearLayoutManager(this@PullRequestsListActivity)
            adapter = PullRequestsListAdapter(this@PullRequestsListActivity, emptyList(), this@PullRequestsListActivity)
        }
    }

    private fun setObservers() {
        gitHubRepositoriesViewModel.command.observe(this, Observer {
            when (it) {
                is GitHubRepositoriesViewModel.Command.ShowLoading -> showLoading(true)
                is GitHubRepositoriesViewModel.Command.HideLoading -> showLoading(false)
            }
        })

        gitHubRepositoriesViewModel.gitHubPullRequestsList.observe(this, Observer { pullRequestsList ->
            if (pullRequestsList != null) {
                rvPullRequestsList.adapter = PullRequestsListAdapter(this@PullRequestsListActivity, pullRequestsList, this)
                for (item in pullRequestsList) {
                    if (item.state == "open") {
                        gitHubPullRequestsOpened++
                    }
                }
                tvPullRequestsCountOpened.text = getString(R.string.pull_requests_opened, gitHubPullRequestsOpened)
            }
        })

        gitHubRepositoriesViewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(this, R.string.error_message, Toast.LENGTH_LONG).show()
        })
    }

    private fun showLoading(visible: Boolean) {
        rvPullRequestsList.visibility = if (visible) View.GONE else View.VISIBLE
    }
}