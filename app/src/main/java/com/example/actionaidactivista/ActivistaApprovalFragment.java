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

import com.example.actionaidactivista.adapters.activista_approval_adapter;
import com.example.actionaidactivista.adapters.contact_adapter;
import com.example.actionaidactivista.models.contact;
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
public class ActivistaApprovalFragment extends Fragment {

    private List<contact> mList;
    private RecyclerView mRecyclerView;
    private activista_approval_adapter mContactAdapter;
    private Dialog mDialog;

    //pagination variables
    private boolean isLoading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount, previousTotal = 0;
    private int view_threshold = 0;
    private int row_num = 30;
    private int page_num = 1;

    //retrofit
    private ApiInterface apiInterface;

    public ActivistaApprovalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = null;
        try {
            // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.fragment_activista_approval, container, false);
            //set title
            ((MainBottomNavActivity) getActivity()).setActionBarTitle("Activista Approval");
            //initialise api interface
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            //initialize widgets
            mRecyclerView = root.findViewById(R.id.contact_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mDialog = new Dialog(getContext());

            //on scroll listener
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
                            getActivistasPagination();
                            isLoading = true;
                        }
                    }
                }
            });

            setHasOptionsMenu(true);
            //get members
            getActivistas();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading UI.", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    //get members
    private void getActivistas() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
        RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
        Call<ResponseBody> activistas = apiInterface.getActivistasForApproval(rows,page);
        methods.showDialog(mDialog, "Loading applications...", true);
        activistas.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if (result.length() == 0) {
                            Toast.makeText(getContext(), "No more applications.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        mList = new ArrayList<>();
                        contact contact;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            contact = new contact();
                            contact.setmContactID(jsonObject.getString("id"));
                            contact.setmName(jsonObject.getString("name"));
                            contact.setmSurname(jsonObject.getString("surname"));
                            contact.setmDob(jsonObject.getString("dob"));
                            contact.setmGender(jsonObject.getString("gender"));
                            contact.setmOccupation(jsonObject.getString("occupation"));
                            contact.setmProvid(jsonObject.getString("provid"));
                            contact.setmDisid(jsonObject.getString("disid"));
                            contact.setmAccountNo(jsonObject.getString("accno"));
                            contact.setmProfileUrl(jsonObject.getString("profile"));
                            contact.setmStatus(jsonObject.getString("approved"));
                            if (jsonObject.getString("email").equalsIgnoreCase("")) {
                                contact.setmEmail("N/A");
                            } else {
                                contact.setmEmail(jsonObject.getString("email"));
                            }
                            if (jsonObject.getString("biography").equalsIgnoreCase("")) {
                                contact.setmBio("N/A");
                            } else {
                                contact.setmBio(jsonObject.getString("biography"));
                            }
                            if (jsonObject.getString("isdobpublic").equalsIgnoreCase("")) {
                                contact.setmDobPublic("N/A");
                            } else {
                                contact.setmDobPublic(jsonObject.getString("isdobpublic"));
                            }
                            if (jsonObject.getString("phone").equalsIgnoreCase("")) {
                                contact.setmPhone("N/A");
                            } else {
                                contact.setmPhone(jsonObject.getString("phone"));
                            }
                            if (jsonObject.getString("join_date").equalsIgnoreCase("")) {
                                contact.setmJoin_date("N/A");
                            } else {
                                contact.setmJoin_date(jsonObject.getString("join_date"));
                            }
                            if (jsonObject.getString("doc_url").equalsIgnoreCase("")) {
                                contact.setmDoc_url("N/A");
                            } else {
                                contact.setmDoc_url(jsonObject.getString("doc_url"));
                            }
                            if (jsonObject.getString("doc_path").equalsIgnoreCase("")) {
                                contact.setmDoc_path("N/A");
                            } else {
                                contact.setmDoc_path(jsonObject.getString("doc_path"));
                            }
                            if (jsonObject.getString("doc_type").equalsIgnoreCase("")) {
                                contact.setmDoc_type("N/A");
                            } else {
                                contact.setmDoc_type(jsonObject.getString("doc_type"));
                            }
                            mList.add(contact);
                        }

                        mContactAdapter = new activista_approval_adapter(mList, getContext());
                        mRecyclerView.setAdapter(mContactAdapter);
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
                methods.showAlert("Failure", "Request failed.Check your internet connection.", getContext());
            }
        });
    }

    //get more members
    private void getActivistasPagination() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
        RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
        Call<ResponseBody> activistas = apiInterface.getActivistasForApproval(rows,page);
        methods.showDialog(mDialog, "Loading applications...", true);
        activistas.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if (result.length() == 0) {
                            Toast.makeText(getContext(), "No more applications.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        List<contact> list = new ArrayList<>();
                        contact contact;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            contact = new contact();
                            contact.setmContactID(jsonObject.getString("id"));
                            contact.setmName(jsonObject.getString("name"));
                            contact.setmSurname(jsonObject.getString("surname"));
                            contact.setmDob(jsonObject.getString("dob"));
                            contact.setmGender(jsonObject.getString("gender"));
                            contact.setmOccupation(jsonObject.getString("occupation"));
                            contact.setmProvid(jsonObject.getString("provid"));
                            contact.setmDisid(jsonObject.getString("disid"));
                            contact.setmAccountNo(jsonObject.getString("accno"));
                            contact.setmProfileUrl(jsonObject.getString("profile"));
                            contact.setmStatus(jsonObject.getString("approved"));
                            if (jsonObject.getString("email").equalsIgnoreCase("")) {
                                contact.setmEmail("N/A");
                            } else {
                                contact.setmEmail(jsonObject.getString("email"));
                            }
                            if (jsonObject.getString("biography").equalsIgnoreCase("")) {
                                contact.setmBio("N/A");
                            } else {
                                contact.setmBio(jsonObject.getString("biography"));
                            }
                            if (jsonObject.getString("isdobpublic").equalsIgnoreCase("")) {
                                contact.setmDobPublic("N/A");
                            } else {
                                contact.setmDobPublic(jsonObject.getString("isdobpublic"));
                            }
                            if (jsonObject.getString("phone").equalsIgnoreCase("")) {
                                contact.setmPhone("N/A");
                            } else {
                                contact.setmPhone(jsonObject.getString("phone"));
                            }
                            if (jsonObject.getString("join_date").equalsIgnoreCase("")) {
                                contact.setmJoin_date("N/A");
                            } else {
                                contact.setmJoin_date(jsonObject.getString("join_date"));
                            }
                            if (jsonObject.getString("doc_url").equalsIgnoreCase("")) {
                                contact.setmDoc_url("N/A");
                            } else {
                                contact.setmDoc_url(jsonObject.getString("doc_url"));
                            }
                            if (jsonObject.getString("doc_path").equalsIgnoreCase("")) {
                                contact.setmDoc_path("N/A");
                            } else {
                                contact.setmDoc_path(jsonObject.getString("doc_path"));
                            }
                            if (jsonObject.getString("doc_type").equalsIgnoreCase("")) {
                                contact.setmDoc_type("N/A");
                            } else {
                                contact.setmDoc_type(jsonObject.getString("doc_type"));
                            }
                            list.add(contact);
                        }

                        //add the new members to the list
                        mContactAdapter.addMember(list);

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
                methods.showAlert("Failure", "Request failed.Check your internet connection.", getContext());
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {
            inflater.inflate(R.menu.search_menu, menu);
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
                    if (mContactAdapter != null) {
                        mContactAdapter.getFilter().filter(newText);
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {

        MenuItem item = menu.findItem(R.id.m_refresh);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}
