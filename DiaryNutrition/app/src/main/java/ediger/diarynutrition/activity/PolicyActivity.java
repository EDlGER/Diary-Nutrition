package ediger.diarynutrition.activity;

import ediger.diarynutrition.R;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PolicyActivity extends AppCompatActivity {

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);

        getSupportActionBar().setHomeButtonEnabled(true);

        WebView webView = (WebView) findViewById(R.id.web_policy);

        activity = this;

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });
        webView.loadUrl("https://edlger.github.io");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
