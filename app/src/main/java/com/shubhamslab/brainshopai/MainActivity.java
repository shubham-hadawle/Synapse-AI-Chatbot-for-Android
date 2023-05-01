package com.shubhamslab.brainshopai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

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

    AdView bannerAdView;
    InterstitialAd mInterstitialAd;
    long seconds = 50;
    boolean appIsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appIsActive = true;

        // Initializing Google AdMob's SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        bannerAdView = findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked();
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                super.onAdFailedToLoad(adError);
                bannerAdView.loadAd(adRequest);
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                super.onAdOpened();
            }
        });

        loadInterstitialAds();
        showInterstitialAds();
        
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

    // Loading Interstitial Advertisements
    private void loadInterstitialAds() {
        AdRequest adRequestForInterstitial = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-1680148931180397/2446192529", adRequestForInterstitial,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        loadInterstitialAds();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;

                    }
                });
    }

    // Showing Interstitial Advertisements
    private void showInterstitialAds() {
        if(appIsActive){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(appIsActive){
                                if(mInterstitialAd != null){
                                    mInterstitialAd.show(MainActivity.this);
                                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                                        @Override
                                        public void onAdClicked() {
                                            // Called when a click is recorded for an ad.
                                        }

                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            // Called when ad is dismissed.
                                            // Set the ad reference to null so you don't show the ad a second time.
                                            mInterstitialAd = null;
                                            loadInterstitialAds();
                                            showInterstitialAds();
                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                                            // Called when ad fails to show.
                                            mInterstitialAd = null;
                                            loadInterstitialAds();
                                            showInterstitialAds();
                                        }

                                        @Override
                                        public void onAdImpression() {
                                            // Called when an impression is recorded for an ad.
                                        }

                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            // Called when ad is shown.
                                        }
                                    });
                                } else {
                                    showInterstitialAds();
                                }
                            }
                        }
                    });
                }
            }, seconds*1000);
        }
    }

//    // Setting appIsActive to 'false' if the App is Stopped
//    @Override
//    protected void onStop() {
//        super.onStop();
//        appIsActive = false;
//    }
}