package com.example.actionaidactivista;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
public class LibraryFragment extends Fragment {

    private List<library_article> mList;
    private RecyclerView mRecyclerView;
    private library_adapter mLibraryAdapter;

    private Dialog mDialog;
    //retrofit
    private ApiInterface apiInterface;

    public LibraryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = null;
        try {
            // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.fragment_library, container, false);
            //set title
            ((MainBottomNavActivity) getActivity()).setActionBarTitle("Library");
            //initialize widgets
            mRecyclerView = root.findViewById(R.id.library_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(getContext());

            //make sure that options menu show
            setHasOptionsMenu(true);
            //get articles
            getArticles();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading UI", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try{
            inflater.inflate(R.menu.search_menu,menu);
            MenuItem item = menu.findItem(R.id.m_search);
            SearchView searchView = (SearchView)item.getActionView();
            searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(mLibraryAdapter != null) {
                        mLibraryAdapter.getFilter().filter(newText);
                    }
                    return false;
                }
            });
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void getArticles() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, "1");
        RequestBody rows = RequestBody.create(MultipartBody.FORM, "5");
        Call<ResponseBody> articles = apiInterface.getLibrary();
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
                        if(result.length() == 0){
                            Toast.makeText(getContext(), "No more articles.", Toast.LENGTH_SHORT).show();
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
                            mList.add(article);
                        }
                        mLibraryAdapter = new library_adapter(mList,getContext());
                        mRecyclerView.setAdapter(mLibraryAdapter);
                    } else {
                        Toast.makeText(getContext(), "Request unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    methods.showAlert("Error", e.toString(), getContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                methods.showDialog(mDialog, "Dismiss", false);
                methods.showAlert("List onFailure", t.toString(), getContext());
            }
        });
    }
}
