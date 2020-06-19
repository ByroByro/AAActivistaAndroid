package com.example.actionaidactivista;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
public class ContactFragment extends Fragment {

    private List<contact> mList;
    private RecyclerView mRecyclerView;
    private contact_adapter mContactAdapter;
    private Dialog mDialog;

    //retrofit
    private ApiInterface apiInterface;

    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = null;
        try {
            // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.fragment_contact, container, false);
            //set title
            //initialise api interface
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            ((MainBottomNavActivity) getActivity()).setActionBarTitle("Activists");
            //initialize widgets
            mRecyclerView = root.findViewById(R.id.contact_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mDialog = new Dialog(getContext());

            //get activistas
            getActivistas();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading UI.", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    private void getActivistas() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, "1");
        RequestBody rows = RequestBody.create(MultipartBody.FORM, "5");
        Call<ResponseBody> activistas = apiInterface.getActivistas();
        methods.showDialog(mDialog, "Loading activistas...", true);
        activistas.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if(result.length() == 0){
                            Toast.makeText(getContext(), "No more activistas.", Toast.LENGTH_SHORT).show();
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
                            mList.add(contact);
                        }

                        mContactAdapter = new contact_adapter(mList, getContext());
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
                methods.showAlert("List onFailure", t.toString(), getContext());
            }
        });
    }
}
