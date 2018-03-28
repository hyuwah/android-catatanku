package io.github.hyuwah.catatanku;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.styles.Github;
import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorMarkdownActivity extends AppCompatActivity {

  @BindView(R.id.editor_markdown_view)
  MarkdownView markdownView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor_markdown);

    ButterKnife.bind(this);

    Bundle bundle = getIntent().getExtras();
    String mTitle = bundle.getString("TITLE");
    String mBody = bundle.getString("BODY");

    // TODO make bundle into variable, handle lifecycle change

    // TODO Handle Empty view?
    if(TextUtils.isEmpty(mTitle)){
      setTitle("Untitled");
    }else{
      setTitle(mTitle);
    }

    markdownView.addStyleSheet(new Github());
    markdownView.loadMarkdown(bundle.getString("BODY"));

  }
}
