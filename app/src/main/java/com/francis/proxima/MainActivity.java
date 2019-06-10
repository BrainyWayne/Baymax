package com.francis.proxima;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * *
 * * Francis Eshun 2019
 */

public class MainActivity extends AppCompatActivity implements AIListener {

    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    DatabaseReference mDataBaseReference;
    FirebaseRecyclerAdapter<ChatMessage,chat_rec> adapter;
    Boolean flagFab = true;
    private static int firstVisibleItem;

    RelativeLayout mainContent;
    ImageView defaultback;
    Toolbar toolbar;
    AppCompatImageView proximalogo;

    static int y;

    private AIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        addBtn = findViewById(R.id.addBtn);

        defaultback = findViewById(R.id.default_background_image);

        mainContent = findViewById(R.id.main_content);
        toolbar = findViewById(R.id.toolbar);
        proximalogo = findViewById(R.id.proxima_logo);


        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mDataBaseReference = FirebaseDatabase.getInstance().getReference();
        mDataBaseReference.keepSynced(true);

     /*   new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TransitionManager.beginDelayedTransition(mainContent);
                defaultback.setVisibility(View.GONE);
                toolbar.setBackgroundColor(getResources().getColor(R.color.white));
                proximalogo.setColorFilter(Color.BLACK);

            }
        }, 4000);*/

        final AIConfiguration config = new AIConfiguration("f8bf4ba0266c4de0a1ce7094c6079a55  ",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();

        firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                /*int currentVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                if(currentVisibleItem > firstVisibleItem) {
                    Toast.makeText(MainActivity.this, "Scroll Up", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(MainActivity.this, "Scroll down", Toast.LENGTH_SHORT).show();
                }

                firstVisibleItem = currentVisibleItem;*/

                y = dy;
            }


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                super.onScrollStateChanged(recyclerView, newState);

                if(recyclerView.SCROLL_STATE_DRAGGING == newState){

                }


                //Scroll events for actions when scrolled
                if(recyclerView.SCROLL_STATE_IDLE == newState){
                    if(y <= 0){
                        Toast.makeText(MainActivity.this, "Francis scrolls Up", Toast.LENGTH_SHORT).show();
                    } else {
                        y = 0;
                        Toast.makeText(MainActivity.this, "Francis scrolls down", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        addBtn.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view)
            {
                aiService.startListening();

                String message = editText.getText().toString().trim();

                if (!message.equals(""))
                {

                    ChatMessage chatMessage = new ChatMessage(message, "user");
                    mDataBaseReference.child("chat").push().setValue(chatMessage);

                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest,Void,AIResponse>(){

                        @SuppressLint("WrongThread")
                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                Log.d("DialogFlowAsyncTask","Request" + aiRequest.toString());
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(AIResponse response) {
                            if (response != null) {

                                Result result = response.getResult();
                                String reply = result.getFulfillment().getSpeech();
                                ChatMessage chatMessage = new ChatMessage(reply, "bot");
                                mDataBaseReference.child("chat").push().setValue(chatMessage);
                            }
                        }
                    }.execute(aiRequest);
                }
                else {
                 //   aiService.startListening();
                }

                editText.setText("");

            }
        });



        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = (ImageView)findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(),R.drawable.ic_send_white_24dp);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mic_white_24dp);


                if (s.toString().trim().length()!=0 && flagFab){
                    ImageViewAnimatedChange(MainActivity.this,fab_img,img);
                    flagFab=false;

                }
                else if (s.toString().trim().length()==0){
                    ImageViewAnimatedChange(MainActivity.this,fab_img,img1);
                    flagFab=true;

                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(ChatMessage.class,R.layout.msglist,chat_rec.class,mDataBaseReference.child("chat")) {
            @Override
            protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {

                if (model.getMsgUser().equals("user")) {


                    //setting the user text
                    viewHolder.rightText.setText(model.getMsgText());

                    viewHolder.rightText.setVisibility(View.VISIBLE);
                    viewHolder.leftText.setVisibility(View.GONE);
                }
                else {

                    //setting the text from the NLP
                    viewHolder.leftText.setText(model.getMsgText());

                    viewHolder.rightText.setVisibility(View.GONE);
                    viewHolder.leftText.setVisibility(View.VISIBLE);
                }
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);

                }

            }
        });

        recyclerView.setAdapter(adapter);
    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {

        Log.d("DialogFlow","onResult" + response.toString());

        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "user");
        mDataBaseReference.child("chat").push().setValue(chatMessage0);

        String reply = result.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply, "bot");
        mDataBaseReference.child("chat").push().setValue(chatMessage);

    }

    @Override
    public void onError(ai.api.model.AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        //Will show animations here whilst audio calling
    }

    @Override
    public void onListeningCanceled() {
        //Listening has been cancelled
    }

    @Override
    public void onListeningFinished() {
        //Listening finished
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
