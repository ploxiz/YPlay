package com.yplay.search;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yplay.BaseActivity;
import com.yplay.R;
import com.yplay.search.exceptions.NoResultsException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private List<MediaObject> searchResults = new ArrayList<>();
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SearchView searchView = findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchResults.clear();
                initRetryButton(query);
                new SearchAsyncTask().execute(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        showKeyboardByForce(); // TODO: find a proper way of showing the keyboard instead of forcing it.

        recyclerView = findViewById(R.id.search_recyclerView);
        recyclerView.setAdapter(new SearchAdapter(this, searchResults));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.search_progressBar);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_search;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.navigation_search;
    }

    private void showKeyboardByForce() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void initRetryButton(final String query) {
        final Button retryButton = findViewById(R.id.search_retry_button);
        final TextView feedbackTextView = findViewById(R.id.search_feedback_textView);

        retryButton.setVisibility(View.INVISIBLE);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryButton.setVisibility(View.INVISIBLE);
                feedbackTextView.setVisibility(View.INVISIBLE);
                new SearchAsyncTask().execute(query);
            }
        });

        feedbackTextView.setVisibility(View.INVISIBLE);
    }

    private class SearchAsyncTask extends AsyncTask<String, Void, Integer> {

        private static final int SUCCESS = 0;
        private static final int NO_RESULTS = 1;
        private static final int UNKNOWN_HOST = 2;
        private static final int ERROR_OCCURRED = 3;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                searchResults = SearchTask.fetchSearchResults(params[0]);
            } catch (UnknownHostException e) {
                return UNKNOWN_HOST;
            } catch (IOException e) {
                return ERROR_OCCURRED;
            } catch (NoResultsException e) {
                return NO_RESULTS;
            }

            return SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer exitCode) {
            progressBar.setVisibility(View.INVISIBLE);

            TextView feedbackTextView = findViewById(R.id.search_feedback_textView);
            Button retryButton = findViewById(R.id.search_retry_button);

            switch (exitCode) {
                case NO_RESULTS:
                    feedbackTextView.setText(getResources().getText(R.string.no_result));
                    feedbackTextView.setVisibility(View.VISIBLE);
                    return;

                case UNKNOWN_HOST: // basically no connection
                    feedbackTextView.setText(getResources().getText(R.string.no_connection));
                    feedbackTextView.setVisibility(View.VISIBLE);
                    retryButton.setVisibility(View.VISIBLE);
                    return;

                case ERROR_OCCURRED:
                    feedbackTextView.setText(getResources().getText(R.string.error));
                    feedbackTextView.setVisibility(View.VISIBLE);
                    retryButton.setVisibility(View.VISIBLE);
                    return;
            }

            recyclerView.setAdapter(new SearchAdapter(SearchActivity.this, searchResults));
        }
    }
}
