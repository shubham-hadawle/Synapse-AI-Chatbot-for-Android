package com.example.brainshopai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messagingEditText;
    ImageButton sendButton;
    List<ChatsModel> messageList;
    MessageAdapter messageAdapter;
    Context context;

    ImageView welcomeLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messagingEditText = findViewById(R.id.messaging_edit_text);
        sendButton = findViewById(R.id.send_button);
        welcomeLogo = findViewById(R.id.welcome_logo);

        //Prompting a Toast if Internet is Not Connected
        if(!isConnected()) {
            Toast.makeText(this, "\t\t\t\t\t\t\t No Internet Connectivity.\nPlease turn your Internet Connection on.", Toast.LENGTH_LONG).show();
        }

        // Setting-up the RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        // On-Click-Listener for Send Button
        sendButton.setOnClickListener((view) -> {
            welcomeTextView.setVisibility(View.GONE);
            welcomeLogo.setVisibility(View.GONE);

            String usersText = messagingEditText.getText().toString().trim();
            addToChat(usersText, ChatsModel.SENT_BY_USER);
            messagingEditText.setText("");

            // An empty message should not be sent by the user
            if(usersText.isEmpty()) {
                Toast.makeText(this, "Please enter a Valid Message.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            callAPI(usersText);
        });

    }

    private void callAPI(String usersText) {
        String url = "http://api.brainshop.ai/get?bid=174488&key=sXoqagV2bLoraSXu&uid=user1&msg=" + usersText;
        String BASE_URL = "http://api.brainshop.ai/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        Call<MessageModel> call = retrofitAPI.getReply(url);
        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(Call<MessageModel> call, Response<MessageModel> response) {
                if(response.isSuccessful()) {
                    MessageModel mm = response.body();
                    addToChat(mm.getCnt(), ChatsModel.SENT_BY_CHATBOT);
                }
            }

            @Override
            public void onFailure(Call<MessageModel> call, Throwable t) {
                String errorMessage = "Failed to generate a Response due to " + t.getMessage() + "\n\nTry asking again OR check your Connection!";
                addToChat(errorMessage, ChatsModel.SENT_BY_CHATBOT);
            }
        });
    }

    private void addToChat(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new ChatsModel(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    // Checking if Internet is Connected
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}