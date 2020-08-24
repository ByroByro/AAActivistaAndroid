package com.example.actionaidactivista;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.example.actionaidactivista.adapters.library_adapter;
import com.example.actionaidactivista.adapters.opportunity_adapter;
import com.example.actionaidactivista.models.library_article;
import com.example.actionaidactivista.models.opportunity;
import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class LibraryFragment extends AppCompatActivity {

    private List<library_article> mList;
    private RecyclerView mRecyclerView;
    private library_adapter mLibraryAdapter;

    private Dialog mDialog;
    //retrofit
    private ApiInterface apiInterface;

    //variables for pagination
    private boolean isLoading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount, previousTotal = 0;
    private int view_threshold = 0;
    private int row_num = 30;
    private int page_num = 1;

    public LibraryFragment() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Inflate the layout for this fragment
            setContentView(R.layout.fragment_library);
            //set title
            getSupportActionBar().setTitle("Library");
            //initialize widgets
            mRecyclerView = findViewById(R.id.library_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(this);

            //on scroll listener for recycler view
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (dy > 0) {
                        if (isLoading) {
                            if (totalItemCount > previousTotal) {
                                isLoading = false;
                                previousTotal = totalItemCount;
                            }
                        }

                        if (!isLoading && (totalItemCount - visibleItemCount) <= (pastVisibleItems + view_threshold)) {
                            page_num++;
                            getArticlesPagination();
                            isLoading = true;
                        }
                    }
                }
            });
            //make sure that options menu show
            //setHasOptionsMenu(true);
            //get articles
            getArticles();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        super.onCreateOptionsMenu(menu);
        try {
            getMenuInflater().inflate(R.menu.search_menu, menu);
            MenuItem item = menu.findItem(R.id.m_search);
            SearchView searchView = (SearchView) item.getActionView();
            searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (mLibraryAdapter != null) {
                        mLibraryAdapter.getFilter().filter(newText);
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {

        MenuItem item = menu.findItem(R.id.m_refresh);
        item.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    //get library material
    private void getArticles() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
        RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
        Call<ResponseBody> articles = apiInterface.getLibrary(rows, page);
        methods.showDialog(mDialog, "Loading articles...", true);
        articles.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if (result.length() == 0) {
                            Toast.makeText(LibraryFragment.this, "No articles.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        mList = new ArrayList<>();
                        library_article article;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            article = new library_article();
                            article.setmArticleID(jsonObject.getString("id"));
                            article.setmTitle(jsonObject.getString("title"));
                            article.setmAuthor(jsonObject.getString("author"));
                            article.setmDate(jsonObject.getString("dateposted"));
                            article.setmFileType(jsonObject.getString("filetype"));
                            article.setmPath(jsonObject.getString("path"));
                            article.setmUrl(jsonObject.getString("url"));
                            article.setmIntType(jsonObject.getString("inttype"));
                            mList.add(article);
                        }
                        mLibraryAdapter = new library_adapter(mList, LibraryFragment.this);
                        mRecyclerView.setAdapter(mLibraryAdapter);
                    } else {
                        Toast.makeText(LibraryFragment.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    methods.showAlert("Error", e.toString(), LibraryFragment.this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                methods.showDialog(mDialog, "Dismiss", false);
                methods.showAlert("Failure", "Request failed.Check your internet connection.", LibraryFragment.this);
            }
        });
    }

    //get library material pagination
    private void getArticlesPagination() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
        RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
        Call<ResponseBody> articles = apiInterface.getLibrary(rows, page);
        methods.showDialog(mDialog, "Loading more articles...", true);
        articles.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if (result.length() == 0) {
                            Toast.makeText(LibraryFragment.this, "No more articles.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        List<library_article> list = new ArrayList<>();
                        library_article article;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            article = new library_article();
                            article.setmArticleID(jsonObject.getString("id"));
                            article.setmTitle(jsonObject.getString("title"));
                            article.setmAuthor(jsonObject.getString("author"));
                            article.setmDate(jsonObject.getString("dateposted"));
                            article.setmFileType(jsonObject.getString("filetype"));
                            article.setmPath(jsonObject.getString("path"));
                            article.setmUrl(jsonObject.getString("url"));
                            article.setmIntType(jsonObject.getString("inttype"));
                            mList.add(article);
                        }

                        //add to list
                        mLibraryAdapter.addArticle(list);
                    } else {
                        Toast.makeText(LibraryFragment.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    methods.showAlert("Error", e.toString(), LibraryFragment.this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                methods.showDialog(mDialog, "Dismiss", false);
                methods.showAlert("Failure", "Request failed.Check your network.", LibraryFragment.this);
            }
        });
    }

    @Override
    public void onDestroy() {
        try {
            mLibraryAdapter.releaseExoPlayer();
        } catch (Exception e) {
            System.out.println(e);
        }
        //Toast.makeText(getContext(),"Library Fragment onDestroy.",Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        try {
            mLibraryAdapter.releaseExoPlayer();
        } catch (Exception e) {
            System.out.println(e);
        }
        super.onBackPressed();
    }
}
