package com.example.findauthor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>
{
    private EditText mBookInput;
    private TextView mTitletext, mAuthorText;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookInput  = (EditText) findViewById(R.id.book)      ;
        mTitletext  = (TextView) findViewById(R.id.titletext) ;
        mAuthorText = (TextView) findViewById(R.id.authortext);

        if(LoaderManager.getInstance(this).getLoader(0) != null)
        {
            LoaderManager.getInstance(this).initLoader(0,null, this);
        }
    }

    public void searchBooks(View view)
    {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputManager != null)
        {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if(connMgr != null)
        {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        String queryString = mBookInput.getText().toString();

        if(networkInfo != null && networkInfo.isConnected() && queryString.length() != 0)
        {
            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString",queryString);
            LoaderManager.getInstance(this).restartLoader(0,queryBundle, this);
        }
        else
        {
            if(queryString.length() == 0)
            {
                mAuthorText.setText(" ");
                mTitletext.setText(R.string.no_search_term);
            }
            else
            {
                mAuthorText.setText(" ");
                mTitletext.setText("Not Connected");
            }
        }
        mAuthorText.setText("");
        mTitletext.setText(R.string.loading);
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args)
    {
        String queryString = " ";

        if(args != null)
        {
            queryString = args.getString("queryString");
        }
        return new BookLoader(this, queryString);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray itemsArray = jsonObject.getJSONArray("items");
            int i = 0;
            String title = null;
            String author = null;

            while(i < itemsArray.length() && (author == null && title == null))
            {
                JSONObject book = itemsArray.getJSONObject(i);
                JSONObject volumeInfo = book.getJSONObject("volumeInfo");
                try
                {
                    title = volumeInfo.getString("title");
                    author = volumeInfo.getString("authors");
                }
                catch (JSONException e)
                {
                    mTitletext.setText(R.string.no_results);
                    mAuthorText.setText(" ");
                    e.printStackTrace();
                }
                i++;
            }

            if(title != null && author != null)
            {
                mTitletext.setText(title);
                mAuthorText.setText(author);
            }
            else
            {
                mTitletext.setText(R.string.no_results);
                mAuthorText.setText(" ");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader)
    {

    }
}